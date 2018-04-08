package com.baddog.optionsscanner;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import org.apache.commons.math3.stat.regression.SimpleRegression;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

import static android.content.ContentValues.TAG;


public class OptionAnalysisService extends Service {

    String OAUTH_TOKEN = null;
    String apiServer;
    boolean NeedHistory;
    boolean NeedOptions;

    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;

    private int LOOK_BACK = 250;
    private double SMOOTHING_STD_DEVIATIONS = 5.0;
    private int MIN_DAYS_TILL_EXPIRY = 4;
    private int MAX_DAYS_TILL_EXPIRY = 16;
    private double PERCENT_STRIKE_RANGE = 4.0;
    private double COST_OF_VOLATILITY_LIMIT = .5;
    private boolean USE_TREND_BIAS = false;
    private double TARGET_TRADE_VALUE = 2000;

    private Realm realm;


    @Override
    public void onCreate() {
        // To avoid cpu-blocking, we create a background handler to run our service
        HandlerThread thread = new HandlerThread("TutorialService",
                Process.THREAD_PRIORITY_BACKGROUND);
        // start the new handler thread
        thread.start();

        mServiceLooper = thread.getLooper();
        // start the service using the background handler
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent serviceIntent, int flags, int startId) {
        apiServer = serviceIntent.getStringExtra("apiserver");
        OAUTH_TOKEN = serviceIntent.getStringExtra("oauthtoken");
        NeedHistory = serviceIntent.getBooleanExtra("NeedHistory", false);
        NeedOptions = serviceIntent.getBooleanExtra("NeedOptions", false);

        //android.os.Debug.waitForDebugger();
        Toast.makeText(this, "onStartCommand", Toast.LENGTH_SHORT).show();

        // call a new service handler. The service ID can be used to identify the service
        Message message = mServiceHandler.obtainMessage();
        message.arg1 = startId;
        mServiceHandler.sendMessage(message);

        return Service.START_NOT_STICKY;
    }

    // Object responsible for
    private final class ServiceHandler extends Handler {

        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            // While calling mServiceHandler.sendMessage(message); from onStartCommand,
            // this method will be called.
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(MainActivity.MessageFromService.ACTION_RESP);
            // Initialization of realm database
            realm = Realm.getDefaultInstance(); // opens "myrealm.realm"


            while (true) {
                Thread.yield();
                getHistory(realm, broadcastIntent);
                Thread.yield();
                getOptions(realm, broadcastIntent);
                Thread.yield();
                updateOptions(realm, broadcastIntent);
            }
            //realm.close();
            // Stop the service
            //stopSelf();
        }
    }

    public void getHistory(Realm realm, Intent broadcastIntent) {
        // Initialization of realm database
        // Realm realm = Realm.getDefaultInstance(); // opens "myrealm.realm"
        // Update realm.symbols with symbolID's
        RealmResults<Symbols> sym = realm.where(Symbols.class).findAll();

        // Update realm.symbols with LastTradePrice
        getSymbolPriceInfo(sym);

        if (NeedHistory) {
            // Collect History. Scan through all the symbols *****************************************************************************
            getSymbolVolatilityInfo(sym);

            Thread.yield();
            broadcastIntent.putExtra("status", "History Complete");
            sendBroadcast(broadcastIntent);
        }
        NeedHistory = false;
    }

    public void getOptions(Realm realm, Intent broadcastIntent) {
        // counter to set Strategy.strategyID
        int strategyIDCounter = 1;

        // Scan through symbolID's and populate options data *******************************************************
        RealmResults<Symbols> sym = realm.where(Symbols.class).findAll();
        if (sym.isLoaded()) {
            if (NeedOptions) {
                int symbolID;
                // Scan through all underlying symbols, obtain all the options associated with each underlying symbol.
                // -------- Underlying Symbols
                for (int i = 0; i < sym.size(); i++) {
                    Symbols symbl = sym.get(i);
                    symbolID = symbl.getSymbolID();

                    Thread.yield();
                    broadcastIntent.putExtra("status", "Options of " + symbl.getSymbol());
                    sendBroadcast(broadcastIntent);
                    double symbolLastTradePrice = symbl.getLastTradePrice();
                    OptionsData OptionsDataJSON = new OptionsData(apiServer, OAUTH_TOKEN, symbolID);
                    try {
                        OptionsDataJSON.run();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    // OptionsDataJSON contains all the options for a single underlying symbol (sym)
                    DateCalc ds = new DateCalc();
                    double maxStrike = symbolLastTradePrice * (PERCENT_STRIKE_RANGE / 100 + 1);
                    double minStrike = symbolLastTradePrice / (PERCENT_STRIKE_RANGE / 100 + 1);
                    long investmentDays = 0;
                    long tradeDays = 0;
                    // Scan through the Options Expiry dates for a single underlying symbol.
                    if (OptionsDataJSON.getExpiryDateCount() > 0) {
                        for (int j = 0; j < OptionsDataJSON.getExpiryDateCount(); j++) {
                            // Get Expiry dates
                            OptionsJSON.OptionExpiryDateJSON ExpiryDateJSON = OptionsDataJSON.getExpiryDate(j);

                            long LongExpiryDate = ds.StrDate2LongDate(ExpiryDateJSON.expiryDate);
                            TradeDateCalc tdc = new TradeDateCalc();
                            investmentDays = tdc.InvestmentDaysTill(realm, LongExpiryDate);
                            tradeDays = tdc.TradeDaysTill(realm, LongExpiryDate);

                            if (tradeDays > 2 && tradeDays <= MAX_DAYS_TILL_EXPIRY) {
                                // Create realmObject of class ExpirationDates and add to ExpiryList
                                realm.beginTransaction();
                                SymbolExpiryDates exp = realm.createObject(SymbolExpiryDates.class);
                                realm.commitTransaction();
                                realm.beginTransaction();
                                symbl.Add2ExpiryList(exp);
                                // Set the long value of the expiry date
                                exp.setLongExpiryDate(LongExpiryDate);
                                // Get the days till expiry plus the number of workdays till expiry
                                realm.commitTransaction();
                                // Scan through List of option roots
                                // ------- Option roots ------------------------------------------------------------------------------------------------
                                boolean ChainFound = false;
                                int k = 0;

                                while (k < ExpiryDateJSON.getStrikePriceChainCount() && !ChainFound) {
                                    //for (int k = 0; k < ExpiryDateJSON.getStrikePriceChainCount(); k++) {
                                    OptionsJSON.ChainPerRootJSON ChainPerRoot = ExpiryDateJSON.getChainPerRoot(k);
                                    if (ChainPerRoot.multiplier == 100) {
                                        ChainFound = true;
                                        // Scan through strike prices -------------------------------------------------------------------------------------
                                        // Add strike prices to ChainPerRoot
                                        for (int l = 0; l < ChainPerRoot.getStrikePricesCount(); l++) {
                                            OptionsJSON.StrikePriceJSON StrikePrices = ChainPerRoot.getStrikePrice(l);
                                            double strikeprice = StrikePrices.strikePrice;

                                            // Only save options to realm that are within PERCENT_STRIKE_RANGE
                                            if (strikeprice >= minStrike && strikeprice <= maxStrike) {
                                                realm.beginTransaction();
                                                // -- Add Call Options to the realmlist  CallOptionsList found in ExpirationDates ---------------------------------
                                                // Save call symbol ID
                                                Options opt1 = realm.createObject(Options.class);
                                                opt1.setOptionID(StrikePrices.callSymbolId);
                                                opt1.setOptionType("CALL");
                                                opt1.setStrikePrice(strikeprice);

                                                // Add options realm object to OptionsList in expiratedate realm object
                                                exp.Add2CallOptionsList(opt1);
                                                // -- Add Put Options to the realmlist  PutOptionsList found in ExpirationDates ---------------------------------
                                                // Save put symbol ID

                                                Options opt2 = realm.createObject(Options.class);
                                                opt2.setOptionID(StrikePrices.putSymbolId);
                                                opt2.setOptionType("PUT");
                                                opt2.setStrikePrice(strikeprice);

                                                // Add options realm object to OptionsList in symbols realm object
                                                exp.Add2PutOptionsList(opt2);
                                                realm.commitTransaction();
                                            }
                                        }
                                    }
                                    k++;   // increment k index for chain per root
                                }    // ---------- End of Scan through Option roots --------------------------------------------------------------

                                // ---- Create the strategies for this expiry date -------------------------------------------------------------------------------------------
                                // Find call and put options for the underlying symbol
                                RealmList<Options> callOptions = exp.getCallOptionsList();
                                RealmList<Options> putOptions = exp.getPutOptionsList();

                                // Scan through call options and match up to put options
                                for (int c = 0; c < callOptions.size(); c++) {
                                    Options callOption = callOptions.get(c);

                                    for (int p = 0; p < putOptions.size(); p++) {
                                        Options putOption = putOptions.get(p);

                                        // Create a strategy realm object using call option and put option
                                        realm.beginTransaction();
                                        Strategy strat = realm.createObject(Strategy.class);
                                        strat.setID(strategyIDCounter);
                                        strategyIDCounter++;
                                        realm.commitTransaction();
                                        realm.beginTransaction();
                                        symbl.AddStrategy(strat);
                                        strat.setCallOption(callOption);
                                        strat.setPutOption(putOption);
                                        realm.commitTransaction();
                                    }
                                }
                            }
                        }
                    }
                }
            }
            NeedOptions = false;
        }
    }

    public void updateOptions(Realm realm, Intent broadcastIntent) {
        long investmentDays = 0;
        long tradeDays = 0;

        // Scan through symbolID's and populate options data *******************************************************
        RealmResults<Symbols> sym = realm.where(Symbols.class).findAll();
        if (sym.isLoaded()) {
            DateCalc ds = new DateCalc();

            // Scan through all underlying symbols, obtain all the options associated with each underlying symbol.
            // -------- Underlying Symbols
            List<Integer> OptionList = new ArrayList<Integer>();

            for (int i = 0; i < sym.size(); i++) {
                Symbols symbl = sym.get(i);
                int symbolID = symbl.getSymbolID();
                Thread.yield();
                broadcastIntent.putExtra("status", "Update of  " + symbl.getSymbol());
                sendBroadcast(broadcastIntent);
                double symbolLastTradePrice = symbl.getLastTradePrice();

                RealmList<SymbolExpiryDates> exp = symbl.getExpiryDates();
                for (int j = 0; j < exp.size(); j++) {
                    RealmList<Options> CallOptions = exp.get(j).getCallOptionsList();
                    long LongExpiryDate = exp.get(j).getLongExpiryDate();
                    TradeDateCalc tdc = new TradeDateCalc();
                    investmentDays = tdc.InvestmentDaysTill(realm, LongExpiryDate);
                    tradeDays = tdc.TradeDaysTill(realm, LongExpiryDate);
                    // Create List of Options
                    for (int k = 0; k < CallOptions.size(); k++) {
                        OptionList.add(CallOptions.get(k).getOptionID());
                    }
                    RealmList<Options> PutOptions = exp.get(j).getPutOptionsList();
                    for (int k = 0; k < PutOptions.size(); k++) {
                        OptionList.add(PutOptions.get(k).getOptionID());
                    }
                    OptionInfoJSON OptionInformation = null;
                    // --- Combine Call Options and Put Options together in one list for use with GSON ---------------------------------
                    OptionsInfoRequestJSON OptionsInfoRequest = new OptionsInfoRequestJSON(OptionList);
                    OptionsInfo OptionsQuoteJSON = new OptionsInfo(apiServer, OAUTH_TOKEN);
                    try {
                        OptionInformation = OptionsQuoteJSON.run(OptionsInfoRequest);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    // Use OptionInformation to populate realm database Options

                    if (OptionInformation != null) {
                        for (int m = 0; m < OptionInformation.optionQuotes.size(); m++) {
                            OptionInfoJSON.OptionQuoteJSON quote = OptionInformation.optionQuotes.get(m);
                            // Find the realm object and update it using OptionInformation
                            RealmResults<Options> opt = realm.where(Options.class).equalTo("OptionID", quote.symbolId).findAll();
                            if (opt.size() > 0) {
                                Options option = opt.get(0);
                                realm.beginTransaction();
                                option.setLastTradePrice(quote.lastTradePrice);
                                option.setLastTradePriceDateTime(ds.StrDate2LongDateTime(quote.lastTradeTime));
                                option.setopenInterest(quote.openInterest);
                                option.setAskPrice(quote.askPrice);
                                option.setBidPrice(quote.bidPrice);
                                realm.commitTransaction();
                                realm.beginTransaction();
                                double MedianPrice;
                                if (USE_TREND_BIAS)
                                    MedianPrice = symbolLastTradePrice +
                                            symbl.getTrendBias(tradeDays);
                                else MedianPrice = symbolLastTradePrice;

                                ProfitAnalyzer PA = new ProfitAnalyzer();
                                double np = PA.CalcOptionNetProfitability(option.getOptionType(), option.getPremium(), option.getStrikePrice(), MedianPrice,
                                        symbl.getVolatility(tradeDays), investmentDays);
                                option.setNetProfitability(np);
                                realm.commitTransaction();
                            } else Log.d(TAG, "RealmResults<Options> opt of zero size");
                        }
                    }
                    OptionList.clear();
                }
            }
            for (int i = 0; i < sym.size(); i++) {
                Symbols symbl = sym.get(i);
                int symbolID = symbl.getSymbolID();

                RealmList<Strategy> strats = symbl.getStrategyList();
                for (int j = 0; j < strats.size(); j++) {
                    Strategy strat = strats.get(j);
                    Options callOption = strat.getCallOption();
                    Options putOption = strat.getPutOption();
                    TradeDateCalc tdc = new TradeDateCalc();
                    investmentDays = tdc.InvestmentDaysTill(realm, callOption.getExpirationDateObject().getLongExpiryDate());
                    tradeDays = tdc.TradeDaysTill(realm, callOption.getExpirationDateObject().getLongExpiryDate());
                    double CallPremium = callOption.getPremium();
                    double PutPremium = putOption.getPremium();
                    int contracts = (int) (TARGET_TRADE_VALUE / (CallPremium + PutPremium) / 100);
                    double transactionFee = 9.95;
                    double FeePerContract = 1.00;
                    double TransactionFeesPerShare = (transactionFee + (FeePerContract * contracts)) * 2 / (contracts * 100);
                    double AllCostsPerShare = TransactionFeesPerShare + CallPremium + PutPremium;
                    realm.beginTransaction();
                    strat.setScore(strat.getCallOption().getnetProfitability(), strat.getPutOption().getnetProfitability(), AllCostsPerShare, investmentDays);
                    realm.commitTransaction();
                }
                realm.beginTransaction();
                symbl.setBestScore();
                realm.commitTransaction();
            }
        }
    }

    public void getSingleSymbolPriceInfo(Symbols symbol) {
        DateCalc ds = new DateCalc();
        int symbolID = symbol.getSymbolID();
        QuoteData QuoteDataJSON = new QuoteData(apiServer, OAUTH_TOKEN, symbolID);
        try {
            QuoteDataJSON.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
        double price = QuoteDataJSON.getLastTradePrice(0);
        realm.beginTransaction();
        symbol.setLastTradePrice(price); // realm write
        String dt = QuoteDataJSON.getLastTradeDateTime(0);
        // Set datetime of last trade price
        if (dt != null) {
            symbol.setLastTradePriceDateTime(ds.StrDate2LongDateTime(dt));
        }  // realm write
        realm.commitTransaction();
    }

    public void getSymbolPriceInfo(RealmResults<Symbols> sym ) {
        if (sym.isLoaded()) {
            for (int i = 0; i < sym.size(); i++) {
                getSingleSymbolPriceInfo(sym.get(i));
            }
        }
    }



    public void getSingleSymbolVolatilityInfo(Symbols symbol, HistoryData HistoryJSON) {
        int symbolID = symbol.getSymbolID();
        try {
            HistoryJSON.RetrieveSymbolData(symbolID);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Move symbol prices into array
        double candles[] = HistoryJSON.JSON2Array(HistoryJSON.Candles);
        // Calculate median and Median Average Deviation
        double dataArray[] = new double[2];
        dataArray = HistoryJSON.MedianAbsoluteDeviation(candles);
        double median = dataArray[0];
        double MAD = dataArray[1];
        // Smooth outliera
        double[] SmoothedPrices = HistoryJSON.SmoothOutliers(candles, median, MAD, SMOOTHING_STD_DEVIATIONS);
        // Calculate volatility at three expiry dates.
        int days1, days2, days3;
        Calendar calendar = Calendar.getInstance();
        int daysTillFriday = Calendar.FRIDAY - calendar.get(Calendar.DAY_OF_WEEK);
        if (daysTillFriday < MIN_DAYS_TILL_EXPIRY) {
            days1 = daysTillFriday + 5;
        } else {
            days1 = daysTillFriday;
        }
        days2 = days1 + 5;
        days3 = days2 + 5;

        HistoryJSON.CalculateIntervallicDeviation(SmoothedPrices, days1, SmoothedPrices.length);
        double vol1 = HistoryJSON.IntervallicDeviation;
        HistoryJSON.CalculateIntervallicDeviation(SmoothedPrices, days2, SmoothedPrices.length);
        double vol2 = HistoryJSON.IntervallicDeviation;
        HistoryJSON.CalculateIntervallicDeviation(SmoothedPrices, days3, SmoothedPrices.length);
        double vol3 = HistoryJSON.IntervallicDeviation;
        // Calculate linear regression
        SimpleRegression regression = new SimpleRegression();
        regression.addData(days1, vol1);
        regression.addData(days2, vol2);
        regression.addData(days3, vol3);
        realm.beginTransaction();
        symbol.setVolatilitySlope(regression.getSlope());
        symbol.setVolatilityIntercept(regression.getIntercept());
        symbol.setTrendBiasSlope(regression.getSlope());
        symbol.setTrendBiasIntercept(regression.getIntercept());
        realm.commitTransaction();
    }

    public void  getSymbolVolatilityInfo(RealmResults<Symbols> sym ) {
        // Collect History. Scan through all the symbols *****************************************************************************
        DateCalc ds = new DateCalc();
        HistoryData HistoryJSON = new HistoryData(apiServer, OAUTH_TOKEN, ds.long2StrDate(ds.LongNow() - LOOK_BACK), ds.long2StrDate(ds.LongNow()));
        for (int i = 0; i < sym.size(); i++) {
            getSingleSymbolVolatilityInfo(sym.get(i), HistoryJSON);
        }
    }




}





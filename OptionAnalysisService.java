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

    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;

    private int LOOK_BACK = 250;
    private double STD_DEVIATIONS = 5.0;
    private int MIN_DAYS_TILL_EXPIRY = 4;
    private int MAX_DAYS_TILL_EXPIRY = 16;
    private double PERCENT_STRIKE_RANGE = 4.0;
    private double COST_OF_VOLATILITY_LIMIT = .5;
    private boolean USE_TREND_BIAS = false;

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
            realm.beginTransaction();
                // delete all realm objects
                realm.deleteAll();
            realm.commitTransaction();

            if (realm.where(Holidays.class).findAll().size() == 0) {
                // Initialize realm with basic data
                Holidays hol = new Holidays();
                hol.PopulateHolidays(realm);
            }
            if (realm.where(Symbols.class).findAll().size() == 0) {
                realm.beginTransaction();
                    SymbolList xx = realm.createObject(SymbolList.class);
                realm.commitTransaction();
                xx.PopulateSymbols(realm);
            }
            Thread.yield();
            getHistory(realm, broadcastIntent);

            getOptions(realm, broadcastIntent);

            //CalcStrategies(broadcastIntent);

            realm.close();

            // Stop the service
            stopSelf();

        }
    }


    public void getHistory(Realm realm, Intent broadcastIntent) {

        // Initialization of realm database
       // Realm realm = Realm.getDefaultInstance(); // opens "myrealm.realm"

        // Update realm.symbols with symbolID's
        RealmResults<Symbols> sym = realm.where(Symbols.class).findAll();
        if (sym.isLoaded()) {

                String symbol;
                for (int i = 0; i < sym.size(); i++) {
                    symbol = sym.get(i).getSymbol();

                    Thread.yield();
                    broadcastIntent.putExtra("status", "History of "+symbol);
                    sendBroadcast(broadcastIntent);

                    SymbolData SymbolDataJSON = new SymbolData(apiServer, OAUTH_TOKEN, symbol);
                    try {
                        SymbolDataJSON.run();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    int symID = SymbolDataJSON.getSymbolID(0);
                    realm.beginTransaction();
                        sym.get(i).setSymbolId(symID);     // realm write
                    realm.commitTransaction();
                }

        }

        // Update realm.symbols with LastTradePrice
        DateCalc ds = new DateCalc();

        int symbolID;
        for (int i = 0; i < sym.size(); i++) {
            symbolID = sym.get(i).getSymbolID();
            QuoteData QuoteDataJSON = new QuoteData(apiServer, OAUTH_TOKEN, symbolID);
            try {
                QuoteDataJSON.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
            double price = QuoteDataJSON.getLastTradePrice(0);
            realm.beginTransaction();
                sym.get(i).setLastTradePrice(price); // realm write
                String dt = QuoteDataJSON.getLastTradeDateTime(0);
                // Set datetime of last trade price
                if (dt != null){
                 sym.get(i).setLastTradePriceDateTime(ds.StrDate2LongDateTime(dt));  }  // realm write
            realm.commitTransaction();
        }


        // Collect History. Scan through all the symbols *****************************************************************************
        HistoryData HistoryJSON = new HistoryData(apiServer, OAUTH_TOKEN, ds.long2StrDate(ds.LongNow() - LOOK_BACK), ds.long2StrDate(ds.LongNow()));
        double[] SmoothedPrices;
        for (int i = 0; i < sym.size(); i++) {
            symbolID = sym.get(i).getSymbolID();
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
            SmoothedPrices = HistoryJSON.SmoothOutliers(candles, median, MAD, STD_DEVIATIONS);
            // Calculate volatility at three expiry dates.
            int days1, days2, days3;
            Calendar calendar = Calendar.getInstance();
            int daysTillFriday = Calendar.FRIDAY - calendar.get(Calendar.DAY_OF_WEEK);
            if (daysTillFriday < MIN_DAYS_TILL_EXPIRY) {
                days1 = daysTillFriday + 7;
            } else {
                days1 = daysTillFriday;
            }
            days2 = days1 + 7;
            days3 = days2 + 7;
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
                sym.get(i).setVolatilitySlope(regression.getSlope());
                sym.get(i).setVolatilityIntercept(regression.getIntercept());
                sym.get(i).setTrendBiasSlope(regression.getSlope());
                sym.get(i).setTrendBiasIntercept(regression.getIntercept());
                sym.get(i).setCalcDate(ds.LongNow());
            realm.commitTransaction();

        }
        Thread.yield();
        broadcastIntent.putExtra("status", "History Complete");
        sendBroadcast(broadcastIntent);

    }

    public void getOptions(Realm realm, Intent broadcastIntent) {
        // counter to set Strategy.strategyID
        int strategyIDCounter=1;

        // Scan through symbolID's and populate options data *******************************************************
        RealmResults<Symbols> sym = realm.where(Symbols.class).findAll();
        if (sym.isLoaded()) {

            int symbolID;
            // Scan through all underlying symbols, obtain all the options associated with each underlying symbol.
            // -------- Underlying Symbols
            for (int i = 0; i < sym.size(); i++) {
                Symbols symbl = sym.get(i);
                symbolID = symbl.getSymbolID();

                Thread.yield();
                broadcastIntent.putExtra("status", "Options of "+symbl.getSymbol());
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
                // ------- Options Expiry dates ------------------------------------------------------------------------------------------------------
                List<Integer> OptionList = new ArrayList<Integer>();
                if (OptionsDataJSON.getExpiryDateCount() > 0) {
                    for (int j = 0; j < OptionsDataJSON.getExpiryDateCount(); j++) {
                        // Get Expiry dates
                        OptionsJSON.OptionExpiryDateJSON ExpiryDateJSON = OptionsDataJSON.getExpiryDate(j);

                        long LongExpiryDate = ds.StrDate2LongDate(ExpiryDateJSON.expiryDate);
                        TradeDateCalc  tdc = new TradeDateCalc();
                        investmentDays = tdc.InvestmentDaysTill(realm,LongExpiryDate);
                        tradeDays = tdc.TradeDaysTill(realm,LongExpiryDate);

                        if (tradeDays>2 && tradeDays<= MAX_DAYS_TILL_EXPIRY) {
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
                                            OptionList.add(StrikePrices.callSymbolId);
                                            Options opt1 = realm.createObject(Options.class);
                                            opt1.setOptionID(StrikePrices.callSymbolId);
                                            opt1.setOptionType("CALL");
                                            opt1.setStrikePrice(strikeprice);

                                            // Add options realm object to OptionsList in expiratedate realm object
                                            exp.Add2CallOptionsList(opt1);
                                            // -- Add Put Options to the realmlist  PutOptionsList found in ExpirationDates ---------------------------------
                                            // Save put symbol ID
                                            OptionList.add(StrikePrices.putSymbolId);
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

                            // Use POST request using Questrade API to retrieve option information using CallOptionList And PutOptionList -----
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
                            assert OptionInformation != null;
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

                            // ---- Create the strategies for this expiry date -------------------------------------------------------------------------------------------
                            // Find call and put options for the underlying symbol
                            RealmList<Options> callOptions = exp.getCallOptionsList();
                            RealmList<Options> putOptions = exp.getPutOptionsList();

                            // Scan through call options and match up to put options
                            for (int c = 0; c < callOptions.size(); c++) {
                                Options callOption = callOptions.get(c);

                                for (int p = 0; p < putOptions.size(); p++) {
                                    Options putOption = putOptions.get(p);

                                    double CallPremium = callOption.getPremium();
                                    double PutPremium = putOption.getPremium();
                                    // Get the days till expiry plus the number of workdays till expiry
                                    tdc = new TradeDateCalc();
                                    investmentDays = tdc.InvestmentDaysTill(realm, callOption.getExpirationDateObject().getLongExpiryDate());
                                    tradeDays = tdc.TradeDaysTill(realm, callOption.getExpirationDateObject().getLongExpiryDate());

                                    realm.beginTransaction();
                                    double stdDev = symbl.getVolatility(tradeDays);
                                    double MedianPrice;
                                    if (USE_TREND_BIAS)
                                        MedianPrice = symbl.getLastTradePrice() +
                                                symbl.getTrendBias(tradeDays);
                                    else MedianPrice = symbl.getLastTradePrice();

                                    realm.commitTransaction();


                                    int contracts = 5;
                                    double transactionFee = 9.95;
                                    double FeePerContract = 1.00;
                                    double TransactionFeesPerShare = (transactionFee + (FeePerContract * contracts)) / (contracts * 100);
                                    double AllCostsPerShare = TransactionFeesPerShare + CallPremium + PutPremium;
                                    // Check if this option par is possibly profitable and the option's expiry date is not too close
                                    if (AllCostsPerShare / stdDev < COST_OF_VOLATILITY_LIMIT && CallPremium > 0 && PutPremium > 0 && tradeDays >= MIN_DAYS_TILL_EXPIRY) {

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

                                        realm.beginTransaction();
                                            strat.setScore(strat.getCallOption().getnetProfitability(), strat.getPutOption().getnetProfitability(), AllCostsPerShare, TransactionFeesPerShare, investmentDays);
                                        realm.commitTransaction();
                                    }
                                }
                            }
                            realm.beginTransaction();
                                symbl.setBestScore();
                            realm.commitTransaction();
                        }
                        OptionList.clear();
                    }
                 }
            }

        }

    }



    public void CalcStrategies(Intent broadcastIntent) {
        Realm realm = Realm.getDefaultInstance(); // opens "myrealm.realm"

        // Scan through symbolID's and populate options data *******************************************************
        RealmResults<Symbols> sym = realm.where(Symbols.class).findAll();
        if (sym.isLoaded()) {
            // Scan through all underlying symbols, then scan throug the symbols expiry dates
            for (int i = 0; i < sym.size(); i++) {
                // Scan through list of expiration dates for a symbol
                Symbols sybl = sym.get(i);

                Thread.yield();
                broadcastIntent.putExtra("status", "Strategies for "+sym.get(i).getSymbol());
                sendBroadcast(broadcastIntent);

                RealmList<SymbolExpiryDates> expiryDates = sybl.getExpiryDates();

                for (int j = 0; j < expiryDates.size(); j++) {

                    // Find call and put options for the underlying symbol
                    RealmList<Options> callOptions = expiryDates.get(j).getCallOptionsList();
                    RealmList<Options> putOptions = expiryDates.get(j).getPutOptionsList();

                    // Scan through call options and match up to put options
                    for (int c = 0; c < callOptions.size(); c++) {
                        Options callOption = callOptions.get(c);

                        for (int p = 0; p < putOptions.size(); p++) {
                            Options putOption = putOptions.get(p);

                            double CallPremium = callOption.getPremium();
                            double PutPremium = putOption.getPremium();
                            // Get the days till expiry plus the number of workdays till expiry
                            TradeDateCalc tdc = new TradeDateCalc();
                            long investmentDays = tdc.InvestmentDaysTill(realm, callOption.getExpirationDateObject().getLongExpiryDate());
                            long tradeDays = tdc.InvestmentDaysTill(realm, callOption.getExpirationDateObject().getLongExpiryDate());
                            //long workDaysTillExpiry = callOption.getExpirationDateObject().getWorkDaysTillExpiry();

                            realm.beginTransaction();
                                double stdDev = sybl.getVolatility(tradeDays);
                                double MedianPrice;
                                if(USE_TREND_BIAS)
                                    MedianPrice = sybl.getLastTradePrice() +
                                            sybl.getTrendBias(tradeDays);
                                else MedianPrice = sybl.getLastTradePrice();

                            realm.commitTransaction();



                            int contracts = 5;
                            double transactionFee = 9.95;
                            double FeePerContract = 1.00;
                            double TransactionFeesPerShare = (transactionFee + (FeePerContract * contracts)) / (contracts * 100);
                            double AllCostsPerShare = TransactionFeesPerShare+CallPremium + PutPremium;
                            // Check if this option par is possibly profitable and the option's expiry date is not too close
                            if (AllCostsPerShare / stdDev < COST_OF_VOLATILITY_LIMIT && CallPremium > 0 && PutPremium > 0 && tradeDays >= MIN_DAYS_TILL_EXPIRY) {

                                // Create a strategy realm object using call option and put option
                                realm.beginTransaction();
                                    Strategy strat = realm.createObject(Strategy.class);
                                    realm.commitTransaction();
                                    realm.beginTransaction();
                                    sybl.AddStrategy(strat);
                                    strat.setCallOption(callOption);
                                    strat.setPutOption(putOption);
                                realm.commitTransaction();

                                realm.beginTransaction();
                                    strat.setScore(strat.getCallOption().getnetProfitability(), strat.getPutOption().getnetProfitability(), AllCostsPerShare, TransactionFeesPerShare, investmentDays );
                                realm.commitTransaction();
                            }
                        }
                    }
                }
            }
        }

    }

}
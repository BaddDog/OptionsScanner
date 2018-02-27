package com.baddog.optionsscanner;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.JsonReader;
import android.view.MenuItem;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.widget.TextView;

import android.net.Uri;
import android.os.AsyncTask;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import com.google.gson.Gson;

import org.apache.commons.math3.stat.regression.RegressionResults;
import org.apache.commons.math3.stat.regression.SimpleRegression;

import java.io.BufferedReader;
import java.io.IOException;


import javax.net.ssl.HttpsURLConnection;

import io.realm.Realm;
import io.realm.RealmAsyncTask;
import io.realm.RealmConfiguration;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static io.realm.internal.network.OkHttpAuthenticationServer.JSON;


public class MainActivity extends Activity implements View.OnClickListener {

    private static String CLIENT_ID = "ATBtay4nP5Gt3IJ9TeMPmaWBAIlIbg";
    private static String CLIENT_SECRET = null;
    private static String REDIRECT_URI = "https://localhost321.com";
    private static String GRANT_TYPE = "authorization_code";
    private static String TOKEN_URL = "https://login.questrade.com/oauth2/token";
    private static String OAUTH_URL = "https://login.questrade.com/oauth2/authorize";

    private int LOOK_BACK = 250;
    private double STD_DEVIATIONS = 5.0;
    private int MIN_DAYS_TILL_EXPIRY = 3;
    private int MAX_DAYS_TILL_EXPIRY = 25;
    private double PERCENT_STRIKE_RANGE = 5.0;

    private BroadcastReceiver broadcastReceiver;
    String OAUTH_TOKEN = null;
    String apiServer;
    Dialog auth_dialog;
    WebView web;
    Button authButton;
    Button historyButton;
    Button OptionsButton;
    Button StrategyButton;
    SharedPreferences pref;
    private TextView mTextMessage;
    private Realm realm;
    static final String STATE_USER = "user";
    private String mUser;
    List<Integer> OptionList = new ArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialization of realm database
        realm = Realm.getDefaultInstance(); // opens "myrealm.realm"
        realm.beginTransaction();
             // delete all realm objects
             realm.deleteAll();
        realm.commitTransaction();

        setContentView(R.layout.activity_main);
        pref = getSharedPreferences("AppPref", MODE_PRIVATE);

        // auth is "Sign in" button on main activity page *****************************************
        authButton = (Button) findViewById(R.id.auth);
        authButton.setOnClickListener(this);
        // Listen for 'history' button *****************************************
        historyButton = (Button) findViewById(R.id.history);
        historyButton.setClickable(false);
        historyButton.setOnClickListener(this);
        // Listen for 'Analyze Options' button *****************************************
        OptionsButton = (Button) findViewById(R.id.AnalyzeOptions);
        OptionsButton.setClickable(false);
        OptionsButton.setOnClickListener(this);
        // Listen for 'Analyze Strategy' button *****************************************
        StrategyButton = (Button) findViewById(R.id.AnalyzeStrategies);
        StrategyButton.setClickable(false);
        StrategyButton.setOnClickListener(this);

        if(realm.where(Holidays.class).findAll().size()==0) {
            // Initialize realm with basic data
            Holidays hol = new Holidays();
            hol.PopulateHolidays(realm);
        }
        if(realm.where(Symbols.class).findAll().size()==0) {
            Symbols sym = new Symbols();
            sym.PopulateSymbols(realm);
        }

            // **** testing *****************************************************
            Date currentTime = Calendar.getInstance().getTime();
            Date date2 = null;
            String dtStart = "2018-03-10T09:27:37Z";
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            try {
                date2 = format.parse(dtStart);
                System.out.println(date2);
            } catch (ParseException e) {
                e.printStackTrace();
            }


            DateSmith DS = new DateSmith();
            long CT = DS.workdayDiff(realm, currentTime, date2);

            RealmResults<Symbols> result2 = realm.where(Symbols.class).findAll();
            String x = result2.last().getSymbol();
            // **************************************************************************
    }

    @Override
    protected void onStart() {
         super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_dashboard);
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }
    };

    public void onClick(View v) {
        // do something when the button is clicked

        switch (v.getId() /*to get clicked view id**/) {
            case R.id.auth:
                AuthenticateQT();
                break;
            case R.id.history:
                // Update realm.symbols with symbolID's, and LastTradePrice & Collect History. Calculate volatility.
                HistoryQT();
                break;
            case R.id.AnalyzeOptions:
                // Populate realm.options using "GET markets/quotes/options"
                OptionsQT();
                break;
            case R.id.AnalyzeStrategies:
                // Populate realm.strategy and calculate scores
                StrategyQT();
                break;
            default:
                break;
        }

    }


    private class TokenGet extends AsyncTask<String, String, String> {
        private ProgressDialog pDialog;
        String Code;
        private Context context;

        private TokenGet(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Contacting Questrade...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            Code = pref.getString("Code", "");
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... args) {
            String accessToken = null;
            GetAccessToken jParser = new GetAccessToken();
            JsonReader AuthJSON = jParser.gettoken(TOKEN_URL, Code, CLIENT_ID, CLIENT_SECRET, REDIRECT_URI, GRANT_TYPE);

            try {
                AuthJSON.beginObject(); // Start processing the JSON object
                while (AuthJSON.hasNext()) { // Loop through all keys
                    String key = AuthJSON.nextName(); // Fetch the next key
                    if (key.equals("access_token")) { // Check if desired key
                        // Fetch the value as a String
                        accessToken = AuthJSON.nextString();
                        break; // Break out of the loop
                    } else {
                        AuthJSON.skipValue(); // Skip values of other keys
                    }
                }
                //AuthJSON.beginObject(); // Start processing the JSON object
                while (AuthJSON.hasNext()) { // Loop through all keys
                    String key = AuthJSON.nextName(); // Fetch the next key
                    if (key.equals("api_server")) { // Check if desired key
                        // Fetch the value as a String
                        apiServer = AuthJSON.nextString();
                        break; // Break out of the loop
                    } else {
                        AuthJSON.skipValue(); // Skip values of other keys
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }



            return accessToken;
        }

        @Override
        protected void onPostExecute(String token) {
            pDialog.dismiss();
            if (token != null) {
                OAUTH_TOKEN = token;
                Log.d("Token Access", token);
                authButton.setText("Authenticated");
                authButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorGreen)));
                // Send a broadcast from onPostExecute() to mainactivity
                Intent intent = new Intent("com.example.UPLOAD_FINISHED");
                context.sendBroadcast(intent);
                // Make History Button Clickable
                historyButton.setClickable(true);
            } else {
                Toast.makeText(getApplicationContext(), "Network Error", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private class GetHistory extends AsyncTask<String, String, String> {

        public  Context context;

        private GetHistory(Context context) throws Exception {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(String... args) {

            // Initialization of realm database
            Realm realm = Realm.getDefaultInstance(); // opens "myrealm.realm"

            // Update realm.symbols with symbolID's
            RealmResults<Symbols> sym = realm.where(Symbols.class).findAll();
            if (sym.isLoaded()) {
                realm.beginTransaction();
                String symbol;
                for (int i = 0; i < sym.size(); i++) {
                    symbol = sym.get(i).getSymbol();
                    SymbolData SymbolDataJSON = new SymbolData(apiServer, OAUTH_TOKEN, symbol);
                    try {
                        SymbolDataJSON.run();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    int symID = SymbolDataJSON.getSymbolID(0);
                    sym.get(i).setSymbolId(symID);
                }
                realm.commitTransaction();
            }

            // Update realm.symbols with LastTradePrice
            DateSmith ds = new DateSmith();
            realm.beginTransaction();
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
                sym.get(i).setLastTradePrice(price);
                String dt = QuoteDataJSON.getLastTradeDateTime(0);
                // Set datetime of last trade price
                sym.get(i).setLastTradePriceDateTime(ds.StrDate2LongDateTime(dt));
            }
            realm.commitTransaction();

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
                int daysTillFriday =  Calendar.FRIDAY - calendar.get(Calendar.DAY_OF_WEEK) ;
                if (daysTillFriday < MIN_DAYS_TILL_EXPIRY) {
                    days1 = daysTillFriday+7;
                } else {
                    days1 = daysTillFriday;
                }
                days2 = days1 + 7;
                days3 = days2 + 7;
                HistoryJSON.CalculateIntervallicDeviation(SmoothedPrices, days1,SmoothedPrices.length);
                double vol1 = HistoryJSON.IntervallicDeviation;
                HistoryJSON.CalculateIntervallicDeviation(SmoothedPrices, days2,SmoothedPrices.length);
                double vol2 = HistoryJSON.IntervallicDeviation;
                HistoryJSON.CalculateIntervallicDeviation(SmoothedPrices, days3,SmoothedPrices.length);
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
            // END OF Collect History. Scan through all the symbols *****************************************************************************
        return "";
        }

        @Override
        protected void onPostExecute(String token) {
                // Change title once completed
                historyButton.setText("History Collected");
                historyButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorGreen)));
                // Make History Button Clickable
                OptionsButton.setClickable(true);

        }

    }

    private void AuthenticateQT () {
        auth_dialog = new Dialog(MainActivity.this);
        auth_dialog.setContentView(R.layout.auth_dialog);
        web = (WebView) auth_dialog.findViewById(R.id.webview);
        web.getSettings().setJavaScriptEnabled(true);
        web.clearCache(true);
        String s_url = OAUTH_URL + "?redirect_uri=" + REDIRECT_URI + "&response_type=code&client_id=" + CLIENT_ID;
        web.loadUrl(s_url);
        // Create a webview client to complete authenication on questrade site ***********************************
        web.setWebChromeClient(new WebChromeClient());
        web.setWebViewClient(new WebViewClient() {
            boolean authComplete = false;
            Intent resultIntent = new Intent();
            String authCode;
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (url.contains("?code=") && !authComplete) {
                    Uri uri = Uri.parse(url);
                    authCode = uri.getQueryParameter("code");
                    Log.i("", "CODE : " + authCode);
                    authComplete = true;
                    resultIntent.putExtra("code", authCode);
                    MainActivity.this.setResult(Activity.RESULT_OK, resultIntent);
                    setResult(Activity.RESULT_CANCELED, resultIntent);
                    SharedPreferences.Editor edit = pref.edit();
                    edit.putString("Code", authCode);
                    edit.apply();
                    auth_dialog.dismiss();
                    // Start AsyncTask to get authentication token
                    new TokenGet(MainActivity.this).execute();
                } else if (url.contains("error=access_denied")) {
                    Log.i("", "ACCESS_DENIED_HERE");
                    resultIntent.putExtra("code", authCode);
                    authComplete = true;
                    setResult(Activity.RESULT_CANCELED, resultIntent);
                    Toast.makeText(getApplicationContext(), "Error Occured", Toast.LENGTH_SHORT).show();
                    auth_dialog.dismiss();
                }
            }
        });
        auth_dialog.show();
        auth_dialog.setTitle("Authorize Questrade");
        auth_dialog.setCancelable(true);
    }

    private void HistoryQT() {
        if (OAUTH_TOKEN != null) {
            // Update realm.symbols with symbolID's, and LastTradePrice
            // Collect History. Calculate volatility.
            try {
                new GetHistory(MainActivity.this).execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void OptionsQT() {
        if (OAUTH_TOKEN != null) {
            // Update realm.Options
            try {
                new GetOptions(MainActivity.this).execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void StrategyQT() {
        if (OAUTH_TOKEN != null) {
            // Update realm.Strategy
            try {
                new CalcStrategies(MainActivity.this).execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class GetOptions extends AsyncTask<String, String, String> {

        public  Context context;

        private GetOptions(Context context) throws Exception {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(String... args) {

            // Initialization of realm database
            Realm realm = Realm.getDefaultInstance(); // opens "myrealm.realm"
            OptionInfoJSON OptionInformation = null;
            // Scan through symbolID's and populate options data *******************************************************
            RealmResults<Symbols> sym = realm.where(Symbols.class).findAll();
            if (sym.isLoaded()) {

                int symbolID;
                // Scan through all underlying symbols, obtain all the options associated with each underlying symbol.
                for (int i = 0; i < sym.size(); i++) {
                    symbolID = sym.get(i).getSymbolID();
                    double symbolLastTradePrice = sym.get(i).getLastTradePrice();
                    OptionsData OptionsDataJSON = new OptionsData(apiServer, OAUTH_TOKEN, symbolID);
                    try {
                        OptionsDataJSON.run();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    // OptionsDataJSON contains all the options for a single underlying symbol (sym)
                    DateSmith ds = new DateSmith();
                    double maxStrike = symbolLastTradePrice*(PERCENT_STRIKE_RANGE/100+1);
                    double minStrike = symbolLastTradePrice/(PERCENT_STRIKE_RANGE/100+1);

                    // Scan through the Options Expiry dates for a single underlying symbol. Add the option IDs into a List called
                    // OptionList and use this list to retrieve option information
                    if(OptionsDataJSON.getExpiryDateCount()>0) {
                        for (int j = 0; j < OptionsDataJSON.getExpiryDateCount(); j++) {
                            // Get Expiry dates
                            OptionsJSON.OptionExpiryDateJSON ExpiryDateJSON = OptionsDataJSON.getExpiryDate(j);
                            long LongExpiryDate = ds.StrDate2LongDate(ExpiryDateJSON.expiryDate);
                            if(LongExpiryDate-ds.LongNow()<=MAX_DAYS_TILL_EXPIRY) {
                                // List of option roots
                                for (int k = 0; k < ExpiryDateJSON.getStrikePriceChainCount(); k++) {
                                    OptionsJSON.ChainPerRootJSON ChainPerRoot = ExpiryDateJSON.getChainPerRoot(k);
                                    if(ChainPerRoot.multiplier==100) {
                                        // Scan through strike prices
                                        for (int l = 0; l < ChainPerRoot.getStrikePricesCount(); l++) {
                                            OptionsJSON.StrikePriceJSON StrikePrices = ChainPerRoot.getStrikePrice(l);
                                            double strikeprice = StrikePrices.strikePrice;

                                            // Only save options to realm that are within PERCENT_STRIKE_RANGE
                                            if(strikeprice>=minStrike && strikeprice<=maxStrike) {
                                                /* TODO Need to save the option ids so that they can be used to retrieve price data and open interest
                                                    private double LastTradePrice;
                                                    private long LastTradePriceDateTime;
                                                    private int openInterest;
                                                */
                                                realm.beginTransaction();
                                                    // Save call symbol ID
                                                    Options opt1 = realm.createObject(Options.class);
                                                    opt1.setOptionID(StrikePrices.callSymbolId);
                                                    opt1.setOptionType("CALL");
                                                    opt1.setLongExpiryDate(LongExpiryDate);
                                                    opt1.setStrikePrice(strikeprice);
                                                    opt1.setUnderlyingID(symbolID);
                                                    OptionList.add(StrikePrices.callSymbolId);

                                                    // Save put symbol ID
                                                    Options opt2 = realm.createObject(Options.class);
                                                    opt2.setOptionID(StrikePrices.putSymbolId);
                                                    opt2.setOptionType("PUT");
                                                    opt2.setLongExpiryDate(LongExpiryDate);
                                                    opt2.setStrikePrice(strikeprice);
                                                    opt2.setUnderlyingID(symbolID);
                                                    OptionList.add(StrikePrices.putSymbolId);
                                                realm.commitTransaction();
                                            }
                                        }
                                   }
                                }
                            }
                        }
                    }
                    // Use OptionList to POST for option information from questrade
                    OptionsInfoRequestJSON OptionsInfoRequest = new OptionsInfoRequestJSON(OptionList);
                    OptionsInfo OptionsQuoteJSON = new OptionsInfo(apiServer, OAUTH_TOKEN);
                    try {
                        OptionInformation = OptionsQuoteJSON.run(OptionsInfoRequest);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // Use OptionInformation to populate realm database Options
                    assert OptionInformation != null;
                    for (int m=0;m<OptionInformation.optionQuotes.size();m++) {
                        OptionInfoJSON.OptionQuoteJSON quote = OptionInformation.optionQuotes.get(m);
                        // Find the realm object and update it using OptionInformation
                        realm.beginTransaction();
                            RealmResults<Options> opt = realm.where(Options.class).equalTo("OptionID",quote.symbolId ).findAll();
                            opt.get(0).setLastTradePrice(quote.lastTradePrice);
                            opt.get(0).setLastTradePriceDateTime(ds.StrDate2LongDateTime(quote.lastTradeTime));
                            opt.get(0).setopenInterest(quote.openInterest);
                        realm.commitTransaction();
                    }
                    // Clear OptionList so it can be used again for the next underlying symbol
                    OptionList.clear();
                }


            }

        return "";
        }


        @Override
        protected void onPostExecute(String token) {
            // Change title once completed
            OptionsButton.setText("Analysis Collected");
            OptionsButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorGreen)));
            StrategyButton.setClickable(true);

        }
    }


private class CalcStrategies extends AsyncTask<String, String, String> {

    public  Context context;

    private CalcStrategies(Context context) throws Exception {
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    @Override
    protected String doInBackground(String... args) {

        // Initialization of realm database
        Realm realm = Realm.getDefaultInstance(); // opens "myrealm.realm"
    return "";
    }


    @Override
    protected void onPostExecute(String token) {
        // Change title once completed
        StrategyButton.setText("Analysis Collected");
        StrategyButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorGreen)));
    }
}


}
package com.baddog.optionsscanner;


import android.content.Context;

import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Debug;
import android.os.Process;
import android.support.v4.content.ContextCompat;
import android.util.JsonReader;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import io.realm.Realm;
import io.realm.RealmResults;

import android.content.BroadcastReceiver;


public class MainActivity extends Activity implements View.OnClickListener {

    private static String CLIENT_ID = "ATBtay4nP5Gt3IJ9TeMPmaWBAIlIbg";
    private static String CLIENT_SECRET = null;
    private static String REDIRECT_URI = "https://localhost321.com";
    private static String GRANT_TYPE = "authorization_code";
    private static String TOKEN_URL = "https://login.questrade.com/oauth2/token";
    private static String OAUTH_URL = "https://login.questrade.com/oauth2/authorize";

    private boolean NeedHistory;
    private boolean NeedOptions;

    String OAUTH_TOKEN = null;
    String apiServer;
    Dialog auth_dialog;
    WebView web;

    // IU Components
    Button resetButton;
    Button authButton;
    Button OptionsButton;
    Button StrategyViewButton;
    TextView ProgressTextView;
    ProgressBar ServiceProgressBar;

    SharedPreferences pref;

    MessageFromService receiver;
    private static final String TAG = "MyActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_DISPLAY);
       pref = getSharedPreferences("AppPref", MODE_PRIVATE);

       // Flags set to false at startup
        NeedHistory = false;
        NeedOptions = false;

       // reset is "Reset Database" button on main activity page *****************************************
        resetButton = (Button) findViewById(R.id.Reset);
        resetButton.setOnClickListener(this);
        // auth is "Sign in" button on main activity page *****************************************
        authButton = (Button) findViewById(R.id.auth);
        authButton.setOnClickListener(this);
        // Listen for 'Analyze Options' button *****************************************
        OptionsButton = (Button) findViewById(R.id.AnalyzeOptions);
        OptionsButton.setClickable(false);
        OptionsButton.setOnClickListener(this);
        // Listen for 'View Strategies' button *****************************************
        StrategyViewButton = (Button) findViewById(R.id.StrategyViews);
        StrategyViewButton.setClickable(false);
        StrategyViewButton.setOnClickListener(this);
        ProgressTextView = (TextView) findViewById(R.id.ProgressText);

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        receiver = new MessageFromService();
        registerReceiver(receiver, new IntentFilter(MessageFromService.ACTION_RESP));
    }
    @Override
    protected void onStop() {
        super.onStop();

        unregisterReceiver(receiver);
    }
    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }


    public void onClick(View v) {
        // do something when the button is clicked

        switch (v.getId() /*to get clicked view id**/) {
            case R.id.Reset:
                // Delete all info in realm database and set flags to indicate a rebuild of history and option info
                NeedHistory = true;
                NeedOptions = true;
                // Initialization of realm database
                Realm realm;
                realm = Realm.getDefaultInstance(); // opens "myrealm.realm"
                realm.beginTransaction();
                    // delete all realm objects
                    realm.deleteAll();
                    // Holiday data objects
                realm.commitTransaction();
                if (realm.where(Holidays.class).findAll().size() == 0) {
                    // Initialize realm with basic data
                    Holidays hol = new Holidays();
                    hol.PopulateHolidays(realm);
                }
                // Symbol data objects
                if (realm.where(Symbols.class).findAll().size() == 0) {
                    realm.beginTransaction();
                     SymbolList xx = realm.createObject(SymbolList.class);
                    realm.commitTransaction();
                    xx.PopulateSymbols(realm);
                }

                realm.close();
                updateTheTextView("Database Reset");
                break;
            case R.id.auth:
                AuthenticateQT();
                break;
             case R.id.AnalyzeOptions:
                 OptionsButton.setEnabled(false);
                 Intent serviceIntent = new Intent(this, OptionAnalysisService.class);

                 //Intent serviceIntent = new Intent(this, OptionAnalysisService.class);
                 serviceIntent.putExtra("apiserver", apiServer);
                 serviceIntent.putExtra("oauthtoken", OAUTH_TOKEN);
                 serviceIntent.putExtra("NeedHistory", NeedHistory);
                 serviceIntent.putExtra("NeedOptions", NeedOptions);

                 startService(serviceIntent);

                break;
            case R.id.StrategyViews:
                // Populate realm.strategy and calculate scores

                Intent it = new Intent(this.getApplicationContext(), ViewSymbolList.class);
                startActivity(it);
                break;
            default:
                break;
        }

    }

    public void updateTheTextView(final String statusMessage) {
        this.runOnUiThread(new Runnable() {
            public void run() {
                ProgressTextView.setText(statusMessage);
            }
        });
    }



    // Broadcast component
    public class MessageFromService extends BroadcastReceiver {
        public static final String ACTION_RESP = "com.android.IntentService";

        // on broadcast received
        @Override
        public void onReceive(Context context, Intent intent) {

            String statusMessage = intent.getStringExtra("status");
            try {
                updateTheTextView(statusMessage);
                //ProgressTextView.setText(statusMessage);

            } catch (Exception e) {
              }
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
                //Intent intent = new Intent("com.example.UPLOAD_FINISHED");
                //context.sendBroadcast(intent);

                // Make authorization Button Clickable
                authButton.setClickable(false);

            } else {
                Toast.makeText(getApplicationContext(), "Network Error", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void AuthenticateQT() {
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






    class Mytask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
        return "";
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            //ServiceProgressBar.setProgress(result);


            //if (result == 100) {
           //     ProgressTextView.setText("Completed");
           //     StrategyViewButton.setEnabled(true);
                // TODO fix
           //     //OptionsButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorGreen)));
           // }

        }
    }



}


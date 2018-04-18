package com.baddog.optionsscanner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Brian on 2018-02-10.
 *
 */

public class QuoteData {
    private final OkHttpClient client = new OkHttpClient();
    private final String Url;
    private final String AuthorizationKey;
    private final Gson gson = new Gson();
    private QuotesJSON qJSON;

    public QuoteData(String apiHost, String Key, int symbolID) {
        this.Url = apiHost+"v1/markets/quotes/" + symbolID ;
        this.AuthorizationKey = "Bearer "+ Key;
    }

    public int run() throws Exception {
        Request request = new Request.Builder()
                .url(Url)
                .get()
                .header("Authorization", AuthorizationKey)
                .build();

        Response response = client.newCall(request).execute();
        int responseCode = response.code();
        if (responseCode == 200) {
            GsonBuilder gson_builder = new GsonBuilder();
            Gson gson = gson_builder.create();
            InputStream is = response.body().byteStream();
            Reader reader = new InputStreamReader(is, "UTF-8");
            qJSON = gson.fromJson(reader,QuotesJSON.class);
        }
        int code = response.code();
        response.close();
        return code;
    }

    public int getQuoteRecordSize() {
        return qJSON.quotes.size();
    }

    public double getLastTradePrice(int index) {
        return qJSON.quotes.get(index).lastTradePrice;
    }

    public String getLastTradeDateTime (int index) { return qJSON.quotes.get(index).lastTradeTime;}

    public double getAskPrice(int index) {
        return qJSON.quotes.get(index).askPrice;
    }
    public double getBidPrice(int index) {
        return qJSON.quotes.get(index).bidPrice;
    }

}

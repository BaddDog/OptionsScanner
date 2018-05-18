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
    private final String Url;
    private final String AuthorizationKey;
    private final Gson gson = new Gson();
    private String LastTradeDateTime;
    private double LastTradePrice;

    public QuoteData(String apiHost, String Key, int symbolID) {
        this.Url = apiHost+"v1/markets/quotes/" + symbolID ;
        this.AuthorizationKey = "Bearer "+ Key;
    }

    public int run() throws Exception {
        int code=0;
        try(Response response = new OkHttpClient().newCall(new Request.Builder()
                .url(Url)
                .get()
                .header("Authorization", AuthorizationKey)
                .build()).execute()) {
            if (response.code() == 200) {
                try (Reader reader = new InputStreamReader(response.body().byteStream(), "UTF-8")) {
                    GsonBuilder gson_builder = new GsonBuilder();
                    Gson gson = gson_builder.create();
                    QuotesJSON qJSON = gson.fromJson(reader, QuotesJSON.class);
                    LastTradePrice = qJSON.quotes.get(0).lastTradePrice;
                    LastTradeDateTime = qJSON.quotes.get(0).lastTradeTime;
                    code = response.code();
                }
            }
        }
        return code;
    }

    //public int getQuoteRecordSize() {
    //    return qJSON.quotes.size();
    //}

   public double getLastTradePrice(int index) {
        return this.LastTradePrice;
    }

   public String getLastTradeDateTime (int index) {
        return this.LastTradeDateTime;
    }

    //public double getAskPrice(int index) {
    //    return qJSON.quotes.get(index).askPrice;
    //}
    //public double getBidPrice(int index) {
    //    return qJSON.quotes.get(index).bidPrice;
    //}

}

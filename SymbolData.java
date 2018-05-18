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

public class SymbolData {
    private final String Url;
    private final String AuthorizationKey;
    //private SymbolsJSON symJSON;
    private int symID;
    public SymbolData(String apiHost, String Key, String symbol) {

        this.Url = apiHost+"v1/symbols/search?prefix=" + symbol ;
        this.AuthorizationKey = "Bearer "+ Key;
    }

    public int run() throws Exception {
        int code=0;
        try(Response response = new OkHttpClient().newCall(new Request.Builder()
                .url(Url)
                .get()
                .header("Authorization", AuthorizationKey)
                .build()).execute())
        {
            if (response.code() == 200) {
                try(Reader reader = new InputStreamReader(response.body().byteStream(), "UTF-8")) {
                    GsonBuilder gson_builder = new GsonBuilder();
                    Gson gson = gson_builder.create();
                    SymbolsJSON symJSON = gson.fromJson(reader, SymbolsJSON.class);
                    symID = symJSON.symbols.get(0).symbolId;
                    code = response.code();
                }
            }

        }
        return code;
    }

    public int getSymbolID() {return this.symID;}
}

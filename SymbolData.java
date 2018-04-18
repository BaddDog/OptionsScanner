package com.baddog.optionsscanner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Date;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Brian on 2018-02-10.
 *
 */

public class SymbolData {
    private final OkHttpClient client = new OkHttpClient();
    private final String Url;
    private final String AuthorizationKey;
    private final Gson gson = new Gson();
    private SymbolsJSON symJSON;

    public SymbolData(String apiHost, String Key, String symbol) {
        this.Url = apiHost+"v1/symbols/search?prefix=" + symbol ;
        this.AuthorizationKey = "Bearer "+ Key;
    }

    public int run() throws Exception {
        Request request = new Request.Builder()
                .url(Url)
                .get()
                .header("Authorization", AuthorizationKey)
                .build();

        Response response = client.newCall(request).execute();

        if (response.code() == 200) {
            GsonBuilder gson_builder = new GsonBuilder();
            Gson gson = gson_builder.create();
            InputStream is = response.body().byteStream();
            Reader reader = new InputStreamReader(is, "UTF-8");
            symJSON = gson.fromJson(reader,SymbolsJSON.class);
         }
        int code = response.code();
        response.close();
        return code;
    }
    public int getSymbolsSize() {
        return symJSON.symbols.size();
    }

    public int getSymbolID(int index) {
        return symJSON.symbols.get(index).symbolId;
    }
}

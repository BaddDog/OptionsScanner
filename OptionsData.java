package com.baddog.optionsscanner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Brian on 2018-02-17.
 */

public class OptionsData {
    private final OkHttpClient client = new OkHttpClient();
    private final String Url;
    private final String AuthorizationKey;
    private int ExpiryDateCount;
    List<OptionsJSON.OptionExpiryDateJSON> ExpiryDateJSON;

    public OptionsData(String apiHost, String Key, int symbolID) {
        this.Url = apiHost+"v1/symbols/"+ symbolID + "/options";
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
                    OptionsJSON optJSON = gson.fromJson(reader, OptionsJSON.class);
                    ExpiryDateCount = optJSON.getOptionExpiryDateJSONSize();
                    ExpiryDateJSON = optJSON.optionChain;
                    code = response.code();
                }
            }

        }
        return code;
    }



    public  int getExpiryDateCount() {
             return this.ExpiryDateCount;
    }



}

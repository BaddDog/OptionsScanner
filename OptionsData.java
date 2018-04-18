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
 * Created by Brian on 2018-02-17.
 */

public class OptionsData {
    private final OkHttpClient client = new OkHttpClient();
    private final String Url;
    private final String AuthorizationKey;
    private final Gson gson = new Gson();
    private OptionsJSON optJSON;

    public OptionsData(String apiHost, String Key, int symbolID) {
        this.Url = apiHost+"v1/symbols/"+ symbolID + "/options";
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
            gson_builder.setLenient();
            Gson gson2 = gson_builder.create();

            InputStream is = response.body().byteStream();
            Reader reader = new InputStreamReader(is, "UTF-8");
            optJSON = gson2.fromJson(reader ,OptionsJSON.class);
        }
        int code = response.code();
        response.close();
        return code;
    }



    public  int getExpiryDateCount() {
        if(optJSON!=null) {
            return this.optJSON.getOptionExpiryDateJSONSize();
        } else {
            return 0;
        }
    }

    public OptionsJSON.OptionExpiryDateJSON getExpiryDate(int index) {
        if (optJSON != null) {
            return this.optJSON.getOptionExpiryDateJSON(index);
        } else {
            return null;
        }
    }




}

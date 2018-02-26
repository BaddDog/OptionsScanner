package com.baddog.optionsscanner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static io.realm.internal.network.OkHttpAuthenticationServer.JSON;

/**
 * Created by Brian on 2018-02-24.
 */

public class OptionsInfo {
    private final OkHttpClient client = new OkHttpClient();
    private final String Url;
    private final String AuthorizationKey;
    public OptionInfoJSON optJSON;

    public OptionsInfo(String apiHost, String Key) {
        this.Url = apiHost + "v1/markets/quotes/options";
        this.AuthorizationKey = "Bearer " + Key;
    }

    public void run(OptionsInfoRequestJSON InfoRequestJSON) throws Exception {
        Gson gson = new Gson();
        String userJson = gson.toJson(InfoRequestJSON);

        //RequestBody formBody = RequestBody.create(JSON, userJson);
        RequestBody body = RequestBody.create(JSON, userJson);
        Request request = new Request.Builder()
                .url(Url)
                .post(body)
                .header("Authorization", AuthorizationKey)
                .build();

        Response response = client.newCall(request).execute();

        if (response.code() == 200) {
            GsonBuilder gson_builder = new GsonBuilder();
            gson_builder.setLenient();
            Gson gson2 = gson_builder.create();

            InputStream is = response.body().byteStream();
            Reader reader = new InputStreamReader(is, "UTF-8");
            optJSON = gson2.fromJson(reader, OptionInfoJSON.class);
        }

    }

}


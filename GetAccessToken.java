package com.baddog.optionsscanner;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import org.apache.http.client.ClientProtocolException;
import android.util.JsonReader;
import javax.net.ssl.HttpsURLConnection;



/**
 * Created by Brian on 2017-10-21.
 *
 */

public class GetAccessToken {

    public GetAccessToken() {
    }

    public JsonReader gettoken(String address,String token,String client_id,String client_secret,String redirect_uri,String grant_type) {
        JsonReader jsonReader = null;
        // Making HTTP request
        try {

            // Create URL
            URL githubEndpoint = new URL(address+"?client_id="+client_id+"&code="+token+"&grant_type="+grant_type+"&redirect_uri="+redirect_uri);
            // Create connection
            HttpsURLConnection conn = (HttpsURLConnection) githubEndpoint.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setUseCaches (false);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            if (conn.getResponseCode() == 200) {
                // Success
                // Further processing here
                InputStream responseBody = conn.getInputStream();
                InputStreamReader responseBodyReader =
                        new InputStreamReader(responseBody, "UTF-8");
                jsonReader = new JsonReader(responseBodyReader);
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    return jsonReader;
    }
}














/*
        try {
            // DefaultHttpClient
            httpClient = new DefaultHttpClient();
            httpPost = new HttpPost(address);

            params.add(new BasicNameValuePair("token_url", address));
            params.add(new BasicNameValuePair("code", token));
            params.add(new BasicNameValuePair("client_id", client_id));
            params.add(new BasicNameValuePair("client_secret", client_secret));
            params.add(new BasicNameValuePair("redirect_uri", redirect_uri));
            params.add(new BasicNameValuePair("grant_type", grant_type));

            httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
            httpPost.setEntity(new UrlEncodedFormEntity(params));
            HttpResponse httpResponse = httpClient.execute(httpPost);
            HttpEntity httpEntity = httpResponse.getEntity();
            is = httpEntity.getContent();

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "n");
            }
            is.close();

            json = sb.toString();
            Log.e("JSONStr", json);
        } catch (Exception e) {
            e.getMessage();
            Log.e("Buffer Error", "Error converting result " + e.toString());
        }
        // Parse the String to a JSON Object
        try {
            jObj = new JSONObject(json);
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
        }
        // Return JSON String
        return jObj;
    }

}
*/
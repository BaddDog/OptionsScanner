package com.baddog.optionsscanner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.commons.math3.stat.descriptive.rank.Median;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Date;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.sqrt;

/**
 * Created by Brian on 2018-01-19.
 * * Retrieve candle history for one symbol
 */

public class HistoryData {
    private OkHttpClient client = new OkHttpClient();
    private String Url = null;
    private String AuthorizationKey = null;
    private Gson gson = new Gson();
    private CandlesJSON Candles;

    private double IntervallicDeviation;
    private double IntervallicTrendBias;
    private double Skew;
    private double Kurtosis;

    private double CONSISTENCY_CONSTANT = 1.4826;

    public HistoryData(String apiHost, String Key, int symbolID, String date1, String date2) {
        this.Url = apiHost + "v1/markets/candles/" + symbolID + "?startTime=" + date1 + "&endTime=" + date2 + "&interval=OneDay";
        this.AuthorizationKey = "Bearer " + Key;
    }

    public void RetrieveData() throws Exception {
        Request request = new Request.Builder()
                .url(Url)
                .get()
                .header("Authorization", AuthorizationKey)
                .build();

        Response response = client.newCall(request).execute();

        DateSmith ds = new DateSmith();
        Date currentDate = new Date(System.currentTimeMillis());
        long intDate = ds.date2long(currentDate);


        if (response.code() == 200) {
            GsonBuilder gson_builder = new GsonBuilder();
            Gson gson = gson_builder.create();
            InputStream is = response.body().byteStream();
            Reader reader = new InputStreamReader(is, "UTF-8");
            Candles = gson.fromJson(reader, CandlesJSON.class);
        }
    }

    public double[] JSON2Array (CandlesJSON Candles) {
        List<CandleJSON> candleList = Candles.candles;
        double Prices[];
        Prices = new double[10];
        for (int i = 0; i<candleList.size(); i++) {
            // Retrieve data from JSON
            long priceDate = new DateSmith().StrDate2LongDate(candleList.get(i).start);
            double price = candleList.get(i).close;
            // Save in Array
            Prices[i]= price;
        }
        return Prices;
    }

    public double MedianAbsoluteDeviation( double[] Prices, int intervallicDays, int period) {
        // Calculate median
        Median mn = new Median();
        double median = mn.evaluate(Prices);

        // Calculate MAD
        double[] aXDiff = new double[Prices.length];
        int candleSize = Prices.length;
        double sumX, meanX, xDiff, sumDiff, sumSkew, sumKurtosis;
        int i, n;

        sumX = 0;
        for (i = max(candleSize - period, 0); i < candleSize - intervallicDays; i++) {
            xDiff = abs(Prices[i] - median);
            sumX += xDiff;
            aXDiff[i] = xDiff;
        }
        Median mn2 = new Median();
        return mn2.evaluate(aXDiff) * CONSISTENCY_CONSTANT;
    }

    public void SmoothOutliers() {

    }

    public void CalculateIntervallicDeviation( double[] Prices, int intervallicDays, int period)
    {

        // Calculate the standard deviation of changes to price over x days for the last period length of days
        // aDiff is pointer to temporary storage vector used for intervallic deviation calculations

        double[] aXDiff;
        int candleSize = Prices.length;
        aXDiff = new double[Prices.length];
        double sumX, meanX, xDiff, sumDiff, sumSkew, sumKurtosis;
        int i, n;

        sumX=0;   n=0;
        for( i=max(candleSize-period,0); i<candleSize-intervallicDays; i++)
        {
            double c1 = Prices[i+intervallicDays];
            double c2 = Prices[i];
            xDiff = (c1-c2)/c2*100;
            sumX += xDiff;
            aXDiff[n]=xDiff;
            n++;


        }
        meanX=sumX/n;
        sumDiff=0; sumSkew=0; sumKurtosis=0;
        for(i=0;i<n;i++)
        {

            sumDiff += (aXDiff[i]-meanX)*(aXDiff[i]-meanX);
            sumSkew += (aXDiff[i]-meanX)*(aXDiff[i]-meanX)*(aXDiff[i]-meanX);
            sumKurtosis += (aXDiff[i]-meanX)*(aXDiff[i]-meanX)*(aXDiff[i]-meanX)*(aXDiff[i]-meanX);
        }

        IntervallicDeviation = sqrt(sumDiff/n);
        IntervallicTrendBias = meanX;
        Skew = sumSkew/n/(IntervallicDeviation*IntervallicDeviation*IntervallicDeviation);
        Kurtosis = (sumKurtosis/n/(IntervallicDeviation*IntervallicDeviation*IntervallicDeviation*IntervallicDeviation))-3;
    }

    public double getClose(int index) {
        return Candles.candles.get(index).close;
    }
    public double getIntervallicDeviation() { return this.IntervallicDeviation; }
    public double getIntervallicTrendBias() { return this.IntervallicTrendBias; }

}


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
    private String APIHost = null;
    private String StartDate = null;
    private String LastDate = null;
    private String AuthorizationKey = null;

    private Gson gson = new Gson();
    public CandlesJSON Candles;

    private double IntervallicDeviation;
    private double IntervallicTrendBias;
    private double Skew;
    private double Kurtosis;

    private double CONSISTENCY_CONSTANT = 1.4826;

    public HistoryData(String apiHost, String key, String date1, String date2) {
        this.APIHost = apiHost;
        this.StartDate = date1;
        this.LastDate = date2;
        this.AuthorizationKey = "Bearer " + key;
    }

    public int RetrieveSymbolData(int symbolID) throws Exception {
        String Url = this.APIHost + "v1/markets/candles/" + symbolID + "?startTime=" + this.StartDate + "&endTime=" + this.LastDate + "&interval=OneDay";
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
            Candles = gson.fromJson(reader, CandlesJSON.class);

        }
        int code = response.code();
        response.close();
        return code;
    }

    public double[] JSON2Array (CandlesJSON Candles) {
        List<CandleJSON> candleList = Candles.candles;
        double Prices[] = new double[candleList.size()];
        for (int i = 0; i<candleList.size(); i++) {
            // Retrieve data from JSON
            double price = candleList.get(i).close;
            // Save in Array
            Prices[i]= price;
        }
        return Prices;
    }

    // Calulated MAD, adjusted with consistency constant to replicate standard deviation
    // (https://eurekastatistics.com/using-the-median-absolute-deviation-to-find-outliers/)
    public double[] MedianAbsoluteDeviation( double[] Prices) {
        List<CandleJSON> candleList = Candles.candles;
        int candleSize = Prices.length;
        double ABSPriceChange[] = new double[candleList.size()-1];
        // Populate ABS(PriceChange) array
        for (int i=0;i<candleSize-1;i++) {
            ABSPriceChange[i] = abs(Prices[i+1]-Prices[i]);
        }
        // Calculate median
        Median mn = new Median();
        double median = mn.evaluate(ABSPriceChange);

        // Calculate MAD
        double[] AbsDiff = new double[ABSPriceChange.length];

        for (int i=0;i<ABSPriceChange.length-1;i++) {
            AbsDiff[i] = abs(ABSPriceChange[i] - median);
        }
        double[] returnValues = new double[2];
        Median mn2 = new Median();
        returnValues[0] = median;
        returnValues[1] = mn2.evaluate(AbsDiff) * CONSISTENCY_CONSTANT;
        return returnValues;
    }

    // Smooth out prices by replacing outliers with a median ammount.
    public double[] SmoothOutliers (double[] Prices, double median, double MAD, double StdDeviations)
    {
        double SmoothedPrices[] = new double[Prices.length];
        double maximumPriceChange = MAD*StdDeviations;

        // PriceChangeFactor stores the amount of change future prices need to be adjusted
        double PriceChangeFactor = 0;

         // Scan though prices. If price change grater than maximumpricechange, then adjust the prices
        int counter=0;
        SmoothedPrices[0]=Prices[0];
        for (int i=1;i<Prices.length;i++) {
            double pricechange = Prices[i]-Prices[i-1];
            if(pricechange>=0) {
             // Price increased or no price change
                if(pricechange>maximumPriceChange) {
                    // Price is outlier and needs to be adjusted
                    PriceChangeFactor -= (pricechange - maximumPriceChange);
                    counter++;
                }
            }
            if(pricechange<0) {
                // Price decreased
                if(abs(pricechange)>maximumPriceChange) {
                    // Price is outlier and needs to be adjusted
                    PriceChangeFactor += abs(pricechange)-maximumPriceChange;
                    counter++;
                }
            }
            // Smooth price and save in SmoothedPrices array
            SmoothedPrices[i]=Prices[i]+PriceChangeFactor;
        }

        return SmoothedPrices;
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

            xDiff = (c1-c2);
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


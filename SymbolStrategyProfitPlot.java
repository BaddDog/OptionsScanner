package com.baddog.optionsscanner;

import android.app.Activity;
import android.content.Intent;
import android.graphics.*;
import android.os.Bundle;

import com.androidplot.util.PixelUtils;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.*;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.*;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class SymbolStrategyProfitPlot extends Activity {

    private Realm realm;
    private XYPlot plot;
    private String apiServer;
    private String OAUTH_TOKEN;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.symbolstrategyprofitplot);

        realm = Realm.getDefaultInstance();

        Intent intent = getIntent();
        int SymID = intent.getIntExtra("SYMBOL_ID", 0);
        int StrategyID = intent.getIntExtra("StrategyID", 0);
        apiServer = intent.getStringExtra("apiserver");
        OAUTH_TOKEN = intent.getStringExtra("oauthtoken");
        int TARGET_TRADE_VALUE = intent.getIntExtra("TargetTradeValue", 0);

        RealmResults<Strategy> sl = realm.where(Strategy.class).equalTo("strategyID", StrategyID).findAll();

        if (sl != null) {
            Strategy strat = sl.get(0);
            // Variables
            int iterations = 10;
            int days = 10;
            int daystillexpiry = (int)strat.getDaysTillExpiration(realm);
            int totaldaysback = (iterations * days) + daystillexpiry + 1;

            Number Volatility[][] = new Number[5][iterations];

            // initialize our XYPlot reference:
            plot = (XYPlot) findViewById(R.id.plot);
            TradeDateCalc tdc = new TradeDateCalc();

            // Retrieve candle information for symbol
            HistoryData HistoryJSON = new HistoryData(apiServer, OAUTH_TOKEN, tdc.long2StrDate(tdc.LongNow() - totaldaysback), tdc.long2StrDate(tdc.LongNow()));
            try {
                while (HistoryJSON.RetrieveSymbolData(SymID) != 200) {
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Scan through different levels of data smoothing
            for (int sigma = 6; sigma > 1; sigma--) {

                // Move symbol prices into array
                double candles[] = HistoryJSON.JSON2Array(HistoryJSON.Candles);
                // Calculate median and Median Average Deviation
                double dataArray[] = new double[2];
                dataArray = HistoryJSON.MedianAbsoluteDeviation(candles);
                double median = dataArray[0];
                double MAD = dataArray[1];
                // Smooth outliera
                double[] SmoothedPrices = HistoryJSON.SmoothOutliers(candles, median, MAD, sigma);

                // Scan using increasing levels of days looked back
                for (int i = 0; i < iterations; i++) {
                    int lookback = (i * days) + daystillexpiry;
                    HistoryJSON.CalculateIntervallicDeviation(SmoothedPrices, daystillexpiry, lookback);
                    //Volatility[sigma][i] = HistoryJSON.getIntervallicDeviation();

                    ProfitAnalyzer PA = new ProfitAnalyzer();
                    double MedianPrice = strat.getCallOption().getLastTradePrice();
                    int investmentdays = tdc.InvestmentDaysTill(realm, strat.getDaysTillExpiration(realm));
                    Options CO = strat.getCallOption();
                    Options PO = strat.getPutOption();
                    double CallPremium = CO.getPremium();
                    double PutPremium = CO.getPremium();
                    double Callnp = PA.CalcOptionNetProfitability(CO.getOptionType(), CallPremium, CO.getStrikePrice(), MedianPrice,
                            HistoryJSON.getIntervallicDeviation(), investmentdays);
                    double Putnp = PA.CalcOptionNetProfitability(PO.getOptionType(), PutPremium, PO.getStrikePrice(), MedianPrice,
                            HistoryJSON.getIntervallicDeviation(), investmentdays);

                    int contracts = Math.max((int) (TARGET_TRADE_VALUE / (CallPremium + PutPremium) / 100), 1);
                    double transactionFee = 9.95;
                    double FeePerContract = 2.00;
                    double TransactionFeesPerShare = (transactionFee + (FeePerContract * contracts)) * 2 / (contracts * 100);
                    double AllCostsPerShare = TransactionFeesPerShare + CallPremium + PutPremium;


                }


            }


            // create a couple arrays of y-values to plot:
            final Number[] domainLabels = {days, 2 * days, 3 * days, 4 * days, 5 * days, 6 * days, 7 * days, 8 * days, 9 * days, 10 * days, 11 * days};
            Number[] series1Numbers = {1, 4, 2, 8, 4, 16, 8, 32, 16, 64};
            Number[] series2Numbers = {5, 2, 10, 5, 20, 10, 40, 20, 80, 40};

            // turn the above arrays into XYSeries':
            // (Y_VALS_ONLY means use the element index as the x value)
            XYSeries series1 = new SimpleXYSeries(
                    Arrays.asList(Volatility[0]), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Series1");
            XYSeries series2 = new SimpleXYSeries(
                    Arrays.asList(Volatility[1]), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Series2");
            XYSeries series3 = new SimpleXYSeries(
                    Arrays.asList(Volatility[2]), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Series3");
            XYSeries series4 = new SimpleXYSeries(
                    Arrays.asList(Volatility[3]), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Series4");
            XYSeries series5 = new SimpleXYSeries(
                    Arrays.asList(Volatility[4]), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Series5");

            // create formatters to use for drawing a series using LineAndPointRenderer
            // and configure them from xml:
            LineAndPointFormatter series1Format =
                    new LineAndPointFormatter(Color.RED, Color.GREEN, Color.BLUE, null);
            LineAndPointFormatter series2Format =
                    new LineAndPointFormatter(Color.RED, Color.GREEN, Color.BLUE, null);
            LineAndPointFormatter series3Format =
                    new LineAndPointFormatter(Color.RED, Color.GREEN, Color.BLUE, null);
            LineAndPointFormatter series4Format =
                    new LineAndPointFormatter(Color.RED, Color.GREEN, Color.BLUE, null);
            LineAndPointFormatter series5Format =
                    new LineAndPointFormatter(Color.RED, Color.GREEN, Color.BLUE, null);


            // add a new series' to the xyplot:
            plot.addSeries(series1, series1Format);
            plot.addSeries(series2, series2Format);
            plot.addSeries(series3, series3Format);
            plot.addSeries(series4, series4Format);
            plot.addSeries(series5, series5Format);

            plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).setFormat(new Format() {
                @Override
                public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
                    int i = Math.round(((Number) obj).floatValue());
                    return toAppendTo.append(domainLabels[i]);
                }

                @Override
                public Object parseObject(String source, ParsePosition pos) {
                    return null;
                }
            });
        }
    }
}
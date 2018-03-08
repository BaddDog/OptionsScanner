package com.baddog.optionsscanner;

import org.apache.commons.math3.stat.descriptive.rank.Percentile;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static java.lang.Math.abs;
import static java.lang.Math.exp;
import static java.lang.Math.sqrt;

/**
 * Created by Brian on 2018-02-28.
 */

public class ProfitAnalyzer {
   double percentile[] = new double[81];

 public ProfitAnalyzer() {

     for (int i=40; i>=-40; i--) {
         double z1 = (double) (i+1) / 10;
         double z0 = (double) (i) / 10;
         percentile[i+40]= phi(z1) - phi(z0);
     }
}

 public double CalcProfitability(double CallStrikePrice, double PutStrikePrice, double MedianPrice, double stdDev, long daysTillExpiry, double FeesPerShare) {
     double prob, Score, score, probsum;

     if (CallStrikePrice > 0 && PutStrikePrice > 0) {
         Score = 0;
         probsum = 0;
         for (int i = 40; i >= -40; i--) {
             double sd = ((double) i / 10);
             double price = MedianPrice + (sd * stdDev);
             prob = percentile[i + 40];
             probsum += prob;
             if (price > CallStrikePrice) {
                 Score += prob * (price - CallStrikePrice);
             }
             if (price < PutStrikePrice) {
                 Score += prob * (PutStrikePrice - price);
             }
         }
         Score -= FeesPerShare;
         score = ((Score / FeesPerShare) * (250 / (double) daysTillExpiry) * 100);
         return score;
     } else return 0;
 }



    double phi(double x)
    {
        int sign;
        double t, y;
        // constants
        double a1 =  0.254829592;
        double a2 = -0.284496736;
        double a3 =  1.421413741;
        double a4 = -1.453152027;
        double a5 =  1.061405429;
        double p  =  0.3275911;
        // Save the sign of x
        sign = 1;
        if (x < 0) sign = -1;
        x = abs(x)/sqrt(2.0);

        // A&S formula 7.1.26
        t = 1.0/(1.0 + p*x);
        y = 1.0 - (((((a5*t + a4)*t) + a3)*t + a2)*t + a1)*t*exp(-x*x);
        return 0.5*(1.0 + sign*y);
    }


}

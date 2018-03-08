package com.baddog.optionsscanner;

import io.realm.RealmObject;
import io.realm.annotations.Required;

/**
 * Created by Brian on 2018-01-12.
 *
 */

public class Strategy extends RealmObject {

    private Options callOption;
    private Options putOption;
    private StrategyTypes strategyType;

    public Strategy() {
    }

    public Strategy(Options callOption, Options putOption) {
        this.callOption = callOption;
        this.putOption = putOption;
    }

    // Setters
    public void setCallOption(Options option) {
        this.callOption = option;
    }
    public void setPutOption(Options option) {
        this.putOption = option;
    }

    public double setScore(double CallStrikePrice, double PutStrikePrice, double MedianPrice, double stdDev, long daysTillExpiry, double FeePerShare) {
        ProfitAnalyzer PA = new ProfitAnalyzer();
        return PA.CalcProfitability(CallStrikePrice, PutStrikePrice, MedianPrice, stdDev, daysTillExpiry, FeePerShare);
    }
}

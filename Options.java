package com.baddog.optionsscanner;

import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.LinkingObjects;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Created by Brian on 2018-01-12.
 *
 */

public class Options extends RealmObject {
    @PrimaryKey
    private int OptionID;
    private String OptionType;   // "Call or "Put"
    private double LastTradePrice;
    private long LastTradePriceDateTime;

    @LinkingObjects("OptionsList")
    private final RealmResults <StrikePrices> strike ;

    public Options() {this.strike = null;}

    public Options(RealmResults<StrikePrices> strike) {
        this.strike = strike;
    }

}

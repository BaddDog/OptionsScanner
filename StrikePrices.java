package com.baddog.optionsscanner;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.LinkingObjects;

/**
 * Created by Brian on 2018-01-16.
 *
 */

public class StrikePrices extends RealmObject {
    private double StrikePrice;
    private int callSymbolId;
    private int putSymbolId;
   // private RealmList<Options> OptionsList;

    @LinkingObjects("StrikePriceList")
    private final RealmResults<ChainPerRoot> rootChain ;

    public StrikePrices() {
        this.rootChain = null;
    }

    public StrikePrices(RealmResults<ChainPerRoot> chain) {
        this.rootChain = chain;
    }
}


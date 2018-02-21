package com.baddog.optionsscanner;


import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.LinkingObjects;

/**
 * Created by Brian on 2018-02-20.
 */

public class ChainPerRoot extends RealmObject {
    private String optionRoot;
    private int multiplier;
    private RealmList<StrikePrices> chainPerStrikePrice;

    @LinkingObjects("ChainPerRootList")
    private final RealmResults <ExpiryDates> expiry ;

    public ChainPerRoot() {this.expiry = null;}

    public ChainPerRoot(RealmResults<ExpiryDates> expiry) {
        this.expiry = expiry;
    }

}

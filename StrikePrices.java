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
    private Options CallOption;
    private Options PutOption;
    private RealmList<Options> OptionsList;

    @LinkingObjects("StrikePriceList")
    private final RealmResults<ExpiryDates> expiry ;

    public StrikePrices() {
        this.expiry = null;
    }

    public StrikePrices(RealmResults<ExpiryDates> expiry) {
        this.expiry = expiry;
    }
}


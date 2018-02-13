package com.baddog.optionsscanner;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.LinkingObjects;

/**
 * Created by Brian on 2018-01-16.
 *
 */

public class ExpiryDates extends RealmObject {
    private long ExpiryDate;
    private int daysTillExpiry;
    private RealmList <StrikePrices> StrikePriceList;

    @LinkingObjects("ExpiryDateList")
    private final RealmResults <Symbols> sym ;

    public ExpiryDates() {this.sym = null;}

    public ExpiryDates(RealmResults<Symbols> sym) {
        this.sym = sym;
    }
}

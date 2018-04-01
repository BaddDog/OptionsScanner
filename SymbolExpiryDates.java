package com.baddog.optionsscanner;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.LinkingObjects;

/**
 * Created by Brian on 2018-03-04.
 */

public class SymbolExpiryDates extends RealmObject {
    private long LongExpiryDate;
    private RealmList<Options> CallOptionsList;
    private RealmList<Options> PutOptionsList;
    @LinkingObjects("ExpiryList")
    private final RealmResults<Symbols> underlying = null;

    public SymbolExpiryDates() {
        CallOptionsList = null;
        PutOptionsList = null;

    }

    public RealmList getCallOptionsList() {
        return this.CallOptionsList;
    }
    public RealmList getPutOptionsList() {
        return this.PutOptionsList;
    }

    public void setLongExpiryDate(long longdate) {
        this.LongExpiryDate = longdate;
    }

    public void Add2CallOptionsList(Options opt) {
        this.CallOptionsList.add(opt);
    }
    public void Add2PutOptionsList(Options opt) {
        this.PutOptionsList.add(opt);
    }

    public Symbols getUnderlyingSymbolObject() {return this.underlying.get(0); }
    public long getLongExpiryDate() {return this.LongExpiryDate; }
    public long getDaysTillExpiry(Realm realm, long LongDate) {
        TradeDateCalc tdc = new TradeDateCalc();
        return tdc.TradeDaysTill(realm, LongDate);
    }
}

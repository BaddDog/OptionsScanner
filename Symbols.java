package com.baddog.optionsscanner;


import java.util.Date;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;


/**
 * Created by Brian on 2018-01-10.
 *
 */

public class Symbols extends RealmObject {

    private String symbol;
    private int symbolID;
    private double lastTradePrice;
    private long LastTradePriceDateTime;
    private long calcDate;
    private double volatility_a;
    private double volatility_b;
    private double trendbias_a;
    private double trendbias_b;
    private RealmList <ExpiryDates> ExpiryDateList;

    public Symbols() {}

    // setters
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
    public void setSymbolId(int id) {
        this.symbolID = id;
    }
    public void setCalcDate(long calcdate) {
        this.calcDate = calcdate;
    }
    public void setLastTradePrice(double price) {
        this.lastTradePrice = price;
    }
    public void setLastTradePriceDateTime (long datetime) {
        this.LastTradePriceDateTime = datetime;
    }

    // getters
    public int getSymbolID() {
        return this.symbolID;
    }

    public String getSymbol() {
        return this.symbol;
    }

    public long getCalcDate() {return this.calcDate;  }

    public void PopulateSymbols(Realm realm) {
            AddSymbol(realm, "AAPL");
            AddSymbol(realm, "EEM");
            AddSymbol(realm, "IWM");
            AddSymbol(realm, "UVXY");
    }

    private void AddSymbol(Realm realm, String symbol) {
        realm.beginTransaction();
            Symbols sym = realm.createObject(Symbols.class);
            sym.setSymbol(symbol);
        realm.commitTransaction();
    }
}

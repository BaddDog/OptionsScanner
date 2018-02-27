package com.baddog.optionsscanner;

import io.realm.Realm;
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
    private int OptionID;
    private String OptionType;   // "Call or "Put"
    private long LongExpiryDate;
    private double StrikePrice;
    private int underlyingID;
    // Save pricing and open interest information at date time 'LastTradePriceDateTime'
    private double LastTradePrice;
    private long LastTradePriceDateTime;
    private int openInterest;


    public Options() {}

    //public Options(RealmResults<Symbols> underlying) {this.underlyingSymbol = underlying;    }

    // Setters
    public void setOptionID(int optionid) {
        this.OptionID = optionid;
    }
    public void setOptionType(String type) {
        this.OptionType = type;
    }
    public void setLastTradePrice(double price) {this.LastTradePrice = price;}
    public void setLastTradePriceDateTime(long datetime) {
        this.LastTradePriceDateTime = datetime;
    }
    public void setopenInterest(int op) {
        this.openInterest = op;
    }
    public void setLongExpiryDate(long longdate) {
        this.LongExpiryDate = longdate;
    }
    public void setStrikePrice(double price) {
        this.StrikePrice = price;
    }
    public void setUnderlyingID(int symbolID) {
        this.underlyingID = symbolID;
    }
}

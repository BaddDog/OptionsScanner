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
    //private int UnderlyingSymbolID;
    //private long LongExpiryDate;
    private double StrikePrice;
    @LinkingObjects("CallOptionsList")
    private final RealmResults<ExpirationDates> CallExpiry = null;
    private double Score;
    // Save pricing and open interest information at date time 'LastTradePriceDateTime'
    private double LastTradePrice;
    private long LastTradePriceDateTime;
    private double askPrice;
    private double bidPrice;
    private int openInterest;


    public Options() {
    }


    public void setOptionType(String type) {
        this.OptionType = type;
    }
    public void setLastTradePrice(double price) {this.LastTradePrice = price;}
    public void setLastTradePriceDateTime(long datetime) {
        this.LastTradePriceDateTime = datetime;
    }
    public void setAskPrice(double price) {this.askPrice = price;}
    public void setBidPrice(double price) {this.bidPrice = price;}
    public void setopenInterest(int op) {
        this.openInterest = op;
    }
    public void setScore(double sc) {
        this.Score = sc;
    }

    public void setStrikePrice(double price) {
        this.StrikePrice = price;
    }
    public void setOptionID(int ID) {this.OptionID = ID;}

    public double getLastTradePrice() {return this.LastTradePrice;}
    public long getLastTradePriceDateTime() {return this.getLastTradePriceDateTime();}
    public double getAskPrice() {return this.askPrice;}
    public double getBidPrice() {return this.bidPrice;}
    public double getScore() {return this.Score;}
    public double getStrikeprice() {return this.StrikePrice;}

    //public long getLongExpiryDate() {return this.LongExpiryDate;}
   // public long getUnderlyingSymbolID() {return this.UnderlyingSymbolID;}

    //public int getDaysTillExpiry() {
    //    DateSmith ds = new DateSmith();
    //    return (int) (this.LongExpiryDate - ds.LongNow());
    //}

    public ExpirationDates getExpirationDateObject() {
    return this.CallExpiry.first();
    }

    double getPremium () {
        if (this.askPrice>0) {
            return this.askPrice;
        } else return this.LastTradePrice;
    }
}

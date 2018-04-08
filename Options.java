package com.baddog.optionsscanner;

import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.LinkingObjects;

/**
 * Created by Brian on 2018-01-12.
 *
 */

public class Options extends RealmObject {
    private int OptionID;
    private String OptionType;   // "CALL or "PUT"
    //private int UnderlyingSymbolID;
    //private long LongExpiryDate;
    private double StrikePrice;
    @LinkingObjects("CallOptionsList")
    private final RealmResults<SymbolExpiryDates> OptionExpiry = null;


    // Save pricing and open interest information at date time 'LastTradePriceDateTime'
    private double LastTradePrice;
    private long LastTradePriceDateTime;
    private double askPrice;
    private double bidPrice;
    private int openInterest;

    private double netProfitability;


    public Options() {
    }

    //public Options(RealmResults<Symbols> underlying) {this.underlyingSymbol = underlying;    }
    public void setOptionType(String type) {
        this.OptionType = type;
    }
   // public void setLongExpiryDate(long exp) {this.LongExpiryDate = exp;    }
   // public void setUnderlyingSymbolID(int id) {this.UnderlyingSymbolID = id; }
    public void setLastTradePrice(double price) {this.LastTradePrice = price;}
    public void setLastTradePriceDateTime(long datetime) {
        this.LastTradePriceDateTime = datetime;
    }
    public void setAskPrice(double price) {this.askPrice = price;}
    public void setBidPrice(double price) {this.bidPrice = price;}
    public void setopenInterest(int op) {
        this.openInterest = op;
    }
    public void setStrikePrice(double price) {
        this.StrikePrice = price;
    }
    public void setOptionID(int ID) {this.OptionID = ID;}
    public void setNetProfitability(double profitability) {
        this.netProfitability = profitability;
    }

    public double getLastTradePrice() {return this.LastTradePrice;}
    public long getLastTradePriceDateTime() {return this.getLastTradePriceDateTime();}
    public double getAskPrice() {return this.askPrice;}
    public double getBidPrice() {return this.bidPrice;}
    public double getStrikePrice() {return this.StrikePrice;}
    public double getnetProfitability() {return this.netProfitability;}
    public String getOptionType() {return this.OptionType;}
    public int getOptionID() {return this.OptionID;}
    public SymbolExpiryDates getExpirationDateObject() {
    return this.OptionExpiry.first();
    }

    double getPremium () {
        if (this.askPrice>0) {
            return this.askPrice;
        } else return  99.0;//this.LastTradePrice;
    }
}

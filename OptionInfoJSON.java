package com.baddog.optionsscanner;

import org.apache.commons.math3.special.Gamma;
import org.joda.time.DateTime;

import java.util.Enumeration;
import java.util.List;

/**
 * Created by Brian on 2018-02-24.
 */

public class OptionInfoJSON {
    List<OptionQuoteJSON> optionQuotes;

    public class OptionQuoteJSON {
        String underlying;  //String	Underlying name
        int underlyingId;	//Integer	Underlying ID
        String symbol;      //	String	Symbol name
        int symbolId;	    //Integer	Symbol ID
        double bidPrice;	//Double	Bid price
        int bidSize;        //	Integer	Bid size
        double askPrice;    //	Double	Ask price
        int askSize;        //	Integer	Ask size
        double lastTradePriceTrHrs; //	Double	Last trade price trade hours
        double lastTradePrice;	//Double	Last trade price
        int lastTradeSize;	//Integer	Last trade size
        String lastTradeTick;	//Enumeration Last trade tick
        String lastTradeTime;	//DateTime Last trade time
        int volume;         // Integer  Volume
        double openPrice;	//Double	Open price
        double highPrice;	//Double	High price
        double lowPrice;	//Double	Low price
        double volatility;	//Double	Volatility
        double delta;	//Double	Delta
        double gamma;	//Double	Gamma
        double theta;	//Double	Theta
        double vega;	//Double	Vega
        double rho;	//Double	Rho
        int openInterest;	//Integer	Open interest
        int delay;	//Integer	How much is data delayed
        boolean isHalted;	//Boolean	Whether or not the symbol was halted
        double VWAP;	//Double	Volume Weighted Average Price

        int getOpenInterest () {return this.openInterest;}


    }



}

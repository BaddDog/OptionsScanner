package com.baddog.optionsscanner;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Required;

/**
 * Created by Brian on 2018-01-12.
 *
 */

public class Strategy extends RealmObject {
    public static final String FIELD_ID = "id";
    private static AtomicInteger INTEGER_COUNTER = new AtomicInteger(0);

    private Options callOption;
    private Options putOption;
    private StrategyTypes strategyType;
    private double Score;


    public Strategy() {
    }

    //  create() & delete() needs to be called inside a transaction.
    static void create(Realm realm) {
        create(realm, false);
    }
    static void create(Realm realm, boolean randomlyInsert) {
        StrategyList parent = realm.where(StrategyList.class).findFirst();
        RealmList<Strategy> items = parent.getSymbolsList();
        Strategy counter = realm.createObject(Strategy.class, increment());
        if (randomlyInsert && items.size() > 0) {
            Random rand = new Random();
            items.listIterator(rand.nextInt(items.size())).add(counter);
        } else {
            items.add(counter);
        }
    }
    static void delete(Realm realm, long id) {
        Symbols item = realm.where(Symbols.class).equalTo(FIELD_ID, id).findFirst();
        // Otherwise it has been deleted already.
        if (item != null) {
            item.deleteFromRealm();
        }
    }

    private static int increment() {
        return INTEGER_COUNTER.getAndIncrement();
    }

    // Setters
    public void setCallOption(Options option) {
        this.callOption = option;
    }
    public void setPutOption(Options option) {
        this.putOption = option;
    }

    public void setScore(double CallStrikePrice, double PutStrikePrice, double MedianPrice, double stdDev, long daysTillExpiry, double FeePerShare) {
        ProfitAnalyzer PA = new ProfitAnalyzer();
        this.Score = PA.CalcProfitability(CallStrikePrice, PutStrikePrice, MedianPrice, stdDev, daysTillExpiry, FeePerShare);
     }
    // Getters
    public long getDaysTillExpiration() {return this.callOption.getExpirationDateObject().getDaysTillExpiry();}
    public double getCallPremium() {return this.callOption.getPremium();}
    public double getCallStrikeprice() {return this.callOption.getStrikePrice();}
    public double getPutPremium() {return this.putOption.getPremium();}
    public double getPutStrikeprice() {return this.putOption.getStrikePrice();}
    public double getScore() {return this.Score;}
    public Options getCallOption() {return this.callOption;}
    public int getid() {return this.hashCode();}
}

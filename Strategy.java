package com.baddog.optionsscanner;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.annotations.LinkingObjects;
import io.realm.annotations.Required;

/**
 * Created by Brian on 2018-01-12.
 *
 */

public class Strategy extends RealmObject {
    public static final String FIELD_ID = "id";
    private static AtomicInteger INTEGER_COUNTER = new AtomicInteger(0);

    private int strategyID;
    private Options callOption;
    private Options putOption;
    private StrategyTypes strategyType;
    private double Score;
    private double Score2;

    @LinkingObjects("StrategyList")
    private final RealmResults<Symbols> underlyingSymbol = null;


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
    public void setID(int id) {
        this.strategyID = id;
    }
    public void setCallOption(Options option) {
        this.callOption = option;
    }
    public void setPutOption(Options option) {
        this.putOption = option;
    }
    public void setScore(double score) {
        this.Score = score;
    }
    public void setScore2(double score) { this.Score2 = Score; }

    public void calcScore(double NetCallProfitability, double NetPutProfitability, double AllFeesPerShare, long investmentdays) {
        this.Score = (NetCallProfitability+NetPutProfitability-AllFeesPerShare)/AllFeesPerShare*(250/investmentdays) *100;
     }
    public void calcScore2(double NetCallProfitability, double NetPutProfitability, double AllFeesPerShare, long investmentdays) {
        this.Score2 = (NetCallProfitability+NetPutProfitability-AllFeesPerShare)/AllFeesPerShare*(250/investmentdays) *100;
    }
    // Getters
    public long getDaysTillExpiration(Realm realm) {
        TradeDateCalc tdc = new TradeDateCalc();
        long nDate =this.callOption.getExpirationDateObject().getLongExpiryDate();
        return tdc.TradeDaysTill(realm, nDate);}

    public double getCallPremium() {return this.callOption.getPremium();}
    public double getCallStrikeprice() {return this.callOption.getStrikePrice();}
    public double getPutPremium() {return this.putOption.getPremium();}
    public double getPutStrikeprice() {return this.putOption.getStrikePrice();}
    public double getScore() {return this.Score;}
    public double getScore2() {return this.Score2;}
    public Options getCallOption() {return this.callOption;}
    public Options getPutOption() {return this.putOption;}
    public int getid() {return this.hashCode();}

}

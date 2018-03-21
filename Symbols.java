package com.baddog.optionsscanner;




import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;


/**
 * Created by Brian on 2018-01-10.
 *
 */

public class Symbols extends RealmObject {
    public static final String FIELD_ID = "id";
    private static AtomicInteger INTEGER_COUNTER = new AtomicInteger(0);

    private int symbolID;
    private String symbol;
    private double lastTradePrice;
    private long LastTradePriceDateTime;
    private long calcDate;
    private double volatility_slope;
    private double volatility_intercept;
    private double trendbias_slope;
    private double trendbias_intercept;
    private RealmList<ExpirationDates> ExpiryList;
    private RealmList<Strategy> StrategyList;

    //  create() & delete() needs to be called inside a transaction.
    static void create(Realm realm) {
        create(realm, false);
    }
    static void create(Realm realm, boolean randomlyInsert) {
        SymbolList parent = realm.where(SymbolList.class).findFirst();
        RealmList<Symbols> items = parent.getSymbolsList();
        Symbols counter = realm.createObject(Symbols.class, increment());
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

    public void setLastTradePriceDateTime(long datetime) {
        this.LastTradePriceDateTime = datetime;
    }

    public void setVolatilitySlope(double slope) {
        this.volatility_slope = slope;
    }

    public void setVolatilityIntercept(double intercept) {
        this.volatility_intercept = intercept;
    }

    public void setTrendBiasSlope(double slope) {
        this.volatility_slope = slope;
    }

    public void setTrendBiasIntercept(double intercept) {
        this.volatility_intercept = intercept;
    }


    // getters
    public int getSymbolID() {
        return this.symbolID;
    }

    public String getSymbol() {
        return this.symbol;
    }

    public long getCalcDate() {
        return this.calcDate;
    }

    public double getLastTradePrice() {
        return this.lastTradePrice;
    }

    public RealmList getExpiryDates() {
        return this.ExpiryList;
    }

    public RealmList getStrategyList() {
        return this.StrategyList;
    }




    private void AddSymbol(Realm realm, String symbol) {
        realm.beginTransaction();
            Symbols sym = realm.createObject(Symbols.class);
            sym.setSymbol(symbol);
        realm.commitTransaction();
    }

    public void Add2ExpiryList(ExpirationDates exp) {
        this.ExpiryList.add(exp);
    }

    public double getVolatility(long dayTillExpiry) {
        double percentVolatility = (this.volatility_slope*dayTillExpiry)+this.volatility_intercept;
        return percentVolatility/100*this.lastTradePrice;
    }
    public double getTrendBias(long dayTillExpiry) {
        return (trendbias_slope*dayTillExpiry)+trendbias_intercept;
    }


    public String getSymbolIDString() {
        return Integer.toString(symbolID);
    }

    public double getBestScore() {
        double BestScore = -9999999;
        for (int i =0; i<this.StrategyList.size(); i++) {
            if(this.StrategyList.get(i).getScore()>BestScore) {
                BestScore = this.StrategyList.get(i).getScore();
            }
        }
        return BestScore;
    }

    public void AddStrategy(Strategy strat) {
        this.StrategyList.add(strat);
    }


}
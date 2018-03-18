package com.baddog.optionsscanner;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by Brian on 2018-03-11.
 */

public class StrategyList extends RealmObject {


    private RealmList<Strategy> StrategyList;


    public StrategyList() { StrategyList= new RealmList<Strategy>();   }

    public RealmList<Strategy> getSymbolsList() {
        return StrategyList;
    }

    public Strategy getStrategy(int index) {
        return StrategyList.get(index);
    }

    public RealmList<Strategy> getStrategyList() {
        return StrategyList;
    }

}




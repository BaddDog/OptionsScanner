package com.baddog.optionsscanner;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Created by Brian on 2018-01-12.
 *
 */

public class StrategyTypes extends RealmObject {
    @PrimaryKey
    private int strategyID;
    private String strategyDescription;
}

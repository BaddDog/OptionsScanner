package com.baddog.optionsscanner;

import io.realm.RealmObject;
import io.realm.annotations.Required;

/**
 * Created by Brian on 2018-01-12.
 *
 */

public class Strategy extends RealmObject {

    private Options callOption;
    private Options putOption;
    private StrategyTypes strategyType;
    private double Score;
}

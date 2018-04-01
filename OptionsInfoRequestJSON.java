package com.baddog.optionsscanner;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import io.realm.RealmList;

/**
 * Created by Brian on 2018-02-24.
 */

public class OptionsInfoRequestJSON {

    boolean stream;
    String mode;

    List<filterJSON> filters = new ArrayList();
    List<Integer> optionIds = new ArrayList();

    OptionsInfoRequestJSON() {    }

    OptionsInfoRequestJSON(int underlying, String expiry, double minPrice, double maxPrice, List OptionList) {
        filterJSON fj = new filterJSON(underlying, expiry, minPrice, maxPrice);
        this.filters.add(fj);
        this.optionIds=OptionList;
        this.stream= false;
        this.mode = "";
    }


    OptionsInfoRequestJSON(List<Integer> ListID) {
        this.optionIds=ListID;
    }

    OptionsInfoRequestJSON(int optID) {
        this.optionIds.add(optID);

    }

    public class filterJSON {
        private String optionType;
        private int underlyingId;
        private String expiryDate;
        private double minstrikePrice;
        private double maxstrikePrice;

        filterJSON(int underlying, String expiry, double minPrice, double maxPrice ) {
            this.optionType = "Call";
            this.underlyingId = underlying;
            this.expiryDate = expiry;
            this.minstrikePrice = minPrice;
            this.maxstrikePrice = maxPrice;
        }
    }
}

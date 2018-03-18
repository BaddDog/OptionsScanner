package com.baddog.optionsscanner;

import java.util.HashMap;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Created by Brian on 2018-03-11.
 */

public class SymbolList extends RealmObject {


    private RealmList<Symbols> SymbolsList;


    public SymbolList() { SymbolsList= new RealmList<Symbols>();   }

    public RealmList<Symbols> getSymbolsList() {
        return SymbolsList;
    }

    public Symbols getSymbol(int index) {
        return SymbolsList.get(index);
    }

    public void PopulateSymbols(Realm realm) {
        AddSymbol(realm, "AAPL");
      //  AddSymbol(realm, "EEM");
      //  AddSymbol(realm, "IWM");
      //  AddSymbol(realm, "UVXY");
    }
    private void AddSymbol(Realm realm, String symbol) {
        realm.beginTransaction();
            Symbols sym = realm.createObject(Symbols.class);
            this.SymbolsList.add(sym);
            sym.setSymbol(symbol);
        realm.commitTransaction();
    }
}




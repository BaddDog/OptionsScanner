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
        AddSymbol(realm, "EEM");
        AddSymbol(realm, "IWM");
        AddSymbol(realm, "UVXY");
        AddSymbol(realm, "USO");

              AddSymbol(realm, "GLD");
                       AddSymbol(realm, "XLE");
                                AddSymbol(realm, "XOP");
                                       AddSymbol(realm, "SLV");
                                                AddSymbol(realm, "GDX");
                                                        AddSymbol(realm, "HYG");
                                                                AddSymbol(realm, "FXI");
                                                                        AddSymbol(realm, "EFA");

              AddSymbol(realm, "TLT");
                AddSymbol(realm, "XLF");
                        AddSymbol(realm, "XME");
                                AddSymbol(realm, "DXJ");
                                        AddSymbol(realm, "IYR");
                                                       AddSymbol(realm, "FB");
                                                                AddSymbol(realm, "BAC");
                                                                        AddSymbol(realm, "NFLX");
                                                                                AddSymbol(realm, "PFE");
                                                                                        AddSymbol(realm, "XOM");
                                                                                                AddSymbol(realm, "VRX");
                                                                                                        AddSymbol(realm, "GILD");
                                                                                                                AddSymbol(realm, "RIG");
                                                                                                                        AddSymbol(realm, "AMZN");
                                                                                                                                AddSymbol(realm, "PG");
                                                                                                                                        AddSymbol(realm, "BABA");
AddSymbol(realm, "TSLA");
        AddSymbol(realm, "CVX");
                AddSymbol(realm, "TWTR");
                        AddSymbol(realm, "WLL");
                                AddSymbol(realm, "F");
                                        AddSymbol(realm, "SRPT");
                                                AddSymbol(realm, "FCX");
   //                                                     AddSymbol(realm, "TSL");
   //                                                             AddSymbol(realm, "QQQ");

    }
    private void AddSymbol(Realm realm, String symbol) {
        realm.beginTransaction();
            Symbols sym = realm.createObject(Symbols.class);
            this.SymbolsList.add(sym);
            sym.setSymbol(symbol);
        realm.commitTransaction();
    }
}




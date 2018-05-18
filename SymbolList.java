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

        AddSymbol(realm, "VRX");
        AddSymbol(realm, "AAPL");
        AddSymbol(realm, "EEM");
        AddSymbol(realm, "IWM");
/*
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
/*
        AddSymbol(realm, "AAL");
        AddSymbol(realm, "ABBV");
        AddSymbol(realm, "ABT");
        AddSymbol(realm, "ABX");

        AddSymbol(realm, "ACAD");

        AddSymbol(realm, "ACN");
        AddSymbol(realm, "AEP");
        AddSymbol(realm, "AFL");
        AddSymbol(realm, "AGN");
        AddSymbol(realm, "AGNC");
        AddSymbol(realm, "AIG");
        AddSymbol(realm, "ALLY");
        AddSymbol(realm, "AMAT");

        AddSymbol(realm, "ANF");
        AddSymbol(realm, "APC");
        AddSymbol(realm, "ARCC");
        AddSymbol(realm, "ATVI");
        AddSymbol(realm, "AVB");
        AddSymbol(realm, "BA");
        AddSymbol(realm, "BBT");
        AddSymbol(realm, "BBY");
        AddSymbol(realm, "BHP");
        AddSymbol(realm, "BMY");
        AddSymbol(realm, "BP");

        AddSymbol(realm, "BUD");
        AddSymbol(realm, "BURL");
        AddSymbol(realm, "BX");
        AddSymbol(realm, "BXP");
        AddSymbol(realm, "BZH");
        AddSymbol(realm, "C");
        AddSymbol(realm, "CAT");
        AddSymbol(realm, "CBOE");
        AddSymbol(realm, "CC");
        AddSymbol(realm, "CCI");
        AddSymbol(realm, "CDNS");
        AddSymbol(realm, "CELG");
        AddSymbol(realm, "CF");
        AddSymbol(realm, "CHS");
        AddSymbol(realm, "CL");
        AddSymbol(realm, "CLNY");
        AddSymbol(realm, "CLR");
        AddSymbol(realm, "CMA");
        AddSymbol(realm, "CNI");
        AddSymbol(realm, "CNQ");
        AddSymbol(realm, "COF");
        AddSymbol(realm, "COP");
        AddSymbol(realm, "COST");
        AddSymbol(realm, "CRM");
        AddSymbol(realm, "CS");
        AddSymbol(realm, "CSCO");
        AddSymbol(realm, "CSX");
        AddSymbol(realm, "CTL");
        AddSymbol(realm, "CVS");
        AddSymbol(realm, "DAL");
        AddSymbol(realm, "DB");
        AddSymbol(realm, "DDR");
        AddSymbol(realm, "DE");
        AddSymbol(realm, "DG");
        AddSymbol(realm, "DHI");
        AddSymbol(realm, "DHR");
        AddSymbol(realm, "DIS");
        AddSymbol(realm, "DLTR");
        AddSymbol(realm, "DNKN");
        AddSymbol(realm, "DOW");
        AddSymbol(realm, "DSW");
        AddSymbol(realm, "ENB");
        AddSymbol(realm, "EPD");
        AddSymbol(realm, "EQIX");
        AddSymbol(realm, "ETE");
        AddSymbol(realm, "ETP");
        AddSymbol(realm, "EUFN");
        AddSymbol(realm, "EWJ");
        AddSymbol(realm, "EWZ");
        AddSymbol(realm, "EXC");
        AddSymbol(realm, "FDX");
        AddSymbol(realm, "FNV");
        AddSymbol(realm, "FXE");
        AddSymbol(realm, "GDXJ");
        AddSymbol(realm, "GE");
        AddSymbol(realm, "GG");
        AddSymbol(realm, "GGP");
        AddSymbol(realm, "GIS");
        AddSymbol(realm, "GLNG");
        AddSymbol(realm, "GLW");
        AddSymbol(realm, "GM");
        AddSymbol(realm, "GMLP");
        AddSymbol(realm, "GNC");
        AddSymbol(realm, "GPS");
        AddSymbol(realm, "GS");
        AddSymbol(realm, "HAIN");
        AddSymbol(realm, "HAR");
        AddSymbol(realm, "HCN");
        AddSymbol(realm, "HCP");
        AddSymbol(realm, "HD");
        AddSymbol(realm, "HES");
        AddSymbol(realm, "HLF");
        AddSymbol(realm, "HPE");
        AddSymbol(realm, "HPQ");
        AddSymbol(realm, "HRL");
        AddSymbol(realm, "HUM");
        AddSymbol(realm, "HYG");
        AddSymbol(realm, "HZNP");
        AddSymbol(realm, "INTC");
        AddSymbol(realm, "JACK");
        //AddSymbol(realm, "JBLU");
        AddSymbol(realm, "JD");
        //AddSymbol(realm, "JNJ");
        AddSymbol(realm, "JPM");
        //AddSymbol(realm, "KBE");
        //AddSymbol(realm, "KEY");
        AddSymbol(realm, "KMI");
        AddSymbol(realm, "KO");
        //AddSymbol(realm, "KRE");
       // AddSymbol(realm, "KS");
        AddSymbol(realm, "LBTYA");
        AddSymbol(realm, "LLY");
        AddSymbol(realm, "LNG");
        AddSymbol(realm, "LOCK");

        AddSymbol(realm, "LOW");
        AddSymbol(realm, "LUV");
        AddSymbol(realm, "LVS");
        AddSymbol(realm, "M");
        AddSymbol(realm, "MCD");
        AddSymbol(realm, "MDLZ");
        AddSymbol(realm, "MDT");
        AddSymbol(realm, "MET");
        AddSymbol(realm, "MGM");
        AddSymbol(realm, "MMP");
        AddSymbol(realm, "MOS");
        AddSymbol(realm, "MPC");
        AddSymbol(realm, "MRK");
        AddSymbol(realm, "MRO");
        AddSymbol(realm, "MS");
        AddSymbol(realm, "MSFT");
        AddSymbol(realm, "MU");
        AddSymbol(realm, "NAVI");
        AddSymbol(realm, "NBR");
        AddSymbol(realm, "NKE");
        AddSymbol(realm, "NKTR");
        AddSymbol(realm, "NRF");
        AddSymbol(realm, "NTAP");
        AddSymbol(realm, "NVDA");
        AddSymbol(realm, "NYLD");
        AddSymbol(realm, "OAS");
        AddSymbol(realm, "OIH");
        AddSymbol(realm, "ORCL");
        AddSymbol(realm, "PAA");
        AddSymbol(realm, "PANW");
        AddSymbol(realm, "PE");
        AddSymbol(realm, "PEP");
        AddSymbol(realm, "PLD");
        AddSymbol(realm, "PNC");
        AddSymbol(realm, "POT");
        AddSymbol(realm, "PPL");
        AddSymbol(realm, "PRU");
        AddSymbol(realm, "PSX");
        AddSymbol(realm, "QCOM");
        AddSymbol(realm, "RAI");
        AddSymbol(realm, "RF");
        AddSymbol(realm, "RRC");
        AddSymbol(realm, "RSX");
        AddSymbol(realm, "SAFM");
        AddSymbol(realm, "SBGI");
        AddSymbol(realm, "SBUX");
        AddSymbol(realm, "SCHW");
        AddSymbol(realm, "SHLD");
        AddSymbol(realm, "SHPG");
        AddSymbol(realm, "SIG");
        AddSymbol(realm, "SLW");
        AddSymbol(realm, "SO");
        AddSymbol(realm, "SPY");
        AddSymbol(realm, "SRE");
        AddSymbol(realm, "STLD");
        AddSymbol(realm, "STWD");
        AddSymbol(realm, "STZ");
        AddSymbol(realm, "SVXY");
        AddSymbol(realm, "SXL");
        AddSymbol(realm, "SYF");
        AddSymbol(realm, "SYMC");
        AddSymbol(realm, "T");
        AddSymbol(realm, "TEVA");
        AddSymbol(realm, "TGT");
        AddSymbol(realm, "TOL");
        AddSymbol(realm, "TRIP");
        AddSymbol(realm, "TSN");
        AddSymbol(realm, "TWX");
        AddSymbol(realm, "UA");
        AddSymbol(realm, "ULTA");
        AddSymbol(realm, "UNP");
        AddSymbol(realm, "UPS");
        AddSymbol(realm, "UUP");
        AddSymbol(realm, "V");
        AddSymbol(realm, "VIPS");
        AddSymbol(realm, "VOD");
        AddSymbol(realm, "VRX");
        AddSymbol(realm, "VTR");
        AddSymbol(realm, "VZ");
        AddSymbol(realm, "WBA");
        AddSymbol(realm, "WDC");
        AddSymbol(realm, "WFC");
        AddSymbol(realm, "WMB");
        AddSymbol(realm, "WMT");
        AddSymbol(realm, "WSM");
        AddSymbol(realm, "WU");
        AddSymbol(realm, "WYN");
        AddSymbol(realm, "WYNN");
        AddSymbol(realm, "X");
        AddSymbol(realm, "XBI");
        AddSymbol(realm, "XHB");
        AddSymbol(realm, "XLB");
        AddSymbol(realm, "XLI");
        AddSymbol(realm, "XLK");
        AddSymbol(realm, "XLP");
        AddSymbol(realm, "XLU");
        AddSymbol(realm, "XLV");
        AddSymbol(realm, "XLY");
        AddSymbol(realm, "YHOO");
        AddSymbol(realm, "YY");
      //  AddSymbol(realm, "Z");
     //   AddSymbol(realm, "ZION");
*/
    }
    private void AddSymbol(Realm realm, String symbol) {
        realm.beginTransaction();
            Symbols sym = realm.createObject(Symbols.class);
            this.SymbolsList.add(sym);
            sym.setSymbol(symbol);
        realm.commitTransaction();
    }
}




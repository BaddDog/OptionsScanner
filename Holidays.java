package com.baddog.optionsscanner;

import java.text.ParseException;
import java.util.Date;
import java.text.SimpleDateFormat
;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Brian on 2018-01-11.
 *
 */

public class Holidays extends RealmObject {

    private long LongDate;
    private String SDate;
    private String Description;


    public Holidays() {}

    // setters
    private void setLongDate(long LongDate) {
        this.LongDate = LongDate;
    }
    private void setSDate(String SDate) {
        this.SDate = SDate;
    }
    private void setDescription(String Description) {
        this.Description = Description;
    }

    public long holidays(Realm realm, Date ExpiryDate1, Date ExpiryDate2) {
        DateSmith ds = new DateSmith();
        long dn1 = ds.date2long(ExpiryDate1);
        long dn2 = ds.date2long(ExpiryDate2);
        final RealmQuery<Holidays> query = realm.where(Holidays.class).between("LongDate", dn1, dn2);
        return query.count();
    }

    public void PopulateHolidays(Realm realm) {

        AddHoliday(realm, 17581,"2018/02/19","President's Day - U.S.");
        AddHoliday(realm,17620,"2018/03/30","Good Friday");
        AddHoliday(realm,17679,"2018/05/28","Memorial Day - U.S.");
        AddHoliday(realm,17716,"2018/07/04","Independence Day - U.S.");
        AddHoliday(realm,17777,"2018/09/03","Labor Day - U.S.");
        AddHoliday(realm,17857,"2018/11/22","Thanksgiving Day - U.S.");
        AddHoliday(realm,17890,"2018/12/25","Christmas Day");
        AddHoliday(realm,17897,"2019/01/01","New Year's Day (Observed)");
        AddHoliday(realm,17917,"2019/01/21","Martin Luther King, Jr. Day");
        AddHoliday(realm,17945,"2019/02/18","President's Day - U.S.");
    }

    private void AddHoliday(Realm realm, long LongDate, String sDate, String Description) {
        realm.beginTransaction();
                Holidays hol = realm.createObject(Holidays.class);
                hol.setLongDate(LongDate);
                hol.setSDate(sDate);
                hol.setDescription(Description);
        realm.commitTransaction();

    }

}




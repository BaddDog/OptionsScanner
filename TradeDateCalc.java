package com.baddog.optionsscanner;

import org.joda.time.DateTimeConstants;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.Weeks;

import java.util.Calendar;
import java.util.Date;


import io.realm.Realm;

/**
 * Created by Brian on 2018-03-29.
 */

public class TradeDateCalc extends DateCalc {

    private static final int DAYS_PER_WEEKEND = 2;
    private static final int WEEK_START = DateTimeConstants.MONDAY;
    private static final int WEEK_END = DateTimeConstants.FRIDAY;

    TradeDateCalc() {
    }

    int workdayDiff(Realm realm, long longStartDate, long longEndDate) {
        Date d1 =this.long2Date(longStartDate);
        Date d2 =this.long2Date(longEndDate);
        //int daysBetween = Days.daysBetween(start, end).getDays();
        int daysBetween = (int)(longEndDate-longStartDate);
        //int weekendsBetween = Weeks.weeksBetween(start.withDayOfWeek(WEEK_START), end.withDayOfWeek(WEEK_START)).getWeeks();
        Date now = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        int weekenddays = 0;
        for(int i = 0;i<=daysBetween;i++) {
            cal.add(Calendar.DAY_OF_YEAR, 1);
            if(cal.get(Calendar.DAY_OF_WEEK)==1 || cal.get(Calendar.DAY_OF_WEEK)==7) weekenddays++;
        }
        // holidays in between
        Holidays hol = new Holidays();
        long hDays = hol.holidays(realm, d1, d2);
        return daysBetween - weekenddays - (int)hDays;
    }

    int workdayDiff2(Realm realm, Date StartDate, Date EndDate) {
         //int daysBetween = Days.daysBetween(start, end).getDays();
        int daysBetween = (int)(EndDate.getTime()-StartDate.getTime())/86400000;
        //int weekendsBetween = Weeks.weeksBetween(start.withDayOfWeek(WEEK_START), end.withDayOfWeek(WEEK_START)).getWeeks();
        Date now = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        int weekenddays = 0;
        for(int i = 0;i<=daysBetween;i++) {
            cal.add(Calendar.DAY_OF_YEAR, 1);
            if(cal.get(Calendar.DAY_OF_WEEK)==1 || cal.get(Calendar.DAY_OF_WEEK)==7) weekenddays++;
        }
        // holidays in between
        Holidays hol = new Holidays();
        long hDays = hol.holidays(realm, StartDate, EndDate);
        return daysBetween - weekenddays - (int)hDays;
    }

    LocalDate toWorkday(LocalDate d) {
        if (d.getDayOfWeek() > WEEK_END) {
            return d.plusDays(DateTimeConstants.DAYS_PER_WEEK - d.getDayOfWeek() + 1);
        }
        return d;
    }



    int TradeDaysTill(Realm realm, long longDate) {
        return workdayDiff(realm, this.LongNow(), longDate);
    }

    int InvestmentDaysTill(Realm realm, long longDate) {
        return workdayDiff(realm, this.LongNow(), longDate)+1;
    }

}


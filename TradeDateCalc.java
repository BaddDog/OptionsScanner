package com.baddog.optionsscanner;

import org.joda.time.DateTimeConstants;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.Weeks;

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
        LocalDate start = LocalDate.fromDateFields(d1);
        LocalDate end = LocalDate.fromDateFields(d2);

        start = toWorkday(start);
        end = toWorkday(end);

        int daysBetween = Days.daysBetween(start, end).getDays();
        int weekendsBetween = Weeks.weeksBetween(start.withDayOfWeek(WEEK_START), end.withDayOfWeek(WEEK_START)).getWeeks();

        // holidays in between
        Holidays hol = new Holidays();
        long hDays = hol.holidays(realm, d1, d2);
        return daysBetween - (weekendsBetween * DAYS_PER_WEEKEND)- (int)hDays;
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


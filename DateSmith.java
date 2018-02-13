package com.baddog.optionsscanner;

/*
 * Created by Brian on 2018-01-28.


 SimpleDateFormat simpleDateFormat =
   new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSZZZZZ");
this format is equal to --> "2016-01-01T09:30:00.000000+01:00"

From String to Date:
String dtStart = "2010-10-15T09:27:37Z";
SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
try {
    Date date = format.parse(dtStart);
    System.out.println(date);
} catch (ParseException e) {
    e.printStackTrace();
}

From Date to String:
SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
try {
    Date date = new Date();
    String dateTime = dateFormat.format(date);
    System.out.println("Current Date Time : " + dateTime);
} catch (ParseException e) {
    e.printStackTrace();
}

int i = (int) (new Date().getTime()/1000);

 */

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import org.joda.time.DateTimeConstants;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.Weeks;

import io.realm.Realm;


public class DateSmith {
    private static final int DAYS_PER_WEEKEND = 2;
    private static final int WEEK_START = DateTimeConstants.MONDAY;
    private static final int WEEK_END = DateTimeConstants.FRIDAY;


    DateSmith() {}

    long LongNow() {
        Date currentTime = Calendar.getInstance().getTime();
        return date2long(currentTime);
    }

    Date DateNow() {
        return Calendar.getInstance().getTime();
    }

    long date2long(Date ldt) {
        return ldt.getTime()/86400000;
    }

    long StrDate2LongDate (String sldt) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSZZZZZ");
        Date dt = null;
        try {
            dt = format.parse(sldt);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dt.getTime()/86400000;
    }

    long StrDate2LongDateTime (String sldt) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSZZZZZ");
        Date dt = null;
        try {
            dt = format.parse(sldt);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return (dt.getTime()/1000);
    }

    Date StrDate2Date (String sldt) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSZZZZZ");
        Date dt = null;
        try {
            dt = format.parse(sldt);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dt;
    }

    String long2StrDate (long DateLong) {
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSZZZZZ");
        Date ldt = new Date(DateLong*86400000);
        return dateformat.format(ldt);
    }

     int workdayDiff(Realm realm, Date d1, Date d2) {

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

}



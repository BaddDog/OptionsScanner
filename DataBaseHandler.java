package com.baddog.optionsscanner;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Brian on 2017-11-02.
 */



public class DataBaseHandler extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "OptionScan.db";

    // Contacts table name
    private static final String SYMBOLS_TABLE_NAME = "symbols";

    // Contacts Table Columns names
    private static final String SYMBOLS_SYMBOL = "symbol";
    private static final String SYMBOLS_SYMBOLID = "symbolID";
    private static final String SYMBOLS_VOLATILITY_A = "volatility_a";
    private static final String SYMBOLS_VOLATILITY_B = "volatility_b";
    private static final String SYMBOLS_TRENDBIAS_A = "trendbias_a";
    private static final String SYMBOLS_TRENDBIAS_B = "trendbias_b";

    public DataBaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_SYMBOLS_TABLE = "CREATE TABLE " + SYMBOLS_TABLE_NAME + "("
                + SYMBOLS_SYMBOLID + " INTEGER NOT NULL PRIMARY KEY," + SYMBOLS_SYMBOL + " TEXT NOT NULL,"
                + SYMBOLS_VOLATILITY_A + " REAL" + SYMBOLS_VOLATILITY_B + " REAL"
                + SYMBOLS_TRENDBIAS_A + " REAL" + SYMBOLS_TRENDBIAS_B + " REAL" + ")";
        db.execSQL(CREATE_SYMBOLS_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + SYMBOLS_TABLE_NAME);

        // Create tables again
        onCreate(db);
    }

    // Getting contacts Count
    public int getSymbolsCount() {
        String countQuery = "SELECT  * FROM " + SYMBOLS_TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();

        // return count
        return cursor.getCount();
    }
}
package com.friscotap.core;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class BeerDbHelper extends SQLiteOpenHelper{
    public static final String TAG = "Frisco DbHelper";

    // Table names
    public static final String TABLE_CLUB = "club";
    public static final String TABLE_COLUMBIA = "columbia";
    public static final String TABLE_CROFTON = "crofton";
    public static final String TABLE_TRIED = "tried";

    // Column names for beer tables
    public static final String COL_ID = "_id";
    public static final String COL_FRISCO_ID = "frisco_id";
    public static final String COL_NAME = "name";
    public static final String COL_ACTIVE = "active";
    public static final String COL_NEW_BEER = "new_beer";
    public static final String COL_ABV= "abv";
    public static final String COL_OUNCES = "ounces";

    // Column names for tried beers table
    public static final String COL_TRY_FRISCO_ID = "frisco_id";
    public static final String COL_TRY_RATING = "rating";

    // Column names for mug club table
    public static final String COL_CLUB_ID = "_id";
    public static final String COL_CLUB_NAME = "name";
    public static final String COL_CLUB_DATE = "date";
    public static final String COL_CLUB_CONFIRM = "confirm";

    // Database variables
    private static final String DB_NAME = "beers.db";
    private static final int DB_VERSION = 2;

    // Draft table creation sql statement
    private static final String CREATE_COLUMBIA = "create table "
            + TABLE_COLUMBIA + "("
            + COL_ID + " integer primary key autoincrement, "
            + COL_FRISCO_ID + " integer, "
            + COL_NAME + " text not null, "
            + COL_ACTIVE + " integer, "
            + COL_NEW_BEER + " integer, "
            + COL_ABV + " text not null, "
            + COL_OUNCES + "  integer"
            + ");";

    private static final String CREATE_CROFTON = "create table "
            + TABLE_CROFTON + "("
            + COL_ID + " integer primary key autoincrement, "
            + COL_FRISCO_ID + " integer, "
            + COL_NAME + " text not null, "
            + COL_ACTIVE + " integer, "
            + COL_NEW_BEER + " integer, "
            + COL_ABV + " text not null, "
            + COL_OUNCES + "  integer"
            + ");";

    // Mug club table creation sql statement
    private static final String CREATE_CLUB = "create table "
            + TABLE_CLUB + "("
            + COL_CLUB_ID + " integer primary key autoincrement, "
            + COL_CLUB_NAME + " text not null, "
            + COL_CLUB_DATE + " integer, "
            + COL_CLUB_CONFIRM + " integer, "
            + "UNIQUE(" + COL_CLUB_NAME + ") ON CONFLICT IGNORE"
            + ");";

    public BeerDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate(): SQL Database");

        try {
            db.execSQL(CREATE_COLUMBIA);
            db.execSQL(CREATE_CROFTON);
            db.execSQL(CREATE_CLUB);
        }
        catch (Exception e) {
            Log.d(TAG, "" + e.toString());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(
                TAG,
                "Updgrading database from version " + oldVersion +
                        " to " + newVersion + ", which will destroy all old data");

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COLUMBIA);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CROFTON);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CLUB);
        onCreate(db);
    }
}

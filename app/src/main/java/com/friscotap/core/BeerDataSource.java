package com.friscotap.core;

import java.util.ArrayList;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.friscotap.mugclub.MugClubBeer;

public class BeerDataSource {
    private static final String TAG = "Frisco DataSource";
    private SQLiteDatabase mDatabase;
    private BeerDbHelper mDbHelper;
    private String mTableName;
    private long mLocation;

    private String[] allBeerColumns = {
            BeerDbHelper.COL_ID,
            BeerDbHelper.COL_FRISCO_ID,
            BeerDbHelper.COL_NAME,
            BeerDbHelper.COL_ACTIVE,
            BeerDbHelper.COL_NEW_BEER,
            BeerDbHelper.COL_ABV,
            BeerDbHelper.COL_OUNCES
    };

    private String[] allClubColumns = {
            BeerDbHelper.COL_CLUB_ID,
            BeerDbHelper.COL_CLUB_NAME,
            BeerDbHelper.COL_CLUB_DATE,
            BeerDbHelper.COL_CLUB_CONFIRM
    };

    public BeerDataSource(Context context) {
        mDbHelper = new BeerDbHelper(context);
        mTableName = BeerDbHelper.TABLE_COLUMBIA;
        mLocation = 0;
    }

    public BeerDataSource(Context context, String t, long loc) {
        mDbHelper = new BeerDbHelper(context);
        mTableName = t;
        mLocation = loc;
    }

    public void open() throws SQLException {
        Log.d(TAG, "Open Database");

        mDatabase = mDbHelper.getWritableDatabase();

        if(mDatabase == null) {
            Log.d(TAG, "open(): Database pointer is null");
        }
    }

    public void clearTable() {
        if(mDatabase.isOpen()) {
            try {
                mDatabase.execSQL("DELETE FROM " +
                                mTableName + " WHERE " + BeerDbHelper.COL_FRISCO_ID + " = " + mLocation
                );
            }
            catch (Exception e) {
                Log.d(TAG, "" + e.toString());
            }
        }
        else {
            Log.d(TAG, "Database not open");
        }
    }

    public void close() {
        Log.d(TAG, "Close Database");
        mDbHelper.close();
    }

    public void deleteBeer(Beer beer) {
        long id = beer.getId();

        mDatabase.delete(
                mTableName,
                BeerDbHelper.COL_NAME + " = " + id + " AND " + BeerDbHelper.COL_FRISCO_ID + " = " + mLocation,
                null
        );
    }

    public void deleteMugBeer(MugClubBeer beer) {
        long id = beer.getId();

        mDatabase.delete(
                BeerDbHelper.TABLE_CLUB,
                BeerDbHelper.COL_CLUB_ID + " = " + id,
                null
        );
    }

    public Beer getBeer(String name) {
        Beer beer = null;

        Cursor cursor = mDatabase.query(
                mTableName,
                allBeerColumns,
                BeerDbHelper.COL_NAME + " = " + name + " AND " + BeerDbHelper.COL_FRISCO_ID + " = " + mLocation,
                null,
                null,
                null,
                null
        );

        if(cursor.getCount() > 0) {
            cursor.moveToFirst();
            beer = cursorToBeer(cursor);
            cursor.close();
        }

        return beer;
    }

    public Beer getBeer(long id) {
        Beer beer = null;

        Cursor cursor = mDatabase.query(
                mTableName,
                allBeerColumns,
                BeerDbHelper.COL_NAME + " = " + id + " AND " + BeerDbHelper.COL_FRISCO_ID + " = " + mLocation,
                null,
                null,
                null,
                null
        );

        if(cursor.getCount() > 0) {
            cursor.moveToFirst();
            beer = cursorToBeer(cursor);
            cursor.close();
        }

        return beer;
    }

    public MugClubBeer getMugBeer(String name) {
        MugClubBeer beer = null;

        Cursor cursor = mDatabase.query(
                BeerDbHelper.TABLE_CLUB,
                allClubColumns,
                BeerDbHelper.COL_CLUB_NAME + " = \"" + name + "\"",
                null,
                null,
                null,
                null
        );

        if(cursor.getCount() > 0) {
            cursor.moveToFirst();
            beer = cursorToMugClubBeer(cursor);
            cursor.close();
        }

        return beer;
    }

    public MugClubBeer getMugBeer(long id) {
        MugClubBeer beer = null;

        Cursor cursor = mDatabase.query(
                BeerDbHelper.TABLE_CLUB,
                allClubColumns,
                BeerDbHelper.COL_CLUB_ID + " = " + id,
                null,
                null,
                null,
                null
        );

        if(cursor.getCount() > 0) {
            cursor.moveToFirst();
            beer = cursorToMugClubBeer(cursor);
            cursor.close();
        }

        return beer;
    }

    public ArrayList<Beer> getAllBeers() {
        ArrayList<Beer> beers = new ArrayList<Beer>();

        try {
            Cursor cursor = mDatabase.query(
                    mTableName,
                    allBeerColumns,
                    BeerDbHelper.COL_FRISCO_ID + " = " + mLocation,
                    null,
                    null,
                    null,
                    null
            );

            cursor.moveToFirst();

            while(!cursor.isAfterLast()) {
                Beer beer = cursorToBeer(cursor);
                beers.add(beer);
                cursor.moveToNext();
            }

            cursor.close();
        }
        catch(Exception e) {
            Log.d(TAG, "" + e.toString());
        }

        return beers;
    }

    public ArrayList<MugClubBeer> getMugBook() {
        ArrayList<MugClubBeer> beers = new ArrayList<MugClubBeer>();

        try {
            Cursor cursor = mDatabase.query(
                    BeerDbHelper.TABLE_CLUB,
                    allClubColumns,
                    null,
                    null,
                    null,
                    null,
                    null
            );

            cursor.moveToFirst();

            while(!cursor.isAfterLast()) {
                MugClubBeer beer = cursorToMugClubBeer(cursor);
                beers.add(beer);
                cursor.moveToNext();
            }

            cursor.close();
        }
        catch(Exception e) {
            Log.d(TAG, "" + e.toString());
        }

        return beers;
    }

    public long insertBeer(Beer beer) {
        long insertId = -1;
        ContentValues values = new ContentValues();

        values.put(BeerDbHelper.COL_FRISCO_ID, beer.getFriscoId());
        values.put(BeerDbHelper.COL_NAME, beer.getName());
        values.put(BeerDbHelper.COL_ACTIVE, beer.getActive());
        values.put(BeerDbHelper.COL_NEW_BEER, beer.getNewBeer());
        values.put(BeerDbHelper.COL_ABV, beer.getAbv());
        values.put(BeerDbHelper.COL_OUNCES, beer.getOunces());

        try {
            insertId = mDatabase.insert(
                    mTableName,
                    null,
                    values
            );
        }
        catch(Exception e) {
            Log.d(TAG, "" + e.toString());
        }

        return insertId;
    }

    public long insertMugBeer(MugClubBeer beer) {
        long insertId = -1;
        ContentValues values = new ContentValues();

        values.put(BeerDbHelper.COL_CLUB_NAME, beer.getName());
        values.put(BeerDbHelper.COL_CLUB_DATE, beer.getDate().getTime());
        values.put(BeerDbHelper.COL_CLUB_CONFIRM, beer.getConfirmed());

        try {
            insertId = mDatabase.insert(
                    BeerDbHelper.TABLE_CLUB,
                    null,
                    values
            );
        }
        catch(Exception e) {
            Log.d(TAG, "" + e.toString());
        }

        return insertId;
    }

    public void updateMugBeer(MugClubBeer beer) {
        ContentValues values = new ContentValues();

        values.put(BeerDbHelper.COL_CLUB_DATE, beer.getDate().getTime());
        values.put(BeerDbHelper.COL_CLUB_CONFIRM, beer.getConfirmed());

        try {
            mDatabase.update(
                    BeerDbHelper.TABLE_CLUB,
                    values,
                    BeerDbHelper.COL_CLUB_ID + " = ?",
                    new String[] { String.valueOf(beer.getId()) }
            );
        }
        catch(Exception e) {
            Log.d(TAG, "" + e.toString());
        }
    }

    private Beer cursorToBeer(Cursor cursor) {
        Beer beer = new Beer();

        beer.setId(cursor.getLong(0));
        beer.setFriscoId(cursor.getLong(1));
        beer.setName(cursor.getString(2));
        beer.setActive(cursor.getInt(3));
        beer.setNewBeer(cursor.getInt(4));
        beer.setAbv(cursor.getString(5));
        beer.setOunces(cursor.getInt(6));

        return beer;
    }

    private MugClubBeer cursorToMugClubBeer(Cursor cursor) {
        MugClubBeer beer = new MugClubBeer();

        beer.setId(cursor.getLong(0));
        beer.setName(cursor.getString(1));
        Date setDate = new Date(cursor.getLong(2));
        beer.setDate(setDate);
        beer.setConfirmed(cursor.getInt(3));

        return beer;
    }
}

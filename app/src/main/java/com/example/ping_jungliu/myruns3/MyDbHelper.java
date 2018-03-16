package com.example.ping_jungliu.myruns3;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Created by Ping-Jung Liu on 2018/1/28.
 */

// standard DbHelper.
// I followed the guidelines to combine helepr and datasrouce.
public class MyDbHelper extends SQLiteOpenHelper {

    public static final String CREATE_TABLE_ENTRIES = "CREATE TABLE IF NOT EXISTS "
            + "entry" + " ("
            + "_id" + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "input_type" + " INTEGER NOT NULL, "
            + "activity_type" + " TEXT NOT NULL, "
            + "date_time" + " DATETIME NOT NULL, "
            + "duration" + " FLOAT NOT NULL, "
            + "distance" + " FLOAT, "
            + "avg_speed" + " FLOAT, "
            + "climb" + " FLOAT, "
            + "calories" + " FLOAT, "
            + "heartrate" + " FLOAT, "
            + "comment" + " TEXT,  "
            + "privacy" + " INTEGER, "
            + "gps_data" + " BLOB " + ");";


    public MyDbHelper(Context context) {
        super(context, "entry.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_ENTRIES);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Log.e("XD",	"Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + "entry");
        onCreate(db);
    }

    public long insertEntry(DbEntry entry) {

        // Insert all the values
        ContentValues data = new ContentValues();

        data.put("input_type", entry.getInputType());
        data.put("activity_type",entry.getActivityType());
        data.put("date_time", entry.getDateTime());
        data.put("duration",entry.getDuration());
        data.put("distance",entry.getDistance());
        data.put("avg_speed",entry.getAvgSpeed());
        data.put("calories",entry.getCalories());
        data.put("climb",entry.getClimb());
        data.put("heartrate",entry.getHeartRate());
        data.put("comment",entry.getComment());

        Gson gson = new Gson();
        data.put("gps_data", gson.toJson(entry.getLocationList()).getBytes());

        SQLiteDatabase database = getWritableDatabase();
        long insertId = database.insert("entry", null, data);
        database.close();
        return insertId;
    }

    public void removeEntry(long rowIndex) {

        SQLiteDatabase database = getWritableDatabase();
        database.delete("entry", "_id"
                + " = " + rowIndex, null);
        database.close();
    }

    public DbEntry fetchEntryByIndex(long rowId) {
        SQLiteDatabase database = getReadableDatabase();

        Cursor cursor = database.query("entry", null,
                "_id" + " = " + rowId, null, null, null, null);
        cursor.moveToFirst();
        DbEntry entry = cursorEntry(cursor);
        cursor.close();
        database.close();

        return entry;
    }

    public ArrayList<DbEntry> fetchEntries() {

        ArrayList<DbEntry> entries = new ArrayList<>();
        SQLiteDatabase database = getReadableDatabase();

        Cursor cursor = database.query("entry",
                null, null, null, null, null, null);

        cursor.moveToFirst();

        // Fetch the entries one by one until cursor reaches the end
        while (!cursor.isAfterLast()) {
            DbEntry entry = cursorEntry(cursor);
            entries.add(entry);
            cursor.moveToNext();
        }

        cursor.close();
        database.close();

        return entries;
    }

    private DbEntry cursorEntry(Cursor cursor) {

        DbEntry entry = new DbEntry();
        entry.setId(cursor.getLong(0));
        entry.setInputType(cursor.getInt(1));
        entry.setActivityType(cursor.getString(2));
        entry.setDateTime(cursor.getLong(3));
        entry.setDuration(cursor.getInt(4));
        entry.setDistance(cursor.getDouble(5));
        entry.setAvgSpeed(cursor.getDouble(6));
        entry.setClimb(cursor.getDouble(7));
        entry.setCalories(cursor.getInt(8));
        entry.setHeartRate(cursor.getInt(9));
        entry.setComment(cursor.getString(10));
        Gson gson = new Gson();
        String json = new String(cursor.getBlob(12));
        Type type = new TypeToken<ArrayList<LatLng>>() {}.getType();
        entry.setLocationList((ArrayList<LatLng>)gson.fromJson(json, type));

        return entry;
    }
}

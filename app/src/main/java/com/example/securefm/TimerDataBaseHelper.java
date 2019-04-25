package com.example.securefm;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TimerDataBaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "Timer";
    public static final int DB_VERSION = 1;
    public TimerDataBaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE TIMER (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "FILE_NAME TEXT," +
                "SIZE_b INTEGER, " +
                "ALGORITHM TEXT," +
                "MODE TEXT," +
                "TIME_ms INTEGER);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void insertTime(SQLiteDatabase db, String fileName, int size, String mode, String algorithm, int time) {
        ContentValues values = new ContentValues();
        values.put("FILE_NAME", fileName);
        values.put("SIZE_b", size);
        values.put("MODE", mode);
        values.put("ALGORITHM", algorithm);
        values.put("TIME_ms", time);
        db.insert("TIMER", null, values);
    }
}

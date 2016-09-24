package com.sunny.leaveme.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.sunny.leaveme.db.entity.ScheduleItem;

/**
 * Created by Administrator on 2016/9/11.
 * DbHelper for get database from system.
 */
public class DbHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "leaveme.db";
    private static final int version = 1;

    public DbHelper(Context context) {
        super(context, DB_NAME, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(ScheduleItem.CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}

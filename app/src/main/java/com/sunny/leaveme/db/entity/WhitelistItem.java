package com.sunny.leaveme.db.entity;

/**
 * Created by Sunny Li on 2016/9/24.
 * Define Table create SQL and Column names
 */

public class WhitelistItem {
    private final static String TAG = "WhitelistItem";

    public final static String TABLE_NAME = "whitelist";
    public final static String ID = "ID";
    public final static String APPNAME = "APPNAME";
    public final static String ISAVAILABLE = "ISAVAILABLE";

    public final static String CREATE = "create table whitelist(" +
            "ID             INTEGER     PRIMARY KEY     AUTOINCREMENT," +
            "APPNAME        TEXT                        NOT NULL," +
            "ISAVAILABLE    INTEGER                     NOT NULL);";

    private int mId = -1;
    private String mAppName = "";
    private boolean mIsAvailable = false;

    public WhitelistItem() {
        mId = -1;
        mAppName = "";
        mIsAvailable = false;
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        this.mId = id;
    }

    public String getAppName() {
        return mAppName;
    }

    public void setAppName(String appName) {
        mAppName = appName;
    }

    public boolean isAvailable() {
        return mIsAvailable;
    }

    public void setAvailable(boolean isAvailable) {
        mIsAvailable = isAvailable;
    }
}

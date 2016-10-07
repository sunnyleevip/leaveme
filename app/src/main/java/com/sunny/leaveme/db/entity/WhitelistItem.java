package com.sunny.leaveme.db.entity;

/**
 * Created by Sunny Li on 2016/9/24.
 * Define Table create SQL and Column names
 */

public class WhitelistItem {
    public final static String TABLE_NAME = "whitelist";
    public final static String ID = "ID";
    public final static String APPLABEL = "APPLABEL";
    public final static String APPNAME = "APPNAME";
    public final static String APPACTIVITY = "APPACTIVITY";
    public final static String ISAVAILABLE = "ISAVAILABLE";

    public final static String CREATE = "create table whitelist(" +
            "ID             INTEGER     PRIMARY KEY," +
            "APPLABEL       TEXT        NOT NULL,"    +
            "APPNAME        TEXT        NOT NULL,"    +
            "APPACTIVITY    TEXT        NOT NULL,"    +
            "ISAVAILABLE    INTEGER     NOT NULL"     +
            ");";

    private int mId = -1;
    private String mAppLabel = "";
    private String mAppName = "";
    private String mAppActivity = "";
    private boolean mIsAvailable = false;

    public WhitelistItem() {
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        this.mId = id;
    }

    public String getAppLabel() {
        return mAppLabel;
    }

    public void setAppLabel(String appLabel) {
        mAppLabel = appLabel;
    }

    public String getAppName() {
        return mAppName;
    }

    public void setAppName(String appName) {
        mAppName = appName;
    }

    public String getAppActivity() {
        return mAppActivity;
    }

    public void setAppActivity(String appActivity) {
        mAppActivity = appActivity;
    }

    public boolean isAvailable() {
        return mIsAvailable;
    }

    public void setAvailable(boolean isAvailable) {
        mIsAvailable = isAvailable;
    }
}

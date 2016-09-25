package com.sunny.leaveme.db.entity;

import android.util.Log;

import java.io.Serializable;
import java.util.Locale;

/**
 * Created by Sunny Li on 2016/9/7.
 * ScheduleTime records Hour and Minute only
 */
public class ScheduleTime implements Serializable {
    private final static String TAG = "ScheduleTime";
    private int mHour = 0;
    private int mMinute = 0;

    public ScheduleTime() {
        mHour = 0;
        mMinute = 0;
    }

    public ScheduleTime(int hour, int minute) {
        mHour = hour;
        mMinute = minute;
    }

    public ScheduleTime(ScheduleTime scheduleTime) {
        mHour = scheduleTime.getHour();
        mMinute = scheduleTime.getMinute();
    }

    public int getHour() {
        return mHour;
    }

    public void setHour(int hour) {
        if ((hour >= 24) || (hour < 0)) {
            Log.e(TAG, "Wrong hour value");
            return;
        }
        this.mHour = hour;
    }

    public int getMinute() {
        return mMinute;
    }

    public void setMinute(int minute) {
        if ((minute >= 60) || (minute < 0)) {
            Log.e(TAG, "Wrong minute value");
            return;
        }
        this.mMinute = minute;
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "%02d", mHour) + ":" +
                String.format(Locale.getDefault(), "%02d", mMinute);
    }

    public boolean isEqualOrAfter(ScheduleTime scheduleTime) {
        if (mHour < scheduleTime.getHour()) {
            return false;
        } else if (mHour == scheduleTime.getHour()) {
            if (mMinute < scheduleTime.getMinute()) {
                return false;
            }
        }

        return true;
    }

    public boolean isAfter(ScheduleTime scheduleTime) {
        if (mHour > scheduleTime.getHour()) {
            return true;
        } else if (mHour == scheduleTime.getHour()) {
            if (mMinute > scheduleTime.getMinute()) {
                return true;
            }
        }

        return false;
    }
}

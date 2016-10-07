package com.sunny.leaveme.db.entity;

import android.util.Log;

import java.io.Serializable;
import java.util.Calendar;

/**
 * Created by Sunny Li on 2016/9/8.
 * Add getCalendar on 2016/9/16
 */
public class ScheduleItem implements Serializable {
    private final static String TAG = "ScheduleItem";

    public final static String TABLE_NAME = "schedule";
    public final static String ID = "ID";
    public final static String STARTTIME = "STARTTIME";
    public final static String ENDTIME = "ENDTIME";
    public final static String REPEAT_DAYS = "REPEAT_DAYS";
    public final static String ISAVAILABLE = "ISAVAILABLE";

    /*
            STARTTIME = Hour * 100 + Minute
            ENDTIME   = Hour * 100 + Minute
            About REPEAT_DAYS
            0  -> None
            1  -> Sunday
            2  -> Monday
            4  -> Tuesday
            8  -> Wednesday
            16 -> Thursday
            32 -> Friday
            64 -> Saturday
    */
    public final static String CREATE = "create table schedule(" +
            "ID             INTEGER     PRIMARY KEY," +
            "STARTTIME      INTEGER     NOT NULL,"    +
            "ENDTIME        INTEGER     NOT NULL,"    +
            "REPEAT_DAYS    INTEGER     NOT NULL,"    +
            "ISAVAILABLE    INTEGER     NOT NULL);";

    private int mId = -1;
    private ScheduleTime mStartTime;
    private ScheduleTime mEndTime;
    private boolean[] mRepeatDays = {
            false,
            false,
            false,
            false,
            false,
            false,
            false
    };
    private final static String[] mStrRepeatDays = {
            "SUN ",
            "MON ",
            "TUE ",
            "WED ",
            "THU ",
            "FRI ",
            "SAT "
    };
    private boolean mIsRepeat = false;
    private boolean mIsAvailable = false;

    public ScheduleItem() {
        mId = -1;
        mStartTime = new ScheduleTime();
        mEndTime = new ScheduleTime();
        mIsRepeat = false;
        mIsAvailable = false;
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        this.mId = id;
    }

    public ScheduleTime getStartTime() {
        return mStartTime;
    }
    /*public void setStartHour(ScheduleTime startTime) {
        mStartTime = startTime;
    }*/
    public ScheduleTime getEndTime() {
        return mEndTime;
    }
    /*public void setEndHour(ScheduleTime endTime) {
        mEndTime = endTime;
    }*/

    public void setRepeatDays(boolean[] days) {
        if (days.length != mRepeatDays.length) {
            Log.e(TAG, "String error length");
            return;
        }

        System.arraycopy(days, 0, mRepeatDays, 0, mRepeatDays.length);
    }

    public boolean[] getRepeatDays() {
        return mRepeatDays;
    }

    public String getRepeatDaysString() {
        String result = "";
        for (int i = 0; i < mRepeatDays.length; ++i) {
            if (mRepeatDays[i]) {
                result += mStrRepeatDays[i];
            }
        }

        return result;
    }

    public boolean isAvailable() {
        return mIsAvailable;
    }

    public void setAvailable(boolean isAvailable) {
        mIsAvailable = isAvailable;
    }

    public boolean isRepeat() {
        return mIsRepeat;
    }

    public void setRepeat(boolean isRepeat) {
        mIsRepeat = isRepeat;
    }

    public void makeVaild() {
        if (!mEndTime.isEqualOrAfter(mStartTime)) {
            mEndTime = new ScheduleTime(mStartTime);
        }
    }

    public Calendar getStartTimeCalendar() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        Log.d(TAG, "cur millis: " + calendar.getTime().toString());
        ScheduleTime curTime = new ScheduleTime(calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE));

        if (mEndTime.isAfter(curTime) && (!mStartTime.isAfter(curTime))) {
            calendar.add(Calendar.SECOND, 1);
        } else {
            if (!mEndTime.isAfter(curTime)) {
                calendar.add(Calendar.DATE,
                        getGapToNextAvailableDate(calendar));
            }
            calendar.set(Calendar.HOUR_OF_DAY, mStartTime.getHour());
            calendar.set(Calendar.MINUTE, mStartTime.getMinute());
            calendar.set(Calendar.SECOND, 0);
        }

        return calendar;
    }

    public Calendar getEndTimeCalendar() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        Log.d(TAG, "cur millis: " + calendar.getTime().toString());
        ScheduleTime curTime = new ScheduleTime(calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE));

        if (!mEndTime.isAfter(curTime)) {
            calendar.add(Calendar.DATE,
                    getGapToNextAvailableDate(calendar));
        }

        calendar.set(Calendar.HOUR_OF_DAY, mEndTime.getHour());
        calendar.set(Calendar.MINUTE, mEndTime.getMinute());
        calendar.set(Calendar.SECOND, 0);

        return calendar;
    }

    // Can only use when current time is later than start time and end time.
    private int getGapToNextAvailableDate(Calendar calendar) {
        int gap = 0;
        if (mIsRepeat) {
            int i = getDayOfWeek(calendar) % 7; // next day in the week
            int curDay = (i + 7 - 1) % 7;
            do {
                gap++;
                if (mRepeatDays[i]) {
                    break;
                }
                i = (i + 1) % 7;
            } while (i != curDay);
        } else {
            gap = 1;
        }

        return gap;
    }

    // let week begin at Sunday
    private int getDayOfWeek(Calendar calendar) {
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        if (calendar.getFirstDayOfWeek() == Calendar.MONDAY) {
            if (++dayOfWeek == 8) {
                dayOfWeek = 1;
            }
        }

        return dayOfWeek;
    }
}

package com.sunny.leaveme.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.sunny.leaveme.db.entity.ScheduleItem;

import java.util.ArrayList;

/**
 * Created by Sunny Li on 2016/9/11.
 * DataHelper for database reader and writer
 */
public class DataHelper {
    private static final String TAG = "DataHelper";

    private DbHelper mDbHelper;
    private SQLiteDatabase mDb = null;

    public DataHelper(Context context) {
        mDbHelper = new DbHelper(context);
        mDb = mDbHelper.getWritableDatabase();
    }

    public void close() {
        mDb.close();
        mDbHelper.close();
    }

    public ArrayList<ScheduleItem> getAllScheduleItems() {
        ArrayList<ScheduleItem> scheduleItems = new ArrayList<>();
        Cursor c = mDb.query(ScheduleItem.TABLE_NAME, null, null, null, null, null, null);
        while (c.moveToNext()) {
            ScheduleItem scheduleItem = getScheduleItemFromCursor(c);
            scheduleItems.add(scheduleItem);
        }
        c.close();

        return scheduleItems;
    }

    private ScheduleItem getScheduleItemFromCursor(Cursor c) {
        ScheduleItem scheduleItem = new ScheduleItem();

        scheduleItem.setId(c.getInt(c.getColumnIndex(ScheduleItem.ID)));

        int iStartTime = c.getInt(c.getColumnIndex(ScheduleItem.STARTTIME));
        scheduleItem.getStartTime().setHour(iStartTime / 100);
        scheduleItem.getStartTime().setMinute(iStartTime % 100);

        int iEndTime = c.getInt(c.getColumnIndex(ScheduleItem.ENDTIME));
        scheduleItem.getEndTime().setHour(iEndTime / 100);
        scheduleItem.getEndTime().setMinute(iEndTime % 100);

        int iRepeatDays = c.getInt(c.getColumnIndex(ScheduleItem.REPEAT_DAYS));
        boolean[] repeatDays = { false, false, false, false, false, false, false };
        if (iRepeatDays > 0) {
            scheduleItem.setRepeat(true);
            int i = 0;
            while (iRepeatDays > 0) {
                if ((iRepeatDays % 2) == 1) {
                    repeatDays[i] = true;
                }
                iRepeatDays /= 2;
                ++i;

                if (i == repeatDays.length) {
                    break;
                }
            }
        } else {
            scheduleItem.setRepeat(false);
        }
        scheduleItem.setRepeatDays(repeatDays);
        Log.d(TAG, "getScheduleItemFromCursor is repeat:" + scheduleItem.isRepeat());
        Log.d(TAG, "getScheduleItemFromCursor repeat days:" + scheduleItem.getRepeatDaysString());

        scheduleItem.setAvailable((c.getInt(c.getColumnIndex(ScheduleItem.ISAVAILABLE))) == 1);

        return scheduleItem;
    }

    public ScheduleItem getScheduleItemsById(int id) {
        ScheduleItem scheduleItem = null;
        Cursor c = mDb.query(ScheduleItem.TABLE_NAME, null, "id=?", new String[] { "" + id }, null, null, null);
        int cnt = 0;
        while (c.moveToNext()) {
            cnt++;
            if (cnt > 1) {
                Log.e(TAG, "Too many result");
                scheduleItem = null;
                break;
            }

            scheduleItem = getScheduleItemFromCursor(c);

            if (scheduleItem.getId() != id) {
                Log.e(TAG, "error when query");
                scheduleItem = null;
                break;
            }
        }
        c.close();

        return scheduleItem;
    }

    public int insertScheduleItem(ScheduleItem scheduleItem) {
        long id = mDb.insert(ScheduleItem.TABLE_NAME, null, createContentValusForScheduleItem(scheduleItem));
        Log.d(TAG, "insert to db id: " + id);
        return (int)id;
    }

    public void updateScheduleItem(ScheduleItem scheduleItem) {
        Log.d(TAG, "update to db id: " + scheduleItem.getId());
        mDb.update(ScheduleItem.TABLE_NAME, createContentValusForScheduleItem(scheduleItem)
                , ScheduleItem.ID + "=?" , new String[]{"" + scheduleItem.getId()});
    }

    public void deleteScheduleItem(ScheduleItem scheduleItem) {
        Log.d(TAG, "delete from db id: " + scheduleItem.getId());
        mDb.delete(ScheduleItem.TABLE_NAME,
                ScheduleItem.ID + "=?", new String[]{"" + scheduleItem.getId()});
    }

    private ContentValues createContentValusForScheduleItem(ScheduleItem scheduleItem) {
        int iStartTime = scheduleItem.getStartTime().getHour() * 100
                + scheduleItem.getStartTime().getMinute();
        int iEndTime = scheduleItem.getEndTime().getHour() * 100
                + scheduleItem.getEndTime().getMinute();
        int iRepeatDays = 0;
        for (int i = 0; i < scheduleItem.getRepeatDays().length; ++i) {
            iRepeatDays *= 2;
            if (scheduleItem.getRepeatDays()[i]) {
                iRepeatDays++;
            }
        }

        Log.d(TAG, "createContentValusForScheduleItem iRepeatDays:" + iRepeatDays);

        ContentValues cv = new ContentValues();
        cv.put(ScheduleItem.STARTTIME, iStartTime);
        cv.put(ScheduleItem.ENDTIME, iEndTime);
        cv.put(ScheduleItem.REPEAT_DAYS, iRepeatDays);
        cv.put(ScheduleItem.ISAVAILABLE, scheduleItem.isAvailable());

        return cv;
    }
}

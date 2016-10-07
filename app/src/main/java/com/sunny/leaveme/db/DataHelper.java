package com.sunny.leaveme.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.sunny.leaveme.db.entity.ScheduleItem;
import com.sunny.leaveme.db.entity.WhitelistItem;

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
        long id = mDb.insert(ScheduleItem.TABLE_NAME, null, createContentValuesForScheduleItem(scheduleItem));
        Log.d(TAG, "insert to db id: " + id);
        return (int)id;
    }

    public void updateScheduleItem(ScheduleItem scheduleItem) {
        Log.d(TAG, "update to db id: " + scheduleItem.getId());
        mDb.update(ScheduleItem.TABLE_NAME, createContentValuesForScheduleItem(scheduleItem)
                , ScheduleItem.ID + "=?" , new String[]{"" + scheduleItem.getId()});
    }

    public void deleteScheduleItem(ScheduleItem scheduleItem) {
        Log.d(TAG, "delete from db id: " + scheduleItem.getId());
        mDb.delete(ScheduleItem.TABLE_NAME,
                ScheduleItem.ID + "=?", new String[]{"" + scheduleItem.getId()});
    }

    private ContentValues createContentValuesForScheduleItem(ScheduleItem scheduleItem) {
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

        Log.d(TAG, "createContentValuesForScheduleItem iRepeatDays:" + iRepeatDays);

        ContentValues cv = new ContentValues();
        cv.put(ScheduleItem.STARTTIME, iStartTime);
        cv.put(ScheduleItem.ENDTIME, iEndTime);
        cv.put(ScheduleItem.REPEAT_DAYS, iRepeatDays);
        cv.put(ScheduleItem.ISAVAILABLE, scheduleItem.isAvailable());

        return cv;
    }

    public ArrayList<WhitelistItem> getAllWhitelistItems() {
        ArrayList<WhitelistItem> whitelistItems = new ArrayList<>();
        Cursor c = mDb.query(WhitelistItem.TABLE_NAME, null, null, null, null, null, null);
        while (c.moveToNext()) {
            WhitelistItem whitelistItem = getWhitelistItemFromCursor(c);
            whitelistItems.add(whitelistItem);
        }
        c.close();

        return whitelistItems;
    }

    public ArrayList<WhitelistItem> getAvailableWhitelistItems() {
        ArrayList<WhitelistItem> whitelistItems = new ArrayList<>();
        String whereClause = WhitelistItem.ISAVAILABLE + "=?";
        String[] whereArgs = new String[] {
            "1"
        };
        Cursor c = mDb.query(WhitelistItem.TABLE_NAME, null, whereClause, whereArgs, null, null, null);
        while (c.moveToNext()) {
            WhitelistItem whitelistItem = getWhitelistItemFromCursor(c);
            whitelistItems.add(whitelistItem);
        }
        c.close();

        return whitelistItems;
    }

    private WhitelistItem getWhitelistItemFromCursor(Cursor c) {
        WhitelistItem whitelistItem = new WhitelistItem();
        whitelistItem.setId(c.getInt(c.getColumnIndex(WhitelistItem.ID)));
        whitelistItem.setAppLabel(c.getString(c.getColumnIndex(WhitelistItem.APPLABEL)));
        whitelistItem.setAppName(c.getString(c.getColumnIndex(WhitelistItem.APPNAME)));
        whitelistItem.setAppActivity(c.getString(c.getColumnIndex(WhitelistItem.APPACTIVITY)));
        whitelistItem.setAvailable(c.getInt(c.getColumnIndex(WhitelistItem.ISAVAILABLE)) == 1);
        return whitelistItem;
    }

    public void insertWhitelistItems(ArrayList<WhitelistItem> whitelistItems) {
        mDb.beginTransaction();
        for (int i = 0; i < whitelistItems.size(); ++i) {
            int iIsAvailable = 0;
            if (whitelistItems.get(i).isAvailable()) {
                iIsAvailable = 1;
            }
            String sql = "insert into " + WhitelistItem.TABLE_NAME +
                    "(" + WhitelistItem.APPLABEL + "," + WhitelistItem.APPNAME +
                    "," + WhitelistItem.APPACTIVITY + "," + WhitelistItem.ISAVAILABLE + ")" +
                    " values" +
                    "('" + whitelistItems.get(i).getAppLabel() + "','" +
                    whitelistItems.get(i).getAppName() + "','" +
                    whitelistItems.get(i).getAppActivity() + "'," +
                    iIsAvailable + ")";
            mDb.execSQL(sql);
        }
        mDb.setTransactionSuccessful();
        mDb.endTransaction();
    }

    public int insertWhitelistItem(WhitelistItem whitelistItem) {
        long id = mDb.insert(WhitelistItem.TABLE_NAME, null, createContentValuesForWhitelistItem(whitelistItem));
        Log.d(TAG, "insert WhitelistItem to db id: " + id);
        return (int)id;
    }

    public void updateWhitelistItem(WhitelistItem whitelistItem) {
        Log.d(TAG, "update to db id: " + whitelistItem.getId());
        mDb.update(WhitelistItem.TABLE_NAME, createContentValuesForWhitelistItem(whitelistItem)
                , WhitelistItem.ID + "=?" , new String[]{"" + whitelistItem.getId()});
    }

    private ContentValues createContentValuesForWhitelistItem(WhitelistItem whitelistItem) {
        ContentValues cv = new ContentValues();
        cv.put(WhitelistItem.APPLABEL, whitelistItem.getAppLabel());
        cv.put(WhitelistItem.APPNAME, whitelistItem.getAppName());
        cv.put(WhitelistItem.APPACTIVITY, whitelistItem.getAppActivity());
        cv.put(WhitelistItem.ISAVAILABLE, whitelistItem.isAvailable());
        return cv;
    }
}

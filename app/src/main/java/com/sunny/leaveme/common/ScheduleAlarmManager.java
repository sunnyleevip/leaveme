package com.sunny.leaveme.common;

import android.content.Context;
import android.util.Log;

import com.sunny.leaveme.db.entity.ScheduleItem;

import java.util.ArrayList;

/**
 * Created by Sunny Li on 2016/9/16.
 * Create Start & Stop alarm for schedule item.
 */
public class ScheduleAlarmManager {
    private final static String TAG = "ScheduleAlarmManager";
    private AlarmHelper mStartAlarmHelper;
    private AlarmHelper mEndAlarmHelper;
    private OnScheduleAlarmTimeoutListener mOnScheduleAlarmTimeoutListener;
    private ArrayList<ScheduleItem> mAlarmItems;

    private ArrayList<Integer> mStartSchedules;
    private ArrayList<Integer> mEndSchedules;

    public ScheduleAlarmManager(Context context, OnScheduleAlarmTimeoutListener onScheduleAlarmTimeoutListener) {
        mAlarmItems = new ArrayList<>();
        mStartSchedules = new ArrayList<>();
        mEndSchedules = new ArrayList<>();
        mOnScheduleAlarmTimeoutListener = onScheduleAlarmTimeoutListener;
        mStartAlarmHelper = new AlarmHelper(context, new AlarmHelper.OnAlarmTimeoutListener() {
            @Override
            public void onAlarmTimeout(int id) {
                Log.d(TAG, "Start Alarm id:" + id);

                ScheduleItem scheduleItem;
                for (int i = 0; i < mAlarmItems.size(); ++i) {
                    if (mAlarmItems.get(i).getId() == id) {
                        scheduleItem = mAlarmItems.get(i);
                        Log.d(TAG, "remove Alarm id:" + id);
                        mStartSchedules.remove(Integer.valueOf(id));
                        mOnScheduleAlarmTimeoutListener.onScheduleStartAlarmTimeout(id, scheduleItem.isRepeat());
                        break;
                    }
                }
            }
        });

        mEndAlarmHelper = new AlarmHelper(context, new AlarmHelper.OnAlarmTimeoutListener() {
            @Override
            public void onAlarmTimeout(int id) {
                id = 0 - id;
                Log.d(TAG, "End Alarm id:" + id);

                ScheduleItem scheduleItem;
                for (int i = 0; i < mAlarmItems.size(); ++i) {
                    if (mAlarmItems.get(i).getId() == id) {
                        scheduleItem = mAlarmItems.get(i);
                        mAlarmItems.remove(i);
                        Log.d(TAG, "remove Alarm id:" + id);
                        mEndSchedules.remove(Integer.valueOf(id));
                        mOnScheduleAlarmTimeoutListener.onScheduleEndAlarmTimeout(id, scheduleItem.isRepeat());
                        if (scheduleItem.isRepeat()) {
                            startAlarm(scheduleItem);
                        }
                        break;
                    }
                }
            }
        });
    }

    public interface OnScheduleAlarmTimeoutListener {
        void onScheduleStartAlarmTimeout(int id, boolean isRepeat);
        void onScheduleEndAlarmTimeout(int id, boolean isRepeat);
    }

    public void startAlarms(ArrayList<ScheduleItem> scheduleItems) {
        Log.d(TAG, "startAlarms");
        for (int i = 0; i < scheduleItems.size(); ++i) {
            ScheduleItem scheduleItem = scheduleItems.get(i);
            startAlarm(scheduleItem);
        }
    }

    public void startAlarm(ScheduleItem scheduleItem) {
        Log.d(TAG, "startAlarm, id:" + scheduleItem.getId());
        if (scheduleItem.isAvailable()) {
            for (int i = 0; i < mAlarmItems.size(); ++i) {
                if (mAlarmItems.get(i).getId() == scheduleItem.getId()) {
                    Log.e(TAG, "Alarm already exist id:" + scheduleItem.getId());
                    return;
                }
            }
            mStartSchedules.add(scheduleItem.getId());
            mEndSchedules.add(scheduleItem.getId());
            mAlarmItems.add(scheduleItem);

            Log.d(TAG, "Start Date: " + scheduleItem.getStartTimeCalendar().getTime().toString());
            mStartAlarmHelper.startOneshotAlarm(scheduleItem.getId(),
                    scheduleItem.getStartTimeCalendar().getTimeInMillis());

            Log.d(TAG, "End Date: " + scheduleItem.getEndTimeCalendar().getTime().toString());
            mEndAlarmHelper.startOneshotAlarm(0 - scheduleItem.getId(),
                    scheduleItem.getEndTimeCalendar().getTimeInMillis());
        }
    }

    public void updateAlarm(ScheduleItem scheduleItem) {
        Log.d(TAG, "updateAlarm");
        cancelAlarm(scheduleItem.getId());
        if (scheduleItem.isAvailable()) {
            startAlarm(scheduleItem);
        }
    }

    public void cancelAlarm(int id) {
        Log.d(TAG, "cancelAlarm, id:" + id);
        for (int i = 0; i < mAlarmItems.size(); ++i) {
            if (mAlarmItems.get(i).getId() == id) {
                mAlarmItems.remove(i);
                for (int j = 0; j < mStartSchedules.size(); ++j) {
                    if (mStartSchedules.get(i) == id) {
                        Log.d(TAG, "Find exist start alarm id: " + id);
                        mStartAlarmHelper.cancelAlarm(id);
                        mStartSchedules.remove(i);
                        break;
                    }
                }

                for (int j = 0; j < mEndSchedules.size(); ++j) {
                    if (mEndSchedules.get(i) == id) {
                        Log.d(TAG, "Find exist end alarm id: " + id);
                        mEndAlarmHelper.cancelAlarm(id);
                        mEndSchedules.remove(i);
                        return;
                    }
                }

                Log.e(TAG, "Cannot find id:" + id + " in mEndSchedules");
            }
        }
        Log.d(TAG, "No exist alarm found, cannot cancel alarm id:" + id);
    }

    public boolean isStartFirstSchedule() {
        return ((mEndSchedules.size() - mStartSchedules.size()) == 1);
    }

    public boolean isEndLastSchedule() {
        return (mEndSchedules.size() == mStartSchedules.size());
    }
}

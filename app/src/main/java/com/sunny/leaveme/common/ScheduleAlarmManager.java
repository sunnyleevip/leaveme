package com.sunny.leaveme.common;

import android.content.Context;
import android.util.Log;

import com.sunny.leaveme.db.entity.ScheduleItem;

import java.util.ArrayList;

/**
 * Created by Sunny Li on 2016/9/16.
 *   Create Start & Stop alarm for schedule item.
 * Modified by Sunny Li on 2016/10/7.
 *   Change AlarmHelper to ClassifiedAlarm.
 */
public class ScheduleAlarmManager {
    private final static String TAG = "ScheduleAlarmManager";
    private final static String ALARM_TYPE_SCHEDULE_START = "Schedule Start Alarm";
    private final static String ALARM_TYPE_SCHEDULE_END = "Schedule End Alarm";
    private OnScheduleAlarmTimeoutListener mOnScheduleAlarmTimeoutListener;
    private ArrayList<ScheduleItem> mAlarmItems;

    private ArrayList<Integer> mStartSchedules;
    private ArrayList<Integer> mEndSchedules;
    private Context mContext;

    public ScheduleAlarmManager(Context context, OnScheduleAlarmTimeoutListener onScheduleAlarmTimeoutListener) {
        mContext = context;
        mAlarmItems = new ArrayList<>();
        mStartSchedules = new ArrayList<>();
        mEndSchedules = new ArrayList<>();
        mOnScheduleAlarmTimeoutListener = onScheduleAlarmTimeoutListener;
        ClassifiedAlarm.registerType(ALARM_TYPE_SCHEDULE_START, new ClassifiedAlarm.OnClassifiedAlarmTimeoutListener() {
            @Override
            public void onClassifiedAlarmTimeout(int id, String type) {
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

        ClassifiedAlarm.registerType(ALARM_TYPE_SCHEDULE_END, new ClassifiedAlarm.OnClassifiedAlarmTimeoutListener() {
            @Override
            public void onClassifiedAlarmTimeout(int id, String type) {
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

    public void finish() {
        ClassifiedAlarm.unregisterType(ALARM_TYPE_SCHEDULE_START);
        ClassifiedAlarm.unregisterType(ALARM_TYPE_SCHEDULE_END);
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
            ClassifiedAlarm.startOneshotAlarm(mContext, ALARM_TYPE_SCHEDULE_START, scheduleItem.getId(),
                    scheduleItem.getStartTimeCalendar().getTimeInMillis());

            Log.d(TAG, "End Date: " + scheduleItem.getEndTimeCalendar().getTime().toString());
            ClassifiedAlarm.startOneshotAlarm(mContext, ALARM_TYPE_SCHEDULE_END, scheduleItem.getId(),
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
                        ClassifiedAlarm.cancelAlarm(mContext, ALARM_TYPE_SCHEDULE_START, id);
                        mStartSchedules.remove(i);
                        break;
                    }
                }

                for (int j = 0; j < mEndSchedules.size(); ++j) {
                    if (mEndSchedules.get(i) == id) {
                        Log.d(TAG, "Find exist end alarm id: " + id);
                        ClassifiedAlarm.cancelAlarm(mContext, ALARM_TYPE_SCHEDULE_END, id);
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

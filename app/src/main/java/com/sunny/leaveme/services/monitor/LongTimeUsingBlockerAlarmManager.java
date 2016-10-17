package com.sunny.leaveme.services.monitor;

import android.content.Context;
import android.util.Log;

import com.sunny.leaveme.common.ClassifiedAlarm;

import java.util.Calendar;

/**
 * Created by Sunny Li on 2016/10/7.
 * Start two alarm to tracking using time and blocking time.
 * Call callback when any of those two alarm is timeout.
 */

class LongTimeUsingBlockerAlarmManager {
    private static final String TAG = "LTUBlockerAlarmManager";
    private final static String ALARM_TYPE_LONG_TIME_USING_BLOCKER = "Long Time Using Blocker Alarm";
    private final static int ALARM_ID_USING = 0;
    private final static int ALARM_ID_BLOCKING = 1;
    private Context mContext;
    private OnAlarmTimeoutListener mOnAlarmTimeoutListener;
    private long mOldLongTimeBlockerStartTimeInMillis = 0;

    LongTimeUsingBlockerAlarmManager(Context context,
                                            OnAlarmTimeoutListener onAlarmTimeoutListener) {
        mContext = context;
        mOnAlarmTimeoutListener = onAlarmTimeoutListener;
        ClassifiedAlarm.registerType(ALARM_TYPE_LONG_TIME_USING_BLOCKER,
                new ClassifiedAlarm.OnClassifiedAlarmTimeoutListener() {
            @Override
            public void onClassifiedAlarmTimeout(int id, String type) {
                if (id == ALARM_ID_USING) {
                    mOnAlarmTimeoutListener.onUsingAlarmTimeout();
                } else if (id == ALARM_ID_BLOCKING) {
                    mOnAlarmTimeoutListener.onBlockingAlarmTimeout();
                }
            }
        });
    }

    void finish() {
        ClassifiedAlarm.unregisterType(ALARM_TYPE_LONG_TIME_USING_BLOCKER);
    }

    void updateAlarm(long alarmStartTimeInMillis, int usingMinutes, int blockingMinutes) {
        cancelAlarm();
        mOldLongTimeBlockerStartTimeInMillis = alarmStartTimeInMillis;
        startAlarm(alarmStartTimeInMillis, usingMinutes, blockingMinutes);
    }

    void restartAlarm(int usingMinutes, int blockingMinutes) {
        cancelAlarm();
        startAlarm(mOldLongTimeBlockerStartTimeInMillis, usingMinutes, blockingMinutes);
    }

    private void startAlarm(long alarmStartTimeInMillis, int usingMinutes, int blockingMinutes) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(alarmStartTimeInMillis);
        calendar.add(Calendar.MINUTE, usingMinutes);
        long longTimeBlockerUsingEndTimeInMillis = calendar.getTimeInMillis();
        calendar.add(Calendar.MINUTE, blockingMinutes);
        long longTimeBlockerBlockingEndTimeInMillis = calendar.getTimeInMillis();
        Log.d(TAG, "alarmStartTimeInMillis:" + alarmStartTimeInMillis);
        Log.d(TAG, "usingMinutes:" + usingMinutes);
        Log.d(TAG, "blockingMinutes:" + blockingMinutes);
        Log.d(TAG, "longTimeBlockerUsingEndTimeInMillis:" + longTimeBlockerUsingEndTimeInMillis);
        Log.d(TAG, "longTimeBlockerBlockingEndTimeInMillis:" + longTimeBlockerBlockingEndTimeInMillis);
        Log.d(TAG, "currentTimeInMillis:" + System.currentTimeMillis());
        ClassifiedAlarm.startOneshotAlarm(mContext, ALARM_TYPE_LONG_TIME_USING_BLOCKER,
                ALARM_ID_USING, longTimeBlockerUsingEndTimeInMillis);
        ClassifiedAlarm.startOneshotAlarm(mContext, ALARM_TYPE_LONG_TIME_USING_BLOCKER,
                ALARM_ID_BLOCKING, longTimeBlockerBlockingEndTimeInMillis);
    }

    void cancelAlarm() {
        ClassifiedAlarm.cancelAlarm(mContext, ALARM_TYPE_LONG_TIME_USING_BLOCKER, ALARM_ID_USING);
        ClassifiedAlarm.cancelAlarm(mContext, ALARM_TYPE_LONG_TIME_USING_BLOCKER, ALARM_ID_BLOCKING);
    }

    interface OnAlarmTimeoutListener {
        void onUsingAlarmTimeout();
        void onBlockingAlarmTimeout();
    }
}

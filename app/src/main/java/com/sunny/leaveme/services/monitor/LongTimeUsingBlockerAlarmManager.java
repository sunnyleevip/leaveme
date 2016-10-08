package com.sunny.leaveme.services.monitor;

import android.content.Context;

import com.sunny.leaveme.common.ClassifiedAlarm;

/**
 * Created by Sunny Li on 2016/10/7.
 * Start two alarm to tracking using time and blocking time.
 * Call callback when any of those two alarm is timeout.
 */

class LongTimeUsingBlockerAlarmManager {
    private final static String ALARM_TYPE_LONG_TIME_USING_BLOCKER = "Long Time Using Blocker Alarm";
    private final static int ALARM_ID_USING = 0;
    private final static int ALARM_ID_BLOCKING = 1;
    private Context mContext;
    private OnAlarmTimeoutListener mOnAlarmTimeoutListener;
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

    void updateAlarm(long usingMillis, long blockingMillis) {
        cancelAlarm();
        startAlarm(usingMillis, blockingMillis);
    }

    void startAlarm(long usingMillis, long blockingMillis) {
        ClassifiedAlarm.startOneshotAlarm(mContext, ALARM_TYPE_LONG_TIME_USING_BLOCKER,
                ALARM_ID_USING, usingMillis);
        ClassifiedAlarm.startOneshotAlarm(mContext, ALARM_TYPE_LONG_TIME_USING_BLOCKER,
                ALARM_ID_BLOCKING, blockingMillis);
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

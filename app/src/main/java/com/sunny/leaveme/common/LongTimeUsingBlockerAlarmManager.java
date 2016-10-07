package com.sunny.leaveme.common;

import android.content.Context;

/**
 * Created by Sunny Li on 2016/10/7.
 * Start two alarm to tracking using time and blocking time.
 * Call callback when any of those two alarm is timeout.
 */

public class LongTimeUsingBlockerAlarmManager {
    private final static String ALARM_TYPE_LONG_TIME_USING_BLOCKER = "Long Time Using Blocker Alarm";
    private final static int ALARM_ID_USING = 0;
    private final static int ALARM_ID_BLOCKING = 1;
    private Context mContext;
    private OnAlarmTimeoutListener mOnAlarmTimeoutListener;
    public LongTimeUsingBlockerAlarmManager(Context context,
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

    public void finish() {
        ClassifiedAlarm.unregisterType(ALARM_TYPE_LONG_TIME_USING_BLOCKER);
    }

    public void updateAlarm(long usingMillis, long blockingMillis) {
        cancelAlarm();
        startAlarm(usingMillis, blockingMillis);
    }

    public void startAlarm(long usingMillis, long blockingMillis) {
        ClassifiedAlarm.startOneshotAlarm(mContext, ALARM_TYPE_LONG_TIME_USING_BLOCKER,
                ALARM_ID_USING, usingMillis);
        ClassifiedAlarm.startOneshotAlarm(mContext, ALARM_TYPE_LONG_TIME_USING_BLOCKER,
                ALARM_ID_BLOCKING, blockingMillis);
    }

    public void cancelAlarm() {
        ClassifiedAlarm.cancelAlarm(mContext, ALARM_TYPE_LONG_TIME_USING_BLOCKER, ALARM_ID_USING);
        ClassifiedAlarm.cancelAlarm(mContext, ALARM_TYPE_LONG_TIME_USING_BLOCKER, ALARM_ID_BLOCKING);
    }

    public interface OnAlarmTimeoutListener {
        void onUsingAlarmTimeout();
        void onBlockingAlarmTimeout();
    }
}

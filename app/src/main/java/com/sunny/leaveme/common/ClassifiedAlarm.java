package com.sunny.leaveme.common;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Sunny Li on 2016/10/6.
 *   User should register types {@link #registerType(String, OnClassifiedAlarmTimeoutListener)}
 *   before start using alarm.
 *   Max alarm number of each type is up to 1000.
 *   Also user should unregister their own type {@link #unregisterType(String)}
 *   when never use it any more.
 */

class ClassifiedAlarm {
    private final static String TAG = "ClassifiedAlarm";
    private final static int MAX_ALARM_NUM_PER_TYPE = 1000;

    private static ArrayList<String> sTypes = new ArrayList<>();
    private static ArrayList<OnClassifiedAlarmTimeoutListener> sAlarmTimeoutListener = new ArrayList<>();
    private static AlarmHelper sAlarmHelper = new AlarmHelper(new AlarmHelper.OnAlarmTimeoutListener() {
        @Override
        public void onAlarmTimeout(int alarmId) {
            int typeId = alarmId / MAX_ALARM_NUM_PER_TYPE;
            int id = alarmId % MAX_ALARM_NUM_PER_TYPE;
            Log.d(TAG, "onAlarmTimeout--type: " + sTypes.get(typeId) + " id:" + id);
            sAlarmTimeoutListener.get(typeId).onClassifiedAlarmTimeout(id, sTypes.get(typeId));
        }
    });

    static int registerType(String typeStr, OnClassifiedAlarmTimeoutListener onAlarmTimeoutListener) {
        sTypes.add(typeStr);
        sAlarmTimeoutListener.add(onAlarmTimeoutListener);
        Log.i(TAG, "registerType: " + typeStr + " id: " + getIdByType(typeStr));
        return getIdByType(typeStr);
    }

    private static void unregisterType(int typeId) {
        Log.i(TAG, "unregisterType: " + sTypes.get(typeId) + " id: " + typeId);
        sTypes.remove(typeId);
        sAlarmTimeoutListener.remove(typeId);
    }

    static void unregisterType(String typeStr) {
        if (getIdByType(typeStr) >= 0) {
            unregisterType(getIdByType(typeStr));
        }
    }

    private static int getIdByType(String typeStr) {
        return sTypes.indexOf(typeStr);
    }

    static void startOneshotAlarm(Context context, String typeStr, int id, long millisecond) {
        if (id >= MAX_ALARM_NUM_PER_TYPE) {
            throw new IllegalArgumentException("id >= MAX_ALARM_NUM_PER_TYPE");
        }
        int alarmId = getIdByType(typeStr) * MAX_ALARM_NUM_PER_TYPE + id;
        sAlarmHelper.startOneshotAlarm(context, alarmId, millisecond);
    }

    static boolean cancelAlarm(Context context, String typeStr, int id) {
        int alarmId = getIdByType(typeStr) * MAX_ALARM_NUM_PER_TYPE + id;
        return sAlarmHelper.cancelAlarm(context, alarmId);
    }

    interface OnClassifiedAlarmTimeoutListener {
        void onClassifiedAlarmTimeout(int id, String type);
    }
}

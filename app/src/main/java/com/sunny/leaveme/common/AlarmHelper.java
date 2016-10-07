package com.sunny.leaveme.common;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Sunny Li on 2016/9/13.
 *   Alarmhelper for using AlarmManager.
 * Modified by Sunny Li on 2016/10/7
 *   User should use ClassifiedAlarm instead of AlarmHelper.
 *   ClassifiedAlarm will register callback to AlarmHelper
 *   and dispatch alarm timeout message to different type of alarm.
 */
class AlarmHelper {
    private final static String TAG = "AlarmHelper";
    private static OnAlarmTimeoutListener sOnAlarmTimeoutListener;

    AlarmHelper(OnAlarmTimeoutListener onAlarmTimeoutListener) {
        sOnAlarmTimeoutListener = onAlarmTimeoutListener;
    }

    /*void startRepeatAlarm(Context context, int id, long millisecond, long intervalMillis) {
        Intent intent = new Intent(context, AlarmBroadcastReceiver.class);
        intent.putExtra("id", id);
        PendingIntent sender = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_NO_CREATE);
        if (sender != null) {
            Log.e(TAG, "Intent exist, cannot create id:"+ id);
            return;
        }
        sender = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        if (sender == null) {
            Log.e(TAG, "Intent cannot create id:"+ id);
            return;
        }

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC_WAKEUP, millisecond, intervalMillis, sender);
    }*/

    void startOneshotAlarm(Context context, int id, long millisecond) {
        Intent intent = new Intent(context, AlarmBroadcastReceiver.class);
        intent.putExtra("id", id);
        PendingIntent sender = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_NO_CREATE);
        if (sender != null) {
            Log.e(TAG, "Intent exist, cannot create id:" + id);
            return;
        }
        sender = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_ONE_SHOT);
        if (sender == null) {
            Log.e(TAG, "Intent cannot create id:" + id);
            return;
        }

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, millisecond, sender);
        Log.d(TAG, "start one shot alarm id:" + id);
    }

    boolean cancelAlarm(Context context, int id) {
        Intent intent = new Intent(context, AlarmBroadcastReceiver.class);
        intent.putExtra("id", id);
        PendingIntent sender = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_ONE_SHOT);
        if (sender == null) {
            Log.d(TAG, "Alarm has already canceled, intent not found id:" + id);
            return false;
        }

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(sender);
        Log.d(TAG, "cancel alarm id: " + id);
        return true;
    }

    public static class AlarmBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (sOnAlarmTimeoutListener != null) {
                sOnAlarmTimeoutListener.onAlarmTimeout(intent.getIntExtra("id", 0));
            }
        }
    }

    interface OnAlarmTimeoutListener {
        void onAlarmTimeout(int id);
    }
}

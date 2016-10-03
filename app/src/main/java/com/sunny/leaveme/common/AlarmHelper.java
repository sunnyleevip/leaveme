package com.sunny.leaveme.common;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

/**
 * Created by Sunny Li on 2016/9/13.
 * Alarmhelper for using alarmmanager.
 */
public class AlarmHelper {
    private final static String TAG = "AlarmHelper";
    private final static String ACTION_ALARM_TIMEOUT = "com.sunny.leaveme.ACTION_ALARM_TIMEOUT";
    private Context mContext;
    private OnAlarmTimeoutListener mOnAlarmTimeoutListener = null;
    private SelfAlarmBroadcastReceiver mSelfAlarmBroadcastReceiver = null;

    public AlarmHelper(Context context, OnAlarmTimeoutListener onAlarmTimeoutListener) {
        mContext = context;
        mOnAlarmTimeoutListener = onAlarmTimeoutListener;

        if (mSelfAlarmBroadcastReceiver == null) {
            mSelfAlarmBroadcastReceiver = new SelfAlarmBroadcastReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION_ALARM_TIMEOUT);
            mContext.registerReceiver(mSelfAlarmBroadcastReceiver, filter);
        }
    }

    @Override
    protected void finalize() throws java.lang.Throwable {
        super.finalize();
        mContext.unregisterReceiver(mSelfAlarmBroadcastReceiver);
        mSelfAlarmBroadcastReceiver = null;
    }

    public void startRepeatAlarm(int id, long millisecond, long intervalMillis) {
        Intent intent = new Intent(mContext, AlarmBroadcastReceiver.class);
        intent.putExtra("id", id);
        PendingIntent sender = PendingIntent.getBroadcast(mContext, id, intent, PendingIntent.FLAG_NO_CREATE);
        if (sender != null) {
            Log.e(TAG, "Intent exist, cannot create id:"+ id);
            return;
        }
        sender = PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        if (sender == null) {
            Log.e(TAG, "Intent cannot create id:"+ id);
            return;
        }

        AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC_WAKEUP, millisecond, intervalMillis, sender);
    }

    public void startOneshotAlarm(int id, long millisecond) {
        Intent intent = new Intent(mContext, AlarmBroadcastReceiver.class);
        intent.putExtra("id", id);
        PendingIntent sender = PendingIntent.getBroadcast(mContext, id, intent, PendingIntent.FLAG_NO_CREATE);
        if (sender != null) {
            Log.e(TAG, "Intent exist, cannot create id:" + id);
            return;
        }
        sender = PendingIntent.getBroadcast(mContext, id, intent, PendingIntent.FLAG_ONE_SHOT);
        if (sender == null) {
            Log.e(TAG, "Intent cannot create id:" + id);
            return;
        }

        AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, millisecond, sender);
        Log.d(TAG, "start one shot alarm id:" + id);
    }

    public boolean cancelAlarm(int id) {
        Intent intent = new Intent(mContext, AlarmBroadcastReceiver.class);
        intent.putExtra("id", id);
        PendingIntent sender = PendingIntent.getBroadcast(mContext, id, intent, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_ONE_SHOT);
        if (sender == null) {
            Log.d(TAG, "Alarm has already canceled, intent not found id:" + id);
            return false;
        }

        AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        am.cancel(sender);
        Log.d(TAG, "cancel alarm id: " + id);
        return true;
    }

    public static class AlarmBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Intent intentSelf = new Intent(ACTION_ALARM_TIMEOUT);
            intentSelf.putExtra("id", intent.getIntExtra("id", 0));
            context.sendBroadcast(intentSelf);
        }
    }

    private class SelfAlarmBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "SelfAlarmBroadcastReceiver on id: " + intent.getIntExtra("id", 0));
            if (mOnAlarmTimeoutListener != null) {
                mOnAlarmTimeoutListener.onAlarmTimeout(intent.getIntExtra("id", 0));
            }
        }
    }

    public interface OnAlarmTimeoutListener {
        void onAlarmTimeout(int id);
    }
}

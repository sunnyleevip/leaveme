package com.sunny.leaveme.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.sunny.leaveme.ScheduleAlarmManager;
import com.sunny.leaveme.db.DataHelper;
import com.sunny.leaveme.db.entity.ScheduleItem;

import java.util.ArrayList;

public class ManagerService extends Service {
    private static final String TAG = "ManagerService";
    private final static String ACTION_UPDATE_VIEW = "com.sunny.leaveme.ACTION_UPDATE_VIEW";
    private final static String ACTION_START_MONITOR = "com.sunny.leaveme.ACTION_START_MONITOR";
    private final static String ACTION_STOP_MONITOR = "com.sunny.leaveme.ACTION_STOP_MONITOR";

    private Context mContext;
    private ScheduleAlarmManager mScheduleAlarmManager;
    private DataHelper mDataHelper;
    private LocalBroadcastManager mLocalBroadcastManager;

    @Override
    public void onCreate() {
        mContext = this;
        mDataHelper = new DataHelper(this);
        final ArrayList<ScheduleItem> scheduleItems = mDataHelper.getAllScheduleItems();
        mScheduleAlarmManager = new ScheduleAlarmManager(this, new ScheduleAlarmManager.OnScheduleAlarmTimeoutListener() {
            @Override
            public void onScheduleStartAlarmTimeout(int id, boolean isRepeat) {
                Log.d(TAG, "Start alarm time out, id:" + id + ", isRepeat:" + isRepeat);
                if (mScheduleAlarmManager.isStartFirstSchedule()) {
                    startMonitor();
                }
            }

            @Override
            public void onScheduleEndAlarmTimeout(int id, boolean isRepeat) {
                Log.d(TAG, "End alarm time out, id:" + id + ", isRepeat:" + isRepeat);
                if (mScheduleAlarmManager.isEndLastSchedule()) {
                    stopMonitor();
                }
                if (!isRepeat) {
                    ScheduleItem scheduleItem = mDataHelper.getScheduleItemsById(id);
                    scheduleItem.setAvailable(false);
                    mDataHelper.updateScheduleItem(scheduleItem);
                    Intent intent = new Intent(ACTION_UPDATE_VIEW);
                    intent.putExtra("id", id);
                    mLocalBroadcastManager.sendBroadcast(intent);
                }
            }
        });
        mScheduleAlarmManager.startAlarms(scheduleItems);

        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
    }

    @Override
    public void onDestroy() {
        mDataHelper.close();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private final IBinder binder = new ManagerServiceBinder();

    public class ManagerServiceBinder extends Binder {
        public ManagerService getService() {
            return ManagerService.this;
        }
    }

    public static class receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
                Intent i= new Intent(context, ManagerService.class);
                context.startService(i);
            }
        }
    }

    private void startMonitor() {
        Intent intent = new Intent(ACTION_START_MONITOR);
        mLocalBroadcastManager.sendBroadcast(intent);
    }

    private void stopMonitor() {
        Intent intent = new Intent(ACTION_STOP_MONITOR);
        mLocalBroadcastManager.sendBroadcast(intent);
    }

    public void addAlarm(ScheduleItem scheduleItem) {
        mScheduleAlarmManager.startAlarm(scheduleItem);
    }

    public void updateAlarm(ScheduleItem scheduleItem) {
        mScheduleAlarmManager.updateAlarm(scheduleItem);
    }

    public void cancelAlarm(int id) {
        mScheduleAlarmManager.cancelAlarm(id);
        if (mScheduleAlarmManager.isEndLastSchedule()) {
            stopMonitor();
        }
    }
}



package com.sunny.leaveme.services.monitor;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.sunny.leaveme.common.ActionStr;
import com.sunny.leaveme.common.SensorReader;
import com.sunny.leaveme.common.SensorReader.SensorChangedListener;
import com.sunny.leaveme.activities.ScreenBlockerActivity;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class MonitorService extends Service {
    private static final String TAG = "MonitorService";
    private final static int MONITOR_CHECK = 101;
    private final static float SENSOR_THRESHOLD_LIGHT_LOW = 1.0f;
    private static final String PREFERENCE_KEY_LIGHT_DETECT = "auto_detected_surrounding_light_switch";
    private static final String PREFERENCE_KEY_LONG_TIME_USE_BLOCKER_SWITCH = "avoid_using_too_long_switch";
    private static final String PREFERENCE_KEY_LONG_TIME_USE_BLOCKER_USING_TIME = "etp_long_time_using_time";
    private static final String PREFERENCE_KEY_LONG_TIME_USE_BLOCKER_BLOCKING_TIME = "etp_long_time_blocking_time";

    private int mReason = MONITOR_REASON_NONE;
    private final static int MONITOR_REASON_NONE = 0;
    private final static int MONITOR_REASON_ALARM = 1;
    private final static int MONITOR_REASON_LIGHT = 2;
    private final static int MONITOR_REASON_LONG_TIME_USING_BLOCK = 3;

    private LocalBroadcastManager mLocalBroadcastManager;

    private static boolean mIsTimerRunning = false;
    private final TimerHandler mTimerHandler = new TimerHandler(this);

    private static class TimerHandler extends Handler {
        private final WeakReference<MonitorService> mService;

        private TimerHandler(MonitorService service) {
            mService = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MONITOR_CHECK:
                    MonitorService monitorService = mService.get();
                    if (monitorService == null) {
                        Log.e(TAG, "handleMessage|monitorService is null");
                        break;
                    }

                    if (!monitorService.isScreenOn()) {
                        Log.e(TAG, "screen off");
                        break;
                    }

                    ActivityManagerHelper activityManagerHelper =
                            new ActivityManagerHelper(monitorService.getContext());
                    if (!"com.sunny.leaveme".equals(activityManagerHelper.currentProcessName())) {
                        monitorService.startScreenBlocker();
                    }

                    break;
            }
        }
    }

    private static TimerTask mTask = null;
    private static Timer mTimer = null;
    private Context mContext = null;
    private LockScreenReceiver mLockScreenReceiver;

    private SensorReader mSensorReader = null;
    private LongTimeUsingBlockerAlarmManager mLongTimeUsingBlockerAlarmManager;
    private int mLongTimeBlockerUsingMinutes = 0;
    private int mLongTimeBlockerBlockingMinutes = 0;
    private long mScreenOffTimeInMillis = 0;
    private long mScreenOnTimeInMillis = 0;
    private long mOldLongTimeBlockerStartTimeInMillis = 0;
    private final static long MILLIS_PER_MINUTE = 1000 * 60;

    @Override
    public void onCreate() {
        mContext = this;
        registerScreenLockReceiver();
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ActionStr.ACTION_START_MONITOR);
        intentFilter.addAction(ActionStr.ACTION_STOP_MONITOR);
        intentFilter.addAction(ActionStr.ACTION_STOP_MONITOR_AND_KEEP_REASON);
        intentFilter.addAction(ActionStr.ACTION_UPDATE_LIGHT_SWITCH_VALUE);
        intentFilter.addAction(ActionStr.ACTION_UPDATE_LONG_TIME_BLOCKER_SWITCH_VALUE);
        intentFilter.addAction(ActionStr.ACTION_UPDATE_LONG_TIME_BLOCKER_TIMING_VALUE);
        mLocalBroadcastManager.registerReceiver(mLocalBroadcastReceiver, intentFilter);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mSensorReader = new SensorReader(this);
        if (mSensorReader.isEnabled(Sensor.TYPE_LIGHT)) {
            // support light sensor
            if (prefs.getBoolean(PREFERENCE_KEY_LIGHT_DETECT, true)) {
                mSensorReader.setSensorChangedListener(Sensor.TYPE_LIGHT, mSensorChangedListener);
            }
        }
        mSensorReader.start();

        // Long time use blocker
        mLongTimeUsingBlockerAlarmManager = new LongTimeUsingBlockerAlarmManager(this,
                new LongTimeUsingBlockerAlarmManager.OnAlarmTimeoutListener() {
                    @Override
                    public void onUsingAlarmTimeout() {
                        Log.d(TAG, "onUsingAlarmTimeout");
                        if (mReason == MONITOR_REASON_NONE) {
                            mReason = MONITOR_REASON_LONG_TIME_USING_BLOCK;
                            startMonitorTimer();
                        }
                    }

                    @Override
                    public void onBlockingAlarmTimeout() {
                        Log.d(TAG, "onBlockingAlarmTimeout");
                        if (mReason == MONITOR_REASON_LONG_TIME_USING_BLOCK) {
                            stopMonitorTimer();
                            stopScreenBlocker();
                            mReason = MONITOR_REASON_NONE;
                        }
                    }
                });
        if (prefs.getBoolean(PREFERENCE_KEY_LONG_TIME_USE_BLOCKER_SWITCH, true)) {
            updateLongTimeUseBlockerAlarm();
        }

        Log.d(TAG, "MonitorService onCreate");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "MonitorService onDestroy");
        mSensorReader.stop();
        mLocalBroadcastManager.unregisterReceiver(mLocalBroadcastReceiver);
        unregisterReceiver(mLockScreenReceiver);
        mLongTimeUsingBlockerAlarmManager.cancelAlarm();
        mLongTimeUsingBlockerAlarmManager.finish();
        stopMonitorTimer();
        stopScreenBlocker();
    }

    private Context getContext() {
        return mContext;
    }

    private BroadcastReceiver mLocalBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "mLocalBroadcastReceiver.onReceive");
            if (intent.getAction().equals(ActionStr.ACTION_START_MONITOR)) {
                mReason = MONITOR_REASON_ALARM;
                startMonitorTimer();
                mSensorReader.start();
            } else if (intent.getAction().equals(ActionStr.ACTION_STOP_MONITOR)) {
                mReason = MONITOR_REASON_NONE;
                stopMonitorTimer();
                mSensorReader.stop();
                stopScreenBlocker();
            } else if (intent.getAction().equals(ActionStr.ACTION_STOP_MONITOR_AND_KEEP_REASON)) {
                stopMonitorTimer();
                mSensorReader.stop();
            } else if (intent.getAction().equals(ActionStr.ACTION_UPDATE_LIGHT_SWITCH_VALUE)) {
                boolean isChecked = intent.getBooleanExtra("light_switch", true);
                if (isChecked) {
                    mSensorReader.setSensorChangedListener(Sensor.TYPE_LIGHT, mSensorChangedListener);
                    mSensorReader.start();
                } else {
                    mSensorReader.removeSensorChangedListener(Sensor.TYPE_LIGHT);
                    mSensorReader.stopWithNoListener();
                    if (mReason == MONITOR_REASON_LIGHT) {
                        stopMonitorTimer();
                        stopScreenBlocker();
                    }
                }
            } else if (intent.getAction().equals(ActionStr.ACTION_UPDATE_LONG_TIME_BLOCKER_SWITCH_VALUE)) {
                boolean isChecked = intent.getBooleanExtra("long_time_blocker_switch", true);
                if (isChecked) {
                    updateLongTimeUseBlockerAlarm();
                } else {
                    mLongTimeUsingBlockerAlarmManager.cancelAlarm();
                }
            } else if (intent.getAction().equals(ActionStr.ACTION_UPDATE_LONG_TIME_BLOCKER_TIMING_VALUE)) {
                updateLongTimeUseBlockerTime();
            }
        }
    };

    SensorChangedListener mSensorChangedListener = new SensorChangedListener(){
        @Override
        public void onSensorChanged(SensorEvent event) {
            Log.d(TAG, "onSensorChanged, value: " + event.values[0]);
            if (event.values[0] < SENSOR_THRESHOLD_LIGHT_LOW) {
                mReason = MONITOR_REASON_LIGHT;
                startMonitorTimer();
            } else {
                mReason = MONITOR_REASON_NONE;
                stopMonitorTimer();
                stopScreenBlocker();
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void registerScreenLockReceiver() {
        mLockScreenReceiver = new LockScreenReceiver();
        IntentFilter lockFilter = new IntentFilter();
        lockFilter.addAction(Intent.ACTION_SCREEN_ON);
        lockFilter.addAction(Intent.ACTION_SCREEN_OFF);
        lockFilter.addAction(Intent.ACTION_USER_PRESENT);
        lockFilter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(mLockScreenReceiver, lockFilter);
    }

    private class LockScreenReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ((intent == null) || (intent.getAction() == null)) {
                Log.e(TAG, "Empty intent");
                return;
            }

            if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                Log.d(TAG, "Do nothing");
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                Log.d(TAG, "stop timer");
                stopMonitorTimer();
                mSensorReader.stop();
                mLongTimeUsingBlockerAlarmManager.cancelAlarm();
                mScreenOffTimeInMillis = System.currentTimeMillis();
            } else if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
                Log.d(TAG, "start timer");
                tryStartScreenBlockerIfNeed();
                mScreenOnTimeInMillis = System.currentTimeMillis();
                updateLongTimeUseBlockerTime();
                if (needUpdateBlockerAlarm()) {
                    updateLongTimeUseBlockerAlarm();
                } else {
                    startOriginalLongTimeUseBlockerAlarm();
                }
            } else if (intent.getAction().equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                String reason = intent.getStringExtra("reason");
                if (reason == null) {
                    Log.d(TAG, "Ignore no reason intent");
                    return;
                }

                if (reason.equals("homekey")) {
                    Log.d(TAG, "homekey");
                    tryStartScreenBlockerIfNeed();
                }
            }
        }
    }

    public static class receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
                Intent i= new Intent(context, MonitorService.class);
                context.startService(i);
            }
        }
    }

    private void stopMonitorTimer() {
        if (mIsTimerRunning) {
            if (mTimer != null) {
                mTimer.cancel();
                mTimer = null;
            }

            if (mTask != null) {
                mTask.cancel();
                mTask = null;
            }

            mIsTimerRunning = false;
        }
    }

    private void startMonitorTimer() {
        if (mTimer == null) {
            mTimer = new Timer();
        }

        if (mTask == null) {
            mTask = new TimerTask() {
                @Override
                public void run() {
                    Message message = new Message();
                    message.what = MONITOR_CHECK;
                    message.obj = System.currentTimeMillis();
                    mTimerHandler.sendMessage(message);
                }
            };
        }

        if (mTimer != null) {
            if (!mIsTimerRunning) {
                mIsTimerRunning = true;
                mTimer.schedule(mTask, 1000, 500);
            }
        }
    }

    private boolean isScreenOn() {
        if (mContext == null) {
            Log.e(TAG, "isScreenOn, mContext is null");
            return false;
        }

        PowerManager powerManager = (PowerManager)mContext.getSystemService(POWER_SERVICE);

        if (powerManager == null) {
            Log.e(TAG, "Cannot access PowerManager");
            return false;
        }

        return powerManager.isInteractive();
    }

    private void startScreenBlocker() {
        Intent intent = new Intent(mContext, ScreenBlockerActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }

    private void stopScreenBlocker() {
        Intent intent = new Intent(ActionStr.ACTION_STOP_SCREEN_BLOCKER);
        mLocalBroadcastManager.sendBroadcast(intent);
    }

    private void tryStartScreenBlockerIfNeed() {
        if (mReason != MONITOR_REASON_NONE) {
            startMonitorTimer();
            mSensorReader.start();
        }
    }

    private void updateLongTimeUseBlockerTime() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        mLongTimeBlockerUsingMinutes = Integer.parseInt(
                prefs.getString(PREFERENCE_KEY_LONG_TIME_USE_BLOCKER_USING_TIME, "0"));
        mLongTimeBlockerBlockingMinutes = Integer.parseInt(
                prefs.getString(PREFERENCE_KEY_LONG_TIME_USE_BLOCKER_BLOCKING_TIME, "0"));
    }

    private boolean needUpdateBlockerAlarm() {
        return (mScreenOnTimeInMillis - mScreenOffTimeInMillis) >
                mLongTimeBlockerBlockingMinutes * MILLIS_PER_MINUTE;
    }

    private void updateLongTimeUseBlockerAlarm() {
        Log.d(TAG, "updateLongTimeUseBlockerAlarm");
        updateLongTimeUseBlockerTime();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        mOldLongTimeBlockerStartTimeInMillis = calendar.getTimeInMillis();
        calendar.add(Calendar.MINUTE, mLongTimeBlockerUsingMinutes);
        long longTimeBlockerUsingEndTimeInMillis = calendar.getTimeInMillis();
        calendar.add(Calendar.MINUTE, mLongTimeBlockerBlockingMinutes);
        long longTimeBlockerBlockingEndTimeInMillis = calendar.getTimeInMillis();
        Log.d(TAG, "mOldLongTimeBlockerStartTimeInMillis:" + mOldLongTimeBlockerStartTimeInMillis);
        Log.d(TAG, "mLongTimeBlockerUsingMinutes:" + mLongTimeBlockerUsingMinutes);
        Log.d(TAG, "mLongTimeBlockerBlockingMinutes:" + mLongTimeBlockerBlockingMinutes);
        Log.d(TAG, "longTimeBlockerUsingEndTimeInMillis:" + longTimeBlockerUsingEndTimeInMillis);
        Log.d(TAG, "longTimeBlockerBlockingEndTimeInMillis:" + longTimeBlockerBlockingEndTimeInMillis);
        mLongTimeUsingBlockerAlarmManager.updateAlarm(
                longTimeBlockerUsingEndTimeInMillis, longTimeBlockerBlockingEndTimeInMillis);
    }

    private void startOriginalLongTimeUseBlockerAlarm() {
        Log.d(TAG, "startOriginalLongTimeUseBlockerAlarm");
        updateLongTimeUseBlockerTime();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(mOldLongTimeBlockerStartTimeInMillis);
        calendar.add(Calendar.MINUTE, mLongTimeBlockerUsingMinutes);
        long longTimeBlockerUsingEndTimeInMillis = calendar.getTimeInMillis();
        calendar.add(Calendar.MINUTE, mLongTimeBlockerBlockingMinutes);
        long longTimeBlockerBlockingEndTimeInMillis = calendar.getTimeInMillis();
        Log.d(TAG, "mOldLongTimeBlockerStartTimeInMillis:" + mOldLongTimeBlockerStartTimeInMillis);
        Log.d(TAG, "mLongTimeBlockerUsingMinutes:" + mLongTimeBlockerUsingMinutes);
        Log.d(TAG, "mLongTimeBlockerBlockingMinutes:" + mLongTimeBlockerBlockingMinutes);
        Log.d(TAG, "longTimeBlockerUsingEndTimeInMillis:" + longTimeBlockerUsingEndTimeInMillis);
        Log.d(TAG, "longTimeBlockerBlockingEndTimeInMillis:" + longTimeBlockerBlockingEndTimeInMillis);
        mLongTimeUsingBlockerAlarmManager.updateAlarm(
                longTimeBlockerUsingEndTimeInMillis, longTimeBlockerBlockingEndTimeInMillis);
    }
}

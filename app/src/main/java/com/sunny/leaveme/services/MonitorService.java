package com.sunny.leaveme.services;

import android.app.ActivityManager;
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

import com.sunny.leaveme.ActionStr;
import com.sunny.leaveme.SensorReader;
import com.sunny.leaveme.SensorReader.SensorChangedListener;
import com.sunny.leaveme.activities.ScreenBlockerActivity;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MonitorService extends Service {
    private static final String TAG = "MonitorService";
    private final static int MONITOR_CHECK = 101;
    private final static float SENSOR_THRESHOLD_LIGHT_LOW = 1.0f;
    private static final String PREFERENCE_KEY_LIGHT_DETECT = "auto_detected_surrounding_light_switch";

    private int mReason = MONITOR_REASON_NONE;
    private final static int MONITOR_REASON_NONE = 0;
    private final static int MONITOR_REASON_ALARM = 1;
    private final static int MONITOR_REASON_LIGHT = 2;

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

                    final int PROCESS_STATE_TOP = 2;
                    ActivityManager.RunningAppProcessInfo currentInfo = null;
                    Field field = null;
                    try {
                        field = ActivityManager.RunningAppProcessInfo.class.getDeclaredField("processState");
                    } catch (Exception ignored) {
                    }

                    ActivityManager am = (ActivityManager) monitorService.getContext().getSystemService(Context.ACTIVITY_SERVICE);
                    List<ActivityManager.RunningAppProcessInfo> appList = am.getRunningAppProcesses();
                    for (ActivityManager.RunningAppProcessInfo app : appList) {
                        if (app.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                                && app.importanceReasonCode == ActivityManager.RunningAppProcessInfo.REASON_UNKNOWN) {
                            Integer state = null;
                            try {
                                if (field != null) {
                                    state = field.getInt(app);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "field.getInt(app) Exception");
                                e.printStackTrace();
                            }
                            if (state != null && state == PROCESS_STATE_TOP) {
                                currentInfo = app;
                                break;
                            }
                        }
                    }

                    if (currentInfo != null) {
                        Log.d(TAG, "packageName: " + currentInfo.processName);
                        if (!currentInfo.processName.equals("com.sunny.leaveme")) {
                            Log.d(TAG, "running something else");
                            monitorService.startScreenBlocker();
                        }
                    } else {
                        Log.d(TAG, "running something else");
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

    @Override
    public void onCreate() {
        mContext = this;
        registerScreenLockReceiver();
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ActionStr.ACTION_START_MONITOR);
        intentFilter.addAction(ActionStr.ACTION_STOP_MONITOR);
        intentFilter.addAction(ActionStr.ACTION_UPDATE_LIGHT_SWITCH_VALUE);
        mLocalBroadcastManager.registerReceiver(mLocalBroadcastReceiver, intentFilter);

        mSensorReader = new SensorReader(this);
        if (mSensorReader.isEnabled(Sensor.TYPE_LIGHT)) {
            // support light sensor
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            if (prefs.getBoolean(PREFERENCE_KEY_LIGHT_DETECT, true)) {
                mSensorReader.setSensorChangedListener(Sensor.TYPE_LIGHT, mSensorChangedListener);
            }
        }
        mSensorReader.start();
        Log.d(TAG, "MonitorService onCreate");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "MonitorService onDestroy");
        mSensorReader.stop();
        mLocalBroadcastManager.unregisterReceiver(mLocalBroadcastReceiver);
        unregisterReceiver(mLockScreenReceiver);
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
                }
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
            } else if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
                Log.d(TAG, "start timer");
                tryStartScreenBlockerIfNeed();
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
}

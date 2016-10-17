package com.sunny.leaveme.services.monitor;

import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Created by Sunny Li on 2016/10/14.
 * Get current process name.
 */

class ActivityManagerHelper {
    private final static String TAG = "ActivityManagerHelper";
    private final static int PROCESS_STATE_TOP = 2;
    private Context mContext;
    ActivityManagerHelper(Context context) {
        mContext = context;
    }

    String currentProcessName() {
        ActivityManager.RunningAppProcessInfo currentInfo = null;
        Field field = null;
        try {
            field = ActivityManager.RunningAppProcessInfo.class.getDeclaredField("processState");
        } catch (Exception ignored) {
        }

        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
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

                if ((state != null) && (state == PROCESS_STATE_TOP)) {
                    currentInfo = app;
                    break;
                }
            }
        }

        if (currentInfo != null) {
            Log.d(TAG, "packageName: " + currentInfo.processName);
            return currentInfo.processName;
        } else {
            return null;
        }
    }
}

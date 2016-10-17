package com.sunny.leaveme.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by Sunny Li on 2016/10/15.
 * Read Preference data
 */

public class PreferenceDataHelper {
    private static final String TAG = "PreferenceDataHelper";
    private static final String PREFERENCE_KEY_LIGHT_DETECT =
            "auto_detected_surrounding_light_switch";
    private static final String PREFERENCE_KEY_LONG_TIME_USE_BLOCKER_SWITCH =
            "avoid_using_too_long_switch";
    private static final String PREFERENCE_KEY_LONG_TIME_USE_BLOCKER_USING_TIME =
            "etp_long_time_using_time";
    private static final String PREFERENCE_KEY_LONG_TIME_USE_BLOCKER_BLOCKING_TIME =
            "etp_long_time_blocking_time";

    private static boolean getSwitchValue(Context context, String preferenceKeySwitch, boolean defValue) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs == null) {
            Log.e(TAG, "Cannot getDefaultSharedPreferences");
            return false;
        }
        return prefs.getBoolean(preferenceKeySwitch, defValue);
    }

    private static String getTextValue(Context context, String preferenceKeySwitch, String defValue) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs == null) {
            Log.e(TAG, "Cannot getDefaultSharedPreferences");
            return null;
        }
        return prefs.getString(preferenceKeySwitch, defValue);
    }

    public static boolean getLightDetectSwitchValue(Context context) {
        return getSwitchValue(context, PREFERENCE_KEY_LIGHT_DETECT, true);
    }

    public static boolean getLongTimeUseBlockerSwitchValue(Context context) {
        return getSwitchValue(context, PREFERENCE_KEY_LONG_TIME_USE_BLOCKER_SWITCH, true);
    }

    public static int getLongTimeUseBlockerUsingTimeValue(Context context) {
        return Integer.parseInt(
                getTextValue(context, PREFERENCE_KEY_LONG_TIME_USE_BLOCKER_USING_TIME, "0"));
    }

    public static int getLongTimeUseBlockerBlockingTimeValue(Context context) {
        return Integer.parseInt(
                getTextValue(context, PREFERENCE_KEY_LONG_TIME_USE_BLOCKER_BLOCKING_TIME, "0"));
    }
}

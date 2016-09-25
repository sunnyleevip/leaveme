package com.sunny.leaveme.activities;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.sunny.leaveme.ActionStr;
import com.sunny.leaveme.R;
import com.sunny.leaveme.services.ManagerService;
import com.sunny.leaveme.services.MonitorService;

import java.util.List;

public class SettingsActivity extends AppCompatPreferenceActivity {
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            Log.d(TAG, "onPreferenceChange");
            if (mLightDetectingSwitch == preference) {
                Log.d(TAG, "Light switch changed: " + value);
                Intent intent = new Intent(ActionStr.ACTION_UPDATE_LIGHT_SWITCH_VALUE);
                intent.putExtra("light_switch", (Boolean)value);
                mLocalBroadcastManager.sendBroadcast(intent);
                return true;
            }

            return true;
        }
    };

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    private Context mContext;
    private final static String TAG = "SettingsActivity";
    private static LocalBroadcastManager mLocalBroadcastManager;
    private static SwitchPreference mLightDetectingSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        startService();
    }

    private void startService() {
        Intent i= new Intent(mContext, ManagerService.class);
        mContext.startService(i);

        i= new Intent(mContext, MonitorService.class);
        mContext.startService(i);
    }

    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || FunctionPreferenceFragment.class.getName().equals(fragmentName);
    }

    private static final String PREFERENCE_KEY_LIGHT_DETECT = "auto_detected_surrounding_light_switch";

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class FunctionPreferenceFragment extends PreferenceFragment {
        private Context mContext;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_function);
            setHasOptionsMenu(true);

            if (mContext == null) {
                Log.e(TAG, "no context");
                return;
            }

            SensorManager sensorManager = (SensorManager)mContext.getSystemService(Context.SENSOR_SERVICE);
            if (sensorManager == null) {
                Log.e(TAG, "no sensorManager");
                return;
            }

            mLightDetectingSwitch = (SwitchPreference)findPreference(PREFERENCE_KEY_LIGHT_DETECT);
            if (sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) == null) {
                Log.e(TAG, "Not support sensor: Sensor.TYPE_LIGHT");
                mLightDetectingSwitch.setEnabled(false);
            } else {
                mLightDetectingSwitch.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
                sBindPreferenceSummaryToValueListener.onPreferenceChange(mLightDetectingSwitch,
                        PreferenceManager
                                .getDefaultSharedPreferences(mLightDetectingSwitch.getContext())
                                .getBoolean(mLightDetectingSwitch.getKey(), true));
            }
        }

        @Override
        public void onAttach(Context context) {
            super.onAttach(context);
            mContext = context;
        }
    }
}

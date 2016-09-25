package com.sunny.leaveme;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Sunny Li on 2016/9/17.
 * SensorReader class, ALS
 */
public class SensorReader {
    private final static String TAG = "SensorReader";

    private android.hardware.SensorManager mSensorManager;
    private boolean isRegistered = false;
    private Sensor mLightSensor;
    private Map<Integer, SensorChangedListener> mListeners;

    public SensorReader(Context context) {
        mSensorManager = (android.hardware.SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
        mLightSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        if (mLightSensor == null) {
            Log.e(TAG, "Not support sensor: Sensor.TYPE_LIGHT");
        }

        mListeners = new HashMap<>();
    }

    public void start() {
        Log.d(TAG, "start");
        if ((mLightSensor != null) && (!isRegistered)) {
            if (mListeners.size() > 0) {
                mSensorManager.registerListener(mSensorEventListener, mLightSensor,
                        android.hardware.SensorManager.SENSOR_DELAY_NORMAL);
                isRegistered = true;
                Log.d(TAG, "registered");
            }
        }
    }

    public void stop() {
        Log.d(TAG, "stop");
        if ((mLightSensor != null) && (isRegistered)) {
            Log.d(TAG, "unregistered");
            mSensorManager.unregisterListener(mSensorEventListener);
            isRegistered = false;
        }
    }

    public void stopWithNoListener() {
        Log.d(TAG, "stopWithNoListener");
        if (mListeners.size() == 0) {
            stop();
        }
    }

    public boolean isEnabled(int type) {
        return (mSensorManager.getDefaultSensor(type) != null);
    }

    public void setSensorChangedListener(int type, SensorChangedListener sensorReaderListener) {
        mListeners.put(type, sensorReaderListener);
    }

    public void removeSensorChangedListener(int type) {
        for(Map.Entry<Integer, SensorChangedListener> entry : mListeners.entrySet()){
            if(type == entry.getKey()){
                mListeners.remove(type);
                return;
            }
        }
        Log.e(TAG, "No listener for type:" + type);
    }

    private final SensorEventListener mSensorEventListener= new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            Log.d(TAG,"onSensorChanged");
            for(Map.Entry<Integer, SensorChangedListener> entry : mListeners.entrySet()){
                if(event.sensor.getType() == entry.getKey()){
                    Log.d(TAG,"type: " + event.sensor.getType());
                    entry.getValue().onSensorChanged(event);
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            Log.d(TAG, "onAccuracyChanged");
        }
    };

    public interface SensorChangedListener {
        void onSensorChanged(SensorEvent event);
    }
}

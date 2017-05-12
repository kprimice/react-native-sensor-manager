package com.sensormanager;

import android.os.Bundle;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.support.annotation.Nullable;

import java.io.*;
import java.util.Date;
import java.util.Timer;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.bridge.ReactApplicationContext;

public class BarometerRecord implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mBarometer;
    private long lastUpdate = 0;
    private int i = 0, n = 0;
    private int delay;

    private ReactContext mReactContext;
    private Arguments mArguments;

    public BarometerRecord(ReactApplicationContext reactContext) {
        mSensorManager = (SensorManager)reactContext.getSystemService(reactContext.SENSOR_SERVICE);
        mReactContext = reactContext;
    }

    public int start(int delay) {
        this.delay = delay;
        if ((mBarometer = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)) != null) {
            mSensorManager.registerListener(this, mBarometer, SensorManager.SENSOR_DELAY_FASTEST);
        } else {
            return (0);
        }
        return (1);
    }

    public void stop() {
        mSensorManager.unregisterListener(this);
    }

    private void sendEvent(String eventName, @Nullable WritableMap params)
    {
        try {
            mReactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, params);
        } catch (RuntimeException e) {
            Log.e("ERROR", "java.lang.RuntimeException: Trying to invoke JS before CatalystInstance has been set!");
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;
        WritableMap map = mArguments.createMap();

        if (mySensor.getType() == Sensor.TYPE_PRESSURE) {
            long curTime = System.currentTimeMillis();
            i++;
            if ((curTime - lastUpdate) > delay) {
                i = 0;
                map.putDouble("press", sensorEvent.values[0]);
                sendEvent("Barometer", map);
                lastUpdate = curTime;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}

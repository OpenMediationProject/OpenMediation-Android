// Copyright 2020 ADTIMING TECHNOLOGY COMPANY LIMITED
// Licensed under the GNU Lesser General Public License Version 3

package com.openmediation.sdk.utils.device;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

import com.openmediation.sdk.utils.AdtUtil;
import com.openmediation.sdk.utils.DeveloperLog;
import com.openmediation.sdk.utils.WorkExecutor;
import com.openmediation.sdk.utils.crash.CrashUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SensorManager {

    private static class SmHolder {
        private static SensorManager singleton = new SensorManager();
    }

    public static SensorManager getSingleton() {
        return SmHolder.singleton;
    }

    private BitSet mSensorsType = new BitSet(6);
    private android.hardware.SensorManager mSensorManager;
    private List<SensorListener> mSensorEventListenerList;
    private JSONArray mSensorArrays;

    private SensorManager() {
        try {
            Context context = AdtUtil.getApplication();
            mSensorEventListenerList = new ArrayList<>();
            mSensorsType.set(1);
            mSensorsType.set(2);
            mSensorsType.set(4);
            mSensorManager = (android.hardware.SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
            WorkExecutor.execute(new RegisterSensorListenerRunnable());
            WorkExecutor.execute(new UnRegisterSensorListenerRunnable(), 10, TimeUnit.SECONDS);
        } catch (Throwable t) {
            CrashUtil.getSingleton().saveException(t);
            DeveloperLog.LogE("SensorManager error:", t);
        }
    }

    public JSONArray getSensorData() {
        if (mSensorArrays != null && mSensorArrays.length() > 0) {
            return mSensorArrays;
        }
        List<JSONObject> sensorsDataList = new ArrayList<>();
        try {
            if (!mSensorEventListenerList.isEmpty()) {
                for (SensorListener sensorEventListener : mSensorEventListenerList) {
                    DeveloperLog.LogD("UNRegisterListener:" + sensorEventListener.mSensorName);
                    mSensorManager.unregisterListener(sensorEventListener);
                    sensorsDataList.add(sensorEventListener.getData());
                }
            }
        } catch (Throwable t) {
            CrashUtil.getSingleton().saveException(t);
            DeveloperLog.LogE("SensorManager getSensors error:", t);
        }
        mSensorArrays = new JSONArray(sensorsDataList);
        return mSensorArrays;
    }

    class RegisterSensorListenerRunnable implements Runnable {

        @Override
        public void run() {
            try {
                if (mSensorManager != null) {
                    List<Sensor> sensorList = mSensorManager.getSensorList(-1);
                    for (Sensor sensor : sensorList) {
                        if (mSensorsType.get(sensor.getType())) {
                            SensorListener sensorEventListener = new SensorListener(sensor);
                            mSensorEventListenerList.add(sensorEventListener);
                            DeveloperLog.LogD("RegisterListener:" + sensorEventListener.mSensorName);
                            mSensorManager.registerListener(sensorEventListener, sensor, 0);
                        }
                    }
                }
            } catch (Throwable t) {
                DeveloperLog.LogE("SensorManager RegisterSensorListenerRunnable error:", t);
                CrashUtil.getSingleton().saveException(t);
            }
        }
    }

    class UnRegisterSensorListenerRunnable implements Runnable {
        @Override
        public void run() {
            getSensorData();
        }
    }

    class SensorListener implements SensorEventListener {

        private int mSensorType;
        private String mSensorName;
        private String mSensorVendor;
        private float[][] mSensorValues;
        private double mValuePow;
        private long mNowTime;

        private SensorListener(Sensor sensor) {
            super();
            try {
                mSensorType = sensor.getType();
                mSensorName = sensor.getName();
                mSensorVendor = sensor.getVendor();
                mSensorValues = new float[2][];
            } catch (Throwable t) {
                DeveloperLog.LogE("SensorManager error:", t);
                CrashUtil.getSingleton().saveException(t);
            }
        }

        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            try {
                if (sensorEvent != null && sensorEvent.values != null) {
                    Sensor sensor = sensorEvent.sensor;
                    if (sensor != null && sensor.getName() != null && sensor.getVendor() != null) {
                        int sensorType = sensorEvent.sensor.getType();
                        String sensorName = sensorEvent.sensor.getName();
                        String sensorVendor = sensorEvent.sensor.getVendor();
                        long sensorTimestamp = sensorEvent.timestamp;
                        float[] sensorValues = sensorEvent.values;
                        if (this.mSensorType == sensorType && this.mSensorName.equals(sensorName) && this.mSensorVendor.equals(sensorVendor)) {
                            float[] valuesOne = this.mSensorValues[0];
                            if (valuesOne == null) {
                                this.mSensorValues[0] = Arrays.copyOf(sensorValues, sensorValues.length);
                                return;
                            }
                            float[] valuesTow = this.mSensorValues[1];
                            if (valuesTow == null) {
                                float[] copyOf = Arrays.copyOf(sensorValues, sensorValues.length);
                                this.mSensorValues[1] = copyOf;
                                this.mValuePow = valuesPow(valuesOne, copyOf);
                            } else
                                if (50000000 <= sensorTimestamp - this.mNowTime) {
                                    this.mNowTime = sensorTimestamp;
                                    if (Arrays.equals(valuesTow, sensorValues)) {
                                        return;
                                    }
                                    double nowPow = valuesPow(valuesOne, sensorValues);
                                    if (nowPow > this.mValuePow) {
                                        this.mSensorValues[1] = Arrays.copyOf(sensorValues, sensorValues.length);
                                        this.mValuePow = nowPow;
                                    }
                                }
                        }
                    }
                }
            } catch (Throwable t) {
                DeveloperLog.LogE("SensorManager onSensorChanged error:", t);
                CrashUtil.getSingleton().saveException(t);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }

        public JSONObject getData() {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("sT", mSensorType);
                jsonObject.put("sN", mSensorName);
                jsonObject.put("sV", mSensorVendor);
                float[] valueOne = mSensorValues[0];
                if (valueOne != null) {
                    jsonObject.put("sVS", new JSONArray(array2List(valueOne)));
                }
                float[] valueTow = mSensorValues[1];
                if (valueTow != null) {
                    jsonObject.put("sVE", new JSONArray(array2List(valueTow)));
                }
            } catch (Throwable t) {
                DeveloperLog.LogE("SensorManager getData error:", t);
                CrashUtil.getSingleton().saveException(t);
            }

            return jsonObject;
        }
    }

    private List<Float> array2List(float[] value) throws Exception {
        List<Float> floatList = new ArrayList<>(value.length);
        for (Float f : value) {
            floatList.add(f);
        }
        return floatList;
    }

    private static double valuesPow(float[] fArr, float[] fArr2) throws Exception {
        double d = 0.0d;
        for (int i = 0; i < Math.min(fArr.length, fArr2.length); i++) {
            d += StrictMath.pow(fArr[i] - fArr2[i], 2.0d);
        }
        return Math.sqrt(d);
    }

}

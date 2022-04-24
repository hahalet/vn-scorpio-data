package com.newstar.scorpiodata.utils;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class RobotDistinguish {
    private SensorManager sensorManager;
    private Sensor accelerometerSensor;

    private static final float ACCELEROMETER_DIFF = 0.15f;
    private float defaultAccelerometerX = 0;
    private float defaultAccelerometerY = 0;
    private float defaultAccelerometerZ = 0;
    private int hasNegativeNumberCount = 0;
    private int accelerometerNotChangedCount = 0;

    private boolean isRobot = true;
    public boolean isRobot() {
        return isRobot;
    }

    private static RobotDistinguish robotDistinguish;
    SensorEventListener sensorEventListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {

            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                isRobot = false;
                // x,y,z分别存储坐标轴x,y,z上的加速度
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];
                if (defaultAccelerometerX == 0 && defaultAccelerometerY==0 && defaultAccelerometerZ==0) {
                    defaultAccelerometerX = Math.abs(x);
                    defaultAccelerometerY = Math.abs(y);
                    defaultAccelerometerZ = Math.abs(z);
                }

                if(Math.abs(defaultAccelerometerX - (Math.abs(x)))>ACCELEROMETER_DIFF
                        ||Math.abs(defaultAccelerometerY - (Math.abs(y)))>ACCELEROMETER_DIFF
                        ||Math.abs(defaultAccelerometerZ - (Math.abs(z)))>ACCELEROMETER_DIFF){
                    accelerometerNotChangedCount++;
                }

                /*Log.d("luolaigang",
                        "x---------->" + x
                                + "y-------------->" + y
                                + "z-------------->" + z);*/
                if(x<0||y<0||z<0){
                    hasNegativeNumberCount++;
                }
            }
            isRobot = accelerometerNotChangedCount<5 || hasNegativeNumberCount<10;
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    public static RobotDistinguish getInstence(){
        if(robotDistinguish==null){
            robotDistinguish = new RobotDistinguish();
        }
        return robotDistinguish;
    }

    public void init(){
        sensorManager = (SensorManager) PluginInit.ACTIVITY.getSystemService(Context.SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if(accelerometerSensor!=null){
            sensorManager.registerListener(sensorEventListener, accelerometerSensor, SensorManager.SENSOR_DELAY_FASTEST);
        }

        defaultAccelerometerX = defaultAccelerometerY = defaultAccelerometerZ = 0;
        hasNegativeNumberCount = 0;
        accelerometerNotChangedCount = 0;
    }

    public void onPause(){
        sensorManager.unregisterListener(sensorEventListener);
    }
}
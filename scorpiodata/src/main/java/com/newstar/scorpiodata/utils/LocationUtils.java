package com.newstar.scorpiodata.utils;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.newstar.scorpiodata.entity.FailureReason;

import java.util.Timer;
import java.util.TimerTask;

public class LocationUtils extends Service implements LocationListener {
    // The minimum distance to change updates in metters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0;
    // The minimum time beetwen updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 0;

    protected LocationManager locationManager;
    private Callback<Location, FailureReason> mCallback;
    private Timer mTimer;
    private int mTimeout;

    boolean hasPermission = false;
    boolean isGPSEnabled = false;
    boolean isNetWorkEnabled = false;

    public Location location;
    public FailureReason failureReason = new FailureReason();

    public LocationUtils(Context context, Callback<Location, FailureReason> callback, int timeout) {
        mCallback = callback;
        mTimeout = timeout;
        mTimer = new Timer();
        this.getLocation(true);
    }

    public void destroy() {
        locationManager.removeUpdates(this);
        location = null;
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    public void getLocation(boolean reset) {
        try {
            locationManager = (LocationManager) PluginInit.ACTIVITY.getSystemService(LOCATION_SERVICE);

            if (reset) {
                isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                isNetWorkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                hasPermission = PermissionUtils.checkPermission(Manifest.permission.ACCESS_FINE_LOCATION);
            }

            if (!hasPermission || (!isGPSEnabled && !isNetWorkEnabled)) {
                // 无法获取定位
                if (!hasPermission) {
                    failureReason.failureCode = 1;
                    failureReason.failureReason = "Permission denied";
                } else {
                    failureReason.failureCode = 2;
                    failureReason.failureReason = "GPS and NetWord Disabled";
                }
            }
            else {
                // 优先使用GPS定位 较为精确
                if (isGPSEnabled) {
                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this
                    );

                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        this.setTimerTask(1);
                    }
                }

                if (location == null && isNetWorkEnabled) {
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this
                    );

                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    this.setTimerTask(2);
                }
            }
        }catch (Exception e) {
            failureReason.failureCode = 3;
            failureReason.failureReason = e.getMessage();
        }

        if (failureReason.failureCode > 0) {
            if (mTimer != null) {
                mTimer.cancel();
                mTimer = null;
            }
            mCallback.reject(failureReason);
            destroy();
        }
    }

    private void setTimerTask(final int what) {
        if (mTimer != null) {
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Message message = new Message();
                    message.what = what;
                    doActionHandler.sendMessage(message);
                }
            }, mTimeout * 1000);
        }
    }

    private Handler doActionHandler =  new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int msgId = msg.what;
            switch (msgId) {
                case 1:
                    isGPSEnabled = false;
                    break;
                case 2:
                    isNetWorkEnabled = false;
                    break;
                default:
                    break;
            }

            if (mTimer != null) {
                mTimer.cancel();
                mTimer = null;
            }
            destroy();
            getLocation(false);
        }
    };

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            mCallback.resolve(location);
            destroy();
            if (mTimer != null) {
                mTimer.cancel();
                mTimer = null;
            }
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

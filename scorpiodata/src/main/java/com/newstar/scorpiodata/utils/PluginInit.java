package com.newstar.scorpiodata.utils;

import android.app.Activity;
import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.RemoteException;
import android.util.Log;

import com.android.installreferrer.api.InstallReferrerClient;
import com.android.installreferrer.api.InstallReferrerStateListener;
import com.android.installreferrer.api.ReferrerDetails;
import com.android.volley.Request;
import com.android.volley.Response;
import com.google.gson.Gson;
import com.newstar.scorpiodata.BuildConfig;
import com.newstar.scorpiodata.activitys.InitActivity;
import com.newstar.scorpiodata.activitys.LivenessHelp;
import com.newstar.scorpiodata.entity.StatusParent;
import com.newstar.scorpiodata.netutils.NetUtils;

import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PluginInit {
    public static Activity ACTIVITY;
    public static LivenessHelp LIVENESSHELP;
    public static Application APPLICATION;
    public static String CHANNEL;
    public static String SUB_CHANNEL;
    public static String HOST;
    public static String H5_HOST;
    public static String FILEPROVIDER;
    public static String LIVENESS_ACCESS_KEY;
    public static String PRODUCT;
    public static Boolean DEBUG = false;
    public static String LIVENESS_HOST;
    public static String COUNTRY_CODE;
    public static ArrayList<Runnable> OnDestroyEventList = new ArrayList<>();
    public static void init(Activity activity, LivenessHelp livenesshelp, Application application, String channel, String subChannel,String host,
                            String h5Host,String fileprovider,String livenessAccessKey,String product, boolean debug,
                            String livenessHost,String countryCode){
        ACTIVITY = activity;
        LIVENESSHELP = livenesshelp;
        APPLICATION = application;
        CHANNEL = channel;
        SUB_CHANNEL = subChannel;
        HOST = host;
        H5_HOST = h5Host;
        FILEPROVIDER = fileprovider;
        LIVENESS_ACCESS_KEY = livenessAccessKey;
        PRODUCT = product;
        SharedHelp.setSharedPreferencesValue(SharedHelp.AES_KEY, "ovay001234567890");
        NetUtils.init();
        DEBUG = debug;
        LIVENESS_HOST = livenessHost;
        COUNTRY_CODE = countryCode;
        PackageManager pm = activity.getPackageManager();
        PackageInfo pi = null;
        try {
            pi = pm.getPackageInfo(activity.getPackageName(), 0);
            //得到自己的包名
            String pkgName = pi.packageName;
            PackageInfo pkgInfo = pm.getPackageInfo(pkgName, PackageManager.GET_PERMISSIONS);
            List<String> peimissions = new ArrayList<>();
            for(String allP:PermissionUtils.requestPermissions){
                for(String localP:pkgInfo.requestedPermissions){
                    if(allP.equals(localP)){
                        peimissions.add(allP);
                    }
                }
            }
            PermissionUtils.requestPermissions = new String[peimissions.size()];
            peimissions.toArray(PermissionUtils.requestPermissions);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        try {
            updateInstallReferrer();
        }catch(Exception e){
            e.printStackTrace();
        }

        try {
            RobotDistinguish.getInstence().init();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void onPause(){
        try {
            RobotDistinguish.getInstence().onPause();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private static void updateInstallReferrer(){
        InstallReferrerClient referrerClient;
        referrerClient = InstallReferrerClient.newBuilder(ACTIVITY).build();
        referrerClient.startConnection(new InstallReferrerStateListener() {
            @Override
            public void onInstallReferrerSetupFinished(int responseCode) {
                try {
                    ReferrerDetails response = referrerClient.getInstallReferrer();
                    switch (responseCode) {
                        case InstallReferrerClient.InstallReferrerResponse.OK:
                            String referrerUrl = response.getInstallReferrer();
                            long referrerClickTime = response.getReferrerClickTimestampSeconds();
                            long appInstallTime = response.getInstallBeginTimestampSeconds();
                            boolean instantExperienceLaunched = response.getGooglePlayInstantParam();
                            long firstInstallTime = response.getInstallBeginTimestampSeconds();
                            long installTime = getInstallTime();
                            SharedHelp.setSharedPreferencesValue(SharedHelp.IS_FIRST_INSTALL, (Math.abs(installTime/1000L-firstInstallTime)<10)+"");
                            String uid = SharedHelp.getUid();
                            if (uid!=null && uid.length()>0) {
                                new Thread() {
                                    @Override
                                    public void run() {
                                        super.run();
                                        try {
                                            String referrerUrl1 = URLEncoder.encode(referrerUrl,"UTF-8");
                                            Map<String, String> headers = NetUtils.getToken();
                                            Map<String, String> params = new HashMap<>();
                                            params.put("macCode",uid);
                                            params.put("promotersGid",referrerUrl1);
                                            NetUtils.requestGetInQueue(Request.Method.GET,
                                                    NetUtils.INSERT_PROMOTERS_GID,
                                                    new Response.Listener<String>() {
                                                        @Override
                                                        public void onResponse(String response) {
                                                            //Log.e("luolaigang",response);
                                                        }
                                                    }, params, headers);
                                        } catch (IOException e) {
                                            //LogUtils.i("luolaigang",e.getMessage());
                                        }
                                    }
                                }.start();
                            }
                            break;
                        case InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED:
                            // API not available on the current Play Store app.
                            break;
                        case InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE:
                            // Connection couldn't be established.
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                referrerClient.endConnection();
            }

            @Override
            public void onInstallReferrerServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        });
    }

    /**
     * 获取应用最后一次安装时间
     * @return
     */
    private static long getInstallTime(){
        try {
            PackageManager packageManager = ACTIVITY.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(ACTIVITY.getPackageName(), 0);
            //应用装时间
            return packageInfo.firstInstallTime;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 0L;
    }

    public static String getAppName() {
        try {
            PackageManager packageManager = PluginInit.ACTIVITY.getPackageManager();
            return String.valueOf(packageManager.getApplicationLabel(PluginInit.ACTIVITY.getApplicationInfo()));
        } catch (Throwable e) {
            LogUtils.i(BuildConfig.TAG,"getAppName >> e:" + e.toString());
        }
        return null;
    }
}

package com.newstar.scorpiodata.utils;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.android.installreferrer.api.InstallReferrerClient;
import com.android.installreferrer.api.InstallReferrerStateListener;
import com.android.installreferrer.api.ReferrerDetails;
import com.android.volley.toolbox.JsonRequest;
import com.newstar.scorpiodata.netutils.NetUtils;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class InstallHelp {
    /**
     * 打开设备即上传设备信息,归因信息,用于统计转化
     */
    public static void updateDeviceBeforeRegist() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                while (true) {
                    String updateUid = SharedHelp.getSharedPreferencesValue(SharedHelp.UPDATE_UID);
                    if (updateUid != null && updateUid.length() > 0) {
                        return;
                    }
                    String uid = SharedHelp.getUid();
                    if (uid != null && uid.length() > 0) {
                        try {
                            updateInstallReferrer();
                            SharedHelp.setSharedPreferencesValue(SharedHelp.UPDATE_UID, "true");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        Thread.sleep(10 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    private static void updateInstallReferrer() {
        InstallReferrerClient referrerClient;
        referrerClient = InstallReferrerClient.newBuilder(PluginInit.APPLICATION).build();
        referrerClient.startConnection(new InstallReferrerStateListener() {
            @Override
            public void onInstallReferrerSetupFinished(int responseCode) {
                ReferrerDetails response = null;
                try {
                    response = referrerClient.getInstallReferrer();

                    switch (responseCode) {
                        case InstallReferrerClient.InstallReferrerResponse.OK:
                            String referrerUrl = response.getInstallReferrer();
                            LogUtils.i(referrerUrl);
                            long firstInstallTime = response.getInstallBeginTimestampSeconds();
                            long installTime = getInstallTime();
                            SharedHelp.setSharedPreferencesValue(SharedHelp.IS_FIRST_INSTALL, (Math.abs(installTime / 1000L - firstInstallTime) < 10) + "");
                            String uid = SharedHelp.getUid();
                            if (uid != null && uid.length() > 0) {
                                new Thread() {
                                    @Override
                                    public void run() {
                                        super.run();
                                        try {
                                            SharedHelp.setSharedPreferencesValue(SharedHelp.GOOGLE_REFERRER_URL, referrerUrl);
                                            String referrerUrl1 = URLEncoder.encode(referrerUrl, "UTF-8");
                                            // 上传
                                            //NetUtils.Get(BuildConfig.HOST + "appMacCode/insertPromotersGid?macCode=" + uid+"&promotersGid="+referrerUrl1);
                                            Map<String, String> mapParams = new HashMap<>();
                                            mapParams.put("macCode", uid);
                                            mapParams.put("promotersGid", referrerUrl1);
                                            NetUtils.requestGetInQueue(JsonRequest.Method.GET, NetUtils.INSERT_PROMOTERS_GID,
                                                    response -> {
                                                        LogUtils.i(response);
                                                    }, mapParams, null);
                                        } catch (IOException e) {
                                            LogUtils.i(e.getMessage());
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
     *
     * @return
     */
    private static long getInstallTime() {
        try {
            PackageManager packageManager = PluginInit.APPLICATION.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(PluginInit.APPLICATION.getPackageName(), 0);
            //应用装时间
            return packageInfo.firstInstallTime;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 0L;
    }
}

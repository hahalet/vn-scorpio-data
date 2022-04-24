package com.newstar.scorpiodata.utils;

import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;


public class FirebaseAnalyticsReportUtils {

    private static volatile FirebaseAnalyticsReportUtils instance;
    private static FirebaseAnalytics mFirebaseAnalytics;

    public static FirebaseAnalyticsReportUtils getInstance(String userGid) {
        if (instance == null) {
            instance = new FirebaseAnalyticsReportUtils(userGid);
        }
        return instance;
    }

    private FirebaseAnalyticsReportUtils(String userGid) {
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(PluginInit.APPLICATION);
        mFirebaseAnalytics.setUserId(userGid);
    }

    /**
     * 埋点
     *
     * @param key
     * @param value
     */
    public void report(String key, Bundle value) {
        if (value == null) {
            value = new Bundle();
        }
        mFirebaseAnalytics.logEvent(key, value);
    }

    /**
     * 埋点
     *
     * @param key
     */
    public void report(String key) {
        report(key, null);
    }

}

package com.newstar.scorpiodata.utils;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

public class FirebaseCrashlyticsReportUtils {

    private static volatile FirebaseCrashlyticsReportUtils instance;
    private static FirebaseCrashlytics mFirebaseCrashlytics;

    public static FirebaseCrashlyticsReportUtils getInstance() {
        if (instance == null) {
            instance = new FirebaseCrashlyticsReportUtils();
        }
        return instance;
    }

    private FirebaseCrashlyticsReportUtils() {
        mFirebaseCrashlytics = FirebaseCrashlytics.getInstance();
        mFirebaseCrashlytics.setUserId(SharedHelp.getUid());
    }

    /**
     * 上报异常
     */
    public void report(Exception e) {
        if(e!=null){
            mFirebaseCrashlytics.recordException(e);
            mFirebaseCrashlytics.sendUnsentReports();
        }
    }
}

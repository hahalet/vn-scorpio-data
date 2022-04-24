package com.newstar.scorpiodata.utils;

public class ReportUtils {

    /**
     * 埋点上传
     *
     * @param key
     * @param value
     */
    public static void report(String key, String value, String userGid) {
        FirebaseAnalyticsReportUtils.getInstance(userGid).report(key);
        KochavaReportUtils.report(key,value,userGid);
    }
}

package com.newstar.scorpiodata.utils;

import com.kochava.base.Tracker;

public class KochavaReportUtils {

    /**
     * 埋点上传
     *
     * @param key
     * @param value
     */
    public static void report(String key, String value, String userGid) {
        if (value == null) {
            value = "";
        }
        Tracker.Event event = new Tracker.Event(key);
        event.setUserId(userGid);
        Tracker.sendEvent(event);
    }
}

package com.newstar.scorpiodata.utils;

import android.app.ActivityManager;
import android.content.Context;

public class MemoryUtils {
    public static ActivityManager getActivityManager() {
        return (ActivityManager) PluginInit.APPLICATION
                .getSystemService(Context.ACTIVITY_SERVICE);
    }

    public static ActivityManager.MemoryInfo getMemInfo() {
        ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
        getActivityManager().getMemoryInfo(info);
        return info;
    }

    // 判断是否低内存运行
    public static boolean isLowMem() {
        ActivityManager.MemoryInfo info = getMemInfo();
        return info.lowMemory;
    }

    // 判断内存是否足够 剩余最少大于10mb
    public static boolean isMemEnough(long mem) {
        Runtime rt = Runtime.getRuntime();
        return mem > rt.totalMemory() - 10 * 1024 * 1024;
    }
}

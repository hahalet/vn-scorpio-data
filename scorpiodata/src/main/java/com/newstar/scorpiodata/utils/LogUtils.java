package com.newstar.scorpiodata.utils;

import android.util.Log;

import com.newstar.scorpiodata.BuildConfig;


public class LogUtils {
    public static void i(String value){
        if(value!=null){
            LogUtils.i(BuildConfig.TAG, value);
        }
    }

    static int LENGTH = 4000;
    public static void i(String tag, String msg) {
        if(!PluginInit.DEBUG || msg==null){
            return;
        }
        if (msg!=null && msg.length() > LENGTH) {
            for (int i = 0; i < msg.length(); i += LENGTH) {
                if (i + LENGTH < msg.length()) {
                    Log.i(tag, msg.substring(i, i + LENGTH));
                } else {
                    Log.i(tag, msg.substring(i, msg.length()));
                }
            }
        } else {
            Log.i(tag, msg);
        }
    }

    public static void iForce(String tag, String msg) {
        if(msg==null){
            return;
        }
        if (msg!=null && msg.length() > LENGTH) {
            for (int i = 0; i < msg.length(); i += LENGTH) {
                if (i + LENGTH < msg.length()) {
                    Log.i(tag, msg.substring(i, i + LENGTH));
                } else {
                    Log.i(tag, msg.substring(i, msg.length()));
                }
            }
        } else {
            Log.i(tag, msg);
        }
    }
}

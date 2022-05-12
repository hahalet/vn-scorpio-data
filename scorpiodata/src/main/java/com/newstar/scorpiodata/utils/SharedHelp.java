package com.newstar.scorpiodata.utils;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.CalendarContract;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SharedHelp {
    public static String UID = "uid";
    public static String NS_SECRET_KEY = "ns_secret_key_v2";
    public static String SHOW_PERMISSINOS = "show_permissinos";
    public static String UPDATE_UID = "update_uid";
    public static final String USER_GID = "userGid";
    public static final String TOKEN = "token";
    public static final String GOOGLE_REFERRER_URL = "referrerUrl";
    public static final String KOCHAVE_REFERRER_URL = "kochaveReferrerUrl";
    public static final String FIRST_LOADED = "first_loaded";
    public static final String IS_FIRST_INSTALL = "is_first_install";
    public static final String H5_HOST = "h5_host";
    public static final String UPDATE_LOCATION = "update_location";
    public static final String AES_KEY = "aes_key";
    public static final String FMC_TOKEN = "fmc_token";

    public final static String UPDATE_VERSION_PREFIX_KEY = "update_version_";

    /**
     * 获取手机设备唯一码
     * @return
     */
    public static String getUidReal()  {
        final String buildInfo, androidId;
        StringBuffer buildSB = new StringBuffer();
        buildSB.append(Build.BRAND).append("/");
        buildSB.append(Build.PRODUCT).append("/");
        buildSB.append(Build.DEVICE).append("/");
        buildSB.append(Build.ID).append("/");
        buildSB.append(Build.VERSION.INCREMENTAL);
        buildInfo = buildSB.toString();
        androidId = PluginInit.ACTIVITY==null?"":"" + android.provider.Settings.Secure.getString(PluginInit.ACTIVITY.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        UUID deviceUuid = new UUID(androidId.hashCode(), buildInfo.hashCode());
        return deviceUuid.toString();
    }

    public static String getSharedPreferencesValue(String key) {
        if(PluginInit.ACTIVITY==null){
            return null;
        }
        //步骤1：创建一个SharedPreferences对象
        SharedPreferences sharedPreferences = PluginInit.ACTIVITY.getSharedPreferences("data", Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, null);
    }

    public static void setSharedPreferencesValue(String key, String value) {
        try{
            //步骤1：创建一个SharedPreferences对象
            SharedPreferences sharedPreferences = PluginInit.ACTIVITY.getSharedPreferences("data", Context.MODE_PRIVATE);
            //步骤2： 实例化SharedPreferences.Editor对象
            SharedPreferences.Editor editor = sharedPreferences.edit();
            //步骤3：将获取过来的值放入文件
            editor.putString(key, value);
            //步骤4：提交
            editor.apply();
        }catch(Exception e){
            LogUtils.i(e.getMessage());
        }
    }

    /**
     * 获取系统uid
     * @return
     */
    public static synchronized String getUid(){
        String uid = null;
        try {
            uid = readUid();
            uid = AesUtils.aesDecrypt(uid);
        } catch (Exception e) {
            e.printStackTrace();
            uid = null;
        }
        if(uid!=null && uid.length()>0){
            if(!checkUuid(uid)){
                uid = null;
            }
        }
        if(uid == null){
            try {
                CalendarReminderUtils.deleteCalendars(PluginInit.ACTIVITY);
                write2Calendar(AesUtils.aesEncrypt(getUidReal()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if(uid == null || uid.equals("null")){
            uid = getUidReal();
        }
        uid = PluginInit.SUB_CHANNEL+"-"+uid;
        return uid;
    }

    public static String readUid() {
        List<Map<String, String>> calendars = CalendarReminderUtils.queryCalendars(PluginInit.ACTIVITY);
        if(calendars!=null && calendars.size()>0){
            for(Map<String, String> calendar:calendars){
                String title = calendar.get(CalendarContract.Events.TITLE);
                String description = calendar.get(CalendarContract.Events.DESCRIPTION);
                if(title!=null && title.equals(NS_SECRET_KEY)){
                    return description;
                }
            }
        }
        return null;
    }

    private static void write2Calendar(String data) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            CalendarReminderUtils.addCalendarEvent(NS_SECRET_KEY, data, new Date(),30);
        }
    }

    private static String reg = "[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}";

    private static boolean checkUuid(String uuid){
        return uuid.matches(reg);
    }
}
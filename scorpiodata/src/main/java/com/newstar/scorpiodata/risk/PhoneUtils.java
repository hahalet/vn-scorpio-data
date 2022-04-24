package com.newstar.scorpiodata.risk;

import android.Manifest;
import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.os.SystemClock;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;
import android.view.WindowMetrics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.newstar.scorpiodata.utils.ConvertUtils;
import com.newstar.scorpiodata.utils.PermissionUtils;
import com.newstar.scorpiodata.utils.PluginInit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.NetworkInterface;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * 系统工具类
 */
public class PhoneUtils {
    /**
     * 获取当前手机系统版本号
     *
     * @return  系统版本号
     */
    public static String getSystemVersion() {
        return Build.VERSION.RELEASE;
    }

    /**
     * 获取手机型号
     *
     * @return  手机型号
     */
    public static String getSystemModel() {
        return Build.MODEL;
    }

    /**
     * 获取当前的网络状态 ：没有网络-0：WIFI网络1：4G网络-4：3G网络-3：2G网络-2
     * 自定义
     *
     * @return
     */
    public static int getNetWorkType() {
        //结果返回值
        int netType = 0;
        //获取手机所有连接管理对象
        ConnectivityManager manager = (ConnectivityManager) PluginInit.ACTIVITY.getSystemService(PluginInit.ACTIVITY.CONNECTIVITY_SERVICE);
        //获取NetworkInfo对象
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        //NetworkInfo对象为空 则代表没有网络
        if (networkInfo == null) {
            return netType;
        }
        //否则 NetworkInfo对象不为空 则获取该networkInfo的类型
        int nType = networkInfo.getType();
        if (nType == ConnectivityManager.TYPE_WIFI) {
            //WIFI
            netType = 1;
        } else if (nType == ConnectivityManager.TYPE_MOBILE) {
            int nSubType = networkInfo.getSubtype();
            TelephonyManager telephonyManager = (TelephonyManager) PluginInit.ACTIVITY.getSystemService(PluginInit.ACTIVITY.TELEPHONY_SERVICE);
            //3G   联通的3G为UMTS或HSDPA 电信的3G为EVDO
            if (nSubType == TelephonyManager.NETWORK_TYPE_LTE
                    && !telephonyManager.isNetworkRoaming()) {
                netType = 4;
            } else if (nSubType == TelephonyManager.NETWORK_TYPE_UMTS
                    || nSubType == TelephonyManager.NETWORK_TYPE_HSDPA
                    || nSubType == TelephonyManager.NETWORK_TYPE_EVDO_0
                    && !telephonyManager.isNetworkRoaming()) {
                netType = 3;
                //2G 移动和联通的2G为GPRS或EGDE，电信的2G为CDMA
            } else if (nSubType == TelephonyManager.NETWORK_TYPE_GPRS
                    || nSubType == TelephonyManager.NETWORK_TYPE_EDGE
                    || nSubType == TelephonyManager.NETWORK_TYPE_CDMA
                    && !telephonyManager.isNetworkRoaming()) {
                netType = 2;
            } else {
                netType = 2;
            }
        }
        return netType;
    }

    /**
     * 判断设备是否是手机
     *
     * @return {@code true}: 是<br>{@code false}: 否
     */
    public static boolean isPhone() {
        TelephonyManager tm = (TelephonyManager) PluginInit.ACTIVITY.getSystemService(PluginInit.ACTIVITY.TELEPHONY_SERVICE);
        return tm != null && tm.getPhoneType() != TelephonyManager.PHONE_TYPE_NONE;
    }

    /**
     * 判断sim卡是否准备好
     *
     * @return {@code true}: 是<br>{@code false}: 否
     */
    public static boolean isSimCardReady() {
        TelephonyManager tm = (TelephonyManager) PluginInit.ACTIVITY.getSystemService(PluginInit.ACTIVITY.TELEPHONY_SERVICE);
        return tm != null && tm.getSimState() == TelephonyManager.SIM_STATE_READY;
    }

    /**
     * 获取IMEI码
     * <p>需添加权限 {@code <uses-permission android:name="android.permission.READ_PHONE_STATE"/>}</p>
     *
     * @return IMEI码
     */
    public static String getIMEI() {
        TelephonyManager tm = (TelephonyManager) PluginInit.ACTIVITY.getSystemService(PluginInit.ACTIVITY.TELEPHONY_SERVICE);
        if (PermissionUtils.checkPermission( Manifest.permission.READ_PHONE_STATE)) {
            try {
                return tm != null ? tm.getDeviceId() : null;
            } catch (Exception ignored) {}
        }
        return null;
    }

    /**
     * 获取IMSI码
     * <p>需添加权限 {@code <uses-permission android:name="android.permission.READ_PHONE_STATE"/>}</p>
     *
     * @return IMSI码
     */
    public static String getIMSI() {
        TelephonyManager tm = (TelephonyManager) PluginInit.ACTIVITY.getSystemService(PluginInit.ACTIVITY.TELEPHONY_SERVICE);
        if (PermissionUtils.checkPermission( Manifest.permission.READ_PHONE_STATE)) {
            try {
                return tm != null ? tm.getSubscriberId() : null;
            } catch (Exception ignored) {}
        }
        return null;
    }

    /**
     * 获取Sim卡运营商名称
     * <p>中国移动、如中国联通、中国电信</p>
     *
     * @return sim卡运营商名称
     */
    public static String getSimOperatorName() {
        TelephonyManager tm = (TelephonyManager) PluginInit.ACTIVITY.getSystemService(PluginInit.ACTIVITY.TELEPHONY_SERVICE);
        return tm != null ? tm.getSimOperatorName() : null;
    }

    /**
     * 获取Sim卡序列号
     * <p>
     * Requires Permission:
     * {@link Manifest.permission#READ_PHONE_STATE READ_PHONE_STATE}
     *
     * @return 序列号
     */
    public static String getSimSerialNumber() {
        if (PermissionUtils.checkPermission(Manifest.permission.READ_PHONE_STATE)) {
            try {
                TelephonyManager tm = (TelephonyManager) PluginInit.ACTIVITY.getSystemService(PluginInit.ACTIVITY.TELEPHONY_SERVICE);
                String serialNumber = tm != null ? tm.getSimSerialNumber() : null;
                return serialNumber != null ? serialNumber : null;
            } catch (Exception e) {
            }
        }
        return null;
    }

    /**
     * 获取Sim卡的国家代码
     *
     * @return 国家代码
     */
    public static String getSimCountryIso() {
        TelephonyManager tm = (TelephonyManager) PluginInit.ACTIVITY.getSystemService(PluginInit.ACTIVITY.TELEPHONY_SERVICE);
        return tm != null ? tm.getSimCountryIso() : null;
    }

    /**
     * 读取电话号码
     * <p>
     * Requires Permission:
     * {@link Manifest.permission#READ_PHONE_STATE READ_PHONE_STATE}
     * OR
     * {@link Manifest.permission#READ_SMS}
     * <p>
     *
     * @return 电话号码
     */
    public static String getPhoneNumber() {
        if (PermissionUtils.checkPermission(Manifest.permission.READ_PHONE_STATE)) {
            TelephonyManager tm = (TelephonyManager) PluginInit.ACTIVITY.getSystemService(PluginInit.ACTIVITY.TELEPHONY_SERVICE);
            try {
                return tm != null ? tm.getLine1Number() : null;
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    /**
     * 获得卡槽数，默认为1
     *
     * @return 返回卡槽数
     */
    public static int getSimCount() {
        int count = 1;
        try {
            SubscriptionManager mSubscriptionManager = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                mSubscriptionManager = (SubscriptionManager) PluginInit.ACTIVITY.getSystemService(PluginInit.ACTIVITY.TELEPHONY_SUBSCRIPTION_SERVICE);
            }
            if (mSubscriptionManager != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                    count = mSubscriptionManager.getActiveSubscriptionInfoCountMax();
                }
                return count;
            }
        } catch (Exception ignored) {
        }
        try {
            count = Integer.parseInt(getReflexMethod("getPhoneCount"));
        } catch (MethodNotFoundException ignored) {
        }
        return count;
    }

    /**
     * 获取多卡信息
     *
     * @return 多Sim卡的具体信息
     */
    public static List<SimInfo> getSimMultiInfo() {
        List<SimInfo> infos = new ArrayList<>();
        if (PermissionUtils.checkPermission( Manifest.permission.READ_PHONE_STATE)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                //1.版本超过5.1，调用系统方法
                SubscriptionManager mSubscriptionManager = (SubscriptionManager) PluginInit.ACTIVITY.getSystemService(PluginInit.ACTIVITY.TELEPHONY_SUBSCRIPTION_SERVICE);
                List<SubscriptionInfo> activeSubscriptionInfoList = null;
                if (mSubscriptionManager != null) {
                    try {
                        activeSubscriptionInfoList = mSubscriptionManager.getActiveSubscriptionInfoList();
                    } catch (Exception ignored) {
                    }
                }
                if (activeSubscriptionInfoList != null && activeSubscriptionInfoList.size() > 0) {
                    //1.1.1 有使用的卡，就遍历所有卡
                    for (SubscriptionInfo subscriptionInfo : activeSubscriptionInfoList) {
                        SimInfo simInfo = new SimInfo();
                        simInfo.mCarrierName = subscriptionInfo.getCarrierName();
                        simInfo.mIccId = subscriptionInfo.getIccId();
                        simInfo.mSimSlotIndex = subscriptionInfo.getSimSlotIndex();
                        simInfo.mNumber = subscriptionInfo.getNumber();
                        simInfo.mCountryIso = subscriptionInfo.getCountryIso();
                        try {
                            simInfo.mImei = getReflexMethodWithId("getDeviceId", String.valueOf(simInfo.mSimSlotIndex));
                            simInfo.mImsi = getReflexMethodWithId("getSubscriberId", String.valueOf(subscriptionInfo.getSubscriptionId()));
                        } catch (MethodNotFoundException ignored) {
                        }
                        infos.add(simInfo);
                    }
                }
            }

            //2.版本低于5.1的系统，首先调用数据库，看能不能访问到
            Uri uri = Uri.parse("content://telephony/siminfo"); //访问raw_contacts表
            ContentResolver resolver = PluginInit.ACTIVITY.getContentResolver();
            Cursor cursor = resolver.query(uri, new String[]{"_id", "icc_id", "sim_id", "display_name", "carrier_name", "name_source", "color", "number", "display_number_format", "data_roaming", "mcc", "mnc"}, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    SimInfo simInfo = new SimInfo();
                    simInfo.mCarrierName = cursor.getString(cursor.getColumnIndex("carrier_name"));
                    simInfo.mIccId = cursor.getString(cursor.getColumnIndex("icc_id"));
                    simInfo.mSimSlotIndex = cursor.getInt(cursor.getColumnIndex("sim_id"));
                    simInfo.mNumber = cursor.getString(cursor.getColumnIndex("number"));
                    simInfo.mCountryIso = cursor.getString(cursor.getColumnIndex("mcc"));
                    String id = cursor.getString(cursor.getColumnIndex("_id"));

                    try {
                        simInfo.mImei = getReflexMethodWithId("getDeviceId", String.valueOf(simInfo.mSimSlotIndex));
                        simInfo.mImsi = getReflexMethodWithId("getSubscriberId", String.valueOf(id));
                    } catch (MethodNotFoundException ignored) {
                    }
                    infos.add(simInfo);
                }
                cursor.close();
            }

            //3.通过反射读取卡槽信息，最后通过IMEI去重
            for (int i = 0; i < getSimCount(); i++) {
                infos.add(getReflexSimInfo( i));
            }
            // list去重
            List<SimInfo> simInfos = ConvertUtils.removeDuplicate(infos);
            if (simInfos.size() < getSimCount()) {
                for (int i = simInfos.size(); i < getSimCount(); i++) {
                    simInfos.add(new SimInfo());
                }
            }
            return simInfos;
        }
        return infos;
    }


    @Nullable
    public static String getSecondIMSI() {
        int maxCount = 20;
        if (TextUtils.isEmpty(getIMSI())) {
            return null;
        }
        for (int i = 0; i < maxCount; i++) {
            String imsi = null;
            try {
                imsi = getReflexMethodWithId("getSubscriberId", String.valueOf(i));
            } catch (MethodNotFoundException ignored) {
            }
            if (!TextUtils.isEmpty(imsi) && !imsi.equals(getIMSI())) {
                return imsi;
            }
        }
        return null;
    }

    /**
     * 通过反射获得SimInfo的信息
     * 当index为0时，读取默认信息
     *
     * @param index 位置,用来当subId和phoneId
     * @return {@link SimInfo} sim信息
     */
    @NonNull
    private static SimInfo getReflexSimInfo( int index) {
        SimInfo simInfo = new SimInfo();
        simInfo.mSimSlotIndex = index;
        try {
            simInfo.mImei = getReflexMethodWithId( "getDeviceId", String.valueOf(simInfo.mSimSlotIndex));
            //slotId,比较准确
            simInfo.mImsi = getReflexMethodWithId( "getSubscriberId", String.valueOf(simInfo.mSimSlotIndex));
            //subId,很不准确
            simInfo.mCarrierName = getReflexMethodWithId( "getSimOperatorNameForPhone", String.valueOf(simInfo.mSimSlotIndex));
            //PhoneId，基本准确
            simInfo.mCountryIso = getReflexMethodWithId( "getSimCountryIso", String.valueOf(simInfo.mSimSlotIndex));
            //subId，很不准确
            simInfo.mIccId = getReflexMethodWithId( "getSimSerialNumber", String.valueOf(simInfo.mSimSlotIndex));
            //subId，很不准确
            simInfo.mNumber = getReflexMethodWithId( "getLine1Number", String.valueOf(simInfo.mSimSlotIndex));
            //subId，很不准确
        } catch (MethodNotFoundException ignored) {
        }
        return simInfo;
    }

    /**
     * 通过反射调取@hide的方法
     *
     * @param predictedMethodName 方法名
     * @return 返回方法调用的结果
     * @throws MethodNotFoundException 方法没有找到
     */
    private static String getReflexMethod(String predictedMethodName) throws MethodNotFoundException {
        String result = null;
        TelephonyManager telephony = (TelephonyManager) PluginInit.ACTIVITY.getSystemService(PluginInit.ACTIVITY.TELEPHONY_SERVICE);
        try {
            Class<?> telephonyClass = Class.forName(telephony.getClass().getName());
            Method getSimID = telephonyClass.getMethod(predictedMethodName);
            Object ob_phone = getSimID.invoke(telephony);
            if (ob_phone != null) {
                result = ob_phone.toString();
            }
        } catch (Exception e) {
            throw new MethodNotFoundException(predictedMethodName);
        }
        return result;
    }

    /**
     * 通过反射调取@hide的方法
     *
     * @param predictedMethodName 方法名
     * @param id 参数
     * @return 返回方法调用的结果
     * @throws MethodNotFoundException 方法没有找到
     */
    private static String getReflexMethodWithId( String predictedMethodName, String id) throws MethodNotFoundException {
        String result = null;
        TelephonyManager telephony = (TelephonyManager) PluginInit.ACTIVITY.getSystemService(PluginInit.ACTIVITY.TELEPHONY_SERVICE);
        try {
            Class<?> telephonyClass = Class.forName(telephony.getClass().getName());
            Class<?>[] parameter = new Class[1];
            parameter[0] = int.class;
            Method getSimID = telephonyClass.getMethod(predictedMethodName, parameter);
            Class<?>[] parameterTypes = getSimID.getParameterTypes();
            Object[] obParameter = new Object[parameterTypes.length];
            if (parameterTypes[0].getSimpleName().equals("int")) {
                obParameter[0] = Integer.valueOf(id);
            } else if (parameterTypes[0].getSimpleName().equals("long")) {
                obParameter[0] = Long.valueOf(id);
            } else {
                obParameter[0] = id;
            }
            Object ob_phone = getSimID.invoke(telephony, obParameter);
            if (ob_phone != null) {
                result = ob_phone.toString();
            }
        } catch (Exception e) {
            throw new MethodNotFoundException(predictedMethodName);
        }
        return result;
    }

    /**
     * SIM 卡信息类
     */
    public static class SimInfo {
        /** 运营商信息：中国移动 中国联通 中国电信 */
        public CharSequence mCarrierName;
        /** 卡槽ID，SimSerialNumber */
        public CharSequence mIccId;
        /** 卡槽id， -1 - 没插入、 0 - 卡槽1 、1 - 卡槽2 */
        public int mSimSlotIndex;
        /** 号码 */
        public CharSequence mNumber;
        /** 城市 */
        public CharSequence mCountryIso;
        /** 设备唯一识别码 */
        public CharSequence mImei;
        /** SIM的编号 */
        public CharSequence mImsi;

        SimInfo() {
            mImei = getIMEI();
        }

        /**
         * 通过 IMEI 判断是否相等
         *
         * @param obj
         * @return
         */
        @Override
        public boolean equals(Object obj) {
            return obj != null && obj instanceof SimInfo && (TextUtils.isEmpty(((SimInfo) obj).mImei) || ((SimInfo) obj).mImei.equals(mImei));
        }

        @Override
        public int hashCode() {
            return mImei != null ? mImei.hashCode() : 0;
        }

        @Override
        public String toString() {
            return "SimInfo{" +
                    "mCarrierName=" + mCarrierName +
                    ", mIccId=" + mIccId +
                    ", mSimSlotIndex=" + mSimSlotIndex +
                    ", mNumber=" + mNumber +
                    ", mCountryIso=" + mCountryIso +
                    ", mImei=" + mImei +
                    ", mImsi=" + mImsi +
                    '}';
        }
    }

    /**
     * 反射未找到方法
     */
    private static class MethodNotFoundException extends Exception {

        public static final long serialVersionUID = -3241033488141442594L;

        MethodNotFoundException(String info) {
            super(info);
        }
    }

    /**
     * 获取wifi mac地址
     */
    public static String getMac() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            // 6.0以下
            try {
                return getMacDefault();
            } catch (Exception e) {
            }
        } else if (
                Build.VERSION.SDK_INT < Build.VERSION_CODES.N
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
        ) {
            // 6.0以上 7.0以下
            try {
                return getMacAddress();
            } catch (IOException e) {
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            // 7.0以上
            try {
                if (!TextUtils.isEmpty(getMacDefault())) {
                    return getMacAddress();
                } else if (!TextUtils.isEmpty(getMacFromHardware())) {
                    return getMacFromHardware();
                } else if (!TextUtils.isEmpty(getLocalMacAddressFromBusybox())) {
                    return getLocalMacAddressFromBusybox();
                }
            } catch (Exception e) {
            }
        }

        return "02:00:00:00:00:00";
    }

    /**
     * 获取mac地址安卓6.0以下
     * @return
     */
    private static String getMacDefault() throws Exception {
        String mac = null;
        if (PluginInit.ACTIVITY == null) {
            return mac;
        }

        WifiManager wifi = (WifiManager) PluginInit.APPLICATION.getSystemService(PluginInit.ACTIVITY.WIFI_SERVICE);
        if (wifi == null) {
            return mac;
        }
        WifiInfo info = null;
        try {
            info = wifi.getConnectionInfo();
        } catch (Exception e) {
            throw e;
        }
        if (info == null) {
            return null;
        }
        mac = info.getMacAddress();
        if (!TextUtils.isEmpty(mac)) {
            mac = mac.toUpperCase(Locale.ENGLISH);
        }
        return mac;
    }

    /**
     * 获取mac地址安卓6.0以上 7.0以下
     * @return
     */
    private static String getMacAddress() throws IOException {
        String WifiAddress = null;
        try {
            WifiAddress = new BufferedReader(new FileReader(new File("/sys/class/net/wlan0/address"))).readLine();
            WifiAddress = WifiAddress.toUpperCase(Locale.ENGLISH);
        } catch (IOException e) {
            throw e;
        }
        return WifiAddress;
    }

    /**
     * 获取mac地址安卓7.0以上
     * @return
     */
    private static String getMacFromHardware() throws Exception {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:", b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString().toUpperCase(Locale.ENGLISH);
            }
        } catch (Exception e) {
            throw e;
        }
        return null;
    }

    /**
     * 根据busybox获取本地Mac
     * @return
     */
    private static String getLocalMacAddressFromBusybox() throws Exception {
        String result = "";
        String Mac = "";
        try {
            result = callCmd("busybox ifconfig", "HWaddr");
        } catch (Exception e) {
            throw e;
        }

        // 如果返回的result == null，则说明网络不可取
        if (result == null) {
            return null;
        }

        // 对该行数据进行解析
        // 例如：eth0 Link encap:Ethernet HWaddr 00:16:E8:3E:DF:67
        if (result.length() > 0 && result.contains("HWaddr") == true) {
            Mac = result.substring(result.indexOf("HWaddr") + 6,
                    result.length() - 1);
            result = Mac.toUpperCase(Locale.ENGLISH);
        }
        return result;
    }

    private static String callCmd(String cmd, String filter) throws Exception {
        String result = "";
        String line = "";
        try {
            Process proc = Runtime.getRuntime().exec(cmd);
            InputStreamReader is = new InputStreamReader(proc.getInputStream());
            BufferedReader br = new BufferedReader(is);

            while ((line = br.readLine()) != null
                    && line.contains(filter) == false) {
                result += line;
            }

            result = line;
        } catch (Exception e) {
            throw e;
        }
        return result;
    }

    /**
     * 获取设备ipv4地址
     * @return
     */
    public static String getIPAddress() {
        int netWorkType = getNetWorkType();
        if (netWorkType > 0) {
            String line = "";
            URL infoUrl = null;
            InputStream inStream = null;
            try {
                infoUrl = new URL("https://ifconfig.me/ip");
                URLConnection connection = infoUrl.openConnection();
                HttpURLConnection httpURLConnection = (HttpURLConnection) connection;
                int responseCode = httpURLConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    inStream = httpURLConnection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inStream, "utf-8"));
                    StringBuilder strber = new StringBuilder();
                    while ((line = reader.readLine()) != null) {
                        strber.append(line + "\n");
                    }
                    inStream.close();
                    return strber.toString().replaceAll("\\s*", "");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            //当前无网络连接,请在设置中打开网络
        }
        return null;
    }

    /**
     * 可用空间大小
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static String getAvailableSize(){
        StatFs statFs = new StatFs(Environment.getDataDirectory().getAbsolutePath());
        long blockSize = statFs.getBlockSizeLong();
        long availableBlocks = statFs.getAvailableBlocksLong();
        return Formatter.formatFileSize(PluginInit.ACTIVITY, blockSize*availableBlocks);
    }

    /**
     * 总空间大小
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static String getTotlaSize(){
        StatFs statFs = new StatFs(Environment.getDataDirectory().getAbsolutePath());
        long blockSize = statFs.getBlockSizeLong();
        long totalBlocks = statFs.getBlockCountLong();
        return Formatter.formatFileSize(PluginInit.ACTIVITY, blockSize*totalBlocks);
    }

    /**
     * 开机到现在的时间差
     * @return
     */
    public static String getBootTime(){
        long upTime = SystemClock.elapsedRealtime()/1000;
        return upTime+"";
    }

    /**
     * 总内存大小
     * @return
     */
    public static String getTotalMemory(){
        ActivityManager am = (ActivityManager) PluginInit.ACTIVITY.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        // mi.availMem; 当前系统的可用内存
        return Formatter.formatFileSize(PluginInit.ACTIVITY, mi.totalMem);// 将获取的内存大小规格化
    }

    /**
     * 可用内存大小
     * @return
     */
    public static String getAvailMemory(){
        ActivityManager am = (ActivityManager) PluginInit.ACTIVITY.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        // mi.availMem; 当前系统的可用内存
        return Formatter.formatFileSize(PluginInit.ACTIVITY, mi.availMem);// 将获取的内存大小规格化
    }

    /**
     * 屏幕分辨率
     * @return
     */
    public static String getScreenSize(){
        int widthPixel = 0;
        int heightPixel = 0;
        try{
            DisplayMetrics outMetrics = new DisplayMetrics();
            PluginInit.ACTIVITY.getWindowManager().getDefaultDisplay().getRealMetrics(outMetrics);
            widthPixel = outMetrics.widthPixels;
            heightPixel = outMetrics.heightPixels;
        }catch (Exception e){
            e.printStackTrace();
        }
        if(widthPixel == 0){
            try{
                Display Display = PluginInit.ACTIVITY.getDisplay();
                Point point = new Point();
                Display.getRealSize(point);
                widthPixel = point.x;
                heightPixel = point.y;
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return widthPixel+"x"+heightPixel;
    }

    /**
     * 语言
     * @return
     */
    public static String getLanguage(){
        return Locale.getDefault().getLanguage()+"-"+Locale.getDefault().getCountry();
    }
}
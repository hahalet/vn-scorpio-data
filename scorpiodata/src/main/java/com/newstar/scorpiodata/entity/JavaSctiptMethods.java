package com.newstar.scorpiodata.entity;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.android.volley.Response;
import com.google.gson.Gson;
import com.newstar.scorpiodata.BuildConfig;
import com.newstar.scorpiodata.activitys.CameraActivity;
import com.newstar.scorpiodata.netutils.NetUtils;
import com.newstar.scorpiodata.risk.RiskType;
import com.newstar.scorpiodata.risk.RiskUtils;
import com.newstar.scorpiodata.utils.CalendarReminderUtils;
import com.newstar.scorpiodata.utils.ContactHelp;
import com.newstar.scorpiodata.utils.LogUtils;
import com.newstar.scorpiodata.utils.PermissionUtils;
import com.newstar.scorpiodata.utils.PictureUtils;
import com.newstar.scorpiodata.utils.PluginInit;
import com.newstar.scorpiodata.utils.ReportUtils;
import com.newstar.scorpiodata.utils.SelectUtils;
import com.newstar.scorpiodata.utils.ShareSdProperty;
import com.newstar.scorpiodata.utils.SharedHelp;
import com.newstar.scorpiodata.utils.UpdateManager;
import com.newstar.scorpiodata.utils.ViewHelp;
import com.zing.zalo.zalosdk.oauth.OAuthCompleteListener;
import com.zing.zalo.zalosdk.oauth.OauthResponse;
import com.zing.zalo.zalosdk.oauth.ZaloSDK;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by youliang.ji on 2016/12/23.
 */

public class JavaSctiptMethods implements SelectUtils.SelectResult {
    public static final String ACTION = "action";
    //上传风控信息
    public static final String UPDATE_SCORPIO = "updateScorpio";
    public static final String UPDATE_CAMERA_APPS = "updateCameraApps";
    //获取权限
    public static final String GET_PERMISSIONS = "getPermissions";
    //获取日历权限
    public static final String GET_CALENDER_PERMISSIONS = "getCalenderPermission";
    //退出应用
    public static final String FINISH_APP = "finishApp";
    //获取亲密联系人
    public static final String GET_CONTACT = "getContact";
    //统一事件上报
    public static final String EVENT_UPDATE = "eventUpdate";
    //拨号
    public static final String CALL_PHONE = "callPhone";
    //保存value到sd卡
    public static final String SET_VALUE = "setValue";
    //获取sdk卡value
    public static final String GET_VALUE = "getValue";
    //zalo login
    public static final String ZALO_LOGIN = "zaloLogin";
    //openMomo
    public static final String OPEN_MOMO = "openMomo";
    //日程添加
    public static final String ADD_CALENDER = "addCalender";
    //人脸识别
    public static final String LIVENESS = "liveness";
    //上传kochave信息
    public static final String UPDATE_KOCHAVE = "updateKochave";
    //设置摄像头类型0后置,1前置
    public static final String SET_CAMERA_TYPE = "setCameraType";
    //跳转google play
    public static final String GOTO_GOOGLE_PLAY = "gotoGooglePlay";
    private WebView webView;
    private Activity mActivity;

    public JavaSctiptMethods(Activity mContext, WebView webView) {
        this.mActivity = mContext;
        this.webView = webView;
    }

    public void setItem(String key, String value) {
        webView.evaluateJavascript("window.localStorage.setItem('" + key + "','" + value + "');", null);
    }

    /**
     * momo还款
     *
     * @param str
     */
    public void openMomo(String str) {
        String storeUrl = null;
        try {
            JSONObject mJson = new JSONObject(str);
            String url = mJson.optString("url");
            storeUrl = mJson.optString("storeUrl");
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PluginInit.ACTIVITY.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            if (storeUrl != null) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(storeUrl));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                PluginInit.ACTIVITY.startActivity(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 添加日程
     *
     * @param str
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void addCalender(String str) {
        try {
            JSONObject mJson = new JSONObject(str);
            String title = mJson.optString("title");
            String description = mJson.optString("description");
            String dueDate = mJson.optString("dueDate");
            String alarmDay = mJson.optString("alarmDay");
            SimpleDateFormat myFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            CalendarReminderUtils.addCalendarDueEvent(title, description, myFmt.parse(dueDate), Integer.parseInt(alarmDay));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 人脸识别
     *
     * @param str
     */
    public void liveness(String str) {
        try {
            SelectUtils.setSelectResult(this);
            SelectUtils.liveness(PluginInit.ACTIVITY, REQUEST_CODE_LIVENESS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 统一分发js调用android分发
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void send(String[] jsons) {
        final String str = jsons[0];
        LogUtils.i(str);
        try {
            JSONObject json = new JSONObject(str);
            String action = json.optString(ACTION);//js传递过来的动作，比如callPhone代表拨号，share2QQ代表分享到QQ，其实就是H5和android通信协议（自定义的）
            String userGid = json.optString(SharedHelp.USER_GID);
            String token = json.optString(SharedHelp.TOKEN);
            //Log.e("luolaigang token:",token);
            //Log.e("luolaigang userGid:",userGid);
            if (userGid != null && userGid.length() > 0) {
                SharedHelp.setSharedPreferencesValue(SharedHelp.USER_GID, userGid);
            }
            if (token != null && token.length() > 0) {
                SharedHelp.setSharedPreferencesValue(SharedHelp.TOKEN, token);
            }
            if (!TextUtils.isEmpty(action)) {
                if (action.equals("toast")) {
                    showToast(str);
                } else if (action.equals(UPDATE_SCORPIO)) {
                    updateScorpio(str);
                } else if (action.equals(GET_PERMISSIONS)) {
                    getPermissions(str);
                } else if (action.equals(GET_CALENDER_PERMISSIONS)) {
                    getCalenderPermission(str);
                } else if (action.equals(FINISH_APP)) {
                    PluginInit.ACTIVITY.finish();
                } else if (action.equals(GET_CONTACT)) {
                    getContact(str);
                } else if (action.equals(EVENT_UPDATE)) {
                    eventUpdate(str);
                } else if (action.equals(CALL_PHONE)) {
                    callphone(str);
                } else if (action.equals(SET_VALUE)) {
                    setValue(str);
                } else if (action.equals(GET_VALUE)) {
                    getValue(str);
                } else if (action.equals(ZALO_LOGIN)) {
                    zaloLogin(str);
                } else if (action.equals(OPEN_MOMO)) {
                    openMomo(str);
                } else if (action.equals(ADD_CALENDER)) {
                    addCalender(str);
                } else if (action.equals(LIVENESS)) {
                    liveness(str);
                } else if (action.equals(UPDATE_CAMERA_APPS)) {
                    //解析js callback方法
                    try {
                        //解析js callback方法
                        JSONObject mJson = new JSONObject(str);
                        String step = mJson.optString("step");//解析js回调方法
                        ArrayList<String> tasks = new ArrayList<>();
                        tasks.add(RiskType.CAMERA_APP_LIST);
                        RiskUtils.updateAll(tasks, step);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else if (action.equals(UPDATE_KOCHAVE)) {
                    updateKochave(str);
                } else if (action.equals(SET_CAMERA_TYPE)) {
                    setCameraType(str);
                } else if (action.equals(GOTO_GOOGLE_PLAY)) {
                    gotoGooglePlay();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 权限获取
     */
    private void updateScorpio(String str) {
        try {
            //解析js callback方法
            JSONObject mJson = new JSONObject(str);
            String step = mJson.optString("step");//解析js回调方法
            RiskUtils.updateAll(null, step);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    OAuthCompleteListener listener = new OAuthCompleteListener() {
        @Override
        public void onAuthenError(int errorCode, String message) {
        }

        @Override
        public void onGetOAuthComplete(OauthResponse response) {
            // map map = Arguments.createMap();
            // map.put("uid", response.getuId()+"");
            // mZaloLoginPromise.resolve(data);
        }
    };

    private void zaloLogin(String str) {
        LogUtils.i("zaloLogin");
        SelectUtils.setSelectResult(this);
        ZaloSDK.Instance.authenticate(PluginInit.ACTIVITY, listener);
    }

    private void eventUpdate(String str) {
        //解析js callback方法
        try {
            JSONObject mJson = new JSONObject(str);
            String key = mJson.optString("key");
            String value = mJson.optString("value");
            String userGid = mJson.optString("userGid");
            ReportUtils.report(key, value, userGid);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static final int REQUEST_CODE_LIVENESS = 1000;
    private static final int SELCT_CONTACT_CODE = 1001;
    private static final int ZALO_LOGIN_CODE = 64725;

    private String contactType;

    private void getContact(String str) {
        //解析js callback方法
        JSONObject mJson = null;
        try {
            mJson = new JSONObject(str);
            contactType = mJson.optString("type");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String callback = mJson.optString("callback");//解析js回调方法
        SelectUtils.setSelectResult(this);
        SelectUtils.selectOneContact(PluginInit.ACTIVITY, SELCT_CONTACT_CODE);
    }

    @Override
    public void onSelect(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_LIVENESS) {
                if (PluginInit.LIVENESSHELP.isSuccess()) {
                    getLivenessResult();
                } else {
                    LogUtils.i("luolaigang", PluginInit.LIVENESSHELP.getErrorInfo());
                    try {
                        JSONObject json = new JSONObject();
                        json.put("errorMsg", PluginInit.LIVENESSHELP.getErrorInfo());
                        String callback = "getLivenessResult";//解析js回调方法
                        invokeJavaScript(callback, json.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else if (requestCode == SELCT_CONTACT_CODE && data != null) {
                Uri contactData = data.getData();
                Cursor cursor = PluginInit.ACTIVITY.getContentResolver().query(contactData, null, null, null, null);
                cursor.moveToFirst();
                Contact contact = ContactHelp.getContactPhone(cursor);
                getContactResult(contact);
            }
        }
    }

    private void getLivenessResult() {
        try {
            Bitmap livenessBitmap = PluginInit.LIVENESSHELP.getLivenessBitmap();
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_tmp.png";
            //String path = PictureUtils.saveBitmap(imageFileName, livenessBitmap, PluginInit.ACTIVITY);
            String callback = "getLivenessResult";//解析js回调方法
            JSONObject json = new JSONObject();
            json.put("base64", PictureUtils.getText(livenessBitmap));
            NetUtils.getLivenessInfos(PluginInit.LIVENESS_ACCESS_KEY, PluginInit.LIVENESSHELP.getLivenessId()
                    , response -> {
                        LogUtils.i("luolaigang", response.toString());
                        com.newstar.scorpiodata.entity.LivenessResult livenessResult = new Gson().fromJson(response.toString(), com.newstar.scorpiodata.entity.LivenessResult.class);
                        if (livenessResult != null && livenessResult.getData() != null) {
                            try {
                                json.put("livenessScore", livenessResult.getData().getLivenessScore());
                                invokeJavaScript(callback, json.toString());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.i("luolaigang", e.getMessage());
        }
    }

    private void getContactResult(
            Contact contact) {
        try {
            //解析js callback方法
            String callback = "getContactResult";//解析js回调方法
            JSONObject json = new JSONObject();
            json.put("name", contact.getName());
            json.put("phone", contact.getPhone());
            json.put("type", contactType);
            //调用js方法必须在主线程
            invokeJavaScript(callback, json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 权限获取
     */
    private void getPermissions(String str) {
        try {
            //解析js callback方法
            JSONObject mJson = new JSONObject(str);
            String callback = mJson.optString("callback");//解析js回调方法

            int noPermisstionCount = PermissionUtils.requestMultiPermissions(PermissionUtils.mPermissionGrant, PermissionUtils.requestPermissions);

            JSONObject json = new JSONObject();
            json.put("noPermissionCount", noPermisstionCount + "");
            //调用js方法必须在主线程
            invokeJavaScript(callback, json.toString());
            LogUtils.i(json.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getCalenderPermission(String str) {
        try {
            //解析js callback方法
            JSONObject mJson = new JSONObject(str);
            String callback = mJson.optString("callback");//解析js回调方法

            int noPermisstionCount = PermissionUtils.requestMultiPermissions(PermissionUtils.mPermissionGrant, PermissionUtils.requestCalendarPermissions);

            JSONObject json = new JSONObject();
            json.put("noPermissionCount", noPermisstionCount + "");
            //调用js方法必须在主线程
            invokeJavaScript(callback, json.toString());
            LogUtils.i(json.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 统一管理所有android调用js方法
     *
     * @param callback js回调方法名
     * @param json     传递json数据
     */
    private void invokeJavaScript(final String callback, final String json) {
        //showToast("回调js方法："+callback+", 参数："+json);
        if (TextUtils.isEmpty(callback)) return;
        //调用js方法必须在主线程
        webView.post(new Runnable() {
            @Override
            public void run() {
                webView.loadUrl("javascript:" + callback + "(" + json + ")");
            }
        });
    }

    public void onBackPress() {
        try {
            //解析js callback方法
            String callback = "onBackPress";//解析js回调方法

            JSONObject json = new JSONObject();
            json.put("onBackPress", "onBackPress");
            //调用js方法必须在主线程
            invokeJavaScript(callback, json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void initKochave() {
        try {
            LogUtils.i("luolaigang initKochave", SharedHelp.getSharedPreferencesValue(SharedHelp.KOCHAVE_REFERRER_URL));
            setItem("kochaveReferrerUrl", SharedHelp.getSharedPreferencesValue(SharedHelp.KOCHAVE_REFERRER_URL).replace("\\", "\\\\"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化壳参数
     */
    public void init() {
        setItem("product", PluginInit.PRODUCT);
        setItem("sourceCode", BuildConfig.SOURCE_CODE + "");
        try {
            setItem("app_version", UpdateManager.getVersionName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        setItem("statusBarHeight", ViewHelp.getStatusBarHeight() + "");
        new Thread() {
            @Override
            public void run() {
                super.run();
                String kochaveReferrerUrl = SharedHelp.getSharedPreferencesValue(SharedHelp.KOCHAVE_REFERRER_URL);
                while (kochaveReferrerUrl == null) {//循环等待，直到获取成功
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    kochaveReferrerUrl = SharedHelp.getSharedPreferencesValue(SharedHelp.KOCHAVE_REFERRER_URL);
                }
                if (kochaveReferrerUrl != null) {
                    initHandler.sendEmptyMessage(0);
                }
            }
        }.start();

        new Thread() {
            @Override
            public void run() {
                super.run();
                String googleReferrerUrl = SharedHelp.getSharedPreferencesValue(SharedHelp.GOOGLE_REFERRER_URL);
                while (googleReferrerUrl == null) {//循环等待，直到获取成功
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    googleReferrerUrl = SharedHelp.getSharedPreferencesValue(SharedHelp.GOOGLE_REFERRER_URL);
                }
                if (googleReferrerUrl != null) {
                    initHandler.sendEmptyMessage(0);
                }
            }
        }.start();

        new Thread() {
            @Override
            public void run() {
                super.run();
                String fmcToken = SharedHelp.getSharedPreferencesValue(SharedHelp.FMC_TOKEN);
                while (fmcToken == null) {//循环等待，直到获取成功
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    fmcToken = SharedHelp.getSharedPreferencesValue(SharedHelp.FMC_TOKEN);
                }
                if (fmcToken != null) {
                    initHandler.sendEmptyMessage(0);
                }
            }
        }.start();
    }

    /**
     * 初始化壳参数
     */
    public void initUid() {
        try {
            JSONObject jsonBack = new JSONObject();
            jsonBack.put("uid", SharedHelp.getUid());
            //调用js方法必须在主线程
            setItem("otherRiskInfo", jsonBack.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    Handler initHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            initKochave();
            setItem("googleReferrerUrl", SharedHelp.getSharedPreferencesValue(SharedHelp.GOOGLE_REFERRER_URL));
            setItem("firstInstall", SharedHelp.getSharedPreferencesValue(SharedHelp.IS_FIRST_INSTALL));
            setItem("fmcToken", SharedHelp.getSharedPreferencesValue(SharedHelp.FMC_TOKEN));
        }
    };

    public void callphone(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            //解析json
            String phone = jsonObject.optString("phone");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//拨号：android 6.0运行时权限
                if (mActivity.checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_DENIED) {
                    mActivity.requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, 100);
                }
            }
            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone));
            mActivity.startActivity(intent);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    /**
     * 保存属性到sd卡
     *
     * @param json
     */
    public void setValue(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            //解析json
            String key = jsonObject.optString("key");
            String value = jsonObject.optString("value");
            SharedHelp.setSharedPreferencesValue(key,value);
            ShareSdProperty.setValue(key, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从sd卡获取属性
     *
     * @param json
     */
    public void getValue(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            //解析json
            String key = jsonObject.optString("key");

            String value = ShareSdProperty.getValue(key);

            String callback = "getValueCallBack";
            JSONObject jsonBack = new JSONObject();
            jsonBack.put("value", value);
            //调用js方法必须在主线程
            invokeJavaScript(callback, jsonBack.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void showToast(String json) {
        try {
            Toast.makeText(mActivity, json, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 分享到QQ
     *
     * @param jsonStr
     */
    public void share2QQ(String jsonStr) {
        try {
            JSONObject json = new JSONObject(jsonStr);
            Log.e("result", json.toString());
            //解析js传递过来的分享参数
            JSONObject mJson = new JSONObject(json.toString());
            String title = mJson.optString("title");
            String url = mJson.optString("url");
            String summary = mJson.optString("summary");
            String imgUrl = mJson.optString("imgUrl");


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 上传kochave信息
     *
     * @param str
     */
    public void updateKochave(String str) {
        try {
            String userGid = SharedHelp.getSharedPreferencesValue(SharedHelp.USER_GID);
            String kochaveReferrerUrl = SharedHelp.getSharedPreferencesValue(SharedHelp.KOCHAVE_REFERRER_URL);
            JSONObject attributionObject = new JSONObject(kochaveReferrerUrl);
            String afStatus = null;
            if (kochaveReferrerUrl == null || kochaveReferrerUrl.length() == 0) {
                afStatus = "no_ko_install";
            } else {
                if ("false".equals(attributionObject.optString("attribution", ""))) {
                    afStatus = "Organic";
                } else {
                    afStatus = "Non-organic";
                }
            }
            attributionObject.put("af_status", afStatus);

            // 自定义请求头
            Map<String, String> headers = NetUtils.getToken();
            Map<String, String> params = new HashMap<>();
            params.put("userGid", userGid);
            JSONObject jsonObjectKochave = new JSONObject();
            jsonObjectKochave.put("register_source", BuildConfig.SOURCE_CODE);
            jsonObjectKochave.put("kochavaInstallData", attributionObject);
            JSONObject jsonObjectKochaveUpdata = new JSONObject();
            jsonObjectKochaveUpdata.put("afInstallData", jsonObjectKochave);
            //MyApplication.i("luolaigang_kochave",jsonObjectKochaveUpdata.toString());
            NetUtils.requestPostInQueue(PluginInit.HOST + "files/registerSouce",
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            //Log.e("luolaigang_kochave",response.toString());
                        }
                    }, jsonObjectKochaveUpdata, params, headers);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * //设置摄像头类型0后置,1前置
     *
     * @param str
     */
    public void setCameraType(String str) {
        try {
            JSONObject mJson = new JSONObject(str);
            String type = mJson.optString("type");
            if (type != null && type.equals("0")) {
                CameraActivity.cameraType = 0;
            } else if (type != null && type.equals("1")) {
                CameraActivity.cameraType = 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 跳转谷歌play
     */
    public void gotoGooglePlay() {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("market://details?id=" + PluginInit.ACTIVITY.getPackageName()));
            //intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + PluginInit.ACTIVITY.getPackageName()));
            if (intent.resolveActivity(PluginInit.ACTIVITY.getPackageManager()) != null) {
                PluginInit.ACTIVITY.startActivity(intent);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}

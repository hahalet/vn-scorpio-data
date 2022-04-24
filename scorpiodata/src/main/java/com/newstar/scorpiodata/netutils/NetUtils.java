package com.newstar.scorpiodata.netutils;

import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.newstar.scorpiodata.BuildConfig;
import com.newstar.scorpiodata.entity.StatusParent;
import com.newstar.scorpiodata.utils.LogUtils;
import com.newstar.scorpiodata.utils.PluginInit;
import com.newstar.scorpiodata.utils.SharedHelp;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class NetUtils {
    //1.创建一个volley队列
    private static RequestQueue queue;
    private static Map header = new HashMap();
    //确认授信资料提交完成
    public static String UPLOAD_RISK_DATA;
    //检查更新
    public static String APP_VERSION_UPDATE;
    //上传归因信息
    public static String INSERT_PROMOTERS_GID;
    //日志记录
    public static String APPERROR_SAVE_SUBMIT;

    public static void init(){
        header.put("channel", PluginInit.CHANNEL);
        header.put("subChannel", PluginInit.SUB_CHANNEL);
        header.put("product", PluginInit.PRODUCT);
        queue = Volley.newRequestQueue(PluginInit.ACTIVITY);
        UPLOAD_RISK_DATA = PluginInit.HOST + "xUserContactss/upload_risk_data";
        APP_VERSION_UPDATE = PluginInit.HOST + "appVersion/geAppVersionInfo";
        INSERT_PROMOTERS_GID = PluginInit.HOST + "appMacCode/insertPromotersGid";
        APPERROR_SAVE_SUBMIT = PluginInit.HOST + "appError/save";
    }

    public static void requestGetInQueue(int methend, String url, Response.Listener<String> listener, Map<String, String> mapParams, Map<String, String> headers) {
        if(headers==null){
            headers = NetUtils.getToken();
        }
        if (mapParams != null && !mapParams.isEmpty() && methend== Request.Method.GET) {
            boolean isFirst = true;
            for (Map.Entry<String, String> entry : mapParams.entrySet()) {
                url = url + (isFirst ? "?" : "&") + entry.getKey() + "=" + entry.getValue();
                isFirst = false;
            }
        }
        StringUtil stringRequest = new StringUtil(
                methend,
                url,
                listener,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        authFailureError(error);
                    }
                }, mapParams, headers
        );

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        //把请求添加到队列中处理,Volley执行的是异步操作.
        queue.add(stringRequest);
    }

    public static void requestPostInQueue(String url, Response.Listener<JSONObject> listener, JSONObject jsonObject, Map<String, String> mapParams, Map<String, String> headers) {
        if(headers==null){
            headers = NetUtils.getToken();
        }
        if (mapParams != null && !mapParams.isEmpty() && jsonObject!=null) {
            boolean isFirst = true;
            for (Map.Entry<String, String> entry : mapParams.entrySet()) {
                url = url + (isFirst ? "?" : "&") + entry.getKey() + "=" + entry.getValue();
                isFirst = false;
            }
        }
        if(jsonObject!=null){
            try {
                jsonObject.put("channel", PluginInit.CHANNEL);
                jsonObject.put("subChannel", PluginInit.SUB_CHANNEL);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        JsonRequest<JSONObject> jsonRequest = new JsonUtil( url, jsonObject,
                listener, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                authFailureError(error);
            }
        }, mapParams, headers);
        //把请求添加到队列中处理,Volley执行的是异步操作.
        queue.add(jsonRequest);
    }

    /**
     * 获取token
     * @return
     */
    public static Map<String, String> getToken(){
        String token = SharedHelp.getSharedPreferencesValue(SharedHelp.TOKEN);
        // 自定义请求头
        Map<String, String> headers = new HashMap<>();
        if(token!=null && token.length()>0) {
            headers.put("token", token);
        }
        headers.putAll(header);
        return headers;
    }

    /**
     * 获取userGid
     * @return
     */
    public static Map<String, String> getUserGid(){
        String userGid = SharedHelp.getSharedPreferencesValue(SharedHelp.USER_GID);
        Map<String, String> mapParams = new HashMap<>();
        if (userGid != null && userGid.length() > 0) {
            mapParams.put("userGid", userGid);
        }
        return mapParams;
    }


    private static void authFailureError(VolleyError error){
        LogUtils.i("luolaigang",error.getMessage());
    }

    public static void getLivenessInfos(String livenessAccessKey,String livenessId, Response.Listener<JSONObject> response){
        JSONObject jsonObject = new JSONObject();
        try{
            jsonObject.put("livenessId", livenessId);
            jsonObject.put("resultType", "IMAGE_URL");
            jsonObject.put("region", PluginInit.COUNTRY_CODE);
        }catch(Exception e){
            LogUtils.i("luolaigang",e.getMessage());
        }
        LogUtils.i("luolaigang",jsonObject.toString());
        // 自定义请求头
        Map<String, String> headers = NetUtils.getToken();

        headers.put("X-ADVAI-KEY", livenessAccessKey);

        NetUtils.requestPostInQueue(PluginInit.LIVENESS_HOST,
                response, jsonObject, null, headers);
    }
}

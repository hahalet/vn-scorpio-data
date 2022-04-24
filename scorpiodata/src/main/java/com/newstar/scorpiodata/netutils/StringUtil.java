package com.newstar.scorpiodata.netutils;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.newstar.scorpiodata.utils.LogUtils;
import com.newstar.scorpiodata.utils.PluginInit;

import java.util.Map;
import java.util.concurrent.Executors;

public class StringUtil extends StringRequest {

    private Map<String, String> mapParams;
    private Map<String, String> headers;

    /**
     * 构建一个StringRequest请求
     *
     * @param method 请求的方式，取值为Request的常量，
     *               如Request.Method.GET
     * @param method
     * @param url    请求url
     * @param headers 需要携带的cookie，不需要给null
     * @paramlistener 请求成功的回调监听
     * @paramerrorListener 请求错误的回调
     * @parammapParams 参数，没有参数，给null
     */
    public StringUtil(int method, String url,
                      Listener<String> listener,
                      ErrorListener errorListener,
                      Map<String, String> mapParams,
                      Map<String, String> headers) {
        super(method, url, listener, errorListener);
        this.headers = headers;
        this.mapParams = mapParams;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        if(headers!=null){
            return headers;
        }else{
            return super.getHeaders();
        }
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        if(mapParams!=null){
            return mapParams;
        }else{
            return super.getParams();
        }
    }
}


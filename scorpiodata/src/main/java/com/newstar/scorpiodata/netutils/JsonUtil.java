package com.newstar.scorpiodata.netutils;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.Map;

public class JsonUtil extends JsonObjectRequest {
    private Map<String, String> headers;
    private Map<String, String> mapParams;

    /**
     * 构建一个StringRequest请求
     *
     * @param url    请求url
     * @param headers 需要携带的cookie，不需要给null
     * @paramlistener 请求成功的回调监听
     * @paramerrorListener 请求错误的回调
     * @parammapParams 参数，没有参数，给null
     */
    public JsonUtil(String url,
                    JSONObject jsonObject,
                    Listener<JSONObject> listener,
                    ErrorListener errorListener,
                    Map<String, String> mapParams,
                    Map<String, String> headers) {
        super(Request.Method.POST, url,jsonObject, listener, errorListener);
        this.mapParams = mapParams;
        this.headers = headers;
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


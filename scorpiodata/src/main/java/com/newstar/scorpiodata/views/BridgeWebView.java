package com.newstar.scorpiodata.views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.newstar.scorpiodata.BuildConfig;
import com.newstar.scorpiodata.activitys.CameraActivity;
import com.newstar.scorpiodata.entity.JavaSctiptMethods;
import com.newstar.scorpiodata.utils.LogUtils;
import com.newstar.scorpiodata.utils.PluginInit;
import com.newstar.scorpiodata.utils.SelectUtils;
import com.newstar.scorpiodata.utils.SharedHelp;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;

/**
 * js和android通信桥梁WebView，封装好js和安卓通信机制
 * Created by youliang.ji on 2016/12/23.
 */

public class BridgeWebView extends WebView implements SelectUtils.SelectResult {

    /***
     * js调用android方法的映射字符串
     **/
    private static final String JS_INTERFACE = "jsInterface";

    public BridgeWebView(Context context) {
        super(context);
    }

    public BridgeWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BridgeWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private long startTime = 0;
    private TextView reload;
    private View loading;

    public void reload() {
        startTime = new Date().getTime();
        loadUrl(PluginInit.H5_HOST);
        reload.setVisibility(GONE);
        loading.setVisibility(VISIBLE);
    }

    /**
     * 注册js和android通信桥梁对象
     *
     * @param javaSctiptMethods 桥梁类对象,该对象提供方法让js调用,默认开启JavaScriptEnabled=true
     * @param loadingHandler
     * @param reload
     * @param loading
     */
    public void addBridgeInterface(JavaSctiptMethods javaSctiptMethods, Handler loadingHandler, TextView reload, View loading) {
        SharedHelp.setSharedPreferencesValue(SharedHelp.FIRST_LOADED, null);
        this.reload = reload;
        this.loading = loading;
        //打开本地缓存提供JS调用,至关重要
        //允许使用JS
        // 设置允许JS弹窗
        getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        getSettings().setDomStorageEnabled(true);
        getSettings().setJavaScriptEnabled(true);
        getSettings().setAppCacheEnabled(true);
        getSettings().setAllowFileAccess(true);
        setWebContentsDebuggingEnabled(BuildConfig.DEBUG);
        getSettings().setCacheMode(BuildConfig.DEBUG?WebSettings.LOAD_NO_CACHE:WebSettings.LOAD_DEFAULT);
        String appCachePath = PluginInit.APPLICATION.getCacheDir().getAbsolutePath();
        getSettings().setAppCachePath(appCachePath);
        getSettings().setDatabaseEnabled(true);
        startTime = new Date().getTime();
        setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                SharedHelp.setSharedPreferencesValue(SharedHelp.FIRST_LOADED, url);
                if (url != null && !url.equals("about:blank")) {
                    LogUtils.i(url);
                    javaSctiptMethods.init();
                }
                long timeFinish = new Date().getTime();
                if (timeFinish - startTime < 2000) {
                    loadingHandler.sendEmptyMessageDelayed(0, 2000 + startTime - timeFinish);
                } else {
                    loadingHandler.sendEmptyMessage(0);
                }
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                String firstLoaded = SharedHelp.getSharedPreferencesValue(SharedHelp.FIRST_LOADED);
                LogUtils.i(firstLoaded);
                if (firstLoaded == null || firstLoaded.equals("about:blank")) {
                    view.loadUrl("about:blank");
                    reload.setVisibility(VISIBLE);
                    loading.setVisibility(GONE);
                }
            }

            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                super.onReceivedHttpError(view, request, errorResponse);
                String firstLoaded = SharedHelp.getSharedPreferencesValue(SharedHelp.FIRST_LOADED);
                LogUtils.i(firstLoaded);
                /*if (firstLoaded == null || firstLoaded.equals("about:blank")) {
                    view.loadUrl("about:blank");
                    reload.setVisibility(VISIBLE);
                    loading.setVisibility(GONE);
                }*/
            }
        });
        setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                mUploadCallbackAboveL = filePathCallback;
                SelectUtils.setSelectResult(BridgeWebView.this);

                if (fileChooserParams.isCaptureEnabled()) {
                    SelectUtils.selectOnePictureSelf(PluginInit.ACTIVITY, REQUEST_CODE);
                } else {
                    SelectUtils.selectOnePicturePick(PluginInit.ACTIVITY, REQUEST_CODE_PICK);
                }

                return true;
            }
        });
        addJavascriptInterface(new MyJavaScriptMethod(javaSctiptMethods), JS_INTERFACE);
    }

    /**
     * 内置js桥梁类
     * Created by youliang.ji on 2016/12/23.
     */

    public class MyJavaScriptMethod {
        private Object mTarget;
        private Method targetMethod;

        public MyJavaScriptMethod(Object targer) {
            this.mTarget = targer;
        }

        /**
         * 内置桥梁方法
         *
         * @param method 方法名
         * @param json   js传递参数，json格式
         */
        @JavascriptInterface
        public void invokeMethod(String method, String[] json) {
            Class<?>[] params = new Class[]{String[].class};
            try {
                Method targetMethod = this.mTarget.getClass().getDeclaredMethod(method, params);
                targetMethod.invoke(mTarget, new Object[]{json});//反射调用js传递过来的方法，传参
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void onSelect(int requestCode, int resultCode, Intent data) {
        if (Activity.RESULT_OK == resultCode) {
            if(requestCode == REQUEST_CODE){
                picPath = data.getStringExtra(CameraActivity.PHOTO_PATH);
            }else if(requestCode == REQUEST_CODE_PICK){
                Cursor cursor = null;
                final String[] projection = {MediaStore.Images.Media.DATA};
                try {
                    cursor = PluginInit.ACTIVITY.getContentResolver().query(data.getData(), projection, null, null,
                            null);
                    if (cursor != null && cursor.moveToFirst()) {
                        final int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                        picPath = cursor.getString(index);
                    }
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }

            if (data != null && picPath != null) {
                callbackImage(picPath);
            }else{
                mUploadCallbackAboveL.onReceiveValue(new Uri[]{});
            }
        }else{
            mUploadCallbackAboveL.onReceiveValue(new Uri[]{});
        }
    }

    private void callbackImage(String photoSrcPath) {
        try {
            // 压缩处理
            if (photoSrcPath != null && !TextUtils.isEmpty(photoSrcPath)) {
                Uri uri = Uri.fromFile(new File(photoSrcPath));
                mUploadCallbackAboveL.onReceiveValue(new Uri[]{uri});
            }else{
                mUploadCallbackAboveL.onReceiveValue(null);
                mUploadCallbackAboveL = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ValueCallback<Uri[]> mUploadCallbackAboveL;
    private int REQUEST_CODE = 10001;
    private int REQUEST_CODE_PICK = 10002;
    private String picPath;
}

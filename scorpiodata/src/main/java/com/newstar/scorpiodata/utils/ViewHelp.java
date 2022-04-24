package com.newstar.scorpiodata.utils;

import android.content.res.Resources;
import android.graphics.Color;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

public class ViewHelp {
    public static void setFullscreen(AppCompatActivity activity, boolean lightBar) {
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        if(lightBar){
            uiOptions = uiOptions|View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        }

        activity.getWindow().getDecorView().setSystemUiVisibility(uiOptions);
        //隐藏标题栏
        if(activity instanceof AppCompatActivity && activity.getSupportActionBar()!=null){
            activity.getSupportActionBar().hide();
        }
        //专门设置一下状态栏导航栏背景颜色为透明，凸显效果。
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        //activity.getWindow().setNavigationBarColor(activity.getResources().getColor(R.color.white));
        activity.getWindow().setStatusBarColor(Color.TRANSPARENT);
    }

    public static int getStatusBarHeight() {
        int height = 90;
        try{
            Resources resources = PluginInit.ACTIVITY.getResources();
            int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
            height = resources.getDimensionPixelSize(resourceId);
        }catch(Exception e){
            e.printStackTrace();
        }

        return height;
    }
}

package com.newstar.scorpiodata.activitys;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.messaging.FirebaseMessaging;
import com.newstar.scorpiodata.utils.SharedHelp;
import com.newstar.scorpiodata.utils.ViewHelp;

import java.util.Objects;

public abstract class InitActivity extends AppCompatActivity implements LivenessHelp {
    public boolean lightBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);// 隐藏标题
        try{
            ViewHelp.setFullscreen(this, lightBar);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
    }
    public void setLightBar(boolean lightBar) {
        this.lightBar = lightBar;
    }
}
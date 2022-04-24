package com.newstar.scorpiodata.activitys;

import android.graphics.Bitmap;

public interface LivenessHelp {
    public Bitmap getLivenessBitmap();
    public String getLivenessId();
    public boolean isSuccess();
    public String getErrorInfo();
    public double getLivenessScore();
}

package com.newstar.scorpiodata.activitys;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import com.newstar.scorpiodata.R;
import com.newstar.scorpiodata.utils.UpdateReceiver;
import com.newstar.scorpiodata.utils.PluginInit;

public class AndroidOInstallPermissionActivity extends Activity {
    public static final int INSTALL_PACKAGE_REQUEST_CODE = 300;
    private AlertDialog mAlertDialog;
    public static UpdateReceiver.AndroidOInstallPermissionListener iListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 26) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.REQUEST_INSTALL_PACKAGES}, INSTALL_PACKAGE_REQUEST_CODE);
        } else {
            finish();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case INSTALL_PACKAGE_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (iListener != null) {
                        iListener.permissionSuccess();
                        finish();
                    }
                } else {
                    showDialog();
                }
                break;
        }
    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(PluginInit.getAppName());
        builder.setMessage(this.getString(R.string.click_setting));
        builder.setPositiveButton(this.getString(R.string.setting), new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                startInstallPermissionSettingActivity();
                mAlertDialog.dismiss();
            }
        });
        builder.setNegativeButton(this.getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (iListener != null) {
                    iListener.permissionFail();
                }
                mAlertDialog.dismiss();
                finish();
            }
        });
        mAlertDialog = builder.create();
        mAlertDialog.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startInstallPermissionSettingActivity() {
        //注意这个是8.0新API
        Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            // 授权成功
            if (iListener != null) {
                iListener.permissionSuccess();
            }
        } else {
            // 授权失败
            if (iListener != null) {
                iListener.permissionFail();
            }
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        iListener = null;
    }
}

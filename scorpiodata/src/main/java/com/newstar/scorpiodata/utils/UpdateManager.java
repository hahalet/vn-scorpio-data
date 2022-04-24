package com.newstar.scorpiodata.utils;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.toolbox.JsonRequest;
import com.google.gson.Gson;
import com.newstar.scorpiodata.R;
import com.newstar.scorpiodata.entity.VersionInfo;
import com.newstar.scorpiodata.entity.VersionInfos;
import com.newstar.scorpiodata.entity.Status;
import com.newstar.scorpiodata.netutils.NetUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateManager {
    public final static int ShowDialogCode = 1;
    public final static int ShowToastCode = 2;
    public final static int ProgressChange = 3;
    public final static int DownloadComplete = 4;
    public static boolean downloading = false;
    private static ProgressDialog progressDialog;
    private static AlertDialog dialog;
    private static DownloadManager downloadManager;
    private static long downloadId;
    private static Runnable removeDownloadRunnable;

    // 检查当前app类型
    public static int checkVariant(Context context) {
        int code = 64;

        return code;
    }

    public static boolean isHttpUrl(String urls) {
        boolean isurl = false;
        String regex = "https?://\\S*\\.apk";//设置正则表达式

        Pattern pat = Pattern.compile(regex.trim());//对比
        Matcher mat = pat.matcher(urls.trim());
        isurl = mat.matches();//判断是否匹配
        if (isurl) {
            isurl = true;
        }
        return isurl;
    }

    private static Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case ShowDialogCode:
                    showNoticeDialog(msg.getData());
                    break;
                case ShowToastCode:
                    Toast.makeText(PluginInit.ACTIVITY, PluginInit.ACTIVITY.getString(R.string.already_new), Toast.LENGTH_SHORT).show();
                    break;
                case ProgressChange:
                    if (progressDialog != null) {
                        int progress = msg.getData().getInt("progress");
                        if (progress < 100) {
                            progressDialog.setProgress(msg.getData().getInt("progress"));
                        }
                    }
                    break;
                case DownloadComplete:
                    downloading = false;
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                    }
                    if (dialog != null) {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setText(PluginInit.ACTIVITY.getString(R.string.install));
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Uri appUri = UpdateReceiver.getFileUri(PluginInit.ACTIVITY);
                                UpdateReceiver.installApp(PluginInit.ACTIVITY, appUri);
                            }
                        });
                    }
                    if (removeDownloadRunnable != null) {
                        PluginInit.OnDestroyEventList.remove(removeDownloadRunnable);
                    }
                    break;
            }
        }
    };

    public static void checkUpdate(int delay, boolean manual) {
        downloadManager = (DownloadManager) PluginInit.ACTIVITY.getSystemService(PluginInit.ACTIVITY.DOWNLOAD_SERVICE);
        Map<String, String> mapParams = new HashMap<>();
        String versionName = null;
        try {
            versionName = getVersionName();
            //mapParams.put("appName", BuildConfig.SOURCE_NAME);
            //mapParams.put("versionNumber", versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        // 自定义请求头
        Map<String, String> headers = NetUtils.getToken();
        String finalVersionName = versionName;
        NetUtils.requestGetInQueue(JsonRequest.Method.GET, NetUtils.APP_VERSION_UPDATE, response -> {
            VersionInfos versionInfos = new Gson().fromJson(response, VersionInfos.class);
            if (versionInfos != null && versionInfos.getStatus() != null) {
                if (versionInfos.getStatus().getCode().intValue() == Status.SUCCEED) {
                    if (versionInfos.getBo() != null) {
                        VersionInfo versionInfo = versionInfos.getBo();
                        new Thread(() -> {
                            try {
                                if (downloading) return;

                                if (dialog != null) {
                                    dialog.dismiss();
                                    dialog = null;
                                }
                                if (progressDialog != null) {
                                    progressDialog.dismiss();
                                    progressDialog = null;
                                }

                                if (versionInfo != null) {
                                    int needUpdate = Integer.parseInt(versionInfo.getNeedUpdate());
                                    Message msg = new Message();

                                    if (needUpdate > -1) {
                                        if (finalVersionName != null && finalVersionName.compareTo(versionInfo.getVersionNumber()) >= 0) {
                                            return;
                                        }
                                        msg.what = ShowDialogCode;

                                        Bundle bundle = new Bundle();
                                        // app最新版本号
                                        bundle.putString("versionNumber", versionInfo.getVersionNumber());
                                        // 版本更新内容
                                        bundle.putString("versionContent", versionInfo.getVersionContent());
                                        // 下载链接
                                        bundle.putString("appDownloadUrl", versionInfo.getAppDownloadUrl());
                                        // 更新标识 0为正常提示更新  1为强制更新
                                        bundle.putInt("needUpdate", needUpdate);
                                        msg.setData(bundle);

                                        if (delay != 0) {
                                            Thread.sleep(delay);
                                        }
                                        mHandler.sendMessage(msg);
                                    } else {
                                        // 判断是否手动触发
                                        if (manual) {
                                            msg.what = ShowToastCode;
                                            mHandler.sendMessage(msg);
                                        }
                                    }
                                }
                            } catch (InterruptedException e) {
                            }
                        }).start();
                    }
                } else {
                    //versionInfos.getStatus().showMessage();
                }
            }
        }, mapParams, headers);

    }

    public static String  getVersionName() throws PackageManager.NameNotFoundException {
        PackageManager packageManager = PluginInit.ACTIVITY.getPackageManager();
        PackageInfo packInfo = packageManager.getPackageInfo(PluginInit.ACTIVITY.getPackageName(), 0);
        return packInfo.versionName;
    }



    private static void showNoticeDialog(Bundle bundle) {
        String versionNumber = bundle.getString("versionNumber");
        String versionContent = bundle.getString("versionContent");
        String appDownloadUrl = bundle.getString("appDownloadUrl");
        // 0普通更新 1强制更新
        int needUpdate = bundle.getInt("needUpdate");

        // 更新静态属性
        UpdateReceiver.versionNumber = versionNumber;
        Activity currActivity = PluginInit.ACTIVITY;
        if (!currActivity.isFinishing()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(currActivity);
            builder.setTitle(PluginInit.ACTIVITY.getString(R.string.find_new_version) + versionNumber)
                    .setMessage(versionContent)
                    .setCancelable(false)
                    .setPositiveButton(PluginInit.ACTIVITY.getString(R.string.ok), null);

            if (needUpdate == 0) {
                builder.setNegativeButton(PluginInit.ACTIVITY.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
            }

            // 先show dialog再set onClickListener点击按钮就不会关闭dialog
            dialog = builder.create();
            dialog.show();

            if (appDownloadUrl == null || TextUtils.isEmpty(appDownloadUrl) || !isHttpUrl(appDownloadUrl)) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setText(PluginInit.ACTIVITY.getString(R.string.gp_update));
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        gotoUpdate(PluginInit.ACTIVITY.getPackageName());
                    }
                });
            } else {
                // 0 未下载完成 1 下载完成
                String currVersionJSON = SharedHelp.getSharedPreferencesValue(SharedHelp.UPDATE_VERSION_PREFIX_KEY + versionNumber);
                try {
                    int currVersionAppDownloadStatus = 0;
                    if (currVersionJSON != null && !TextUtils.isEmpty(currVersionJSON)) {
                        JSONObject versionObj = new JSONObject(currVersionJSON);
                        currVersionAppDownloadStatus = versionObj.getInt("status");
                    }
                    if (currVersionAppDownloadStatus == 0) {
                        Uri appDownloadUri = Uri.parse(appDownloadUrl);
                        String appFileName = appDownloadUrl.substring(appDownloadUrl.lastIndexOf("/") + 1).replace(".apk", "") + timestamp() + ".apk";

                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setText(PluginInit.ACTIVITY.getString(R.string.download));
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // 如果下载中不再次下载
                                if (downloading) {
                                    Toast toast = Toast.makeText(currActivity, PluginInit.ACTIVITY.getString(R.string.downloading), Toast.LENGTH_SHORT);
                                    toast.setGravity(Gravity.BOTTOM, 0, 0);
                                    toast.show();
                                    return;
                                }

                                if (checkPermission(currActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                    Toast.makeText(PluginInit.ACTIVITY, PluginInit.ACTIVITY.getString(R.string.start_download), Toast.LENGTH_SHORT).show();
                                    IntentFilter filter = new IntentFilter();
                                    filter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
                                    filter.addAction(DownloadManager.ACTION_NOTIFICATION_CLICKED);
                                    BroadcastReceiver receiver = new UpdateReceiver(mHandler);
                                    PluginInit.ACTIVITY.registerReceiver(receiver, filter);

                                    DownloadManager.Request request = new DownloadManager.Request(appDownloadUri);
                                    // 完成后显示通知
                                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                    // 运行wifi和移动网络下载
                                    request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
                                    // 显示下载界面
                                    request.setVisibleInDownloadsUi(true);
                                    // 设置下载目录
                                    request.setDestinationInExternalFilesDir(
                                            PluginInit.ACTIVITY, Environment.DIRECTORY_DOWNLOADS, appFileName
                                    );
                                    // 允许被发现
                                    request.allowScanningByMediaScanner();
                                    // 设置标题
                                    request.setTitle(PluginInit.ACTIVITY.getString(R.string.download) + PluginInit.getAppName() + "-v" + versionNumber);
                                    // 设置描述
                                    request.setDescription(PluginInit.ACTIVITY.getString(R.string.downloading));

                                    downloadId = downloadManager.enqueue(request);

                                    // 注册onDestroy事件 移除下载
                                    removeDownloadRunnable = new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                String json = SharedHelp.getSharedPreferencesValue(SharedHelp.UPDATE_VERSION_PREFIX_KEY + versionNumber);
                                                JSONObject versionObj = new JSONObject(json);
                                                // 如果还没下载完则移除 并删除文件
                                                if (versionObj.getInt("status") == 0) {
                                                    downloadManager.remove(downloadId);
                                                }
                                            } catch (Exception e) {
                                            }
                                        }
                                    };
                                    PluginInit.OnDestroyEventList.add(removeDownloadRunnable);

                                    // 显示下载进度框
                                    progressDialog = new ProgressDialog(currActivity);
                                    progressDialog.setMax(100);
                                    progressDialog.setTitle(PluginInit.ACTIVITY.getString(R.string.downloading));
                                    progressDialog.setMessage(PluginInit.ACTIVITY.getString(R.string.wait_download));
                                    progressDialog.setCancelable(false);
                                    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                                    //这里设置的是是否显示进度,设为false才是显示的哦！
                                    progressDialog.setIndeterminate(false);
                                    progressDialog.show();

                                    Timer timer = new Timer();
                                    TimerTask timerTask = new TimerTask() {
                                        @Override
                                        public void run() {
                                            int[] downloadBytesAndStatus = UpdateReceiver.getBytesAndStatus(currActivity, downloadId);
                                            if (((downloadBytesAndStatus[0] == downloadBytesAndStatus[1]) && downloadBytesAndStatus[2] == DownloadManager.STATUS_SUCCESSFUL) || downloadBytesAndStatus[2] == DownloadManager.STATUS_FAILED) {
                                                timer.cancel();
                                                this.cancel();
                                            } else {
                                                float progress = (float) downloadBytesAndStatus[0] / downloadBytesAndStatus[1];
                                                Message progressMsg = new Message();
                                                progressMsg.what = ProgressChange;
                                                Bundle progressBundle = new Bundle();
                                                progressBundle.putInt("progress", (int) (progress * 100));
                                                progressMsg.setData(progressBundle);
                                                mHandler.sendMessage(progressMsg);
                                            }
                                        }
                                    };
                                    timer.schedule(timerTask, 0, 300);

                                    downloading = true;

                                    try {
                                        JSONObject versionJSON = new JSONObject();
                                        versionJSON.put("myId", downloadId);
                                        versionJSON.put("fileName", appFileName);
                                        versionJSON.put("status", 0);
                                        SharedHelp.setSharedPreferencesValue(SharedHelp.UPDATE_VERSION_PREFIX_KEY + versionNumber, versionJSON.toString());
                                    } catch (JSONException ignore) {
                                    }
                                } else {
                                    // 申请权限
                                    AlertDialog.Builder builder = new AlertDialog.Builder(currActivity);
                                    builder.setTitle(R.string.allow_storage_permission)
                                            .setCancelable(false)
                                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    verifyPermissions(currActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE, 1);
                                                }
                                            })
                                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                }
                                            });

                                    AlertDialog alertDialog = builder.create();
                                    alertDialog.show();
                                }
                            }
                        });
                    } else if (currVersionAppDownloadStatus == 1) {
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setText(PluginInit.ACTIVITY.getString(R.string.install));
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Uri appUri = UpdateReceiver.getFileUri(PluginInit.ACTIVITY);
                                UpdateReceiver.installApp(PluginInit.ACTIVITY, appUri);
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String timestamp() {
        Long timestamp = System.currentTimeMillis();
        return timestamp.toString();
    }

    // 检查是否有权限
    public static final boolean checkPermission(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public static void verifyPermissions(Activity activity, String permission, int requestCode) {
        try {
            int check = ActivityCompat.checkSelfPermission(PluginInit.APPLICATION, permission);
            if (check != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{permission}, requestCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 跳转google play更新
    public static void gotoUpdate(String packageName) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("market://details?id=" + packageName));
            if (intent.resolveActivity(PluginInit.ACTIVITY.getPackageManager()) != null) {
                PluginInit.ACTIVITY.startActivity(intent);
            } else {
                intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + packageName));
                if (intent.resolveActivity(PluginInit.ACTIVITY.getPackageManager()) != null) {
                    PluginInit.ACTIVITY.startActivity(intent);
                }
            }
        } catch (Exception e) {
        }
    }

}

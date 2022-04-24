package com.newstar.scorpiodata.utils;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.newstar.scorpiodata.R;
import com.newstar.scorpiodata.activitys.AndroidOInstallPermissionActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class UpdateReceiver extends BroadcastReceiver {
    public static String versionNumber;
    private static Handler mHandler;

    public UpdateReceiver(Handler handler) {
        mHandler = handler;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String versionJSON = SharedHelp.getSharedPreferencesValue(SharedHelp.UPDATE_VERSION_PREFIX_KEY + versionNumber);
        if (versionJSON == null || TextUtils.isEmpty(versionJSON)) return;

        try {
            JSONObject versionObj = new JSONObject(versionJSON);
            long did = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
            long myId = versionObj.getLong("myId");
            if (myId != did) return;
            int[] downloadBytesAndStatus = getBytesAndStatus(context, myId);

            if ((downloadBytesAndStatus[0] == downloadBytesAndStatus[1]) && downloadBytesAndStatus[2] == DownloadManager.STATUS_SUCCESSFUL && versionNumber != null) {
                // 标识已经下载完成
                UpdateManager.downloading = false;
                versionObj.put("status", 1);
                SharedHelp.setSharedPreferencesValue(SharedHelp.UPDATE_VERSION_PREFIX_KEY + versionNumber, versionObj.toString());
                Uri downloadUri = getFileUri(context);

                // 更新提示框
                if (mHandler != null) {
                    Message msg = new Message();
                    msg.what = UpdateManager.DownloadComplete;
                    mHandler.sendMessage(msg);
                }

                installApp(context, downloadUri);
            }
        } catch (JSONException e) {}
    }

    // 通过query查询下载状态，包括已下载数据大小，总大小，下载状态
    public static int[] getBytesAndStatus(Context context, long downloadId) {
        int[] bytesAndStatus = new int[]{
                -1, -1, 0
        };
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(context.DOWNLOAD_SERVICE);
        DownloadManager.Query query = new DownloadManager.Query().setFilterById(downloadId);
        Cursor cursor = null;
        try {
            cursor = downloadManager.query(query);
            if (cursor != null && cursor.moveToFirst()) {
                //已经下载文件大小
                bytesAndStatus[0] = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                //下载文件的总大小
                bytesAndStatus[1] = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                //下载状态
                bytesAndStatus[2] = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return bytesAndStatus;
    }

    // 获取下载文件uri
    public static Uri getFileUri(Context context) {
        Uri downloadUri = null;
        String versionJSON = SharedHelp.getSharedPreferencesValue(SharedHelp.UPDATE_VERSION_PREFIX_KEY + versionNumber);
        if (versionJSON == null || TextUtils.isEmpty(versionJSON)) return null;
        try {
            JSONObject versionObj = new JSONObject(versionJSON);
            long myId = versionObj.getLong("myId");
            String appFileName = versionObj.getString("fileName");
            DownloadManager downloadManager = (DownloadManager)
                    context.getSystemService(context.DOWNLOAD_SERVICE);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {            // 6.0以下
                downloadUri = downloadManager.getUriForDownloadedFile(myId);
            } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {     // 6.0 - 7.0
                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(myId);
                query.setFilterByStatus(DownloadManager.STATUS_SUCCESSFUL);
                Cursor cur = downloadManager.query(query);
                if (cur != null) {
                    if (cur.moveToFirst()) {
                        String uriString = cur.getString(cur.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                        if (!TextUtils.isEmpty(uriString)) {
                            downloadUri = Uri.fromFile(new File(Uri.parse(uriString).getPath()));
                        }
                    }
                    cur.close();
                }
            } else {    // 7.0以上
                downloadUri = FileProvider.getUriForFile(context,
                        context.getPackageName() + ".fileprovider",
                        new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), appFileName));
            }
        } catch (JSONException e) {}

        return downloadUri;
    }

    // 安装app申请权限
    public static void installApp(Context context, Uri downloadUri) {
        if (downloadUri == null) return;;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {           // 大于安卓8.0需要申请安装权限
            boolean hasInstallPermission = context.getPackageManager().canRequestPackageInstalls();
            if (!hasInstallPermission) {
                final AndroidOInstallPermissionListener listener = new AndroidOInstallPermissionListener() {
                    @Override
                    public void permissionSuccess() {
                        installAppEnd(context, downloadUri);
                    }

                    @Override
                    public void permissionFail() {
                        Toast.makeText(context, context.getString(R.string.auth_failure), Toast.LENGTH_SHORT).show();
                    }
                };

                AndroidOInstallPermissionActivity.iListener = listener;
                Intent requestInstallPermissionIntent = new Intent(context, AndroidOInstallPermissionActivity.class);
                requestInstallPermissionIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(requestInstallPermissionIntent);
            } else {
                installAppEnd(context, downloadUri);
            }
        } else {
            installAppEnd(context, downloadUri);
        }
    }

    // 最终执行安装
    private static void installAppEnd(Context context, Uri downloadUri) {
        if (downloadUri != null) {
            Intent intentInstall = new Intent();
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
                intentInstall.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }
            intentInstall.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intentInstall.setAction(Intent.ACTION_VIEW);
            intentInstall.setDataAndType(downloadUri, "application/vnd.android.package-archive");
            if (PluginInit.ACTIVITY != null && intentInstall.resolveActivity(context.getPackageManager()) != null) {
                PluginInit.ACTIVITY.startActivity(intentInstall);
            } else {

            }
        }
    }

    public interface AndroidOInstallPermissionListener {
        void permissionSuccess();

        void permissionFail();
    }
}

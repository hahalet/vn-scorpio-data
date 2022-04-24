package com.newstar.scorpiodata.utils;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.newstar.scorpiodata.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by qianxiaoai on 2016/7/7.
 */
public class PermissionUtils {
    public static PermissionUtils.PermissionGrant mPermissionGrant = new PermissionGrant() {
        @Override
        public void onPermissionGranted(int requestCode) {
            switch (requestCode) {
                case PermissionUtils.CODE_ACCESS_NETWORK_STATE:
                    Toast.makeText(PluginInit.ACTIVITY, "Result Permission Grant CODE_ACCESS_NETWORK_STATE", Toast.LENGTH_SHORT).show();
                    break;
                case PermissionUtils.CODE_ACCESS_WIFI_STATE:
                    Toast.makeText(PluginInit.ACTIVITY, "Result Permission Grant CODE_ACCESS_WIFI_STATE", Toast.LENGTH_SHORT).show();
                    break;
                case PermissionUtils.CODE_INTERNET:
                    Toast.makeText(PluginInit.ACTIVITY, "Result Permission Grant CODE_INTERNET", Toast.LENGTH_SHORT).show();
                    break;
                case PermissionUtils.CODE_READ_CONTACTS:
                    Toast.makeText(PluginInit.ACTIVITY, "Result Permission Grant CODE_READ_CONTACTS", Toast.LENGTH_SHORT).show();
                    break;
                case PermissionUtils.CODE_CAMERA:
                    Toast.makeText(PluginInit.ACTIVITY, "Result Permission Grant CODE_CAMERA", Toast.LENGTH_SHORT).show();
                    break;
                case PermissionUtils.CODE_READ_PHONE_STATE:
                    Toast.makeText(PluginInit.ACTIVITY, "Result Permission Grant CODE_READ_PHONE_STATE", Toast.LENGTH_SHORT).show();
                    break;
                case PermissionUtils.CODE_WRITE_EXTERNAL_STORAGE:
                    Toast.makeText(PluginInit.ACTIVITY, "Result Permission Grant CODE_WRITE_EXTERNAL_STORAGE", Toast.LENGTH_SHORT).show();
                    break;
                case PermissionUtils.CODE_READ_EXTERNAL_STORAGE:
                    Toast.makeText(PluginInit.ACTIVITY, "Result Permission Grant CODE_READ_EXTERNAL_STORAGE", Toast.LENGTH_SHORT).show();
                    break;
                case PermissionUtils.CODE_ACCESS_FINE_LOCATION:
                    Toast.makeText(PluginInit.ACTIVITY, "Result Permission Grant CODE_ACCESS_FINE_LOCATION", Toast.LENGTH_SHORT).show();
                    break;
                case PermissionUtils.CODE_READ_SMS:
                    Toast.makeText(PluginInit.ACTIVITY, "Result Permission Grant CODE_READ_SMS", Toast.LENGTH_SHORT).show();
                    break;
                case PermissionUtils.CODE_CALL_PHONE:
                    Toast.makeText(PluginInit.ACTIVITY, "Result Permission Grant CODE_CALL_PHONE", Toast.LENGTH_SHORT).show();
                    break;
                case PermissionUtils.CODE_READ_CALENDAR:
                    Toast.makeText(PluginInit.ACTIVITY, "Result Permission Grant CODE_READ_CALENDAR", Toast.LENGTH_SHORT).show();
                    break;
                case PermissionUtils.CODE_WRITE_CALENDAR:
                    Toast.makeText(PluginInit.ACTIVITY, "Result Permission Grant CODE_WRITE_CALENDAR", Toast.LENGTH_SHORT).show();
                    break;
                case PermissionUtils.CODE_ACCESS_COARSE_LOCATION:
                    Toast.makeText(PluginInit.ACTIVITY, "Result Permission Grant CODE_ACCESS_COARSE_LOCATION", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };

    private static final String TAG = PermissionUtils.class.getSimpleName();
    public static final int CODE_ACCESS_NETWORK_STATE = 0;
    public static final int CODE_ACCESS_WIFI_STATE = 1;
    public static final int CODE_INTERNET = 2;
    public static final int CODE_READ_CONTACTS = 3;
    public static final int CODE_CAMERA = 4;
    public static final int CODE_READ_PHONE_STATE = 5;
    public static final int CODE_WRITE_EXTERNAL_STORAGE = 6;
    public static final int CODE_READ_EXTERNAL_STORAGE = 7;
    public static final int CODE_ACCESS_FINE_LOCATION = 8;
    public static final int CODE_ACCESS_COARSE_LOCATION = 9;
    public static final int CODE_READ_SMS = 10;
    public static final int CODE_CALL_PHONE = 11;
    public static final int CODE_READ_CALENDAR = 12;
    public static final int CODE_WRITE_CALENDAR = 13;
    public static final int CODE_MULTI_PERMISSION = 100;

    public static final String PERMISSION_ACCESS_NETWORK_STATE = Manifest.permission.ACCESS_NETWORK_STATE;
    public static final String PERMISSION_ACCESS_WIFI_STATE = Manifest.permission.ACCESS_WIFI_STATE;
    public static final String PERMISSION_INTERNET = Manifest.permission.INTERNET;
    public static final String PERMISSION_READ_CONTACTS = Manifest.permission.READ_CONTACTS;
    public static final String PERMISSION_CAMERA = Manifest.permission.CAMERA;
    public static final String PERMISSION_READ_PHONE_STATE = Manifest.permission.READ_PHONE_STATE;
    public static final String PERMISSION_WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    public static final String PERMISSION_READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE;
    public static final String PERMISSION_ACCESS_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    public static final String PERMISSION_READ_SMS = Manifest.permission.READ_SMS;
    public static final String PERMISSION_CALL_PHONE = Manifest.permission.CALL_PHONE;
    public static final String PERMISSION_READ_CALENDAR = Manifest.permission.READ_CALENDAR;
    public static final String PERMISSION_WRITE_CALENDAR = Manifest.permission.WRITE_CALENDAR;

    public static String[] requestPermissions = {
            PERMISSION_ACCESS_NETWORK_STATE,
            PERMISSION_ACCESS_WIFI_STATE,
            PERMISSION_INTERNET,
            PERMISSION_READ_CONTACTS,
            PERMISSION_CAMERA,
            PERMISSION_READ_PHONE_STATE,
            PERMISSION_WRITE_EXTERNAL_STORAGE,
            PERMISSION_READ_EXTERNAL_STORAGE,
            PERMISSION_ACCESS_FINE_LOCATION,
            PERMISSION_READ_SMS,
            PERMISSION_CALL_PHONE,
            PERMISSION_READ_CALENDAR,
            PERMISSION_WRITE_CALENDAR
    };

    public static String[] requestCalendarPermissions = {
            PERMISSION_READ_CALENDAR,
            PERMISSION_WRITE_CALENDAR
    };

    private static Map<String, Integer> PERMISSIONS_HITS = new HashMap();

    static {
        PERMISSIONS_HITS.put(PERMISSION_READ_CONTACTS, R.string.permission_contacts_hint);
        PERMISSIONS_HITS.put(PERMISSION_CAMERA, R.string.permission_camera_hint);
        PERMISSIONS_HITS.put(PERMISSION_READ_PHONE_STATE, R.string.permission_phone_hint);
        PERMISSIONS_HITS.put(PERMISSION_WRITE_EXTERNAL_STORAGE, R.string.permission_storage_hint);
        PERMISSIONS_HITS.put(PERMISSION_ACCESS_FINE_LOCATION, R.string.permission_location_hint);
        PERMISSIONS_HITS.put(PERMISSION_READ_SMS, R.string.permission_sms_hint);
        PERMISSIONS_HITS.put(PERMISSION_CALL_PHONE, R.string.permission_call_phone_hint);
        PERMISSIONS_HITS.put(PERMISSION_READ_CALENDAR, R.string.permission_read_calendar_hint);
        PERMISSIONS_HITS.put(PERMISSION_WRITE_CALENDAR, R.string.permission_write_calendar_hint);
    }

    public interface PermissionGrant {
        void onPermissionGranted(int requestCode);
    }

    /**
     * Requests permission.
     *
     * @param activity
     * @param requestCode request code, e.g. if you need request CAMERA permission,parameters is PermissionUtils.CODE_CAMERA
     */
    public static void requestPermission(final Activity activity, final int requestCode, PermissionGrant permissionGrant) {
        if (activity == null) {
            return;
        }

        if (requestCode < 0 || requestCode >= requestPermissions.length) {
            return;
        }

        final String requestPermission = requestPermissions[requestCode];

        //如果是6.0以下的手机，ActivityCompat.checkSelfPermission()会始终等于PERMISSION_GRANTED，
        // 但是，如果用户关闭了你申请的权限，ActivityCompat.checkSelfPermission(),会导致程序崩溃(java.lang.RuntimeException: Unknown exception code: 1 msg null)，
        // 你可以使用try{}catch(){},处理异常，也可以在这个地方，低于23就什么都不做，
        // 个人建议try{}catch(){}单独处理，提示用户开启权限。
//        if (Build.VERSION.SDK_INT < 23) {
//            return;
//        }

        int checkSelfPermission;
        try {
            checkSelfPermission = ActivityCompat.checkSelfPermission(activity, requestPermission);
        } catch (RuntimeException e) {
            Toast.makeText(activity, "please open this permission", Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        if (checkSelfPermission != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(PluginInit.ACTIVITY, requestPermission)) {
                shouldShowRationale(requestCode, requestPermission);

            } else {
                ActivityCompat.requestPermissions(activity, new String[]{requestPermission}, requestCode);
            }

        } else {
            Toast.makeText(activity, "opened:" + requestPermissions[requestCode], Toast.LENGTH_SHORT).show();
            if (permissionGrant != null) {
                permissionGrant.onPermissionGranted(requestCode);
            }
        }
    }

    private static void requestMultiResult(Activity activity, String[] permissions, int[] grantResults, PermissionGrant permissionGrant) {

        if (activity == null) {
            return;
        }

        //TODO
        Map<String, Integer> perms = new HashMap<>();

        ArrayList<String> notGranted = new ArrayList<>();
        for (int i = 0; i < permissions.length; i++) {
            perms.put(permissions[i], grantResults[i]);
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                notGranted.add(permissions[i]);
            }
        }

        if (notGranted.size() == 0) {
            Toast.makeText(activity, "all permission success" + notGranted, Toast.LENGTH_SHORT)
                    .show();
            permissionGrant.onPermissionGranted(CODE_MULTI_PERMISSION);
        } else {
            openSettingActivity(activity, "those permission need granted!");
        }
    }

    private static String getPermissionsString(List<String> permissionsList) {
        String permissions = "";
        if (permissionsList != null && permissionsList.size() > 0) {
            for (String permission : permissionsList) {
                if (PERMISSIONS_HITS.get(permission) != null) {
                    permissions = permissions + PluginInit.ACTIVITY.getString(PERMISSIONS_HITS.get(permission));
                } else {
                }
            }
        }
        return permissions;
    }

    /**
     * 一次申请多个权限
     */
    public static int requestMultiPermissions(PermissionGrant grant,String[] requestPermissions) {
        final List<String> permissionsList = getNoGrantedPermission(PluginInit.ACTIVITY, false, requestPermissions);
        final List<String> shouldRationalePermissionsList = getNoGrantedPermission(PluginInit.ACTIVITY, true, requestPermissions);

        //TODO checkSelfPermission
        if (permissionsList == null || shouldRationalePermissionsList == null) {
            return 0;
        }

        if (permissionsList.size() > 0) {
            String showPermissions = SharedHelp.getSharedPreferencesValue(SharedHelp.SHOW_PERMISSINOS);
            SharedHelp.setSharedPreferencesValue(SharedHelp.SHOW_PERMISSINOS,"false");
            if(showPermissions == null){
                ActivityCompat.requestPermissions(PluginInit.ACTIVITY, permissionsList.toArray(new String[permissionsList.size()]),
                        CODE_MULTI_PERMISSION);
            }else{
                showMessageOKCancel(PluginInit.ACTIVITY.getString(R.string.shoud_open_permissions) + getPermissionsString(permissionsList),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                PluginInit.ACTIVITY.startActivity(getAppDetailSettingIntent());
                            }
                        });
            }
            return permissionsList.size();
        } else if (shouldRationalePermissionsList.size() > 0) {
            ActivityCompat.requestPermissions(PluginInit.ACTIVITY, shouldRationalePermissionsList.toArray(new String[shouldRationalePermissionsList.size()]),
                    CODE_MULTI_PERMISSION);
            return shouldRationalePermissionsList.size();
        } else {
            grant.onPermissionGranted(CODE_MULTI_PERMISSION);
            return 0;
        }
    }

    private static Intent getAppDetailSettingIntent() {
        Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
        localIntent.setData(Uri.fromParts("package", PluginInit.ACTIVITY.getPackageName(), null));
        return localIntent;
    }

    private static void shouldShowRationale(final int requestCode, final String requestPermission) {
        //TODO
        String[] permissionsHint = PluginInit.ACTIVITY.getResources().getStringArray(R.array.permissions);
        showMessageOKCancel("Rationale: " + permissionsHint[requestCode], new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ActivityCompat.requestPermissions(PluginInit.ACTIVITY,
                        new String[]{requestPermission},
                        requestCode);
            }
        });
    }

    private static void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(PluginInit.ACTIVITY)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();

    }

    /**
     * @param activity
     * @param requestCode  Need consistent with requestPermission
     * @param permissions
     * @param grantResults
     */
    public static void requestPermissionsResult(final Activity activity, final int requestCode, @NonNull String[] permissions,
                                                @NonNull int[] grantResults, PermissionGrant permissionGrant) {

        if (activity == null) {
            return;
        }

        if (requestCode == CODE_MULTI_PERMISSION) {
            requestMultiResult(activity, permissions, grantResults, permissionGrant);
            return;
        }

        if (requestCode < 0 || requestCode >= requestPermissions.length) {
            Toast.makeText(activity, "illegal requestCode:" + requestCode, Toast.LENGTH_SHORT).show();
            return;
        }

        if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //TODO success, do something, can use callback
            permissionGrant.onPermissionGranted(requestCode);

        } else {
            String[] permissionsHint = activity.getResources().getStringArray(R.array.permissions);
            openSettingActivity(activity, "Result" + permissionsHint[requestCode]);
        }

    }

    private static void openSettingActivity(final Activity activity, String message) {
        showMessageOKCancel(message, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                intent.setData(uri);
                activity.startActivity(intent);
            }
        });
    }


    /**
     * @param activity
     * @param isShouldRationale true: return no granted and shouldShowRequestPermissionRationale permissions, false:return no granted and !shouldShowRequestPermissionRationale
     * @return
     */
    public static ArrayList<String> getNoGrantedPermission(Activity activity, boolean isShouldRationale, String[] requestPermissions) {

        ArrayList<String> permissions = new ArrayList<>();

        for (int i = 0; i < requestPermissions.length; i++) {
            String requestPermission = requestPermissions[i];
            //TODO checkSelfPermission
            int checkSelfPermission = -1;
            try {
                checkSelfPermission = ActivityCompat.checkSelfPermission(activity, requestPermission);
            } catch (RuntimeException e) {
                Toast.makeText(activity, "please open those permission", Toast.LENGTH_SHORT)
                        .show();
                return null;
            }
            if (checkSelfPermission != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, requestPermission)) {
                    if (isShouldRationale) {
                        permissions.add(requestPermission);
                    }
                } else {
                    if (!isShouldRationale) {
                        permissions.add(requestPermission);
                    }
                }
            }
        }
        return permissions;
    }

    public static boolean checkPermission(String permission) {
        if (ContextCompat.checkSelfPermission(PluginInit.ACTIVITY, permission) == PackageManager.PERMISSION_GRANTED)
            return true;
        else
            return false;
    }
}
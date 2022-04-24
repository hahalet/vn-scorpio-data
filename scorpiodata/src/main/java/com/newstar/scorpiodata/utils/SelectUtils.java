package com.newstar.scorpiodata.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.MediaStore;

import androidx.core.content.FileProvider;

import com.newstar.scorpiodata.activitys.CameraActivity;

import java.io.File;

public class SelectUtils {
    public interface SelectResult {
        public void onSelect(int requestCode, int resultCode, Intent data);
    }

    private static SelectResult selectResult;

    public static void setSelectResult(SelectResult selectResult) {
        SelectUtils.selectResult = selectResult;
    }

    public static void selectOneContact(Activity activity, int requestCode) {
        if(PermissionUtils.checkPermission( Manifest.permission.READ_CONTACTS)){
            Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
            activity.startActivityForResult(intent, requestCode);
        }else{
            PermissionUtils.requestMultiPermissions(PermissionUtils.mPermissionGrant, PermissionUtils.requestPermissions);
        }
    }

    public static void liveness(Activity activity, int requestCode) {
        if(PermissionUtils.checkPermission(Manifest.permission.CAMERA)){
            try{
                Intent intent = new Intent(PluginInit.ACTIVITY, Class.forName("ai.advance.liveness.sdk.activity.LivenessActivity"));
                activity.startActivityForResult(intent, requestCode);
            }catch(Exception e){
                e.printStackTrace();
            }

        }else{
            PermissionUtils.requestMultiPermissions(PermissionUtils.mPermissionGrant, PermissionUtils.requestPermissions);
        }
    }

    /**
     * 拍照
     * @param activity
     * @param requestCode
     * @return
     */
    public static String selectOnePicture(Activity activity, int requestCode) {
        final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(PluginInit.APPLICATION.getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = PictureUtils.createImageFile(activity);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (photoFile != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    Uri photoURI = FileProvider.getUriForFile(PluginInit.APPLICATION,"com.mozadongpro.loan.android.fileprovider", photoFile);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                } else {
                    Uri uri = Uri.fromFile(photoFile);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                }
                activity.startActivityForResult(intent, requestCode);
                return photoFile.getAbsolutePath();
            }
        }
        return null;
    }

    /**
     * 选择一张图片
     * @param activity
     * @param requestCode
     * @return
     */
    public static Uri selectOnePicturePick(Activity activity, int requestCode) {
        final Intent intent = new Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        activity.startActivityForResult(intent, requestCode);
        return null;
    }


    /**
     * 拍照
     * @param activity
     * @param requestCode
     * @return
     */
    public static String selectOnePictureSelf(Activity activity, int requestCode) {
        Intent intent = new Intent(activity, CameraActivity.class);
        activity.startActivityForResult(intent, requestCode);
        return null;
    }

    public static void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (selectResult != null) {
            selectResult.onSelect(requestCode, resultCode, data);
        }
        setSelectResult(null);
    }
}

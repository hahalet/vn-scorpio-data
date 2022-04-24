package com.newstar.scorpiodata.utils;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtils {
 
    /**
     * 把图片压缩到200K
     * 
     * @param oldpath
     *            压缩前的图片路径
     * @param newPath
     *            压缩后的图片路径
     * @return
     */
    public static File compressFile(String oldpath, String newPath) {
        Bitmap compressBitmap = FileUtils.decodeFile(oldpath);
        Bitmap newBitmap = ratingImage(oldpath, compressBitmap);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        newBitmap.compress(CompressFormat.PNG, 100, os);
        byte[] bytes = os.toByteArray();
 
        File file = null ;
        try {
            file = FileUtils.getFileFromBytes(bytes, newPath);
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            if(newBitmap != null ){
                if(!newBitmap.isRecycled()){
                    newBitmap.recycle();
                }
                newBitmap  = null;
            }
            if(compressBitmap != null ){
                if(!compressBitmap.isRecycled()){
                    compressBitmap.recycle();
                }
                compressBitmap  = null;
            }
        }
        return file;
    }
 
    private static Bitmap ratingImage(String filePath,Bitmap bitmap){
        int degree = readPictureDegree(filePath);
        return rotaingImageView(degree, bitmap);
    }
 
    /**
     *  旋转图片
     * @param angle
     * @param bitmap
     * @return Bitmap
     */
    public static Bitmap rotaingImageView(int angle , Bitmap bitmap) {
        //旋转图片 动作
        Matrix matrix = new Matrix();;
        matrix.postRotate(angle);
        System.out.println("angle2=" + angle);
        // 创建新的图片
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return resizedBitmap;
    }
 
    /**
     * 读取图片属性：旋转的角度
     * @param path 图片绝对路径
     * @return degree旋转的角度
     */
    public static int readPictureDegree(String path) {
        int degree  = 0;
        try {
                ExifInterface exifInterface = new ExifInterface(path);
                int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                        degree = 90;
                        break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                        degree = 180;
                        break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                        degree = 270;
                        break;
                }
        } catch (IOException e) {
                e.printStackTrace();
        }
        return degree;
    }
 
    /**
     * 把字节数组保存为一个文件
     * 
     * @param b
     * @param outputFile
     * @return
     */
    public static File getFileFromBytes(byte[] b, String outputFile) {
        File ret = null;
        BufferedOutputStream stream = null;
        try {
            ret = new File(outputFile);
            FileOutputStream fstream = new FileOutputStream(ret);
            stream = new BufferedOutputStream(fstream);
            stream.write(b);
        } catch (Exception e) {
            // log.error("helper:get file from byte process error!");
            e.printStackTrace();
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    // log.error("helper:get file from byte process error!");
                    e.printStackTrace();
                }
            }
        }
        return ret;
    }
 
    /**
     * 图片压缩
     * 
     * @param fPath
     * @return
     */
    public static Bitmap decodeFile(String fPath) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        opts.inDither = false; // Disable Dithering mode
        opts.inPurgeable = true; // Tell to gc that whether it needs free
        opts.inInputShareable = true; // Which kind of reference will be used to
        BitmapFactory.decodeFile(fPath, opts);
        final int REQUIRED_SIZE = 200;
        int scale = 1;
        if (opts.outHeight > REQUIRED_SIZE || opts.outWidth > REQUIRED_SIZE) {
            final int heightRatio = Math.round((float) opts.outHeight
                    / (float) REQUIRED_SIZE);
            final int widthRatio = Math.round((float) opts.outWidth
                    / (float) REQUIRED_SIZE);
            scale = heightRatio < widthRatio ? heightRatio : widthRatio;//
        }
        opts.inJustDecodeBounds = false;
        opts.inSampleSize = scale;
        Bitmap bm = BitmapFactory.decodeFile(fPath, opts).copy(Config.ARGB_8888, false);
        return bm;
    }
    /**
     * 创建目录
     * @param path
     */
    public static void setMkdir(String path)
    {
        File file = new File(path);
        if(!file.exists())
        {
            file.mkdirs();
        }else{
        }
    }
    /**
     * 获取目录名称
     * @param url
     * @return FileName
     */
    public static String getFileName(String url)
    {
        int lastIndexStart = url.lastIndexOf("/");
        if(lastIndexStart!=-1)
        {
            return url.substring(lastIndexStart+1, url.length());
        }else{
            return null;
        }
    }
    /**
     * 删除该目录下的文件
     * 
     * @param path
     */
    public static void delFile(String path) {
        if (!TextUtils.isEmpty(path)) {
            File file = new File(path);
            if (file.exists()) {
                file.delete();
            }
        }
    }

    /**
     * 根据Uri获取图片文件的绝对路径
     */
    public static String getRealFilePath(final Uri uri) {
        if (null == uri) {
            return null;
        }
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = PluginInit.ACTIVITY.getContentResolver().query(uri,
                    new String[] { MediaStore.Images.ImageColumns.DATA }, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }

    public static String getText(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }

        try {
            return getText(new FileInputStream(file));
        } catch (FileNotFoundException e) {
        }

        return null;
    }

    public static String getText(InputStream inputStream) {
        byte[] data = null;
        try {
            ByteArrayOutputStream builder = new ByteArrayOutputStream();
            byte[] buff = new byte[100];
            int rc = 0;
            while ((rc = inputStream.read(buff,0,100)) >0) {
                builder.write(buff,0,rc);
            }
            data = builder.toByteArray();
            return new String(Base64.encode(data,Base64.DEFAULT));
        } catch (IOException e) {
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                }
            }
        }

        return null;
    }
}
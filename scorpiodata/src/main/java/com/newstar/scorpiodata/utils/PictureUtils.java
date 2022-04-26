package com.newstar.scorpiodata.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Base64;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PictureUtils {
    //图片最大宽度
    private static final int MAX_WIDTH = 1900;
    // 压缩图片
    public static final void compressBitmap(Activity activity, String srcPath, long sizeLimit, Callback<File, Exception> callback) throws Exception {
        if (srcPath == null || TextUtils.isEmpty(srcPath)) {
            throw new Exception("photoSrcPath is null");
        }

        if (!fileIsExists(srcPath)) {
            throw new Exception("file dosen't exists");
        }
        CompressImageTask task = new CompressImageTask(activity, srcPath, sizeLimit, callback);
        task.execute();
    }

    public static class CompressImageTask extends AsyncTask<Void, Integer, Integer> {
        private Activity activity;
        private String srcPath;
        private Long sizeLimit;
        private Callback<File, Exception> callback;
        private File targetFile;
        private WeakReference<Bitmap> imageReference;
        private Boolean isSuccess = true;
        private BitmapFactory.Options bmOptions;

        CompressImageTask(Activity activity, String srcPath, Long sizeLimit, Callback<File, Exception> callback) {
            this.activity = activity;
            this.srcPath = srcPath;
            this.sizeLimit = sizeLimit;
            this.callback = callback;

            try {
                targetFile = createImageFile(activity);
            } catch (Exception e) {
                callback.reject(e);
                isSuccess = false;
            }
        }

        @Override
        protected Integer doInBackground(Void... params) {
            if (!isSuccess) {
                return null;
            }

            // 获取图片选择角度
            int degree = getBitmapDegree(srcPath);
            // 图片转换成Bitmap
            try {
                bmOptions = new BitmapFactory.Options();
                bmOptions.inPreferredConfig = Bitmap.Config.RGB_565;
                // 采样率  采样后的图片的宽高均为原始图片宽高的1/2 占用内存减少2倍
                bmOptions.inSampleSize = 1;
                // 设置为true，不将图片解码到内存中
                bmOptions.inJustDecodeBounds = true;
                // 第一次解析图片为了获得图片尺寸
                BitmapFactory.decodeFile(srcPath, bmOptions);
                while (bmOptions.outWidth/bmOptions.inSampleSize>MAX_WIDTH) {
                    bmOptions.inSampleSize *= 2;
                }
                // 最后要设置成false
                bmOptions.inJustDecodeBounds = false;
                imageReference = new WeakReference<Bitmap>(BitmapFactory.decodeFile(srcPath, bmOptions));
            } catch (OutOfMemoryError me) {
                try {
                    bmOptions = new BitmapFactory.Options();
                    // 是否低内存运行
                    if (MemoryUtils.isLowMem()) {
                        // 采用RGB_565每像素占用16byte内存 占用内存减少了2倍
                        bmOptions.inPreferredConfig = Bitmap.Config.RGB_565;
                    } else {
                        // 色彩模式 ARGB_8888每个像素占32byte
                        bmOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    }
                    // 采样率  采样后的图片的宽高均为原始图片宽高的1/2 占用内存减少2倍
                    bmOptions.inSampleSize = 1;
                    // 设置为true，不将图片解码到内存中
                    bmOptions.inJustDecodeBounds = true;
                    // 第一次解析图片为了获得图片尺寸
                    BitmapFactory.decodeFile(srcPath, bmOptions);
                    // 图片宽度
                    int imageWidth = bmOptions.outWidth;
                    // 图片高度
                    int imageHeight = bmOptions.outHeight;
                    // 获取图片需要占用的内存大小
                    long imageMem = getImageOccupyMem(imageWidth, imageHeight, bmOptions);
                    // 判断内存是否充足
                    boolean isEnough = MemoryUtils.isMemEnough(imageMem);
                    // 如果内存不足 修改色彩模式和反复降低采样率 直到内存够为止
                    while (isEnough == false) {
                        bmOptions.inPreferredConfig = Bitmap.Config.RGB_565;
                        bmOptions.inSampleSize *= 2;
                        isEnough = MemoryUtils.isMemEnough(getImageOccupyMem(imageWidth, imageHeight, bmOptions));
                    }
                    // 最后要设置成false
                    bmOptions.inJustDecodeBounds = false;
                    imageReference = new WeakReference<Bitmap>(BitmapFactory.decodeFile(srcPath, bmOptions));
                } catch (Exception e) {
                    callback.reject(e);
                    isSuccess = false;
                    return null;
                }
            }

            if (imageReference != null && imageReference.get() != null) {
                try {
                    // 旋转图片
                    imageReference = rotateBitmapByDegree(imageReference, degree);

                    if (imageReference != null && imageReference.get() != null) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        int quality = 100;
                        imageReference.get().compress(Bitmap.CompressFormat.JPEG, quality, baos);// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中

                        while (baos.toByteArray().length / 1024 > sizeLimit && quality > 10) {
                            baos.reset(); // 重置baos即清空baos
                            imageReference.get().compress(Bitmap.CompressFormat.JPEG, quality, baos);// 这里压缩options%，把压缩后的数据存放到baos中
                            quality -= 10;// 每次都减少10
                        }

                        // 释放bitmap
                        if (imageReference.get() != null && !imageReference.get().isRecycled()) {
                            imageReference.get().recycle();
                        }

                        // 把压缩后的图片数据写入新的文件中
                        baos.writeTo(new BufferedOutputStream(new FileOutputStream(targetFile)));
                        baos.close();

                        // 删除源文件
                        //File srcFile = new File(srcPath);
                        //srcFile.delete();

                        return null;
                    } else {
                        callback.reject(new Exception("rotate image failure"));
                        isSuccess = false;
                        return null;
                    }
                } catch (IOException e) {
                    callback.reject(e);
                    isSuccess = false;
                    return null;
                }
            } else {
                callback.reject(new Exception("decode file failure"));
                isSuccess = false;
                return null;
            }
        }

        @Override
        protected void onPostExecute(Integer integer) {
            if (isSuccess) {
                callback.resolve(targetFile);
            }
        }
    }

    //判断文件是否存在
    public static final boolean fileIsExists(String strFile) {
        try {
            File f = new File(strFile);
            if (!f.exists()) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    // 创建图片文件
    public static final File createImageFile(Activity activity) throws IOException, Exception {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_tmp";
        if (PermissionUtils.checkPermission( Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                File storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                File imageFile = new File(storageDir, imageFileName + ".png");
                return imageFile;
            } else {
                File cacheDir = activity.getCacheDir();
                File imageFile = new File(cacheDir, imageFileName + ".png");
                return imageFile;
            }
        } else {
            // 申请写权限
            PermissionUtils.requestPermission(activity, PermissionUtils.CODE_WRITE_EXTERNAL_STORAGE, null);
            throw new Exception("Permission denied");
        }
    }

    // 获取图片旋转角度
    public static final int getBitmapDegree(String path) {
        int degree = 0;//被旋转的角度
        try {
            // 从指定路径下读取图片，并获取其EXIF信息
            ExifInterface exifInterface = new ExifInterface(path);
            // 获取图片的旋转信息
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
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

    // 旋转图片
    public static final WeakReference<Bitmap> rotateBitmapByDegree(WeakReference<Bitmap> bmReference, int degree) {
        try {
            // 根据旋转角度，生成旋转矩阵
            Matrix matrix = new Matrix();
            matrix.postRotate(degree);
            WeakReference<Bitmap> newBmReference;

            try {
                // 将原始图片按照旋转矩阵进行旋转，并得到新的图片
                newBmReference = new WeakReference<Bitmap>(Bitmap.createBitmap(bmReference.get(), 0, 0, bmReference.get().getWidth(), bmReference.get().getHeight(), matrix, true));
            } catch (OutOfMemoryError e) {
                throw e;
            }

            if (bmReference.get() != null && !bmReference.get().isRecycled() && bmReference.get() != newBmReference.get()) {
                bmReference.get().recycle();
            }

            return newBmReference;
        } catch (Exception ignore) {}
        return bmReference;
    }

    // 判断图片解析占用内存
    public static final long getImageOccupyMem(int width, int height, BitmapFactory.Options options) {
        int inSampleSize = options.inSampleSize;
        Bitmap.Config inPreferredConfig = options.inPreferredConfig;
        // 每个像素占用字节
        int perPixBytes = 1;

        switch (inPreferredConfig) {
            case ARGB_8888:
                perPixBytes *= 32;
                break;
            case ARGB_4444:
                perPixBytes *= 16;
                break;
            case RGB_565:
                perPixBytes *= 16;
                break;
            case ALPHA_8:
                perPixBytes *= 4;
                break;
        }

        if (inSampleSize == 0) {
            inSampleSize = 1;
        }
        if (inSampleSize > 1 && inSampleSize % 2 == 0) {
            inSampleSize = inSampleSize - (inSampleSize % 2);
        }

        width = width / inSampleSize;
        height = height / inSampleSize;

        return width * height * perPixBytes;
    }

    /**
     * Save Bitmap
     *
     * @param name file name
     * @param bm  picture to save
     */
    public static String saveBitmap(String name, Bitmap bm, Context mContext) {
        //指定我们想要存储文件的地址
        String TargetPath = mContext.getFilesDir() + "/images/";
        //判断指定文件夹的路径是否存在
        if (!fileIsExist(TargetPath)) {
        } else {
            //如果指定文件夹创建成功，那么我们则需要进行图片存储操作
            File saveFile = new File(TargetPath, name);
            try {
                FileOutputStream saveImgOut = new FileOutputStream(saveFile);
                // compress - 压缩的意思
                bm.compress(Bitmap.CompressFormat.JPEG, 80, saveImgOut);
                //存储完成后需要清除相关的进程
                saveImgOut.flush();
                saveImgOut.close();
                return saveFile.getAbsolutePath();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }
    static boolean fileIsExist(String fileName)
    {
        //传入指定的路径，然后判断路径是否存在
        File file=new File(fileName);
        if (file.exists())
            return true;
        else{
            //file.mkdirs() 创建文件夹的意思
            return file.mkdirs();
        }
    }


    public static String getText(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        InputStream inputStream = new ByteArrayInputStream(baos.toByteArray());
        try {
            return getText(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
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
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    public static String getText(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }

        try {
            return getText(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }
}

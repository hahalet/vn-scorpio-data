package com.newstar.scorpiodata.activitys;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.newstar.scorpiodata.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CameraActivity extends AppCompatActivity implements View.OnClickListener {
    //摄像头类型0前置,1后置
    public static int cameraType = 1;
    public static final String PHOTO_PATH = "photo_path";
    public static final String CAMERA_TYPE = "camera_type";
    public static final String CAMERA_FRONT = "camera_front";
    public static final String CAMERA_BACK = "camera_back";
    private Executor executor = Executors.newSingleThreadExecutor();
    private int REQUEST_CODE_PERMISSIONS = 1001;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"};

    PreviewView mPreviewView;
    private FrameLayout tools;
    ImageView captureImage;
    ImageView back;
    ImageView swap_camera;
    private FrameLayout preview_frame;
    private ImageView preview_image;
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);// 隐藏标题
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        getWindow().getDecorView().setSystemUiVisibility(uiOptions);
        //隐藏标题栏
        if (this instanceof AppCompatActivity && getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        //专门设置一下状态栏导航栏背景颜色为透明，凸显效果。
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        //activity.getWindow().setNavigationBarColor(activity.getResources().getColor(R.color.white));
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_main);

        cameraSelectorIndex = cameraType;
        mPreviewView = findViewById(R.id.previewView);
        tools = findViewById(R.id.tools);
        captureImage = findViewById(R.id.captureImg);
        swap_camera = findViewById(R.id.swap_camera);
        back = findViewById(R.id.back);
        swap_camera.setOnClickListener(this);
        back.setOnClickListener(this);
        preview_frame = findViewById(R.id.preview_view);
        preview_image = findViewById(R.id.preview_image);
        preview_image.setOnTouchListener((v, event) -> true);
        ImageView cancel = findViewById(R.id.cancel);
        ImageView ok = findViewById(R.id.ok);
        cancel.setOnClickListener(this);
        ok.setOnClickListener(this);
        if(allPermissionsGranted()){
            startCamera(); //start camera if permission has been granted by user
        } else{
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }
    ProcessCameraProvider cameraProvider;
    private void startCamera() {

        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void run() {
                try {
                    if(cameraProvider!=null){
                        cameraProvider.unbindAll();
                    }else{
                        cameraProvider = cameraProviderFuture.get();
                    }
                    bindPreview(cameraProvider);
                } catch (ExecutionException | InterruptedException e) {
                    // No errors need to be handled for this Future.
                    // This should never be reached.
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {

        Preview preview = new Preview.Builder()
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(cameraSelectorIndex)
                .build();
        //设置宽高比
        //设置实际的尺寸
        int rotation = this.getWindowManager().getDefaultDisplay().getRotation();
        int width = 900;
        int height = 1200;
        if(rotation== Surface.ROTATION_0 || rotation== Surface.ROTATION_180){
            width = 900;
            height = 1200;
        }else{
            width = 1200;
            height = 900;
        }
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                //.setTargetResolution(new android.util.Size(width, height))
                .build();

        ImageCapture.Builder builder = new ImageCapture.Builder();

        final ImageCapture imageCapture = builder
                .setTargetRotation(this.getWindowManager().getDefaultDisplay().getRotation())
                .build();

        preview.setSurfaceProvider(mPreviewView.getSurfaceProvider());

        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis, imageCapture);
        captureImage.setOnClickListener(v -> {
            SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
            File file = new File(getBatchDirectoryName(), mDateFormat.format(new Date())+ ".jpg");

            ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(file).build();
            imageCapture.takePicture(outputFileOptions, executor, new ImageCapture.OnImageSavedCallback () {
                @Override
                public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            path = file.getAbsolutePath();
                            tools.setVisibility(View.GONE);
                            preview_frame.setVisibility(View.VISIBLE);
                            cameraProvider.unbindAll();
                            preview_image.setImageDrawable(BitmapDrawable.createFromPath(path));
                        	//handler.sendEmptyMessage(0);
						}
                    });
                }
                @Override
                public void onError(@NonNull ImageCaptureException error) {
                    error.printStackTrace();
                }
            });
        });
    }

    public String getBatchDirectoryName() {
        String app_folder_path = "";
        app_folder_path = getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath()+ "/images";
        File dir = new File(app_folder_path);
        if (!dir.exists() && !dir.mkdirs()) {

        }

        return app_folder_path;
    }

    private boolean allPermissionsGranted(){

        for(String permission : REQUIRED_PERMISSIONS){
            if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                this.finish();
            }
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.back) {
            finish();
        } else if (id == R.id.swap_camera) {
            toggleFrontBackCamera();
        } else if (id == R.id.ok) {
            Intent intent = new Intent();
            //把返回数据存入Intent
            intent.putExtra(PHOTO_PATH, path);
            //设置返回数据
            setResult(RESULT_OK, intent);
            //关闭Activity
            finish();
        } else if (id == R.id.cancel) {
            startCamera();
            tools.setVisibility(View.VISIBLE);
            preview_frame.setVisibility(View.GONE);
        }

    }

    int cameraSelectorIndex = CameraSelector.LENS_FACING_BACK;
    private void toggleFrontBackCamera() {
        if (cameraSelectorIndex == CameraSelector.LENS_FACING_FRONT) {
            cameraSelectorIndex = CameraSelector.LENS_FACING_BACK;
        }else if (cameraSelectorIndex == CameraSelector.LENS_FACING_BACK) {
            cameraSelectorIndex = CameraSelector.LENS_FACING_FRONT;
        }
        startCamera();
    }

    String path;
}

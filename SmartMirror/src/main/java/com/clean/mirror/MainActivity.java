package com.clean.mirror;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImage.OnPictureSavedListener;
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

import com.clean.mirror.utils.PermissionUtils;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.clean.mirror.utils.CameraHelper;
import com.clean.mirror.utils.GPUImageTools;
import com.clean.mirror.utils.GPUImageFilterTools.FilterAdjuster;
import com.clean.mirror.utils.GPUImageTools.FilterType;
import com.clean.mirror.view.VerticalSeekBar;

@SuppressWarnings("deprecation")
public class MainActivity extends AppCompatActivity implements
        OnSeekBarChangeListener, OnClickListener ,Camera.AutoFocusCallback{
    private GLSurfaceView glSurfaceView;
    private GPUImage mGPUImage;
    private static Camera mCamera;
    private CameraHelper mHelper;
    private int mCurrentCameraId = 1;
    private GPUImageFilter mFilter;
    private VerticalSeekBar vSeekBar;
    private SeekBar mSeekBar;
    private Parameters parameters;

    private Button lj_bt, pz_bt, fz_bt;
    private Button nor_bt, invert_bt, hue_bt, sepia_bt, gray_bt, shar_bt,
            sobel_bt, convo_bt, emboss_bt, posterize_bt, group_bt, mono_bt;
    private Button remember_bt;

    private ImageView jk_img;


    public static final int MEDIA_TYPE_IMAGE = 1;
    private float start_x;
    private float start_y;
    private float stop_x;
    private float stop_y;

    private int hProgress = 480;
    private int vprogress;
    private int jk = 0;
    private static boolean flipHorizontal = true;
    private static boolean seekbarisVisible = true;
    private static boolean flag = false;
    private static boolean scrollViewisVisible = false;
    private Timer timer = null;
    private Handler handler = new Handler();

    private HorizontalScrollView scrollView;

    private ViewGroup advg;
    private AdView mAdView;

    private SharedPreferences sp;

    private PowerManager powerManager;

    private WakeLock wakeLock;

    private boolean iscomment = false;

    private ImageView block_img;

    private static final int REQUEST_PERMISSION_CAMERA_CODE = 1;
    private static final int REQUEST_PERMISSION_WRITE_CODE = 2;

    private static boolean isRequestPermission = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//		initAppsflyer();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setScreenBrightness(1.0f);
        requestPermission(Manifest.permission.CAMERA);
    }

    private void onCreate() {
        setContentView(R.layout.activity_main);
        initView();
        initOnClickListener();
        initCamera();
        loadMobAd();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initOnClickListener() {
        timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                handler.post(new Runnable() {

                    public void run() {
                        // TODO Auto-generated method stub
                        mSeekBar.setVisibility(View.GONE);
                        vSeekBar.setVisibility(View.GONE);
                        block_img.setVisibility(View.GONE);
                        seekbarisVisible = false;
                    }
                });
            }
        }, 5000);
        lj_bt.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (scrollViewisVisible) {
                    scrollView.setVisibility(View.GONE);
                    pz_bt.setVisibility(View.VISIBLE);
                    fz_bt.setVisibility(View.VISIBLE);
                    scrollViewisVisible = false;
                } else {
                    scrollView.setVisibility(View.VISIBLE);
                    pz_bt.setVisibility(View.GONE);
                    fz_bt.setVisibility(View.GONE);
                    scrollViewisVisible = true;
                }

            }
        });

        pz_bt.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub
                if(PermissionUtils.isOwnPermisson(MainActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                    p();
                }else {
                    Toast.makeText(MainActivity.this, "文件操作权限未被允许，无法进行拍照", Toast.LENGTH_SHORT).show();
                }

            }
        });
        fz_bt.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                mCamera.stopPreview();
                mCamera.setPreviewCallback(null);
                mCamera.release();
                mCamera = null;

                mCamera = Camera.open(mCurrentCameraId);
                if (flipHorizontal) {
                    flipHorizontal = false;
                } else {
                    flipHorizontal = true;
                }
                setUpCamera();
                glSurfaceView.requestRender();
            }
        });

        block_img.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (timer != null) {
                    timer.cancel();
                    timer = null;
                }
                switch (jk) {
                    case 0:
                        jk = R.drawable.jk1;
                        break;
                    case R.drawable.jk1:
                        jk = R.drawable.jk2;
                        break;
                    case R.drawable.jk2:
                        jk = R.drawable.jk3;
                        break;
                    case R.drawable.jk3:
                        jk = R.drawable.jk4;
                        break;
                    case R.drawable.jk4:
                        jk = R.drawable.jk5;
                        break;
                    case R.drawable.jk5:
                        jk = 0;
                        break;

                    default:
                        break;
                }

                if (jk == 0) {
                    jk_img.setBackground(null);
                } else {
                    jk_img.setBackgroundResource(jk);
                }
                timer = new Timer();
                timer.schedule(new TimerTask() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        handler.post(new Runnable() {

                            public void run() {
                                // TODO Auto-generated method stub
                                mSeekBar.setVisibility(View.GONE);
                                vSeekBar.setVisibility(View.GONE);
                                block_img.setVisibility(View.GONE);
                                seekbarisVisible = false;
                            }
                        });
                    }
                }, 5000);
            }
        });
        glSurfaceView.setOnTouchListener(new View.OnTouchListener() {

            @SuppressLint("ClickableViewAccessibility")
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (timer != null) {
                            timer.cancel();
                            timer = null;
                        }
                        if (!seekbarisVisible) {
                            flag = false;
                            mSeekBar.setVisibility(View.VISIBLE);
                            vSeekBar.setVisibility(View.VISIBLE);
                            block_img.setVisibility(View.VISIBLE);
                            seekbarisVisible = true;
                        } else {
                            flag = true;
                        }

                        start_x = event.getX();
                        start_y = event.getY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        stop_x = event.getX();
                        stop_y = event.getY();
                        float dx = stop_x - start_x;
                        float dy = stop_y - start_y;
                        float absdx = Math.abs(dx);
                        float absdy = Math.abs(dy);

                        if (absdx >= 0.4f || absdy >= 0.4f) {
                            flag = false;
                            if (absdx > absdy) {
                                hProgress = (int) (hProgress + dx);
                                mSeekBar.setProgress(hProgress);

                            } else {
                                vprogress = (int) (vprogress - dy);
                                vSeekBar.setMoveProgress(vprogress);

                            }
                            start_x = stop_x;
                            start_y = stop_y;
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        if (!flag) {
                            timer = new Timer();
                            timer.schedule(new TimerTask() {

                                @Override
                                public void run() {
                                    // TODO Auto-generated method stub
                                    handler.post(new Runnable() {

                                        public void run() {
                                            // TODO Auto-generated method stub
                                            mSeekBar.setVisibility(View.GONE);
                                            vSeekBar.setVisibility(View.GONE);
                                            block_img.setVisibility(View.GONE);
                                            seekbarisVisible = false;
                                        }
                                    });
                                }
                            }, 5000);
                        } else {
                            mSeekBar.setVisibility(View.GONE);
                            vSeekBar.setVisibility(View.GONE);
                            block_img.setVisibility(View.GONE);
                            seekbarisVisible = false;
                        }
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
    }

    private void p() {
        if (mCamera.getParameters().getFocusMode()
                .equals(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            takePicture();
        } else {
            mCamera.autoFocus(this);
        }
    }

    private void requestPermission(String permission) {
        if (PermissionUtils.isOwnPermisson(this, permission)) {
            if(TextUtils.equals(Manifest.permission.CAMERA,permission)){
                onCreate();
            }else {
//                p();
            }
        } else {
            isRequestPermission = true;
            if(TextUtils.equals(Manifest.permission.CAMERA,permission)){
                PermissionUtils.requestPermission(this, permission, REQUEST_PERMISSION_CAMERA_CODE);
            }else {
                PermissionUtils.requestPermission(this, permission, REQUEST_PERMISSION_WRITE_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == REQUEST_PERMISSION_CAMERA_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                onCreate();
            } else {
                Toast.makeText(this, "摄像头权限未被允许", Toast.LENGTH_SHORT).show();
                finish();
                System.exit(0);
            }
        } else if(requestCode == REQUEST_PERMISSION_WRITE_CODE){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                p();
            } else {
                Toast.makeText(this, "文件操作权限未被允许，将无法进行拍照", Toast.LENGTH_SHORT).show();
            }
        }else {
            Toast.makeText(this, "权限请求失败", Toast.LENGTH_SHORT).show();
            finish();
            System.exit(0);
        }
    }

    private void loadMobAd() {
        // TODO Auto-generated method stub
        mAdView = new AdView(this);
        mAdView.setAdSize(AdSize.SMART_BANNER);
        mAdView.setAdUnitId("ca-app-pub-9995113056490612/7413201418");
        mAdView.setAdListener(new AdListener() {

            @Override
            public void onAdClosed() {
                // TODO Auto-generated method stub
                super.onAdClosed();
                advg.setVisibility(View.GONE);
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // TODO Auto-generated method stub
                super.onAdFailedToLoad(errorCode);
            }

            @Override
            public void onAdLeftApplication() {
                // TODO Auto-generated method stub
                super.onAdLeftApplication();
            }

            @Override
            public void onAdLoaded() {
                // TODO Auto-generated method stub
                super.onAdLoaded();
                advg.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAdOpened() {
                // TODO Auto-generated method stub
                super.onAdOpened();
            }

        });

        AdRequest ar = new AdRequest.Builder().build();
        mAdView.loadAd(ar);
        advg.addView(mAdView);
    }

    protected void takePicture() {
        // TODO Auto-generated method stub
        if (flipHorizontal) {
            parameters.setRotation(90);
        } else {
            parameters.setRotation(270);
        }
        mCamera.setParameters(parameters);
        mCamera.takePicture(null, null, new A());
    }

    private void initView() {
        // TODO Auto-generated method stub
        mGPUImage = new GPUImage(this);
        mHelper = new CameraHelper(this);
        glSurfaceView = findViewById(R.id.surfaceview);
        mGPUImage.setGLSurfaceView(glSurfaceView);
        mSeekBar = findViewById(R.id.seekbar);
        mSeekBar.setOnSeekBarChangeListener(this);
        vSeekBar = findViewById(R.id.verticalSeekBar);
        vSeekBar.setOnSeekBarChangeListener(this);
        jk_img = findViewById(R.id.jk_img);
        lj_bt = findViewById(R.id.img_lj);
        fz_bt = findViewById(R.id.img_fz);
        pz_bt = findViewById(R.id.img_pz);
        nor_bt = findViewById(R.id.normal);
        nor_bt.setOnClickListener(this);
        invert_bt = findViewById(R.id.invert);
        invert_bt.setOnClickListener(this);
        hue_bt = findViewById(R.id.hue);
        hue_bt.setOnClickListener(this);
        sepia_bt = findViewById(R.id.sepia);
        sepia_bt.setOnClickListener(this);
        gray_bt = findViewById(R.id.gray);
        gray_bt.setOnClickListener(this);
        shar_bt = findViewById(R.id.shar);
        shar_bt.setOnClickListener(this);
        sobel_bt = findViewById(R.id.sobel);
        sobel_bt.setOnClickListener(this);
        convo_bt = findViewById(R.id.convolution);
        convo_bt.setOnClickListener(this);
        emboss_bt = findViewById(R.id.emboss);
        emboss_bt.setOnClickListener(this);
        posterize_bt = findViewById(R.id.postersize);
        posterize_bt.setOnClickListener(this);
        group_bt = findViewById(R.id.grouped);
        group_bt.setOnClickListener(this);
        mono_bt = findViewById(R.id.monochrome);
        mono_bt.setOnClickListener(this);
        remember_bt = new Button(this);
        scrollView = findViewById(R.id.scroll);
        block_img = findViewById(R.id.block);
        advg = findViewById(R.id.ad_fra);
    }

    @SuppressLint("InvalidWakeLockTag")
    private void initCamera() {
        sp = getSharedPreferences("Mirror", Activity.MODE_PRIVATE);
        jk = sp.getInt("jk_img", 0);
        iscomment = sp.getBoolean("iscomment", iscomment);
        if (jk != 0) {
            jk_img.setBackgroundResource(jk);
        }
        mCamera = Camera.open(mCurrentCameraId);
        setUpCamera();
        powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK
                | PowerManager.ON_AFTER_RELEASE, "My lOCK");
        wakeLock.acquire();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if(mCamera==null){
            mCamera=Camera.open(mCurrentCameraId);
        }
        setUpCamera();
    }

    @SuppressLint("InlinedApi")
    private void setUpCamera() {
        // TODO Auto-generated method stub
        parameters = mCamera.getParameters();
        vprogress = sp.getInt("vprogress", 0);
        Log.e("TGA", "v=" + vprogress);
        vSeekBar.setProgress(vprogress);
        parameters.setZoom(vprogress / 10);

        hProgress = sp.getInt("hProgress", 480);
        mSeekBar.setProgress(hProgress);
        Log.e("TGA", "h=" + ((hProgress / 4 - 120) / 10));
        parameters.setExposureCompensation((hProgress / 4 - 120) / 10);

        if (parameters.getSupportedFocusModes().contains(
                Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters
                    .setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }

        mCamera.setParameters(parameters);
        int orientation = mHelper.getCameraDisplayOrientation(this,
                mCurrentCameraId);
        mGPUImage.setUpCamera(mCamera, orientation, flipHorizontal, false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isRequestPermission) {
            return;
        }
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }

    protected void switchFilterTo(GPUImageFilter filter) {
        // TODO Auto-generated method stub
        if (mFilter == null
                || (filter != null && !mFilter.getClass().equals(
                filter.getClass()))) {
            mFilter = filter;
            mGPUImage.setFilter(mFilter);
            new FilterAdjuster(mFilter);
        }
    }

    public void onProgressChanged(SeekBar seekBar, int progress,
                                  boolean fromUser) {
        if (seekBar instanceof VerticalSeekBar) {
            vprogress = seekBar.getProgress();
            if (vprogress > 900) {
                // Toast.makeText(this, "It can't be enlarged anymore!",
                // Toast.LENGTH_SHORT).show();
            } else if (vprogress < 0) {
                // Toast.makeText(this, "It can't be reduced anymore!",
                // Toast.LENGTH_SHORT).show();
            } else {
                setCameraZoom(vprogress);
            }
            // vSeekBar.setProgress(vprogress);
            // if(){}
            // vSeekBar.setThumbOffset(90);
        } else {
            // SetLightness(this, progress);
            hProgress = seekBar.getProgress();
            if (hProgress >= 960) {
                // Toast.makeText(this, "It can't be any brighter than this!",
                // Toast.LENGTH_SHORT).show();
            } else if (hProgress <= 0) {
                // Toast.makeText(this, "It can't be any darker than this!",
                // Toast.LENGTH_SHORT).show();
            } else {
                // setScreenBrightness((float)hProgress/255f);
                setCamerExpose(hProgress / 4 - 120);
            }

        }
    }

    public void onStartTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub
        // Log.e("TGA", "START");
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public void onStopTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub
        // Log.e("TGA", "STOP");
        timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                handler.post(new Runnable() {

                    public void run() {
                        // TODO Auto-generated method stub
                        mSeekBar.setVisibility(View.GONE);
                        vSeekBar.setVisibility(View.GONE);
                        seekbarisVisible = false;
                    }
                });
            }
        }, 5000);
    }

    private void setScreenBrightness(float num) {
        try {
            WindowManager.LayoutParams layoutParams = super.getWindow()
                    .getAttributes();
            layoutParams.screenBrightness = num;
            getWindow().setAttributes(layoutParams);
        } catch (Exception e) {
            // TODO: handle exception
            Toast.makeText(MainActivity.this,
                    "Unable to change the brightness!", Toast.LENGTH_SHORT)
                    .show();
        }

    }

    private void setCamerExpose(int value) {
        // TODO Auto-generated method stub
        parameters.setExposureCompensation(value / 10);
        mCamera.setParameters(parameters);
    }

    private void setCameraZoom(int value) {
        // TODO Auto-generated method stub
        try {
            // parameters = mCamera.getParameters();
            parameters.setZoom(value / 10);
            mCamera.setParameters(parameters);

        } catch (Exception e) {
            // TODO: handle exception
            // Log.e("TGA", value+"");
            Toast.makeText(MainActivity.this, "It can't be enlarged anymore!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.invert:
                if (remember_bt.getTag() == null) {
                    remember_bt.setTag(invert_bt);
                } else {
                    ((Button) remember_bt.getTag()).setTextColor(Color
                            .parseColor("#ffffff"));
                    remember_bt.setTag(invert_bt);
                }
                switchFilterTo(GPUImageTools.createFilterForType(this,
                        FilterType.INVERT));
                invert_bt.setTextColor(Color.parseColor("#a3a3a3"));
                break;
            case R.id.hue:
                if (remember_bt.getTag() == null) {
                    remember_bt.setTag(hue_bt);
                } else {
                    ((Button) remember_bt.getTag()).setTextColor(Color
                            .parseColor("#ffffff"));
                    remember_bt.setTag(hue_bt);
                }
                switchFilterTo(GPUImageTools.createFilterForType(this,
                        FilterType.HUE));
                hue_bt.setTextColor(Color.parseColor("#a3a3a3"));
                break;
            case R.id.sepia:
                if (remember_bt.getTag() == null) {
                    remember_bt.setTag(sepia_bt);
                } else {
                    ((Button) remember_bt.getTag()).setTextColor(Color
                            .parseColor("#ffffff"));
                    remember_bt.setTag(sepia_bt);
                }
                switchFilterTo(GPUImageTools.createFilterForType(this,
                        FilterType.SEPIA));
                sepia_bt.setTextColor(Color.parseColor("#a3a3a3"));
                break;
            case R.id.gray:
                if (remember_bt.getTag() == null) {
                    remember_bt.setTag(gray_bt);
                } else {
                    ((Button) remember_bt.getTag()).setTextColor(Color
                            .parseColor("#ffffff"));
                    remember_bt.setTag(gray_bt);
                }
                switchFilterTo(GPUImageTools.createFilterForType(this,
                        FilterType.GRAYSCALE));
                gray_bt.setTextColor(Color.parseColor("#a3a3a3"));
                break;
            case R.id.shar:
                if (remember_bt.getTag() == null) {
                    remember_bt.setTag(shar_bt);
                } else {
                    ((Button) remember_bt.getTag()).setTextColor(Color
                            .parseColor("#ffffff"));
                    remember_bt.setTag(shar_bt);
                }
                switchFilterTo(GPUImageTools.createFilterForType(this,
                        FilterType.SHARPEN));
                shar_bt.setTextColor(Color.parseColor("#a3a3a3"));
                break;
            case R.id.sobel:
                if (remember_bt.getTag() == null) {
                    remember_bt.setTag(sobel_bt);
                } else {
                    ((Button) remember_bt.getTag()).setTextColor(Color
                            .parseColor("#ffffff"));
                    remember_bt.setTag(sobel_bt);
                }
                switchFilterTo(GPUImageTools.createFilterForType(this,
                        FilterType.SOBEL_EDGE_DETECTION));
                sobel_bt.setTextColor(Color.parseColor("#a3a3a3"));
                break;
            case R.id.convolution:
                if (remember_bt.getTag() == null) {
                    remember_bt.setTag(convo_bt);
                } else {
                    ((Button) remember_bt.getTag()).setTextColor(Color
                            .parseColor("#ffffff"));
                    remember_bt.setTag(convo_bt);
                }
                switchFilterTo(GPUImageTools.createFilterForType(this,
                        FilterType.THREE_X_THREE_CONVOLUTION));
                convo_bt.setTextColor(Color.parseColor("#a3a3a3"));
                break;
            case R.id.emboss:
                if (remember_bt.getTag() == null) {
                    remember_bt.setTag(emboss_bt);
                } else {
                    ((Button) remember_bt.getTag()).setTextColor(Color
                            .parseColor("#ffffff"));
                    remember_bt.setTag(emboss_bt);
                }
                switchFilterTo(GPUImageTools.createFilterForType(this,
                        FilterType.EMBOSS));
                emboss_bt.setTextColor(Color.parseColor("#a3a3a3"));
                break;
            case R.id.postersize:
                if (remember_bt.getTag() == null) {
                    remember_bt.setTag(posterize_bt);
                } else {
                    ((Button) remember_bt.getTag()).setTextColor(Color
                            .parseColor("#ffffff"));
                    remember_bt.setTag(posterize_bt);
                }
                switchFilterTo(GPUImageTools.createFilterForType(this,
                        FilterType.POSTERIZE));
                posterize_bt.setTextColor(Color.parseColor("#a3a3a3"));
                break;
            case R.id.grouped:
                if (remember_bt.getTag() == null) {
                    remember_bt.setTag(group_bt);
                } else {
                    ((Button) remember_bt.getTag()).setTextColor(Color
                            .parseColor("#ffffff"));
                    remember_bt.setTag(group_bt);
                }
                switchFilterTo(GPUImageTools.createFilterForType(this,
                        FilterType.FILTER_GROUP));
                group_bt.setTextColor(Color.parseColor("#a3a3a3"));
                break;
            case R.id.monochrome:
                if (remember_bt.getTag() == null) {
                    remember_bt.setTag(mono_bt);
                } else {
                    ((Button) remember_bt.getTag()).setTextColor(Color
                            .parseColor("#ffffff"));
                    remember_bt.setTag(mono_bt);
                }
                switchFilterTo(GPUImageTools.createFilterForType(this,
                        FilterType.MONOCHROME));
                mono_bt.setTextColor(Color.parseColor("#a3a3a3"));
                break;
            case R.id.normal:
                if (remember_bt.getTag() == null) {
                    remember_bt.setTag(nor_bt);
                } else {
                    ((Button) remember_bt.getTag()).setTextColor(Color
                            .parseColor("#ffffff"));
                    remember_bt.setTag(nor_bt);
                }
                switchFilterTo(GPUImageTools.createFilterForType(this,
                        FilterType.PIXELATION));
                nor_bt.setTextColor(Color.parseColor("#a3a3a3"));
                break;

            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
        SharedPreferences.Editor se = sp.edit();
        if (hProgress >= 960) {
            se.putInt("hProgress", 960);
        } else if (hProgress <= 0) {
            se.putInt("hProgress", 0);
        } else {
            se.putInt("hProgress", hProgress);
        }

        if (vprogress >= 900) {
            se.putInt("vprogress", 900);
        } else if (vprogress <= 0) {
            se.putInt("vprogress", 0);
        } else {
            se.putInt("vprogress", vprogress);
        }
        se.putInt("jk_img", jk);
        se.putBoolean("iscomment", iscomment);
        se.commit();
        wakeLock.release();
    }

    @Override
    public void onAutoFocus(boolean success, Camera camera) {
        takePicture();
    }

    /******************************************************************************************/

    class A implements PictureCallback,OnPictureSavedListener{

        String folderName="SmartMirror";
        String fileName=System.currentTimeMillis() + ".jpg";
        File pictureFile;
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if (pictureFile == null) {
                Toast.makeText(MainActivity.this,"Failed to save photo, please try again!",
                        Toast.LENGTH_SHORT).show();
            }else {
                saveToPictures(data);
            }
        }

        private void saveToPictures(byte[] data) {
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
                Bitmap bitmap = BitmapFactory.decodeFile(pictureFile.getAbsolutePath());
                glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
                mGPUImage.saveToPictures(bitmap, folderName, fileName,this);
            } catch (IOException ignored) {
            }
        }

        @SuppressLint("SimpleDateFormat")
        private File getOutputMediaFile(int type) {
            File mediaStorageDir = new File(
                    Environment
                            .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    "MyCameraApp");

            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    Toast.makeText(MainActivity.this,
                            "Failed to create a file, please try again!",
                            Toast.LENGTH_SHORT).show();
                    return null;
                }
            }
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File mediaFile;
            if (type == MEDIA_TYPE_IMAGE) {
                mediaFile = new File(mediaStorageDir.getPath() + File.separator
                        + "IMG_" + timeStamp + ".jpg");
            } else {
                return null;
            }
            return mediaFile;
        }

        @Override
        public void onPictureSaved(Uri uri) {
            pictureFile.delete();
            mCamera.startPreview();
            glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
            Toast.makeText(MainActivity.this,
                    "Photo already saved to album", Toast.LENGTH_SHORT)
                    .show();
        }
    }
}

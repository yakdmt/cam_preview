package com.yakdmt.campreview;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener,
        View.OnClickListener{

    private TextureView mTextureView;
    private boolean isCameraExist;
    private Button mPreviewButton;
    private Camera mCamera;
    private SurfaceTexture mSurfaceTexture;
    private boolean isCameraOpen;
    private boolean isStarting;
    private boolean isStopping;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextureView = (TextureView) findViewById(R.id.texture_view);
        mTextureView.setSurfaceTextureListener(this);
        PackageManager packageManager = getPackageManager();
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            isCameraExist = true;
        }
        mPreviewButton = (Button) findViewById(R.id.button);
        mPreviewButton.setOnClickListener(this);
        startCamera();
    }


    synchronized protected void startCamera() {
        if (!isStarting) {
            isStarting = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        mCamera = Camera.open();

                        setCameraDisplayOrientation(MainActivity.this, 0, mCamera);

                        Camera.Parameters parameters = mCamera.getParameters();

                        parameters.setPreviewSize(parameters.getSupportedPreviewSizes().get(0).width,
                                parameters.getSupportedPreviewSizes().get(0).height);

                        try {
                            mCamera.setParameters(parameters);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        mCamera.setPreviewTexture(mSurfaceTexture);
                        mCamera.startPreview();
                        isCameraOpen = true;
                        isStarting = false;
                    } catch (NullPointerException e) {
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "Camera not available", Toast.LENGTH_SHORT).show();
                            }
                        });
                        e.printStackTrace();
                        isStarting = false;
                    } catch (Exception e) {
                        e.printStackTrace();
                        stopCamera();
                        isStarting = false;
                    }
                }
            }).start();
        }
    }

    private void stopCamera() {
        if (!isStopping) {
            isStopping = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {

                        if (mCamera != null) {
                            mCamera.stopPreview();
                            mCamera.release();
                            mCamera = null;
                            isCameraOpen = false;
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    isStopping = false;
                }
            }).start();
        }
    }


    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        this.mSurfaceTexture = surface;
        if (isCameraExist) {
            startCamera();
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        stopCamera();
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        // Logging.i(LOG_TAG, " onSurfaceTextureUpdated ");
    }

    public void setCameraDisplayOrientation(Activity activity, int cameraId, Camera camera) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();

        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        camera.setDisplayOrientation((info.orientation - degrees + 360) % 360);

    }

    public void onResume() {
        super.onResume();
        //startCamera();
        try {
            mTextureView.setSurfaceTextureListener(this);
            //stopCamera();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onClick(View v) {
        if (isCameraOpen) {
            stopCamera();
            mTextureView.setVisibility(View.GONE);
            mPreviewButton.setText("start");
        } else {
            startCamera();
            mTextureView.setVisibility(View.VISIBLE);
            mTextureView.setSurfaceTextureListener(this);
            mPreviewButton.setText("stop");
        }
    }
}

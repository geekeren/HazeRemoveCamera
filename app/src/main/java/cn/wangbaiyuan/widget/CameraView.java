package cn.wangbaiyuan.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by BrainWang on 2016/4/9.
 */

public class CameraView extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {

    private final SurfaceHolder surfaceHolder;
    private Camera camera;
    public boolean cameraOpen = false;
    private float mCameraOrientation;

    private Activity mActivity;

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        surfaceHolder = getHolder();
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

    }

    public void bindActivity(Activity activity) {
        mActivity = activity;
    }

    public void startCamera() {
        if (!cameraOpen) {
            surfaceHolder.addCallback(this);
            initCamera();
            cameraOpen = true;
        }

    }

    private void initCamera() {
        int cameras = Camera.getNumberOfCameras();
        Camera.CameraInfo info = new Camera.CameraInfo();
        for (int i = 0; i < cameras; i++) {
            Camera.getCameraInfo(i, info);
            try {
                if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    camera = Camera.open(i);
                    break;
                } else if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    camera = Camera.open(i);
                }
            } catch (Exception e) {
                Log.e("mcamera", "mcamera:" + i);
                e.printStackTrace();
            }

        }
        //没有后置摄像头
        if (camera == null)
            try {
                camera = Camera.open();
            } catch (Exception e) {

                camera = null;
                e.printStackTrace();
            }
        else try {
            camera.setPreviewDisplay(getHolder());
            camera.setPreviewCallback(this);
        } catch (Exception e) {
            camera.release();//释放资源
            camera = null;
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {

//        Camera.Size previewSize = camera.getParameters().getPreviewSize();
//
//        Matrix matrix = new Matrix();
//        matrix.postRotate(mCameraOrientation);

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        initCamera();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (camera != null) {
            int currentCamera = Camera.CameraInfo.CAMERA_FACING_BACK;
            Camera.Parameters parameters = camera.getParameters();//得到相机设置参数
            Camera.Size size = camera.getParameters().getPreviewSize(); //获取预览大小

            parameters.setPictureFormat(PixelFormat.JPEG);//设置图片格式
            Camera.CameraInfo info = new Camera.CameraInfo();
            camera.getCameraInfo(currentCamera, info);
            int rotation = mActivity.getWindowManager().getDefaultDisplay().getRotation();
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
            int resultA = 0, resultB = 0;
            if (currentCamera == Camera.CameraInfo.CAMERA_FACING_BACK) {
                resultA = (info.orientation - degrees + 360) % 360;
                resultB = (info.orientation - degrees + 360) % 360;
                camera.setDisplayOrientation(resultA);
            } else {
                resultA = (360 + 360 - info.orientation - degrees) % 360;
                resultB = (info.orientation + degrees) % 360;
                camera.setDisplayOrientation(resultA);
            }
            camera.setPreviewCallback(this);
            parameters.setRotation(resultB);
            mCameraOrientation = resultB;
            camera.setParameters(parameters);
            //实现自动对焦
//            camera.autoFocus(new Camera.AutoFocusCallback() {
//                @Override
//                public void onAutoFocus(boolean success, Camera camera) {
//                    if(success){
//                        //initCamera();//实现相机的参数初始化
//                        camera.cancelAutoFocus();//只有加上了这一句，才会自动对焦。
//                    }
//                }
//
//            });
            camera.startPreview();//开始预览
        }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (camera != null) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }
}
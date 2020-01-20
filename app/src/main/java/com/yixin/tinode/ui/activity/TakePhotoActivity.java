package com.yixin.tinode.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.blankj.utilcode.util.ToastUtils;
import com.cjt2325.cameralibrary.JCameraView;
import com.cjt2325.cameralibrary.listener.ClickListener;
import com.cjt2325.cameralibrary.listener.ErrorListener;
import com.cjt2325.cameralibrary.listener.JCameraListener;
import com.yixin.tinode.R;
import com.yixin.tinode.app.AppConst;
import com.yixin.tinode.ui.base.BaseActivity;
import com.yixin.tinode.ui.base.BasePresenter;
import com.yixin.tinode.util.UIUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import kr.co.namee.permissiongen.PermissionFail;
import kr.co.namee.permissiongen.PermissionGen;
import kr.co.namee.permissiongen.PermissionSuccess;


/**
 * @描述 拍照界面
 */
public class TakePhotoActivity extends BaseActivity {
    private JCameraView mJCameraView;

    @Override
    public void init() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
            PermissionGen.with(this)
                    .addRequestCode(100)
                    .permissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO)
                    .request();
    }

    @Override
    public void initView() {
        mJCameraView = (JCameraView) findViewById(R.id.cameraview);
        //(0.0.7+)设置视频保存路径（如果不设置默认为Environment.getExternalStorageDirectory().getPath()）
        mJCameraView.setSaveVideoPath(Environment.getExternalStorageDirectory().getPath());

        //设置只能录像或只能拍照或两种都可以（默认两种都可以）
        mJCameraView.setFeatures(JCameraView.BUTTON_STATE_BOTH);

        //设置视频质量
        mJCameraView.setMediaQuality(JCameraView.MEDIA_QUALITY_MIDDLE);
        //设置小视频保存路径
        File file = new File(AppConst.VIDEO_SAVE_DIR);
        if (!file.exists())
            file.mkdirs();
        mJCameraView.setSaveVideoPath(AppConst.VIDEO_SAVE_DIR);
    }

    @Override
    public void initListener() {
        mJCameraView.setJCameraLisenter(new JCameraListener() {
            @Override
            public void captureSuccess(Bitmap bitmap) {
                //获取到拍照成功后返回的Bitmap
                String path = saveBitmap(bitmap, AppConst.PHOTO_SAVE_DIR);
                Intent data = new Intent();
                data.putExtra("take_photo", true);
                data.putExtra("path", path);
                //data.putExtra("bitmap",bitmap);
                setResult(RESULT_OK, data);
                finish();
            }

            @Override
            public void recordSuccess(String url, Bitmap firstFrame) {
                //获取成功录像后的视频路径
                Intent data = new Intent();
                data.putExtra("take_photo", false);
                data.putExtra("path", url);
                setResult(RESULT_OK, data);
                finish();
            }
        });

        mJCameraView.setErrorLisenter(new ErrorListener() {
            @Override
            public void onError() {
                //打开Camera失败回调
                Log.i("CJT", "open camera error");
                ToastUtils.showShort("打开摄像机失败");
            }

            @Override
            public void AudioPermissionError() {
                //没有录取权限回调
                Log.i("CJT", "AudioPermissionError");
                ToastUtils.showShort("没有录音权限");
            }
        });
        //左边按钮点击事件
        mJCameraView.setLeftClickListener(new ClickListener() {
            @Override
            public void onClick() {
                finish();
            }
        });
        //右边按钮点击事件
        mJCameraView.setRightClickListener(new ClickListener() {
            @Override
            public void onClick() {
            }
        });
    }

    @PermissionSuccess(requestCode = 100)
    public void permissionSuccess() {
//        UIUtils.postTaskDelay(() -> {
//            recreate();
//            //            Intent intent = getIntent();
//            //            finish();
//            //            startActivity(intent);
//        }, 500);
        //        UIUtils.postTaskSafely(() -> recreate());
    }

    @PermissionFail(requestCode = 100)
    public void permissionFail() {
        UIUtils.showToast("获取相机权限失败");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mJCameraView != null)
            mJCameraView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mJCameraView != null)
            mJCameraView.onPause();
    }

    @Override
    protected BasePresenter createPresenter() {
        return null;
    }

    @Override
    protected int provideContentViewId() {
        return R.layout.activity_take_photo;
    }


    public static String saveBitmap(Bitmap bm, String dir) {
        String path = "";
        File f = new File(dir, "yixin_" + SystemClock.currentThreadTimeMillis() + ".png");
        if (f.exists()) {
            f.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(f);

            bm.compress(Bitmap.CompressFormat.PNG, 60, out);
            out.flush();
            out.close();
            path = f.getAbsolutePath();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return path;
    }

}

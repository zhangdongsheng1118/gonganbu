package com.yixin.tinode.ui.activity;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bm.library.PhotoView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.yixin.tinode.R;
import com.yixin.tinode.api.ApiRetrofit;
import com.yixin.tinode.ui.base.BaseActivity;
import com.yixin.tinode.ui.base.BasePresenter;
import com.yixin.tinode.util.LogUtils;
import com.yixin.tinode.util.PathUtil;
import com.yixin.tinode.util.PopupWindowUtils;
import com.yixin.tinode.util.UIUtils;
import com.yixin.tinode.util.glide.progress.ProgressInterceptor;
import com.yixin.tinode.util.glide.progress.ProgressListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

import butterknife.BindView;
import okhttp3.ResponseBody;
import rx.schedulers.Schedulers;

/**
 * @创建者 CSDN_LQR
 * @描述 查看头像
 */
public class ShowBigImageActivity extends BaseActivity {

    public String mUrl;
    public String mName;
    public int mIsLocal;

    @BindView(R.id.ibToolbarMore)
    ImageButton mIbToolbarMore;
    @BindView(R.id.pv)
    PhotoView mPv;
    @BindView(R.id.pb)
    ProgressBar mPb;
    private FrameLayout mView;
    private PopupWindow mPopupWindow;

    @Override
    public void init() {
        mUrl = getIntent().getStringExtra("url");
        mName = getIntent().getStringExtra("name");
        mIsLocal=getIntent().getIntExtra("islocal",0);
    }

    @Override
    public void initView() {
        setToolbarTitle(mName);
        mIbToolbarMore.setVisibility(View.VISIBLE);
        if (TextUtils.isEmpty(mUrl)) {
            finish();
            return;
        }
        mPv.enable();// 启用图片缩放功能

        ProgressInterceptor.addListener(mUrl, new ProgressListener() {
            @Override
            public void onProgress(int progress) {
                LogUtils.i("进度" + progress);
            }
        });
        Glide.with(this)
                .load(mUrl)
//                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(new GlideDrawableImageViewTarget(mPv) {
                    @Override
                    public void onLoadStarted(Drawable placeholder) {
                        super.onLoadStarted(placeholder);
                        mPb.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> animation) {
                        super.onResourceReady(resource, animation);

                        mPb.setVisibility(View.GONE);
                        ProgressInterceptor.removeListener(mUrl);
                    }
                });
    }

    private static class ProgressHandler extends Handler {

        private final WeakReference<Activity> mActivity;
        private final ProgressBar mProgressImageView;

        public ProgressHandler(Activity activity, ProgressBar progressImageView) {
            super(Looper.getMainLooper());
            mActivity = new WeakReference<>(activity);
            mProgressImageView = progressImageView;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mProgressImageView.getVisibility() == View.GONE) {
                mProgressImageView.setVisibility(View.VISIBLE);
            }
            final Activity activity = mActivity.get();
            if (activity != null) {
                switch (msg.what) {
                    case 1:
                        int percent = msg.arg1 * 100 / msg.arg2;
                        mProgressImageView.setProgress(percent);
                        if (percent >= 100) {
                            mProgressImageView.setVisibility(View.GONE);
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }

    @Override
    public void initListener() {
        mIbToolbarMore.setOnClickListener(v -> showPopupMenu());
    }

    @Override
    protected BasePresenter createPresenter() {
        return null;
    }

    @Override
    protected int provideContentViewId() {
        return R.layout.activity_show_big_image;
    }

    private void showPopupMenu() {
        if (mView == null) {
            mView = new FrameLayout(this);
            mView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            mView.setBackgroundColor(UIUtils.getColor(R.color.white));

            TextView tv = new TextView(this);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, UIUtils.dip2Px(45));
            tv.setLayoutParams(params);
            tv.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            tv.setPadding(UIUtils.dip2Px(20), 0, 0, 0);
            tv.setTextColor(UIUtils.getColor(R.color.gray0));
            tv.setTextSize(14);
            tv.setText(UIUtils.getString(R.string.save_to_phone));
            mView.addView(tv);
            final String finalUrl=mUrl;
            final int finalIsLocal=mIsLocal;
            tv.setOnClickListener(v -> {
                        //if (finalUrl.startsWith("file")) {
                if (mIsLocal>0){
                File file = new File(Uri.parse(finalUrl).getPath());
                            UIUtils.showToast(copyToDisk(file) ? UIUtils.getString(R.string.save_success) : UIUtils.getString(R.string.save_fail));
                            mPopupWindow.dismiss();
                            mPopupWindow = null;
                        } else {
                            ApiRetrofit.getInstance()
                                    .mApi
                                    .downloadPic(mUrl)
                                    .subscribeOn(Schedulers.newThread())
                                    .subscribe(responseBody -> {
                                        boolean success = saveToDisk(responseBody);
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                UIUtils.showToast(success ? UIUtils.getString(R.string.save_success) : UIUtils.getString(R.string.save_fail));
                                                mPopupWindow.dismiss();
                                                mPopupWindow = null;
                                            }
                                        });
                                    });
                        }

                    }
            );
        }
        mPopupWindow = PopupWindowUtils.getPopupWindowAtLocation(mView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, getWindow().getDecorView().getRootView(), Gravity.BOTTOM, 0, 0);
        mPopupWindow.setOnDismissListener(() -> PopupWindowUtils.makeWindowLight(ShowBigImageActivity.this));
        PopupWindowUtils.makeWindowDark(ShowBigImageActivity.this);
    }

    private boolean copyToDisk(File file) {
        try {
            InputStream in = null;
            FileOutputStream out = null;
            try {
                in = new FileInputStream(file);
                out = new FileOutputStream(new File(PathUtil.getInstance().getImagePath(), SystemClock.currentThreadTimeMillis() + ".jpg"));
                int c;

                while ((c = in.read()) != -1) {
                    out.write(c);
                }
            } catch (IOException e) {
                return false;
            } finally {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private boolean saveToDisk(ResponseBody body) {
        try {
            InputStream in = null;
            FileOutputStream out = null;
            try {
                in = body.byteStream();
                out = new FileOutputStream(new File(PathUtil.getInstance().getImagePath(), SystemClock.currentThreadTimeMillis() + ".jpg"));
                int c;

                while ((c = in.read()) != -1) {
                    out.write(c);
                }
            } catch (IOException e) {
                return false;
            } finally {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}


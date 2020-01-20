package com.yixin.tinode.ui.activity;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.jaeger.library.StatusBarUtil;
import com.starrtc.demo.demo.service.KeepLiveService;
import com.starrtc.demo.utils.AEvent;
import com.yixin.tinode.R;
import com.yixin.tinode.db.tinode.BaseDb;
import com.yixin.tinode.model.cache.UserCache;
import com.yixin.tinode.tinode.account.Utils;
import com.yixin.tinode.tinode.util.UiUtils;
import com.yixin.tinode.ui.base.BaseActivity;
import com.yixin.tinode.ui.base.BasePresenter;
import com.yixin.tinode.util.UIUtils;
import com.zuozhan.app.AppEnvirment;
import com.zuozhan.app.activity.ZHLoginActivity;
import com.zuozhan.app.activity.ZHMainActivity;
import com.zuozhan.app.inteface.LoginCallback;
import com.zuozhan.app.util.LocationUtil;
import com.zuozhan.app.util.LogUtil;
import com.zuozhan.app.util.RuntimePermissionsManager;
import com.zuozhan.app.util.ServerHostShareprefrensUtils;
import com.zuozhan.app.util.ShareprefrensUtils;
import com.zuozhan.app.util.ToastUtils;

import butterknife.BindView;
import kr.co.namee.permissiongen.PermissionGen;

import static com.yixin.tinode.util.Utils.isNetworkConnected;
import static com.yixin.tinode.util.Utils.showNetworkAlert;

/**
 * @创建者 CSDN_LQR
 * @描述 一信闪屏页
 */
public class SplashActivity extends BaseActivity {

    @BindView(R.id.rlButton)
    RelativeLayout mRlButton;
    @BindView(R.id.btnLogin)
    Button mBtnLogin;
    @BindView(R.id.btnRegister)
    Button mBtnRegister;

    @Override
    public void init() {
//        PermissionGen.with(this)
//
//                .addRequestCode(100)
//                .permissions(
//                        //电话通讯录
//                        Manifest.permission.GET_ACCOUNTS,
//                        Manifest.permission.READ_PHONE_STATE,
//                        //位置
//                        Manifest.permission.ACCESS_FINE_LOCATION,
//                        Manifest.permission.ACCESS_COARSE_LOCATION,
//                        Manifest.permission.ACCESS_FINE_LOCATION,
//                        //相机、麦克风
//                        Manifest.permission.RECORD_AUDIO,
//                        Manifest.permission.WAKE_LOCK,
//                        Manifest.permission.CAMERA,
//                        Manifest.permission.MODIFY_AUDIO_SETTINGS,
//                        //存储空间
//                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                        Manifest.permission.WRITE_SETTINGS,
//                        Manifest.permission.REQUEST_INSTALL_PACKAGES,
//                        //NET
//                        Manifest.permission.ACCESS_NETWORK_STATE
//                        //Audio
//
//                )
//                .request();
//        if (!TextUtils.isEmpty(UserCache.getToken())) {
//            Intent intent = new Intent(this, MainActivity.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//            jumpToActivity(intent);
//            finish();
//        }

    }

    @Override
    public void initView() {
        boolean isHasPermission = RuntimePermissionsManager.whetherAllPermissionsGranted(this);
        if (isHasPermission) {
            initALL();
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                RuntimePermissionsManager.requestRequiredPermissions(this);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        try {
            if (requestCode == RuntimePermissionsManager.PERMISSION_REQUEST_CODE) {
                if (RuntimePermissionsManager.hasDeniedPermissions(permissions, grantResults)) {
                    LogUtil.d(this, "onRequestPermissionsResult deny");
                    finish();
                } else {
                    if (LocationUtil.getIntance().checkAndOpenGPS(this)) {
                        initALL();
                    } else {
                        ToastUtils.showToast("请打开gps");
                        finish();
                    }
                    LogUtil.d(this, "onRequestPermissionsResult accept");
                }
            } else {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        } catch (Throwable e) {
            LogUtil.d(this, "permission result error=" + e.toString());
        }
    }


    private void initALL() {
        ServerHostShareprefrensUtils.initSP();
        StatusBarUtil.setColor(this, UIUtils.getColor(R.color.black));

        AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
        alphaAnimation.setDuration(1000);
        mRlButton.startAnimation(alphaAnimation);
        if (!AppEnvirment.isLogin()) {
            context.startActivity(new Intent(context, ZHLoginActivity.class));
            finish();
            return;
        }
        String uid = BaseDb.getInstance().getUid();
        if (!TextUtils.isEmpty(uid) && isNetworkConnected(this)) {
            mBtnLogin.setVisibility(View.GONE);
            mBtnRegister.setVisibility(View.GONE);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    final AccountManager accountManager = AccountManager.get(context);
                    // If uid is non-null, get account to use it to login by saved token
                    final Account account = UiUtils.getSavedAccount(context, accountManager, uid);
                    if (account != null) {
                        // Check if sync is enabled.
                        if (ContentResolver.getMasterSyncAutomatically()) {
                            if (!ContentResolver.getSyncAutomatically(account, Utils.SYNC_AUTHORITY)) {
                                ContentResolver.setSyncAutomatically(account, Utils.SYNC_AUTHORITY, true);
                            }
                        }
                        // Account found, try to use it for login
                        UiUtils.loginWithSavedAccount(accountManager, account, new LoginCallback() {

                            @Override
                            public void onCallBack(boolean success, String result) {
                                context.startActivity(new Intent(context, success ? ZHMainActivity.class : ZHLoginActivity.class));
                                finish();
                            }
                        });
                        return;
                    }
                }
            }, 1500);
        } else if (!isNetworkConnected(this)) {
            showNetworkAlert(context);
        } else {
            context.startActivity(new Intent(context, ZHLoginActivity.class));
            finish();
        }

    }

    @Override
    public void initListener() {
        mBtnLogin.setOnClickListener(v -> {
            if (isNetworkConnected(SplashActivity.this)) {
                jumpToActivity(LoginActivity.class);
                finish();
            } else {
                showNetworkAlert(context);
            }

        });
        mBtnRegister.setOnClickListener(v -> {
            if (isNetworkConnected(SplashActivity.this)) {
                jumpToActivity(RegisterActivity.class);
                finish();
            } else {
                showNetworkAlert(context);
            }
        });
    }

    @Override
    protected BasePresenter createPresenter() {
        return null;
    }

    @Override
    protected int provideContentViewId() {
        return R.layout.activity_splash;
    }
}

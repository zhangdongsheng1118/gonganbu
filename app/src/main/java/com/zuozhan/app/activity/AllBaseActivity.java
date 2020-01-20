package com.zuozhan.app.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.jaeger.library.StatusBarUtil;
import com.starrtc.demo.demo.BaseActivity;
import com.starrtc.demo.demo.MLOC;
import com.starrtc.demo.demo.service.KeepLiveService;
import com.starrtc.demo.utils.AEvent;
import com.starrtc.demo.utils.IEventListener;
import com.yixin.tinode.R;
import com.yixin.tinode.ui.activity.web.WebCommonActivity;
import com.yixin.tinode.util.UIUtils;
import com.yixin.tinode.widget.CustomDialog;
import com.zuozhan.app.AppEnvirment;
import com.zuozhan.app.BaseIP;
import com.zuozhan.app.RouterUtil;
import com.zuozhan.app.bean.RenWuLeiBean;
import com.zuozhan.app.util.AppManager;
import com.zuozhan.app.util.ToastUtils;

import butterknife.ButterKnife;
import me.drakeet.materialdialog.MaterialDialog;

public class AllBaseActivity extends BaseActivity {
    private boolean isDestory = false;
    private CustomDialog mDialogWaiting;
    private MaterialDialog mMaterialDialog;

    protected AllBaseActivity context;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtil.setColor(this, UIUtils.getColor(R.color.color_152130), 10);
        AppManager.addActivity(this);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        ButterKnife.bind(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            isDestory = true;
            AppManager.finishActivity(this);
        } catch (Exception e) {
        }
        AEvent.removeListener(AEvent.AEVENT_LOGIN_STATUS);
    }

    public boolean isFinish() {
        return isFinishing() || isDestory;
    }

    public void goHome() {
        AppManager.finishAllActivity();
        RouterUtil.goHome(this);
    }

    public void goHome(int type) {
        AppManager.finishAllActivity();
        RouterUtil.goHome(this, type);
    }

    public void goHome(int type, RenWuLeiBean.DataBean dataBean) {
        AppManager.finishAllActivity();
        RouterUtil.goHome(this, type, dataBean);
    }

    public void goHome(int type, RenWuLeiBean.DataBean dataBean, String topicTmp) {
        AppManager.finishAllActivity();
        RouterUtil.goHome(this, type, dataBean, topicTmp);
    }

    Fragment curFragment;

    /**
     * 切换frgament fragment 如果存在不重启
     *
     * @param id
     * @param fragment
     */
    protected void switchFragment(int id, Fragment fragment) {
        if (!isFinish()) {
            if (fragment != this.curFragment) {
                if (!fragment.isAdded()) {
                    if (this.curFragment != null) {
                        this.getSupportFragmentManager()
                                .beginTransaction()
                                .hide(this.curFragment)
                                .add(id, fragment)
                                .commitAllowingStateLoss();
                    } else {
                        this.getSupportFragmentManager()
                                .beginTransaction()
                                .add(id, fragment)
                                .commitAllowingStateLoss();
                    }
                } else {
                    this.getSupportFragmentManager()
                            .beginTransaction()
                            .hide(this.curFragment)
                            .show(fragment)
                            .commitAllowingStateLoss();
                }
                this.curFragment = fragment;
            }
        }
    }

    /**
     * 切换frgament 每次创建新的
     *
     * @param id
     * @param fragment
     */
    protected void switchNewFragment(int id, Fragment fragment) {
        FragmentTransaction ft = this.getSupportFragmentManager().beginTransaction();
        ft.setTransition(0);
        ft.replace(id, fragment);
        ft.commitAllowingStateLoss();
        curFragment = fragment;
    }

    ProgressDialog pd;

    public void showLoading(String string) {
        try {
            if (pd == null) {
                pd = new ProgressDialog(this);
                pd.setCanceledOnTouchOutside(false);
                pd.setCancelable(false);
            }
            if (TextUtils.isEmpty(string)) {
                pd.setMessage("加载中...");
            } else {
                pd.setMessage("" + string);
            }
            if (!pd.isShowing()) {
                pd.show();
            }
        } catch (Throwable e) {
        }
    }

    public void showLoading(int string) {
        if (pd == null) {
            pd = new ProgressDialog(this);
            pd.setCanceledOnTouchOutside(false);
            pd.setCancelable(false);
        }
        pd.setMessage("" + getResources().getString(string));
        if (!pd.isShowing()) {
            pd.show();
        }
    }

    public void dismissLoading() {
        try {
            if (pd != null && pd.isShowing()) {
                pd.dismiss();
            }
        } catch (Exception e) {
        }
    }

    protected void hideSoft() {
        InputMethodManager imm =
                (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
        }
    }

    protected void showSoft() {
        InputMethodManager imm =
                (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
        }
    }

    public void jumpToActivity(Intent intent) {
        startActivity(intent);
    }

    public void jumpToActivity(Class activity) {
        Intent intent = new Intent(this, activity);
        startActivity(intent);
    }

    public void jumpToWebViewActivity(String url) {
//        Intent intent = new Intent(this, WebViewActivity.class);
//        intent.putExtra("url", url);
        Intent intent = new Intent(this, WebCommonActivity.class)
                .putExtra(WebCommonActivity.TYPE_KEY, WebCommonActivity.FLAG_GUIDE_DICTIONARY_BOUNCE_EFFACT)
                .putExtra(WebCommonActivity.URL_KEY, url);
        jumpToActivity(intent);
    }


    public void jumpToActivityAndClearTask(Class activity) {
        Intent intent = new Intent(this, activity);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    public void jumpToActivityAndClearTop(Class activity) {
        Intent intent = new Intent(this, activity);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }


    /**
     * 显示等待提示框
     */
    public Dialog showWaitingDialog(String tip) {
        hideWaitingDialog();
        View view = View.inflate(this, R.layout.dialog_waiting, null);
        if (!TextUtils.isEmpty(tip))
            ((TextView) view.findViewById(R.id.tvTip)).setText(tip);
        mDialogWaiting = new CustomDialog(this, view, R.style.MyDialog);
        mDialogWaiting.show();
        mDialogWaiting.setCancelable(true);
        return mDialogWaiting;
    }

    /**
     * 隐藏等待提示框
     */
    public void hideWaitingDialog() {
        if (mDialogWaiting != null) {
            mDialogWaiting.dismiss();
            mDialogWaiting = null;
        }
    }

    /**
     * 显示MaterialDialog
     */
    public MaterialDialog showMaterialDialog(String title, String message, String positiveText, String negativeText, View.OnClickListener positiveButtonClickListener, View.OnClickListener negativeButtonClickListener) {
        hideMaterialDialog();
        mMaterialDialog = new MaterialDialog(this);
        if (!TextUtils.isEmpty(title)) {
            mMaterialDialog.setTitle(title);
        }
        if (!TextUtils.isEmpty(message)) {
            mMaterialDialog.setMessage(message);
        }
        if (!TextUtils.isEmpty(positiveText)) {
            mMaterialDialog.setPositiveButton(positiveText, positiveButtonClickListener);
        }
        if (!TextUtils.isEmpty(negativeText)) {
            mMaterialDialog.setNegativeButton(negativeText, negativeButtonClickListener);
        }
        mMaterialDialog.show();
        return mMaterialDialog;
    }

    /**
     * 隐藏MaterialDialog
     */
    public void hideMaterialDialog() {
        if (mMaterialDialog != null) {
            mMaterialDialog.dismiss();
            mMaterialDialog = null;
        }
    }

    Handler handler = new Handler() {
        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
            switch (msg.what) {
                case CONNECT_RTC:
                /*    if (rtc_count < 3) {
                        showLoading("RTC服务器重连中..."+(rtc_count+1)+"次");
                        startRTC();
                        rtc_count++;
                    } else {
                        ToastUtils.showToast("RTC 连接失败，超时3次");
                        dismissLoading();
                        handler.removeMessages(CONNECT_RTC);
                        rtc_count = 0;
                    }*/
                    break;
            }
        }
    };

    private static final int CONNECT_RTC = 101;

    private int rtc_count = 0;

    @Override
    protected void connectRTC() {
        super.connectRTC();
        if (AppEnvirment.getUserBean() == null || AppEnvirment.getUserBean().data == null) {
            return;
        }
       // showLoading("RTC服务器连接中...");
        AEvent.addListener(AEvent.AEVENT_LOGIN_STATUS, new IEventListener() {
            @Override
            public void dispatchEvent(String aEventID, boolean success, Object eventObj) {
                handler.removeMessages(CONNECT_RTC);
                dismissLoading();
                if (success) {
                    if (MLOC.userId == null) {
                        ToastUtils.showToast("RTC 用户初始化失败");
                    }
                    ToastUtils.showToast("登录成功");
                } else {
                    ToastUtils.showToast("登录失败");
                    reconnectRTC();
                }
            }
        });
        rtc_count = 0;
        startRTC();
    }

    private void startRTC(){
        handler.sendEmptyMessageDelayed(CONNECT_RTC, 15 * 1000);
        Intent intent = new Intent(this, KeepLiveService.class);
        intent.putExtra("userId", AppEnvirment.getUserBean().data.id + "");
        intent.putExtra("camera", BaseIP.camera);
        startService(intent);
    }

    @Override
    protected void reconnectRTC() {
        super.reconnectRTC();
        showLoading("RTC服务器重连中...");
        AEvent.addListener(AEvent.AEVENT_LOGIN_STATUS, new IEventListener() {
            @Override
            public void dispatchEvent(String aEventID, boolean success, Object eventObj) {
                handler.removeMessages(CONNECT_RTC);
                dismissLoading();
                if (success) {
                    if (MLOC.userId == null) {
                        ToastUtils.showToast("RTC 用户初始化失败");
                    }
                    ToastUtils.showToast("登录成功");
                } else {
                    ToastUtils.showToast("登录失败");
                }
            }
        });
        rtc_count = 0;
        startRTC();
    }

}

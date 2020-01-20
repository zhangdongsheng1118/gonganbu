package com.yixin.tinode.ui.activity;

import android.content.Intent;
import android.view.View;

import com.lqr.optionitemview.OptionItemView;
import com.yixin.tinode.R;
import com.yixin.tinode.app.MyApp;
import com.yixin.tinode.db.tinode.BaseDb;
import com.yixin.tinode.model.cache.UserCache;
import com.yixin.tinode.tinode.Cache;
import com.yixin.tinode.ui.base.BaseActivity;
import com.yixin.tinode.ui.base.BasePresenter;
import com.yixin.tinode.widget.CustomDialog;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @创建者 CSDN_LQR
 * @描述 设置界面
 */
public class SettingActivity extends BaseActivity {

    private View mExitView;

    @BindView(R.id.avSetting)
    OptionItemView mAVSetting;
    @BindView(R.id.offlineMap)
    OptionItemView mOfflineMap;
    @BindView(R.id.oivAbout)
    OptionItemView mOivAbout;
    @BindView(R.id.oivHelpFeedback)
    OptionItemView mOivHelpFeedback;
    @BindView(R.id.oivExit)
    OptionItemView mOivExit;
    private CustomDialog mExitDialog;

    private void initSetting() {

    }

    @Override
    public void initListener() {
        mOivAbout.setOnClickListener(v -> jumpToActivity(AboutActivity.class));
        mOfflineMap.setOnClickListener(v -> jumpToActivity(DownLoadOfflineMapActivity.class));
        mOivHelpFeedback.setOnClickListener(v1 -> {
//            jumpToWebViewActivity(AppConst.WeChatUrl.HELP_FEED_BACK);
            initSetting();
        });

        mOivExit.setOnClickListener(v -> {
            if (mExitView == null) {
                mExitView = View.inflate(this, R.layout.dialog_exit, null);
                mExitDialog = new CustomDialog(this, mExitView, R.style.MyDialog);
                mExitView.findViewById(R.id.tvExitAccount).setOnClickListener(v1 -> {
//                    RongIMClient.getInstance().logout();
                    UserCache.clear();
                    mExitDialog.dismiss();
                    MyApp.exit();
//                    Cache.getTinode().logout();
//                    Cache.getTinode().refreshTopic();
                    BaseDb.getInstance().logout();
                    Cache.invalidate();
                    jumpToActivityAndClearTask(LoginActivity.class);
                });
                mExitView.findViewById(R.id.tvExitApp).setOnClickListener(v1 -> {
//                    RongIMClient.getInstance().disconnect();
                    mExitDialog.dismiss();
                    MyApp.exit();
                });
            }
            mExitDialog.show();
        });
    }

    @Override
    protected BasePresenter createPresenter() {
        return null;
    }

    @Override
    protected int provideContentViewId() {
        return R.layout.activity_setting;
    }

    @OnClick({R.id.oivNewMsgNotifySet, R.id.avSetting})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.oivNewMsgNotifySet:
                startActivity(new Intent(context, SettingNewMsgNotifyActivity.class));
                break;
            case R.id.avSetting:
                startActivity(new Intent(context, SettingAVActivity.class));
                break;
        }
    }

}

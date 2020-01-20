package com.yixin.tinode.ui.fragment;

import android.content.ComponentName;
import android.content.Intent;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.lqr.optionitemview.OptionItemView;
import com.yixin.tinode.R;
import com.yixin.tinode.app.AppConst;
import com.yixin.tinode.ui.activity.MainActivity;
import com.yixin.tinode.ui.base.BaseFragment;
import com.yixin.tinode.ui.presenter.DiscoveryFgPresenter;
import com.yixin.tinode.ui.view.IDiscoveryFgView;

import butterknife.BindView;


/**
 * @创建者 CSDN_LQR
 * @描述 发现界面
 */
public class DiscoveryFragment extends BaseFragment<IDiscoveryFgView, DiscoveryFgPresenter> implements IDiscoveryFgView {
    @BindView(R.id.oivCamera)
    OptionItemView oivCamera;
    @BindView(R.id.oivCard)
    OptionItemView oivCard;

    @Override
    public void initListener() {
        oivCamera.setOnClickListener(v -> {
            String pack = "cn.zhousheng.com.easymonitor";
            if (AppUtils.isInstallApp(pack)) {
                AppUtils.launchApp("cn.zhousheng.com.easymonitor");
            } else {
                ToastUtils.showShort("未安装应用");
            }
        });
        oivCard.setOnClickListener(v -> ((MainActivity) getActivity()).jumpToWebViewActivity(AppConst.WeChatUrl.YIXIN_ADMIN));
//        mOivGame.setOnClickListener(v -> ((MainActivity) getActivity()).jumpToWebViewActivity(AppConst.WeChatUrl.GAME));
    }

    public void sampleLaunchApp1() {
        Intent intent = new Intent();
        //包名 包名+类名（全路径）
        ComponentName comp = new ComponentName("com.demo.surfaceviewdemo", "com.demo.surfaceviewdemo.JumpTestActivity");
        intent.setComponent(comp);
        intent.setAction("android.intent.action.MAIN");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("data", "123");
        startActivity(intent);
    }

    public void sampleLaunchApp2() {
        Intent intent = new Intent();
        //包名 包名+类名（全路径）
        intent.setClassName("com.demo.surfaceviewdemo", "com.demo.surfaceviewdemo.JumpTestActivity");
        intent.putExtra("data", "123");
        startActivity(intent);
    }


    @Override
    protected DiscoveryFgPresenter createPresenter() {
        return new DiscoveryFgPresenter((MainActivity) getActivity());
    }

    @Override
    protected int provideContentViewId() {
        return R.layout.fragment_discovery;
    }
}

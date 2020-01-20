package com.yixin.tinode.ui.activity;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.blankj.utilcode.util.AppUtils;
import com.yixin.tinode.R;
import com.yixin.tinode.ui.base.BaseActivity;
import com.yixin.tinode.ui.base.BasePresenter;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ezy.boost.update.UpdateManager;

/**
 * @创建者 CSDN_LQR
 * @描述 关于界面
 */
public class AboutActivity extends BaseActivity {


	@BindView(R.id.iv_version)
	TextView ivVersion;

	@Override
	protected BasePresenter createPresenter() {
		return null;
	}

	@Override
	public void initView() {
		super.initView();
		ivVersion.setText("一信  "+AppUtils.getAppVersionName());
	}

	@OnClick({R.id.oiv_check})
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.oiv_check:
				UpdateManager.checkVersionManu(context);
				break;
		}
	}

	@Override
	protected int provideContentViewId() {
		return R.layout.activity_about;
	}
}

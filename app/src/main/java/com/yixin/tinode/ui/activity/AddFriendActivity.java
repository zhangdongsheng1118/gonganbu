package com.yixin.tinode.ui.activity;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yixin.tinode.R;
import com.yixin.tinode.model.cache.UserCache;
import com.yixin.tinode.ui.base.BaseActivity;
import com.yixin.tinode.ui.presenter.AddFriendAtPresenter;
import com.yixin.tinode.ui.view.IAddFriendAtView;
import com.yixin.tinode.util.UIUtils;


import butterknife.BindView;
import co.tinode.tinodesdk.Tinode;

/**
 * @创建者 CSDN_LQR
 * @描述 添加朋友界面
 */

public class AddFriendActivity extends BaseActivity<IAddFriendAtView, AddFriendAtPresenter> implements IAddFriendAtView {

    @BindView(R.id.llSearchUser)
    LinearLayout mLlSearchUser;
    @BindView(R.id.tvAccount)
    TextView mTvAccount;

    @Override
    public void initView() {
        setToolbarTitle(UIUtils.getString(R.string.add_friend));

        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        String login = pref.getString(LoginActivity.PREFS_LAST_LOGIN, null);
        mTvAccount.setText(login + "");
    }

    @Override
    public void initListener() {
        mLlSearchUser.setOnClickListener(v -> jumpToActivity(SearchUserActivity.class));
    }

    @Override
    protected AddFriendAtPresenter createPresenter() {
        return new AddFriendAtPresenter(this);
    }

    @Override
    protected int provideContentViewId() {
        return R.layout.activity_add_friend;
    }
}

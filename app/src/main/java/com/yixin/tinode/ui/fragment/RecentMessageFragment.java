package com.yixin.tinode.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.lqr.recyclerview.LQRRecyclerView;
import com.yixin.tinode.R;
import com.yixin.tinode.app.AppConst;
import com.yixin.tinode.manager.BroadcastManager;
import com.yixin.tinode.ui.base.BaseFragment;
import com.yixin.tinode.ui.presenter.RecentMessageFgPresenter;
import com.yixin.tinode.ui.view.IRecentMessageFgView;
import com.zuozhan.app.activity.AllBaseActivity;
import com.zuozhan.app.activity.ZHMainActivity;

import java.util.Date;

import butterknife.BindView;


/**
 * 获取消息后，插入数据库，刷新列表。
 *
 * @创建者 CSDN_LQR
 * @描述 最近会话列表界面
 */
public class RecentMessageFragment extends BaseFragment<IRecentMessageFgView, RecentMessageFgPresenter> implements IRecentMessageFgView {

    private boolean isFirst = true;
    @BindView(R.id.rvRecentMessage)
    LQRRecyclerView mRvRecentMessage;

    public void datasetChanged(String topic, Date time, String from_id,String msg) {
        mPresenter.newMsgComing(topic, time, from_id, msg);
    }

    @Override
    public void init() {
        registerBR();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
//        super.setUserVisibleHint(isVisibleToUser);
        Log.e("RecentMessageFragment", "RecentMessageFragment setUserVisibleHint:" + isVisibleToUser);
        //初次调用时mpresenter可能未被初始化F
        if (isVisibleToUser) {
            //相当于Fragment的onResume
            //if (!isFirst) {
            //mPresenter.getConversationsOnce();
            //}
        } else {
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e("RecentMessageFragment", "RecentMessageFragment onResume");
        mPresenter.getConversations();
        isFirst = false;
        ((ZHMainActivity) getActivity()).setUnRead();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        unRegisterBR();
    }

    private void registerBR() {
        BroadcastManager.getInstance(getActivity()).register(AppConst.UPDATE_CONVERSATIONS, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mPresenter.getConversations();
            }
        });
    }

    private void unRegisterBR() {
        BroadcastManager.getInstance(getActivity()).unregister(AppConst.UPDATE_CONVERSATIONS);
    }

    @Override
    protected RecentMessageFgPresenter createPresenter() {
        return new RecentMessageFgPresenter((AllBaseActivity) getActivity());
    }

    @Override
    protected int provideContentViewId() {
        return R.layout.fragment_recent_message;
    }

    @Override
    public LQRRecyclerView getRvRecentMessage() {
        return mRvRecentMessage;
    }
}

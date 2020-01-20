package com.yixin.tinode.ui.view;


import android.widget.EditText;

import com.lqr.recyclerview.LQRRecyclerView;
import co.tinode.tinodesdk.model.AvatarPhoto;

import cn.bingoogolapple.refreshlayout.BGARefreshLayout;
import co.tinode.tinodesdk.Topic;

public interface ISessionAtView {

    BGARefreshLayout getRefreshLayout();

    LQRRecyclerView getRvMsg();

    EditText getEtContent();

    Topic getTopic();
    long getSearchMsgId();
    void setSearchMsgId(long searchMsgId);
}

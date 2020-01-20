package com.yixin.tinode.ui.view;

import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.lqr.recyclerview.LQRRecyclerView;

import cn.bingoogolapple.refreshlayout.BGARefreshLayout;
import co.tinode.tinodesdk.Topic;

public interface ICollectionAtView {
    EditText getEtSearchContent();

    RelativeLayout getRlNoResultTip();

    LinearLayout getLlSearch();

    public BGARefreshLayout getRefreshLayout();

    public LQRRecyclerView getRvMsg();
}

package com.yixin.tinode.ui.view;


import com.baidu.mapapi.search.core.PoiInfo;
import com.lqr.recyclerview.LQRRecyclerView;

public interface IMyLocationAtView {
    LQRRecyclerView getRvPOI();

    public void onItemClick(PoiInfo poiInfo);
}

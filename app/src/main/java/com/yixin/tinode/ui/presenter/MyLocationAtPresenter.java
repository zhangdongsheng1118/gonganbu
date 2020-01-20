package com.yixin.tinode.ui.presenter;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import com.baidu.mapapi.search.core.PoiInfo;
import com.lqr.adapter.LQRAdapterForRecyclerView;
import com.lqr.adapter.LQRViewHolderForRecyclerView;
import com.yixin.tinode.R;
import com.yixin.tinode.model.data.LocationData;
import com.yixin.tinode.ui.base.BaseActivity;
import com.yixin.tinode.ui.base.BasePresenter;
import com.yixin.tinode.ui.view.IMyLocationAtView;

import java.util.ArrayList;
import java.util.List;

public class MyLocationAtPresenter extends BasePresenter<IMyLocationAtView> {

    private List<PoiInfo> mData = new ArrayList<>();
    private int mSelectedPosi = 0;
    private LQRAdapterForRecyclerView<PoiInfo> mAdapter;

    public MyLocationAtPresenter(BaseActivity context) {
        super(context);
    }

    public void loadData(List<PoiInfo> obj) {
        mData.clear();
        mData.addAll(obj);
        setAdapter();
    }

    private void setAdapter() {
        if (mAdapter == null) {
            mAdapter = new LQRAdapterForRecyclerView<PoiInfo>(mContext, mData, R.layout.item_location_poi) {
                @Override
                public void convert(LQRViewHolderForRecyclerView helper, PoiInfo item, int position) {
                    helper.setText(R.id.tvTitle, item.name).setText(R.id.tvDesc, item.address)
                            .setViewVisibility(R.id.ivSelected, mSelectedPosi == position ? View.VISIBLE : View.GONE);
                }
            };
            getView().getRvPOI().setAdapter(mAdapter);
            mAdapter.setOnItemClickListener((helper, parent, itemView, position) -> {
                mSelectedPosi = position;
                getView().onItemClick(mData.get(position));
            });
        } else {
            mAdapter.notifyDataSetChangedWrapper();
        }
    }

    public void sendLocation() {
        if (mData != null && mData.size() > mSelectedPosi) {
            PoiInfo poi = mData.get(mSelectedPosi);
            Intent data = new Intent();
            LocationData locationData = new LocationData(poi.location.latitude, poi.location.longitude, poi.name, getMapUrl(poi.location.latitude, poi.location.longitude));
            data.putExtra("location", locationData);
            mContext.setResult(Activity.RESULT_OK, data);
            mContext.finish();
        }
    }


    //    获取位置静态图
    //    http://apis.map.qq.com/ws/staticmap/v2/?center=39.8802147,116.415794&zoom=10&size=600*300&maptype=landform&markers=size:large|color:0xFFCCFF|label:k|39.8802147,116.415794&key=OB4BZ-D4W3U-B7VVO-4PJWW-6TKDJ-WPB77
    //    http://st.map.qq.com/api?size=708*270&center=114.215843,22.685120&zoom=17&referer=weixin
    //    http://st.map.qq.com/api?size=708*270&center=116.415794,39.8802147&zoom=17&referer=weixin
    private String getMapUrl(double latitude, double longitude) {

        return "http://api.map.baidu.com/staticimage?ak=${ak}&mcode=${mcode}&width=708&height=270&center=${lat},${lng}&zoom=17&markers=${lat},${lng}&markerStyles=l,&copyright=1"
                .replace("${ak}", "h22B9sWB8P6y6q5XcN6xi3yKpSuNeNSf")
                .replace("${mcode}", "64:3A:97:AE:B4:C3:95:9D:AA:E3:57:BC:D8:67:9D:EE:91:27:E6:CC;com.yixin.tinode")
                .replace("${lng}", "" + latitude).replace("${lat}", "" + longitude);
    }
}

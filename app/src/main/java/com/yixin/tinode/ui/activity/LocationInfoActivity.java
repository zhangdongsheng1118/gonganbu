package com.yixin.tinode.ui.activity;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.blankj.utilcode.util.ToastUtils;
import com.lqr.recyclerview.LQRRecyclerView;
import com.yixin.tinode.R;
import com.yixin.tinode.model.data.LocationData;
import com.yixin.tinode.ui.base.BaseActivity;
import com.yixin.tinode.ui.presenter.MyLocationAtPresenter;
import com.yixin.tinode.ui.view.IMyLocationAtView;
import com.yixin.tinode.util.LogUtils;
import com.yixin.tinode.util.UIUtils;
import com.zhy.autolayout.AutoLinearLayout;

import java.util.List;

import butterknife.BindView;


/**
 * @创建者 CSDN_LQR
 * @描述
 */
public class LocationInfoActivity extends BaseActivity<IMyLocationAtView, MyLocationAtPresenter> implements IMyLocationAtView, BaiduMap.OnMapStatusChangeListener {

    private LocationClient mLocClient;
    private BaiduMap mBaiduMap;

    @BindView(R.id.rlMap)
    RelativeLayout mRlMap;
    @BindView(R.id.map)
    MapView mMap;
    @BindView(R.id.ibShowLocation)
    ImageButton mIbShowLocation;
    @BindView(R.id.tv_name)
    TextView mTvName;

    private boolean isButtonLoc = false;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(getApplicationContext());

        super.onCreate(savedInstanceState);
    }

    private void showMarker(LatLng point) {
//        mBaiduMap.clear();
//
        //构建Marker图标
        BitmapDescriptor bitmap = BitmapDescriptorFactory
                .fromResource(R.drawable.icon_gcoding);
//构建MarkerOption，用于在地图上添加Marker
        OverlayOptions option = new MarkerOptions()
                .position(point)
                .icon(bitmap);
//在地图上添加Marker，并显示
        mBaiduMap.addOverlay(option);
    }

    @Override
    public void initView() {
        mBaiduMap = mMap.getMap();
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        // 定位初始化
        mLocClient = new LocationClient(context);
        mLocClient.registerLocationListener(new BDAbstractLocationListener() {

            @Override
            public void onReceiveLocation(BDLocation location) {
                // map view 销毁后不在处理新接收的位置
                if (location == null || mMap == null) {
                    return;
                }


                MyLocationData locData = new MyLocationData.Builder()
                        .accuracy(location.getRadius()).latitude(location.getLatitude())
                        .longitude(location.getLongitude()).build();
                mBaiduMap.setMyLocationData(locData);
                if (isButtonLoc) {
                    isButtonLoc = false;
                    LatLng ll = new LatLng(location.getLatitude(),
                            location.getLongitude());
                    MapStatus.Builder builder = new MapStatus.Builder();
                    builder.target(ll).zoom(18.0f);
                    mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
                }
                mLocClient.stop();
            }
        });
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);
        mLocClient.setLocOption(option);
        mLocClient.start();
        mBaiduMap.setOnMapStatusChangeListener(this);

        mMap.showZoomControls(false);

        LocationData locationData = (LocationData) getIntent().getSerializableExtra("location");
        LatLng point = new LatLng(locationData.getLat(), locationData.getLng());
        showMarker(point);
        MapStatus.Builder builder = new MapStatus.Builder();
        builder.target(point).zoom(18.0f);
        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
        mTvName.setText(locationData.getPoi());
    }

    @Override
    public void initData() {
        requestLocationUpdate();
    }


    @Override
    public void onDestroy() {
        // 退出时销毁定位
        super.onDestroy();
        mLocClient.stop();
        // 关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
    }

    @Override
    public void initListener() {
        mIbShowLocation.setOnClickListener(v -> {
            isButtonLoc = true;
            requestLocationUpdate();
        });
//        mSensorManager.registerListener(MyLocationActivity.this, mOritationSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void requestLocationUpdate() {
        mLocClient.start();
    }

    @Override
    protected MyLocationAtPresenter createPresenter() {
        return new MyLocationAtPresenter(this);
    }

    @Override
    protected int provideContentViewId() {
        return R.layout.activity_location_info;
    }

//
//    @Override
//    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
//            search(view.getSearchText());
//        }
//        return false;
//    }


    @Override
    public void onItemClick(PoiInfo poiInfo) {
    }


    @Override
    public LQRRecyclerView getRvPOI() {
        return null;
    }

    @Override
    public void onMapStatusChangeFinish(MapStatus mapStatus) {

    }

    @Override
    public void onMapStatusChangeStart(MapStatus mapStatus) {

    }

    @Override
    public void onMapStatusChangeStart(MapStatus mapStatus, int i) {

    }

    @Override
    public void onMapStatusChange(MapStatus mapStatus) {

    }

}

package com.yixin.tinode.ui.activity;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
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
public class MyLocationActivity extends BaseActivity<IMyLocationAtView, MyLocationAtPresenter> implements IMyLocationAtView, OnGetPoiSearchResultListener, OnGetGeoCoderResultListener, BaiduMap.OnMapStatusChangeListener, SensorEventListener {

    int maxHeight = UIUtils.dip2Px(300);
    int minHeight = UIUtils.dip2Px(150);

    private SensorManager mSensorManager;
    private Sensor mOritationSensor;
    private LocationClient mLocClient;
    private BaiduMap mBaiduMap;
    GeoCoder mSearch;
    private PoiSearch mPoiSearch;

    @BindView(R.id.btnToolbarSend)
    Button mBtnToolbarSend;
    @BindView(R.id.rlMap)
    RelativeLayout mRlMap;
    @BindView(R.id.map)
    MapView mMap;
    @BindView(R.id.ibShowLocation)
    ImageButton mIbShowLocation;
    @BindView(R.id.rvPOI)
    LQRRecyclerView mRvPOI;
    @BindView(R.id.pb)
    ProgressBar mPb;
    @BindView(R.id.ivPin)
    ImageView mIvPin;

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
//        //构建Marker图标
//        BitmapDescriptor bitmap = BitmapDescriptorFactory
//                .fromResource(R.drawable.icon_gcoding);
////构建MarkerOption，用于在地图上添加Marker
//        OverlayOptions option = new MarkerOptions()
//                .position(point)
//                .icon(bitmap);
////在地图上添加Marker，并显示
//        mBaiduMap.addOverlay(option);
        if (!mIvPin.isShown()) {
            mIvPin.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void initView() {
        mBtnToolbarSend.setVisibility(View.VISIBLE);
        setRlMapHeight(maxHeight);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mOritationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
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
                if (isFirstLoc || isButtonLoc) {
                    isFirstLoc = false;
                    isButtonLoc = false;
                    LatLng ll = new LatLng(location.getLatitude(),
                            location.getLongitude());
                    MapStatus.Builder builder = new MapStatus.Builder();
                    builder.target(ll).zoom(18.0f);
                    mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
                    searchNearBy(ll);

                    showMarker(ll);
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
        mSearch = GeoCoder.newInstance();
        mSearch.setOnGetGeoCodeResultListener(this);
        mPoiSearch = PoiSearch.newInstance();
        mPoiSearch.setOnGetPoiSearchResultListener(this);

        mMap.showZoomControls(false);
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
        mSearch.destroy();
    }

    @Override
    public void initListener() {
        mBtnToolbarSend.setOnClickListener(v -> mPresenter.sendLocation());
        mRvPOI.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 && Math.abs(dy) > 10 && ((GridLayoutManager) mRvPOI.getLayoutManager()).findFirstCompletelyVisibleItemPosition() <= 1 && mRlMap.getHeight() == maxHeight) {
                    LogUtils.sf("上拉缩小");
                    setRlMapHeight(minHeight);
                    UIUtils.postTaskDelay(() -> mRvPOI.moveToPosition(0), 0);
                } else if (dy < 0 && Math.abs(dy) > 10 && ((GridLayoutManager) mRvPOI.getLayoutManager()).findFirstCompletelyVisibleItemPosition() == 1 && mRlMap.getHeight() == minHeight) {
                    LogUtils.sf("下拉放大");
                    setRlMapHeight(maxHeight);
                    UIUtils.postTaskDelay(() -> mRvPOI.moveToPosition(0), 0);
                }
            }
        });
        mIbShowLocation.setOnClickListener(v -> requestLocationUpdate());
//        mSensorManager.registerListener(MyLocationActivity.this, mOritationSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void requestLocationUpdate() {
        isButtonLoc = true;
        mLocClient.start();
    }

    private void setRlMapHeight(int height) {
        AutoLinearLayout.LayoutParams params = (AutoLinearLayout.LayoutParams) mRlMap.getLayoutParams();
        params.height = height;
        mRlMap.setLayoutParams(params);
    }

    @Override
    protected MyLocationAtPresenter createPresenter() {
        return new MyLocationAtPresenter(this);
    }

    @Override
    protected int provideContentViewId() {
        return R.layout.activity_my_location;
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
//        if (myLocation != null) {
//            myLocation.setRotation(event.values[0]);
//        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private double latitude; // 经度
    private double longitude; // 维度

    private void search(String message) {
        mPb.setVisibility(View.VISIBLE);
        mRvPOI.setVisibility(View.GONE);

        mPoiSearch.searchNearby(new PoiNearbySearchOption().keyword(message).location(new LatLng(latitude, longitude)).radius(50000).pageNum(1));
//        view.keybordState(false);
//        view.searchText("");
    }

    public void searchNearBy(LatLng latLng) {
        mPb.setVisibility(View.VISIBLE);
        mRvPOI.setVisibility(View.GONE);
        mSearch.reverseGeoCode(new ReverseGeoCodeOption()
                .location(latLng));
    }

//
//    @Override
//    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
//            search(view.getSearchText());
//        }
//        return false;
//    }


    boolean isFirstLoc = true; // 是否首次定位


    @Override
    public void onItemClick(PoiInfo poiInfo) {
        MapStatus.Builder builder = new MapStatus.Builder();
        builder.target(poiInfo.location).zoom(18.0f);
        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
        latitude = poiInfo.location.latitude;
        longitude = poiInfo.location.longitude;

        showMarker(poiInfo.location);
    }


    @Override
    public LQRRecyclerView getRvPOI() {
        return mRvPOI;
    }


    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {

        mPb.setVisibility(View.GONE);
        mRvPOI.setVisibility(View.VISIBLE);
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            ToastUtils.showShort("未找到结果");
            return;
        }

        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            latitude = result.getLocation().latitude;
            longitude = result.getLocation().longitude;

            List<PoiInfo> poiInfoList = result.getPoiList();
            mPresenter.loadData(poiInfoList);
        }
    }

    @Override
    public void onGetPoiResult(PoiResult result) {

        mPb.setVisibility(View.GONE);
        mRvPOI.setVisibility(View.VISIBLE);

        if (result == null || result.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
            ToastUtils.showShort("未找到结果");
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            mPresenter.loadData(result.getAllPoi());
            return;
        }
    }

    @Override
    public void onMapStatusChangeFinish(MapStatus mapStatus) {

        LatLng ll = mapStatus.target;
        searchNearBy(ll);

        showMarker(ll);
    }

    @Override
    public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {

    }

    @Override
    public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {

    }

    @Override
    public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

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

package com.yixin.tinode.ui.activity;

import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
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
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.yixin.tinode.R;
import com.yixin.tinode.model.data.LocationData;
import com.yixin.tinode.tinode.Cache;
import com.yixin.tinode.tinode.util.UiUtils;
import com.yixin.tinode.tinode.widgets.LetterTileDrawable;
import com.yixin.tinode.ui.base.BaseActivity;
import com.yixin.tinode.ui.presenter.MapAtPresenter;
import com.yixin.tinode.ui.view.IMapAtView;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import co.tinode.tinodesdk.ComTopic;
import co.tinode.tinodesdk.NotConnectedException;
import co.tinode.tinodesdk.NotSynchronizedException;
import co.tinode.tinodesdk.Tinode;
import co.tinode.tinodesdk.Topic;
import co.tinode.tinodesdk.model.Description;
import co.tinode.tinodesdk.model.MsgServerData;
import co.tinode.tinodesdk.model.MsgServerInfo;
import co.tinode.tinodesdk.model.MsgServerMeta;
import co.tinode.tinodesdk.model.MsgServerPres;
import co.tinode.tinodesdk.model.Subscription;
import co.tinode.tinodesdk.model.VCard;

import static com.yixin.tinode.util.Utils.isNetworkConnected;
import static com.yixin.tinode.util.Utils.showNetworkAlert;

public class MapActivity extends BaseActivity<IMapAtView, MapAtPresenter> implements BaiduMap.OnMapStatusChangeListener {
    private String TAG = "MapActivity";
    private LocationClient mLocClient;
    private BaiduMap mBaiduMap;
    private Timer timer;
    @BindView(R.id.rlMap)
    RelativeLayout mRlMap;
    @BindView(R.id.map)
    MapView mMap;
    @BindView(R.id.ibShowLocation)
    ImageButton mIbShowLocation;
    Map<String, Marker> markerMap;
//    @BindView(R.id.tv_name)
//    TextView mTvName;

    private boolean isButtonLoc = false;
    private String mTopicName;
    private Topic mTopic = null;

    class MyTask extends TimerTask {

        @Override
        public void run() {
            mPresenter.sendRealTimePositionMessage(new LocationData(mLocClient.getLastKnownLocation().getLatitude(), mLocClient.getLastKnownLocation().getLongitude(), "", ""), mTopic);

        }

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(getApplicationContext());
        markerMap = new HashMap<>();
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mTopicName = getIntent().getStringExtra("TopicName");
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancel(mTopicName, 0);
        final Tinode tinode = Cache.getTinode();
        tinode.setListener(new UiUtils.EventListener(this, tinode.isConnected()));
        mTopic = tinode.getTopic(mTopicName);
        if (mTopic == null) {
            Log.e(TAG, "Attempt to instantiate an unknown topic: " + mTopicName);
            mTopic = (ComTopic<VCard>) tinode.newTopic(mTopicName, null);
        }
        mTopic.setListener(new TListener());
        subscribe(tinode, mTopic);
        VCard pub = (VCard) mTopic.getPub();
        mToolbarTitle.setText(pub.fn + " 群位置共享");

    }

    /**
     * 显示目标位置
     *
     * @param point
     */
    private void showMarker(String from, LatLng point) {
        Topic topic = Cache.getTinode().getTopic(from);
        if (topic == null) {
            return;
        }
        VCard toPub = (VCard) topic.getPub();
//        UiUtils.assignBitmap(mContext, ivAvatar,
//                toPub != null ? toPub.getBitmap() : null,
//                toPub != null ? toPub.fn : null,
//                topic.getName());

        Bitmap bmp = toPub.getBitmap();
        BitmapDescriptor bitmap;
        if (bmp != null) {
            RoundedBitmapDrawable roundedBitmapDrawable1 = RoundedBitmapDrawableFactory.create(getResources(), bmp);
            roundedBitmapDrawable1.setCircular(true);

            //构建Marker图标
            bitmap = BitmapDescriptorFactory.fromBitmap(UiUtils.drawableToBitmap(roundedBitmapDrawable1));
        } else {
            LetterTileDrawable drawable = new LetterTileDrawable(context.getResources());
            drawable.setContactTypeAndColor(
                    Topic.getTopicTypeByName(topic.getName()) == Topic.TopicType.P2P ?
                            LetterTileDrawable.TYPE_PERSON : LetterTileDrawable.TYPE_GROUP)
                    .setLetterAndColor(toPub != null ? toPub.fn : null, topic.getName())
                    .setIsCircular(true);
            bitmap = BitmapDescriptorFactory.fromBitmap(UiUtils.drawableToBitmap(drawable));
        }

        //构建MarkerOption，用于在地图上添加Marker
        OverlayOptions option = new MarkerOptions()
                .position(point)
                .icon(bitmap);
        if (markerMap.containsKey(from)) {
            markerMap.get(from).remove();
        }
        //在地图上添加Marker，并显示
        Marker marker = (Marker) mBaiduMap.addOverlay(option);
        markerMap.put(from, marker);
    }

    /**
     * 清理已经离开的人
     *
     * @param from
     */
    private void cleanMember(String from) {
        if (markerMap.containsKey(from)) {
            markerMap.get(from).remove();
            markerMap.remove(from);
        }
    }

    @Override
    public void initView() {
        mBaiduMap = mMap.getMap();
        mBaiduMap.clear();
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
//                mLocClient.stop();
                if (timer == null) {
                    timer = new Timer();
                    timer.schedule(new MyTask(), 1000, 10000);
                }
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

//        LocationData locationData = (LocationData) getIntent().getSerializableExtra("location");
//        LatLng point = new LatLng(locationData.getLat(), locationData.getLng());
//        showMarker(point);
//        MapStatus.Builder builder = new MapStatus.Builder();
//        builder.target(point).zoom(18.0f);
//        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
//        mTvName.setText(locationData.getPoi());
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
        mBaiduMap.clear();
        timer.cancel();
        timer.purge();
        timer = null;
        mPresenter.sendLeaveMessage(new LocationData(mLocClient.getLastKnownLocation().getLatitude(), mLocClient.getLastKnownLocation().getLongitude(), "", ""), mTopic);
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

    @Override
    protected MapAtPresenter createPresenter() {
        return new MapAtPresenter(this);
    }

    private void requestLocationUpdate() {
        mLocClient.start();
    }

    @Override
    protected int provideContentViewId() {
        return R.layout.activity_map;
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

    private void subscribe(Tinode tinode, Topic me) {
        if (!me.isAttached()) {
            try {
                Log.d(TAG, "Trying to subscribe to me");
                me.subscribe(null, me
                        .getMetaGetBuilder()
                        .withGetDesc()
                        .withGetSub()
                        .withGetData()
                        .build());
            } catch (NotSynchronizedException ignored) {
                /* */
            } catch (NotConnectedException ignored) {
                if (!isNetworkConnected(this)) {
                    showNetworkAlert(context);
                } else {
                    /* offline - ignored */
                    Toast.makeText(this, R.string.no_connection, Toast.LENGTH_SHORT).show();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            com.yixin.tinode.util.Utils.reconnect(context, tinode, new com.yixin.tinode.util.Utils.ReconnectListener() {
                                @Override
                                public void after() {
                                    subscribe(tinode, me);
                                }
                            });
                        }
                    }).start();
                }

            } catch (Exception err) {
                Log.i(TAG, "Subscription failed " + err.getMessage());
                Toast.makeText(this,
                        "连接通讯服务器失败", Toast.LENGTH_LONG).show();
            }
        }
    }

    private class TListener extends Topic.Listener {
        @Override
        public void onSubscribe(int code, String text) {
            super.onSubscribe(code, text);
        }

        @Override
        public void onData(MsgServerData data) {
            super.onData(data);
            if (!data.content.txt.contains("isRTPMsg")) {
                return;
            }
            if (data.content.txt.contains("isLeave")) {
                cleanMember(data.from);
                return;
            }
            JSONObject jsonObject = JSON.parseObject(data.content.txt);
            LatLng point = new LatLng(jsonObject.getDouble("lat"), jsonObject.getDouble("lng"));
            showMarker(data.from, point);
        }

        @Override
        public void onPres(MsgServerPres pres) {
            super.onPres(pres);
        }

        @Override
        public void onInfo(MsgServerInfo info) {
            super.onInfo(info);
        }

        @Override
        public void onMeta(MsgServerMeta meta) {
            super.onMeta(meta);
        }

        @Override
        public void onMetaSub(Subscription sub) {
            super.onMetaSub(sub);
        }

        @Override
        public void onContUpdate(Subscription sub) {
            super.onContUpdate(sub);
        }

        @Override
        public void onLeave(boolean unsub, int code, String text) {
            super.onLeave(unsub, code, text);
        }

        @Override
        public void onMetaDesc(Description desc) {
            super.onMetaDesc(desc);
        }

        @Override
        public void onMetaTags(String[] tags) {
            super.onMetaTags(tags);
        }

        @Override
        public void onOnline(boolean online) {
            super.onOnline(online);
        }

        @Override
        public void onSubsUpdated() {
            super.onSubsUpdated();
        }
    }
}

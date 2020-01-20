package com.zuozhan.app.util;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;

import com.google.gson.Gson;
import com.starrtc.demo.utils.AEvent;
import com.yixin.tinode.app.MyApp;
import com.zuozhan.app.AppEnvirment;
import com.zuozhan.app.BaseIP;
import com.zuozhan.app.bean.LocationBean;

import java.io.IOException;
import java.net.Socket;

public class LocationUtil {

    boolean isStop = false;

    static LocationUtil locationUtil = new LocationUtil();
    LocationManager locationManager; //系统定位
    public String mLongitude = ""; // 经度
    public String mLatitude = ""; // 维度
    private String mName = ""; // 维度
    public double clongitude; // 经度
    public double clatitude; // 维度

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public String getMissionId() {
        return missionId;
    }

    public void setMissionId(String missionId) {
        this.missionId = missionId;
    }

    private String caseId = ""; //
    private String missionId = ""; //

    HandlerThread handlerThread = new HandlerThread("location");
    Handler handler;

    private LocationUtil() {
        if (locationManager == null) {
            //获取定位服务
            locationManager = (LocationManager) MyApp.getInstance().getSystemService(Context.LOCATION_SERVICE);

        }
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
    }


    public static LocationUtil getIntance() {
        if (locationUtil == null) {
            locationUtil = new LocationUtil();
        }

        return locationUtil;
    }

    public void start() {
        isStop = false;
        startLocation();
        handler.post(new Runnable() {
            @Override
            public void run() {
                startUploadLocation();
            }
        });
        looperLocation();
    }

    private void looperLocation() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startUploadLocation();
                if (!isStop) {
                    looperLocation();
                }
            }
        }, 1000 * BaseIP.location_upload_time);
    }

    public void stop() {
        handler.removeCallbacksAndMessages(null);
        isStop = true;
        stopLocation();
    }

    public void startLocation() {
        boolean t = ActivityCompat.checkSelfPermission(MyApp.getInstance(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(MyApp.getInstance(), Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        if (t) {
            if (locationManager == null) {
                //获取定位服务
                locationManager = (LocationManager) MyApp.getInstance().getSystemService(Context.LOCATION_SERVICE);
            }
            String bestProvider = getProvider();
            Location location = locationManager.getLastKnownLocation(bestProvider);

            if (location != null) {
                clongitude = location.getLongitude();
                clatitude = location.getLatitude();
                AEvent.notifyListener(AEvent.AEVENT_LOCATION,true,"纬度：" + mLatitude + "经度:  " + mLongitude);
                mLongitude = String.valueOf(location.getLongitude());
                mLatitude = String.valueOf(location.getLatitude());
            } else {
                ToastUtils.showDebugToast("获取位置失败");
            }
            locationManager.removeUpdates(locationListener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, BaseIP.location_time * 1000,
                    1, locationListener);
            LogUtil.d("location", "bestProvider = " + bestProvider + "-Location" + "纬度：" + mLatitude + "经度:  " + mLongitude);
        } else {
            ToastUtils.showDebugToast("定位权限未开启，定位失败");
        }
    }

    public void startUploadLocation() {
        upload();
    }

    private void upload() {
        LocationBean locationBean = new LocationBean();
        try {
            if (AppEnvirment.getUserBean() != null && AppEnvirment.getUserBean().data != null) {
                locationBean.user_id = AppEnvirment.getUserBean().data.id + "";
                BatteryManager batteryManager = (BatteryManager) AppEnvirment.getApplication().getSystemService(Context.BATTERY_SERVICE);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    int battery = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
                    locationBean.battery = battery + "";
                } else {
                    locationBean.battery = "0";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        locationBean.capture_time = System.currentTimeMillis() + "";
        locationBean.lat = mLatitude + "";
        locationBean.lon = mLongitude + "";
        locationBean.missionId = missionId;
        locationBean.caseId = caseId;
        startClient(BaseIP.rtc_ip, 10010, new Gson().toJson(locationBean));
    }

//        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60 * 1000, 0, locationListener);
//        locationManager.requestLocationUpdates(bestProvider, 60 * 1000, 0, locationListener);


    public void stopLocation() {
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }
    }


    private LocationListener locationListener = new LocationListener() {
        /**
         * 位置信息变化时触发:当坐标改变时触发此函数，如果Provider传进相同的坐标，它就不会被触发
         * @param location
         */
        @Override
        public void onLocationChanged(Location location) {
            clongitude = location.getLongitude();
            clatitude = location.getLatitude();
            mLongitude = String.valueOf(location.getLongitude());
            mLatitude = String.valueOf(location.getLatitude());
            AEvent.notifyListener(AEvent.AEVENT_LOCATION,true,"纬度：" + mLatitude + "经度:  " + mLongitude);
            LogUtil.d("location", "onLocationChanged = "+ "-Location" + "纬度：" + mLatitude + "经度:  " + mLongitude);
        }

        /**
         * GPS状态变化时触发:Provider被disable时触发此函数，比如GPS被关闭
         * @param provider
         * @param status
         * @param extras
         */
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            LogUtil.d("location", "onStatusChanged = -Location" + status + "provider = " + provider);
            switch (status) {
                //GPS状态为可见时
                case LocationProvider.AVAILABLE:
                    ToastUtils.showDebugToast("当前GPS正常");
                    break;
                //GPS状态为服务区外时
                case LocationProvider.OUT_OF_SERVICE:
                    ToastUtils.showDebugToast("当前GPS不可用-服务区外");
                    break;
                //GPS状态为暂停服务时
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    ToastUtils.showDebugToast("当前GPS不可用-暂停");
                    break;
            }
        }

        /**
         * 方法描述：GPS开启时触发
         * @param provider
         */
        @Override
        public void onProviderEnabled(String provider) {
        }

        /**
         * 方法描述： GPS禁用时触发
         * @param provider
         */
        @Override
        public void onProviderDisabled(String provider) {
        }
    };

    public boolean checkAndOpenGPS(Context context) {
        boolean isOpen = true;
        if (!locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
            ToastUtils.showToast("请打开gps定位");
            // 转到手机设置界面，用户设置GPS
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            isOpen = false;
        }
        return isOpen;
    }

    /**
     * 定位查询条件
     * 返回查询条件 ，获取目前设备状态下，最适合的定位方式
     */
    private String getProvider() {
        // 构建位置查询条件
        Criteria criteria = new Criteria();
        // 设置定位精确度 Criteria.ACCURACY_COARSE比较粗略，Criteria.ACCURACY_FINE则比较精细
        //Criteria.ACCURACY_FINE,当使用该值时，在建筑物当中，可能定位不了,建议在对定位要求并不是很高的时候用Criteria.ACCURACY_COARSE，避免定位失败
        // 查询精度：高
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        // 设置是否要求速度
        criteria.setSpeedRequired(false);
        // 是否查询海拨：否
        criteria.setAltitudeRequired(false);
        // 是否查询方位角 : 否
        criteria.setBearingRequired(false);
        // 是否允许付费：是
        criteria.setCostAllowed(true);
        // 电量要求：低
        criteria.setPowerRequirement(Criteria.POWER_HIGH);
        // 返回最合适的符合条件的provider，第2个参数为true说明 , 如果只有一个provider是有效的,则返回当前provider
        return locationManager.getBestProvider(criteria, true);
    }


    public Socket socket;

    private void startClient(final String address, final int port, final String msg) {
        ThreadPoolManager.runSubThread(new Runnable() {
            @Override
            public void run() {
                if (socket == null || socket.isClosed()) {
                    try {
                        LogUtil.d("location启动客户端");
                        socket = new Socket(address, port);
                        LogUtil.d("location客户端连接成功");
//                        PrintWriter pw = new PrintWriter(socket.getOutputStream());
//                        InputStream inputStream = socket.getInputStream();
//                        byte[] buffer = new byte[1024];
//                        int len = -1;
//                        while ((len = inputStream.read(buffer)) != -1) {
//                            String data = new String(buffer, 0, len);
//                            LogUtil.d("location收到服务器的数据---------------------------------------------:" + data);
////                            EventBus.getDefault().post(new MessageClient(data));
//                        }
                        LogUtil.d("location客户端连接完成");
//                        pw.close();
                    } catch (Exception EE) {
                        EE.printStackTrace();
                        LogUtil.d("location客户端无法连接服务器");

                    } finally {
//                        try {
//                            socket.close();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                        socket = null;
                    }
                }

                sendTcpMessage(msg);
            }
        });

    }

    public void sendTcpMessage(final String msg) {
        ThreadPoolManager.runSubThread(new Runnable() {
            @Override
            public void run() {
                if (socket != null && socket.isConnected()) {
                    try {
                        socket.getOutputStream().write((msg + "\n").getBytes());
                        socket.getOutputStream().flush();
                        LogUtil.d("location上报成功 = " + (msg + "\n"));
                    } catch (IOException e) {
                        try {
                            socket.close();
                            socket = null;
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                        e.printStackTrace();
                    }
                }
            }
        });

    }

}

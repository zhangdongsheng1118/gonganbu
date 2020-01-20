package com.zuozhan.app.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.starrtc.demo.demo.MLOC;
import com.starrtc.demo.demo.VoipHttpListener;
import com.starrtc.demo.demo.ZHHttpManager;
import com.starrtc.demo.demo.voip.VoipAudioActivity;
import com.starrtc.demo.listener.XHSuperRoomManagerListener;
import com.starrtc.demo.utils.AEvent;
import com.starrtc.starrtcsdk.api.XHClient;
import com.starrtc.starrtcsdk.api.XHConstants;
import com.starrtc.starrtcsdk.api.XHSuperRoomManager;
import com.starrtc.starrtcsdk.apiInterface.IXHResultCallback;
import com.starrtc.starrtcsdk.core.audio.StarRTCAudioManager;
import com.starrtc.starrtcsdk.core.pusher.XHCameraRecorder;
import com.yixin.tinode.R;
import com.yixin.tinode.tinode.Cache;
import com.yixin.tinode.util.PopupWindowUtils;
import com.zuozhan.app.AppEnvirment;
import com.zuozhan.app.BaseIP;
import com.zuozhan.app.LocationTestActivity;
import com.zuozhan.app.RouterUtil;
import com.zuozhan.app.bean.BaseBean;
import com.zuozhan.app.bean.DuijiangBean;
import com.zuozhan.app.bean.FileBean;
import com.zuozhan.app.bean.MyUtil;
import com.zuozhan.app.bean.RenWuLeiBean;
import com.zuozhan.app.httpUtils.HttpUtil;
import com.zuozhan.app.photo.imagepicker.ImagePicker;
import com.zuozhan.app.photo.imagepicker.bean.ImageItem;
import com.zuozhan.app.photo.imagepicker.ui.ImageGridActivity;
import com.zuozhan.app.treelist.Node;
import com.zuozhan.app.treelist.OnTreeNodeClickListener;
import com.zuozhan.app.treelist.SimpleTreeRecyclerAdapter;
import com.zuozhan.app.util.LocationUtil;
import com.zuozhan.app.util.LogUtil;
import com.zuozhan.app.util.ThreadPoolManager;
import com.zuozhan.app.util.ToastUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import butterknife.BindView;
import co.tinode.tinodesdk.MeTopic;

public class ZHRenwuInfoActivity extends AllBaseActivity {
    public static String CREATER_ID = "CREATER_ID";          //创建者ID
    public static String LIVE_TYPE = "LIVE_TYPE";           //创建信息
    public static String LIVE_ID = "LIVE_ID";            //直播ID
    public static String LIVE_NAME = "LIVE_NAME";          //直播名称

    @BindView(R.id.title_left)
    View title_left;
    @BindView(R.id.content_view)
    LinearLayout content_view;
    @BindView(R.id.title_center_image)
    View title_center_image;
    @BindView(R.id.title_center)
    TextView title_center;
    @BindView(R.id.renwu_info_all_upload)
    View renwu_info_all_upload;
    @BindView(R.id.renwu_info_all_people)
    View renwu_info_all_people;
    @BindView(R.id.rinfo_btn1)
    View rinfo_btn1;
    @BindView(R.id.red_text)
    TextView red_text;
    @BindView(R.id.renwu_btn2)
    View renwu_btn2;
    @BindView(R.id.rwanjian)
    TextView rwanjian;
    @BindView(R.id.rwmingcheng)
    TextView rwmingcheng;
    @BindView(R.id.fuzeren)
    TextView fuzeren;
    @BindView(R.id.duixiang)
    TextView duixiang;
    @BindView(R.id.push_btn)
    TextView push_btn;
    @BindView(R.id.renwu_btn3)
    View renwu_btn3;
    @BindView(R.id.renwu_btn4)
    View renwu_btn4;
    @BindView(R.id.renwu_btn5)
    View renwu_btn5;
    @BindView(R.id.rinfo_btn0)
    View rinfo_btn0;
    @BindView(R.id.title_right)
    View title_right;
    @BindView(R.id.webview)
    WebView webview;

    private String mPrivateMsgTargetId;
    private XHSuperRoomManager superRoomManager;
    private String createrId;
    private String liveId;
    private String liveName;
    private XHConstants.XHSuperRoomType roomType;
    private StarRTCAudioManager starRTCAudioManager;

    RenWuLeiBean.DataBean dataBean;
    ComponentName  mComponent;
    AudioManager mAudioManager;
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zh_activity_renwuinfo);
        try {
            KeyguardManager km = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
            KeyguardManager.KeyguardLock kl = km.newKeyguardLock("name");
            kl.disableKeyguard();
        }catch (Exception e){}
        mAudioManager =(AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mComponent = new ComponentName(getPackageName(), VolumeKeyBroadcastReceiver.class.getName());
        mAudioManager.registerMediaButtonEventReceiver(mComponent);
        title_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        renwu_info_all_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopu(v);
            }
        });
        renwu_info_all_people.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                load("javascript:checkUserList();");
            }
        });
        renwu_btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                load("javascript:usershow();");
            }
        });
        renwu_btn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                load("javascript:devshow();");
            }
        });
        renwu_btn5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                load("javascript:carshow();");
            }
        });
        rinfo_btn0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                load("javascript:panTo('" + AppEnvirment.getUserBean().data.realName + "');");
            }
        });
        dataBean = (RenWuLeiBean.DataBean) getIntent().getSerializableExtra("info");

        MyUtil.setText(rwanjian, dataBean.caseName, "案件：");
        MyUtil.setText(rwmingcheng, dataBean.name, "任务名称：");
        MyUtil.setText(fuzeren, dataBean.responsibleUserName, "负责人：");
        MyUtil.setText(duixiang, dataBean.actionObject, "行动对象：");

        if (BaseIP.isDebug) {
            title_right.setVisibility(View.VISIBLE);
            title_right.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(ZHRenwuInfoActivity.this, LocationTestActivity.class));
                }
            });
        } else {
            title_right.setVisibility(View.GONE);
        }
        rinfo_btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (BaseIP.isShowMap) {
                        content_view.removeView(webview);
                        webview.stopLoading();
                        webview.destroy();
                        webview.setVisibility(View.GONE);
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                stop(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                        goHome(1, dataBean);
                    }
                });

            }
        });
        renwu_btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RouterUtil.goActivity(ZHRenwuInfoActivity.this, ZHExceptionListActivity.class,
                        dataBean.caseId);
            }
        });
        if (BaseIP.isShowMap) {
            initWebView(webview);
        } else {
            webview.setVisibility(View.GONE);
        }

        createrId = getIntent().getStringExtra(CREATER_ID);
        liveName = getIntent().getStringExtra(LIVE_NAME);
        liveId = getIntent().getStringExtra(LIVE_ID);
        roomType = (XHConstants.XHSuperRoomType) getIntent().getSerializableExtra(LIVE_TYPE);
        if (AEvent.RED == 0) {
            red_text.setText("0");
            red_text.setVisibility(View.GONE);
        } else {
            red_text.setVisibility(View.VISIBLE);
            red_text.setText("" + AEvent.RED);
        }
        title_center.setText("任务执行");
        AEvent.addListener(AEvent.AEVENT_VOIP_START, this);
        AEvent.addListener(AEvent.AEVENT_VOIP_STOP, this);
        initRTCAudio();

        LocationUtil.getIntance().setCaseId(dataBean.caseId);
        LocationUtil.getIntance().setMissionId(dataBean.id + "");
        LocationUtil.getIntance().start();

        String other = getIntent().getStringExtra("other");
        if ("0".equals(other)) {
            HttpUtil.addKeyNodesByToken(dataBean.caseId + "", dataBean.id + "",
                    LocationUtil.getIntance().mLongitude, LocationUtil.getIntance().mLatitude, "接收了任务", new HttpUtil.Callback<BaseBean>() {
                        @Override
                        public void onResponse(BaseBean call) {
                            if (call != null && call.code == 1) {
                                LogUtil.d("任务状态修改了");
                            }
                        }
                    });
        }
    }

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    private void initWebView(WebView webView) {
        try {
            WebSettings settings = webView.getSettings();

            settings.setLoadWithOverviewMode(true);//设置WebView是否使用预览模式加载界面。
            webView.setVerticalScrollBarEnabled(false);//不能垂直滑动
            webView.setHorizontalScrollBarEnabled(false);//不能水平滑动
            settings.setTextSize(WebSettings.TextSize.NORMAL);//通过设置WebSettings，改变HTML中文字的大小
            settings.setJavaScriptCanOpenWindowsAutomatically(true);//支持通过JS打开新窗口
            settings.setAllowFileAccess(false);
            settings.setJavaScriptEnabled(true);//设置js可用
            //设置WebView属性，能够执行Javascript脚本
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                    try {
                        handler.proceed();
                    }catch (Exception e){}
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    load("javascript:panTo('" + AppEnvirment.getUserBean().data.realName + "');");
                }
            });
//        webView.setWebChromeClient(new WebChromeClient());
            settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);//支持内容重新布局
            String url = BaseIP.IP_MAP + dataBean.caseId + "&userName=" + AppEnvirment.getUserBean().data.realName + "&token=" + AppEnvirment.getToken()
                    + "&lat=" + LocationUtil.getIntance().mLatitude + "&lng=" + LocationUtil.getIntance().mLongitude;
            webView.loadUrl(url);
//        webView.loadUrl("file:///android_asset/websocket.html");
            LogUtil.d("地图：" + url);
            ToastUtils.showDebugToast("(测试模式)地图：" + url);
            webView.addJavascriptInterface(new AndroidForJs(this), "AndroidForJs");
        } catch (Throwable throwable) {

        }
    }

    public class AndroidForJs {

        private AllBaseActivity context;

        public AndroidForJs(AllBaseActivity context) {
            this.context = context;
        }
        //音频采集
        @JavascriptInterface
        public void goAudioUpload() {
            leaveAndStartOtherMedia(new Runnable() {
                @Override
                public void run() {
                    String uid = AppEnvirment.getUserBean().data.id + "";
                    RouterUtil.goLiveActivityAudio(context, uid);
                }
            });
        }
        //视频采集
        @JavascriptInterface
        public void goVideoUpload() {
            leaveAndStartOtherMedia(new Runnable() {
                @Override
                public void run() {
                    String uid = AppEnvirment.getUserBean().data.id + "";
                    RouterUtil.goLiveActivity(ZHRenwuInfoActivity.this, uid);
                }
            });
        }
        //图片采集
        @JavascriptInterface
        public void goImageUpload() {
            Intent intent = new Intent(ZHRenwuInfoActivity.this, ImageGridActivity.class);
            startActivityForResult(intent, 1000);
        }
        //语音通话  系统用户的id
        @JavascriptInterface
        public void goAudio(String id) {
            leaveAndStartOtherMedia(new Runnable() {
                @Override
                public void run() {
                    String uid = AppEnvirment.getUserBean().data.id + "";
                    RouterUtil.goAudioActivity(context, id);
                }
            });
        }
        //视频通话  String id
        @JavascriptInterface
        public void goVideo(String id) {
            leaveAndStartOtherMedia(new Runnable() {
                @Override
                public void run() {
                    RouterUtil.goVideoActivity(context, id);
                }
            });
        }
        //播放视频   标题  流地址 图片地图
        @JavascriptInterface
        public void playVideo(String title, String videoUrl, String imageUrl) {
            RouterUtil.goVideoPlay(context, title, videoUrl, imageUrl);
        }
        @JavascriptInterface
        public void goChatRoomToPeople(String topicTmp) {
            try {
                if (BaseIP.isShowMap) {
                    webview.stopLoading();
                    webview.destroy();
                    webview.setVisibility(View.GONE);
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
            stop(new Runnable() {
                @Override
                public void run() {
                    finish();
                    goHome(1, dataBean, topicTmp);
                }
            });
        }
        //数组 0 lat 1lng
//        @JavascriptInterface
//        public void goChatRoomToPeople(String[] latlng){
//            if (latlng == null || latlng.length !=2){
//                return;
//            }
////            Intent intent=new Intent();
////            intent.putExtra("lat",latlng[0]);
////            intent.putExtra("lng",latlng[1]);
////            setResult(111,intent);
//            ToastUtils.showToast("当前选择坐标"+latlng);
//        }
    }
    private void leaveAndStartOtherMedia(Runnable runnable){
        ThreadPoolManager.runSubThread(new Runnable() {
            @Override
            public void run() {
                starRTCAudioManager.stop();
                removeListener();
                superRoomManager.leaveSuperRoom(new IXHResultCallback() {
                    @Override
                    public void success(Object data) {
                        runnable.run();
                    }

                    @Override
                    public void failed(final String errMsg) {
                        runnable.run();
                        MLOC.showMsg(ZHRenwuInfoActivity.this, errMsg);
                    }
                });
            }
        });
    }
    private void load(String trigger) {
        try {
            if (!BaseIP.isShowMap) {
                return;
            }
            if (isFinish() || isFinishing() || !webview.isShown()) {
                return;
            }
            LogUtil.d("trigger =" + trigger);
            webview.loadUrl(trigger);
        } catch (Throwable e) {
        }
    }
    private void showPopu(View view) {
        List<Node> nodes = new ArrayList<>();
        nodes.add(new Node("1", "-1", "音频采集",
                null, null));
        nodes.add(new Node("1", "-1", "视频采集",
                null, null));
        nodes.add(new Node("1", "-1", "图片采集",
                null, null));
        View menuView = View.inflate(this, R.layout.list_popuwindow, null);
        RecyclerView recycle = menuView.findViewById(R.id.recycle);
        PopupWindow popupWindow = PopupWindowUtils.getPopupWindowInCenter(menuView, content_view);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        recycle.setLayoutManager(manager);
        SimpleTreeRecyclerAdapter adapter = new SimpleTreeRecyclerAdapter(recycle, this,
                new ArrayList<>(), 1, R.drawable.shipingziyuan_xiala_icon, R.drawable.xiugaixinxi_shouqi_icon);
        recycle.setAdapter(adapter);
        adapter.getAllNodes().clear();
        adapter.addData(nodes);
        adapter.setOnTreeNodeClickListener(new OnTreeNodeClickListener() {
            @Override
            public void onClick(Node node, int position) {
                if (node.isLeaf()) {
                    if (position == 2) {
                        Intent intent = new Intent(ZHRenwuInfoActivity.this, ImageGridActivity.class);
                        startActivityForResult(intent, 1000);
                        return;
                    }
                    leaveAndStartOtherMedia(new Runnable() {
                        @Override
                        public void run() {
                            String uid = AppEnvirment.getUserBean().data.id + "";
                            if (position == 0) {
                                RouterUtil.goLiveActivityAudio(ZHRenwuInfoActivity.this, uid);
                            } else if (position == 1) {
                                RouterUtil.goLiveActivity(ZHRenwuInfoActivity.this, uid);
                            }
                        }
                    });
                }
                popupWindow.dismiss();
            }
        });
    }
    Handler handler = new Handler();

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
//            case RouterUtil.AUDIO_CONSTANS:
//                showLoading("对讲组恢复中...");
//                handler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        joinRTC();
//                    }
//                }, 1000);
//                break;
            case 1000:
                if (resultCode == ImagePicker.RESULT_CODE_ITEMS) {
                    if (data != null) {
                        final MeTopic me = Cache.getTinode().getMeTopic();
                        ArrayList<ImageItem> images = (ArrayList<ImageItem>) data.getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);
                        if (images != null && images.size() > 0) {
                            ImageItem imageItem = images.get(0);
                            showLoading(null);
                            HttpUtil.uploadFile(imageItem.path, new HttpUtil.Callback<FileBean>() {
                                @Override
                                public void onResponse(FileBean call) {
                                    dismissLoading();
                                    if (call != null && call.data != null) {
                                        ToastUtils.showToast("上传成功");
                                    } else {
                                        ToastUtils.showToast("上传失败");
                                    }
                                }
                            });
                        }
                    }
                }
        }
    }
    @Override
    public void onResume() {
        super.onResume();
//        MLOC.canPickupVoip = false;
    }
    @Override
    protected void onStart() {
        super.onStart();
        webview.setVisibility(View.VISIBLE);
        webview.onResume();
    }
    @Override
    public void onPause() {
        super.onPause();
        webview.onPause();
        webview.setVisibility(View.GONE);
        LogUtil.d("onPause ");
//        MLOC.canPickupVoip = true;
    }
    @Override
    public void onRestart() {
        super.onRestart();
        LogUtil.d("onRestart ");
        addListener();
    }
    private boolean isScreenOn() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        return pm.isScreenOn();
    }

    @Override
    public void onStop() {
        LogUtil.d("onStop ");
        if (isScreenOn()) {
            removeListener();
        }
        super.onStop();
    }
    boolean isDown = false;
    public void pttDown(){
        push_btn.setSelected(true);
        isDown = true;
       /* new Thread(new Runnable() {
            @Override
            public void run() {*/
                superRoomManager.pickUpMic(new IXHResultCallback() {
                    @Override
                    public void success(Object data) {
                        soundPlay(R.raw.beep2);
                        vibrate();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtils.showToast("可以发言了");
                            }
                        });
                    }
                    @Override
                    public void failed(String errMsg) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtils.showToast("发言失败");
                            }
                        });
                    }
                });
            }
      /*  }).start();
    }*/
    public void soundPlay(int raw){
//        SoundPool sp = new SoundPool(5, AudioManager.STREAM_MUSIC, 5);
//        int soundId = sp.load(this, R.raw.beep2, 1);
//        sp.play(soundId, 1, 1, 0, 0, 1);
        try {
            MediaPlayer mPlayer = MediaPlayer.create(this,raw);
            mPlayer.start();
        } catch (Exception e){

        }
    }

    public void pttUp(){
        push_btn.setSelected(false);
        isDown = false;
       /* new Thread(new Runnable() {
            @Override
            public void run() {*/
                superRoomManager.layDownMic(new IXHResultCallback() {
                    @Override
                    public void success(Object data) {
                        soundPlay(R.raw.ptt_not_old);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtils.showToast("已经交出发言权限");
                            }
                        });
                    }
                    @Override
                    public void failed(String errMsg) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtils.showToast("交出发言权限失败");
                            }
                        });
                    }
                });
            }
     /*   }).start();
    }*/
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        LogUtil.d("onStop "+keyCode);
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){
            if (event.getRepeatCount() == 0){
                pttDown();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){
            if (event.getRepeatCount() == 0){
                pttUp();
            }
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }


    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(ZHRenwuInfoActivity.this).setCancelable(true)
                .setTitle("是否要退出?")
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                    }
                }).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        stop(new Runnable() {
                            @Override
                            public void run() {
                                finish();
                            }
                        });
                    }
                }
        ).show();
    }
    Vibrator vibrator;

    private void vibrate() {
        if (vibrator == null) {
            vibrator = (Vibrator) this.getSystemService(this.VIBRATOR_SERVICE);
        }
        vibrator.vibrate(200);
    }


    private void initRTCAudio() {
//        try {
//            if (XHCustomConfig.getInstance().getAudioCodecType() != XHConstants.XHAudioCodecConfigEnum.STREAM_AUDIO_CODEC_OPUS) {
//                XHCustomConfig.getInstance().setDefConfigAudioCodecType(XHConstants.XHAudioCodecConfigEnum.STREAM_AUDIO_CODEC_OPUS);
//            }
//        } catch (Throwable throwable) {
//        }
        starRTCAudioManager = StarRTCAudioManager.create(this);
        starRTCAudioManager.start(new StarRTCAudioManager.AudioManagerEvents() {
            @Override
            public void onAudioDeviceChanged(StarRTCAudioManager.AudioDevice selectedAudioDevice, Set availableAudioDevices) {
//                if (BaseIP.isDebug) {
//                    handler.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            ToastUtils.showToast("声音：" + selectedAudioDevice);
//                        }
//                    }, 3000);
//                }
            }
        });
        superRoomManager = XHClient.getInstance().getSuperRoomManager(this);
        superRoomManager.setRtcMediaType(XHConstants.XHRtcMediaTypeEnum.STAR_RTC_MEDIA_TYPE_AUDIO_ONLY);
        superRoomManager.setRecorder(new XHCameraRecorder());
        superRoomManager.addListener(new XHSuperRoomManagerListener());
        addListener();
        push_btn.setOnTouchListener(new View.OnTouchListener() {
            int lastAction = -111;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (lastAction != event.getAction()) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            pttDown();
                            break;
                        case MotionEvent.ACTION_OUTSIDE:
                        case MotionEvent.ACTION_UP:
                            pttUp();
                            break;
                    }
                    lastAction = event.getAction();
                }
                return true;
            }
        });
        //0 duijiang 1shipin 2zhibo
        if (TextUtils.isEmpty(dataBean.teamId)) {
            if (BaseIP.isDebug) {
                ToastUtils.showToast("小队id为空！，尝试加入测试152测试小队");
                dataBean.teamId = "152";
            }
        }
        showLoading("正在加入对讲组...");

        HttpUtil.selectIntercomInfoByTokenAndCaseId("0", dataBean.teamId + "", new HttpUtil.Callback<DuijiangBean>() {
            @Override
            public void onResponse(DuijiangBean call) {
                if (call == null || call.data == null || call.data.intercomId == null) {
//                    createrId = AppEnvirment.getUserBean().data.id + "";
//                    liveName = dataBean.name;
//                    roomType = XHConstants.XHSuperRoomType.XHSuperRoomTypeGlobalPublic;
//                    createNewLive();
                    ToastUtils.showToast("对讲组不存在！！！！！！ ");
                    dismissLoading();
                } else {
                    createrId = call.data.userId + "";
                    liveName = call.data.name;
                    liveId = call.data.intercomId;
//                    liveId = "Wz@NWuVj3ZjyaEeBa4aFdoNnoWyAXMlk";
                    roomType = XHConstants.XHSuperRoomType.XHSuperRoomTypeGlobalPublic;
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            joinLive();
                        }
                    },500);
                }
            }
        });
    }
    boolean isJoining = false;

    private void joinLive() {
        if (isJoining) {
            return;
        }
        isJoining = true;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                isJoining = false;
                ToastUtils.showToast("加入对讲组超时");
                dismissLoading();
            }
        }, 15 * 1000);//对讲组超时
        try {
            String ip = MLOC.IP;
            String room = MLOC.CHATROOM_SERVER_URL;
            superRoomManager.joinSuperRoom(liveId, new IXHResultCallback() {
                @Override
                public void success(Object data) {
                    isJoining = false;
                    handler.removeCallbacksAndMessages(null);
                    dismissLoading();
                    ToastUtils.showToast("加入成功,按住下方按钮发言");
                    LogUtil.d("XHLiveManager", "watchLive success " + data);
                }

                @Override
                public void failed(final String errMsg) {
                    isJoining = false;
                    handler.removeCallbacksAndMessages(null);
                    dismissLoading();
                    ToastUtils.showToast("加入失败" + errMsg);
                    LogUtil.d("XHLiveManager", "watchLive failed " + errMsg);
                    stopAndFinish();
                }
            });
        } catch (Throwable e) {
            isJoining = false;
            ToastUtils.showToast("加入失败" + e.getMessage());
            dismissLoading();
        }
    }
    private void stopAndFinish() {
        starRTCAudioManager.stop();
        removeListener();
    }
    private void stopAndFinish(Runnable runnable) {
        starRTCAudioManager.stop();
        removeListener();
        if (runnable !=null ){
            handler.post(new Runnable() {
                @Override
                public void run() {
                    runnable.run();
                }
            });
        }
    }
    private void stop(Runnable runnable) {
        try {
            showLoading("结束中");
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.showToast("RTC接口超时");
                    handler.removeCallbacksAndMessages(null);
                    dismissLoading();
                    if (runnable !=null ){
                        runnable.run();
                    }
                }
            },5000);
            superRoomManager.leaveSuperRoom(new IXHResultCallback() {
                @Override
                public void success(Object data) {
                    handler.removeCallbacksAndMessages(null);
                    dismissLoading();
                    stopAndFinish(runnable);
                }

                @Override
                public void failed(final String errMsg) {
                    handler.removeCallbacksAndMessages(null);
                    dismissLoading();
                    MLOC.showMsg(ZHRenwuInfoActivity.this, errMsg);
                    stopAndFinish(runnable);
                }
            });
        }catch (Exception e){}
    }
    //    boolean isAdd = false;
    public void addListener() {
//        if (isAdd){
//            return;
//        }
//        isAdd = true;
        AEvent.addListener(AEvent.AEVENT_SUPER_ROOM_ERROR, this);
        AEvent.addListener(AEvent.AEVENT_SUPER_ROOM_ADD_UPLOADER, this);
        AEvent.addListener(AEvent.AEVENT_SUPER_ROOM_REMOVE_UPLOADER, this);
        AEvent.addListener(AEvent.AEVENT_SUPER_ROOM_GET_ONLINE_NUMBER, this);
        AEvent.addListener(AEvent.AEVENT_SUPER_ROOM_SELF_KICKED, this);
        AEvent.addListener(AEvent.AEVENT_SUPER_ROOM_SELF_BANNED, this);
        AEvent.addListener(AEvent.AEVENT_SUPER_ROOM_REV_MSG, this);
        AEvent.addListener(AEvent.AEVENT_SUPER_ROOM_REV_PRIVATE_MSG, this);
        AEvent.addListener(AEvent.AEVENT_SUPER_ROOM_SELF_COMMANDED_TO_STOP, this);
        AEvent.addListener(AEvent.AEVENT_RED, this);
        AEvent.addListener(AEvent.AEVENT_PTT, this);
    }
    private void removeListener() {
//        isAdd= false;
        AEvent.removeListener(AEvent.AEVENT_SUPER_ROOM_ERROR, this);
        AEvent.removeListener(AEvent.AEVENT_SUPER_ROOM_ADD_UPLOADER, this);
        AEvent.removeListener(AEvent.AEVENT_SUPER_ROOM_REMOVE_UPLOADER, this);
        AEvent.removeListener(AEvent.AEVENT_SUPER_ROOM_GET_ONLINE_NUMBER, this);
        AEvent.removeListener(AEvent.AEVENT_SUPER_ROOM_SELF_KICKED, this);
        AEvent.removeListener(AEvent.AEVENT_SUPER_ROOM_SELF_BANNED, this);
        AEvent.removeListener(AEvent.AEVENT_SUPER_ROOM_REV_MSG, this);
        AEvent.removeListener(AEvent.AEVENT_SUPER_ROOM_REV_PRIVATE_MSG, this);
        AEvent.removeListener(AEvent.AEVENT_SUPER_ROOM_SELF_COMMANDED_TO_STOP, this);
        AEvent.removeListener(AEvent.AEVENT_RED, this);
        AEvent.removeListener(AEvent.AEVENT_PTT, this);
    }
    @Override
    public void dispatchEvent(String aEventID, boolean success, Object eventObj) {
        super.dispatchEvent(aEventID, success, eventObj);
        LogUtil.d(aEventID + "" + eventObj);
        switch (aEventID) {
            case AEvent.AEVENT_SUPER_ROOM_ERROR:
                ToastUtils.showToast(aEventID + "" + eventObj);
                showLoading("对讲组异常或超时,重新加入对讲组...");
                leaveAndStartOtherMedia(new Runnable() {
                    @Override
                    public void run() {
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                joinRTC();
                            }
                        }, 1000);
                    }
                });
                break;
            case AEvent.AEVENT_SUPER_ROOM_ADD_UPLOADER:
                try {
                    JSONObject data = (JSONObject) eventObj;
                    String addId = data.getString("actorID");

                    ZHHttpManager.getZhHttpManager().getHeadPicById(addId, new VoipHttpListener() {
                        @Override
                        public void onUserInfo(String userID, String headpic) {
                            try {
                                title_center.setText(userID+"");
                                title_center_image.setVisibility(View.VISIBLE);
                            } catch (Exception e){}
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (!BaseIP.isDebug) {
                    return;
                }
                ToastUtils.showToast(aEventID + "" + eventObj);
                break;
            case AEvent.AEVENT_SUPER_ROOM_REMOVE_UPLOADER:
                try {
                    JSONObject data = (JSONObject) eventObj;
                    String removeUserId = data.getString("actorID");
                    title_center.setText("任务执行");
                    title_center_image.setVisibility(View.GONE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (!BaseIP.isDebug) {
                    return;
                }
                ToastUtils.showToast(aEventID + "" + eventObj);
                break;
            case AEvent.AEVENT_SUPER_ROOM_GET_ONLINE_NUMBER:
                if (!BaseIP.isDebug) {
                    return;
                }
                ToastUtils.showToast(aEventID + "" + eventObj);
                break;
            case AEvent.AEVENT_SUPER_ROOM_SELF_KICKED:
                if (!BaseIP.isDebug) {
                    return;
                }
                ToastUtils.showToast(aEventID + "" + eventObj);
                break;
            case AEvent.AEVENT_SUPER_ROOM_REV_MSG:
                if (!BaseIP.isDebug) {
                    return;
                }
                ToastUtils.showToast(aEventID + "" + eventObj);
                break;
            case AEvent.AEVENT_SUPER_ROOM_REV_PRIVATE_MSG:
                if (!BaseIP.isDebug) {
                    return;
                }
                ToastUtils.showToast(aEventID + "" + eventObj);
                break;
            case AEvent.AEVENT_SUPER_ROOM_SELF_COMMANDED_TO_STOP:
                if (!BaseIP.isDebug) {
                    return;
                }
                ToastUtils.showToast(aEventID + "" + eventObj);
                break;
            case AEvent.AEVENT_VOIP_START:
                leaveAndStartOtherMedia(new Runnable() {
                    @Override
                    public void run() {

                    }
                });
                break;
            case AEvent.AEVENT_VOIP_STOP:
                showLoading("对讲组恢复中...");
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        joinRTC();
                    }
                }, 1000);
                break;
            case AEvent.AEVENT_RED:
                if (AEvent.RED == 0) {
                    red_text.setText("0");
                    red_text.setVisibility(View.GONE);
                } else {
                    red_text.setVisibility(View.VISIBLE);
                    red_text.setText("" + AEvent.RED);
                }
                break;
            case AEvent.AEVENT_PTT:
                if (isDown){
                    pttUp();
                } else {
                    pttDown();
                }
                break;
        }
    }
    void joinRTC() {
        starRTCAudioManager.start(new StarRTCAudioManager.AudioManagerEvents() {
            @Override
            public void onAudioDeviceChanged(StarRTCAudioManager.AudioDevice audioDevice, Set set) {

            }
        });
        superRoomManager = XHClient.getInstance().getSuperRoomManager(ZHRenwuInfoActivity.this);
        superRoomManager.setRtcMediaType(XHConstants.XHRtcMediaTypeEnum.STAR_RTC_MEDIA_TYPE_AUDIO_ONLY);
        superRoomManager.setRecorder(new XHCameraRecorder());
        superRoomManager.addListener(new XHSuperRoomManagerListener());
        addListener();
        joinLive();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        AEvent.removeListener(AEvent.AEVENT_VOIP_START, this);
        AEvent.removeListener(AEvent.AEVENT_VOIP_STOP, this);
        try {
            mAudioManager.unregisterMediaButtonEventReceiver(mComponent);
            if (starRTCAudioManager != null) {
                starRTCAudioManager.stop();
            }
            if (BaseIP.isShowMap) {
                content_view.removeView(webview);
                webview.stopLoading();
                webview.destroy();
                webview.setVisibility(View.GONE);
            }
        } catch (Throwable e) {
        }
        handler.removeCallbacksAndMessages(null);
        LocationUtil.getIntance().stop();
    }
}

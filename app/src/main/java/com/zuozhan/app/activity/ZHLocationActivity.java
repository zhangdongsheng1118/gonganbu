package com.zuozhan.app.activity;

import android.content.Intent;
import android.net.http.SslError;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.yixin.tinode.R;
import com.yixin.tinode.model.data.LocationData;
import com.zuozhan.app.AppEnvirment;
import com.zuozhan.app.BaseIP;
import com.zuozhan.app.RouterUtil;
import com.zuozhan.app.util.LocationUtil;
import com.zuozhan.app.util.LogUtil;

import butterknife.BindView;

public class ZHLocationActivity extends AllBaseActivity {

    @BindView(R.id.webview)
    WebView webview;
    @BindView(R.id.ivToolbarNavigation)
    View ivToolbarNavigation;

    boolean isLook = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zh_activity_location);
        LocationData data = (LocationData) getIntent().getSerializableExtra("location");
        String lat = "";
        String lng = "";
        if (data != null) {
            isLook = true;
            lat = data.getLat() + "";
            lng = data.getLng() + "";
        } else {
            isLook = false;
            lat = LocationUtil.getIntance().mLatitude;
            lng = LocationUtil.getIntance().mLongitude;
        }

        initWebView(webview, lat, lng);
        ivToolbarNavigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initWebView(WebView webView, String lat, String lng) {
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
                handler.proceed();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                load("javascript:panTo('" + AppEnvirment.getUserBean().data.realName + "');");
            }
        });
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);//支持内容重新布局
        String url = BaseIP.IP_MAP + "0&lat=" + lat + "&lng=" + lng;
        webView.loadUrl(url);
        webView.addJavascriptInterface(new AndroidForJs(this), "AndroidForJs");
    }

    public class AndroidForJs {

        private AllBaseActivity context;

        public AndroidForJs(AllBaseActivity context) {
            this.context = context;
        }

        //音频采集
        @JavascriptInterface
        public void goAudioUpload() {
            String uid = AppEnvirment.getUserBean().data.id + "";
            RouterUtil.goLiveActivityAudio(context, uid);
        }

        //视频采集
        @JavascriptInterface
        public void goVideoUpload() {
            String uid = AppEnvirment.getUserBean().data.id + "";
            RouterUtil.goLiveActivity(ZHLocationActivity.this, uid);
        }

        //图片采集
        @JavascriptInterface
        public void goImageUpload() {
            String uid = AppEnvirment.getUserBean().data.id + "";
            RouterUtil.goLiveActivity(ZHLocationActivity.this, uid);
        }

        //语音通话  系统用户的id
        @JavascriptInterface
        public void goAudio(String id) {
            RouterUtil.goAudioActivity(context, id);
        }

        //视频通话  String id
        @JavascriptInterface
        public void goVideo(String id) {
            RouterUtil.goVideoActivity(context, id);
        }


        //播放视频   标题  流地址 图片地图
        @JavascriptInterface
        public void playVideo(String title, String videoUrl, String imageUrl) {
            RouterUtil.goVideoPlay(context, title, videoUrl, imageUrl);
        }

        @JavascriptInterface
        public void goChatRoomToPeople(String topicTmp) {
            LogUtil.d("a"+topicTmp);

        }

        //数组 0 lat 1lng
        @JavascriptInterface
        public void sendlatlng(String lat , String lng) {
            if (isLook) {
                return;
            }
            if (lat == null || lng ==null) {
                return;
            }
            Intent intent = new Intent();
            intent.putExtra("lat",""+ lat);
            intent.putExtra("lng", ""+lng);
            setResult(111, intent);
            finish();
        }
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

}

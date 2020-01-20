package com.zuozhan.app.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.xiao.nicevideoplayer.NiceVideoPlayer;
import com.yixin.tinode.R;

public class ZHVideoInfoActivity2 extends AllBaseActivity {


    WebView mNiceVideoPlayer2;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zh_activity_video_play2);
//        String url = getIntent().getStringExtra("data");
        String url = "http://demo.easydss.com:10080/frecord/stream_222258.4b221ca26f31f4238d/stream_222258/20190707/20190707181952/stream_222258_record.m3u8";
        String image = getIntent().getStringExtra("image");
        String title = getIntent().getStringExtra("title");
        mNiceVideoPlayer2 = findViewById(R.id.nice_video_player);
        initWebView(mNiceVideoPlayer2,url);

    }

    @Override
    protected void onStop() {
        super.onStop();
        // 在onStop时释放掉播放器
//        NiceVideoPlayerManager.instance().releaseNiceVideoPlayer();
    }

    @Override
    public void onBackPressed() {
        // 在全屏或者小窗口时按返回键要先退出全屏或小窗口，
        // 所以在Activity中onBackPress要交给NiceVideoPlayer先处理。
//        if (NiceVideoPlayerManager.instance().onBackPressd()) return;
        super.onBackPressed();
    }

    private void initWebView(WebView webView, String content){
        WebSettings settings = webView.getSettings();

        //settings.setUseWideViewPort(true);//调整到适合webview的大小，不过尽量不要用，有些手机有问题
        settings.setLoadWithOverviewMode(true);//设置WebView是否使用预览模式加载界面。
        webView.setVerticalScrollBarEnabled(false);//不能垂直滑动
        webView.setHorizontalScrollBarEnabled(false);//不能水平滑动
        settings.setTextSize(WebSettings.TextSize.NORMAL);//通过设置WebSettings，改变HTML中文字的大小
        settings.setJavaScriptCanOpenWindowsAutomatically(true);//支持通过JS打开新窗口
        settings.setAllowFileAccess(true);
        //设置WebView属性，能够执行Javascript脚本
        webView.getSettings().setJavaScriptEnabled(true);//设置js可用
        webView.setWebViewClient(new WebViewClient());
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);//支持内容重新布局
        if (content == null){
            return;
        }

        webView.loadUrl(content);

    }
}

package com.zuozhan.app.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.yixin.tinode.R;

import butterknife.BindView;

public class WebDataActivity extends AllBaseActivity{

    @BindView(R.id.webview)
    WebView webview;
    @BindView(R.id.title_left)
    View title_left;
    @BindView(R.id.title_center)
    TextView title_center;
    String title="";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zh_activity_webview);
        title = getIntent().getStringExtra("title");
        title_center.setText(title);
        title_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        String content = getIntent().getStringExtra("data");
        initWebView(webview,content);
    }

    private void initWebView(WebView webView,String content){
        WebSettings settings = webView.getSettings();

        //settings.setUseWideViewPort(true);//调整到适合webview的大小，不过尽量不要用，有些手机有问题
        settings.setLoadWithOverviewMode(true);//设置WebView是否使用预览模式加载界面。
        webView.setVerticalScrollBarEnabled(false);//不能垂直滑动
        webView.setHorizontalScrollBarEnabled(false);//不能水平滑动
        settings.setTextSize(WebSettings.TextSize.NORMAL);//通过设置WebSettings，改变HTML中文字的大小
        settings.setJavaScriptCanOpenWindowsAutomatically(true);//支持通过JS打开新窗口
        settings.setAllowFileAccess(true);
        settings.setDefaultTextEncodingName("UTF-8") ;
        //设置WebView属性，能够执行Javascript脚本
        settings.setJavaScriptEnabled(true);//设置js可用
        webView.setWebViewClient(new WebViewClient());
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);//支持内容重新布局
        if (content == null){
            return;
        }

//        webView.setBackgroundColor(0); // 设置背景色
//        webView.loadDataWithBaseURL(null, "加载中。。", "text/html", "utf-8",null);
        webView.loadData(content, "text/html", "UTF-8");
//        webView.setVisibility(View.VISIBLE);
//        webView.setBackgroundColor(getResources().getColor(R.color.color_122131));
    }
}

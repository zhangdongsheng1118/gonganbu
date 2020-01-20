package com.zuozhan.app.activity;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.yixin.tinode.R;
import com.zuozhan.app.RouterUtil;
import com.zuozhan.app.adapter.TongZhiAdapter;
import com.zuozhan.app.bean.ArticleBean;
import com.zuozhan.app.httpUtils.MyService;
import com.zuozhan.app.net.HttpService;
import com.zuozhan.app.util.AppManager;

import java.util.ArrayList;

import butterknife.BindView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ZHSearchTongzhiActivity extends AllBaseActivity {
    @BindView(R.id.recycler_view)
    RecyclerView recycler_view;
    @BindView(R.id.title_left)
    View title_left;
    @BindView(R.id.title_center)
    EditText title_center;
    @BindView(R.id.iv_no)
    View iv_no;
    @BindView(R.id.str)
    View edit_str;

    TongZhiAdapter adapter;
    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zh_activity_search_tongzhi);
        title_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        recycler_view.setLayoutManager(manager);
        adapter = new TongZhiAdapter(this);
        recycler_view.setAdapter(adapter);

//        getStatue(null, null);

        title_center.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String ss = title_center.getText().toString();
                if (TextUtils.isEmpty(ss)) {
                    edit_str.setVisibility(View.INVISIBLE);
                } else {
                    edit_str.setVisibility(View.VISIBLE);
                }
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                       // String str = title_center.getText().toString();
                       // getStatue(str, str);
                    }
                }, 1000);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        title_center.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    hideSoft();
                    //点击搜索的时候隐藏软键盘
                    String str = title_center.getText().toString();
                    getStatue(str, str);
                    // 在这里写搜索的操作,一般都是网络请求数据
                    return true;
                }
                return false;
            }
        });
    }

    private void getStatue(String s1, String s2) {
        showLoading(null);
        HttpService.getApiToHeader(MyService.class)
                .getServiceApi().getArticle("1", s2)
                .enqueue(new Callback<ArticleBean>() {
                    @Override
                    public void onResponse(Call<ArticleBean> call, Response<ArticleBean> response) {
                        dismissLoading();
                        if (response.body() != null && (response.body().code == 2 || response.body().code == 1001)) {
                            AppManager.finishAllActivity();
                            RouterUtil.goLoginToClear(ZHSearchTongzhiActivity.this);
                            return;
                        }
                        ArticleBean renWuLeiBean = response.body();
                        if (renWuLeiBean != null) {
                            adapter.reshes(renWuLeiBean.data);
                            recycler_view.setVisibility(View.VISIBLE);
                            iv_no.setVisibility(View.GONE);
                        } else {
                            adapter.reshes(new ArrayList<>());
                            recycler_view.setVisibility(View.GONE);
                            iv_no.setVisibility(View.INVISIBLE);
                        }

                    }

                    @Override
                    public void onFailure(Call<ArticleBean> call, Throwable t) {
                        adapter.reshes(new ArrayList<>());
                        recycler_view.setVisibility(View.GONE);
                        iv_no.setVisibility(View.INVISIBLE);
                        dismissLoading();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}

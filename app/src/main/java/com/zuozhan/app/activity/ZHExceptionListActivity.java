package com.zuozhan.app.activity;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.yixin.tinode.R;
import com.zuozhan.app.RouterUtil;
import com.zuozhan.app.adapter.ExceptionListAdapter;
import com.zuozhan.app.adapter.TongZhiAdapter;
import com.zuozhan.app.bean.ArticleBean;
import com.zuozhan.app.bean.EeceptionBean;
import com.zuozhan.app.httpUtils.MyService;
import com.zuozhan.app.net.HttpService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ZHExceptionListActivity extends AllBaseActivity {
    RecyclerView recyclerView;
    ImageView imageView;
    ImageView exception_iv;
    String caseId = "";
    ExceptionListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zh_activity_exceptionlist);
        recyclerView = findViewById(R.id.rec_tz);
        imageView = findViewById(R.id.back_img);
        exception_iv = findViewById(R.id.exception_iv);
        caseId = getIntent().getStringExtra("info");
        //点击返回箭头关闭此页面
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        exception_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RouterUtil.goActivity(ZHExceptionListActivity.this, ZHExceptionInUploadActivity.class);
            }
        });
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);
        adapter = new ExceptionListAdapter(this);
        recyclerView.setAdapter(adapter);
    }


    @Override
    protected void onResume() {
        super.onResume();
        HttpService.getApi(MyService.class)
                .getServiceApi().getCallPoliceList(caseId)
                .enqueue(new Callback<EeceptionBean>() {
                    @Override
                    public void onResponse(Call<EeceptionBean> call, Response<EeceptionBean> response) {
                        EeceptionBean articleBean = response.body();
                        try {
                            if (articleBean == null) {
                                adapter.reshes(null);
                                return;
                            }
                            adapter.reshes(articleBean.data);
                        } catch (Exception e) {

                        }
                    }

                    @Override
                    public void onFailure(Call<EeceptionBean> call, Throwable t) {
                        try {
                            adapter.reshes(null);
                        } catch (Exception e) {

                        }

                    }
                });
    }
}

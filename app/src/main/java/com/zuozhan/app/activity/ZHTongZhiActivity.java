package com.zuozhan.app.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.yixin.tinode.R;
import com.zuozhan.app.RouterUtil;
import com.zuozhan.app.adapter.TongZhiAdapter;
import com.zuozhan.app.bean.ArticleBean;
import com.zuozhan.app.httpUtils.MyService;
import com.zuozhan.app.net.HttpService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ZHTongZhiActivity extends AllBaseActivity {
    RecyclerView recyclerView;
    ImageView imageView;
    ImageView message_search;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zh_activity_tong_zhi);
        recyclerView=findViewById(R.id.rec_tz);
        imageView=findViewById(R.id.back_img);
        message_search=findViewById(R.id.message_search);
        //点击返回箭头关闭此页面
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        message_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RouterUtil.goActivity(ZHTongZhiActivity.this,ZHSearchTongzhiActivity.class);
            }
        });
        LinearLayoutManager manager=new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);
        TongZhiAdapter adapter=new TongZhiAdapter(this);
        recyclerView.setAdapter(adapter);
        HttpService.getApi(MyService.class)
                .getServiceApi().getArticle("1")
                .enqueue(new Callback<ArticleBean>() {
                    @Override
                    public void onResponse(Call<ArticleBean> call, Response<ArticleBean> response) {
                        ArticleBean articleBean =response.body();
                        if (articleBean==null){
                            return;
                        }
                        adapter.reshes(articleBean.data);
                    }

                    @Override
                    public void onFailure(Call<ArticleBean> call, Throwable t) {

                    }
                });

    }
}

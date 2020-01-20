package com.zuozhan.app.activity;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.yixin.tinode.R;
import com.zuozhan.app.AppEnvirment;
import com.zuozhan.app.RouterUtil;
import com.zuozhan.app.adapter.RenWuAdapter;
import com.zuozhan.app.bean.RenWuLeiBean;
import com.zuozhan.app.httpUtils.MyService;
import com.zuozhan.app.net.HttpService;
import com.zuozhan.app.util.AppManager;

import java.util.ArrayList;

import butterknife.BindView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ZHRenwuActivity extends AllBaseActivity {
    @BindView(R.id.recycler_view)
    RecyclerView recycler_view;
    @BindView(R.id.title_left)
    View title_left;
    @BindView(R.id.message_search)
    View message_search;
    @BindView(R.id.title_center)
    TextView title_center;
    @BindView(R.id.group1)
    RadioGroup group1;
    @BindView(R.id.iv_no)
    View iv_no;

    RenWuAdapter adapter;

    String str = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zh_activity_renwu);
        title_center.setText("任务");
        title_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        message_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RouterUtil.goActivity(ZHRenwuActivity.this, ZHSearchRenwuActivity.class);
            }
        });
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        recycler_view.setLayoutManager(manager);
        adapter = new RenWuAdapter(this);
        recycler_view.setAdapter(adapter);

        group1.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.check_1:
                        getStatue(null);
                        break;
                    case R.id.check_2:
                        getStatue("0");
                        break;
                    case R.id.check_3:
                        getStatue("1");
                        break;
                    case R.id.check_4:
                        getStatue("2");
                        break;
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        getStatue(str);
    }

    private void getStatue(String string) {
        str = string;
        showLoading(null);
        HttpService.getApiToHeader(MyService.class)
                .getServiceApi().getRenWu(AppEnvirment.getToken(), string)
                .enqueue(new Callback<RenWuLeiBean>() {
                    @Override
                    public void onResponse(Call<RenWuLeiBean> call, Response<RenWuLeiBean> response) {
                        dismissLoading();
                        if (response.body() != null && (response.body().code == 2 || response.body().code == 1001)) {
                            AppManager.finishAllActivity();
                            RouterUtil.goLoginToClear(ZHRenwuActivity.this);
                            return;
                        }
                        RenWuLeiBean renWuLeiBean = response.body();
                        if (adapter == null) {
                            iv_no.setVisibility(View.VISIBLE);
                            recycler_view.setVisibility(View.GONE);
                            return;
                        }
                        if (renWuLeiBean != null) {
                            if (renWuLeiBean.getData() == null || renWuLeiBean.getData().size() == 0){
                                adapter.reshes(new ArrayList<>());
                                iv_no.setVisibility(View.VISIBLE);
                                recycler_view.setVisibility(View.GONE);
                            }else {
                                adapter.reshes(renWuLeiBean.getData());
                                iv_no.setVisibility(View.GONE);
                                recycler_view.setVisibility(View.VISIBLE);
                            }

                        } else {
                            adapter.reshes(new ArrayList<>());
                            iv_no.setVisibility(View.VISIBLE);
                            recycler_view.setVisibility(View.GONE);
                        }

                    }

                    @Override
                    public void onFailure(Call<RenWuLeiBean> call, Throwable t) {
                        dismissLoading();
                        if (adapter == null) {
                            iv_no.setVisibility(View.VISIBLE);
                            recycler_view.setVisibility(View.GONE);
                            return;
                        }
                        adapter.reshes(new ArrayList<>());
                        iv_no.setVisibility(View.VISIBLE);
                        recycler_view.setVisibility(View.GONE);
                    }
                });
    }
}

package com.zuozhan.app.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.starrtc.demo.StartRTCUtil;
import com.yixin.tinode.R;
import com.yixin.tinode.app.MyApp;
import com.yixin.tinode.db.tinode.BaseDb;
import com.yixin.tinode.model.cache.UserCache;
import com.yixin.tinode.tinode.Cache;
import com.zuozhan.app.AppEnvirment;
import com.zuozhan.app.RouterUtil;
import com.zuozhan.app.activity.ZHRenwuActivity;
import com.zuozhan.app.activity.ZHTongZhiActivity;
import com.zuozhan.app.adapter.RenWuAdapter;
import com.zuozhan.app.bean.RenWuLeiBean;
import com.zuozhan.app.bean.UserBean;
import com.zuozhan.app.httpUtils.HttpUtil;
import com.zuozhan.app.httpUtils.MyService;
import com.zuozhan.app.imageloader.ImageLoader;
import com.zuozhan.app.net.HttpService;
import com.zuozhan.app.util.AppManager;
import com.zuozhan.app.util.ShareprefrensUtils;
import com.zuozhan.app.util.ToastUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class Fragment1 extends Fragment {
    ImageView imageView;
    RecyclerView recyclerView;
    private RenWuAdapter adapter;
    TextView name;
    TextView phone;
    TextView zhiwei;
    TextView bumen;
    TextView ivMore;
    ImageView iv_icon;

    public Fragment1() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.zh_fragment_fragment1, container, false);

        recyclerView = view.findViewById(R.id.rev_rw);
        iv_icon = view.findViewById(R.id.iv_icon);
        imageView = view.findViewById(R.id.message_btn);
        name = view.findViewById(R.id.name);
        phone = view.findViewById(R.id.phone);
        zhiwei = view.findViewById(R.id.zhiwei);
        bumen = view.findViewById(R.id.bumen);
        ivMore = view.findViewById(R.id.ivMore);
        imageView = view.findViewById(R.id.message_btn);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ZHTongZhiActivity.class);
                startActivity(intent);
            }
        });
        initNetData();
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);
        adapter = new RenWuAdapter(getActivity());
        recyclerView.setAdapter(adapter);
        ivMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RouterUtil.goActivity(Fragment1.this, ZHRenwuActivity.class);
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        initNetData1();
    }

    private void initNetData1() {
        HttpService.getApiToHeader(MyService.class)
                .getServiceApi().getMyMissionByTokenAndCondition()
                .enqueue(new Callback<RenWuLeiBean>() {
                    @Override
                    public void onResponse(Call<RenWuLeiBean> call, Response<RenWuLeiBean> response) {
                        if (response.body() != null && (response.body().code == 2 || response.body().code == 1001)) {
                            AppManager.finishAllActivity();
                            RouterUtil.goLoginToClear(getActivity());
                            return;
                        }
                        RenWuLeiBean renWuLeiBean = response.body();
                        if (renWuLeiBean == null) {
                            ToastUtils.showToast("当前任务数据为空");
                            adapter.reshes(new ArrayList<>());
                            return;
                        }
                        if (renWuLeiBean.getData() != null) {
                            if (renWuLeiBean.getData().size()>3){
                                List<RenWuLeiBean.DataBean> dataBeans=new ArrayList<>();
                                for (int i = 0; i < 3; i++) {
                                    dataBeans.add(renWuLeiBean.getData().get(i));
                                }
                                adapter.reshes(dataBeans);

                            }else {
                                adapter.reshes(renWuLeiBean.getData());
                            }
                        }else {
                            ToastUtils.showToast("当前任务数据为空");
                            adapter.reshes(new ArrayList<>());
                        }

                    }

                    @Override
                    public void onFailure(Call<RenWuLeiBean> call, Throwable t) {
                        ToastUtils.showToast("当前任务列表接口返回失败："+t.getMessage());
                        adapter.reshes(new ArrayList<>());
                        //重新加载
                        initNetData2();
                    }
                });
    }


    private void initNetData2() {
        HttpService.getApiToHeader(MyService.class)
                .getServiceApi().getMyMissionByTokenAndCondition()
                .enqueue(new Callback<RenWuLeiBean>() {
                    @Override
                    public void onResponse(Call<RenWuLeiBean> call, Response<RenWuLeiBean> response) {
                        if (response.body() != null && (response.body().code == 2 || response.body().code == 1001)) {
                            AppManager.finishAllActivity();
                            RouterUtil.goLoginToClear(getActivity());
                            return;
                        }
                        RenWuLeiBean renWuLeiBean = response.body();
                        if (renWuLeiBean == null) {
                            ToastUtils.showToast("当前任务数据为空");
                            adapter.reshes(new ArrayList<>());
                            return;
                        }
                        if (renWuLeiBean.getData() != null) {
                            if (renWuLeiBean.getData().size()>3){
                                List<RenWuLeiBean.DataBean> dataBeans=new ArrayList<>();
                                for (int i = 0; i < 3; i++) {
                                    dataBeans.add(renWuLeiBean.getData().get(i));
                                }
                                adapter.reshes(dataBeans);

                            }else {
                                adapter.reshes(renWuLeiBean.getData());
                            }
                        }else {
                            ToastUtils.showToast("当前任务数据为空");
                            adapter.reshes(new ArrayList<>());
                        }

                    }

                    @Override
                    public void onFailure(Call<RenWuLeiBean> call, Throwable t) {
                        ToastUtils.showToast("当前任务列表接口返回失败："+t.getMessage());
                        adapter.reshes(new ArrayList<>());
                    }
                });
    }
    private void initNetData() {
        HttpUtil.getUserInfo(new HttpUtil.Callback<UserBean>() {
            @Override
            public void onResponse(UserBean userBean) {
                try {
                    if (userBean != null && userBean.data != null && userBean.code == 1) {
                        String username = userBean.data.realName;
                        String phone1 = userBean.data.phone;
                        String duty = userBean.data.duty;
                        String employer = userBean.data.departmentName;
                        name.setText("姓名：" + username);
                        if (TextUtils.isEmpty(phone1)) {
                            phone.setText("手机号：无");
                        } else {
                            phone.setText("手机号：" + phone1);
                        }
                        zhiwei.setText("职位：" + duty);
                        bumen.setText("部门：" + employer);
                        ImageLoader.loadImage(getActivity(), iv_icon, userBean.data.headPic);
                    } else {
                        ShareprefrensUtils.setSharePreferencesBoolean(ShareprefrensUtils.ISLOGIN, false);
                        ShareprefrensUtils.setSharePreferences(ShareprefrensUtils.TOKEN, "");
                        ShareprefrensUtils.setSharePreferences(ShareprefrensUtils.USERINFO, "");
                        StartRTCUtil.loginOut(getActivity());
                        AppEnvirment.LoginOut();
                        MyApp.exit();
                        UserCache.clear();
                        BaseDb.getInstance().logout();
                        Cache.invalidate();
                        RouterUtil.goLoginToClear(Fragment1.this);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            UserBean userBean = AppEnvirment.getUserBean();
            if (userBean == null || userBean.data == null) {
                return;
            }
            String username = userBean.data.realName;
            String phone1 = userBean.data.phone;
            String duty = userBean.data.duty;
            String employer = userBean.data.departmentName;
            name.setText("姓名：" + username);
            if (TextUtils.isEmpty(phone1)) {
                phone.setText("手机号：无");
            } else {
                phone.setText("手机号：" + phone1);
            }
            zhiwei.setText("职位：" + duty);
            bumen.setText("部门：" + employer);
            ImageLoader.loadImage(getActivity(), iv_icon, userBean.data.headPic);
        }
    }

}

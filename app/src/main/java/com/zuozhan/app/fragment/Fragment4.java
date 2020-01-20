package com.zuozhan.app.fragment;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.starrtc.demo.StartRTCUtil;
import com.starrtc.demo.demo.setting.SettingActivity;
import com.yixin.tinode.R;
import com.yixin.tinode.app.MyApp;
import com.yixin.tinode.db.tinode.BaseDb;
import com.yixin.tinode.model.cache.UserCache;
import com.yixin.tinode.tinode.Cache;
import com.zuozhan.app.AppEnvirment;
import com.zuozhan.app.BaseIP;
import com.zuozhan.app.RouterUtil;
import com.zuozhan.app.activity.ZHChangePswActivity;
import com.zuozhan.app.activity.ZHChangeUserActivity;
import com.zuozhan.app.activity.ZHOperationGuideActivity;
import com.zuozhan.app.activity.ZHShipinActivity;
import com.zuozhan.app.activity.ZHTongZhiActivity;
import com.zuozhan.app.bean.MyUtil;
import com.zuozhan.app.bean.UserBean;
import com.zuozhan.app.imageloader.ImageLoader;
import com.zuozhan.app.util.AppManager;
import com.zuozhan.app.util.ShareprefrensUtils;
import com.zuozhan.app.util.ToastUtils;

/**
 * A simple {@link Fragment} subclass.
 */
public class Fragment4 extends Fragment implements View.OnClickListener {

    ImageView imageView;
    TextView user_name;
    CardView cardview_1;
    CardView cardview_2;
    CardView cardview_3;
    CardView cardview_4;
    CardView cardview_5;
    CardView cardview_6;
    View message_search;

    public Fragment4() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.zh_fragment_fragment4, container, false);
        imageView = view.findViewById(R.id.tou);
        user_name = view.findViewById(R.id.user_name);
        cardview_1 = view.findViewById(R.id.cardview_1);
        cardview_2 = view.findViewById(R.id.cardview_2);
        cardview_3 = view.findViewById(R.id.cardview_3);
        cardview_4 = view.findViewById(R.id.cardview_4);
        cardview_5 = view.findViewById(R.id.cardview_5);
        cardview_6 = view.findViewById(R.id.cardview_6);
        message_search = view.findViewById(R.id.message_search);
        cardview_1.setOnClickListener(this);
        cardview_2.setOnClickListener(this);
        cardview_3.setOnClickListener(this);
        cardview_4.setOnClickListener(this);
        cardview_5.setOnClickListener(this);
        cardview_6.setOnClickListener(this);
        message_search.setOnClickListener(this);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden){
            UserBean userBean = AppEnvirment.getUserBean();
            if (userBean == null || userBean.data == null){
                return;
            }
            ImageLoader.loadImage(this, imageView, userBean.data.headPic);
            MyUtil.setText(user_name,userBean.data.realName);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        UserBean userBean = AppEnvirment.getUserBean();
        if (userBean == null || userBean.data == null){
            return;
        }
        ImageLoader.loadImage(this, imageView, userBean.data.headPic);
        MyUtil.setText(user_name,userBean.data.realName);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //修改个人信息
            case R.id.cardview_1:
                RouterUtil.goActivity(this, ZHChangeUserActivity.class);
                break;
                //修改密码
            case R.id.cardview_2:
                RouterUtil.goActivity(this, ZHChangePswActivity.class);
                break;
            case R.id.cardview_3:
                RouterUtil.goActivity(this, ZHShipinActivity.class);
                break;
            case R.id.cardview_4:
                RouterUtil.goActivity(this, ZHTongZhiActivity.class);
                break;
            case R.id.cardview_5:
                RouterUtil.goActivity(this, ZHOperationGuideActivity.class);
                break;
            case R.id.cardview_6:
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("提示")
                        .setMessage("是否要退出当前账号")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ShareprefrensUtils.setSharePreferencesBoolean(ShareprefrensUtils.ISLOGIN, false);
                                ShareprefrensUtils.setSharePreferences(ShareprefrensUtils.TOKEN, "");
                                ShareprefrensUtils.setSharePreferences(ShareprefrensUtils.USERINFO, "");
                                StartRTCUtil.loginOut(getActivity());
                                AppEnvirment.LoginOut();
                                MyApp.exit();
                                UserCache.clear();
                                BaseDb.getInstance().logout();
                                Cache.invalidate();
                                AppManager.finishAllActivity();
                                RouterUtil.goLoginToClear(Fragment4.this);
                            }
                        }).setNeutralButton("取消", null).show();


                break;
            case R.id.message_search:
                if (BaseIP.isDebug){
                    ToastUtils.showToast("(测试模式)rtc设置");
                }
                RouterUtil.goActivity(this, SettingActivity.class);
                ToastUtils.showToast("点击设置");
                break;
        }
    }
}

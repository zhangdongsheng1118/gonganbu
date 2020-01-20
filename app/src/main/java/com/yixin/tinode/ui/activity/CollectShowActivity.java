package com.yixin.tinode.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.yixin.tinode.R;
import com.yixin.tinode.app.AppConst;
import com.yixin.tinode.ui.base.BaseActivity;
import com.yixin.tinode.ui.base.BasePresenter;
import com.yixin.tinode.util.TimeUtils;
import com.yixin.tinode.util.UIUtils;

import butterknife.BindView;
import co.tinode.tinodesdk.model.Drafty;

public class CollectShowActivity extends BaseActivity {

    @BindView(R.id.tvToolbarTitle)
    TextView mTvToolbarTitle;
    @BindView(R.id.text)
    TextView text;
    @BindView(R.id.tvFromAndTime)
    TextView tvFromAndTime;
    @BindView(R.id.image)
    ImageView imageView;

    Drafty.CollectDes des;
    String createTime;
    String fromName;
    String groupName;
    int type;

    @Override
    public void initView() {
        super.initView();
        mTvToolbarTitle.setText("详情");

        Bundle bundle = getIntent().getExtras();
        String information = bundle.getString("des","{}");
        createTime = bundle.getString("createTime","");
        fromName = bundle.getString("fromName","");
        groupName = bundle.getString("groupName","");
        type = bundle.getInt("type",0);
        des = new Gson().fromJson(information, Drafty.CollectDes.class);
        tvFromAndTime.setText("来自 " + fromName + " - " + groupName + TimeUtils.getMsgFormatTime(Long.valueOf(createTime), true));
        switch (type) {
            case AppConst.COLLECTION_TYPE_FILE:
//                helper.setViewVisibility(R.id.ivHeader, View.VISIBLE);
                break;
            case AppConst.COLLECTION_TYPE_TXT:
//                helper.setViewVisibility(R.id.ivHeader, View.GONE);
                text.setText(des.getTxt());
                break;
            case AppConst.COLLECTION_TYPE_VIDEO:
//                helper.setViewVisibility(R.id.ivHeader, View.VISIBLE);
                break;
            case AppConst.COLLECTION_TYPE_PIC:
//                helper.setViewVisibility(R.id.ivHeader, View.GONE);
                text.setVisibility(View.GONE);
                imageView.setVisibility(View.VISIBLE);
                String image = des.getTxt();
                Glide.with(this).load(image.startsWith("http") ? image + "?width=200" : "http://" + image + "?width=200").error(R.mipmap.default_img_failed)
                        .override(UIUtils.dip2Px(150), UIUtils.dip2Px(300))
                        .fitCenter()
                        .into(imageView);
                break;
            case AppConst.COLLECTION_TYPE_LOCATION:
//                helper.setViewVisibility(R.id.ivHeader, View.VISIBLE);
                break;
            case AppConst.COLLECTION_TYPE_SOUND:
//                helper.setViewVisibility(R.id.ivHeader, View.VISIBLE);
                break;
        }

    }

    @Override
    public void initData() {
        super.initData();


    }

    @Override
    public void initListener() {
        super.initListener();
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CollectShowActivity.this, ShowBigImageActivity.class);
                intent.putExtra("url", des.getTxt());
                intent.putExtra("name", des.getFname());
                CollectShowActivity.this.jumpToActivity(intent);
            }
        });
    }

    @Override
    protected BasePresenter createPresenter() {
        return null;
    }

    @Override
    protected int provideContentViewId() {
        return R.layout.activity_collect_show;
    }
}

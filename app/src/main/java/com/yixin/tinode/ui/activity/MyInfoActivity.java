package com.yixin.tinode.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.yixin.imagepicker.ImagePicker;
import com.yixin.imagepicker.bean.ImageItem;
import com.yixin.imagepicker.ui.ImageGridActivity;
import com.lqr.optionitemview.OptionItemView;
import com.yixin.tinode.R;
import com.yixin.tinode.app.AppConst;
import com.yixin.tinode.manager.BroadcastManager;
import com.yixin.tinode.tinode.Cache;
import com.yixin.tinode.tinode.util.UiUtils;
import com.yixin.tinode.ui.base.BaseActivity;
import com.yixin.tinode.ui.presenter.MyInfoAtPresenter;
import com.yixin.tinode.ui.view.IMyInfoAtView;

import java.util.ArrayList;

import butterknife.BindView;
import co.tinode.tinodesdk.MeTopic;


/**
 * @创建者 CSDN_LQR
 * @描述 我的个人信息
 */
public class MyInfoActivity extends BaseActivity<IMyInfoAtView, MyInfoAtPresenter> implements IMyInfoAtView {

    public static final int REQUEST_IMAGE_PICKER = 1000;

    @BindView(R.id.llHeader)
    LinearLayout mLlHeader;
    @BindView(R.id.ivHeader)
    ImageView mIvHeader;
    @BindView(R.id.oivName)
    OptionItemView mOivName;
    @BindView(R.id.oivAccount)
    OptionItemView mOivAccount;
    @BindView(R.id.oivQRCodeCard)
    OptionItemView mOivQRCodeCard;

    @Override
    public void init() {
        super.init();
        registerBR();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        registerBR();
    }

    @Override
    public void initData() {
        mPresenter.loadUserInfo();
    }

    @Override
    public void initListener() {
        mIvHeader.setOnClickListener(v -> {
//            Intent intent = new Intent(MyInfoActivity.this, ShowBigImageActivity.class);
//            intent.putExtra("url", mPresenter.mUserInfo.getPortraitUri().toString());
//            jumpToActivity(intent);
        });
        mLlHeader.setOnClickListener(v -> {
            Intent intent = new Intent(this, ImageGridActivity.class);
            startActivityForResult(intent, REQUEST_IMAGE_PICKER);
        });
        mOivQRCodeCard.setOnClickListener(v -> jumpToActivity(QRCodeCardActivity.class));
        mOivName.setOnClickListener(v -> jumpToActivity(ChangeMyNameActivity.class));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterBR();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_IMAGE_PICKER:
                if (resultCode == ImagePicker.RESULT_CODE_ITEMS) {
                    if (data != null) {
                        final MeTopic me = Cache.getTinode().getMeTopic();
                        ArrayList<ImageItem> images = (ArrayList<ImageItem>) data.getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);
                        if (images != null && images.size() > 0) {
                            ImageItem imageItem = images.get(0);
                            UiUtils.updateAvatar(MyInfoActivity.this, me, imageItem);

//                            mPresenter.setPortrait(imageItem);
                        }
                    }
                }
        }
    }


    private void registerBR() {
        BroadcastManager.getInstance(this).register(AppConst.CHANGE_INFO_FOR_CHANGE_NAME, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mPresenter.loadUserInfo();
            }
        });

    }

    private void unregisterBR() {
        BroadcastManager.getInstance(this).unregister(AppConst.CHANGE_INFO_FOR_CHANGE_NAME);
    }

    @Override
    protected MyInfoAtPresenter createPresenter() {
        return new MyInfoAtPresenter(this);
    }

    @Override
    protected int provideContentViewId() {
        return R.layout.activity_my_info;
    }

    @Override
    public ImageView getIvHeader() {
        return mIvHeader;
    }

    @Override
    public OptionItemView getOivName() {
        return mOivName;
    }

    @Override
    public OptionItemView getOivAccount() {
        return mOivAccount;
    }
}

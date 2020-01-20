package com.yixin.tinode.ui.activity;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.lqr.ninegridimageview.LQRNineGridImageView;
import com.lqr.ninegridimageview.LQRNineGridImageViewAdapter;
import com.yixin.tinode.R;
import com.yixin.tinode.app.AppConst;
import com.yixin.tinode.db.DBManager;
import com.yixin.tinode.db.model.GroupMember;
import com.yixin.tinode.ui.base.BaseActivity;
import com.yixin.tinode.ui.base.BasePresenter;
import com.yixin.tinode.util.LogUtils;
import com.yixin.tinode.util.UIUtils;

import java.util.List;

import butterknife.BindView;
import cn.bingoogolapple.qrcode.zxing.QRCodeEncoder;
import co.tinode.tinodesdk.model.Subscription;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.yixin.tinode.R.id.ivCard;

public class QRCodeCardActivity extends BaseActivity {

    private Subscription mUserInfo;
    private String mGroupId;

    @BindView(R.id.ivHeader)
    ImageView mIvHeader;
    @BindView(R.id.ngiv)
    LQRNineGridImageView mNgiv;
    @BindView(R.id.tvName)
    TextView mTvName;
    @BindView(ivCard)
    ImageView mIvCard;
    @BindView(R.id.tvTip)
    TextView mTvTip;

    @Override
    public void init() {
        mGroupId = getIntent().getStringExtra("groupId");
    }

    @Override
    public void initView() {
        mTvTip.setText(UIUtils.getString(R.string.qr_code_card_tip));
    }

    public void initData() {
        if (TextUtils.isEmpty(mGroupId)) {
//            mUserInfo = DBManager.getInstance().getUserInfo(UserCache.getId());
//            if (mUserInfo != null) {
//                Glide.with(this).load(mUserInfo.getPortraitUri()).centerCrop().into(mIvHeader);
//                mTvName.setText(mUserInfo.getName());
//                setQRCode(AppConst.QrCodeCommon.ADD + mUserInfo.getUserId());
//            }
        } else {
            mNgiv.setVisibility(View.VISIBLE);
            mIvHeader.setVisibility(View.GONE);
            Observable.just(DBManager.getInstance().getGroupsById(mGroupId))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(groups -> {
                        if (groups == null)
                            return;
                        mTvName.setText(groups.getName());
                    });
            mNgiv.setAdapter(new LQRNineGridImageViewAdapter<GroupMember>() {
                @Override
                protected void onDisplayImage(Context context, ImageView imageView, GroupMember groupMember) {
                    Glide.with(context).load(groupMember.getPortraitUri()).centerCrop().into(imageView);
                }
            });
            List<GroupMember> groupMembers = DBManager.getInstance().getGroupMembers(mGroupId);
            mNgiv.setImagesData(groupMembers);
            setQRCode(AppConst.QrCodeCommon.JOIN + mGroupId);
            mTvTip.setVisibility(View.GONE);
        }
    }

    private void setQRCode(String content) {
        Observable.just(QRCodeEncoder.syncEncodeQRCode(content, UIUtils.dip2Px(100)))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bitmap -> mIvCard.setImageBitmap(bitmap), this::loadQRCardError);
    }

    private void loadQRCardError(Throwable throwable) {
        LogUtils.sf(throwable.getLocalizedMessage());
    }

    @Override
    protected BasePresenter createPresenter() {
        return null;
    }

    @Override
    protected int provideContentViewId() {
        return R.layout.activity_qr_code_card;
    }
}

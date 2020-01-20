package com.yixin.tinode.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lqr.optionitemview.OptionItemView;
import com.yixin.tinode.R;
import com.yixin.tinode.app.AppConst;
import com.yixin.tinode.manager.BroadcastManager;
import com.yixin.tinode.ui.activity.CollectActivity;
import com.yixin.tinode.ui.activity.MainActivity;
import com.yixin.tinode.ui.activity.MyInfoActivity;
import com.yixin.tinode.ui.activity.SettingActivity;
import com.yixin.tinode.ui.base.BaseFragment;
import com.yixin.tinode.ui.presenter.MeFgPresenter;
import com.yixin.tinode.ui.view.IMeFgView;
import com.yixin.tinode.util.LogUtils;
import com.yixin.tinode.util.UIUtils;
import com.yixin.tinode.widget.CustomDialog;

import butterknife.BindView;
import butterknife.OnClick;
import cn.bingoogolapple.qrcode.zxing.QRCodeEncoder;
import co.tinode.tinodesdk.model.VCard;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @创建者 CSDN_LQR
 * @描述 我界面
 */
public class MeFragment extends BaseFragment<IMeFgView, MeFgPresenter> implements IMeFgView {

    private CustomDialog mQrCardDialog;

    @BindView(R.id.llMyInfo)
    LinearLayout mLlMyInfo;
    @BindView(R.id.ivHeader)
    ImageView mIvHeader;
    @BindView(R.id.tvName)
    TextView mTvName;
    @BindView(R.id.tvAccount)
    TextView mTvAccount;
    @BindView(R.id.ivQRCordCard)
    ImageView mIvQRCordCard;

    @BindView(R.id.oivAlbum)
    OptionItemView mOivAlbum;

    @BindView(R.id.oivCollection)
    OptionItemView mOivCollection;

    @BindView(R.id.oivWallet)
    OptionItemView mOivWallet;
    @BindView(R.id.oivCardPaket)
    OptionItemView mOivCardPaket;

    @BindView(R.id.oivSetting)
    OptionItemView mOivSetting;
    boolean isFirst = true;

    @Override
    public void init() {
        registerBR();
    }

    @OnClick({R.id.oivCollection})
    public void onViewClick(View v) {
        switch (v.getId()) {
            case R.id.oivCollection:
                startActivity(new Intent(getActivity(), CollectActivity.class));
                break;
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && isFirst) {
            isFirst = false;
            mPresenter.loadUserInfo();
        }
    }

    @Override
    public void initData() {
        mPresenter.loadUserInfo();
    }

    @Override
    public void initView(View rootView) {
//        mIvQRCordCard.setOnClickListener(v -> showQRCard());
        mOivAlbum.setOnClickListener(v -> ((MainActivity) getActivity()).jumpToWebViewActivity(AppConst.WeChatUrl.MY_JIAN_SHU));
        mOivWallet.setOnClickListener(v -> ((MainActivity) getActivity()).jumpToWebViewActivity(AppConst.WeChatUrl.MY_OSCHINA));
        mOivCardPaket.setOnClickListener(v -> ((MainActivity) getActivity()).jumpToWebViewActivity(AppConst.WeChatUrl.MY_GITHUB));
    }

    @Override
    public void initListener() {
        mLlMyInfo.setOnClickListener(v -> ((MainActivity) getActivity()).jumpToActivityAndClearTop(MyInfoActivity.class));
        mOivSetting.setOnClickListener(v -> ((MainActivity) getActivity()).jumpToActivityAndClearTop(SettingActivity.class));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterBR();
    }

    private void showQRCard() {
        if (mQrCardDialog == null) {
            View qrCardView = View.inflate(getActivity(), R.layout.include_qrcode_card, null);
            ImageView ivHeader = (ImageView) qrCardView.findViewById(R.id.ivHeader);
            TextView tvName = (TextView) qrCardView.findViewById(R.id.tvName);
            ImageView ivCard = (ImageView) qrCardView.findViewById(R.id.ivCard);
            TextView tvTip = (TextView) qrCardView.findViewById(R.id.tvTip);
            tvTip.setText(UIUtils.getString(R.string.qr_code_card_tip));

            VCard userInfo = mPresenter.getUserInfo();
            if (userInfo != null) {
//                Glide.with(getActivity()).load(userInfo.getPortraitUri()).centerCrop().into(ivHeader);
                tvName.setText(userInfo.fn);
                Observable.just(QRCodeEncoder.syncEncodeQRCode(AppConst.QrCodeCommon.ADD + userInfo.n, UIUtils.dip2Px(100)))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(bitmap -> ivCard.setImageBitmap(bitmap), this::loadQRCardError);
            }

            mQrCardDialog = new CustomDialog(getActivity(), 300, 400, qrCardView, R.style.MyDialog);
        }
        mQrCardDialog.show();
    }

    private void loadQRCardError(Throwable throwable) {
        LogUtils.sf(throwable.getLocalizedMessage());
    }

    private void registerBR() {
        BroadcastManager.getInstance(getActivity()).register(AppConst.CHANGE_INFO_FOR_ME, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mPresenter.loadUserInfo();
            }
        });
    }

    private void unregisterBR() {
        BroadcastManager.getInstance(getActivity()).unregister(AppConst.CHANGE_INFO_FOR_ME);
    }

    @Override
    protected MeFgPresenter createPresenter() {
        return new MeFgPresenter((MainActivity) getActivity());
    }

    @Override
    protected int provideContentViewId() {
        return R.layout.fragment_me;
    }

    @Override
    public ImageView getIvHeader() {
        return mIvHeader;
    }

    @Override
    public TextView getTvName() {
        return mTvName;
    }

    @Override
    public TextView getTvAccount() {
        return mTvAccount;
    }
}

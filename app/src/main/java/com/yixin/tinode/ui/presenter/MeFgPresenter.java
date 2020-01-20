package com.yixin.tinode.ui.presenter;

import android.content.SharedPreferences;
import android.graphics.Bitmap;

import com.yixin.tinode.R;
import com.yixin.tinode.tinode.Cache;
import com.yixin.tinode.tinode.widgets.RoundImageDrawable;
import com.yixin.tinode.ui.activity.LoginActivity;
import com.yixin.tinode.ui.base.BaseActivity;
import com.yixin.tinode.ui.base.BasePresenter;
import com.yixin.tinode.ui.view.IMeFgView;
import com.yixin.tinode.util.LogUtils;
import com.yixin.tinode.util.StringUtils;
import com.yixin.tinode.util.UIUtils;

import co.tinode.tinodesdk.MeTopic;
import co.tinode.tinodesdk.model.VCard;


public class MeFgPresenter extends BasePresenter<IMeFgView> {

    private VCard mUserInfo;
    private MeTopic me;
    private boolean isFirst = true;

    public MeFgPresenter(BaseActivity context) {
        super(context);
    }

    public void loadUserInfo() {
        me = (MeTopic) Cache.getTinode().getMeTopic();
        if (me != null && StringUtils.isEmpty(getView().getTvAccount().getText().toString())) {
//            activity.findViewById(R.id.uploadAvatar).setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    UiUtils.requestAvatar(AccountInfoFragment.this);
//                }
//            });

            mUserInfo = (VCard) me.getPub();
            fillView();
        }
    }

    public void fillView() {
        if (mUserInfo != null) {
//            Glide.with(mContext).load(mUserInfo.getPortraitUri()).centerCrop().into(getView().getIvHeader());
            final Bitmap bmp = mUserInfo.getBitmap();
            if (bmp != null) {
                getView().getIvHeader().setImageDrawable(new RoundImageDrawable(bmp));
            }
            final SharedPreferences pref = android.preference.PreferenceManager.getDefaultSharedPreferences(mContext);
            String login = pref.getString(LoginActivity.PREFS_LAST_LOGIN, null);

            String yixinAccount="一信号："+Cache.getTinode().getMyId();
            getView().getTvAccount().setText(yixinAccount);//(UIUtils.getString(R.string.my_chat_account, login));
            getView().getTvName().setText(mUserInfo.fn);
        }
    }

    private void loadError(Throwable throwable) {
        LogUtils.sf(throwable.getLocalizedMessage());
        UIUtils.showToast(throwable.getLocalizedMessage());
    }

    public VCard getUserInfo() {
        return mUserInfo;
    }
}

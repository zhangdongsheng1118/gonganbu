package com.yixin.tinode.ui.presenter;

import android.content.SharedPreferences;
import android.graphics.Bitmap;

import com.yixin.imagepicker.bean.ImageItem;
import com.yixin.tinode.R;
import com.yixin.tinode.tinode.Cache;
import com.yixin.tinode.tinode.widgets.RoundImageDrawable;
import com.yixin.tinode.ui.activity.LoginActivity;
import com.yixin.tinode.ui.base.BaseActivity;
import com.yixin.tinode.ui.base.BasePresenter;
import com.yixin.tinode.ui.view.IMyInfoAtView;
import com.yixin.tinode.util.LogUtils;
import com.yixin.tinode.util.UIUtils;

import co.tinode.tinodesdk.MeTopic;
import co.tinode.tinodesdk.model.VCard;

public class MyInfoAtPresenter extends BasePresenter<IMyInfoAtView> {

    public VCard mUserInfo;
    private MeTopic me;

    public MyInfoAtPresenter(BaseActivity context) {
        super(context);
    }

    public void loadUserInfo() {
        me = (MeTopic) Cache.getTinode().getMeTopic();
        if (me != null) {
//            activity.findViewById(R.id.uploadAvatar).setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    UiUtils.requestAvatar(AccountInfoFragment.this);
//                }
//            });
            mUserInfo = (VCard) me.getPub();
            if(mUserInfo == null){
                return;
            }
            final Bitmap bmp = mUserInfo.getBitmap();
            if (bmp != null) {
                getView().getIvHeader().setImageDrawable(new RoundImageDrawable(bmp));
            }
            final SharedPreferences pref = android.preference.PreferenceManager.getDefaultSharedPreferences(mContext);
            String login = pref.getString(LoginActivity.PREFS_LAST_LOGIN, null);
//            Glide.with(mContext).load(mUserInfo.getPortraitUri()).centerCrop().into(getView().getIvHeader());
            getView().getOivName().setRightText(mUserInfo.fn);
            getView().getOivAccount().setRightText(UIUtils.getString(R.string.my_chat_account, login));
        }
    }

    public void setPortrait(ImageItem imageItem) {
//        mContext.showWaitingDialog(UIUtils.getString(R.string.please_wait));
        //上传头像
//        ApiRetrofit.getInstance().getQiNiuToken()
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(qiNiuTokenResponse -> {
//                    if (qiNiuTokenResponse != null && qiNiuTokenResponse.getCode() == 200) {
//                        if (mUploadManager == null)
//                            mUploadManager = new UploadManager();
//                        File imageFile = new File(imageItem.path);
//                        QiNiuTokenResponse.ResultEntity result = qiNiuTokenResponse.getResult();
//                        String domain = result.getDomain();
//                        String token = result.getToken();
//                        //上传到七牛
//                        mUploadManager.put(imageFile, null, token, (s, responseInfo, jsonObject) -> {
//                            if (responseInfo.isOK()) {
//                                String key = jsonObject.optString("key");
//                                String imageUrl = "http://" + domain + "/" + key;
//                                //修改自己服务器头像数据
//                                ApiRetrofit.getInstance().setPortrait(imageUrl)
//                                        .subscribeOn(Schedulers.io())
//                                        .observeOn(AndroidSchedulers.mainThread())
//                                        .subscribe(setPortraitResponse -> {
//                                            if (setPortraitResponse != null && setPortraitResponse.getCode() == 200) {
//                                                Friend friend = DBManager.getInstance().getFriendById(UserCache.getId());
//                                                if (friend != null) {
//                                                    friend.setPortraitUri(imageUrl);
//                                                    DBManager.getInstance().saveOrUpdateFriend(friend);
//                                                    DBManager.getInstance().updateGroupMemberPortraitUri(UserCache.getId(), imageUrl);
//                                                    Glide.with(mContext).load(friend.getPortraitUri()).centerCrop().into(getView().getIvHeader());
//                                                    BroadcastManager.getInstance(mContext).sendBroadcast(AppConst.CHANGE_INFO_FOR_ME);
//                                                    BroadcastManager.getInstance(mContext).sendBroadcast(AppConst.UPDATE_CONVERSATIONS);
//                                                    BroadcastManager.getInstance(mContext).sendBroadcast(AppConst.UPDATE_GROUP);
//                                                    UIUtils.showToast(UIUtils.getString(R.string.set_success));
//                                                }
//                                                mContext.hideWaitingDialog();
//                                            } else {
//                                                uploadError(null);
//                                            }
//                                        }, this::uploadError);
//                            } else {
//                                uploadError(null);
//                            }
//                        }, null);
//                    } else {
//                        uploadError(null);
//                    }
//                }, this::uploadError);
    }

    private void uploadError(Throwable throwable) {
        if (throwable != null)
            LogUtils.sf(throwable.getLocalizedMessage());
        mContext.hideWaitingDialog();
        UIUtils.showToast(UIUtils.getString(R.string.set_fail));
    }
}

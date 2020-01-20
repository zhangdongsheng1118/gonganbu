package com.yixin.tinode.ui.presenter;

import com.yixin.tinode.R;
import com.yixin.tinode.api.ApiRetrofit;
import com.yixin.tinode.ui.base.BaseActivity;
import com.yixin.tinode.ui.base.BasePresenter;
import com.yixin.tinode.ui.view.IPostScriptAtView;
import com.yixin.tinode.util.LogUtils;
import com.yixin.tinode.util.UIUtils;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class PostScriptAtPresenter extends BasePresenter<IPostScriptAtView> {

    public PostScriptAtPresenter(BaseActivity context) {
        super(context);
    }

    public void addFriend(String userId) {
//        String msg = getView().getEtMsg().getText().toString().trim();
//        ApiRetrofit.getInstance().sendFriendInvitation(userId, msg)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(friendInvitationResponse -> {
//                    if (friendInvitationResponse.getCode() == 200) {
//                        UIUtils.showToast(UIUtils.getString(R.string.rquest_sent_success));
//                        mContext.finish();
//                    } else {
//                        UIUtils.showToast(UIUtils.getString(R.string.rquest_sent_fail));
//                    }
//                }, this::loadError);
    }

    private void loadError(Throwable throwable) {
        LogUtils.sf(throwable.getLocalizedMessage());
        UIUtils.showToast(throwable.getLocalizedMessage());
    }
}

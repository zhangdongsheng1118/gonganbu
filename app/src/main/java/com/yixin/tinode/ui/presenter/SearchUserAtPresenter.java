package com.yixin.tinode.ui.presenter;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.blankj.utilcode.util.ToastUtils;
import com.lqr.adapter.LQRAdapterForRecyclerView;
import com.lqr.adapter.LQRViewHolderForRecyclerView;
import com.yixin.tinode.R;
import com.yixin.tinode.tinode.Cache;
import com.yixin.tinode.tinode.util.UiUtils;
import com.yixin.tinode.ui.activity.UserInfoActivity;
import com.yixin.tinode.ui.base.BaseActivity;
import com.yixin.tinode.ui.base.BasePresenter;
import com.yixin.tinode.ui.view.ISearchUserAtView;
import com.yixin.tinode.util.LogUtils;
import com.yixin.tinode.util.UIUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import co.tinode.tinodesdk.PromisedReply;
import co.tinode.tinodesdk.Tinode;
import co.tinode.tinodesdk.model.ServerMessage;
import co.tinode.tinodesdk.model.Subscription;
import co.tinode.tinodesdk.model.VCard;

public class SearchUserAtPresenter extends BasePresenter<ISearchUserAtView> {

    private LQRAdapterForRecyclerView<Subscription> mAdapter;
    private List<Subscription> mData = new ArrayList<>();

    public SearchUserAtPresenter(BaseActivity context) {
        super(context);
    }


    private void setAdapter() {
        if (mAdapter == null) {
            mAdapter = new LQRAdapterForRecyclerView<Subscription>(mContext, mData, R.layout.item_recent_message) {
                @Override
                public void convert(LQRViewHolderForRecyclerView helper, Subscription item, int position) {
                    ImageView ivHeader = helper.getView(R.id.ivHeader);

                    final Subscription<VCard, String> userInfo = mData.get(position);
                    VCard pub = userInfo.pub;
                    String topic = userInfo.user;
                    if (userInfo != null) {
//                            Glide.with(mContext).load(userInfo.getPortraitUri()).centerCrop().into(ivHeader);
                        UiUtils.assignBitmap(mContext, ivHeader,
                                pub != null ? pub.getBitmap() : null,
                                pub != null ? pub.fn : null,
                                topic);

                        helper.setText(R.id.tvDisplayName, pub != null ? pub.fn : "未知用户")
//									.setText(R.id.tvTime, TimeUtils.getMsgFormatTime(item.getReceivedTime()))
                                .setViewVisibility(R.id.ngiv, View.GONE)
                                .setViewVisibility(R.id.ivHeader, View.VISIBLE);
                    }

                    helper.setBackgroundColor(R.id.flRoot, android.R.color.white);
                }
            };
            mAdapter.setOnItemClickListener((helper, parent, itemView, position) -> {
//                UserInfo userInfo = new UserInfo(result.getId(), result.getNickname(), Uri.parse(result.getPortraitUri()));
                LogUtils.e("search click topic:" + mAdapter.getItem(position).topic
                        + "user:" + mAdapter.getItem(position).user);
                Intent intent = new Intent(mContext, UserInfoActivity.class);
                intent.putExtra("subscription", mAdapter.getItem(position));
                mContext.jumpToActivity(intent);
            });

            getView().getRvMsg().setAdapter(mAdapter);
        } else {
            mAdapter.notifyDataSetChangedWrapper();
        }
    }

    public void searchUser() {
        mData.clear();
        String content = getView().getEtSearchContent().getText().toString().trim();

        if (TextUtils.isEmpty(content)) {
            UIUtils.showToast(UIUtils.getString(R.string.content_no_empty));
            return;
        }

        mContext.showWaitingDialog(UIUtils.getString(R.string.please_wait));

        final Tinode tinode = Cache.getTinode();
        try {
            tinode.fndSet(content).thenApply(new PromisedReply.SuccessListener<ServerMessage>() {
                @Override
                public PromisedReply<ServerMessage> onSuccess(ServerMessage result) throws Exception {
                    if (result.ctrl != null && result.ctrl.code == ServerMessage.STATUS_OK) {
                        tinode.fndGet().thenApply(new PromisedReply.SuccessListener<ServerMessage>() {
                            @Override
                            public PromisedReply<ServerMessage> onSuccess(ServerMessage result) throws Exception {
                                if (result.meta != null) {
                                    Subscription[] data = result.meta.sub;
                                    mData.addAll(Arrays.asList(data));
                                    mContext.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            mContext.hideWaitingDialog();
                                            setAdapter();
                                            getView().getLlSearch().setVisibility(View.GONE);
                                        }
                                    });
                                } else {
                                    mContext.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            mContext.hideWaitingDialog();
                                            getView().getRlNoResultTip().setVisibility(View.VISIBLE);
                                            getView().getLlSearch().setVisibility(View.GONE);
                                        }
                                    });
                                }
                                return null;
                            }
                        }, new PromisedReply.FailureListener<ServerMessage>() {
                            @Override
                            public PromisedReply<ServerMessage> onFailure(Exception err) throws Exception {
                                mContext.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mContext.hideWaitingDialog();
                                        ToastUtils.showShort("获取结果失败");
                                    }
                                });
                                return null;
                            }
                        });
                    } else {
                        mContext.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mContext.hideWaitingDialog();
                                ToastUtils.showShort("搜索失败");
                            }
                        });
                    }
                    return null;
                }
            }, new PromisedReply.FailureListener<ServerMessage>() {
                @Override
                public PromisedReply<ServerMessage> onFailure(Exception err) throws Exception {
                    mContext.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mContext.hideWaitingDialog();
                            ToastUtils.showShort("搜索失败");
                        }
                    });
                    return null;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            mContext.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mContext.hideWaitingDialog();
                    ToastUtils.showShort("搜索异常");
                }
            });
        }

//        if (RegularUtils.isMobile(content)) {
//            ApiRetrofit.getInstance().getUserInfoFromPhone(AppConst.REGION, content)
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe(getUserInfoByPhoneResponse -> {
//                        mContext.hideWaitingDialog();
//                        if (getUserInfoByPhoneResponse.getCode() == 200) {
//                            GetUserInfoByPhoneResponse.ResultEntity result = getUserInfoByPhoneResponse.getResult();
//
//                        } else {
//                            getView().getRlNoResultTip().setVisibility(View.VISIBLE);
//                            getView().getLlSearch().setVisibility(View.GONE);
//                        }
//                    }, this::loadError);
//        } else {
//            ApiRetrofit.getInstance().getUserInfoById(content)
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe(getUserInfoByIdResponse -> {
//                        mContext.hideWaitingDialog();
//                        if (getUserInfoByIdResponse.getCode() == 200) {
//                            GetUserInfoByIdResponse.ResultEntity result = getUserInfoByIdResponse.getResult();
//                            UserInfo userInfo = new UserInfo(result.getId(), result.getNickname(), Uri.parse(result.getPortraitUri()));
//                            Intent intent = new Intent(mContext, UserInfoActivity.class);
//                            intent.putExtra("userInfo", userInfo);
//                            mContext.jumpToActivity(intent);
//                        } else {
//                            getView().getRlNoResultTip().setVisibility(View.VISIBLE);
//                            getView().getLlSearch().setVisibility(View.GONE);
//                        }
//                    }, this::loadError);
//        }
    }

    private void loadError(Throwable throwable) {
        mContext.hideWaitingDialog();
        LogUtils.sf(throwable.getLocalizedMessage());
    }
}

package com.yixin.tinode.ui.presenter;

import android.content.Intent;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
import com.lqr.adapter.LQRAdapterForRecyclerView;
import com.lqr.adapter.LQRViewHolderForRecyclerView;
import com.yixin.tinode.R;
import com.yixin.tinode.db.tinode.BaseDb;
import com.yixin.tinode.db.tinode.MessageDb;
import com.yixin.tinode.db.tinode.StoredMessage;
import com.yixin.tinode.db.tinode.StoredTopic;
import com.yixin.tinode.db.tinode.SubscriberDb;
import com.yixin.tinode.tinode.Cache;
import com.yixin.tinode.tinode.util.UiUtils;
import com.yixin.tinode.tinode.widgets.LetterTileDrawable;
import com.yixin.tinode.tinode.widgets.RoundImageDrawable;
import com.yixin.tinode.ui.activity.SessionActivity;
import com.yixin.tinode.ui.activity.UserInfoActivity;
import com.yixin.tinode.ui.base.BaseActivity;
import com.yixin.tinode.ui.base.BasePresenter;
import com.yixin.tinode.ui.view.ISearchMsgAtView;
import com.yixin.tinode.ui.view.ISearchUserAtView;
import com.yixin.tinode.util.LogUtils;
import com.yixin.tinode.util.UIUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import co.tinode.tinodesdk.PromisedReply;
import co.tinode.tinodesdk.Storage;
import co.tinode.tinodesdk.Tinode;
import co.tinode.tinodesdk.Topic;
import co.tinode.tinodesdk.model.ServerMessage;
import co.tinode.tinodesdk.model.Subscription;
import co.tinode.tinodesdk.model.VCard;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class SearchMsgAtPresenter extends BasePresenter<ISearchMsgAtView> {

    private LQRAdapterForRecyclerView<StoredMessage> mAdapter;
    private List<StoredMessage> mData = new ArrayList<>();

    public SearchMsgAtPresenter(BaseActivity context) {
        super(context);
    }


    private void setAdapter() {
        if (mAdapter == null) {
            mAdapter = new LQRAdapterForRecyclerView<StoredMessage>(mContext, mData, R.layout.item_recent_message) {
                @Override
                public void convert(LQRViewHolderForRecyclerView helper, StoredMessage item, int position) {
                    ImageView ivHeader = helper.getView(R.id.ivHeader);

                    Topic topic = getView().getTopic();
                    Subscription<VCard, ?> sub = topic != null ? topic.getSubscription(item.from) : null;
                    if (sub != null && sub.pub != null) {

                        VCard pub = sub.pub;
                        UiUtils.assignBitmap(mContext, ivHeader,
                                pub != null ? pub.getBitmap() : null,
                                pub != null ? pub.fn : null,
                                topic.getName());

                        helper.setText(R.id.tvDisplayName, pub != null ? pub.fn : "未知用户")
//									.setText(R.id.tvTime, TimeUtils.getMsgFormatTime(item.getReceivedTime()))
                                .setViewVisibility(R.id.ngiv, View.GONE)
                                .setViewVisibility(R.id.ivHeader, View.VISIBLE);
                    }

                    helper.setText(R.id.tvContent, item.search);
                    helper.setBackgroundColor(R.id.flRoot, android.R.color.white);
                }
            };
            mAdapter.setOnItemClickListener((helper, parent, itemView, position) -> {
                Topic topic = getView().getTopic();
                StoredMessage item = mData.get(position);
                Subscription<VCard, ?> sub = topic != null ? topic.getSubscription(item.from) : null;
                if (sub != null) {
                    Intent intent = new Intent(mContext, SessionActivity.class);
                    intent.putExtra("sessionId", sub.topic);
                    if (topic.getTopicType() == Topic.TopicType.P2P) {
                        intent.putExtra("sessionType", SessionActivity.SESSION_TYPE_PRIVATE);
                    } else {
                        intent.putExtra("sessionType", SessionActivity.SESSION_TYPE_GROUP);
                    }
                    intent.putExtra("firstMsgId", item.id);
                    mContext.jumpToActivity(intent);
                } else {
                    ToastUtils.showShort("未查询到用户信息");
                }
            });

            getView().getRvMsg().setAdapter(mAdapter);
        } else {
            mAdapter.notifyDataSetChangedWrapper();
        }
    }

    public void searchUser() {
        mData.clear();
        String content = getView().getEtSearchContent().getText().toString();

        if (TextUtils.isEmpty(content.trim())) {
            UIUtils.showToast(UIUtils.getString(R.string.content_no_empty));
            return;
        }

        mContext.showWaitingDialog(UIUtils.getString(R.string.please_wait));

        Observable.just(MessageDb.queryList(BaseDb.getInstance().getWritableDatabase(), ((StoredTopic) getView().getTopic().getLocal()).id, 1, 100, content))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(messages -> {
                    if (messages.size() > 0) {
                        mData.addAll(messages);

                        mContext.hideWaitingDialog();
                        setAdapter();
                        getView().getLlSearch().setVisibility(View.GONE);
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
                }, throwable -> {
                    mContext.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mContext.hideWaitingDialog();
                            ToastUtils.showShort("搜索失败");
                        }
                    });
                });
    }

    private void loadError(Throwable throwable) {
        mContext.hideWaitingDialog();
        LogUtils.sf(throwable.getLocalizedMessage());
    }
}

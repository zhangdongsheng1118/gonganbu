package com.yixin.tinode.ui.presenter;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.lqr.adapter.LQRAdapterForRecyclerView;
import com.lqr.adapter.LQRViewHolderForRecyclerView;
import com.lqr.ninegridimageview.LQRNineGridImageView;
import com.lqr.ninegridimageview.LQRNineGridImageViewAdapter;
import com.yixin.tinode.R;
import com.yixin.tinode.db.DBManager;
import com.yixin.tinode.db.model.GroupMember;
import com.yixin.tinode.db.model.Groups;
import com.yixin.tinode.db.tinode.BaseDb;
import com.yixin.tinode.db.tinode.StoredTopic;
import com.yixin.tinode.db.tinode.TopicDb;
import com.yixin.tinode.tinode.Cache;
import com.yixin.tinode.tinode.util.UiUtils;
import com.yixin.tinode.ui.activity.SessionActivity;
import com.yixin.tinode.ui.base.BaseActivity;
import com.yixin.tinode.ui.base.BasePresenter;
import com.yixin.tinode.ui.view.IGroupListAtView;
import com.yixin.tinode.util.TimeUtils;
import com.yixin.tinode.util.UIUtils;
import com.yixin.tinode.widget.CustomDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import co.tinode.tinodesdk.ComTopic;
import co.tinode.tinodesdk.NotConnectedException;
import co.tinode.tinodesdk.PromisedReply;
import co.tinode.tinodesdk.Topic;
import co.tinode.tinodesdk.model.ServerMessage;
import co.tinode.tinodesdk.model.VCard;


public class GroupListAtPresenter extends BasePresenter<IGroupListAtView> {

    private List<Topic> mData = new ArrayList<>();
    private LQRAdapterForRecyclerView<Topic> mAdapter;
    private int mUnreadCountTotal = 0;
    private LQRNineGridImageViewAdapter mNgivAdapter = new LQRNineGridImageViewAdapter<GroupMember>() {
        @Override
        protected void onDisplayImage(Context context, ImageView imageView, GroupMember groupMember) {
            Glide.with(context).load(groupMember.getPortraitUri()).centerCrop().into(imageView);
        }
    };
    private CustomDialog mConversationMenuDialog;

    public GroupListAtPresenter(BaseActivity context) {
        super(context);
    }

    public void loadGroups() {
        loadData();
        setAdapter();
    }

    private void loadData() {
        mData.clear();
        //List<Topic> list = Cache.getTinode().getFilteredTopicsForSession(Topic.TopicType.GRP, null);
        List<Topic> list = Cache.getTinode().getGroupTopics(null);
        if (list != null) {
            mData.addAll(list);
            orderData();
        }
        if (mData != null && mData.size() > 0) {
            getView().getLlGroups().setVisibility(View.VISIBLE);
        } else {
            getView().getLlGroups().setVisibility(View.GONE);
        }
    }

    private void orderData() {
        /*
         * int compare(Person p1, Person p2) 返回一个基本类型的整型，
         * 返回负数表示：p1 小于p2，
         * 返回0 表示：p1和p2相等，
         * 返回正数表示：p1大于p2
         */
        Collections.sort(mData, (p1, p2) -> {
            if (p1.isTop()) {
                if (p2.isTop()) {
                    Date date1 = p1.getLastTs();
                    Date date2 = p2.getLastTs();
                    if (date1 == null) {
                        return 1;
                    }
                    if (date2 == null) {
                        return -1;
                    }
                    return date1.getTime() < date2.getTime() ? 1 : -1;
                } else {
                    return -1;
                }
            } else {
                if (p2.isTop()) {
                    return 1;
                } else {
                    Date date1 = p1.getLastTs();
                    Date date2 = p2.getLastTs();
                    if (date1 == null) {
                        return 1;
                    }
                    if (date2 == null) {
                        return -1;
                    }
                    return date1.getTime() < date2.getTime() ? 1 : -1;
                }
            }
        });

    }

    private void setAdapter() {
        if (mAdapter == null) {
            mAdapter = new LQRAdapterForRecyclerView<Topic>(mContext, mData, R.layout.item_group_list) {
                @Override
                public void convert(LQRViewHolderForRecyclerView helper, Topic item1, int position) {
                    ComTopic<VCard> item = (ComTopic<VCard>) item1;
                    if (item.getTopicType() == Topic.TopicType.P2P) {
                        return;
                    } else if (item.getTopicType() == Topic.TopicType.GRP) {

                        ImageView ivHeader = helper.getView(R.id.ivHeader);

                        final Topic userInfo = mData.get(position);
                        VCard pub = (VCard) userInfo.getPub();
                        String topic = userInfo.getName();
                        if (userInfo != null) {
//                            Glide.with(mContext).load(userInfo.getPortraitUri()).centerCrop().into(ivHeader);

                            UiUtils.assignBitmap(mContext, ivHeader,
                                    null,
                                    pub != null ? pub.fn : null,
                                    topic);

                            helper.setText(R.id.tvDisplayName, pub != null ? pub.fn : "未知用户");
//									.setText(R.id.tvTime, TimeUtils.getMsgFormatTime(item.getReceivedTime()))
                            helper.setViewVisibility(R.id.ngiv, View.GONE)
                                    .setViewVisibility(R.id.ivHeader, View.VISIBLE);
                        }
                    }
                }
            };
            mAdapter.setOnItemClickListener((helper, parent, itemView, position) -> {
                Intent intent = new Intent(mContext, SessionActivity.class);
                Topic item = mData.get(position);
                intent.putExtra("sessionId", item.getName());
                if (item.getTopicType() == Topic.TopicType.P2P) {
                    intent.putExtra("sessionType", SessionActivity.SESSION_TYPE_PRIVATE);
                } else {
                    intent.putExtra("sessionType", SessionActivity.SESSION_TYPE_GROUP);
                }
                mContext.jumpToActivity(intent);
            });
            getView().getRvGroupList().setAdapter(mAdapter);
        } else {
            mAdapter.notifyDataSetChangedWrapper();
        }
    }


}

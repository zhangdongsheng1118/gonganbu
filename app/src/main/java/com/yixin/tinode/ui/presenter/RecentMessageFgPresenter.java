package com.yixin.tinode.ui.presenter;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.StringUtils;
import com.lqr.adapter.LQRAdapterForRecyclerView;
import com.lqr.adapter.LQRViewHolderForRecyclerView;
import com.yixin.tinode.R;
import com.yixin.tinode.app.MyApp;
import com.yixin.tinode.db.tinode.StoredTopic;
import com.yixin.tinode.tinode.Cache;
import com.yixin.tinode.tinode.util.UiUtils;
import com.yixin.tinode.ui.activity.SessionActivity;
import com.yixin.tinode.ui.base.BasePresenter;
import com.yixin.tinode.ui.view.IRecentMessageFgView;
import com.yixin.tinode.util.TimeUtils;
import com.yixin.tinode.util.UIUtils;
import com.yixin.tinode.widget.CustomDialog;
import com.zuozhan.app.activity.AllBaseActivity;
import com.zuozhan.app.activity.ZHMainActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import co.tinode.tinodesdk.ComTopic;
import co.tinode.tinodesdk.NotConnectedException;
import co.tinode.tinodesdk.Topic;
import co.tinode.tinodesdk.model.MetaSetDesc;
import co.tinode.tinodesdk.model.PrivateType;
import co.tinode.tinodesdk.model.VCard;

/**
 *
 */
public class RecentMessageFgPresenter extends BasePresenter<IRecentMessageFgView> {

    private List<Topic> mData = new ArrayList<>();
    private LQRAdapterForRecyclerView<Topic> mAdapter;
    private int mUnreadCountTotal = 0;
    //加载倒计时，
    private int loadCount = 3;
    //    private LQRNineGridImageViewAdapter mNgivAdapter = new LQRNineGridImageViewAdapter<GroupMember>() {
    //        @Override
    //        protected void onDisplayImage(Context context, ImageView imageView, GroupMember groupMember) {
    //            Glide.with(context).load(groupMember.getPortraitUri()).centerCrop().into(imageView);
    //        }
    //    };
    private CustomDialog mConversationMenuDialog;

    public RecentMessageFgPresenter(AllBaseActivity context) {
        super(context);
    }

    //TODO msg should not get this way,later i will fetch it from the server
    public void newMsgComing(String topic, Date time, String fromUid, String msg) {
        if(topic==null){
            loadData();
            mAdapter.notifyDataSetChangedWrapper();
            return;
        }
        Topic cur = Cache.getTinode().getTopic(topic);
        //TOTO 这里先这样实现,因为默认是所有人互相是好友
        if (cur == null) {
            return;
        }

        if (!StringUtils.isEmpty(msg) && time != null) {
            cur.setLastMsg(msg);

            cur.setTouched(time);
            ((StoredTopic) cur.getLocal()).lastUsed = time;

            cur.setLastAct(fromUid);

            //added by pcg
            Topic fromUser=Cache.getTinode().getTopic(fromUid);
            if(fromUser!=null){
                VCard pub = (VCard) fromUser.getPub();
                cur.setUserName(pub.fn);
            } else {
                cur.setUserName(fromUid);
            }
        }

        loadData();
        mAdapter.notifyDataSetChangedWrapper();
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
                    Date date1 = p1.getTouched();
                    Date date2 = p2.getTouched();
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
                    Date date1 = p1.getTouched();
                    Date date2 = p2.getTouched();
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

    private void refreshConversations() {
        loadCount--;

        MyApp.getMainHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                loadData();
                setAdapter();

                if (mData.size() == 0 && loadCount > 0) {
                    refreshConversations();
                } else {
                    ((AllBaseActivity) mContext).hideWaitingDialog();
                }
            }
        }, 200);
    }

    public void getConversations() {
        if (!mContext.isFinishing()) {
            ((AllBaseActivity) mContext).showWaitingDialog("正在加载数据……");
        }

        loadData();
        setAdapter();

        if (mData.size() == 0) {
            refreshConversations();
        } else {
            ((AllBaseActivity) mContext).hideWaitingDialog();
        }
    }

    public void getConversationsOnce() {
        loadData();
        setAdapter();
    }

    private void loadData() {
        mData.clear();
        List<Topic> list = Cache.getTinode().getFilteredTopicsForSession(Topic.TopicType.USER, null);
        //这句话华为等部分机型报错，注释掉
        //        Log.e("liulei", "消息列表" + (JSON.toJSONString(list)));

        if (list != null) {
            mData.addAll(list);
            orderData();
        }
    }

    /*
        private void filterData(List<Topic> conversations) {
            for (int i = 0; i < conversations.size(); i++) {
                Topic item = conversations.get(i);
                //其他消息会话不显示（比如：系统消息）
                if (!(item.getTopicType() == Topic.TopicType.P2P || item.getTopicType() == Topic.TopicType.GRP)) {
                    conversations.remove(i);
                    i--;
                    continue;
                }
                if (item.getTopicType() == Topic.TopicType.GRP) {
    //				List<GroupMember> groupMembers = DBManager.getInstance().getGroupMembers(item.getTargetId());
    //				if (groupMembers == null || groupMembers.size() == 0) {
    //					DBManager.getInstance().deleteGroupsById(item.getTargetId());//删除没有群成员的群
    //					conversations.remove(i);
    //					i--;
    //				}
                } else if (item.getTopicType() == Topic.TopicType.P2P) {
    //				if (!DBManager.getInstance().isMyFriend(item.getTargetId())) {
    //					conversations.remove(i);
    //					i--;
    //				}
                }
            }
            mUnreadCountTotal = 0;
            for (Topic conversation : conversations) {
                mUnreadCountTotal += conversation.getUnreadCount();
            }
            updateTotalUnreadView();
            if (mAdapter != null)
                mAdapter.notifyDataSetChangedWrapper();
        }

        private void updateTotalUnreadView() {
            if (mUnreadCountTotal > 0) {
                ((MainActivity) mContext).getTvMessageCount().setText(mUnreadCountTotal + "");
                ((MainActivity) mContext).getTvMessageCount().setVisibility(View.VISIBLE);
                ((MainActivity) mContext).setToolbarTitle(UIUtils.getString(R.string.app_name) + "(" + mUnreadCountTotal + ")");
            } else {
                ((MainActivity) mContext).getTvMessageCount().setVisibility(View.GONE);
                ((MainActivity) mContext).setToolbarTitle(UIUtils.getString(R.string.app_name));
            }
        }
    */
    private void setAdapter() {
        if (mAdapter == null) {
            mAdapter = new LQRAdapterForRecyclerView<Topic>(mContext, mData, R.layout.item_recent_message) {
                @Override
                public void convert(LQRViewHolderForRecyclerView helper, Topic item1, int position) {
                    ComTopic<VCard> item = (ComTopic<VCard>) item1;
                    if (item.getTouched() != null) {
                        ((TextView) helper.getView(R.id.tvTime)).setText(TimeUtils.getMsgFormatTime(item.getTouched().getTime(), false));
                    } else {
                        ((TextView) helper.getView(R.id.tvTime)).setText("");
                    }

                    if(item1.getLastMsg() == null) {
                        ((TextView) helper.getView(R.id.tvContent)).setText("");
                    } else if (item1.getLastMsg() != null) {
                        if(item1.getUserName()!=null){
                            ((TextView) helper.getView(R.id.tvContent)).setText(item1.getUserName() + "：" + item1.getLastMsg());
                        }else{
                            Topic fromUser=Cache.getTinode().getTopic(item1.getLastAct());
                            if(fromUser!=null){
                                VCard pub = (VCard) fromUser.getPub();
                                if(pub!=null){
                                    item1.setUserName(pub.fn);
                                } else {
                                    item1.setUserName(null);
                                }
                                ((TextView) helper.getView(R.id.tvContent)).setText(item1.getUserName() == null? item1.getLastAct():pub.fn + "：" + item1.getLastMsg());
                            } else {

                            }
                        }
                    } else {
                        ((TextView) helper.getView(R.id.tvContent)).setText(item1.getLastAct()+ "：" + item1.getLastMsg());
                    }

                    if (item.getTopicType() == Topic.TopicType.P2P) {
                        helper.getView(R.id.tvGroup).setVisibility(View.GONE);
                        ImageView ivHeader = helper.getView(R.id.ivHeader);

                        final Topic userInfo = mData.get(position);
                        VCard pub = (VCard) userInfo.getPub();
                        String topic = userInfo.getName();
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
                    } else if (item.getTopicType() == Topic.TopicType.GRP) {
                        helper.getView(R.id.tvGroup).setVisibility(View.VISIBLE);
                        //						Groups groups = DBManager.getInstance().getGroupsById(item.getTargetId());
                        //						//九宫格头像
                        //						LQRNineGridImageView ngiv = helper.getView(R.id.ngiv);
                        //						ngiv.setAdapter(mNgivAdapter);
                        //						ngiv.setImagesData(DBManager.getInstance().getGroupMembers(item.getTargetId()));
                        //						//群昵称
                        //						helper.setText(R.id.tvDisplayName, groups == null ? "" : groups.getName())
                        //								.setText(R.id.tvTime, TimeUtils.getMsgFormatTime(item.getReceivedTime()))
                        //								.setViewVisibility(R.id.ngiv, View.VISIBLE)
                        //								.setViewVisibility(R.id.ivHeader, View.GONE);

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

                            helper.setText(R.id.tvDisplayName, pub != null ? pub.fn : "未知编组")
                                    //									.setText(R.id.tvTime, TimeUtils.getMsgFormatTime(item.getReceivedTime()))
                                    .setViewVisibility(R.id.ngiv, View.GONE)
                                    .setViewVisibility(R.id.ivHeader, View.VISIBLE);
                        }
                    }

                    //                    helper.setBackgroundColor(R.id.flRoot, item.isTop() ? UIUtils.getColor(R.color.gray7) : UIUtils.getColor(android.R.color.white))
                    helper.setBackgroundColor(R.id.flRoot, R.color.color_122131)
                            .setText(R.id.tvCount, item.getUnreadCount() > 99 ? "99" : item.getUnreadCount() + "")
                            .setViewVisibility(R.id.tvCount, item.getUnreadCount() > 0 ? View.VISIBLE : View.GONE);
                    if (!TextUtils.isEmpty(item.getDraft())) {
                        //                        MoonUtils.identifyFaceExpression(mContext, tvContent, item.getDraft(), ImageSpan.ALIGN_BOTTOM);
                        helper.setViewVisibility(R.id.tvDraft, View.VISIBLE);
                        return;
                    } else {
                        helper.setViewVisibility(R.id.tvDraft, View.GONE);
                    }
                    //                    tvContent.setText(item.getComment());
                }
            };
            mAdapter.setOnItemClickListener((helper, parent, itemView, position) -> {
                ((ZHMainActivity) mContext).detachedTopic();

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
            mAdapter.setOnItemLongClickListener((helper, parent, itemView, position) -> {
                Topic item = mData.get(position);
                View conversationMenuView = View.inflate(mContext, R.layout.dialog_conversation_menu, null);
                mConversationMenuDialog = new CustomDialog(mContext, conversationMenuView, R.style.MyDialog);
                TextView tvSetConversationToTop = (TextView) conversationMenuView.findViewById(R.id.tvSetConversationToTop);
                tvSetConversationToTop.setText(item.isTop() ? UIUtils.getString(R.string.cancel_conversation_to_top) : UIUtils.getString(R.string.set_conversation_to_top));
                conversationMenuView.findViewById(R.id.tvSetConversationToTop).setOnClickListener(v -> {
                            updateTop(item, item.isTop() ? Topic.FALSE : Topic.TRUE);
                            mConversationMenuDialog.dismiss();
                            mConversationMenuDialog = null;
                    /*
                            if (!item.isAttached()) {
                                try {
                                    item.subscribe(null,
                                            item.getMetaGetBuilder()
                                                    .withGetSub()
                                                    .withGetData()
                                                    .build()).thenApply(new PromisedReply.SuccessListener<ServerMessage>() {
                                        @Override
                                        public PromisedReply<ServerMessage> onSuccess(ServerMessage result) throws Exception {
                                            updateTop(item, item.isTop() ? Topic.FALSE : Topic.TRUE);
                                            return null;
                                        }
                                    }, null);
                                } catch (NotConnectedException ignored) {
                                    Log.d("", "Offline mode, ignore");
                                } catch (Exception ex) {
                                    Log.e("", "something went wrong", ex);
                                } finally {
                                    mConversationMenuDialog.dismiss();
                                    mConversationMenuDialog = null;
                                }
                            } else {
                                updateTop(item, item.isTop() ? Topic.FALSE : Topic.TRUE);
                            }*/
                        }
                );
                conversationMenuView.findViewById(R.id.tvDeleteConversation).setOnClickListener(v -> {
                    //删除聊天。topic表置位。当来到新消息时或在通讯录里点击时将topic恢复。
                    //先取消置顶
                    updateTop(item,Topic.FALSE);
                    boolean isSuccess = Cache.getTinode().getTopic(item.getName()).deleteLocal();

                    if (isSuccess) {
                        Cache.getTinode().delTopicInMemory(item.getName());
                        loadData();
                        mAdapter.notifyDataSetChangedWrapper();
                    } else {
                        Toast.makeText(mContext, "操作失败", Toast.LENGTH_SHORT).show();
                    }

                    mConversationMenuDialog.dismiss();
                    mConversationMenuDialog = null;
                });
                mConversationMenuDialog.show();
                return true;
            });
            getView().getRvRecentMessage().setAdapter(mAdapter);
        } else {
            mAdapter.notifyDataSetChangedWrapper();
        }
    }

    private void updateTop(Topic mTopic, int top) {
        try {
            mTopic.setIsTop(top);
            mTopic.updateState();

            mContext.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadData();
                    mAdapter.notifyDataSetChangedWrapper();
//                            try {
//                                mTopic.leave();
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
                }
            });

            PrivateType privateType = ((PrivateType) mTopic.getPriv());
            if (privateType == null) {
                privateType = new PrivateType();
            }
            privateType.setTop(top);
            MetaSetDesc des = new MetaSetDesc();
            des.defacs = null;
            des.pub = null;
            des.priv = privateType;

            mTopic.setDescription(des);
        } catch (NotConnectedException ignored) {
            Toast.makeText(mContext, R.string.no_connection, Toast.LENGTH_SHORT).show();
        } catch (Exception ignored) {
            Toast.makeText(mContext, R.string.action_failed, Toast.LENGTH_SHORT).show();
        }
    }
}

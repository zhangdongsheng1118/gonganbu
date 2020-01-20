package com.yixin.tinode.ui.presenter;

import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.blankj.utilcode.util.ToastUtils;
import com.google.gson.Gson;
import com.lqr.adapter.LQRAdapterForRecyclerView;
import com.lqr.adapter.LQRViewHolderForRecyclerView;
import com.yixin.tinode.R;
import com.yixin.tinode.app.AppConst;
import com.yixin.tinode.db.model.GroupMember;
import com.yixin.tinode.db.tinode.BaseDb;
import com.yixin.tinode.db.tinode.MessageDb;
import com.yixin.tinode.db.tinode.StoredTopic;
import com.yixin.tinode.manager.BroadcastManager;
import com.yixin.tinode.tinode.Cache;
import com.yixin.tinode.tinode.util.UiUtils;
import com.yixin.tinode.ui.activity.CreateGroupActivity;
import com.yixin.tinode.ui.activity.RemoveGroupMemberActivity;
import com.yixin.tinode.ui.activity.SessionInfoActivity;
import com.yixin.tinode.ui.activity.UserInfoActivity;
import com.yixin.tinode.ui.base.BaseActivity;
import com.yixin.tinode.ui.base.BasePresenter;
import com.yixin.tinode.ui.view.ISessionInfoAtView;
import com.yixin.tinode.util.LogUtils;
import com.yixin.tinode.util.PinyinUtils;
import com.yixin.tinode.util.UIUtils;
import com.yixin.tinode.widget.CustomDialog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import co.tinode.tinodesdk.MeTopic;
import co.tinode.tinodesdk.NotConnectedException;
import co.tinode.tinodesdk.PromisedReply;
import co.tinode.tinodesdk.Topic;
import co.tinode.tinodesdk.model.AcsHelper;
import co.tinode.tinodesdk.model.Drafty;
import co.tinode.tinodesdk.model.ServerMessage;
import co.tinode.tinodesdk.model.Subscription;
import co.tinode.tinodesdk.model.VCard;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.yixin.tinode.ui.activity.SessionActivity.SESSION_TYPE_GROUP;
import static com.yixin.tinode.ui.activity.SessionActivity.SESSION_TYPE_PRIVATE;
import static com.yixin.tinode.util.Utils.antiShakeUtil;

/**
 * 添加、删除群成员；退群，解散群；群成员信息
 *****/
public class SessionInfoAtPresenter extends BasePresenter<ISessionInfoAtView> {
    private String mSessionId;
    private List<GroupMember> mData = new ArrayList<>();
    private LQRAdapterForRecyclerView<GroupMember> mAdapter;
    private boolean mIsManager = false;
    public boolean mIsCreateNewGroup = false;
    public String mDisplayName = "";
    private CustomDialog mSetDisplayNameDialog;
    private int mSessionType;
    private Topic topic;

    public SessionInfoAtPresenter(BaseActivity context, String sessionId, int conversationType) {
        super(context);
        mSessionId = sessionId;
        mSessionType = conversationType;
    }

    public void loadMembers() {
        loadData();
        setAdapter();
    }

    private void loadData() {
        if (mSessionType == SESSION_TYPE_PRIVATE) {
            topic = Cache.getTinode().getTopic(mSessionId);
            if (topic != null) {
                mData.clear();

                VCard userInfo = (VCard) topic.getPub();
                GroupMember newMember = new GroupMember(mSessionId,
                        mSessionId,
                        userInfo.fn,
                        userInfo.photo,
                        userInfo.fn,
                        PinyinUtils.getPinyin(userInfo.fn),
                        PinyinUtils.getPinyin(userInfo.fn),
                        "",
                        "",
                        "");
                mData.add(newMember);
                //mData.add(new GroupMember("", "", ""));//+
            }
            mIsCreateNewGroup = true;
        } else {
            topic = Cache.getTinode().getTopic(mSessionId);
            Collection<Subscription> subscriptions = topic.getSubscriptions();
            if (subscriptions == null) {
                //暂时容错
                return;
            }

            List<GroupMember> groupMembers = new ArrayList<>();
            groupMembers.add(null);
            for (Subscription sub : subscriptions) {
                VCard userInfo = (VCard) sub.pub;
                if (userInfo != null) {
                    GroupMember newMember = new GroupMember(mSessionId,
                            sub.user,
                            userInfo.fn,
                            userInfo.photo,
                            userInfo.fn,
                            PinyinUtils.getPinyin(userInfo.fn),
                            PinyinUtils.getPinyin(userInfo.fn),
                            "",
                            "",
                            "");
                    if (sub.acs.isOwner()) {
                        groupMembers.set(0, newMember);
                    } else {
                        groupMembers.add(newMember);
                    }
                }
            }

            if (groupMembers != null && groupMembers.size() > 0) {
                mIsManager = topic.isOwner();

                mData.clear();
                mData.addAll(groupMembers);
               // mData.add(new GroupMember("", "", ""));//+
//                if (mIsManager) {
//                    mData.add(new GroupMember("", "", ""));//-
//                }
            }
            mIsCreateNewGroup = false;
        }
        for(int i=0;i<mData.size();i++){
            Log.d("SessionInfoAtPresenter",new Gson().toJson(mData.get(i)));
        }
        setAdapter();
    }

    private void setAdapter() {
        if (mAdapter == null) {
            mAdapter = new LQRAdapterForRecyclerView<GroupMember>(mContext, mData, R.layout.item_member_info) {
                @Override
                public void convert(LQRViewHolderForRecyclerView helper, GroupMember item, int position) {
                    ImageView ivHeader = helper.getView(R.id.ivHeader);
//                    if (mIsManager && position >= mData.size() - 2) {//+和-
//                        if (position == mData.size() - 2) {//+
//                            ivHeader.setImageResource(R.mipmap.ic_add_team_member);
//                        } else {//-
//                            ivHeader.setImageResource(R.mipmap.ic_remove_team_member);
//                        }
//                        helper.setText(R.id.tvName, "");
//                    } else if (!mIsManager && position >= mData.size() - 1) {//+
//                        ivHeader.setImageResource(R.mipmap.ic_add_team_member);
//                        helper.setText(R.id.tvName, "");
//                    } else {
//                        Glide.with(mContext).load(item.getPortraitUri()).centerCrop().into(ivHeader);
                        UiUtils.assignBitmap(mContext, ivHeader,
                                item != null && item.getPhoto() != null ? item.getPhoto().getBitmap() : null,
                                item != null ? item.getDisplayName() : null,
                                item != null ? item.getUserId() : null);
                        if(item != null){
                            helper.setText(R.id.tvName, item.getName());
//                        }

                    }
                }
            };
            mAdapter.setOnItemClickListener((helper, parent, itemView, position) -> {
//                if (mIsManager && position >= mData.size() - 2) {//+和-
//                    if (position == mData.size() - 2) {//+
//                        addMember(mSessionType == SESSION_TYPE_GROUP);
//                    } else {//-
//                        removeMember();
//                    }
//                } else if (!mIsManager && position >= mData.size() - 1) {//+
//                    addMember(mSessionType == SESSION_TYPE_GROUP);
//                } else {
                if (antiShakeUtil.check()) return;
                seeUserInfo(mData.get(position));
//                }
            });
            getView().getRvMember().setAdapter(mAdapter);
        } else {
            mAdapter.notifyDataSetChangedWrapper();
        }
    }

    private void addMember(boolean isAddMember) {
        Intent intent = new Intent(mContext, CreateGroupActivity.class);

        //如果是群组的话就把当前已经的群成员发过去
        if (isAddMember) {
            ArrayList<String> selectedTeamMemberAccounts = new ArrayList<>();
            for (int i = 0; i < mData.size(); i++) {
                selectedTeamMemberAccounts.add(mData.get(i).getUserId());
            }
            intent.putExtra("selectedMember", selectedTeamMemberAccounts);
        }

        mContext.startActivityForResult(intent, SessionInfoActivity.REQ_ADD_MEMBERS);
    }

    private void removeMember() {
        Intent intent = new Intent(mContext, RemoveGroupMemberActivity.class);
        intent.putExtra("sessionId", mSessionId);
        mContext.startActivityForResult(intent, SessionInfoActivity.REQ_REMOVE_MEMBERS);
    }

    private void seeUserInfo(GroupMember userInfo) {
        Intent intent = new Intent(mContext, UserInfoActivity.class);
        intent.putExtra("topic", userInfo.getUserId());
        mContext.jumpToActivity(intent);
    }

    private void addMember(Topic mTopic, ArrayList<String> selectedIds) {
        ArrayList<String> invitedList = new ArrayList<>();
        for (String groupMemberId : selectedIds) {
            try {
                mTopic.invite(groupMemberId, AcsHelper.STR_MODE_NORAML /* use default */).thenApply(new PromisedReply.SuccessListener<ServerMessage>() {
                    @Override
                    public PromisedReply onSuccess(ServerMessage result) throws Exception {
                        //此处可能存在异步写问题
                        if (result.ctrl.code != 200) {
                            mContext.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mContext.hideWaitingDialog();
                                    UIUtils.showToast("添加成员失败：" + result.ctrl.text);
                                }
                            });
                        }

                        try {
                            invitedList.add(result.ctrl.params.get("user").toString());
                        } catch (Exception e) {
                            LogUtils.e(e.getMessage());
                            invitedList.add("");
                        }


                        String meTopic = Cache.getTinode().getMyId();
                        String name = "";
                        Topic me = (MeTopic) Cache.getTinode().getMeTopic();
                        if (me != null) {
                            VCard data = (VCard) me.getPub();
                            name = data.fn;
                        }
                        String tgt = groupMemberId;
                        String tgtName = "";
                        Collection<Subscription> subscriptions = topic.getSubscriptions();
                        for (Subscription sub : subscriptions) {
                            if (sub.user.equals(groupMemberId)) {
                                tgtName = ((VCard) sub.pub).fn;
                                break;
                            }
                        }
                        sendMessage(Drafty.parse("").insertAddMember(Drafty.MEMBER_OPERATE_TYPE_ADD, meTopic, name, tgt, tgtName), null, null);

                        if (invitedList.size() >= selectedIds.size() - 1) {
                            mContext.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    LogUtils.sf("添加群成员结束");
                                    mContext.hideWaitingDialog();
                                    mTopic.clearSubs();
                                    loadData();
                                    LogUtils.sf("重新加载数据");
                                    UIUtils.showToast(UIUtils.getString(R.string.add_member_success));
                                }
                            });
                        }
                        return null;
                    }
                }, new PromisedReply.FailureListener() {
                    @Override
                    public PromisedReply onFailure(Exception err) throws Exception {
                        mContext.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                addMembersError(err);
                            }
                        });
                        return null;
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public void addGroupMember(ArrayList<String> selectedIds) {
        LogUtils.sf("addGroupMember : " + selectedIds);
        mContext.showWaitingDialog(UIUtils.getString(R.string.please_wait));

        Topic mTopic = Cache.getTinode().getTopic(mSessionId);
        try {
            if (!mTopic.isAttached()) {
                mTopic.subscribe().thenApply(new PromisedReply.SuccessListener<ServerMessage>() {
                    @Override
                    public PromisedReply<ServerMessage> onSuccess(ServerMessage result) throws Exception {
                        addMember(mTopic, selectedIds);
                        return null;
                    }
                }, new PromisedReply.FailureListener<ServerMessage>() {
                    @Override
                    public PromisedReply<ServerMessage> onFailure(final Exception err) {
                        mContext.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (err instanceof NotConnectedException) {
                                    Toast.makeText(mContext, R.string.no_connection, Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(mContext, R.string.action_failed, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        return null;
                    }
                });
            } else {
                addMember(mTopic, selectedIds);
            }
        } catch (NotConnectedException ignored) {
            Toast.makeText(mContext, R.string.no_connection, Toast.LENGTH_SHORT).show();
            // Go back to contacts
        } catch (Exception e) {
            Toast.makeText(mContext, "添加失败", Toast.LENGTH_SHORT).show();
        }


    }

    private void deleteMembers(Topic mTopic, ArrayList<String> toDel, ArrayList<String> selectedIds) {
        ArrayList<String> invitedList = new ArrayList<>();
        for (String groupMemberId : toDel) {
            try {
                mTopic.eject(groupMemberId, false/* use default */).thenApply(new PromisedReply.SuccessListener<ServerMessage>() {
                    @Override
                    public PromisedReply onSuccess(ServerMessage result) throws Exception {
                        //此处可能存在异步写问题
                        if (result.ctrl.code != 200) {
                            mContext.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mContext.hideWaitingDialog();
                                    UIUtils.showToast("删除成员失败：" + result.ctrl.text);
                                }
                            });
                        }

                        try {
                            invitedList.add(result.ctrl.params.get("user").toString());
                        } catch (Exception e) {
                            LogUtils.e(e.getMessage());
                            invitedList.add("");
                        }

                        String meTopic = Cache.getTinode().getMyId();
                        String name = "";
                        Topic me = (MeTopic) Cache.getTinode().getMeTopic();
                        if (me != null) {
                            VCard data = (VCard) me.getPub();
                            name = data.fn;
                        }
                        String tgt = groupMemberId;
                        String tgtName = "";
                        for (GroupMember tmp : mData) {
                            if (tmp.getUserId().equals(groupMemberId)) {
                                tgtName = tmp.getName();
                                break;
                            }
                        }
                        sendMessage(Drafty.parse("").insertAddMember(Drafty.MEMBER_OPERATE_TYPE_REMOVE, meTopic, name, tgt, tgtName), null, null);

                        if (invitedList.size() >= selectedIds.size() - 1) {
                            mContext.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    LogUtils.sf("删除结束");
                                    mContext.hideWaitingDialog();
                                    mTopic.clearSubs();
                                    loadData();
                                    UIUtils.showToast(UIUtils.getString(R.string.del_member_success));
                                }
                            });
                        }
                        return null;
                    }
                }, new PromisedReply.FailureListener() {
                    @Override
                    public PromisedReply onFailure(Exception err) throws Exception {
                        mContext.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                delMembersError(err);
                            }
                        });
                        return null;
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void deleteGroupMembers(ArrayList<String> selectedIds) {
        mContext.showWaitingDialog(UIUtils.getString(R.string.please_wait));

        ArrayList<String> toDel = new ArrayList<>();
        for (int i = 0; i < mData.size(); i++) {
            GroupMember member = mData.get(i);
            if (selectedIds.contains(member.getUserId())) {
                LogUtils.sf("删除用户：" + member.getUserId());
                toDel.add(member.getUserId());
            }
        }

        Topic mTopic = Cache.getTinode().getTopic(mSessionId);
        try {
            if (!mTopic.isAttached()) {
                mTopic.subscribe().thenApply(new PromisedReply.SuccessListener<ServerMessage>() {
                    @Override
                    public PromisedReply<ServerMessage> onSuccess(ServerMessage result) throws Exception {
                        deleteMembers(mTopic, toDel, selectedIds);
                        return null;
                    }
                }, new PromisedReply.FailureListener<ServerMessage>() {
                    @Override
                    public PromisedReply<ServerMessage> onFailure(final Exception err) {
                        mContext.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (err instanceof NotConnectedException) {
                                    Toast.makeText(mContext, R.string.no_connection, Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(mContext, R.string.action_failed, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        return null;
                    }
                });
            } else {
                deleteMembers(mTopic, toDel, selectedIds);
            }
        } catch (NotConnectedException ignored) {
            Toast.makeText(mContext, R.string.no_connection, Toast.LENGTH_SHORT).show();
            // Go back to contacts
        } catch (Exception e) {
            Toast.makeText(mContext, "删除失败", Toast.LENGTH_SHORT).show();
        }

    }

    private void addMembersError(Throwable throwable) {
        LogUtils.sf(throwable.getLocalizedMessage());
        mContext.hideWaitingDialog();
        UIUtils.showToast(UIUtils.getString(R.string.add_member_fail));
    }

    private void delMembersError(Throwable throwable) {
        LogUtils.sf(throwable.getLocalizedMessage());
        mContext.hideWaitingDialog();
        UIUtils.showToast(UIUtils.getString(R.string.del_member_fail));
    }

    public void loadOtherInfo(int sessionType, String sessionId) {
        setToTop();
        switch (sessionType) {
            case SESSION_TYPE_PRIVATE:
                getView().getBtnQuit().setText(UIUtils.getString(R.string.delete_and_exit));
                break;
            case SESSION_TYPE_GROUP:
                //设置群信息
                Topic topic = Cache.getTinode().getTopic(sessionId);
                VCard groups = (VCard) topic.getPub();
                if (groups != null) {
                    getView().getOivGroupName().setRightText(groups.fn);
                    mDisplayName = groups.fn;
                    getView().getOivNickNameInGroup().setRightText(mDisplayName);
                    getView().getBtnQuit().setText(topic.isOwner() ? UIUtils.getString(R.string.dismiss_this_group) :
                            UIUtils.getString(R.string.delete_and_exit));
                }
//                Observable.just(DBManager.getInstance().getGroupsById(sessionId))
//                        .subscribeOn(Schedulers.io())
//                        .observeOn(AndroidSchedulers.mainThread())
//                        .subscribe(groups -> {
//                            if (groups == null)
//                                return;
//                            mGroups = groups;
//                            //设置群信息
//                            getView().getOivGroupName().setRightText(groups.getName());
//                            mDisplayName = TextUtils.isEmpty(groups.getDisplayName()) ?
//                                    DBManager.getInstance().getUserInfo(UserCache.getId()).getName() :
//                                    groups.getDisplayName();
//                            getView().getOivNickNameInGroup().setRightText(mDisplayName);
//                            getView().getBtnQuit().setText(groups.getRole().equals("0") ? UIUtils.getString(R.string.dismiss_this_group) :
//                                    UIUtils.getString(R.string.delete_and_exit));
//                        }, this::loadOtherError);
                break;
        }
    }

    private void setToTop() {
//        Observable.just(RongIMClient.getInstance().getConversation(mConversationType, mSessionId))
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(conversation -> {
//                    if (conversation != null) {
//                        getView().getSbToTop().setChecked(conversation.isTop());
//                    }
//                });
    }

    private void loadOtherError(Throwable throwable) {
        LogUtils.sf(throwable.getLocalizedMessage());
    }


    public void quit() {
        String tip = "";
        Topic topic = Cache.getTinode().getTopic(mSessionId);

        if (mSessionType == SESSION_TYPE_PRIVATE) {
            tip = "你确定删除该好友？";

            mContext.showMaterialDialog(null, tip, UIUtils.getString(R.string.sure), UIUtils.getString(R.string.cancel)
                    , v -> {

                        try {
                            topic.leave(true).thenApply(new PromisedReply.SuccessListener<ServerMessage>() {
                                @Override
                                public PromisedReply onSuccess(ServerMessage result) throws Exception {
                                    mContext.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            mContext.hideMaterialDialog();
                                            if (result.ctrl.code != 200) {
                                                UIUtils.showToast("删除失败：" + result.ctrl.text);
                                            }

                                            BroadcastManager.getInstance(mContext).sendBroadcast(AppConst.UPDATE_CONVERSATIONS);
                                            BroadcastManager.getInstance(mContext).sendBroadcast(AppConst.UPDATE_GROUP);
                                            BroadcastManager.getInstance(mContext).sendBroadcast(AppConst.CLOSE_CURRENT_SESSION);
                                            mContext.finish();
                                        }
                                    });
                                    return null;
                                }
                            }, new PromisedReply.FailureListener() {
                                @Override
                                public PromisedReply onFailure(Exception err) throws Exception {
                                    mContext.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            quitError(err);
                                        }
                                    });
                                    return null;
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                            UIUtils.showToast(UIUtils.getString(R.string.exit_group_fail));
                        }
                    }
                    , v -> mContext.hideMaterialDialog());
        } else {
            if (topic.isOwner()) {
                tip = UIUtils.getString(R.string.are_you_sure_to_dismiss_this_group);
                //del topic
//            quitGroupResponseObservable = ApiRetrofit.getInstance().dissmissGroup(mSessionId);
            } else {
                tip = UIUtils.getString(R.string.you_will_never_receive_any_msg_after_quit);
                //leave
//            quitGroupResponseObservable = ApiRetrofit.getInstance().quitGroup(mSessionId);
            }
            mContext.showMaterialDialog(null, tip, UIUtils.getString(R.string.sure), UIUtils.getString(R.string.cancel)
                    , v -> {
                        if (topic.isOwner()) {
                            try {
                                topic.delete().thenApply(new PromisedReply.SuccessListener<ServerMessage>() {
                                    @Override
                                    public PromisedReply onSuccess(ServerMessage result) throws Exception {
                                        mContext.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                mContext.hideMaterialDialog();
                                                if (result.ctrl.code != 200) {
                                                    UIUtils.showToast("解散失败：" + result.ctrl.text);
                                                }

                                                BroadcastManager.getInstance(mContext).sendBroadcast(AppConst.UPDATE_CONVERSATIONS);
                                                BroadcastManager.getInstance(mContext).sendBroadcast(AppConst.UPDATE_GROUP);
                                                BroadcastManager.getInstance(mContext).sendBroadcast(AppConst.CLOSE_CURRENT_SESSION);
                                                mContext.finish();
                                            }
                                        });
                                        return null;
                                    }
                                }, new PromisedReply.FailureListener() {
                                    @Override
                                    public PromisedReply onFailure(Exception err) throws Exception {
                                        mContext.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                quitError(err);
                                            }
                                        });
                                        return null;
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                                UIUtils.showToast(UIUtils.getString(R.string.exit_group_fail));
                            }
                        } else {
                            try {
                                topic.leave(true).thenApply(new PromisedReply.SuccessListener<ServerMessage>() {
                                    @Override
                                    public PromisedReply onSuccess(ServerMessage result) throws Exception {
                                        mContext.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                mContext.hideMaterialDialog();
                                                if (result.ctrl.code != 200) {
                                                    UIUtils.showToast("退出群失败：" + result.ctrl.text);
                                                }

                                                String meTopic = Cache.getTinode().getMyId();
                                                String name = "";
                                                Topic me = (MeTopic) Cache.getTinode().getMeTopic();
                                                if (me != null) {
                                                    VCard data = (VCard) me.getPub();
                                                    name = data.fn;
                                                }
                                                sendMessage(Drafty.parse("").insertAddMember(Drafty.MEMBER_OPERATE_TYPE_REMOVE, meTopic, name, meTopic, name), null, null);

                                                BroadcastManager.getInstance(mContext).sendBroadcast(AppConst.UPDATE_CONVERSATIONS);
                                                BroadcastManager.getInstance(mContext).sendBroadcast(AppConst.UPDATE_GROUP);
                                                BroadcastManager.getInstance(mContext).sendBroadcast(AppConst.CLOSE_CURRENT_SESSION);
                                                mContext.finish();
                                            }
                                        });
                                        return null;
                                    }
                                }, new PromisedReply.FailureListener()

                                {
                                    @Override
                                    public PromisedReply onFailure(Exception err) throws
                                            Exception {
                                        mContext.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                quitError(err);
                                            }
                                        });
                                        return null;
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                                UIUtils.showToast(UIUtils.getString(R.string.exit_group_fail));
                            }
                        }
                    }
                    , v -> mContext.hideMaterialDialog());
        }
    }


    private boolean sendMessage(Drafty content, PromisedReply.SuccessListener<ServerMessage> succCb, PromisedReply.FailureListener<ServerMessage> failCb) {
        if (topic != null) {
            try {
                PromisedReply<ServerMessage> reply = topic.publish(content);
                reply.thenApply(new PromisedReply.SuccessListener<ServerMessage>() {
                    @Override
                    public PromisedReply<ServerMessage> onSuccess(ServerMessage result) throws Exception {
                        // Updates message list with "delivered" icon.
                        if (succCb != null) {
                            succCb.onSuccess(result);
                        }
                        return null;
                    }
                }, null);
            } catch (NotConnectedException ignored) {
                Log.d("", "sendMessage -- NotConnectedException", ignored);
            } catch (Exception ignored) {
                Log.d("", "sendMessage -- Exception", ignored);
//                Toast.makeText(mContext, R.string.failed_to_send_message, Toast.LENGTH_SHORT).show();
                return false;
            }
            return true;
        }
        return false;
    }

    private void quitError(Throwable throwable) {
        LogUtils.sf(throwable.getLocalizedMessage());
        mContext.hideMaterialDialog();
        UIUtils.showToast(UIUtils.getString(R.string.exit_group_fail));
    }

    public void clearConversationMsg() {
        mContext.showMaterialDialog(null, UIUtils.getString(R.string.are_you_sure_to_clear_msg_record), UIUtils.getString(R.string.clear), UIUtils.getString(R.string.cancel)
                , v1 -> {
//                    RongIMClient.getInstance().clearMessages(mConversationType, mSessionId, new RongIMClient.ResultCallback<Boolean>() {
//                        @Override
//                        public void onSuccess(Boolean aBoolean) {
//                            mContext.hideMaterialDialog();
//                            BroadcastManager.getInstance(mContext).sendBroadcast(AppConst.UPDATE_CONVERSATIONS);
//                            BroadcastManager.getInstance(mContext).sendBroadcast(AppConst.REFRESH_CURRENT_SESSION);
//                        }
//
//                        @Override
//                        public void onError(RongIMClient.ErrorCode errorCode) {
//                            mContext.hideMaterialDialog();
//                        }
//                    });

                    //首页的最新消息是通过查询获得的，并不是保存到数据库里的。
                    Observable.just(MessageDb.delete(BaseDb.getInstance().getWritableDatabase(), ((StoredTopic) topic.getLocal()).id, -1, 0, -1, false))
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(success -> {
                                ToastUtils.showShort("清空聊天记录成功");
                                mContext.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mContext.hideMaterialDialog();
//                                        BroadcastManager.getInstance(mContext).sendBroadcast(AppConst.UPDATE_CONVERSATIONS);
                                        topic.setLastMsg(null);
                                        //topic.setLastTs(null);
                                        Cache.getTinode().changeTopicName(topic, topic.getName());
                                    }
                                });
                            }, throwable -> {
                                ToastUtils.showShort("清空聊天记录失败");
                                mContext.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mContext.hideMaterialDialog();
                                    }
                                });
                            });
                }, v2 -> mContext.hideMaterialDialog());
    }

    public void setDisplayName() {
        View view = View.inflate(mContext, R.layout.dialog_group_display_name_change, null);
        mSetDisplayNameDialog = new CustomDialog(mContext, view, R.style.MyDialog);
        EditText etName = (EditText) view.findViewById(R.id.etName);
        etName.setText(mDisplayName);
        etName.setSelection(mDisplayName.length());
        view.findViewById(R.id.tvCancle).setOnClickListener(v -> mSetDisplayNameDialog.dismiss());
        view.findViewById(R.id.tvOk).setOnClickListener(v -> {
            mContext.hideWaitingDialog();
            String displayName = etName.getText().toString().trim();
            if (!TextUtils.isEmpty(displayName)) {
//                ApiRetrofit.getInstance().setGroupDisplayName(mSessionId, displayName)
//                        .subscribeOn(Schedulers.io())
//                        .observeOn(AndroidSchedulers.mainThread())
//                        .subscribe(setGroupDisplayNameResponse -> {
//                            if (setGroupDisplayNameResponse != null && setGroupDisplayNameResponse.getCode() == 200) {
//                                Groups groups = DBManager.getInstance().getGroupsById(mSessionId);
//                                if (groups != null) {
//                                    groups.setDisplayName(displayName);
//                                    groups.saveOrUpdate("groupid=?", groups.getGroupId());
//                                    mDisplayName = displayName;
//                                    getView().getOivNickNameInGroup().setRightText(mDisplayName);
//                                }
//                                UIUtils.showToast(UIUtils.getString(R.string.change_success));
//                            } else {
//                                UIUtils.showToast(UIUtils.getString(R.string.change_fail));
//                            }
//                            mSetDisplayNameDialog.dismiss();
//                        }, this::setDisplayNameError);
            }
        });
        mSetDisplayNameDialog.show();
    }


    private void setDisplayNameError(Throwable throwable) {
        LogUtils.sf(throwable.getLocalizedMessage());
        UIUtils.showToast(UIUtils.getString(R.string.change_fail));
        mSetDisplayNameDialog.dismiss();
    }
}

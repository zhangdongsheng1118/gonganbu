package com.yixin.tinode.ui.presenter;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Toast;

import com.github.promeg.pinyinhelper.Pinyin;
import com.lqr.adapter.LQRAdapterForRecyclerView;
import com.lqr.adapter.LQRHeaderAndFooterAdapter;
import com.lqr.adapter.LQRViewHolderForRecyclerView;
import com.yixin.tinode.R;
import com.yixin.tinode.db.model.Friend;
import com.yixin.tinode.tinode.Cache;

import co.tinode.tinodesdk.model.AcsHelper;

import com.yixin.tinode.tinode.util.UiUtils;
import com.yixin.tinode.ui.activity.CreateGroupActivity;
import com.yixin.tinode.ui.activity.SessionActivity;
import com.yixin.tinode.ui.base.BaseActivity;
import com.yixin.tinode.ui.base.BasePresenter;
import com.yixin.tinode.ui.view.ICreateGroupAtView;
import com.yixin.tinode.util.LogUtils;
import com.yixin.tinode.util.SortUtils;
import com.yixin.tinode.util.UIUtils;

import java.util.ArrayList;
import java.util.List;

import co.tinode.tinodesdk.ComTopic;
import co.tinode.tinodesdk.NotConnectedException;
import co.tinode.tinodesdk.PromisedReply;
import co.tinode.tinodesdk.Topic;
import co.tinode.tinodesdk.model.ServerMessage;
import co.tinode.tinodesdk.model.VCard;

//import com.yixin.tinode.api.ApiRetrofit;
//import com.yixin.tinode.db.DBManager;
//import com.yixin.tinode.model.cache.UserCache;


public class CreateGroupAtPresenter extends BasePresenter<ICreateGroupAtView> {

    private String mGroupName = "";
    private List<Friend> mData = new ArrayList<>();
    private List<Friend> mSelectedData = new ArrayList<>();
    private LQRHeaderAndFooterAdapter mAdapter;
    private LQRAdapterForRecyclerView<Friend> mSelectedAdapter;

    public CreateGroupAtPresenter(BaseActivity context) {
        super(context);
    }

    public void loadContacts() {
        loadData();
        setAdapter();
        setSelectedAdapter();
    }

    private void loadData() {
//        Observable.just(DBManager.getInstance().getFriends())
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(friends -> {
//                    if (friends != null && friends.size() > 0) {
//                        mData.clear();
//                        mData.addAll(friends);
//                        //整理排序
//                        SortUtils.sortContacts(mData);
//                        if (mAdapter != null)
//                            mAdapter.notifyDataSetChanged();
//                    }
//                }, this::loadError);


        List<Topic> topics = Cache.getTinode().getFilteredTopics(Topic.TopicType.P2P, null);

        if (topics != null && topics.size() > 0) {
            mData.clear();
            for (Topic tmp : topics) {
                VCard user = (VCard) tmp.getPub();
                Friend friend = new Friend(tmp.getName(), user.fn, user.photo);
                String spelling = Pinyin.toPinyin(friend.getDisplayName(), "");
                friend.setDisplayNameSpelling(spelling);
                friend.setNameSpelling(spelling);
                mData.add(friend);
            }
            //整理排序
            SortUtils.sortContacts(mData);
            if (mAdapter != null)
                mAdapter.notifyDataSetChanged();
        }
    }

    private void setAdapter() {
        if (mAdapter == null) {
            LQRAdapterForRecyclerView adapter = new LQRAdapterForRecyclerView<Friend>(mContext, mData, R.layout.item_contact) {
                @Override
                public void convert(LQRViewHolderForRecyclerView helper, Friend item, int position) {
                    helper.setText(R.id.tvName, item.getDisplayName()).setViewVisibility(R.id.cb, View.VISIBLE);
                    ImageView ivHeader = helper.getView(R.id.ivHeader);
//                    Glide.with(mContext).load(item.getPortraitUri()).centerCrop().into(ivHeader);
                    UiUtils.assignBitmap(mContext, ivHeader,
                            item != null && item.getPhoto() != null ? item.getPhoto().getBitmap() : null,
                            item != null ? item.getDisplayName() : null,
                            item.getUserId());

                    CheckBox cb = helper.getView(R.id.cb);
                    //如果添加群成员的话，需要判断是否已经在群中
                    if (((CreateGroupActivity) mContext).mSelectedTeamMemberAccounts != null &&
                            ((CreateGroupActivity) mContext).mSelectedTeamMemberAccounts.contains(item.getUserId())) {
                        cb.setChecked(true);
                        helper.setEnabled(R.id.cb, false).setEnabled(R.id.root, false);
                    } else {
                        helper.setEnabled(R.id.cb, true).setEnabled(R.id.root, true);
                        //没有在已有群中的联系人，根据当前的选中结果判断
                        cb.setChecked(mSelectedData.contains(item) ? true : false);
                    }

                    String str = "";
                    //得到当前字母
                    String currentLetter = item.getDisplayNameSpelling().charAt(0) + "";
                    if (position == 0) {
                        str = currentLetter;
                    } else {
                        //得到上一个字母
                        String preLetter = mData.get(position - 1).getDisplayNameSpelling().charAt(0) + "";
                        //如果和上一个字母的首字母不同则显示字母栏
                        if (!preLetter.equalsIgnoreCase(currentLetter)) {
                            str = currentLetter;
                        }

                        int nextIndex = position + 1;
                        if (nextIndex < mData.size() - 1) {
                            //得到下一个字母
                            String nextLetter = mData.get(nextIndex).getDisplayNameSpelling().charAt(0) + "";
                            //如果和下一个字母的首字母不同则隐藏下划线
                            if (!nextLetter.equalsIgnoreCase(currentLetter)) {
                                helper.setViewVisibility(R.id.vLine, View.VISIBLE);
                            } else {
                                helper.setViewVisibility(R.id.vLine, View.VISIBLE);
                            }
                        } else {
                            helper.setViewVisibility(R.id.vLine, View.INVISIBLE);
                        }
                    }
                    if (position == mData.size() - 1) {
                        helper.setViewVisibility(R.id.vLine, View.GONE);
                    }

                    //根据str是否为空决定字母栏是否显示
                    if (TextUtils.isEmpty(str)) {
                        helper.setViewVisibility(R.id.tvIndex, View.GONE);
                    } else {
                        helper.setViewVisibility(R.id.tvIndex, View.VISIBLE);
                        helper.setText(R.id.tvIndex, str);
                    }
                }
            };
//            adapter.addHeaderView(getView().getHeaderView());
            mAdapter = adapter.getHeaderAndFooterAdapter();
            getView().getRvContacts().setAdapter(mAdapter);

            ((LQRAdapterForRecyclerView) mAdapter.getInnerAdapter()).setOnItemClickListener((lqrViewHolder, viewGroup, view, i) -> {
                //选中或反选
                Friend friend = mData.get(i);
                if (mSelectedData.contains(friend)) {
                    mSelectedData.remove(friend);
                } else {
                    mSelectedData.add(friend);
                }
                mSelectedAdapter.notifyDataSetChangedWrapper();
                mAdapter.notifyDataSetChanged();
                if (mSelectedData.size() > 0) {
                    getView().getBtnToolbarSend().setEnabled(true);
                    getView().getBtnToolbarSend().setText(UIUtils.getString(R.string.sure_with_count, mSelectedData.size()));
                } else {
                    getView().getBtnToolbarSend().setEnabled(false);
                    getView().getBtnToolbarSend().setText(UIUtils.getString(R.string.sure));
                }
            });
        }
    }

    private void setSelectedAdapter() {
        if (mSelectedAdapter == null) {
            mSelectedAdapter = new LQRAdapterForRecyclerView<Friend>(mContext, mSelectedData, R.layout.item_selected_contact) {
                @Override
                public void convert(LQRViewHolderForRecyclerView helper, Friend item, int position) {
                    ImageView ivHeader = helper.getView(R.id.ivHeader);
//                    Glide.with(mContext).load(item.getPortraitUri()).centerCrop().into(ivHeader);
                    UiUtils.assignBitmap(mContext, ivHeader,
                            item != null && item.getPhoto() != null ? item.getPhoto().getBitmap() : null,
                            item != null ? item.getDisplayName() : null,
                            item.getUserId());
                }
            };
            getView().getRvSelectedContacts().setAdapter(mSelectedAdapter);
        }
    }

    public void addGroupMembers() {
        ArrayList<String> selectedIds = new ArrayList<>(mSelectedData.size());
        for (int i = 0; i < mSelectedData.size(); i++) {
            Friend friend = mSelectedData.get(i);
            selectedIds.add(friend.getUserId());
        }
        Intent data = new Intent();
        data.putStringArrayListExtra("selectedIds", selectedIds);
        mContext.setResult(Activity.RESULT_OK, data);
        mContext.finish();
    }

    public void createGroup() {
        Topic me = Cache.getTinode().getMeTopic();
        VCard user = (VCard) me.getPub();
        Friend my = new Friend(me.getName(), user.fn, user.photo);
        String spelling = Pinyin.toPinyin(my.getDisplayName(), "");
        my.setDisplayNameSpelling(spelling);
        my.setNameSpelling(spelling);
        mSelectedData.add(0, my);
        int size = mSelectedData.size();

        mGroupName = "";
        if (size > 3) {
            for (int i = 0; i < 3; i++) {
                Friend friend = mSelectedData.get(i);
                mGroupName += friend.getName() + "、";
            }
        } else {
            for (Friend friend : mSelectedData) {
                mGroupName += friend.getName() + "、";
            }
        }
        mGroupName = mGroupName.substring(0, mGroupName.length() - 1);

        mContext.showWaitingDialog(UIUtils.getString(R.string.please_wait));

        final ComTopic<VCard> topic = new ComTopic<VCard>(Cache.getTinode(), (Topic.Listener) null);
        topic.setPub(new VCard(mGroupName, user.getBitmap()));
        //topic.setLastTs();
       // topic.setPriv(subtitle);
        try {
            topic.subscribe().thenApply(new PromisedReply.SuccessListener<ServerMessage>() {
                @Override
                public PromisedReply<ServerMessage> onSuccess(ServerMessage result) throws Exception {
                    for (int i = 0; i < size; i++) {
                        Friend friend = mSelectedData.get(i);
                        topic.invite(friend.getUserId(), AcsHelper.STR_MODE_NORAML/* use default */);
                    }

                    Intent intent = new Intent(mContext, SessionActivity.class);
                    intent.putExtra("sessionId", topic.getName());
                    intent.putExtra("sessionType", SessionActivity.SESSION_TYPE_GROUP);
                    mContext.hideWaitingDialog();
                    mContext.jumpToActivity(intent);
                    mContext.finish();

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
        } catch (NotConnectedException ignored) {
            Toast.makeText(mContext, R.string.no_connection, Toast.LENGTH_SHORT).show();
            // Go back to contacts
        } catch (Exception e) {
            Toast.makeText(mContext, R.string.failed_to_create_topic, Toast.LENGTH_SHORT).show();
        }
    }

    private void loadError(Throwable throwable) {
        LogUtils.sf(throwable.getLocalizedMessage());
        mContext.hideWaitingDialog();
    }
}

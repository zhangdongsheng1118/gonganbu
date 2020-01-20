package com.yixin.tinode.ui.presenter;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Toast;

import com.blankj.utilcode.util.ToastUtils;
import com.github.promeg.pinyinhelper.Pinyin;
import com.lqr.adapter.LQRAdapterForRecyclerView;
import com.lqr.adapter.LQRHeaderAndFooterAdapter;
import com.lqr.adapter.LQRViewHolderForRecyclerView;
import com.yixin.tinode.R;
import com.yixin.tinode.db.model.Friend;
import com.yixin.tinode.tinode.Cache;
import com.yixin.tinode.tinode.TinodeUtil;
import com.yixin.tinode.tinode.util.UiUtils;
import com.yixin.tinode.ui.activity.CreateGroupActivity;
import com.yixin.tinode.ui.activity.SessionActivity;
import com.yixin.tinode.ui.base.BaseActivity;
import com.yixin.tinode.ui.base.BasePresenter;
import com.yixin.tinode.ui.view.ICreateGroupAtView;
import com.yixin.tinode.ui.view.IRelayAtView;
import com.yixin.tinode.util.LogUtils;
import com.yixin.tinode.util.SortUtils;
import com.yixin.tinode.util.UIUtils;

import java.util.ArrayList;
import java.util.List;

import co.tinode.tinodesdk.ComTopic;
import co.tinode.tinodesdk.NotConnectedException;
import co.tinode.tinodesdk.PromisedReply;
import co.tinode.tinodesdk.Topic;
import co.tinode.tinodesdk.model.Drafty;
import co.tinode.tinodesdk.model.ServerMessage;
import co.tinode.tinodesdk.model.VCard;

//import com.yixin.tinode.api.ApiRetrofit;
//import com.yixin.tinode.db.DBManager;
//import com.yixin.tinode.model.cache.UserCache;


public class RelayAtPresenter extends BasePresenter<IRelayAtView> {

    private String mGroupName = "";
    private List<Friend> mData = new ArrayList<>();
    private List<Friend> mSelectedData = new ArrayList<>();
    private LQRHeaderAndFooterAdapter mAdapter;
    private LQRAdapterForRecyclerView<Friend> mSelectedAdapter;
    private String key;

    public RelayAtPresenter(BaseActivity context) {
        super(context);
    }

    public void loadContacts() {
        loadData();
        setAdapter();
//        setSelectedAdapter();
    }

    private void loadData() {
        List<Topic> topics = TinodeUtil.getFilteredTopics(Topic.TopicType.USER, null, key);

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
                    helper.setText(R.id.tvName, item.getDisplayName());
//                    .setViewVisibility(R.id.cb, View.VISIBLE);
                    ImageView ivHeader = helper.getView(R.id.ivHeader);
//                    Glide.with(mContext).load(item.getPortraitUri()).centerCrop().into(ivHeader);
                    UiUtils.assignBitmap(mContext, ivHeader,
                            item != null && item.getPhoto() != null ? item.getPhoto().getBitmap() : null,
                            item != null ? item.getDisplayName() : null,
                            item.getUserId());

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
            adapter.addHeaderView(getView().getHeaderView());
            mAdapter = adapter.getHeaderAndFooterAdapter();
            getView().getRvContacts().setAdapter(mAdapter);

            ((LQRAdapterForRecyclerView) mAdapter.getInnerAdapter()).setOnItemClickListener((lqrViewHolder, viewGroup, view, i) -> {
                //发送消息并返回
                Friend friend = mData.get(i - 1);
                try {
                    Drafty content = getView().getDrafty();
                    if (content == null) {
                        mContext.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtils.showShort("未获取到消息");
                            }
                        });
                        return;
                    }

                    Topic topic = Cache.getTinode().getTopic(friend.getUserId());
                    if(!topic.isAttached()){
                        topic.subscribe(null,
                                topic.getMetaGetBuilder()
                                        .withGetDesc()
                                        .withGetSub()
                                        .withGetData()
                                        .withGetDel()
                                        .build()).thenApply(new PromisedReply.SuccessListener<ServerMessage>() {
                            @Override
                            public PromisedReply<ServerMessage> onSuccess(ServerMessage result) throws Exception {
                                publishMsg(topic, content);
                                return null;
                            }
                        }, new PromisedReply.FailureListener<ServerMessage>() {
                            @Override
                            public PromisedReply<ServerMessage> onFailure(Exception err) throws Exception {
                                mContext.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ToastUtils.showShort("连接失败，不能发送");
                                    }
                                });
                                return null;
                            }
                        });
                    }else{
                        publishMsg(topic, content);
                    }

                } catch (NotConnectedException ignored) {
                    mContext.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtils.showShort("已离线，发送失败");
                        }
                    });
                    Log.d("", "sendMessage -- NotConnectedException", ignored);
                } catch (Exception ignored) {
                    mContext.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtils.showShort("发送失败");
                        }
                    });
                    Log.d("", "sendMessage -- Exception", ignored);
//                Toast.makeText(mContext, R.string.failed_to_send_message, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void publishMsg(Topic topic, Drafty content) throws Exception {
        topic.publish(content).thenApply(new PromisedReply.SuccessListener<ServerMessage>() {
            @Override
            public PromisedReply<ServerMessage> onSuccess(ServerMessage result) throws Exception {
                // Updates message list with "delivered" icon.
                mContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtils.showShort("已发送");
                        mContext.setResult(Activity.RESULT_OK);
                        mContext.finish();
                    }
                });
                return null;
            }
        }, new PromisedReply.FailureListener<ServerMessage>() {
            @Override
            public PromisedReply<ServerMessage> onFailure(Exception err) throws Exception {
                mContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtils.showShort("发送失败");
                    }
                });
                return null;
            }
        });
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

    private void loadError(Throwable throwable) {
        LogUtils.sf(throwable.getLocalizedMessage());
        mContext.hideWaitingDialog();
    }
}

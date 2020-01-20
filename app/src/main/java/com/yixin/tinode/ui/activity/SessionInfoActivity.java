package com.yixin.tinode.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.kyleduo.switchbutton.SwitchButton;
import com.lqr.optionitemview.OptionItemView;
import com.lqr.recyclerview.LQRRecyclerView;
import com.yixin.tinode.R;
import com.yixin.tinode.app.AppConst;
import com.yixin.tinode.manager.BroadcastManager;
import com.yixin.tinode.tinode.Cache;
import com.yixin.tinode.ui.base.BaseActivity;
import com.yixin.tinode.ui.presenter.SessionInfoAtPresenter;
import com.yixin.tinode.ui.view.ISessionInfoAtView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;
import co.tinode.tinodesdk.NotConnectedException;
import co.tinode.tinodesdk.PromisedReply;
import co.tinode.tinodesdk.Tinode;
import co.tinode.tinodesdk.Topic;
import co.tinode.tinodesdk.model.MetaSetDesc;
import co.tinode.tinodesdk.model.PrivateType;
import co.tinode.tinodesdk.model.ServerMessage;

import static com.yixin.tinode.ui.activity.SessionActivity.SESSION_TYPE_GROUP;
import static com.yixin.tinode.ui.activity.SessionActivity.SESSION_TYPE_PRIVATE;
import static com.yixin.tinode.util.Utils.showNetworkAlert;

/**
 * 查找聊天记录需要修改messagedb，添加搜索字段
 *
 * @创建者 CSDN_LQR
 * @描述 会话信息界面
 */
public class SessionInfoActivity extends BaseActivity<ISessionInfoAtView, SessionInfoAtPresenter> implements ISessionInfoAtView {

    public static int REQ_ADD_MEMBERS = 1000;
    public static int REQ_REMOVE_MEMBERS = 1001;
    public static int REQ_SET_GROUP_NAME = 1002;

    private String mSessionId = "";
    private int mSessionType;

    @BindView(R.id.llGroupPart1)
    LinearLayout mLlGroupPart1;
    @BindView(R.id.llGroupPart2)
    LinearLayout mLlGroupPart2;

    @BindView(R.id.rvMember)
    LQRRecyclerView mRvMember;

    @BindView(R.id.oivGroupName)
    OptionItemView mOivGroupName;
    @BindView(R.id.oivQRCordCard)
    OptionItemView mOivQRCordCard;
    @BindView(R.id.oivNickNameInGroup)
    OptionItemView mOivNickNameInGroup;
    @BindView(R.id.oivClearMsgRecord)
    OptionItemView mOivClearMsgRecord;

    @BindView(R.id.sbToTop)
    SwitchButton mSbToTop;
    @BindView(R.id.btnQuit)
    Button mBtnQuit;

    private Topic mTopic = null;
    private String TAG = SessionActivity.class.getSimpleName();

    @Override
    public void init() {
        Intent intent = getIntent();
        mSessionId = intent.getStringExtra("sessionId");
        mSessionType = intent.getIntExtra("sessionType", SESSION_TYPE_PRIVATE);

        registerBR();
    }

    @Override
    public void initView() {
        switch (mSessionType) {
            case SESSION_TYPE_PRIVATE:
                mLlGroupPart1.setVisibility(View.GONE);
                mLlGroupPart2.setVisibility(View.GONE);
                mBtnQuit.setVisibility(View.INVISIBLE);
                break;
            case SESSION_TYPE_GROUP:
                mLlGroupPart1.setVisibility(View.VISIBLE);
//                mLlGroupPart2.setVisibility(View.VISIBLE);
                mBtnQuit.setVisibility(View.INVISIBLE);
                break;
        }
    }

    @Override
    public void initData() {
        mPresenter.loadMembers();
        mPresenter.loadOtherInfo(mSessionType, mSessionId);
    }

    @Override
    public void initListener() {
        mOivGroupName.setOnClickListener(v -> {
            Intent intent = new Intent(SessionInfoActivity.this, SetGroupNameActivity.class);
            intent.putExtra("groupId", mSessionId);
            startActivityForResult(intent, REQ_SET_GROUP_NAME);
        });
        mOivQRCordCard.setOnClickListener(v -> {
            Intent intent = new Intent(SessionInfoActivity.this, QRCodeCardActivity.class);
            intent.putExtra("groupId", mSessionId);
            jumpToActivity(intent);
        });
        mOivNickNameInGroup.setOnClickListener(v -> mPresenter.setDisplayName());
        mSbToTop.setOnCheckedChangeListener((buttonView, isChecked) -> {
//            RongIMClient.getInstance().setConversationToTop(mConversationType, mSessionId, isChecked);
            updateTop(mTopic, isChecked ? AppConst.TRUE : AppConst.FALSE);
        });
        mOivClearMsgRecord.setOnClickListener(v -> mPresenter.clearConversationMsg());
        mBtnQuit.setOnClickListener(v -> mPresenter.quit());
    }

    private void updateTop(Topic mTopic, int top) {
        try {
            mTopic.setIsTop(top);
            mTopic.updateState();

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
            Log.d("", "Offline mode, ignore");
        } catch (Exception ex) {
            Log.e("", "something went wrong", ex);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_ADD_MEMBERS) {
            if (resultCode == RESULT_OK) {
                ArrayList<String> selectedIds = data.getStringArrayListExtra("selectedIds");
                mPresenter.addGroupMember(selectedIds);
            }
        } else if (requestCode == REQ_REMOVE_MEMBERS) {
            if (resultCode == RESULT_OK) {
                ArrayList<String> selectedIds = data.getStringArrayListExtra("selectedIds");
                mPresenter.deleteGroupMembers(selectedIds);
            }
        } else if (requestCode == REQ_SET_GROUP_NAME) {
            if (resultCode == RESULT_OK) {
                String groupName = data.getStringExtra("group_name");
                mOivGroupName.setRightText(groupName);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unRegisterBR();
    }

    private void registerBR() {
        BroadcastManager.getInstance(this).register(AppConst.UPDATE_GROUP_MEMBER, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String groupId = intent.getStringExtra("String");
                if (mSessionId.equalsIgnoreCase(groupId)) {
                    mPresenter.loadMembers();
                }
            }
        });
    }

    @OnClick({R.id.oivSearch})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.oivSearch:
                Intent intent = new Intent(context, SearchMsgActivity.class);
                intent.putExtra("topic", mSessionId);
                startActivity(intent);
                break;
        }
    }

    private void unRegisterBR() {
        BroadcastManager.getInstance(this).unregister(AppConst.UPDATE_GROUP_MEMBER);
    }

    @Override
    protected SessionInfoAtPresenter createPresenter() {
        return new SessionInfoAtPresenter(this, mSessionId, mSessionType);
    }

    private void subscribe(Tinode tinode) {
        if (!mTopic.isAttached()) {
            try {
                mTopic.subscribe(null,
                        mTopic.getMetaGetBuilder()
                                .withGetDesc()
                                .withGetSub()
                                .build()).thenApply(new PromisedReply.SuccessListener<ServerMessage>() {
                    @Override
                    public PromisedReply<ServerMessage> onSuccess(ServerMessage result) throws Exception {

                        return null;
                    }
                }, null);
            } catch (NotConnectedException ignored) {
                if (!com.yixin.tinode.util.Utils.isNetworkConnected(this)) {
                    showNetworkAlert(context);
                } else {
                    Log.d(TAG, "Offline mode, ignore");
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            com.yixin.tinode.util.Utils.reconnect(context, tinode, new com.yixin.tinode.util.Utils.ReconnectListener() {
                                @Override
                                public void after() {
                                    subscribe(tinode);
                                }
                            });
                        }
                    }).start();
                }

            } catch (Exception ex) {
                Toast.makeText(this, R.string.action_failed, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "something went wrong", ex);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        final Tinode tinode = Cache.getTinode();
        mTopic = tinode.getTopic(mSessionId);
        if (mTopic != null) {
            subscribe(tinode);

            mSbToTop.setChecked(mTopic.isTop());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mTopic != null) {
            // Deactivate current topic
            if (mTopic.isAttached()) {
                try {
                    mTopic.leave();
                } catch (Exception ex) {
                    Log.e(TAG, "something went wrong in Topic.leave", ex);
                }
            }
        }
    }

    @Override
    protected int provideContentViewId() {
        return R.layout.activity_session_info;
    }

    @Override
    public LQRRecyclerView getRvMember() {
        return mRvMember;
    }

    @Override
    public OptionItemView getOivGroupName() {
        return mOivGroupName;
    }

    @Override
    public OptionItemView getOivNickNameInGroup() {
        return mOivNickNameInGroup;
    }

    @Override
    public SwitchButton getSbToTop() {
        return mSbToTop;
    }

    @Override
    public Button getBtnQuit() {
        return mBtnQuit;
    }

}

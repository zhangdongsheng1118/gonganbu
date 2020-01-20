package com.yixin.tinode.ui.activity;

import android.content.Intent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.yixin.tinode.R;
import com.yixin.tinode.app.AppConst;
import com.yixin.tinode.manager.BroadcastManager;
import com.yixin.tinode.tinode.Cache;
import com.yixin.tinode.tinode.util.UiUtils;
import com.yixin.tinode.ui.base.BaseActivity;
import com.yixin.tinode.ui.base.BasePresenter;
import com.yixin.tinode.util.LogUtils;
import com.yixin.tinode.util.UIUtils;

import butterknife.BindView;
import co.tinode.tinodesdk.NotConnectedException;
import co.tinode.tinodesdk.PromisedReply;
import co.tinode.tinodesdk.Topic;
import co.tinode.tinodesdk.model.ServerMessage;
import co.tinode.tinodesdk.model.VCard;

/**
 * @创建者 CSDN_LQR
 * @描述 设置群名称界面
 */
public class SetGroupNameActivity extends BaseActivity {

    private String mGroupId;

    @BindView(R.id.btnToolbarSend)
    Button mBtnToolbarSend;
    @BindView(R.id.etName)
    EditText mEtName;

    Topic mTopic;

    @Override
    public void init() {
        mGroupId = getIntent().getStringExtra("groupId");
    }

    @Override
    public void initView() {
        if (TextUtils.isEmpty(mGroupId)) {
            finish();
            return;
        }
        mBtnToolbarSend.setVisibility(View.VISIBLE);
        mBtnToolbarSend.setText(UIUtils.getString(R.string.save));
    }

    @Override
    public void initData() {
        mTopic = Cache.getTinode().getTopic(mGroupId);
        if (mTopic.isAdmin() || mTopic.isOwner()) {
            mBtnToolbarSend.setEnabled(true);
            VCard pub = (VCard) mTopic.getPub();
            if (pub != null) {
                if (!TextUtils.isEmpty(pub.fn)) {
                    mEtName.setText(pub.fn);
                    mEtName.setSelection(pub.fn.length());
                }
            }
        } else {
            mBtnToolbarSend.setEnabled(false);
        }
    }

    @Override
    public void initListener() {
        mEtName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mBtnToolbarSend.setEnabled(mEtName.getText().toString().trim().length() > 0);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mBtnToolbarSend.setOnClickListener(v -> {
            String groupName = mEtName.getText().toString().trim();
            UIUtils.showToast(UIUtils.getString(R.string.please_wait));
            if (!TextUtils.isEmpty(groupName)) {
                if (!mTopic.isAttached()) {
                    try {
                        mTopic.subscribe(null,
                                mTopic.getMetaGetBuilder()
                                        .withGetSub()
                                        .withGetData()
                                        .build()).thenApply(new PromisedReply.SuccessListener<ServerMessage>() {
                            @Override
                            public PromisedReply<ServerMessage> onSuccess(ServerMessage result) throws Exception {
                                updateName(groupName);
                                return null;
                            }
                        }, null);
                    } catch (NotConnectedException ignored) {
                        UIUtils.showToast(UIUtils.getString(R.string.no_connection));
                    } catch (Exception ex) {
                        UIUtils.showToast(UIUtils.getString(R.string.action_failed));
                    }
                } else {
                    try {
                        updateName(groupName);
                    } catch (NotConnectedException ignored) {
                        UIUtils.showToast(UIUtils.getString(R.string.no_connection));
                    } catch (Exception ex) {
                        UIUtils.showToast(UIUtils.getString(R.string.action_failed));
                    }
                }
//                ApiRetrofit.getInstance().setGroupName(mGroupId, groupName)
//                        .subscribeOn(Schedulers.io())
//                        .observeOn(AndroidSchedulers.mainThread())
//                        .subscribe(setGroupNameResponse -> {
//                            if (setGroupNameResponse != null && setGroupNameResponse.getCode() == 200) {
//                                Groups groups = DBManager.getInstance().getGroupsById(mGroupId);
//                                if (groups != null) {
//                                    groups.setName(groupName);
//                                    groups.saveOrUpdate("groupid=?", groups.getGroupId());
//                                }
//                                BroadcastManager.getInstance(SetGroupNameActivity.this).sendBroadcast(AppConst.UPDATE_CONVERSATIONS);
//                                BroadcastManager.getInstance(SetGroupNameActivity.this).sendBroadcast(AppConst.UPDATE_CURRENT_SESSION_NAME);
//                                UIUtils.showToast(UIUtils.getString(R.string.set_success));
//                                Intent data = new Intent();
//                                data.putExtra("group_name", groupName);
//                                setResult(RESULT_OK, data);
//                                hideWaitingDialog();
//                                finish();
//                            } else {
//                                hideWaitingDialog();
//                                UIUtils.showToast(UIUtils.getString(R.string.set_fail));
//                            }
//                        }, this::loadError);
            }
        });
    }

    private void updateName(String groupName) throws Exception {
//        showWaitingDialog(UIUtils.getString(R.string.please_wait));
        UiUtils.updateTitle(context, mTopic, groupName, mTopic.getPriv(), new PromisedReply.SuccessListener<ServerMessage>() {
            @Override
            public PromisedReply<ServerMessage> onSuccess(ServerMessage result) throws Exception {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        BroadcastManager.getInstance(SetGroupNameActivity.this).
                                sendBroadcast(AppConst.UPDATE_CONVERSATIONS);
                        BroadcastManager.getInstance(SetGroupNameActivity.this).
                                sendBroadcast(AppConst.UPDATE_CURRENT_SESSION_NAME);
                        UIUtils.showToast(UIUtils.getString(R.string.set_success));
                        Intent data = new Intent();
                        data.putExtra("group_name", groupName);
                        setResult(RESULT_OK, data);

//                        hideWaitingDialog();
                        try {
                            mTopic.leave();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        finish();
                    }
                });
                return null;
            }
        }, new PromisedReply.FailureListener() {
            @Override
            public PromisedReply onFailure(Exception err) throws Exception {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        hideWaitingDialog();
                        if (err instanceof NotConnectedException) {
                            UIUtils.showToast(UIUtils.getString(R.string.no_connection));
                        } else if(err.getMessage().contains("403")){
                            UIUtils.showToast("您不是群主，没有权限更改群名称。");
                        }else {
                            UIUtils.showToast(UIUtils.getString(R.string.action_failed));
                        }
                    }
                });
                return null;
            }
        });
    }

    @Override
    protected BasePresenter createPresenter() {
        return null;
    }

    @Override
    protected int provideContentViewId() {
        return R.layout.activity_group_name_set;
    }

    private void loadError(Throwable throwable) {
        LogUtils.sf(throwable.getLocalizedMessage());
        hideWaitingDialog();
        UIUtils.showToast(UIUtils.getString(R.string.set_fail));
    }
}

package com.yixin.tinode.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.lqr.adapter.LQRAdapterForRecyclerView;
import com.lqr.adapter.LQRViewHolderForRecyclerView;
import com.lqr.recyclerview.LQRRecyclerView;
import com.yixin.tinode.R;
import com.yixin.tinode.db.model.GroupMember;
import com.yixin.tinode.tinode.Cache;
import com.yixin.tinode.tinode.util.UiUtils;
import com.yixin.tinode.ui.base.BaseActivity;
import com.yixin.tinode.ui.base.BasePresenter;
import com.yixin.tinode.util.PinyinUtils;
import com.yixin.tinode.util.UIUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import butterknife.BindView;
import co.tinode.tinodesdk.Topic;
import co.tinode.tinodesdk.model.Subscription;
import co.tinode.tinodesdk.model.VCard;

/**
 * @创建者 CSDN_LQR
 * @描述 移出群成员界面(该界面简单就不使用mvp了)
 */

public class RemoveGroupMemberActivity extends BaseActivity {

    private String mGroupId;
    private List<GroupMember> mData = new ArrayList<>();
    private List<GroupMember> mSelectedData = new ArrayList<>();

    @BindView(R.id.btnToolbarSend)
    Button mBtnToolbarSend;
    @BindView(R.id.rvMember)
    LQRRecyclerView mRvMember;
    private LQRAdapterForRecyclerView<GroupMember> mAdapter;

    @Override
    public void init() {
        mGroupId = getIntent().getStringExtra("sessionId");
    }

    @Override
    public void initView() {
        if (TextUtils.isEmpty(mGroupId)) {
            finish();
            return;
        }

        mBtnToolbarSend.setText(UIUtils.getString(R.string.delete));
        mBtnToolbarSend.setVisibility(View.VISIBLE);
        mBtnToolbarSend.setBackgroundResource(R.drawable.shape_btn_delete);
        mBtnToolbarSend.setEnabled(false);
    }

    @Override
    public void initData() {
        Topic topic = Cache.getTinode().getTopic(mGroupId);
        Collection<Subscription> subscriptions = topic.getSubscriptions();
        List<GroupMember> groupMembers = new ArrayList<>();
        for (Subscription sub : subscriptions) {
            VCard userInfo = (VCard) sub.pub;
            if (userInfo == null) {
                //跳过me
                continue;
            }
            if (!sub.user.equals(Cache.getTinode().getMyId())) {
                GroupMember newMember = new GroupMember(mGroupId,
                        sub.user,
                        userInfo.fn,
                        userInfo.photo,
                        userInfo.fn,
                        PinyinUtils.getPinyin(userInfo.fn),
                        PinyinUtils.getPinyin(userInfo.fn),
                        "",
                        "",
                        "");
                groupMembers.add(newMember);
            }
        }

        mData.clear();
        mData.addAll(groupMembers);
        setAdapter();
    }

    @Override
    public void initListener() {
        mBtnToolbarSend.setOnClickListener(v -> {
            ArrayList<String> selectedIds = new ArrayList<>(mSelectedData.size());
            for (int i = 0; i < mSelectedData.size(); i++) {
                GroupMember groupMember = mSelectedData.get(i);
                selectedIds.add(groupMember.getUserId());
            }
            Intent data = new Intent();
            data.putStringArrayListExtra("selectedIds", selectedIds);
            setResult(Activity.RESULT_OK, data);
            finish();
        });
    }

    private void setAdapter() {
        if (mAdapter == null) {
            mAdapter = new LQRAdapterForRecyclerView<GroupMember>(this, mData, R.layout.item_contact) {
                @Override
                public void convert(LQRViewHolderForRecyclerView helper, GroupMember item, int position) {
                    helper.setText(R.id.tvName, item.getName()).setViewVisibility(R.id.cb, View.VISIBLE);
                    ImageView ivHeader = helper.getView(R.id.ivHeader);
//                    Glide.with(RemoveGroupMemberActivity.this).load(item.getPortraitUri()).centerCrop().into(ivHeader);
                    UiUtils.assignBitmap(context, ivHeader,
                            item != null && item.getPhoto() != null ? item.getPhoto().getBitmap() : null,
                            item != null ? item.getDisplayName() : null,
                            item.getUserId());

                    CheckBox cb = helper.getView(R.id.cb);
                    cb.setClickable(true);
                    cb.setChecked(mSelectedData.contains(item) ? true : false);
                    cb.setOnClickListener(v -> {
                        if (cb.isChecked()) {
                            mSelectedData.add(item);
                        } else {
                            mSelectedData.remove(item);
                        }
                        if (mSelectedData.size() > 0) {
                            mBtnToolbarSend.setEnabled(true);
                            mBtnToolbarSend.setText(UIUtils.getString(R.string.delete) + "(" + mSelectedData.size() + ")");
                        } else {
                            mBtnToolbarSend.setEnabled(false);
                            mBtnToolbarSend.setText(UIUtils.getString(R.string.delete));
                        }
                    });
                }
            };
            mAdapter.setOnItemClickListener((helper, parent, itemView, position) -> {
                Intent intent = new Intent(RemoveGroupMemberActivity.this, UserInfoActivity.class);
                intent.putExtra("topic", mData.get(position).getUserId());
                jumpToActivity(intent);
            });
            mRvMember.setAdapter(mAdapter);
        } else {
            mAdapter.notifyDataSetChangedWrapper();
        }
    }

    @Override
    protected BasePresenter createPresenter() {
        return null;
    }

    @Override
    protected int provideContentViewId() {
        return R.layout.activity_remove_group_member;
    }

}

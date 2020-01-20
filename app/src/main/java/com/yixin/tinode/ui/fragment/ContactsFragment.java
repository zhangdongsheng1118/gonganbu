package com.yixin.tinode.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.lqr.adapter.LQRAdapterForRecyclerView;
import com.lqr.adapter.LQRHeaderAndFooterAdapter;
import com.lqr.recyclerview.LQRRecyclerView;
import com.yixin.tinode.R;
import com.yixin.tinode.app.AppConst;
import com.yixin.tinode.db.model.Friend;
import com.yixin.tinode.manager.BroadcastManager;
import com.yixin.tinode.ui.activity.CreateGroupActivity;
import com.yixin.tinode.ui.activity.GroupListActivity;
import com.yixin.tinode.ui.activity.MainActivity;
import com.yixin.tinode.ui.activity.NewFriendActivity;
import com.yixin.tinode.ui.activity.SearchGlobalActivity;
import com.yixin.tinode.ui.base.BaseFragment;
import com.yixin.tinode.ui.presenter.ContactsFgPresenter;
import com.yixin.tinode.ui.view.IContactsFgView;
import com.yixin.tinode.util.PopupWindowUtils;
import com.yixin.tinode.util.UIUtils;
import com.yixin.tinode.widget.QuickIndexBar;
import com.zuozhan.app.RouterUtil;
import com.zuozhan.app.activity.AllBaseActivity;

import java.util.List;

import butterknife.BindView;


/**
 * @创建者 CSDN_LQR
 * @描述 通讯录界面
 */
public class ContactsFragment extends BaseFragment<IContactsFgView, ContactsFgPresenter> implements IContactsFgView {

    @BindView(R.id.rvContacts)
    LQRRecyclerView mRvContacts;
    @BindView(R.id.qib)
    QuickIndexBar mQib;
    @BindView(R.id.tvLetter)
    TextView mTvLetter;
    @BindView(R.id.message_search1)
    View message_search;
    @BindView(R.id.message_btn1)
    View message_btn;
    private View mHeaderView;
    private TextView mFooterView;
    private TextView mTvNewFriendUnread;

    @Override
    public void initView(View rootView) {
        mHeaderView = View.inflate(getActivity(), R.layout.header_rv_contacts, null);
        mTvNewFriendUnread = (TextView) mHeaderView.findViewById(R.id.tvNewFriendUnread);
        mFooterView = new TextView(getContext());
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, UIUtils.dip2Px(50));
        mFooterView.setLayoutParams(params);
        mFooterView.setGravity(Gravity.CENTER);
        mFooterView.setTextColor(getResources().getColor(R.color.white));
        registerBR();
    }

    @Override
    public void initData() {
        message_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RouterUtil.goActivity(ContactsFragment.this, SearchGlobalActivity.class);
            }
        });
        message_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View menuView = View.inflate(getActivity(), R.layout.menu_main, null);

                PopupWindow popupWindow = PopupWindowUtils.getPopupWindowAsDropDown(menuView,
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,message_btn,
                        0,20);
                menuView.findViewById(R.id.tvCreateGroup).setOnClickListener(v1 -> {
                    RouterUtil.goActivity(ContactsFragment.this, CreateGroupActivity.class);
                    popupWindow.dismiss();
                });
//                menuView.findViewById(R.id.tvAddFriend).setOnClickListener(v1 -> {
//                    RouterUtil.goActivity(ContactsFragment.this, SearchUserActivity.class);
//                    popupWindow.dismiss();
//                });
            }
        });
    }

    @Override
    public void initListener() {
        mHeaderView.findViewById(R.id.llNewFriend).setOnClickListener(v -> {
            ((AllBaseActivity) getActivity()).jumpToActivity(NewFriendActivity.class);
//            ((MainActivity) getActivity()).mTvContactRedDot.setVisibility(View.GONE);
            mTvNewFriendUnread.setVisibility(View.GONE);
        });
        mHeaderView.findViewById(R.id.llGroup).setOnClickListener(v -> ((AllBaseActivity) getActivity()).jumpToActivity(GroupListActivity.class));
        mQib.setOnLetterUpdateListener(new QuickIndexBar.OnLetterUpdateListener() {
            @Override
            public void onLetterUpdate(String letter) {
                //显示对话框
                showLetter(letter);
                //滑动到第一个对应字母开头的联系人
                if ("↑".equalsIgnoreCase(letter)) {
                    mRvContacts.moveToPosition(0);
                } else if ("☆".equalsIgnoreCase(letter)) {
                    mRvContacts.moveToPosition(0);
                } else {
                    List<Friend> data = ((LQRAdapterForRecyclerView) ((LQRHeaderAndFooterAdapter) mRvContacts.getAdapter()).getInnerAdapter()).getData();
                    for (int i = 0; i < data.size(); i++) {
                        Friend friend = data.get(i);
                        String c = "";
                        try {
                            c = friend.getDisplayNameSpelling().charAt(0) + "";
                        }catch (Exception e){

                        }
                        if (c.equalsIgnoreCase(letter)) {
                            mRvContacts.moveToPosition(i);
                            break;
                        }
                    }
                }
            }

            @Override
            public void onLetterCancel() {
                //隐藏对话框
                hideLetter();
            }
        });
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            //相当于Fragment的onResume
            mPresenter.loadContacts();
        } else {
            //相当于Fragment的onPause
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        mPresenter.loadContacts();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterBR();
    }

    private void registerBR() {
        BroadcastManager.getInstance(getActivity()).register(AppConst.UPDATE_RED_DOT, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                ((MainActivity) getActivity()).mTvContactRedDot.setVisibility(View.VISIBLE);
                mTvNewFriendUnread.setVisibility(View.VISIBLE);
            }
        });
        BroadcastManager.getInstance(getActivity()).register(AppConst.UPDATE_FRIEND, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mPresenter.loadContacts();
            }
        });
    }

    private void unregisterBR() {
        BroadcastManager.getInstance(getActivity()).unregister(AppConst.UPDATE_RED_DOT);
        BroadcastManager.getInstance(getActivity()).unregister(AppConst.UPDATE_FRIEND);
    }

    private void showLetter(String letter) {
        mTvLetter.setVisibility(View.VISIBLE);
        mTvLetter.setText(letter);
    }

    private void hideLetter() {
        mTvLetter.setVisibility(View.GONE);
    }

    /**
     * 是否显示快速导航条
     *
     * @param show
     */
    public void showQuickIndexBar(boolean show) {
        if (mQib != null) {
            mQib.setVisibility(show ? View.VISIBLE : View.GONE);
            mQib.invalidate();
        }
    }

    @Override
    protected ContactsFgPresenter createPresenter() {
        return new ContactsFgPresenter((AllBaseActivity) getActivity());
    }

    @Override
    protected int provideContentViewId() {
        return R.layout.fragment_contacts;
    }

    @Override
    public View getHeaderView() {
        return mHeaderView;
    }

    @Override
    public LQRRecyclerView getRvContacts() {
        return mRvContacts;
    }

    @Override
    public TextView getFooterView() {
        return mFooterView;
    }
}

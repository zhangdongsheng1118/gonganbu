package com.yixin.tinode.ui.activity;

import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lqr.recyclerview.LQRRecyclerView;
import com.yixin.tinode.R;
import com.yixin.tinode.tinode.Cache;
import com.yixin.tinode.ui.base.BaseActivity;
import com.yixin.tinode.ui.presenter.SearchGlobalAtPresenter;
import com.yixin.tinode.ui.presenter.SearchMsgAtPresenter;
import com.yixin.tinode.ui.view.ISearchGlobalAtView;
import com.yixin.tinode.ui.view.ISearchMsgAtView;

import butterknife.BindView;
import cn.bingoogolapple.refreshlayout.BGANormalRefreshViewHolder;
import cn.bingoogolapple.refreshlayout.BGARefreshLayout;
import co.tinode.tinodesdk.Tinode;
import co.tinode.tinodesdk.Topic;


/**
 * 搜索联系人/群聊/聊天记录
 *
 * @描述 搜索用户界面
 */
public class SearchGlobalActivity extends BaseActivity<ISearchGlobalAtView, SearchGlobalAtPresenter> implements ISearchGlobalAtView {
    @BindView(R.id.llToolbarSearch)
    LinearLayout mLlToolbarSearch;
    @BindView(R.id.etSearchContent)
    EditText mEtSearchContent;
    @BindView(R.id.rlNoResultTip)
    RelativeLayout mRlNoResultTip;
    @BindView(R.id.llSearch)
    LinearLayout mLlSearch;
    @BindView(R.id.tvMsg)
    TextView mTvMsg;
    @BindView(R.id.rvMsg)
    LQRRecyclerView mRvMsg;
    @BindView(R.id.tv_tip)
    TextView mTvTip;

    @Override
    public void initView() {
        mToolbarTitle.setVisibility(View.GONE);
        mLlToolbarSearch.setVisibility(View.VISIBLE);
        mTvTip.setText("未搜索到结果");

        mEtSearchContent.setHint("昵称搜索本地联系人/群聊/聊天记录");
    }

    @Override
    public void initListener() {
        try {
            Cache.getTinode().subscribe(Tinode.TOPIC_FND, null, null,false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mEtSearchContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String content = mEtSearchContent.getText().toString().trim();
                mRlNoResultTip.setVisibility(View.GONE);
                if (content.length() > 0) {
                    mLlSearch.setVisibility(View.VISIBLE);
                    mTvMsg.setText(content);
                } else {
                    mLlSearch.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mLlSearch.setOnClickListener(v -> mPresenter.searchUser());
    }

    @Override
    protected SearchGlobalAtPresenter createPresenter() {
        return new SearchGlobalAtPresenter(this);
    }

    @Override
    protected int provideContentViewId() {
        return R.layout.activity_search_user;
    }

    @Override
    public EditText getEtSearchContent() {
        return mEtSearchContent;
    }

    @Override
    public RelativeLayout getRlNoResultTip() {
        return mRlNoResultTip;
    }

    @Override
    public LinearLayout getLlSearch() {
        return mLlSearch;
    }

    @Override
    public BGARefreshLayout getRefreshLayout() {
        return null;
    }

    @Override
    public LQRRecyclerView getRvMsg() {
        return mRvMsg;
    }
}

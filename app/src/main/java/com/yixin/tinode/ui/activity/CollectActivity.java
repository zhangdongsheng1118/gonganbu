package com.yixin.tinode.ui.activity;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lqr.recyclerview.LQRRecyclerView;
import com.yixin.tinode.R;
import com.yixin.tinode.ui.base.BaseActivity;
import com.yixin.tinode.ui.presenter.CollectionAtPresenter;
import com.yixin.tinode.ui.view.ICollectionAtView;
import com.yixin.tinode.util.StringUtils;
import com.zhy.autolayout.AutoRelativeLayout;

import butterknife.BindView;
import butterknife.OnClick;
import cn.bingoogolapple.refreshlayout.BGANormalRefreshViewHolder;
import cn.bingoogolapple.refreshlayout.BGARefreshLayout;


/**
 * @描述 收藏
 */
public class CollectActivity extends BaseActivity<ICollectionAtView, CollectionAtPresenter> implements ICollectionAtView, BGARefreshLayout.BGARefreshLayoutDelegate {

    @BindView(R.id.llToolbarSearch)
    LinearLayout mLlToolbarSearch;
    @BindView(R.id.etSearchContent)
    EditText mEtSearchContent;
    @BindView(R.id.iv_search)
    ImageView ivSearch;

    @BindView(R.id.rlNoResultTip)
    RelativeLayout mRlNoResultTip;
    @BindView(R.id.llSearch)
    LinearLayout mLlSearch;
    @BindView(R.id.tvMsg)
    TextView mTvMsg;

    @BindView(R.id.refreshLayout)
    BGARefreshLayout mRefreshLayout;
    @BindView(R.id.rvMsg)
    LQRRecyclerView mRvMsg;

    @BindView(R.id.tv_tip)
    TextView mTvTip;
    @BindView(R.id.tvToolbarTitle)
    TextView tvTitle;

    @Override
    public void initView() {
//        mToolbarTitle.setVisibility(View.GONE);
//        mLlToolbarSearch.setVisibility(View.VISIBLE);
        ivSearch.setVisibility(View.VISIBLE);
        AutoRelativeLayout.LayoutParams layoutParams = (AutoRelativeLayout.LayoutParams) ivSearch.getLayoutParams();
        layoutParams.addRule(AutoRelativeLayout.ALIGN_PARENT_RIGHT);
        ivSearch.setLayoutParams(layoutParams);

        mTvTip.setText("没有收藏结果");
        tvTitle.setText("收藏");

        String key = getIntent().getStringExtra("key");
        if (!StringUtils.isBlank(key)) {
            mEtSearchContent.setText(key);
            mPresenter.getList();
        }
    }

    @OnClick({R.id.iv_search})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_search:
                tvTitle.setVisibility(View.GONE);
                ivSearch.setVisibility(View.GONE);
                mLlToolbarSearch.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    public void initListener() {
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

        mLlSearch.setOnClickListener(v -> mPresenter.getList());

        initRefreshLayout();
        mPresenter.initList();
    }

    @Override
    protected CollectionAtPresenter createPresenter() {
        return new CollectionAtPresenter(this);
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
    public void onBGARefreshLayoutBeginRefreshing(BGARefreshLayout bgaRefreshLayout) {
    }

    @Override
    public boolean onBGARefreshLayoutBeginLoadingMore(BGARefreshLayout bgaRefreshLayout) {
        return mPresenter.getList();
    }

    private void initRefreshLayout() {
        mRefreshLayout.setDelegate(this);
        BGANormalRefreshViewHolder refreshViewHolder = new BGANormalRefreshViewHolder(this, false);
        refreshViewHolder.setRefreshingText("");
        refreshViewHolder.setPullDownRefreshText("");
        refreshViewHolder.setReleaseRefreshText("");
        mRefreshLayout.setRefreshViewHolder(refreshViewHolder);
    }

    @Override
    public BGARefreshLayout getRefreshLayout() {
        return mRefreshLayout;
    }

    @Override
    public LQRRecyclerView getRvMsg() {
        return mRvMsg;
    }
}

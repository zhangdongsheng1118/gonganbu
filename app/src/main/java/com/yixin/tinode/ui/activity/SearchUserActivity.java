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
import com.yixin.tinode.ui.presenter.SearchUserAtPresenter;
import com.yixin.tinode.ui.view.ISearchUserAtView;

import butterknife.BindView;
import cn.bingoogolapple.refreshlayout.BGANormalRefreshViewHolder;
import cn.bingoogolapple.refreshlayout.BGARefreshLayout;
import co.tinode.tinodesdk.Tinode;


/**
 * @创建者 搜索流程：
 * {"set":{"id":"75189","topic":"fnd","desc":{"public":"email:alice@example.com"}}}
 * *********************{"ctrl":{"id":"75189","topic":"fnd","code":200,"text":"ok","ts":"2018-06-15T15:39:49.403Z"}}
 * {"get":{"id":"75190","topic":"fnd","what":"sub"}}
 * ********************{"meta":{"id":"75190","topic":"fnd","ts":"2018-06-15T15:39:49.438Z","sub":[{"updated":"2018-06-09T06:17:44.576Z","acs":{},"public":{"fn":"Alice Johnson","photo":{"data":"/9j/4AAQSkZJRgABAQEASABIAAD/4QCYRXhpZgAATU0AKgAAAAgABAEaAAUAAAABAAAAPgEbAAUAAAABAAAARgEoAAMAAAABAAIAAIdpAAQAAAABAAAATgAAAAAAAABIAAAAAQAAAEgAAAABAAWQAAAHAAAABDAyMTCgAAAHAAAABDAxMDCgAQADAAAAAQABAACgAgAEAAAAAQAAAECgAwAEAAAAAQAAAEAAAAAA/+EEYmh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC8APD94cGFja2V0IGJlZ2luPSfvu78nIGlkPSdXNU0wTXBDZWhpSHpyZVN6TlRjemtjOWQnPz4KPHg6eG1wbWV0YSB4bWxuczp4PSdhZG9iZTpuczptZXRhLyc+CjxyZGY6UkRGIHhtbG5zOnJkZj0naHR0cDovL3d3dy53My5vcmcvMTk5OS8wMi8yMi1yZGYtc3ludGF4LW5zIyc+CgogPHJkZjpEZXNjcmlwdGlvbiB4bWxuczpleGlmPSdodHRwOi8vbnMuYWRvYmUuY29tL2V4aWYvMS4wLyc+CiAgPGV4aWY6WFJlc29sdXRpb24+NzI8L2V4aWY6WFJlc29sdXRpb24+CiAgPGV4aWY6WVJlc29sdXRpb24+NzI8L2V4aWY6WVJlc29sdXRpb24+CiAgPGV4aWY6UmVzb2x1dGlvblVuaXQ+SW5jaDwvZXhpZjpSZXNvbHV0aW9uVW5pdD4KICA8ZXhpZjpGbGFzaFBpeFZlcnNpb24+Rmxhc2hQaXggVmVyc2lvbiAxLjA8L2V4aWY6Rmxhc2hQaXhWZXJzaW9uPgogIDxleGlmOlhSZXNvbHV0aW9uPjcyPC9leGlmOlhSZXNvbHV0aW9uPgogIDxleGlmOllSZXNvbHV0aW9uPjcyPC9leGlmOllSZXNvbHV0aW9uPgogIDxleGlmOlJlc29sdXRpb25Vbml0PkluY2g8L2V4aWY6UmVzb2x1dGlvblVuaXQ+CiAgPGV4aWY6Rmxhc2hQaXhWZXJzaW9uPkZsYXNoUGl4IFZlcnNpb24gMS4wPC9leGlmOkZsYXNoUGl4VmVyc2lvbj4KICA8ZXhpZjpYUmVzb2x1dGlvbj43MjwvZXhpZjpYUmVzb2x1dGlvbj4KICA8ZXhpZjpZUmVzb2x1dGlvbj43MjwvZXhpZjpZUmVzb2x1dGlvbj4KICA8ZXhpZjpSZXNvbHV0aW9uVW5pdD5JbmNoPC9leGlmOlJlc29sdXRpb25Vbml0PgogIDxleGlmOkV4aWZWZXJzaW9uPkV4aWYgVmVyc2lvbiAyLjE8L2V4aWY6RXhpZlZlcnNpb24+CiAgPGV4aWY6Rmxhc2hQaXhWZXJzaW9uPkZsYXNoUGl4IFZlcnNpb24gMS4wPC9leGlmOkZsYXNoUGl4VmVyc2lvbj4KICA8ZXhpZjpDb2xvclNwYWNlPnNSR0I8L2V4aWY6Q29sb3JTcGFjZT4KICA8ZXhpZjpQaXhlbFhEaW1lbnNpb24+NjQwPC9leGlmOlBpeGVsWERpbWVuc2lvbj4KICA8ZXhpZjpQaXhlbFlEaW1lbnNpb24+NDI2PC9leGlmOlBpeGVsWURpbWVuc2lvbj4KIDwvcmRmOkRlc2NyaXB0aW9uPgoKPC9yZGY6UkRGPgo8L3g6eG1wbWV0YT4KPD94cGFja2V0IGVuZD0ncic/Pgr/4gxYSUNDX1BST0ZJTEUAAQEAAAxITGlubwIQAABtbnRyUkdCIFhZWiAHzgACAAkABgAxAABhY3NwTVNGVAAAAABJRUMgc1JHQgAAAAAAAAAAAAAAAAAA9tYAAQAAAADTLUhQICAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABFjcHJ0AAABUAAAADNkZXNjAAABhAAAAGx3dHB0AAAB8AAAABRia3B0AAACBAAAABRyWFlaAAACGAAAABRnWFlaAAACLAAAABRiWFlaAAACQAAAABRkbW5kAAACVAAAAHBkbWRkAAACxAAAAIh2dWVkAAADTAAAAIZ2aWV3AAAD1AAAACRsdW1pAAAD+AAAABRtZWFzAAAEDAAAACR0ZWNoAAAEMAAAAAxyVFJDAAAEPAAACAxnVFJDAAAEPAAACAxiVFJDAAAEPAAACAx0ZXh0AAAAAENvcHlyaWdodCAoYykgMTk5OCBIZXdsZXR0LVBhY2thcmQgQ29tcGFueQAAZGVzYwAAAAAAAAASc1JHQiBJRUM2MTk2Ni0yLjEAAAAAAAAAAAAAABJzUkdCIElFQzYxOTY2LTIuMQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA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","type":"jpg"}},"private":["email:alice@example.com"],"user":"usrop8w2nz0Yx4"}]}}
 * @描述 搜索用户界面
 */
public class SearchUserActivity extends BaseActivity<ISearchUserAtView, SearchUserAtPresenter> implements ISearchUserAtView, BGARefreshLayout.BGARefreshLayoutDelegate {

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

    @BindView(R.id.refreshLayout)
    BGARefreshLayout mRefreshLayout;
    @BindView(R.id.rvMsg)
    LQRRecyclerView mRvMsg;

    @Override
    public void initView() {
        mToolbarTitle.setVisibility(View.GONE);
        mLlToolbarSearch.setVisibility(View.VISIBLE);

        mEtSearchContent.setHint(R.string.wechat_qq_phone);
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
    protected SearchUserAtPresenter createPresenter() {
        return new SearchUserAtPresenter(this);
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

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mRefreshLayout.endRefreshing();
            }
        }, 1000);
    }

    @Override
    public boolean onBGARefreshLayoutBeginLoadingMore(BGARefreshLayout bgaRefreshLayout) {
        return false;
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

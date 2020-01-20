package com.yixin.tinode.ui.presenter;

import android.content.Intent;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.blankj.utilcode.util.ToastUtils;
import com.lqr.adapter.LQRAdapterForRecyclerView;
import com.lqr.adapter.LQRViewHolderForRecyclerView;
import com.yixin.tinode.R;
import com.yixin.tinode.app.AppConst;
import com.yixin.tinode.db.tinode.BaseDb;
import com.yixin.tinode.db.tinode.MessageDb;
import com.yixin.tinode.db.tinode.StoredMessageForSearch;
import com.yixin.tinode.tinode.util.UiUtils;
import com.yixin.tinode.ui.activity.SearchMsgActivity;
import com.yixin.tinode.ui.activity.SessionActivity;
import com.yixin.tinode.ui.base.BaseActivity;
import com.yixin.tinode.ui.base.BasePresenter;
import com.yixin.tinode.ui.view.ISearchGlobalAtView;
import com.yixin.tinode.util.LogUtils;
import com.yixin.tinode.util.TimeUtils;
import com.yixin.tinode.util.UIUtils;

import java.util.ArrayList;
import java.util.List;

import co.tinode.tinodesdk.Topic;
import co.tinode.tinodesdk.model.VCard;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class SearchGlobalAtPresenter extends BasePresenter<ISearchGlobalAtView> {
    private LQRAdapterForRecyclerView<StoredMessageForSearch> mAdapter;
    private List<StoredMessageForSearch> mData = new ArrayList<>();

    public SearchGlobalAtPresenter(BaseActivity context) {
        super(context);
    }

    public static final int VIEW_TYPE_USER = R.layout.item_search;
    public static final int VIEW_TYPE_MSG = R.layout.item_search;
    public static final int VIEW_TYPE_MORE = R.layout.item_search_more;

    private void setAdapter() {
        if (mAdapter == null) {
            //列表数据，类型0联系人1聊天记录。首个显示标题。两个类型之间显示分割线。超过三个显示更多选项。
            mAdapter = new LQRAdapterForRecyclerView<StoredMessageForSearch>(mContext, mData, R.layout.item_recent_message) {

                @Override
                public int getItemViewType(int position) {
                    return getItem(position).viewType;
                }

                @Override
                public void convert(LQRViewHolderForRecyclerView helper, StoredMessageForSearch item, int position) {
                    int viewType = getItemViewType(position);
                    switch (viewType) {
                        case VIEW_TYPE_USER:
                            ImageView ivHeader = helper.getView(R.id.ivHeader);

                            VCard pub = item.pub;
                            UiUtils.assignBitmap(mContext, ivHeader,
                                    pub != null ? pub.getBitmap() : null,
                                    pub != null ? pub.fn : null,
                                    item.from);

                            helper.setText(R.id.tvDisplayName, pub != null ? pub.fn : "未知用户")
//									.setText(R.id.tvTime, TimeUtils.getMsgFormatTime(item.getReceivedTime()))
                                    .setViewVisibility(R.id.ngiv, View.GONE)
                                    .setViewVisibility(R.id.ivHeader, View.VISIBLE);

                            if (item.dataType == AppConst.SEARCH_DATA_TYPE_MSG) {
                                helper.setText(R.id.tvContent, item.search);
                                helper.setText(R.id.tvTime, TimeUtils.getMsgFormatTime(item.ts.getTime(), true));
                                helper.setText(R.id.tv_title, "聊天记录");
                            } else {
                                helper.setText(R.id.tvContent, "");
                                helper.setText(R.id.tvTime, "");
                                helper.setText(R.id.tv_title, "联系人");
                            }

                            if (position == 0) {
                                helper.getView(R.id.ll_title).setVisibility(View.VISIBLE);
                            } else {
                                int pre = position - 1;
                                if (pre >= 0) {
                                    int preType = getItem(pre).dataType;
                                    if (preType != item.dataType && viewType != VIEW_TYPE_MORE) {
                                        helper.getView(R.id.ll_title).setVisibility(View.VISIBLE);
                                    } else {
                                        helper.getView(R.id.ll_title).setVisibility(View.GONE);
                                    }
                                } else {
                                    helper.getView(R.id.ll_title).setVisibility(View.GONE);
                                }
                            }
                            break;
                        case VIEW_TYPE_MORE:
                            break;
                    }
                }
            };
            mAdapter.setOnItemClickListener((helper, parent, itemView, position) -> {
                StoredMessageForSearch item = mData.get(position);
                if (item.viewType == VIEW_TYPE_USER || item.viewType == VIEW_TYPE_MSG) {
                    if (item.count > 1) {
                        Intent intent = new Intent(mContext, SearchMsgActivity.class);
                        intent.putExtra("topic", item.from);
                        intent.putExtra("key", getView().getEtSearchContent().getText().toString());
                        mContext.startActivity(intent);
                    } else {
                        Intent intent = new Intent(mContext, SessionActivity.class);
                        intent.putExtra("sessionId", item.from);
                        if (Topic.getTopicTypeByName(item.from) == Topic.TopicType.P2P) {
                            intent.putExtra("sessionType", SessionActivity.SESSION_TYPE_PRIVATE);
                        } else {
                            intent.putExtra("sessionType", SessionActivity.SESSION_TYPE_GROUP);
                        }
                        intent.putExtra("firstMsgId", item.msgId);
                        mContext.jumpToActivity(intent);
                    }
                } else {
                    ToastUtils.showShort("developing");
                }
            });

            getView().getRvMsg().setAdapter(mAdapter);
            getView().getRvMsg().addItemDecoration(new SpaceItemDecoration(50));
        } else {
            mAdapter.notifyDataSetChangedWrapper();
        }
    }

    class SpaceItemDecoration extends RecyclerView.ItemDecoration {
        int mSpace;

        /**
         * Retrieve any offsets for the given item. Each field of <code>outRect</code> specifies
         * the number of pixels that the item view should be inset by, similar to padding or margin.
         * The default implementation sets the bounds of outRect to 0 and returns.
         * <p>
         * <p>
         * If this ItemDecoration does not affect the positioning of item views, it should set
         * all four fields of <code>outRect</code> (left, top, right, bottom) to zero
         * before returning.
         * <p>
         * <p>
         * If you need to access Adapter for additional data, you can call
         * {@link RecyclerView#getChildAdapterPosition(View)} to get the adapter position of the
         * View.
         *
         * @param outRect Rect to receive the output.
         * @param view    The child view to decorate
         * @param parent  RecyclerView this ItemDecoration is decorating
         * @param state   The current state of RecyclerView.
         */
        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            outRect.bottom = 1;
            int curPos = parent.getChildAdapterPosition(view);
            int pre = curPos - 1;
            if (pre >= 0) {
                int preType = ((LQRAdapterForRecyclerView<StoredMessageForSearch>) parent.getAdapter()).getItem(pre).dataType;
                int curType = ((LQRAdapterForRecyclerView<StoredMessageForSearch>) parent.getAdapter()).getItem(curPos).dataType;
                if (preType != curType) {
                    outRect.top = mSpace;
                } else {
                }
            } else {
            }
        }

        public SpaceItemDecoration(int space) {
            this.mSpace = space;
        }

    }

    public void searchUser() {
        mData.clear();
        String content = getView().getEtSearchContent().getText().toString();

        if (TextUtils.isEmpty(content.trim())) {
            UIUtils.showToast(UIUtils.getString(R.string.content_no_empty));
            return;
        }

        mContext.showWaitingDialog(UIUtils.getString(R.string.please_wait));
//列表数据，类型0联系人1聊天记录。首个显示标题。两个类型之间显示分割线。超过三个显示更多选项。
        Observable.just(MessageDb.searchList(BaseDb.getInstance().getWritableDatabase(), content))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(messages -> {
                    if (messages.size() > 0) {
                        //处理viewType，新增更多选项
                        int sameCount = 0;
                        for (int index = 0; index < messages.size(); ++index) {
                            StoredMessageForSearch tmp = messages.get(index);

                            int dataType = tmp.dataType;
                            if (dataType == AppConst.SEARCH_DATA_TYPE_MSG) {
                                tmp.viewType = VIEW_TYPE_MSG;
                            } else if (dataType == AppConst.SEARCH_DATA_TYPE_USER) {
                                tmp.viewType = VIEW_TYPE_USER;
                            } else {

                            }

                            mData.add(tmp);
                            ++sameCount;

                            int after = index + 1;
                            if (after < messages.size()) {
                                if (messages.get(after).dataType != tmp.dataType) {
                                    if (sameCount == 100) {
                                        StoredMessageForSearch more = new StoredMessageForSearch();
                                        more.dataType = tmp.dataType;
                                        more.viewType = VIEW_TYPE_MORE;
                                        mData.add(more);
                                    }

                                    sameCount = 0;
                                } else {
                                }
                            }
                        }

                        mContext.hideWaitingDialog();
                        setAdapter();
                        getView().getLlSearch().setVisibility(View.GONE);
                    } else {
                        mContext.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mContext.hideWaitingDialog();
                                getView().getRlNoResultTip().setVisibility(View.VISIBLE);
                                getView().getLlSearch().setVisibility(View.GONE);
                            }
                        });
                    }
                }, throwable -> {
                    mContext.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mContext.hideWaitingDialog();
                            ToastUtils.showShort("搜索失败");
                        }
                    });
                });
    }

    private void loadError(Throwable throwable) {
        mContext.hideWaitingDialog();
        LogUtils.sf(throwable.getLocalizedMessage());
    }
}

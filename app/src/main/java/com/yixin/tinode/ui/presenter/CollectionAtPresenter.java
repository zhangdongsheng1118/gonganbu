package com.yixin.tinode.ui.presenter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.Gson;
import com.lqr.adapter.LQRAdapterForRecyclerView;
import com.lqr.adapter.LQRViewHolderForRecyclerView;
import com.yixin.tinode.R;
import com.yixin.tinode.api.ApiRetrofit;
import com.yixin.tinode.api.param.ReturnResult;
import com.yixin.tinode.api.param.TiCollection;
import com.yixin.tinode.app.AppConst;
import com.yixin.tinode.ui.activity.CollectShowActivity;
import com.yixin.tinode.ui.activity.LoginActivity;
import com.yixin.tinode.ui.activity.RelayActivity;
import com.yixin.tinode.ui.base.BaseActivity;
import com.yixin.tinode.ui.base.BasePresenter;
import com.yixin.tinode.ui.view.ICollectionAtView;
import com.yixin.tinode.util.LogUtils;
import com.yixin.tinode.util.TimeUtils;
import com.yixin.tinode.util.UIUtils;
import com.yixin.tinode.widget.CustomDialog;

import java.util.ArrayList;
import java.util.List;

import co.tinode.tinodesdk.model.Drafty;
import rx.schedulers.Schedulers;

public class CollectionAtPresenter extends BasePresenter<ICollectionAtView> {

    private LQRAdapterForRecyclerView<TiCollection> mAdapter;
    private List<TiCollection> mData = new ArrayList<>();
    private CustomDialog mSessionMenuDialog;

    public CollectionAtPresenter(BaseActivity context) {
        super(context);
    }

    private void setAdapter() {
        if (mAdapter == null) {
            mAdapter = new LQRAdapterForRecyclerView<TiCollection>(mContext, mData, R.layout.item_collection) {
                @Override
                public void convert(LQRViewHolderForRecyclerView helper, TiCollection item, int position) {
                    ImageView ivHeader = helper.getView(R.id.ivHeader);
                    ivHeader.setVisibility(View.GONE);
                    ImageView ivPic = helper.getView(R.id.ivPic);
                    ivPic.setVisibility(View.GONE);

                    Drafty.CollectDes des = new Gson().fromJson(item.getDes(), Drafty.CollectDes.class);

                    switch (item.getType()) {
                        case AppConst.COLLECTION_TYPE_FILE:
                            helper.setViewVisibility(R.id.tvContent, View.GONE);
                            helper.setViewVisibility(R.id.rl_file, View.VISIBLE);

                            break;
                        case AppConst.COLLECTION_TYPE_TXT:
                            helper.setViewVisibility(R.id.tvContent, View.VISIBLE);
                            helper.setText(R.id.tvContent, des.getTxt());
                            break;
                        case AppConst.COLLECTION_TYPE_VIDEO:
                            break;
                        case AppConst.COLLECTION_TYPE_PIC:
                            ivPic.setVisibility(View.VISIBLE);
                            String image = des.getTxt();
                            Glide.with(mContext).load(image.startsWith("http") ? image + "?width=200" : "http://" + image + "?width=200").error(R.mipmap.default_img_failed)
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .override(UIUtils.dip2Px(150), UIUtils.dip2Px(300))
                                    .fitCenter()
                                    .into(ivPic);
                            break;
                        case AppConst.COLLECTION_TYPE_LOCATION:
                            break;
                        case AppConst.COLLECTION_TYPE_SOUND:
                            break;
                    }

                    if (item.getCreateTime() != null) {
                        helper.setText(R.id.tvTime, TimeUtils.getMsgFormatTime(Long.valueOf(item.getCreateTime()), true));
                    }
                    helper.setText(R.id.tvDisplayName, item.getFromName());
//                    helper.setBackgroundColor(R.id.flRoot, android.R.color.white);
                }
            };
            mAdapter.setOnItemClickListener((helper, parent, itemView, position) -> {
//                Topic topic = getView().getTopic();
//                TiCollection item = mData.get(position);
//                Subscription<VCard, ?> sub = topic != null ? topic.getSubscription(item.from) : null;
//                if (sub != null) {
//                    Intent intent = new Intent(mContext, SessionActivity.class);
//                    intent.putExtra("sessionId", sub.topic);
//                    if (topic.getTopicType() == Topic.TopicType.P2P) {
//                        intent.putExtra("sessionType", SessionActivity.SESSION_TYPE_PRIVATE);
//                    } else {
//                        intent.putExtra("sessionType", SessionActivity.SESSION_TYPE_GROUP);
//                    }
//                    intent.putExtra("firstMsgId", item.id);
//                    mContext.jumpToActivity(intent);
//                } else {
//                    ToastUtils.showShort("未查询到用户信息");
//                }

                Intent intent = new Intent(mContext, CollectShowActivity.class);
                intent.putExtra("des", mData.get(position).getDes());
                intent.putExtra("createTime", mData.get(position).getCreateTime());
                intent.putExtra("fromName", mData.get(position).getFromName());
                intent.putExtra("type", mData.get(position).getType());
                mContext.jumpToActivity(intent);
            });
            mAdapter.setOnItemLongClickListener((helper, parent, itemView, position) -> {
                View sessionMenuView = View.inflate(mContext, R.layout.dialog_session_menu, null);
                mSessionMenuDialog = new CustomDialog(mContext, sessionMenuView, R.style.MyDialog);
                mSessionMenuDialog.setCancelable(true);
                TextView tvReCall = (TextView) sessionMenuView.findViewById(R.id.tvReCall);
                tvReCall.setVisibility(View.GONE);
                TextView tvDelete = (TextView) sessionMenuView.findViewById(R.id.tvDelete);
                TextView tvCopy = (TextView) sessionMenuView.findViewById(R.id.tv_copy);
                tvCopy.setVisibility(View.GONE);
                TextView tvSend = (TextView) sessionMenuView.findViewById(R.id.tv_send);
                TextView tvCollection = (TextView) sessionMenuView.findViewById(R.id.tv_collection);
                tvCollection.setVisibility(View.GONE);

                tvDelete.setOnClickListener(v -> {
                            ApiRetrofit.getInstance().mApi.delCollect(mAdapter.getItem(position)).subscribeOn(Schedulers.newThread())
                                    .subscribe((ReturnResult responseBody) -> {
                                        if (responseBody.success()) {
                                            mSessionMenuDialog.dismiss();
                                            mSessionMenuDialog = null;
                                            mData.remove(position);
                                            mContext.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    mAdapter.notifyDataSetChanged();
                                                }
                                            });

                                        } else {
                                            ToastUtils.showShort("删除失败");
                                        }
                                    }, throwable -> {
                                        mContext.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                ToastUtils.showShort("删除请求失败");
                                            }
                                        });
                                    });
                        }
                );

                tvSend.setOnClickListener(v -> {
                    Intent intent = new Intent(mContext, RelayActivity.class);
                    TiCollection collection = mAdapter.getItem(position);
                    intent.putExtra("drafty", SessionAtPresenter.toDrafty(new Gson().fromJson(collection.getDes(), Drafty.CollectDes.class), collection.getType()));
                    mContext.startActivityForResult(intent, AppConst.REQUEST_CODE_RELAY_MSG);

                    mSessionMenuDialog.dismiss();
                    mSessionMenuDialog = null;
                });
                mSessionMenuDialog.show();
                return false;
            });

            getView().getRvMsg().setAdapter(mAdapter);
        } else {
            mAdapter.notifyDataSetChangedWrapper();
        }
    }

    private int page = 1;
    private String oldKey = null;
    private boolean canLoadMore = true;

    public void initList() {
        httpList("");
    }

    private void httpList(String content) {
        if (!content.equals(oldKey)) {
            page = 1;
            mData.clear();
            canLoadMore = true;
        }
        oldKey = content;

        mContext.showWaitingDialog(UIUtils.getString(R.string.please_wait));
//        ((StoredTopic) getView().getTopic().getLocal()).id
        TiCollection collection = new TiCollection();
//        collection.setTopic(Cache.getTinode().getMyId());
//        collection.setDes(content);
//        collection.setRows(AppConst.ROWS);
//        collection.setPage(page);

        final SharedPreferences pref = android.preference.PreferenceManager.getDefaultSharedPreferences(mContext);
        String login = pref.getString(LoginActivity.PREFS_LAST_LOGIN, null);
        collection.setUserName(login);
        ApiRetrofit.getInstance().mApi.listCollect(collection).subscribeOn(Schedulers.newThread())
                .subscribe((ReturnResult<List<TiCollection>> responseBody) -> {
                    mContext.hideWaitingDialog();
                    if (responseBody.success()) {
                        page++;
                        int size = ((List<TiCollection>) responseBody.getData()).size();
                        if (size > 0) {
                            mData.addAll((List<TiCollection>) responseBody.getData());
                            canLoadMore = size == AppConst.ROWS;

                            mContext.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    setAdapter();
                                    getView().getLlSearch().setVisibility(View.GONE);
                                }
                            });
                        } else {
                            mContext.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    canLoadMore = false;
                                    mContext.hideWaitingDialog();
                                    getView().getRlNoResultTip().setVisibility(View.VISIBLE);
                                    getView().getLlSearch().setVisibility(View.GONE);
                                }
                            });
                        }
                    } else {
                        mContext.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtils.showShort("数据加载失败");
                            }
                        });
                    }
                }, throwable -> {
                    mContext.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mContext.hideWaitingDialog();
                            ToastUtils.showShort("加载失败");
                        }
                    });
                });
//        Observable.just(MessageDb.queryList(BaseDb.getInstance().getWritableDatabase(), 1l, 1, 100, content))
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(messages -> {
//
//                });
//        TiCollection collectionTest = new TiCollection();
//        collection.setId(0);
//        collection.setTopic("usrzCOXhNqihRs");
//        collection.setType(0);
//        collection.setUserName("sss");
//        collection.setDes("{\"txt\":\"ttt\"}");
//        collection.setFromName("贺新");
//        collection.setPage(0);
//        collection.setRows(0);
//        collection.setTs(0);
//        collection.setUserName(System.currentTimeMillis()+"");
//        mData.add(collection);
//        mData.add(collection);
        mContext.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setAdapter();
                getView().getLlSearch().setVisibility(View.GONE);
            }
        });
    }

    public boolean getList() {
        String content = getView().getEtSearchContent().getText().toString();

        if (TextUtils.isEmpty(content.trim())) {
            UIUtils.showToast(UIUtils.getString(R.string.content_no_empty));
            return false;
        }
        httpList(content);
        return canLoadMore;
    }

    private void loadError(Throwable throwable) {
        mContext.hideWaitingDialog();
        LogUtils.sf(throwable.getLocalizedMessage());
    }
}

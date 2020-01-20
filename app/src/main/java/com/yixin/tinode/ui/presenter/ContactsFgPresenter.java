package com.yixin.tinode.ui.presenter;

import android.content.Intent;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.github.promeg.pinyinhelper.Pinyin;
import com.lqr.adapter.LQRAdapterForRecyclerView;
import com.lqr.adapter.LQRHeaderAndFooterAdapter;
import com.lqr.adapter.LQRViewHolderForRecyclerView;
import com.yixin.tinode.R;
import com.yixin.tinode.db.DBManager;
import com.yixin.tinode.db.model.Friend;
import com.yixin.tinode.tinode.Cache;

import co.tinode.tinodesdk.model.AvatarPhoto;

import com.yixin.tinode.tinode.util.UiUtils;
import com.yixin.tinode.ui.activity.SessionActivity;
import com.yixin.tinode.ui.activity.UserInfoActivity;
import com.yixin.tinode.ui.base.BaseActivity;
import com.yixin.tinode.ui.base.BasePresenter;
import com.yixin.tinode.ui.view.IContactsFgView;
import com.yixin.tinode.util.LogUtils;
import com.yixin.tinode.util.PinyinUtils;
import com.yixin.tinode.util.SortUtils;
import com.yixin.tinode.util.UIUtils;
import com.zuozhan.app.activity.AllBaseActivity;

import java.util.ArrayList;
import java.util.List;

import co.tinode.tinodesdk.Topic;
import co.tinode.tinodesdk.model.VCard;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class ContactsFgPresenter extends BasePresenter<IContactsFgView> {

    private List<Friend> mData = new ArrayList<>();
    private LQRHeaderAndFooterAdapter mAdapter;

    public ContactsFgPresenter(AllBaseActivity context) {
        super(context);
    }

    public void loadContacts() {
        setAdapter();
        loadData();
    }

    private void loadData() {
        List<Topic> topics = Cache.getTinode().getFilteredTopics(Topic.TopicType.P2P, null);

        if (topics != null && topics.size() > 0) {
            mData.clear();
            for (Topic tmp : topics) {

                VCard user = (VCard) tmp.getPub();
                if (user == null) {
                    continue;
                }
                Friend friend = new Friend(tmp.getName(), user.fn, user.photo);
                String spelling = Pinyin.toPinyin(friend.getDisplayName(), "");
                friend.setDisplayNameSpelling(spelling);
                friend.setNameSpelling(spelling);
                mData.add(friend);
            }
            getView().getFooterView().setText(UIUtils.getString(R.string.count_of_contacts, mData.size()));
            //整理排序
            SortUtils.sortContacts(mData);
            if (mAdapter != null)
                mAdapter.notifyDataSetChanged();
        }
//        Observable.just(DBManager.getInstance().getFriends())
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(friends -> {
//                    if (friends != null && friends.size() > 0) {
//                        mData.clear();
//                        mData.addAll(friends);
//                        getView().getFooterView().setText(UIUtils.getString(R.string.count_of_contacts, mData.size()));
//                        //整理排序
//                        SortUtils.sortContacts(mData);
//                        if (mAdapter != null)
//                            mAdapter.notifyDataSetChanged();
//                    }
//                }, this::loadError);
    }

    private void setAdapter() {
        if (mAdapter == null) {
            LQRAdapterForRecyclerView adapter = new LQRAdapterForRecyclerView<Friend>(mContext, mData, R.layout.item_contact) {
                @Override
                public void convert(LQRViewHolderForRecyclerView helper, Friend item, int position) {
                    helper.setText(R.id.tvName, item.getDisplayName());

                    ImageView ivHeader = helper.getView(R.id.ivHeader);
                    Bitmap bitmap = item != null && item.getPhoto() != null ? item.getPhoto().getBitmap() : null;
                    UiUtils.assignBitmap(mContext, ivHeader,
                            bitmap,
                            item != null ? item.getDisplayName() : null,
                            item.getUserId());

                    String str = "";
                    String currentLetter = "";
                    //得到当前字母
                    try {
                        currentLetter = item.getDisplayNameSpelling().charAt(0) + "";
                        if (position == 0) {
                            str = currentLetter;
                        } else {
                            //得到上一个字母
                            String preLetter = mData.get(position - 1).getDisplayNameSpelling().charAt(0) + "";
                            //如果和上一个字母的首字母不同则显示字母栏
                            if (!preLetter.equalsIgnoreCase(currentLetter)) {
                                str = currentLetter;
                            }
                        }
                    } catch (Exception e) {
                    }

                    int nextIndex = position + 1;
                    if (nextIndex < mData.size() - 1) {
                        String nextLetter = "";
                        //得到下一个字母
                        try{
                            nextLetter = mData.get(nextIndex).getDisplayNameSpelling().charAt(0) + "";
                        }catch (Exception e){

                        }
                        //如果和下一个字母的首字母不同则隐藏下划线
                        if (!nextLetter.equalsIgnoreCase(currentLetter)) {
                            helper.setViewVisibility(R.id.vLine, View.INVISIBLE);
                        } else {
                            helper.setViewVisibility(R.id.vLine, View.VISIBLE);
                        }
                    } else {
                        helper.setViewVisibility(R.id.vLine, View.INVISIBLE);
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
            adapter.addFooterView(getView().getFooterView());
            mAdapter = adapter.getHeaderAndFooterAdapter();
            getView().getRvContacts().setAdapter(mAdapter);
        }
        ((LQRAdapterForRecyclerView) mAdapter.getInnerAdapter()).setOnItemClickListener((lqrViewHolder, viewGroup, view, i) -> {
            Intent intent = new Intent(mContext, UserInfoActivity.class);
//            intent.putExtra("userInfo", DBManager.getInstance().getUserInfo(mData.get(i - 1).getUserId()));//-1是因为有头部
            intent.putExtra("topic", mData.get(i - 1).getUserId());
            mContext.jumpToActivity(intent);
        });
    }

    private void loadError(Throwable throwable) {
        LogUtils.e(throwable.getLocalizedMessage());
        UIUtils.showToast(UIUtils.getString(R.string.load_contacts_error));
    }
}

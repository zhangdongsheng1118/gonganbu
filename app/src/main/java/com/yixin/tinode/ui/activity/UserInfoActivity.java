package com.yixin.tinode.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.lqr.optionitemview.OptionItemView;
import com.yixin.tinode.R;
import com.yixin.tinode.app.AppConst;
import com.yixin.tinode.db.tinode.BaseDb;
import com.yixin.tinode.db.tinode.StoredTopic;
import com.yixin.tinode.db.tinode.TopicDb;
import com.yixin.tinode.manager.BroadcastManager;
import com.yixin.tinode.tinode.Cache;
import com.yixin.tinode.ui.base.BaseActivity;
import com.yixin.tinode.ui.base.BasePresenter;
import com.yixin.tinode.util.LogUtils;
import com.yixin.tinode.util.UIUtils;
import com.zuozhan.app.RouterUtil;
import com.zuozhan.app.bean.MyUtil;
import com.zuozhan.app.bean.UserBean;
import com.zuozhan.app.httpUtils.HttpUtil;
import com.zuozhan.app.imageloader.ImageLoader;

import butterknife.BindView;
import co.tinode.tinodesdk.ComTopic;
import co.tinode.tinodesdk.NotConnectedException;
import co.tinode.tinodesdk.PromisedReply;
import co.tinode.tinodesdk.Topic;
import co.tinode.tinodesdk.model.ServerMessage;
import co.tinode.tinodesdk.model.VCard;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @创建者 CSDN_LQR
 * @描述 用户信息界面
 */
public class UserInfoActivity extends BaseActivity {

//    Topic mUserInfo;

    @BindView(R.id.ibToolbarMore)
    ImageButton mIbToolbarMore;

    @BindView(R.id.ivHeader)
    ImageView mIvHeader;
    @BindView(R.id.title_left)
    ImageView title_left;
    @BindView(R.id.tvName)
    TextView mTvName;
    @BindView(R.id.ivGender)
    ImageView mIvGender;
    @BindView(R.id.tvAccount)
    TextView mTvAccount;
    @BindView(R.id.tv_phone)
    TextView tv_phone;
    @BindView(R.id.tvNickName)
    TextView mTvNickName;
    @BindView(R.id.tvArea)
    TextView mTvArea;
    @BindView(R.id.tvSignature)
    TextView mTvSignature;

    @BindView(R.id.oivAliasAndTag)
    OptionItemView mOivAliasAndTag;
    @BindView(R.id.llArea)
    LinearLayout mLlArea;
    @BindView(R.id.llSignature)
    LinearLayout mLlSignature;

    @BindView(R.id.btnCheat)
    View mBtnCheat;
    @BindView(R.id.btnAddToVideo)
    View btnAddToVideo;
    @BindView(R.id.btnAudioCheat)
    View btnAudioCheat;
    @BindView(R.id.mBtnAddToContact)
    View mBtnAddToContact;

    @BindView(R.id.rlMenu)
    RelativeLayout mRlMenu;
    @BindView(R.id.svMenu)
    ScrollView mSvMenu;
    @BindView(R.id.oivAlias)
    OptionItemView mOivAlias;
    @BindView(R.id.oivDelete)
    OptionItemView mOivDelete;

    @Override
    public void init() {

        registerBR();
    }

    private void loadUserError(Throwable throwable) {
        LogUtils.sf(throwable.getLocalizedMessage());
    }

    UserBean userBean;

    String topicTmp;

    @Override
    public void initView() {
        mIbToolbarMore.setVisibility(View.GONE);
        title_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        Intent intent = getIntent();
        topicTmp = (String) intent.getExtras().getSerializable("topic");
        if (topicTmp == null) {
            return;
        }
//        if (TextUtils.isEmpty(topicTmp)) {
//            Subscription subscription = (Subscription) intent.getExtras().getSerializable("subscription");
//            subscription.topic = subscription.user;
//            topicTmp = subscription.user;
//        } else {
//        }

        HttpUtil.getUserBySUid(topicTmp, new HttpUtil.Callback<UserBean>() {
            @Override
            public void onResponse(UserBean call) {
                userBean = call;
                initUser(userBean);
            }
        });


//        Observable.just(TopicDb.readOne(BaseDb.getInstance().getWritableDatabase(), Cache.getTinode(), topicTmp))
//                // 不能用,代码写的问题多台
////        Observable.just(UserDb.readOne(BaseDb.getInstance().getWritableDatabase(), topicTmp))
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(subscription -> {
//                    if (subscription == null) {
//                        Subscription sub = (Subscription) intent.getExtras().getSerializable("subscription");
//                        sub.topic = sub.user;
//                        mUserInfo =new Topic();
//                        mUserInfo.merge(sub);
//                        mUserInfo.uid=sub.user;
//                        initUser(mUserInfo);
//                    } else {
//                        mUserInfo = subscription;
//                        initUser(mUserInfo);
//                    }
//                }, this::loadUserError);
    }

    private void initUser(UserBean topic) {
//        VCard pub = (VCard) topic.getPub();
//        UiUtils.assignBitmap(context, mIvHeader,
//                pub != null ? pub.getBitmap() : null,
//                pub != null ? pub.fn : null,
//                topic.getName());
        if (topic == null || topic.data == null) {
            return;
        }
        ImageLoader.loadImage(this,mIvHeader,topic.data.headPic);

//        Glide.with(this).load(DBManager.getInstance().getPortraitUri(mUserInfo)).centerCrop().into(mIvHeader);
        MyUtil.setText(mTvName, topic.data.realName, "");
        MyUtil.setText(mTvAccount, topic.data.duty, "职位：");
        MyUtil.setText(tv_phone, topic.data.phone, "手机号：");
        MyUtil.setText(mTvNickName, topic.data.departmentName, "部门：");
//        Tinode tinode = Cache.getTinode();
//        Topic mFriend = TopicDb.readOne(BaseDb.getInstance().getReadableDatabase(), tinode, mUserInfo.getName());
//        if (mFriend == null) {//陌生人
//            mBtnCheat.setVisibility(View.GONE);
//            mBtnAddToContact.setVisibility(View.VISIBLE);
//            btnAudioCheat.setVisibility(View.GONE);
//            btnAddToVideo.setVisibility(View.GONE);
//            mTvNickName.setVisibility(View.INVISIBLE);
//        } else {
        mBtnCheat.setVisibility(View.VISIBLE);
        mBtnAddToContact.setVisibility(View.GONE);
        btnAudioCheat.setVisibility(View.VISIBLE);
        btnAddToVideo.setVisibility(View.VISIBLE);
//            if (tinode.isMe(mUserInfo.getName())) {//我
//                mTvNickName.setVisibility(View.INVISIBLE);
//                mOivAliasAndTag.setVisibility(View.GONE);
//                mLlArea.setVisibility(View.GONE);
//                mLlSignature.setVisibility(View.GONE);
//            } else {//我的朋友
//                String nickName = pub != null ? pub.fn : null;
//                mTvName.setText(nickName);
//                if (TextUtils.isEmpty(nickName)) {
//                    mTvNickName.setVisibility(View.INVISIBLE);
//                } else {
//                    mTvNickName.setText(UIUtils.getString(R.string.nickname_colon, nickName));
//                }
//            }
//        }
    }

    @Override
    public void initData() {
//        initUser();
    }

    @Override
    public void initListener() {
        mIbToolbarMore.setOnClickListener(v -> showMenu());
        mOivAliasAndTag.setOnClickListener(v -> jumpToSetAlias());

        mBtnCheat.setOnClickListener(v -> {
            //如果之前已删除，置为显示
            Observable.just(TopicDb.readOne(BaseDb.getInstance().getWritableDatabase(), Cache.getTinode(), topicTmp))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(subscription -> {
                        if (subscription != null) {
                            subscription.setState(Topic.STATE_NORMAL);
                            TopicDb.updateState(BaseDb.getInstance().getWritableDatabase(), ((StoredTopic) subscription.getLocal()).id, Topic.STATE_NORMAL);
                        }
                    }, this::loadUserError);

            Intent intent = new Intent(UserInfoActivity.this, SessionActivity.class);
            intent.putExtra("sessionId", topicTmp);
            intent.putExtra("sessionType", SessionActivity.SESSION_TYPE_PRIVATE);
            jumpToActivity(intent);
            finish();
        });
        btnAudioCheat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userBean == null || userBean.data == null) {
                    return;
                }
//                RouterUtil.goAudioActivity(UserInfoActivity.this,  "771388");
                RouterUtil.goAudioActivity(UserInfoActivity.this, userBean.data.id + "");
            }
        });

        btnAddToVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userBean == null || userBean.data == null) {
                    return;
                }
                RouterUtil.goVideoActivity(UserInfoActivity.this, userBean.data.id + "");
//                RouterUtil.goVideoActivity(UserInfoActivity.this, "771388");
            }
        });

        mBtnAddToContact.setOnClickListener(v -> {
            //跳转到写附言界面
//            Intent intent = new Intent(UserInfoActivity.this, PostScriptActivity.class);
//            intent.putExtra("userId", mUserInfo.getUserId());
//            jumpToActivity(intent);

            final ComTopic<VCard> topic = new ComTopic<>(Cache.getTinode(), topicTmp, (Topic.Listener) null);
//            topic.setPub(new VCard(title, avatar));
//            topic.setPriv(subtitle);
            try {
                topic.subscribe().thenApply(new PromisedReply.SuccessListener<ServerMessage>() {
                    @Override
                    public PromisedReply<ServerMessage> onSuccess(ServerMessage result) throws Exception {
                        Intent intent = new Intent(UserInfoActivity.this, SessionActivity.class);
                        intent.putExtra("sessionId", topicTmp);
                        intent.putExtra("sessionType", SessionActivity.SESSION_TYPE_PRIVATE);
                        jumpToActivity(intent);

                        return null;
                    }
                }, new PromisedReply.FailureListener<ServerMessage>() {
                    @Override
                    public PromisedReply<ServerMessage> onFailure(final Exception err) {
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (err instanceof NotConnectedException) {
                                    Toast.makeText(context, R.string.no_connection, Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(context, R.string.action_failed, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        return null;
                    }
                });
            } catch (NotConnectedException ignored) {
                Toast.makeText(context, R.string.no_connection, Toast.LENGTH_SHORT).show();
                // Go back to contacts
            } catch (Exception e) {
                Toast.makeText(context, R.string.failed_to_create_topic, Toast.LENGTH_SHORT).show();
            }

        });

        mRlMenu.setOnClickListener(v -> hideMenu());

        mOivAlias.setOnClickListener(v -> {
            jumpToSetAlias();
            hideMenu();
        });
        mOivDelete.setOnClickListener(v -> {
            if (userBean == null || userBean.data == null) {
                return;
            }
            hideMenu();
            showMaterialDialog(UIUtils.getString(R.string.delete_contact),
                    UIUtils.getString(R.string.delete_contact_content, userBean.data.realName),
                    UIUtils.getString(R.string.delete),
                    UIUtils.getString(R.string.cancel),
                    v1 -> {
//                        ApiRetrofit.getInstance()
//                                .deleteFriend(mUserInfo.getUserId())
//                                .subscribeOn(Schedulers.io())
//                                .observeOn(AndroidSchedulers.mainThread())
//                                .subscribe(deleteFriendResponse -> {
//                                    hideMaterialDialog();
//                                    if (deleteFriendResponse.getCode() == 200) {
//                                        RongIMClient.getInstance().getConversation(Conversation.ConversationType.PRIVATE, mUserInfo.getUserId(), new RongIMClient.ResultCallback<Conversation>() {
//                                            @Override
//                                            public void onSuccess(Conversation conversation) {
//                                                RongIMClient.getInstance().clearMessages(Conversation.ConversationType.PRIVATE, mUserInfo.getUserId(), new RongIMClient.ResultCallback<Boolean>() {
//                                                    @Override
//                                                    public void onSuccess(Boolean aBoolean) {
//                                                        RongIMClient.getInstance().removeConversation(Conversation.ConversationType.PRIVATE, mUserInfo.getUserId(), null);
//                                                    }
//
//                                                    @Override
//                                                    public void onError(RongIMClient.ErrorCode errorCode) {
//
//                                                    }
//                                                });
//                                            }
//
//                                            @Override
//                                            public void onError(RongIMClient.ErrorCode errorCode) {
//
//                                            }
//                                        });
//                                        //通知对方被删除(把我的id发给对方)
//                                        DeleteContactMessage deleteContactMessage = DeleteContactMessage.obtain(UserCache.getId());
//                                        RongIMClient.getInstance().sendMessage(Message.obtain(mUserInfo.getUserId(), Conversation.ConversationType.PRIVATE, deleteContactMessage), "", "", null, null);
//                                        DBManager.getInstance().deleteFriendById(mUserInfo.getUserId());
//                                        UIUtils.showToast(UIUtils.getString(R.string.delete_success));
//                                        BroadcastManager.getInstance(UserInfoActivity.this).sendBroadcast(AppConst.UPDATE_FRIEND);
//                                        finish();
//                                    } else {
//                                        UIUtils.showToast(UIUtils.getString(R.string.delete_fail));
//                                    }
//                                }, this::loadError);
                    }
                    , v2 -> {
                        hideMaterialDialog();
                    });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unRegisterBR();
    }

    private void loadError(Throwable throwable) {
        hideMaterialDialog();
        LogUtils.sf(throwable.getLocalizedMessage());
    }

    private void jumpToSetAlias() {
        Intent intent = new Intent(this, SetAliasActivity.class);
//        intent.putExtra("userId", mUserInfo.getUserId());
        jumpToActivity(intent);
    }

    private void showMenu() {
        mRlMenu.setVisibility(View.VISIBLE);
        TranslateAnimation ta = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 1, Animation.RELATIVE_TO_SELF, 0);
        ta.setDuration(200);
        mSvMenu.startAnimation(ta);
    }

    private void hideMenu() {
        TranslateAnimation ta = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 1);
        ta.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mRlMenu.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        ta.setDuration(200);
        mSvMenu.startAnimation(ta);
    }

    private void registerBR() {
        BroadcastManager.getInstance(this).register(AppConst.CHANGE_INFO_FOR_USER_INFO, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
//                mUserInfo = DBManager.getInstance().getUserInfo(mUserInfo.getUserId());
                initData();
            }
        });
    }

    private void unRegisterBR() {
        BroadcastManager.getInstance(this).unregister(AppConst.CHANGE_INFO_FOR_USER_INFO);
    }

    @Override
    protected BasePresenter createPresenter() {
        return null;
    }

    @Override
    protected int provideContentViewId() {
        return R.layout.activity_user_info;
    }
}

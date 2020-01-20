package com.yixin.tinode.ui.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lqr.audio.AudioRecordManager;
import com.lqr.audio.IAudioRecordListener;
import com.lqr.emoji.EmotionKeyboard;
import com.lqr.emoji.EmotionLayout;
import com.lqr.emoji.IEmotionExtClickListener;
import com.lqr.emoji.IEmotionSelectedListener;
import com.lqr.recyclerview.LQRRecyclerView;
import com.yixin.tinode.R;
import com.yixin.tinode.api.param.TiSetting;
import com.yixin.tinode.app.AppConst;
import com.yixin.tinode.db.tinode.StoredMessage;
import com.yixin.tinode.manager.BroadcastManager;
import com.yixin.tinode.model.data.LocationData;
import com.yixin.tinode.tinode.Cache;
import com.yixin.tinode.tinode.PausableSingleThreadExecutor;
import com.yixin.tinode.tinode.account.Utils;
import com.yixin.tinode.tinode.util.UiUtils;
import com.yixin.tinode.ui.adapter.SessionAdapter;
import com.yixin.tinode.ui.base.BaseActivity;
import com.yixin.tinode.ui.presenter.SessionAtPresenter;
import com.yixin.tinode.ui.service.BackgroundService;
import com.yixin.tinode.ui.view.ISessionAtView;
import com.yixin.tinode.util.UIUtils;
import com.zuozhan.app.activity.ZHLocationActivity;
import com.zuozhan.app.photo.imagepicker.ImagePicker;
import com.zuozhan.app.photo.imagepicker.bean.ImageItem;
import com.zuozhan.app.photo.imagepicker.ui.ImageGridActivity;
import com.zuozhan.app.photo.imagepicker.ui.ImagePreviewActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import butterknife.BindView;
import cn.bingoogolapple.refreshlayout.BGANormalRefreshViewHolder;
import cn.bingoogolapple.refreshlayout.BGARefreshLayout;
import co.tinode.tinodesdk.ComTopic;
import co.tinode.tinodesdk.NotConnectedException;
import co.tinode.tinodesdk.PromisedReply;
import co.tinode.tinodesdk.Tinode;
import co.tinode.tinodesdk.Topic;
import co.tinode.tinodesdk.model.Description;
import co.tinode.tinodesdk.model.MsgServerData;
import co.tinode.tinodesdk.model.MsgServerInfo;
import co.tinode.tinodesdk.model.MsgServerMeta;
import co.tinode.tinodesdk.model.MsgServerPres;
import co.tinode.tinodesdk.model.ServerMessage;
import co.tinode.tinodesdk.model.Subscription;
import co.tinode.tinodesdk.model.VCard;

import static com.lqr.emoji.EmotionKeyboard.getSoftButtonsBarHeight;
import static com.yixin.tinode.app.AppConst.ACTION_ATTACH_FILE;
import static com.yixin.tinode.app.AppConst.ACTION_ATTACH_IMAGE;
import static com.yixin.tinode.app.AppConst.AV_REQUEST;
import static com.yixin.tinode.util.Utils.antiShakeUtil;
import static com.yixin.tinode.util.Utils.showNetworkAlert;

/**
 * @创建者 CSDN_LQR
 * @描述 会话界面（单聊、群聊）
 */
public class SessionActivity extends BaseActivity<ISessionAtView, SessionAtPresenter> implements ISessionAtView, IEmotionSelectedListener, BGARefreshLayout.BGARefreshLayoutDelegate {
    public enum CURRENT_ACTION {
        DESTROY, TOVIDEOROOM, TOLIVESTREAMING, RECORDVOICE, TAKEPHOTO
    }

    private String TAG = SessionActivity.class.getSimpleName();

    public final static int SESSION_TYPE_PRIVATE = 1;
    public final static int SESSION_TYPE_GROUP = 2;

    private String mTopicName = "";
    private boolean mIsFirst = false;
    private Topic.TopicType mConversationType = Topic.TopicType.P2P;
    private MediaPlayer mMediaPlayer = null;
    private boolean mPTTOff = true;

    CURRENT_ACTION mCurAction = CURRENT_ACTION.DESTROY;
    @BindView(R.id.ibToolbarMore)
    ImageButton mIbToolbarMore;
    @BindView(R.id.ivMap)
    ImageButton ivMap;
    @BindView(R.id.ivUploadVideo)
    ImageButton ivUploadVideo;
    @BindView(R.id.btnFastTakePhoto)
    ImageButton btnFastTakePhoto;

    @BindView(R.id.llRoot)
    LinearLayout mLlRoot;
    @BindView(R.id.llContent)
    LinearLayout mLlContent;
    @BindView(R.id.refreshLayout)
    BGARefreshLayout mRefreshLayout;
    @BindView(R.id.rvMsg)
    LQRRecyclerView mRvMsg;

    @BindView(R.id.ivAudio)
    ImageView mIvAudio;
    @BindView(R.id.btnAudio)
    Button mBtnAudio;
    @BindView(R.id.etContent)
    EditText mEtContent;
    @BindView(R.id.ivEmo)
    ImageView mIvEmo;
    @BindView(R.id.ivMore)
    ImageView mIvMore;
    @BindView(R.id.btnSend)
    Button mBtnSend;

    @BindView(R.id.flEmotionView)
    FrameLayout mFlEmotionView;
    @BindView(R.id.elEmotion)
    EmotionLayout mElEmotion;
    @BindView(R.id.llMore)
    LinearLayout mLlMore;

    @BindView(R.id.rlAlbum)
    RelativeLayout mRlAlbum;
    @BindView(R.id.rlTakePhoto)
    RelativeLayout mRlTakePhoto;
    @BindView(R.id.rlLocation)
    RelativeLayout mRlLocation;
    @BindView(R.id.rlFile)
    RelativeLayout mRlFile;
    //    @BindView(R.id.rlMeeting)
//    RelativeLayout mRlMeeting;
    //added by pcg
//    @BindView(R.id.fabPTT)
//    MovableFloatingActionButton mFabPTT;
    @BindView(R.id.rootLayout)
    FrameLayout mRootLayout;

    private EmotionKeyboard mEmotionKeyboard;

    ////tinode
    // How long a typing indicator should play its animation, milliseconds.
    private static final int TYPING_INDICATOR_DURATION = 4000;
    private Timer mTypingAnimationTimer;
    private String mMessageText = null;
    private Topic mTopic = null;
    private PausableSingleThreadExecutor mMessageSender = null;
    private PromisedReply.FailureListener<ServerMessage> mFailureListener;

    private long mSearchStartMsgId;
    //AV
    private boolean isError;
    private Toast logToast;
    private String selfUid = "123";
    private long callStartedTimeMs = 0;
    private long pushSuccessTimeMs = 0;
    private long recvSuccessTimeMs = 0;
    private boolean establishTimeDisplayed;
    //文件发送方式
    private TiSetting settingSendType;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        initTopic(intent);
    }

    private void initTopic(Intent intent) {
//        if (mTopic != null && mTopic.isAttached()) {
//            try {
//                mTopic.leave();
//            } catch (Exception ex) {
//                Log.e(TAG, "something went wrong in Topic.leave", ex);
//            }
//        }
        mTopicName = intent.getStringExtra("sessionId");

        int sessionType = intent.getIntExtra("sessionType", SESSION_TYPE_PRIVATE);
        switch (sessionType) {
            case SESSION_TYPE_PRIVATE:
                mConversationType = Topic.TopicType.P2P;
                break;
            case SESSION_TYPE_GROUP:
                mConversationType = Topic.TopicType.GRP;
                break;
        }

        if (intent.hasExtra("firstMsgId")) {
            mSearchStartMsgId = intent.getLongExtra("firstMsgId", 0);
        }
        mMessageText = intent.getStringExtra(Intent.EXTRA_TEXT);
    }

    @Override
    public void init() {
        initTopic(getIntent());

        initAudioRecordManager();
        //设置会话已读
        registerBR();

        mMessageSender = new PausableSingleThreadExecutor();
        mMessageSender.pause();
        mFailureListener = new UiUtils.ToastFailureListener(this);

        //TODO 通知未完成  还需要写筛选取消
        NotificationManager nm = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancelAll();
    }

    @Override
    public void initView() {
        mIbToolbarMore.setImageResource(R.mipmap.ic_session_info);
        mIbToolbarMore.setVisibility(View.VISIBLE);

        boolean isP2P = mConversationType == Topic.TopicType.P2P;
        ivUploadVideo.setVisibility(isP2P ? View.INVISIBLE : View.INVISIBLE);
        btnFastTakePhoto.setVisibility(isP2P ? View.INVISIBLE : View.INVISIBLE);
        ivMap.setVisibility(isP2P ? View.INVISIBLE : View.INVISIBLE);
        //mFabPTT.setVisibility(isP2P ? View.INVISIBLE : View.VISIBLE);

        mElEmotion.attachEditText(mEtContent);
        initEmotionKeyboard();
        initRefreshLayout();

//        mRlLocation.setVisibility(View.INVISIBLE);

        settingSendType = SettingNewMsgNotifyActivity.getSettingNotify(this);
        //TODO 先写死为tinode方式,以后再选择seaweed
        settingSendType.setSendFileType(AppConst.SEND_FILE_TYPE_TINODE);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void initListener() {
        ivUploadVideo.setOnClickListener(v -> {
            ToastUtils.showShort("这个按钮是视频上传！");
        });
        ivMap.setOnClickListener(v -> {
            ToastUtils.showShort("这个按钮是地图！");
            Intent intent = new Intent(SessionActivity.this, MapActivity.class);
            intent.putExtra("TopicName", mTopicName);
            startActivity(intent);
        });
        btnFastTakePhoto.setOnClickListener(v -> {
            ToastUtils.showShort("这个按钮是快速取证！");
            Intent intent = new Intent(SessionActivity.this, TakePhotoActivity.class);
            startActivityForResult(intent, AppConst.REQUEST_TAKE_PHOTO);
        });
        mIbToolbarMore.setOnClickListener(v -> {
            if (antiShakeUtil.check()) return;
            Intent intent = new Intent(SessionActivity.this, SessionInfoActivity.class);
            intent.putExtra("sessionId", mTopicName);
            intent.putExtra("sessionType", mConversationType == Topic.TopicType.P2P ? SessionActivity.SESSION_TYPE_PRIVATE : SessionActivity.SESSION_TYPE_GROUP);
            jumpToActivity(intent);
        });
        mElEmotion.setEmotionSelectedListener(this);
        mElEmotion.setEmotionAddVisiable(true);
        mElEmotion.setEmotionSettingVisiable(true);
        mElEmotion.setEmotionExtClickListener(new IEmotionExtClickListener() {
            @Override
            public void onEmotionAddClick(View view) {
                UIUtils.showToast("add");
            }

            @Override
            public void onEmotionSettingClick(View view) {
                UIUtils.showToast("setting");
            }
        });

        mRvMsg.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        closeBottomAndKeyboard();
                        break;
                }
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });

        mIvAudio.setOnClickListener(v -> {
            if (mBtnAudio.isShown()) {
                hideAudioButton();
                mEtContent.requestFocus();
                if (mEmotionKeyboard != null) {
                    mEmotionKeyboard.showSoftInput();
                }
            } else {
                mEtContent.clearFocus();
                showAudioButton();
                hideEmotionLayout();
                hideMoreLayout();
            }
            //            UIUtils.postTaskDelay(() -> mRvMsg.smoothMoveToPosition(mRvMsg.getAdapter().getItemCount() - 1), 50);
        });
        mEtContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mEtContent.getText().toString().trim().length() > 0) {
                    mBtnSend.setVisibility(View.VISIBLE);
                    mIvMore.setVisibility(View.GONE);
                    //                    RongIMClient.getInstance().sendTypingStatus(mConversationType, mTopicName, TextMessage.class.getAnnotation(MessageTag.class).value());
                } else {
                    mBtnSend.setVisibility(View.GONE);
                    mIvMore.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mEtContent.setOnFocusChangeListener((v, hasFocus) -> {
            //            if (hasFocus) {
            //                UIUtils.postTaskDelay(() -> mRvMsg.smoothMoveToPosition(mRvMsg.getAdapter().getItemCount() - 1), 50);
            //            }
        });
        mBtnSend.setOnClickListener(v -> mPresenter.sendTextMsg());
        mBtnAudio.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    /*if(mConversationType==Topic.TopicType.P2P){
                        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                            this.requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 11);
                        }else {
                            AudioRecordManager.getInstance(SessionActivity.this).startRecord();
                        }
                    }*/
                    //else{
                    mCurAction = CURRENT_ACTION.RECORDVOICE;
                    if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                        this.requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 11);
                    }

                    AudioRecordManager.getInstance(SessionActivity.this).startRecord();

                    //}
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (isCancelled(v, event)) {
                        AudioRecordManager.getInstance(SessionActivity.this).willCancelRecord();
                    } else {
                        AudioRecordManager.getInstance(SessionActivity.this).continueRecord();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    AudioRecordManager.getInstance(SessionActivity.this).stopRecord();
                    AudioRecordManager.getInstance(SessionActivity.this).destroyRecord();
                    break;
            }
            return false;
        });

        mRlAlbum.setOnClickListener(v -> {
            if (antiShakeUtil.check()) return;
            Intent intent = new Intent(this, ImageGridActivity.class);
            startActivityForResult(intent, AppConst.REQUEST_IMAGE_PICKER);
        });

        mRlTakePhoto.setOnClickListener(v -> {
            if (antiShakeUtil.check()) return;
            mCurAction = CURRENT_ACTION.TAKEPHOTO;
            Intent intent = new Intent(SessionActivity.this, TakePhotoActivity.class);
            startActivityForResult(intent, AppConst.REQUEST_TAKE_PHOTO);
        });

        mRlLocation.setOnClickListener(v -> {
            Intent intent = new Intent(SessionActivity.this, ZHLocationActivity.class);
            startActivityForResult(intent, 111);
//            mPresenter.sendLocationMessage();
        });


        mRlFile.setOnClickListener(v -> {
            if (antiShakeUtil.check()) return;
            mPresenter.openFileSelector("*/*", R.string.select_file, AppConst.REQUEST_FILE);
        });

//        mFabPTT.SetOnMyTouchListener(new MovableFloatingActionButton.onMyTouchListener() {
//            @Override
//            public void onTouchDown() {
//                //push voice
//                if (mMediaPlayer == null) {
//                    mMediaPlayer = MediaPlayer.create(SessionActivity.this, R.raw.ring2);
//                }
//                mMediaPlayer.seekTo(0);
//                mMediaPlayer.start();
//            }
//
//            @Override
//            public void onTouchMove() {
//
//            }
//
//            @Override
//            public void onTouchUp() {
//                if (mMediaPlayer != null) {
//                    mMediaPlayer.pause();
//                }
//            }
//        });
    }

    private void initAudioRecordManager() {
        AudioRecordManager.getInstance(this).setMaxVoiceDuration(AppConst.DEFAULT_MAX_AUDIO_RECORD_TIME_SECOND);
        File audioDir = new File(AppConst.AUDIO_SAVE_DIR);
        if (!audioDir.exists()) {
            audioDir.mkdirs();
        }
        AudioRecordManager.getInstance(this).setAudioSavePath(audioDir.getAbsolutePath());
        AudioRecordManager.getInstance(this).setAudioRecordListener(new IAudioRecordListener() {
            private TextView mTimerTV;
            private TextView mStateTV;
            private ImageView mStateIV;
            private PopupWindow mRecordWindow;

            @Override
            public void initTipView() {
                View view = View.inflate(SessionActivity.this, R.layout.popup_audio_wi_vo, null);
                mStateIV = (ImageView) view.findViewById(R.id.rc_audio_state_image);
                mStateTV = (TextView) view.findViewById(R.id.rc_audio_state_text);
                mTimerTV = (TextView) view.findViewById(R.id.rc_audio_timer);
                mRecordWindow = new PopupWindow(view, -1, -1);
                mRecordWindow.showAtLocation(mLlRoot, 17, 0, 0);
                mRecordWindow.setFocusable(true);
                mRecordWindow.setOutsideTouchable(false);
                mRecordWindow.setTouchable(false);
            }

            @Override
            public void setTimeoutTipView(int counter) {
                if (this.mRecordWindow != null) {
                    this.mStateIV.setVisibility(View.GONE);
                    this.mStateTV.setVisibility(View.VISIBLE);
                    this.mStateTV.setText(R.string.voice_rec);
                    this.mStateTV.setBackgroundResource(R.drawable.bg_voice_popup);
                    this.mTimerTV.setText(String.format("%s", new Object[]{Integer.valueOf(counter)}));
                    this.mTimerTV.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void setRecordingTipView() {
                if (this.mRecordWindow != null) {
                    this.mStateIV.setVisibility(View.VISIBLE);
                    this.mStateIV.setImageResource(R.mipmap.ic_volume_1);
                    this.mStateTV.setVisibility(View.VISIBLE);
                    this.mStateTV.setText(R.string.voice_rec);
                    this.mStateTV.setBackgroundResource(R.drawable.bg_voice_popup);
                    this.mTimerTV.setVisibility(View.GONE);
                }
            }

            @Override
            public void setAudioShortTipView() {
                if (this.mRecordWindow != null) {
                    mStateIV.setImageResource(R.mipmap.ic_volume_wraning);
                    mStateTV.setText(R.string.voice_short);
                }
            }

            @Override
            public void setCancelTipView() {
                if (this.mRecordWindow != null) {
                    this.mTimerTV.setVisibility(View.GONE);
                    this.mStateIV.setVisibility(View.VISIBLE);
                    this.mStateIV.setImageResource(R.mipmap.ic_volume_cancel);
                    this.mStateTV.setVisibility(View.VISIBLE);
                    this.mStateTV.setText(R.string.voice_cancel);
                    this.mStateTV.setBackgroundResource(R.drawable.corner_voice_style);
                }
            }

            @Override
            public void destroyTipView() {
                if (this.mRecordWindow != null) {
                    this.mRecordWindow.dismiss();
                    this.mRecordWindow = null;
                    this.mStateIV = null;
                    this.mStateTV = null;
                    this.mTimerTV = null;
                }
            }

            @Override
            public void onStartRecord() {
                //                RongIMClient.getInstance().sendTypingStatus(mConversationType, mTopicName, VoiceMessage.class.getAnnotation(MessageTag.class).value());
            }

            @Override
            public void onFinish(Uri audioPath, int duration) {
                if (audioPath == null) {
                    return;
                }
                //发送文件
                File file = new File(audioPath.getPath());
                if (file.exists()) {
                    sendAudioFile(file, duration);
                }
            }

            @Override
            public void onAudioDBChanged(int db) {
                switch (db / 5) {
                    case 0:
                        this.mStateIV.setImageResource(R.mipmap.ic_volume_1);
                        break;
                    case 1:
                        this.mStateIV.setImageResource(R.mipmap.ic_volume_2);
                        break;
                    case 2:
                        this.mStateIV.setImageResource(R.mipmap.ic_volume_3);
                        break;
                    case 3:
                        this.mStateIV.setImageResource(R.mipmap.ic_volume_4);
                        break;
                    case 4:
                        this.mStateIV.setImageResource(R.mipmap.ic_volume_5);
                        break;
                    case 5:
                        this.mStateIV.setImageResource(R.mipmap.ic_volume_6);
                        break;
                    case 6:
                        this.mStateIV.setImageResource(R.mipmap.ic_volume_7);
                        break;
                    default:
                        this.mStateIV.setImageResource(R.mipmap.ic_volume_8);
                }
            }
        });
    }

    private boolean isCancelled(View view, MotionEvent event) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);

        if (event.getRawX() < location[0] || event.getRawX() > location[0] + view.getWidth()
                || event.getRawY() < location[1] - 40) {
            return true;
        }

        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();

        final Tinode tinode = Cache.getTinode();
        tinode.setListener(new UiUtils.EventListener(this, tinode.isConnected()));

        if (TextUtils.isEmpty(mTopicName)) {
            // mTopicName is empty, so this is an external intent
            Intent intent = getIntent();
            Uri contactUri = intent.getData();
            mMessageText = intent.getStringExtra(Intent.EXTRA_TEXT);
            if (contactUri != null) {
                Cursor cursor = getContentResolver().query(contactUri,
                        new String[]{Utils.DATA_PID}, null, null, null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        mTopicName = cursor.getString(cursor.getColumnIndex(Utils.DATA_PID));
                    }
                    cursor.close();
                }
            }
        }

        if (TextUtils.isEmpty(mTopicName)) {
            Log.e(TAG, "Activity resumed with an empty topic name");
            finish();
            return;
        } else {
            Log.d(TAG, "Activity resumed with topic=" + mTopicName);
        }

        // Cancel all pending notifications addressed to the current topic
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancel(mTopicName, 0);

        // Get a known topic.
        mTopic = tinode.getTopic(mTopicName);
        if (mTopic != null) {
            VCard vcard = (VCard) mTopic.getPub();
            if (vcard != null) {
                setToolbarTitle(vcard.fn);
            }
            //            UiUtils.setupToolbar(this, mTopic.getPub(), mTopicName, mTopic.getOnline());
        } else {
            // New topic by name, either an actual grp* or p2p* topic name or a usr*
            Log.e(TAG, "Attempt to instantiate an unknown topic: " + mTopicName);
            mTopic = (ComTopic<VCard>) tinode.newTopic(mTopicName, null);
        }
        mTopic.setListener(new TListener());

        subscribe(tinode);

        mPresenter.resetDraft();
        mPresenter.onResume();

        if (!mIsFirst) {
            mEtContent.clearFocus();
        } else {
            mIsFirst = false;
        }
    }

    private void subscribe(Tinode tinode) {
        if (!mTopic.isAttached()) {
            try {
                mTopic.subscribe(null,
                        mTopic.getMetaGetBuilder()
                                .withGetDesc()
                                .withGetSub()
                                .withGetLaterData2(20)      // for msg fetch
                                .withGetDel()
                                .build()).thenApply(new PromisedReply.SuccessListener<ServerMessage>() {
                    @Override
                    public PromisedReply<ServerMessage> onSuccess(ServerMessage result) throws Exception {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                VCard vcard = (VCard) mTopic.getPub();
                                if (vcard != null) {
                                    setToolbarTitle(vcard.fn);
                                }
                            }
                        });

                        mMessageSender.resume();
                        // Submit unsent messages for processing.
                        mMessageSender.submit(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    PromisedReply last = mTopic.publishPending();
                                    if (last.getResult() != null) {
                                        //发送位置信息，返回activity时，tinode没有连接上导致发送失败，发送成功后没有更新列表导致有加载中进度条
                                        mPresenter.runLoader(true);
                                    }
                                } catch (Exception ignored) {
                                }
                            }
                        });

                        return null;
                    }
                }, mFailureListener);
            } catch (NotConnectedException ignored) {
                if (!com.yixin.tinode.util.Utils.isNetworkConnected(this)) {
                    showNetworkAlert(context);
                } else {
                    Log.d(TAG, "Offline mode, ignore");
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            com.yixin.tinode.util.Utils.reconnect(context, tinode, new com.yixin.tinode.util.Utils.ReconnectListener() {
                                @Override
                                public void after() {
                                    subscribe(tinode);
                                }
                            });
                        }
                    }).start();
                }

            } catch (Exception ex) {
                Toast.makeText(this, R.string.action_failed + ex.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "something went wrong", ex);
            }
        }
    }

    @Override
    public long getSearchMsgId() {
        return mSearchStartMsgId;
    }

    @Override
    public void setSearchMsgId(long searchMsgId) {
        this.mSearchStartMsgId = searchMsgId;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case AppConst.REQUEST_CODE_RELAY_MSG:
                if (resultCode == RESULT_OK) {
                    mPresenter.hideDialog();
                }
                break;
            case AppConst.REQUEST_IMAGE_PICKER:
                if (resultCode == ImagePicker.RESULT_CODE_ITEMS) {//返回多张照片
                    if (data != null) {
                        mTopic.resumeLocal();
                        //是否发送原图
                        boolean isOrig = data.getBooleanExtra(ImagePreviewActivity.ISORIGIN, false);
                        ArrayList<ImageItem> images = (ArrayList<ImageItem>) data.getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);
                        Log.e("CSDN_LQR", isOrig ? "发原图" : "不发原图");//若不发原图的话，需要在自己在项目中做好压缩图片算法
                        for (ImageItem imageItem : images) {
                            //							if (isOrig) {
                            //								imageFileSource = new File(imageItem.path);
                            //								imageFileThumb = ImageUtils.genThumbImgFile(imageItem.path);
                            //							} else {
                            //								//压缩图片
                            //								imageFileSource = ImageUtils.genThumbImgFile(imageItem.path);
                            //								imageFileThumb = ImageUtils.genThumbImgFile(imageFileSource.getAbsolutePath());
                            //							}

                            if (imageItem.path != null) {
                                data.setData(Uri.fromFile(new File(imageItem.path)));
                                sendFile(data, true);
                            }
                        }
                    }
                }
            case AppConst.REQUEST_TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    mTopic.resumeLocal();
                    String path = data.getStringExtra("path");
                    data.setData(Uri.fromFile(new File(path)));
                    boolean bTakePhoto=data.getBooleanExtra("take_photo", true);
                    sendFile(data, data.getBooleanExtra("take_photo", true));
                }
                break;
            case AppConst.REQUEST_MY_LOCATION:
                if (resultCode == RESULT_OK) {
                    mTopic.resumeLocal();
                    LocationData locationData = (LocationData) data.getSerializableExtra("location");
                    mPresenter.sendLocationMessage(locationData);
                }
                break;
            case AppConst.REQUEST_FILE:
                mTopic.resumeLocal();
                sendFile(data, false);
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void sendAudioFile(File file, int duration) {
        if (settingSendType.isSendFileByTinode()) {
            mPresenter.sendAudioFileTinode(file, duration);
        }
    }

    public void resend(Intent data) {
        String type = data.getStringExtra("type");
        if(type.equals("video") || type.equals("file")) {
            sendFile(data, false);
        } else if(type.equals("image")) {
            sendFile(data, true);
        }
    }

    public void resend(String type, long id) {
        if(id < 0) return;
        if(type.equals("location") || type.equals("text")) {
            syncMessages(id, true);
        }
    }

    private void sendFile(Intent data, Boolean isImg) {
        if (data == null) {
            return;
        }

        Uri uri = data.getData();
        if (settingSendType.isSendFileByTinode()) {
            final Bundle args = new Bundle();
            args.putLong("msgId", data.getLongExtra("msgId", -1));
            args.putParcelable("uri", uri);
            args.putInt("requestCode", isImg ? ACTION_ATTACH_IMAGE : ACTION_ATTACH_FILE);
            args.putString("topic", mTopicName);
            // Must use unique ID for each upload. Otherwise trouble.
            LoaderManager.getInstance(context).initLoader(Cache.getUniqueCounter(), args, mPresenter);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPresenter.saveDraft();

        mMessageSender.pause();
        if (mTypingAnimationTimer != null) {
            mTypingAnimationTimer.cancel();
            mTypingAnimationTimer = null;
        }

        mPresenter.onPause();
        Cache.getTinode().setListener(null);
        if (mTopic != null) {
            mTopic.setListener(null);

            // Deactivate current topic
            if (mTopic.isAttached()) {
                try {
                    mTopic.leave();
                } catch (Exception ex) {
                    Log.e(TAG, "something went wrong in Topic.leave", ex);
                }
            }
        }

        SessionAdapter adapter = ((SessionAdapter) mRvMsg.getAdapter());
        if (adapter != null) {
            List<StoredMessage> list = adapter.getData();
            if (list.size() > 0) {
                StoredMessage msg = list.get(list.size() - 1);
                //mTopic.setLastTs(msg.ts);
                mTopic.setLastMsg(msg.search);
                Subscription<VCard, ?> sub = mTopic.getSubscription(msg.from);
                if (sub != null && sub.pub != null) {
                    mTopic.setUserName(sub.pub.fn);
                }
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        //release the mediaPlayer
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(this, BackgroundService.class));
        } else {
            startService(new Intent(this, BackgroundService.class));
        }
        sendBroadcast(new Intent(AppConst.ACTION_NO_TINODE_LISTENER));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unRegisterBR();
        mMessageSender.shutdownNow();
        //added by pcg
        //Thread.setDefaultUncaughtExceptionHandler(null);

        if (logToast != null) {
            logToast.cancel();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        UiUtils.setVisibleTopic(hasFocus ? mTopicName : null);
    }

    private void registerBR() {
        BroadcastManager.getInstance(this).register(AppConst.UPDATE_CURRENT_SESSION, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //                Message message = intent.getParcelableExtra("result");
                //                if (message != null) {
                //                    if (message.getTargetId().equals(mTopicName)) {
                //                        mPresenter.receiveNewMessage(message);
                //                    }
                //                }
            }
        });
        BroadcastManager.getInstance(this).register(AppConst.REFRESH_CURRENT_SESSION, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //				mPresenter.loadMessage();
            }
        });
        BroadcastManager.getInstance(this).register(AppConst.UPDATE_CURRENT_SESSION_NAME, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //                setTitle();
            }
        });
        BroadcastManager.getInstance(this).register(AppConst.CLOSE_CURRENT_SESSION, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                finish();
            }
        });
    }

    private void unRegisterBR() {
        BroadcastManager.getInstance(this).unregister(AppConst.UPDATE_CURRENT_SESSION);
        BroadcastManager.getInstance(this).unregister(AppConst.REFRESH_CURRENT_SESSION);
        BroadcastManager.getInstance(this).unregister(AppConst.UPDATE_CURRENT_SESSION_NAME);
        BroadcastManager.getInstance(this).unregister(AppConst.CLOSE_CURRENT_SESSION);
    }

    private void initRefreshLayout() {
        mRefreshLayout.setDelegate(this);
        BGANormalRefreshViewHolder refreshViewHolder = new BGANormalRefreshViewHolder(this, false);
        refreshViewHolder.setRefreshingText("");
        refreshViewHolder.setPullDownRefreshText("");
        refreshViewHolder.setReleaseRefreshText("");
        mRefreshLayout.setRefreshViewHolder(refreshViewHolder);
    }

    private void initEmotionKeyboard() {
        mEmotionKeyboard = EmotionKeyboard.with(this);
        mEmotionKeyboard.bindToEditText(mEtContent);
        mEmotionKeyboard.bindToContent(mLlContent);
        mEmotionKeyboard.setEmotionLayout(mFlEmotionView);
        mEmotionKeyboard.bindToEmotionButton(mIvEmo, mIvMore);
        mEmotionKeyboard.setOnEmotionButtonOnClickListener(view -> {
            switch (view.getId()) {
                case R.id.ivEmo:
//                    UIUtils.postTaskDelay(() -> mRvMsg.smoothMoveToPosition(mRvMsg.getAdapter().getItemCount() - 1), 50);
                    mEtContent.clearFocus();
                    if (!mElEmotion.isShown()) {
                        if (mLlMore.isShown()) {
                            showEmotionLayout();
                            mLlMore.setVisibility(View.INVISIBLE);
                            hideAudioButton();
                            return true;
                        }
                    } else if (mElEmotion.isShown() && !mLlMore.isShown()) {
                        mIvEmo.setImageResource(R.drawable.jishiliaotian_icon_01);
                        return false;
                    }
                    showMoreLayout();
                    showEmotionLayout();
                    mLlMore.setVisibility(View.INVISIBLE);
                    hideAudioButton();
                    return true;
                case R.id.ivMore:
//                    UIUtils.postTaskDelay(() -> mRvMsg.smoothMoveToPosition(mRvMsg.getAdapter().getItemCount() - 1), 50);
                    mEtContent.clearFocus();

                    showMoreLayout();
                    hideEmotionLayout();
                    hideAudioButton();
                    return true;
            }
            return false;
        });
    }

    private void showAudioButton() {
        mBtnAudio.setVisibility(View.VISIBLE);
        mEtContent.setVisibility(View.GONE);
        mIvAudio.setImageResource(R.drawable.jishiliaotian_icon_01);

        if (mFlEmotionView.isShown()) {
            if (mEmotionKeyboard != null) {
                mEmotionKeyboard.interceptBackPress();
            }
        } else {
            if (mEmotionKeyboard != null) {
                mEmotionKeyboard.hideSoftInput();
            }
        }
    }

    private void hideAudioButton() {
        mBtnAudio.setVisibility(View.GONE);
        mEtContent.setVisibility(View.VISIBLE);
        mIvAudio.setImageResource(R.drawable.jishiliaotian_icon_03);
    }

    private void showEmotionLayout() {
        mElEmotion.setVisibility(View.VISIBLE);
        mIvEmo.setImageResource(R.drawable.jishiliaotian_icon_01);
    }

    private void hideEmotionLayout() {
        mElEmotion.setVisibility(View.GONE);
        mIvEmo.setImageResource(R.drawable.jishiliaotian_icon_01);
    }

    private void hideMoreLayout() {
        mLlMore.setVisibility(View.GONE);
    }

    private int getSupportSoftInputHeight() {
        Rect r = new Rect();
        getWindow().getDecorView().getWindowVisibleDisplayFrame(r);
        int screenHeight = getWindow().getDecorView().getRootView().getHeight();
        int softInputHeight = screenHeight - r.bottom;
        if (Build.VERSION.SDK_INT >= 20) {
            softInputHeight -= getSoftButtonsBarHeight(context);
        }

        if (softInputHeight < 0) {
            Log.w("LQR", "EmotionKeyboard--Warning: value of softInputHeight is below zero!");
        }

        return softInputHeight;
    }

    private void showMoreLayout() {
        if (!mFlEmotionView.isShown()) {
            int softInputHeight = this.getSupportSoftInputHeight();
            if (softInputHeight <= 0) {
                softInputHeight = getSharedPreferences("EmotionKeyBoard", MODE_PRIVATE).getInt("sofe_input_height", mEmotionKeyboard.dip2Px(500));
            }

            mEmotionKeyboard.hideSoftInput();
//            mFlEmotionView.getLayoutParams().height = softInputHeight;
            LogUtils.e("showMoreLayout mFlEmotionView.height=" + softInputHeight);
            mFlEmotionView.setVisibility(View.VISIBLE);
        }

        mLlMore.setVisibility(View.VISIBLE);
    }

    private void closeBottomAndKeyboard() {
        mElEmotion.setVisibility(View.GONE);
        mLlMore.setVisibility(View.GONE);
        if (mEmotionKeyboard != null) {
            mEmotionKeyboard.hideSoftInput();
            mEmotionKeyboard.interceptBackPress();
            mIvEmo.setImageResource(R.drawable.jishiliaotian_icon_01);
        }
    }

    @Override
    public void onBackPressed() {
        if (mElEmotion.isShown() || mLlMore.isShown()) {
            mEmotionKeyboard.interceptBackPress();
            mIvEmo.setImageResource(R.drawable.jishiliaotian_icon_01);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected SessionAtPresenter createPresenter() {
        return new SessionAtPresenter(this, mTopicName, mConversationType);
    }

    @Override
    protected int provideContentViewId() {
        return R.layout.activity_session;
    }

    @Override
    public void onEmojiSelected(String key) {
        LogUtils.e("onEmojiSelected : " + key);
    }

    @Override
    public void onStickerSelected(String categoryName, String stickerName, String
            stickerBitmapPath) {
//        LogUtils.e("onStickerSelected : categoryName = " + categoryName + " , stickerName = " + stickerName);
//        LogUtils.e("onStickerSelected : stickerBitmapPath = " + stickerBitmapPath);
        mPresenter.sendSticker(stickerName, stickerBitmapPath);
    }

    @Override
    public BGARefreshLayout getRefreshLayout() {
        return mRefreshLayout;
    }

    @Override
    public LQRRecyclerView getRvMsg() {
        return mRvMsg;
    }

    @Override
    public EditText getEtContent() {
        return mEtContent;
    }

    @Override
    public void onBGARefreshLayoutBeginRefreshing(BGARefreshLayout refreshLayout) {
        mPresenter.loadMore();
    }

    @Override
    public boolean onBGARefreshLayoutBeginLoadingMore(BGARefreshLayout refreshLayout) {
        return false;
    }

    ////////////////tinode/////////////////
    @Override
    public Topic getTopic() {
        return mTopic;
    }

    public void sendKeyPress() {
        if (mTopic != null) {
            mTopic.noteKeyPress();
        }
    }

    public void submitForExecution(Runnable runnable) {
        mMessageSender.submit(runnable);
    }

    private class TListener extends Topic.Listener {

        TListener() {
        }

        @Override
        public void onLeave(boolean unsub, int code, String text) {
            super.onLeave(unsub, code, text);
        }

        @Override
        public void onMeta(MsgServerMeta meta) {
            super.onMeta(meta);
        }

        @Override
        public void onMetaSub(Subscription sub) {
            try {
                Log.d(TAG, "SessionActivity Sub " + sub.topic + " is " + new ObjectMapper().writeValueAsString(sub.pub));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            super.onMetaSub(sub);
        }

        @Override
        public void onMetaTags(String[] tags) {
            super.onMetaTags(tags);
        }

        @Override
        public void onSubscribe(int code, String text) {
            // Topic name may change after subscription, i.e. new -> grpXXX
            mTopicName = mTopic.getName();
            /*
            MessagesFragment fragment = (MessagesFragment) getSupportFragmentManager().
                    findFragmentByTag(FRAGMENT_MESSAGES);
            if (fragment != null && fragment.isVisible()) {
                fragment.runLoader();
            }
            */
        }

        @Override
        public void onData(MsgServerData data) {
            if (SessionAdapter.getItemViewType(new StoredMessage(data)) == SessionAdapter.RECALL_NOTIFICATION && mConversationType == Topic.TopicType.P2P) {
                if (mTopic != null) {
                    try {
                        int seq = Integer.parseInt(data.content.txt);
                        //群聊需要获取Delete权限，单聊暂时不支持delete。单聊的撤回方案：发送撤回消息，收到撤回消息后，删除对应的消息。
                        // 群聊撤回方案：硬删除消息，发送撤回消息
                        PromisedReply<ServerMessage> reply = mTopic.delMessages(seq, seq + 1, true);
                        reply.thenApply(new PromisedReply.SuccessListener<ServerMessage>() {
                            @Override
                            public PromisedReply<ServerMessage> onSuccess(ServerMessage result) throws Exception {
                                // Updates message list with "delivered" icon.
                                mPresenter.runLoader(false);
                                return null;
                            }
                        }, mFailureListener);
                    } catch (NotConnectedException ignored) {
                        Log.d(TAG, "sendMessage -- NotConnectedException", ignored);
                    } catch (Exception ignored) {
                        Log.d(TAG, "sendMessage -- Exception", ignored);
                    }
                } else {
                    UIUtils.showToast(UIUtils.getString(R.string.recall_fail));
                }
            } else {
                mPresenter.runLoader(true);
            }
        }

        @Override
        public void onPres(MsgServerPres pres) {
            Log.d(TAG, "Topic '" + mTopicName + "' onPres what='" + pres.what + "'");
            //delete memeber pres what= acs
            //add member pres what=acs act src tgt
        }

        @Override
        public void onInfo(MsgServerInfo info) {
            switch (info.what) {
                case "read":
                case "recv":
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mPresenter.notifyDataSetChanged();
                        }
                    });
                    break;
                case "kp":
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Show typing indicator as animation over avatar in toolbar
//                            mTypingAnimationTimer = UiUtils.toolbarTypingIndicator(MessageActivity.this,
//                                    mTypingAnimationTimer, TYPING_INDICATOR_DURATION);
                        }
                    });
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onSubsUpdated() {
            //            runOnUiThread(new Runnable() {
            //                @Override
            //                public void run() {
            //                    TopicInfoFragment fragment = (TopicInfoFragment) getSupportFragmentManager().
            //                            findFragmentByTag(FRAGMENT_INFO);
            //
            //                    if (fragment != null && fragment.isVisible()) {
            //                        fragment.notifyDataSetChanged();
            //                    }
            //                }
            //            });
        }

        @Override
        public void onMetaDesc(final Description desc) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    VCard vcard = (VCard) mTopic.getPub();
                    if (vcard != null) {
                        setToolbarTitle(vcard.fn);
                    }
                    //                    TopicInfoFragment fragment = (TopicInfoFragment) getSupportFragmentManager().
                    //                            findFragmentByTag(FRAGMENT_INFO);
                    //                    if (fragment != null && fragment.isVisible()) {
                    //                        fragment.notifyContentChanged();
                    //                    }
                }
            });
        }

        @Override
        public void onContUpdate(final Subscription sub) {
            onMetaDesc(null);
        }

        @Override
        public void onOnline(final boolean online) {
            //            runOnUiThread(new Runnable() {
            //                @Override
            //                public void run() {
            //                    UiUtils.toolbarSetOnline(MessageActivity.this, mTopic.getOnline());
            //                }
            //            }            );
        }
    }



    private void displayEstablishTime() {
        if (callStartedTimeMs != 0 && pushSuccessTimeMs != 0 && recvSuccessTimeMs != 0
                && !establishTimeDisplayed) {
            establishTimeDisplayed = true;
            long pushSuccessDelay = pushSuccessTimeMs > callStartedTimeMs
                    ? pushSuccessTimeMs - callStartedTimeMs : 100;
            long recvSuccessDelay = recvSuccessTimeMs > callStartedTimeMs
                    ? recvSuccessTimeMs - callStartedTimeMs : 100;
            logAndToast("Call success, push " + pushSuccessDelay + "ms, recv "
                    + recvSuccessDelay + "ms");
        }
    }



    // Log |msg| and Toast about it.
    private void logAndToast(String msg) {

        if (logToast != null) {
            logToast.cancel();
        }
        logToast = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        logToast.show();
    }

    private boolean validateUrl(String url) {
        if (true || URLUtil.isHttpsUrl(url) || URLUtil.isHttpUrl(url)) {
            return true;
        }
        new AlertDialog.Builder(this)
                .setTitle(getText(R.string.invalid_url_title))
                .setMessage(getString(R.string.invalid_url_text, url))
                .setCancelable(false)
                .setNeutralButton(R.string.ok, (dialog, id) -> dialog.cancel())
                .create()
                .show();
        return false;
    }



    ///////////////send file by tinode
    // Try to send all pending messages.
    public void syncAllMessages(final boolean runLoader) {
        syncMessages(-1, runLoader);
    }

    // Try to send the specified message.
    public void syncMessages(final long msgId, final boolean runLoader) {
        mMessageSender.submit(new Runnable() {
            @Override
            public void run() {
                PromisedReply<ServerMessage> promise;
                if (msgId >= 0) {
                    promise = mTopic.syncOne(msgId);
                } else {
                    promise = mTopic.syncAll();
                }
                if (runLoader) {
                    promise.thenApply(new PromisedReply.SuccessListener<ServerMessage>() {
                        @Override
                        public PromisedReply<ServerMessage> onSuccess(ServerMessage result) {
                            mPresenter.runLoader(true);
                            return null;
                        }
                    });
                }
                promise.thenCatch(new PromisedReply.FailureListener<ServerMessage>() {
                    @Override
                    public PromisedReply<ServerMessage> onFailure(Exception err) {
                        Log.w(TAG, "Sync failed", err);
                        return null;
                    }
                });
            }
        });
    }
}

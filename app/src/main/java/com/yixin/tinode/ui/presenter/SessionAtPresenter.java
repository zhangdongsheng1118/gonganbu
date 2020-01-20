package com.yixin.tinode.ui.presenter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.os.Parcelable;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.blankj.utilcode.util.ClipboardUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.google.gson.Gson;
import com.lqr.audio.AudioPlayManager;
import com.lqr.audio.IAudioPlayListener;
import com.lqr.recyclerview.LQRRecyclerView;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.model.Progress;
import com.lzy.okgo.request.GetRequest;
import com.lzy.okserver.OkDownload;
import com.lzy.okserver.download.DownloadListener;
import com.lzy.okserver.download.DownloadTask;
import com.scoopit.weedfs.client.outer.UploadAndGetUrl;
import com.tencent.bugly.crashreport.BuglyLog;
import com.yixin.tinode.R;
import com.yixin.tinode.api.ApiRetrofit;
import com.yixin.tinode.api.param.TiCollection;
import com.yixin.tinode.app.AppConst;
import com.yixin.tinode.db.tinode.BaseDb;
import com.yixin.tinode.db.tinode.MessageDb;
import com.yixin.tinode.db.tinode.StoredMessage;
import com.yixin.tinode.db.tinode.StoredTopic;
import com.yixin.tinode.model.data.LocationData;
import com.yixin.tinode.tinode.Cache;
import com.yixin.tinode.tinode.util.UiUtils;
import com.yixin.tinode.ui.activity.ContentActivity;
import com.yixin.tinode.ui.activity.LocationInfoActivity;
import com.yixin.tinode.ui.activity.LoginActivity;
import com.yixin.tinode.ui.activity.RelayActivity;
import com.yixin.tinode.ui.activity.SessionActivity;
import com.yixin.tinode.ui.activity.ShowBigImageActivity;
import com.yixin.tinode.ui.adapter.SessionAdapter;
import com.yixin.tinode.ui.base.BaseActivity;
import com.yixin.tinode.ui.base.BasePresenter;
import com.yixin.tinode.ui.view.ISessionAtView;
import com.yixin.tinode.util.FileOpenUtils;
import com.yixin.tinode.util.LogUtils;
import com.yixin.tinode.util.MediaFileUtils;
import com.yixin.tinode.util.PathUtil;
import com.yixin.tinode.util.StringUtils;
import com.yixin.tinode.util.UIUtils;
import com.yixin.tinode.util.Utils;
import com.yixin.tinode.util.VideoThumbUploader;
import com.yixin.tinode.widget.CustomDialog;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import cn.bingoogolapple.refreshlayout.BGARefreshLayout;
import co.tinode.tinodesdk.LargeFileHelper;
import co.tinode.tinodesdk.NotConnectedException;
import co.tinode.tinodesdk.PromisedReply;
import co.tinode.tinodesdk.Storage;
import co.tinode.tinodesdk.Topic;
import co.tinode.tinodesdk.model.Drafty;
import co.tinode.tinodesdk.model.MetaSetSub;
import co.tinode.tinodesdk.model.MsgServerCtrl;
import co.tinode.tinodesdk.model.MsgSetMeta;
import co.tinode.tinodesdk.model.ServerMessage;
import co.tinode.tinodesdk.model.Subscription;
import co.tinode.tinodesdk.model.VCard;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import top.zibin.luban.Luban;

import static com.yixin.tinode.app.AppConst.ACTION_ATTACH_FILE;
import static com.yixin.tinode.app.AppConst.ACTION_ATTACH_IMAGE;
import static com.yixin.tinode.ui.adapter.SessionAdapter.RECEIVE_FILE;
import static com.yixin.tinode.ui.adapter.SessionAdapter.RECEIVE_IMAGE;
import static com.yixin.tinode.ui.adapter.SessionAdapter.RECEIVE_IMAGE_INLINE;
import static com.yixin.tinode.ui.adapter.SessionAdapter.RECEIVE_LOCATION;
import static com.yixin.tinode.ui.adapter.SessionAdapter.RECEIVE_TEXT;
import static com.yixin.tinode.ui.adapter.SessionAdapter.RECEIVE_VIDEO;
import static com.yixin.tinode.ui.adapter.SessionAdapter.RECEIVE_VOICE;
import static com.yixin.tinode.ui.adapter.SessionAdapter.SEND_FILE;
import static com.yixin.tinode.ui.adapter.SessionAdapter.SEND_IMAGE;
import static com.yixin.tinode.ui.adapter.SessionAdapter.SEND_IMAGE_INLINE;
import static com.yixin.tinode.ui.adapter.SessionAdapter.SEND_LOCATION;
import static com.yixin.tinode.ui.adapter.SessionAdapter.SEND_TEXT;
import static com.yixin.tinode.ui.adapter.SessionAdapter.SEND_VIDEO;
import static com.yixin.tinode.ui.adapter.SessionAdapter.SEND_VOICE;
import static com.yixin.tinode.util.Utils.antiShakeUtil;


public class SessionAtPresenter extends BasePresenter<ISessionAtView> implements LoaderManager.LoaderCallbacks<SessionAtPresenter.UploadResult> {
    private static final int MESSAGES_TO_LOAD = 20;
    // Delay before sending out a RECEIVED notification to be sure we are not sending too many.
    // private static final int RECV_DELAY = 500;
    private static final int READ_DELAY = 1000;
    //131072k
    public static final long MAX_ATTACHMENT_SIZE = 20 * (1 << 20);

    // Maximum size of file to send in-band. 256KB.
    private static final long MAX_INBAND_ATTACHMENT_SIZE = 1 << 15;

    private static final String TAG = SessionAtPresenter.class.getSimpleName();

    public Topic.TopicType mConversationType;
    //    private String mPushCotent = "";//接收方离线时需要显示的push消息内容。
    //    private String mPushData = "";//接收方离线时需要在push消息中携带的非显示内容。
    //    private int mMessageCount = 5;//一次获取历史消息的最大数量

    private List<StoredMessage> mData = new ArrayList<>();
    private SessionAdapter mAdapter;
    private CustomDialog mSessionMenuDialog;
    protected Topic mTopic;
    private Timer mNoteTimer = null;
    private PromisedReply.FailureListener<ServerMessage> mFailureListener;
    // It cannot be local.
    private UploadProgress mUploadProgress;

    public SessionAtPresenter(BaseActivity context, String sessionId, Topic.TopicType conversationType) {
        super(context);
        mConversationType = conversationType;

        // Creating a strong reference from this Fragment, otherwise it will be immediately garbage collected.
        mUploadProgress = new UploadProgress();
        // This needs to be rebound on activity creation.
        FileUploader.setProgressHandler(mUploadProgress);
    }

    public void hideDialog() {
        if (mSessionMenuDialog != null && mSessionMenuDialog.isShowing()) {
            mSessionMenuDialog.dismiss();
            mSessionMenuDialog = null;
        }
    }

    public void onResume() {
        // Check periodically if all messages were read;
        mNoteTimer = new Timer();
        mNoteTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                sendReadNotification();
            }
        }, READ_DELAY, READ_DELAY);

        mTopic = getView().getTopic();

        setAdapter();
        mAdapter.setTopicName(mTopic.getName());

        long startMsgId = getView().getSearchMsgId();
        if (startMsgId > 0) {
            mAdapter.setmPagesToLoad(MessageDb.getPageFromMsgId(BaseDb.getInstance().getReadableDatabase(), ((StoredTopic) mTopic.getLocal()).id, startMsgId, MESSAGES_TO_LOAD));
        }
        //首次进入加载到最后一条
        runLoader(true);
    }

    //将搜索的消息显示在视野中
    public void searchMsgToInSight() {
        long startMsgId = getView().getSearchMsgId();
        if (startMsgId > 0) {
            int position = mAdapter.getPostionByMsgId(startMsgId);
            if (position >= 0) {
                getRecyclerView().smoothMoveToPosition(position);
            } else {
                getView().setSearchMsgId(0);
            }
        }
    }

    public void notifyDataSetChanged() {
        mAdapter.notifyDataSetChanged();
    }

    public void sendReadNotification() {
        if (mTopic != null) {
            mTopic.noteRead();
        }
    }

    public void onPause() {
        // Stop reporting read messages
        if (mNoteTimer != null) {
            mNoteTimer.cancel();
            mNoteTimer = null;
        }
    }

    public BGARefreshLayout getRefreshLayout() {
        return getView().getRefreshLayout();
    }

    public void runLoader(boolean scrollToBottom) {
        Log.e(TAG, "runLoader ");
        mContext.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdapter.runLoader(scrollToBottom);
            }
        });
    }

    private LQRRecyclerView getRecyclerView() {
        return getView().getRvMsg();
    }

    //refresh
    public void loadMore() {
        getLocalHistoryMessage();
        mAdapter.notifyDataSetChangedWrapper();
    }

    public void resetDraft() {
        //        Observable.just(RongIMClient.getInstance().getTextMessageDraft(mConversationType, mTopicName))
        //                .subscribeOn(Schedulers.io())
        //                .observeOn(AndroidSchedulers.mainThread())
        //                .subscribe(s -> {
        //                    if (!TextUtils.isEmpty(s)) {
        //                        getView().getEtContent().setText(s);
        //                        RongIMClient.getInstance().clearTextMessageDraft(mConversationType, mTopicName);
        //                    }
        //                }, this::loadError);
    }

    public void saveDraft() {
        String draft = getView().getEtContent().getText().toString();
        if (!TextUtils.isEmpty(draft)) {
            //            RongIMClient.getInstance().saveTextMessageDraft(mConversationType, mTopicName, draft);
        }
    }

    public void setAdapter() {
        if (mAdapter == null) {
            mAdapter = new SessionAdapter(mContext, this);
            mAdapter.setOnItemClickListenerMine((helper, parent, itemView, position) -> {
                if(antiShakeUtil.check()) return;
                StoredMessage message = mAdapter.getMessage(position);
                Drafty msgContent = message.content;
                int viewType = mAdapter.getItemViewType(message);

                if (viewType == SEND_TEXT || viewType == RECEIVE_TEXT) {
                    ((SessionActivity)mContext).resend("text", message.id);
                }else if (viewType == SEND_VOICE || viewType == RECEIVE_VOICE) {
                    final ImageView ivAudio = helper.getView(R.id.ivAudio);
                    try {
                        AudioPlayManager.getInstance().startPlay(mContext, Uri.parse(msgContent.getVoiceUrl(Cache.getTinode().getBaseUrl())), new IAudioPlayListener() {
                            @Override
                            public void onStart(Uri var1) {
                                if (ivAudio != null && ivAudio.getBackground() instanceof AnimationDrawable) {
                                    AnimationDrawable animation = (AnimationDrawable) ivAudio.getBackground();
                                    animation.start();
                                }
                            }

                            @Override
                            public void onStop(Uri var1) {
                                if (ivAudio != null && ivAudio.getBackground() instanceof AnimationDrawable) {
                                    AnimationDrawable animation = (AnimationDrawable) ivAudio.getBackground();
                                    animation.stop();
                                    animation.selectDrawable(0);
                                }
                            }

                            @Override
                            public void onComplete(Uri var1) {
                                if (ivAudio != null && ivAudio.getBackground() instanceof AnimationDrawable) {
                                    AnimationDrawable animation = (AnimationDrawable) ivAudio.getBackground();
                                    animation.stop();
                                    animation.selectDrawable(0);
                                }
                            }
                        });
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                } else if (viewType == SEND_IMAGE_INLINE || viewType == RECEIVE_IMAGE_INLINE) {
                    Map<String, Object> data = msgContent.getInfo();
                    Bundle args = new Bundle();
                    if (data != null) {
                        try {
                            Object val = data.get("val");
                            args.putByteArray("image", val instanceof String ?
                                    Base64.decode((String) val, Base64.DEFAULT) :
                                    (byte[]) val);
                            args.putString("mime", (String) data.get("mime"));
                            args.putString("name", (String) data.get("name"));
                        } catch (ClassCastException ignored) {
                        }
                    }
                    mContext.startActivity(new Intent(mContext, ContentActivity.class)
                            .putExtra(ContentActivity.KEY_FRAGMENT, ContentActivity.FRAGMENT_VIEW_IMAGE)
                            .putExtras(args));
                } else if (viewType == SEND_IMAGE || viewType == RECEIVE_IMAGE) {
                    if(helper.getView(R.id.ivError) == itemView) {
                        String path = "";
                        try {
                            path = "file://" + (String) message.content.ent[0].data.get("local");
                        } catch (Exception e) {
                            path = "";
                        }

                        if(message.id < 0 || path.equals("") || path.length() == 0) return;

                        Intent intent = new Intent();
                        Uri uri = Uri.parse(path);
                        intent.setData(uri);
                        intent.putExtra("type", "image");
                        intent.putExtra("msgId", message.id);
                        ((SessionActivity)mContext).resend(intent);
                    } else {
                        String url = (String) msgContent.getInfo().get("local");
                        int isLocal = 1;
                        if (StringUtils.isEmpty(url) || !FileUtils.isFileExists(url)) {
                            isLocal = 0;
                            try {
                                url = msgContent.getImageUrl(false, Cache.getTinode().getBaseUrl());
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            }
                        }
                        Intent intent = new Intent(mContext, ShowBigImageActivity.class);
                        intent.putExtra("islocal", isLocal);
                        intent.putExtra("url", url);
                        intent.putExtra("name", msgContent.getInfo().get("name").toString());
                        mContext.jumpToActivity(intent);
                    }
                    //                    viewType == SEND_STICKER || viewType == RECEIVE_STICKER||
                } else if (viewType == SEND_VIDEO || viewType == RECEIVE_VIDEO || viewType == RECEIVE_FILE || viewType == SEND_FILE) {
                    if(helper.getView(R.id.ivError) == itemView) {
                        String path = "";
                        try {
                            path = "file://" + (String) message.content.ent[0].data.get("local");
                        } catch (Exception e) {
                            path = "";
                        }

                        if(message.id < 0 || path.equals("") || path.length() == 0) return;

                        Intent intent = new Intent();
                        Uri uri = Uri.parse(path);
                        intent.setData(uri);
                        if(viewType == SEND_VIDEO || viewType == RECEIVE_VIDEO) intent.putExtra("type", "video");
                        else intent.putExtra("type", "file");
                        intent.putExtra("msgId", message.id);
                        ((SessionActivity)mContext).resend(intent);
                    } else {
                        if (message.seq > 0) {
                            SessionAdapter.FileInfo file = SessionAdapter.getFileInfo(msgContent);
                            //                    if (MediaFileUtils.isVideoFileType(file.name)) {
                            file.msgId = message.getId();
                            downloadMediaMessage(file);
                        }
                    }
                } else if (viewType == RECEIVE_LOCATION || viewType == SEND_LOCATION) {
                    if(helper.getView(R.id.ivError) == itemView) {
                        ((SessionActivity)mContext).resend("location", message.id);
                    } else {
                        LocationData data = SessionAdapter.parseStoredMsgToLocationData(message);
                        Intent intent = new Intent(mContext, LocationInfoActivity.class);
                        intent.putExtra("location", data);
                        mContext.startActivity(intent);
                    }
                }
                else{
                    Log.e(TAG,"error getItemViewType=UnkownType");
                }

            });
            getView().getRvMsg().setItemViewCacheSize(50);
            getView().getRvMsg().setAdapter(mAdapter);
            mAdapter.setOnItemLongClickListenerMine((helper, viewGroup, view, position) -> {
                StoredMessage message = mAdapter.getMessage(position);
                Drafty msgContent = message.content;
                int viewType = mAdapter.getItemViewType(message);

                //根据消息类型控制显隐
                if (viewType == SessionAdapter.RECEIVE_NOTIFICATION || viewType == SessionAdapter.RECALL_NOTIFICATION) {
                    return false;
                }

                View sessionMenuView = View.inflate(mContext, R.layout.dialog_session_menu, null);
                mSessionMenuDialog = new CustomDialog(mContext, sessionMenuView, R.style.MyDialog);
                TextView tvReCall = (TextView) sessionMenuView.findViewById(R.id.tvReCall);
                TextView tvDelete = (TextView) sessionMenuView.findViewById(R.id.tvDelete);
                TextView tvCopy = (TextView) sessionMenuView.findViewById(R.id.tv_copy);
                TextView tvSend = (TextView) sessionMenuView.findViewById(R.id.tv_send);
                TextView tvCollection = (TextView) sessionMenuView.findViewById(R.id.tv_collection);

                tvCollection.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //text/img/file/sound/sound
                        TiCollection collection = new TiCollection();
                        int type = getTypeByViewType(viewType);
                        collection.setDes(new Gson().toJson(parseDrafty(msgContent, type)));

                        if (mConversationType == Topic.TopicType.P2P) {
                            collection.setFromName(((VCard) mTopic.getPub()).fn);
                        } else if (mConversationType == Topic.TopicType.GRP) {
                            String name = "";
                            Subscription<VCard, ?> sub = mTopic != null ? mTopic.getSubscription(message.from) : null;
                            if (sub != null && sub.pub != null) {
                                name = sub.pub.fn + "-";
                            }
                            collection.setFromName(name + ((VCard) mTopic.getPub()).fn);
                        }
                        //                        collection.setPath();
                        //                        collection.setTopic(Cache.getTinode().getMyId());
                        final SharedPreferences pref = android.preference.PreferenceManager.getDefaultSharedPreferences(mContext);
                        String login = pref.getString(LoginActivity.PREFS_LAST_LOGIN, null);
                        collection.setUserName(login);
                        collection.setType(type);
                        //                        collection.setMessageTime(System.currentTimeMillis() + "");
                        System.out.println(collection.toString());
                        ApiRetrofit.getInstance().mApi.addCollect(collection).subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(sendCodeResponse -> {
                                    if (sendCodeResponse.success()) {
                                        ToastUtils.showShort("收藏成功");
                                    } else {
                                        ToastUtils.showShort("收藏失败");
                                    }

                                    hideDialog();
                                }, error -> {
                                    ToastUtils.showShort("收藏失败");

                                    hideDialog();
                                });
                    }
                });

                if (!message.isMine()) {
                    tvReCall.setVisibility(View.GONE);
                } else {
                    long msgTime = System.currentTimeMillis() - message.ts.getTime();
                    if (msgTime > 2 * 60 * 1000) {
                        //超过两分钟不能撤回
                        tvReCall.setVisibility(View.GONE);
                    }
                }

                if (viewType == SessionAdapter.RECEIVE_RED_PACKET || viewType == SessionAdapter.SEND_RED_PACKET) {
                    tvReCall.setVisibility(View.GONE);
                    tvSend.setVisibility(View.GONE);
                    tvCollection.setVisibility(View.GONE);
                }

                if (viewType == SEND_TEXT || viewType == RECEIVE_TEXT) {
                    tvCopy.setVisibility(View.VISIBLE);
                    tvCopy.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ClipboardUtils.copyText(msgContent.txt);
                            ToastUtils.showShort("已复制");

                            hideDialog();
                        }
                    });
                } else {
                    tvCopy.setVisibility(View.GONE);
                }

                if (viewType == SEND_VOICE || viewType == RECEIVE_VOICE) {
                    tvSend.setVisibility(View.GONE);
                }
                //                else if (viewType == SEND_IMAGE || viewType == RECEIVE_IMAGE) {
                //                } else if (viewType == SEND_VIDEO || viewType == RECEIVE_VIDEO || viewType == RECEIVE_FILE || viewType == SEND_FILE) {
                //                } else if (viewType == RECEIVE_LOCATION || viewType == SEND_LOCATION) {
                //                }
                tvReCall.setOnClickListener(v -> {
                            if (mTopic != null) {
                                try {
                                    //群聊需要获取Delete权限，单聊暂时不支持delete。单聊的撤回方案：发送撤回消息，收到撤回消息后，删除对应的消息。
                                    // 群聊撤回方案：硬删除消息，发送撤回消息
                                    if (mConversationType == Topic.TopicType.P2P) {
                                        sendMessage(new Drafty().insertRecall(message.seq + ""), new PromisedReply.SuccessListener<ServerMessage>() {
                                            @Override
                                            public PromisedReply<ServerMessage> onSuccess(ServerMessage result) throws Exception {
                                                mContext.runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        //                                                        UIUtils.showToast(UIUtils.getString(R.string.recall_success));
                                                        hideDialog();
                                                    }
                                                });

                                                if (mTopic != null) {
                                                    try {
                                                        int seq = message.seq;
                                                        //群聊需要获取Delete权限，单聊暂时不支持delete。单聊的撤回方案：发送撤回消息，收到撤回消息后，删除对应的消息。
                                                        // 群聊撤回方案：硬删除消息，发送撤回消息
                                                        PromisedReply<ServerMessage> reply = mTopic.delMessages(seq, seq + 1, true);
                                                        reply.thenApply(new PromisedReply.SuccessListener<ServerMessage>() {
                                                            @Override
                                                            public PromisedReply<ServerMessage> onSuccess(ServerMessage result) throws Exception {
                                                                // Updates message list with "delivered" icon.
                                                                runLoader(false);
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

                                                return null;
                                            }
                                        }, new PromisedReply.FailureListener<ServerMessage>() {
                                            @Override
                                            public PromisedReply<ServerMessage> onFailure(Exception err) throws Exception {

                                                mContext.runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        UIUtils.showToast(UIUtils.getString(R.string.action_failed));
                                                        hideDialog();
                                                    }
                                                });
                                                return null;
                                            }
                                        });
                                    } else {
                                        PromisedReply<ServerMessage> reply = mTopic.delMessages(message.seq, message.seq + 1, true);
                                        reply.thenApply(new PromisedReply.SuccessListener<ServerMessage>() {
                                            @Override
                                            public PromisedReply<ServerMessage> onSuccess(ServerMessage result) throws Exception {
                                                // Updates message list with "delivered" icon.
                                                runLoader(false);

                                                mContext.runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        UIUtils.showToast(UIUtils.getString(R.string.recall_success));
                                                        hideDialog();
                                                    }
                                                });

                                                sendMessage(new Drafty().insertRecall(message.seq + ""));
                                                return null;
                                            }
                                        }, mFailureListener);
                                    }
                                } catch (NotConnectedException ignored) {
                                    Log.d(TAG, "sendMessage -- NotConnectedException", ignored);
                                } catch (Exception ignored) {
                                    Log.d(TAG, "sendMessage -- Exception", ignored);
                                }
                            } else {
                                UIUtils.showToast(UIUtils.getString(R.string.recall_fail));
                            }
                        }
                );
                tvDelete.setOnClickListener(v -> {
                            if (mTopic != null) {
                                try {
                                    PromisedReply<ServerMessage> reply = mTopic.delMessages(message.seq, message.seq + 1, false);
                                    reply.thenApply(new PromisedReply.SuccessListener<ServerMessage>() {
                                        @Override
                                        public PromisedReply<ServerMessage> onSuccess(ServerMessage result) throws Exception {
                                            // Updates message list with "delivered" icon.
                                            runLoader(false);
                                            hideDialog();
                                            return null;
                                        }
                                    }, mFailureListener);
                                } catch (NotConnectedException ignored) {
                                    Log.d(TAG, "sendMessage -- NotConnectedException", ignored);
                                } catch (Exception ignored) {
                                    Log.d(TAG, "sendMessage -- Exception", ignored);
                                }
                            }
                        }
                );

                tvSend.setOnClickListener(v -> {
                    Intent intent = new Intent(mContext, RelayActivity.class);
                    intent.putExtra("drafty", msgContent);
                    mContext.startActivityForResult(intent, AppConst.REQUEST_CODE_RELAY_MSG);

                    hideDialog();
                });
                mSessionMenuDialog.show();
                return false;
            });
            UIUtils.postTaskDelay(() -> getView().getRvMsg().smoothMoveToPosition(mData.size() - 1), 200);
        } else {
            mAdapter.notifyDataSetChangedWrapper();
            if (getView() != null && getView().getRvMsg() != null) {
                rvMoveToBottom();
            }
        }
    }

    public static Drafty toDrafty(Drafty.CollectDes des, int type) {
        Drafty drafty = Drafty.parse("");
        Map<String, Object> data = drafty.getInfo();
        switch (type) {
            case AppConst.COLLECTION_TYPE_FILE:
                data.put("name", des.getFname());
                data.put("val", des.getTxt());
                data.put("length", des.getLength());
                data.put("cover", des.getPoster());
                break;
            case AppConst.COLLECTION_TYPE_TXT:
                drafty.txt = des.getTxt();
                break;
            case AppConst.COLLECTION_TYPE_VIDEO:
                data.put("name", des.getFname());
                data.put("val", des.getTxt());
                data.put("length", des.getLength());
                data.put("cover", des.getPoster());
                break;
            case AppConst.COLLECTION_TYPE_PIC:
                data.put("name", des.getFname());
                data.put("val", des.getTxt());
                break;
            case AppConst.COLLECTION_TYPE_SOUND:
                data.put("name", des.getFname());
                data.put("val", des.getTxt());
                data.put("length", des.getLength());
                break;
            case AppConst.COLLECTION_TYPE_LOCATION:
                data.put("name", des.getFname());
                data.put("val", des.getTxt());
                data.put("lat", des.getLat());
                data.put("lng", des.getLng());
                break;
        }

        return drafty;
    }

    public static Drafty.CollectDes parseDrafty(Drafty drafty, int type) {
        Drafty.CollectDes des = new Drafty.CollectDes();
        Map<String, Object> data = drafty.getInfo();
        switch (type) {
            case AppConst.COLLECTION_TYPE_FILE:
                des.setFname((String) data.get("name"));
                des.setTxt((String) data.get("val"));
                des.setLength(data.get("length") == null ? 0 : Long.parseLong(data.get("length").toString()));
                des.setPoster((String) data.get("cover"));
                break;
            case AppConst.COLLECTION_TYPE_TXT:
                des.setTxt(drafty.txt);
                break;
            case AppConst.COLLECTION_TYPE_VIDEO:
                des.setFname((String) data.get("name"));
                des.setTxt((String) data.get("val"));
                des.setLength(data.get("length") == null ? 0 : Long.parseLong(data.get("length").toString()));
                des.setPoster((String) data.get("cover"));
                break;
            case AppConst.COLLECTION_TYPE_PIC:
                des.setFname((String) data.get("name"));
                des.setTxt((String) data.get("val"));
                break;
            case AppConst.COLLECTION_TYPE_SOUND:
                des.setFname((String) data.get("name"));
                des.setTxt((String) data.get("val"));
                des.setLength(data.get("length") == null ? 0 : Long.parseLong(data.get("length").toString()));
                break;
            case AppConst.COLLECTION_TYPE_LOCATION:
                des.setFname((String) data.get("name"));
                des.setTxt((String) data.get("val"));
                des.setLat((Double) data.get("lat"));
                des.setLng((Double) data.get("lng"));
                break;
        }

        return des;
    }

    private int getTypeByViewType(int viewType) {
        int type = AppConst.COLLECTION_TYPE_TXT;
        switch (viewType) {
            case SEND_IMAGE:
            case RECEIVE_IMAGE:
                type = AppConst.COLLECTION_TYPE_PIC;
                break;
            case RECEIVE_VIDEO:
            case SEND_VIDEO:
                type = AppConst.COLLECTION_TYPE_VIDEO;
                break;
            case RECEIVE_FILE:
            case SEND_FILE:
                type = AppConst.COLLECTION_TYPE_FILE;
                break;
            case RECEIVE_LOCATION:
            case SEND_LOCATION:
                type = AppConst.COLLECTION_TYPE_LOCATION;
                break;
            case RECEIVE_VOICE:
            case SEND_VOICE:
                type = AppConst.COLLECTION_TYPE_SOUND;
                break;
        }
        return type;
    }

    private void rvMoveToBottom() {
        getView().getRvMsg().smoothMoveToPosition(mData.size() - 1);
    }

    public void sendTextMsg() {
        mTopic.resumeLocal();
        sendTextMsg(getView().getEtContent().getText().toString());
        getView().getEtContent().setText("");
    }

    public void sendTextMsg(String content) {
        if (!StringUtils.isEmpty(content)) {
            if (sendMessage(Drafty.parse(content))) {
            }
        } else {
        }
    }

    //仅保存在本地  //插入本地数据库但是还没发送到服务器
    private long sendMessageToLocal(Drafty content) {
        long msgId = -1;
        if (mTopic != null) {
            try {
                msgId = mTopic.publishPreFile(content);
                runLoader(true); // Shows pending message
            } catch (Exception ignored) {
                Log.d(TAG, "sendMessage -- Exception", ignored);
                Toast.makeText(mContext, "消息保存失败", Toast.LENGTH_SHORT).show();
                return msgId;
            }
        }
        return msgId;
    }

    private boolean sendMessagePendding(StoredMessage msg) {
        if (mTopic != null) {
            try {
                PromisedReply<ServerMessage> reply = mTopic.publishAfter(msg.getId(), msg.content);
                runLoader(true); // Shows pending message
                reply.thenApply(new PromisedReply.SuccessListener<ServerMessage>() {
                    @Override
                    public PromisedReply<ServerMessage> onSuccess(ServerMessage result) throws Exception {
                        // Updates message list with "delivered" icon.
                        runLoader(true);
                        return null;
                    }
                }, mFailureListener);
            } catch (NotConnectedException ignored) {
                Log.d(TAG, "sendMessage -- NotConnectedException", ignored);
            } catch (Exception ignored) {
                Log.d(TAG, "sendMessage -- Exception", ignored);
                Toast.makeText(mContext, R.string.failed_to_send_message, Toast.LENGTH_SHORT).show();
                return false;
            }
            return true;
        }
        return false;
    }

    private boolean sendMessage(Drafty content) {
        return sendMessage(content, null, null);
    }

    private boolean sendMessage(Drafty content, PromisedReply.SuccessListener<ServerMessage> succCb, PromisedReply.FailureListener<ServerMessage> failCb) {
        if (failCb != null) {
            mFailureListener = failCb;
        }

        if (mTopic != null) {
            try {
                PromisedReply<ServerMessage> reply = mTopic.publish(content);
                runLoader(true); // Shows pending message
                reply.thenApply(new PromisedReply.SuccessListener<ServerMessage>() {
                    @Override
                    public PromisedReply<ServerMessage> onSuccess(ServerMessage result) throws Exception {
                        // Updates message list with "delivered" icon.
                        runLoader(true);

                        if (succCb != null) {
                            succCb.onSuccess(result);
                        }

                        //群聊需要获取Delete权限，单聊暂时不支持delete。单聊的撤回方案：发送撤回消息，收到撤回消息后，删除对应的消息。
                        // 群聊撤回方案：硬删除消息，发送撤回消息
                        if (mConversationType == Topic.TopicType.GRP) {
                            String myId = Cache.getTinode().getMyId();
                            if (!mTopic.getSubscription(myId).acs.getGivenHelper().isDeleter()) {
                                //没有删除权限，去获取
                                mTopic.setMeta(new MsgSetMeta(null, new MetaSetSub(myId, "JRWPAD"), null)).thenApply(new PromisedReply.SuccessListener<ServerMessage>() {
                                    @Override
                                    public PromisedReply onSuccess(ServerMessage result) throws Exception {
                                        Log.e("", JSON.toJSONString(result));
                                        return null;
                                    }
                                }, new PromisedReply.FailureListener() {
                                    @Override
                                    public PromisedReply onFailure(Exception err) throws Exception {
                                        return null;
                                    }
                                });
                            }
                        }
                        return null;
                    }
                }, mFailureListener);
            } catch (NotConnectedException ignored) {
                Log.d(TAG, "sendMessage -- NotConnectedException", ignored);
            } catch (Exception ignored) {
                Log.d(TAG, "sendMessage -- Exception", ignored);
                //                Toast.makeText(mContext, R.string.failed_to_send_message, Toast.LENGTH_SHORT).show();
                return false;
            }
            return true;
        }
        return false;
    }

//    public void openFileSelector(String mimeType, int title, int resultCode) {
//        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//        intent.setType(mimeType);
//        intent.addCategory(Intent.CATEGORY_OPENABLE);
//        try {
//            mContext.startActivityForResult(
//                    Intent.createChooser(intent, mContext.getString(title)), resultCode);
//        } catch (android.content.ActivityNotFoundException ex) {
//            Toast.makeText(mContext, R.string.file_manager_not_found, Toast.LENGTH_SHORT).show();
//        }
//    }

    public void openFileSelector(String mimeType, int title, int resultCode) {
        Intent chooserIntent;
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(mimeType);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        List<ResolveInfo> resInfo = mContext.getPackageManager().queryIntentActivities(intent, 0);

        if (!resInfo.isEmpty()) {
            ResolveInfo resolveInfo = null;
            for (ResolveInfo rInfo : resInfo) {
                if(rInfo.activityInfo == null) continue;
                if (rInfo.activityInfo.packageName.contains("com.huawei.hidisk")) {
                    resolveInfo = rInfo;
                } else if(rInfo.activityInfo.packageName.contains("com.android.documentsui")) {
                    if(resolveInfo == null || !resolveInfo.activityInfo.packageName.contains("com.huawei.hidisk")) {
                        resolveInfo = rInfo;
                    }
                }
            }

            if(resolveInfo == null) {
                chooserIntent = Intent.createChooser(intent, mContext.getString(title));
            } else {
                List<Intent> targetedShareIntents = new ArrayList<Intent>();
                Intent targetedShareIntent = new Intent(Intent.ACTION_GET_CONTENT);
                targetedShareIntent.setType(mimeType);
                targetedShareIntent.addCategory(Intent.CATEGORY_OPENABLE);
                targetedShareIntent.setPackage(resolveInfo.activityInfo.packageName);
                targetedShareIntent.setClassName(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name);
                targetedShareIntents.add(targetedShareIntent);

                chooserIntent = Intent.createChooser(targetedShareIntents.remove(targetedShareIntents.size() - 1), mContext.getString(title));
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetedShareIntents.toArray(new Parcelable[]{}));
            }
        } else {
            chooserIntent = Intent.createChooser(intent, mContext.getString(title));
        }

        try {
            mContext.startActivityForResult(chooserIntent, resultCode);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(mContext, R.string.file_manager_not_found, Toast.LENGTH_SHORT).show();
        }
    }

    public void sendSticker(String name, String path) {
        Drafty content = Drafty.parse(" ");
        content.insertImageSticker(name, path);
        sendMessage(content);
    }

    public void sendLocationMessage(LocationData locationData) {
        Drafty content = Drafty.parse(" ");
        content.insertLocation(locationData.getLat(), locationData.getLng(), locationData.getPoi(), locationData.getImgUrl());
        sendMessage(content);
    }

    /**
     * tinode sendfile
     */
    public void sendAudioFileTinode(File file, int duration) {
        if (!file.exists() || file.length() == 0L) {
            LogUtils.sf(UIUtils.getString(R.string.send_audio_fail));
            return;
        }

        mTopic.resumeLocal();

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                // Upload then send message with a link. This is a long-running blocking call.
                String url = null;
                final LargeFileHelper uploader = Cache.getTinode().getFileUploader();
                try {
                    MsgServerCtrl ctrl = uploader.upload(new FileInputStream(file), file.getName(), "audio/mp3", file.length(), null);
                    boolean success = (ctrl != null && ctrl.code == 200);
                    if (success) {
                        url = ctrl.getStringParam("url", null);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return url;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);

                if (com.blankj.utilcode.util.StringUtils.isEmpty(s)) {
                    ToastUtils.showShort("语音上传失败！");
                } else {
                    Drafty content = Drafty.parse(" ");
                    try {
                        content.insertVoice("audio/mp3", new URL(Cache.getTinode().getBaseUrl(), s).toString(), file.getName(), duration);
                        sendMessage(content);
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                        ToastUtils.showShort("语音发送失败！");
                    }
                }
            }
        }.execute();
    }

    public void downloadMediaMessage(SessionAdapter.FileInfo fileInfo) {
        if (FileUtils.isFileExists(fileInfo.local)) {
            FileOpenUtils.openFile(mContext, fileInfo.local);
            return;
        }

        LogUtils.i("downloadMediaMessage");
        File file = new File(PathUtil.getInstance().getFilePath().getAbsolutePath() + "/" + fileInfo.name);
        if (file.exists()) {
            FileOpenUtils.openFile(mContext, file.getAbsolutePath());
            return;
        }
        //兼容tinode和seaweed
        Object val = fileInfo.val;
        if (val != null) {
            //消息体中存在文件
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(file);
                fos.write(val instanceof String ?
                        Base64.decode((String) val, Base64.DEFAULT) :
                        (byte[]) val);

                FileOpenUtils.openFile(mContext, file.getAbsolutePath());
            } catch (NullPointerException | ClassCastException | IOException ex) {
                Log.w(TAG, "Failed to save attachment to storage", ex);
                ToastUtils.showShort(R.string.failed_to_download);
            } catch (ActivityNotFoundException ex) {
                Log.w(TAG, "No application can handle downloaded file");
                ToastUtils.showShort(R.string.failed_to_open_file);
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (Exception ignored) {
                    }
                }
            }
        } else {
            //下载文件
            OkDownload okDownload = OkDownload.getInstance();
            GetRequest<File> request = OkGo.get(fileInfo.url);
            request.headers("X-Tinode-APIKey", Cache.getTinode().getApiKey())
                    .headers("Authorization", "Token " + Cache.getTinode().getAuthToken());

            DownloadTask task = okDownload.getTask(fileInfo.url);
            if (task == null) {
                task = okDownload.request(fileInfo.url, request).fileName(fileInfo.name).save().register(new DownloadListener("downloadMediaMessage") {
                    private long lastTime = 0;

                    @Override
                    public void onStart(Progress progress) {
                        ToastUtils.showShort("开始下载");
                    }

                    @Override
                    public void onProgress(Progress progress) {
                        LogUtils.i("下载进度" + progress);

                        if (System.currentTimeMillis() - lastTime > 500) {
                            int position = mAdapter.getPostionByMsgId(fileInfo.msgId);
                            if (position >= 0) {
                                UploadAndGetUrl.FileInfo info = new UploadAndGetUrl.FileInfo();
                                info.progress = Math.round(progress.fraction * 100);
                                mAdapter.notifyItemChanged(position, info);
                                lastTime = System.currentTimeMillis();
                            }
                        }
                    }

                    @Override
                    public void onError(Progress progress) {
                        ToastUtils.showShort("文件下载失败");
                    }

                    @Override
                    public void onFinish(File file, Progress progress) {
                        ToastUtils.showShort("下载完成");
                        //                    FileOpenUtils.openFile(mContext, file.getAbsolutePath());
                        int position = mAdapter.getPostionByMsgId(fileInfo.msgId);
                        if (position >= 0) {
                            mAdapter.notifyItemChanged(position);
                        }
                    }

                    @Override
                    public void onRemove(Progress progress) {

                    }
                });
            }

            task.start();
        }
    }

    //获取会话中，从指定消息之前、指定数量的最新消息实体
    public void getLocalHistoryMessage() {
        //没有消息第一次调用应设置为:-1。
        if (!mAdapter.loadNextPage() && !StoredTopic.isAllDataLoaded(mTopic, mTopic.getBeginId())) { // for msg fetch
            Log.d(TAG, "Calling server for more data");
            try {
                mTopic.getMeta(mTopic.getMetaGetBuilder().withGetEarlierData2(MESSAGES_TO_LOAD).build())
                        .thenApply(
                                new PromisedReply.SuccessListener<ServerMessage>() {
                                    @Override
                                    public PromisedReply<ServerMessage> onSuccess(ServerMessage result) throws Exception {
                                        getRefreshLayout().endRefreshing();
                                        return null;
                                    }
                                },
                                new PromisedReply.FailureListener<ServerMessage>() {
                                    @Override
                                    public PromisedReply<ServerMessage> onFailure(Exception err) throws Exception {
                                        getRefreshLayout().endRefreshing();
                                        return null;
                                    }
                                }
                        );
            } catch (Exception e) {
                getRefreshLayout().endRefreshing();
            }
        } else {
            getRefreshLayout().endRefreshing();
        }
    }

    private void loadError(Throwable throwable) {
        LogUtils.sf(throwable.getLocalizedMessage());
    }

    //////////////send file by tinode ////////
    // 整体的文件发送逻辑：分两种，一是tinode自带的文件发送，二是使用seaweed。
    //tinode发送文件分两种：消息体携带文件、文件url。涉及两种消息IM、EX。
    //IM是消息体中携带文件，文件为图片。
    // EX会包括以上两种情况，以文件大小区分。
    //
    //seaweed发送文件只有一种：文件url。涉及两种消息IMAGE、EX。
    //IMAGE文件为图片。
    // EX包括各种文件格式。
    //
    @NonNull
    @Override
    public Loader<UploadResult> onCreateLoader(int id, Bundle args) {
        return new FileUploader(mContext, args);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<UploadResult> loader, final UploadResult data) {
        final SessionActivity activity = (SessionActivity) mContext;

        if (activity != null) {
            // Kill the loader otherwise it will keep uploading the same file whenever the activity
            // is created.
            LoaderManager.getInstance(activity).destroyLoader(loader.getId());
        } else {
            return;
        }

        // Avoid processing the same result twice;
        if (data.processed) {
            return;
        } else {
            data.processed = true;
        }

        if (data.msgId > 0) {
            activity.syncMessages(data.msgId, true);
        } else if (data.msgId == -1) {
            runLoader(true);
            //ToastUtils.showLong(data.error);
        } else if (data.msgId == -2) {
            ToastUtils.showLong(data.error);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<UploadResult> loader) {
    }

    private static class FileUploader extends AsyncTaskLoader<UploadResult> {
        private static WeakReference<UploadProgress> sProgress;
        private final Bundle mArgs;
        private UploadResult mResult = null;

        FileUploader(Activity activity, Bundle args) {
            super(activity);
            mArgs = args;
        }

        static void setProgressHandler(UploadProgress progress) {
            sProgress = new WeakReference<>(progress);
        }

        @Override
        public void onStartLoading() {
            long msgId = -1;
            if (mResult != null) {
                // Loader has result already. Deliver it.
                deliverResult(mResult);
            } else if (mArgs.getLong("msgId") <= 0) {
                // Create a new message which will be updated with upload progress.
                Storage store = BaseDb.getInstance().getStore();
                final int requestCode = mArgs.getInt("requestCode");
                Drafty drafty = new Drafty();
                if (requestCode == ACTION_ATTACH_FILE){
                    drafty.attachFile(null, "null", "未知文件", 0, null, null);
                }
                else if (requestCode == ACTION_ATTACH_IMAGE) {
                    //TODO fix type meaning
                    //drafty.attachImage(null, null,"null.png", 0, null);
                    drafty.attachImage(null, null,"未知图片", 0, null);
                }

//                if (requestCode == ACTION_ATTACH_FILE) {
//                    drafty.attachFile(null, "null", "null.mp4", 0, null, null);
//                } else if (requestCode == ACTION_ATTACH_IMAGE) {
//                    drafty.insertImage(0, null, "null", 0, 0, "null.png", null);
//                }
                msgId = store.msgDraft(Cache.getTinode().getTopic(mArgs.getString("topic")), drafty);
                mArgs.putLong("msgId", msgId);
            } else {
                msgId = mArgs.getLong("msgId");
            }
            BuglyLog.d(TAG,"new drafty msgId="+msgId);
            UploadProgress p = sProgress.get();
            if (p != null) {
                p.onStart(msgId);
            }else{
                BuglyLog.d(TAG,"Wow!!!,progress p==null");
            }
            forceLoad();

        }

        @Nullable
        @Override
        public UploadResult loadInBackground() {
            // Don't upload again if upload was completed already.
            if (mResult == null) {
                mResult = doUpload(getId(), getContext(), mArgs, sProgress);
            }
            return mResult;
        }

        @Override
        public void onStopLoading() {
            super.onStopLoading();
            cancelLoad();
        }
    }

    private static Bundle getFileDetails(final Context context, Uri uri) {
        final ContentResolver resolver = context.getContentResolver();
        String fname = null;
        long fsize = 0L;

        String mimeType = resolver.getType(uri);
        if (mimeType == null) {
            mimeType = UiUtils.getMimeType(uri);
        }

        Cursor cursor = resolver.query(uri, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            fname = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            fsize = cursor.getLong(cursor.getColumnIndex(OpenableColumns.SIZE));
            cursor.close();
        }

        String path = UiUtils.getPath(context, uri);
        // Still no size? Try opening directly.
        if (fsize == 0) {
            if (path != null) {
                File file = new File(path);
                if (fname == null) {
                    fname = file.getName();
                }
                fsize = file.length();
            }
        }
        if(fname == null && path != null) {
            fname = path.substring(path.lastIndexOf("/") + 1);
        }

        Bundle result = new Bundle();
        result.putString("mime", mimeType);
        result.putString("name", fname);
        result.putLong("size", fsize);
        result.putString("path", path);
        return result;
    }

    // Send file as a link.消息适配及显示
    private static Drafty draftyAttachment(String mimeType, String fname, String refUrl, long size, String localPath, String cover) {
        Drafty content = new Drafty();
        content.attachFile(mimeType, refUrl, fname, size, cover, localPath, Drafty.SEND_FILE_TYPE_TINODE);
        return content;
    }

    // Send image using tinode largefile mode
    private static Drafty draftyImage(String mimeType, String fname, String refUrl, long size, String localPath) {
        Drafty content = new Drafty();
        content.attachImage(mimeType, refUrl, fname, size, localPath);
        return content;
    }

    private static Drafty.Cover genUploadCover(String fname, final WeakReference<UploadProgress> callbackProgress, String path) {
        Drafty.Cover cover = null;
        // 判断是否为视频，视频先生成cover。drafty中需要push多个entity，需要上传一个图片和一个文件。
        if (MediaFileUtils.isVideoFileType(fname)) {
            UploadProgress p = callbackProgress.get();
            if (p != null) {
                p.showThumbProgressDialog();
            }

            File coverFile = VideoThumbUploader.getInstance().createThumbSync(path, 200, 200);

            if (coverFile != null) {
                cover = new Drafty.Cover();
                cover.fname = coverFile.getName();
                cover.mime = UiUtils.getMimeType(Uri.fromFile(coverFile));
                cover.localPath = coverFile.getAbsolutePath();

                // Upload then send message with a link. This is a long-running blocking call.
                final LargeFileHelper uploader = Cache.getTinode().getFileUploader();
                try {
                    MsgServerCtrl ctrl = ctrl = uploader.upload(new FileInputStream(coverFile), cover.fname, cover.mime, coverFile.length(),
                            null);
                    boolean success = (ctrl != null && ctrl.code == 200);
                    if (success) {
                        cover.refurl = ctrl.getStringParam("url", null);
                        Log.e(TAG, "genUploadCover url:" + cover.refurl);
                    } else {
                        if (p != null) {
                            p.showToast("封面上传失败");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (p != null) {
                        p.showToast("封面上传失败");
                    }
                }
            }

            if (p != null) {
                p.hideThumbProgressDialog();
            }
        }

        return cover;
    }

    private static UploadResult doUpload(final int loaderId, final Context context, final Bundle args,
                                         final WeakReference<UploadProgress> callbackProgress) {
        final UploadResult result = new UploadResult();
        Storage store = BaseDb.getInstance().getStore();

        final int requestCode = args.getInt("requestCode");
        final String topicName = args.getString("topic");
        final Uri uri = args.getParcelable("uri");
        Uri dstUri=uri;
        result.msgId = args.getLong("msgId");

        if (uri == null) {
            Log.w(TAG, "Received null URI");
            result.error = "Null URI";
            return result;
        }

        final Topic topic = Cache.getTinode().getTopic(topicName);

        Drafty content = null;
        Drafty.Cover cover = null;
        boolean success = false;
        InputStream is = null;
        try {
            int imageWidth = 0, imageHeight = 0;

            Bundle fileDetails = getFileDetails(context, uri);
            String fname = fileDetails.getString("name");
            long fsize = fileDetails.getLong("size");
            String mimeType = fileDetails.getString("mime");
            String path = fileDetails.getString("path");

            if (fsize == 0) {
                Log.w(TAG, "File size is zero " + uri);
                store.msgDiscard(topic, result.msgId);
                result.error = "不能发送大小为0的文件";
                result.msgId = -2;
                return result;
            }

            if (fname == null) {
                fname = context.getString(R.string.default_attachment_name);
            }

            final ContentResolver resolver = context.getContentResolver();
            is = resolver.openInputStream(uri);
            //超过文件最大限制直接返回
            if (fsize > MAX_ATTACHMENT_SIZE) {
                Log.w(TAG, "File is too big, size=" + fsize);

                store.msgDiscard(topic, result.msgId);
                result.msgId = -2;
                result.error = context.getString(R.string.attachment_too_large,
                        UiUtils.bytesToHumanSize(fsize), UiUtils.bytesToHumanSize(MAX_ATTACHMENT_SIZE));
                return result;
            }

            //发送图片
            if (requestCode == ACTION_ATTACH_IMAGE) {
                //先压缩
                String compressedPath=uri.toString();
                String srcPicPath=UiUtils.getPath(context, uri);
                //File file=new File(compressedPath);

                File fileLuban = new File(AppConst.LUBAN_SAVE_DIR);
                if (!fileLuban.exists())
                    fileLuban.mkdirs();

                List<String> stringList=new ArrayList<>();
                stringList.add(srcPicPath);

                //File file=new File(srcPicPath);
                //long srcLength=file.length();

                List<File> fileOutList = Luban.with(context).load(stringList).setTargetDir(AppConst.LUBAN_SAVE_DIR).get();

                if(fileOutList.size()>0){
                    fsize=fileOutList.get(0).length();
                    dstUri=Uri.fromFile(fileOutList.get(0));
                    is.close();
                    is = resolver.openInputStream(dstUri);
                    Drafty upContent = draftyImage(mimeType, fname, fileOutList.get(0).toString(), -1, UiUtils.getPath(context, dstUri));
                    store.msgDraftUpdate(topic, result.msgId, upContent);
                }else{
                    Drafty upContent = draftyImage(mimeType, fname, uri.toString(), -1, UiUtils.getPath(context, uri));
                    store.msgDraftUpdate(topic, result.msgId, upContent);
                }


            } else if (requestCode == ACTION_ATTACH_FILE) {
                // 判断是否为视频，视频先生成cover。drafty中需要push多个entity，需要上传一个图片和一个文件。
                //下面这行就做了上传cover的工作,上传完把url返回给cover,是为了后面再上传视频文件的时候，把cover里面的信息
                //附带上去,也就说这个genUploadCover内部已经用LargeFileHelper上传了cover,然后返回了cover的url而已
                cover = genUploadCover(fname, callbackProgress, path);
                Drafty upContent = draftyAttachment(mimeType, fname, uri.toString(), fsize, UiUtils.getPath(context, uri), cover == null ? null : cover.refurl).attachFileCover(cover);
                store.msgDraftUpdate(topic, result.msgId, upContent);
            }

            UploadProgress start = callbackProgress.get();
            if (start != null) {
                start.onStart(result.msgId);
                // This assignment is needed to ensure that the loader does not keep
                // a strong reference to activity while potentially slow upload process
                // is running.
                start = null;
            }

            // Upload then send message with a link. This is a long-running blocking call.
            final LargeFileHelper uploader = Cache.getTinode().getFileUploader();

            MsgServerCtrl ctrl = uploader.upload(is, fname, mimeType, fsize,
                    new LargeFileHelper.FileHelperProgress() {
                        @Override
                        public void onProgress(long progress, long size) {
                            UploadProgress p = callbackProgress.get();
                            if (p != null) {
                                if (!p.onProgress(loaderId, result.msgId, progress, size)) {
                                    uploader.cancel();
                                }
                            }
                        }
                    });
            is.close();
            success = (ctrl != null && ctrl.code == 200);

            if (success) {
                if (requestCode == ACTION_ATTACH_IMAGE) {
                    content = draftyImage(mimeType, fname, ctrl.getStringParam("url", null), fsize, UiUtils.getPath(context, dstUri));
                } else {
                    content = draftyAttachment(mimeType, fname, ctrl.getStringParam("url", null), fsize, UiUtils.getPath(context, uri), cover == null ? null : cover.refurl).attachFileCover(cover);
                }
            }

        }
        catch (Exception ex) {
            result.error = ex.getMessage();
            if (!"cancelled".equals(result.error)) {
                Log.w(TAG, "Failed to attach file", ex);
            }
        }
//        catch (IOException | NullPointerException ex) {
//            result.error = ex.getMessage();
//            if (!"cancelled".equals(result.error)) {
//                Log.w(TAG, "Failed to attach file", ex);
//            }
//        }
        finally {

        }
        if (result.msgId > 0) {
            if (success) {
                // Success: mark message as ready for delivery. If content==null it won't be saved.
                store.msgReady(topic, result.msgId, content);
            } else {
                // Failure: discard draft.
                store.msgFailed(topic, result.msgId, content);
                result.msgId = -1;
                result.error = "发送失败";
            }
        }
        return result;
    }

    static class UploadResult {
        String error;
        long msgId = -1;
        boolean processed = false;

        UploadResult() {
        }

        @NonNull
        public String toString() {
            return "msgId=" + msgId + ", error='" + error + "'";
        }
    }

    private class UploadProgress {
        ProgressDialog dialog = null;
        private long lastTime = 0;

        UploadProgress() {
        }

        void onStart(final long msgId) {
            // Reload the cursor.
            lastTime = 0;
            runLoader(true);
        }

        /**
         * 显示生成封面中的等待框
         */
        void showThumbProgressDialog() {
            mContext.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dialog = new ProgressDialog(mContext);
                    dialog.setMessage("正在生成封面，请稍后");
                    dialog.show();
                }
            });
        }

        void hideThumbProgressDialog() {
            mContext.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
            });
        }

        void showToast(String msg) {
            mContext.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.showShort(msg);
                }
            });
        }

        // Returns true to continue the upload, false to cancel.
        boolean onProgress(final int loaderId, final long msgId, final long progress, final long total) {
            // Check for cancellation.
            Integer oldLoaderId = mAdapter.getLoaderMapping(msgId);
            if (oldLoaderId == null) {
                mAdapter.addLoaderMapping(msgId, loaderId);
            } else if (oldLoaderId != loaderId) {
                // Loader id has changed, cancel.
                return false;
            }

            if (mContext == null) {
                return true;
            }

            long t = System.currentTimeMillis();
            if(lastTime == 0) lastTime = t;
            if (t - lastTime > 200) {
                lastTime = t;
                mContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        final int position = mAdapter.getPostionByMsgId(msgId);

                        if (position < 0) {
                            return;
                        }

                        //upload progress
                        UploadAndGetUrl.FileInfo info = new UploadAndGetUrl.FileInfo();
                        info.progress = (int) ((total > 0 ? (float) progress / total : (float) progress) * 100);
                        mAdapter.notifyItemChanged(position, info);
                    }
                });
            }

            return true;
        }
    }
}

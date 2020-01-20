package com.yixin.tinode.ui.service;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.yixin.tinode.R;
import com.yixin.tinode.api.param.TiSetting;
import com.yixin.tinode.app.AppConst;
import com.yixin.tinode.db.tinode.StoredMessage;
import com.yixin.tinode.tinode.Cache;
import com.yixin.tinode.tinode.account.Utils;
import com.yixin.tinode.ui.activity.SessionActivity;
import com.yixin.tinode.ui.activity.SettingNewMsgNotifyActivity;
import com.yixin.tinode.ui.adapter.SessionAdapter;
import com.zuozhan.app.activity.ZHMainActivity;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import co.tinode.tinodesdk.NotConnectedException;
import co.tinode.tinodesdk.PromisedReply;
import co.tinode.tinodesdk.Tinode;
import co.tinode.tinodesdk.Topic;
import co.tinode.tinodesdk.model.MsgServerCtrl;
import co.tinode.tinodesdk.model.MsgServerData;
import co.tinode.tinodesdk.model.MsgServerInfo;
import co.tinode.tinodesdk.model.MsgServerMeta;
import co.tinode.tinodesdk.model.MsgServerPres;
import co.tinode.tinodesdk.model.ServerMessage;
import co.tinode.tinodesdk.model.Subscription;
import co.tinode.tinodesdk.model.VCard;

import static android.support.v4.app.NotificationCompat.PRIORITY_LOW;
import static com.lzy.okgo.utils.HttpUtils.runOnUiThread;
import static com.yixin.tinode.ui.activity.MainActivity.datasetChanged;


public class BackgroundService extends Service {

    private Context mContext;
    private BroadcastReceiver mBrNet;
    private BroadcastReceiver mBrNoTinodeListener;

    public BackgroundService() {
    }

    @Override
    public void onCreate() {
        mContext = this;
        super.onCreate();
        startForeground(1, getNotification());
        regBroadcast();
    }

    public Notification getNotification() {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, createNotifyChannel((NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE)))
                .setSmallIcon(R.mipmap.ic_launcher).setContentTitle("作战指挥平台");
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(mContext, ZHMainActivity.class), 0);
        Notification notification = mBuilder
                .setPriority(PRIORITY_LOW)
                .setChannelId(NOTIFY_CHANNEL_MSG)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setContentIntent(pendingIntent)
                .build();
        notification.flags |= Notification.FLAG_NO_CLEAR;

        return notification;
    }

    private String createNotifyChannel(NotificationManager mNotificationManager) {
        String channel = "";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            if (mNotificationManager == null) {
                mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            }
            if (mNotificationManager.getNotificationChannel(NOTIFY_CHANNEL_MSG) != null) {
                return channel;
            }
            CharSequence name = "通知";
            String description = "消息通知";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(NOTIFY_CHANNEL_MSG, name, importance);
            mChannel.setDescription(description);
            mChannel.enableLights(true);
//
            mNotificationManager.createNotificationChannel(mChannel);

            channel = NOTIFY_CHANNEL_MSG;
        }

        return channel;
    }
//    @NonNull
//    @TargetApi(26)
//    private synchronized String createChannel() {
//        NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
//
//        String name = "snap map fake location ";
//        int importance = NotificationManager.IMPORTANCE_LOW;
//
//        NotificationChannel mChannel = new NotificationChannel("snap map channel", name, importance);
//
//        mChannel.enableLights(true);
//        mChannel.setLightColor(Color.BLUE);
//        if (mNotificationManager != null) {
//            mNotificationManager.createNotificationChannel(mChannel);
//        } else {
//            stopSelf();
//        }
//        return "snap map channel";
//    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregBroadcast();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void reg(String action, BroadcastReceiver br) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(action);
        registerReceiver(br, filter);
    }

    private final String NOTIFY_CHANNEL_MSG = "tinode_channel_msg";


    private void regBroadcast() {
        reg(ConnectivityManager.CONNECTIVITY_ACTION, mBrNet = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (NetworkUtils.isConnected()) {
                    try {
                        Tinode tinode = Cache.getTinode();
                        if (!tinode.reconnectNow()) {
                            final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
                            String hostName = sharedPref.getString(Utils.PREFS_HOST_NAME, Cache.HOST_NAME);
                            boolean tls = sharedPref.getBoolean(Utils.PREFS_USE_TLS, false);
                            tinode.connect(hostName, tls).getResult();
                            Log.e("", "网络已连接，后台自动重连2");
                        } else {
                            Log.e("", "网络已连接，后台自动重连1");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });


        Handler handler = new Handler(Looper.getMainLooper());
        reg(AppConst.ACTION_NO_TINODE_LISTENER, mBrNoTinodeListener = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Activity top = ActivityUtils.getTopActivity();
                if (top != null && top.getClass().getName().contains("SessionActivity")) {
                    //有活跃的activity，不需要处理
                    Log.e("", "ACTION_NO_TINODE_LISTENER app活跃不需要处理");
                    return;
                } else {
                }
                Log.e("", "ACTION_NO_TINODE_LISTENER app进入后台");

                TiSetting notify = SettingNewMsgNotifyActivity.getSettingNotify(context);
                if (notify != null && notify.getNotification() == AppConst.FALSE) {
                    return;
                }

                Tinode tinode = Cache.getTinode();
                tinode.setListener(new Tinode.EventListener() {
                    @Override
                    public void onDataMessage(MsgServerData data) {
                        super.onDataMessage(data);
                        if (data.content.txt.contains("isRTPMsg")) {
                            return;
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                datasetChanged(data.topic, data.ts,data.from, data.content.txt);

                            }
                        });

                    }

                    @Override
                    public void onDisconnect(boolean byServer, int code, String reason) {
                        // Show that we are disconnected
                        if (code <= 0) {
                            Log.d("", "Network error");
                        } else {
                            Log.d("", "Tinode error: " + code);

                            try {
                                Tinode tinode = Cache.getTinode();
                                if (!tinode.reconnectNow()) {
                                    final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
                                    String hostName = sharedPref.getString(Utils.PREFS_HOST_NAME, Cache.HOST_NAME);
                                    boolean tls = sharedPref.getBoolean(Utils.PREFS_USE_TLS, false);
                                    tinode.connect(hostName, tls).getResult();
                                    ToastUtils.showShort("后台自动重连2");
                                } else {
                                    ToastUtils.showShort("后台自动重连1");
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (URISyntaxException e) {
                                e.printStackTrace();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onConnect(int code, String reason, Map<String, Object> params) {
                        super.onConnect(code, reason, params);
                    }

                    @Override
                    public void onLogin(int code, String text) {
                        super.onLogin(code, text);
                    }

                    @Override
                    public void onMessage(ServerMessage msg) {
                        super.onMessage(msg);
                    }

                    @Override
                    public void onRawMessage(String msg) {
                        super.onRawMessage(msg);
                    }

                    @Override
                    public void onCtrlMessage(MsgServerCtrl ctrl) {
                        super.onCtrlMessage(ctrl);
                    }

                    @Override
                    public void onInfoMessage(MsgServerInfo info) {
                        super.onInfoMessage(info);
                    }

                    @Override
                    public void onMetaMessage(MsgServerMeta meta) {
                        super.onMetaMessage(meta);
                    }

                    @Override
                    public void onPresMessage(MsgServerPres pres) {
                        super.onPresMessage(pres);
                        if (pres.parseWhat(pres.what) == MsgServerPres.What.MSG) {
                            Log.e("pres listener  equal", pres.what);
                            Topic mTopic = tinode.getTopic(pres.src);
                            if (mTopic != null) {
                                mTopic.setListener(new Topic.Listener() {
                                    @Override
                                    public void onData(MsgServerData data) {
                                        super.onData(data);
                                        if (data.content.txt.contains("isRTPMsg")) {
                                            return;
                                        }
                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {

                                                createNotification(data, notify);

                                                if (mTopic != null) {
                                                    mTopic.setListener(null);

                                                    // Deactivate current topic
                                                    if (mTopic.isAttached()) {
                                                        try {
                                                            mTopic.leave();
                                                        } catch (Exception ex) {
                                                            Log.e("", "something went wrong in Topic.leave", ex);
                                                        }
                                                    }
                                                }
                                            }
                                        });
                                    }
                                });
                                if (!mTopic.isAttached()) {
                                    try {
                                        mTopic.subscribe(null,
                                                mTopic.getMetaGetBuilder()
                                                        .withGetSub()
                                                        .withGetData()
                                                        .build()).thenApply(new PromisedReply.SuccessListener<ServerMessage>() {
                                            @Override
                                            public PromisedReply<ServerMessage> onSuccess(ServerMessage result) throws Exception {
                                                return null;
                                            }
                                        }, null);
                                    } catch (NotConnectedException ignored) {
                                        Log.d("", "Offline mode, ignore");
                                    } catch (Exception ex) {
                                        Log.e("", "something went wrong", ex);
                                    }
                                }
                            }
                        } else {
                            Log.e("pres listener not equal", pres.what);
                        }
                    }
                });
            }
        });
    }

    private void unregBroadcast() {
        if (mBrNet != null) {
            unregisterReceiver(mBrNet);
        }

        if (mBrNoTinodeListener != null) {
            unregisterReceiver(mBrNoTinodeListener);
        }
    }


    //自动重连
    //启动service
    private void createNotification(MsgServerData data, TiSetting notify) {
        String title = "";
        String topicName = data.topic;
        Topic topic = Cache.getTinode().getTopic(topicName);
        Subscription<VCard, ?> sub = topic != null ? topic.getSubscription(data.from) : null;
        if (sub != null && sub.pub != null) {
            title = sub.pub.fn;
        }

        // Start without a delay
        // Vibrate for 100 milliseconds
        // Sleep for 1000 milliseconds
        long[] pattern = {0, 100, 1000};


        //ToastUtils.showShort("收到消息：" + data.content.txt);
        NotificationManager notificationManager = (NotificationManager) BackgroundService.this
                .getSystemService(android.content.Context.NOTIFICATION_SERVICE);
        Notification.Builder builder;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            NotificationChannel mChannel = new NotificationChannel(NOTIFY_CHANNEL_MSG, "通知", NotificationManager.IMPORTANCE_HIGH);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(mChannel);
            }
            builder = new Notification.Builder(BackgroundService.this, NOTIFY_CHANNEL_MSG);
        } else {
            builder = new Notification.Builder(BackgroundService.this);
        }
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        builder.setTicker("您有一条新消息");
        builder.setContentTitle(title);
        builder.setContentText(SessionAdapter.getMsgText(new StoredMessage(data)));

        if (notify == null || notify.getVibrate() == AppConst.TRUE) {
            builder.setVibrate(pattern);
        }
        if (notify == null || notify.getSound() == AppConst.TRUE) {
            builder.setSound(Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getPackageName() + "/" + R.raw.tip));
        }

        Intent intent = new Intent(mContext, SessionActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("sessionId", topicName);
        if (Topic.getTopicTypeByName(topicName) == Topic.TopicType.P2P) {
            intent.putExtra("sessionType", SessionActivity.SESSION_TYPE_PRIVATE);
        } else {
            intent.putExtra("sessionType", SessionActivity.SESSION_TYPE_GROUP);
        }
        intent.setAction("com.yixin.tinode.ui.activity.SessionActivity");
        PendingIntent pendingIntent = PendingIntent.getActivity(BackgroundService.this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        builder.setContentIntent(pendingIntent);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            builder.setChannelId(NOTIFY_CHANNEL_MSG);
        }
        Notification build = builder.build();
        build.flags = Notification.FLAG_AUTO_CANCEL;
        if (notificationManager != null) {
            notificationManager.notify((int) System.currentTimeMillis(), build);
        }
    }
}

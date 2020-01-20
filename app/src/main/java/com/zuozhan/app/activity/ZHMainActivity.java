package com.zuozhan.app.activity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationManagerCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.starrtc.demo.demo.RTCHttpService;
import com.starrtc.demo.demo.VoipHttpListener;
import com.starrtc.demo.demo.ZHHttpManager;
import com.starrtc.demo.utils.AEvent;
import com.starrtc.demo.utils.IEventListener;
import com.yixin.tinode.R;
import com.yixin.tinode.api.param.TiSetting;
import com.yixin.tinode.app.AppConst;
import com.yixin.tinode.app.MyApp;
import com.yixin.tinode.db.tinode.BaseDb;
import com.yixin.tinode.db.tinode.StoredTopic;
import com.yixin.tinode.db.tinode.TopicDb;
import com.yixin.tinode.db.tinode.UserDb;
import com.yixin.tinode.manager.BroadcastManager;
import com.yixin.tinode.tinode.Cache;
import com.yixin.tinode.tinode.util.UiUtils;
import com.yixin.tinode.ui.activity.SessionActivity;
import com.yixin.tinode.ui.activity.SettingAVActivity;
import com.yixin.tinode.ui.activity.SettingNewMsgNotifyActivity;
import com.yixin.tinode.ui.fragment.ContactsFragment;
import com.yixin.tinode.ui.fragment.RecentMessageFragment;
import com.yixin.tinode.ui.service.BackgroundService;
import com.yixin.tinode.util.LogUtils;
import com.yixin.tinode.util.Utils;
import com.zuozhan.app.AppEnvirment;
import com.zuozhan.app.BaseIP;
import com.zuozhan.app.RouterUtil;
import com.zuozhan.app.adapter.MyAdapter;
import com.zuozhan.app.bean.RenWuLeiBean;
import com.zuozhan.app.bean.UserBean;
import com.zuozhan.app.fragment.Fragment1;
import com.zuozhan.app.fragment.Fragment4;
import com.zuozhan.app.httpUtils.HttpUtil;
import com.zuozhan.app.util.AppManager;
import com.zuozhan.app.util.LocationUtil;
import com.zuozhan.app.util.LogUtil;
import com.zuozhan.app.util.ShareprefrensUtils;

import java.util.ArrayList;
import java.util.Date;

import co.tinode.tinodesdk.MeTopic;
import co.tinode.tinodesdk.NotConnectedException;
import co.tinode.tinodesdk.NotSynchronizedException;
import co.tinode.tinodesdk.PromisedReply;
import co.tinode.tinodesdk.Tinode;
import co.tinode.tinodesdk.Topic;
import co.tinode.tinodesdk.User;
import co.tinode.tinodesdk.model.Description;
import co.tinode.tinodesdk.model.MsgServerData;
import co.tinode.tinodesdk.model.MsgServerInfo;
import co.tinode.tinodesdk.model.MsgServerPres;
import co.tinode.tinodesdk.model.ServerMessage;
import co.tinode.tinodesdk.model.Subscription;
import co.tinode.tinodesdk.model.VCard;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.yixin.tinode.util.Utils.isNetworkConnected;
import static com.yixin.tinode.util.Utils.showNetworkAlert;

public class ZHMainActivity extends AllBaseActivity {
    private static final String TAG = "ZHMainActivity";
    FrameLayout frameLayout;
    BottomNavigationView bottomNavigationView;
    ArrayList<Fragment> fragments = new ArrayList<>();
    Fragment fragment = null;
    MyAdapter adapter;
    ImageView renwu_btn2;
    private MeListener mMeTopicListener = null;
    int type = 0;
    RenWuLeiBean.DataBean dataBean;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zh_activity_main);
        type = getIntent().getIntExtra("type", 0);
        dataBean = (RenWuLeiBean.DataBean) getIntent().getSerializableExtra("isShowEx");
        initView();
        initData();

        renwu_btn2 = findViewById(R.id.renwu_btn2);
        if (dataBean == null) {
            initSDK();
            renwu_btn2.setVisibility(View.GONE);
        } else {
            renwu_btn2.setVisibility(View.VISIBLE);
            renwu_btn2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    RouterUtil.goActivity(ZHMainActivity.this,
                            ZHRenwuInfoActivity.class, dataBean);
                }
            });

            String topicTmp = getIntent().getStringExtra("topicTmp");
            if (!TextUtils.isEmpty(topicTmp)){
                try {
                    Observable.just(TopicDb.readOne(BaseDb.getInstance().getWritableDatabase(), Cache.getTinode(), topicTmp))
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(subscription -> {
                                if (subscription != null) {
                                    subscription.setState(Topic.STATE_NORMAL);
                                    TopicDb.updateState(BaseDb.getInstance().getWritableDatabase(), ((StoredTopic) subscription.getLocal()).id, Topic.STATE_NORMAL);
                                }
                            }, this::loadUserError);
                }catch (Exception e){}
                Intent intent = new Intent(this, SessionActivity.class);
                intent.putExtra("sessionId", topicTmp);
                intent.putExtra("sessionType", SessionActivity.SESSION_TYPE_PRIVATE);
                jumpToActivity(intent);
            }
        }

        AEvent.addListener(AEvent.AEVENT_LOGOUT, new IEventListener() {
            @Override
            public void dispatchEvent(String aEventID, boolean success, Object eventObj) {
                goHome();
            }
        });
        AEvent.addListener(AEvent.AEVENT_LOGOUT_2, new IEventListener() {
            @Override
            public void dispatchEvent(String aEventID, boolean success, Object eventObj) {
                AppManager.finishAllActivity();
            }
        });
        ZHHttpManager.getZhHttpManager().setRtcHttpService(new RTCHttpService() {
            @Override
            public void getHeadPicById(String id, VoipHttpListener voipHttpListener) {
                HttpUtil.getHeadPicById(id, new HttpUtil.Callback<UserBean>() {
                    @Override
                    public void onResponse(UserBean call) {
                        if (call!=null && call.data!=null && voipHttpListener!=null){
                            try {
                                String name =call.data.username;
                                String headpic =call.data.headPic;
                                voipHttpListener.onUserInfo(name,headpic);
                            } catch (Exception e){}

                        }
                    }
                });
            }
        });
        LocationUtil.getIntance().startLocation();
    }

    private void loadUserError(Throwable throwable) {
        LogUtils.sf(throwable.getLocalizedMessage());
    }
    private void initSDK() {
        AEvent.setHandler(new Handler());
        connectRTC();
    }


    public void initListener() {
        fragment = new Fragment1();
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.homeItem:
                        fragment = fragments.get(0);
                        break;
                    case R.id.vipItem:
                        fragment = fragments.get(1);
                        break;
                    case R.id.cheItem:
                        fragment = fragments.get(2);
                        break;
                    case R.id.meItem:
                        fragment = fragments.get(3);
                        break;
                }

                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                for (int i = 0; i < fragments.size(); i++) {
                    if (!fragments.get(i).isHidden()) {
                        fragmentTransaction.hide(fragments.get(i));
                    }
                }

                if (fragment.isAdded()) {
                    fragmentTransaction.show(fragment).commit();
                } else {
                    try {
                        fragmentTransaction.add(R.id.frame, fragment).show(fragment).commit();
                    } catch (Throwable t){}
                }

                return true;
            }
        });
    }


    private void initAdapter() {
        adapter = new MyAdapter(getSupportFragmentManager(), fragments);
    }

    public void initView() {
        frameLayout = findViewById(R.id.frame);
        bottomNavigationView = findViewById(R.id.bottomNavigat);
    }

    TextView redView;
    public void initData() {
        if (!BaseIP.quchu_im){
            initIMSetting();
        }

        AppEnvirment.userBean =
                new Gson().fromJson(ShareprefrensUtils.getSharePreferences(ShareprefrensUtils.USERINFO, ""), UserBean.class);
        fragments.add(new Fragment1());
        fragments.add(new RecentMessageFragment());
        fragments.add(new ContactsFragment());
        fragments.add(new Fragment4());
        initAdapter();
        initListener();

        if (type == 0) {
            bottomNavigationView.setSelectedItemId(R.id.homeItem);
        } else if (type == 1) {
            bottomNavigationView.setSelectedItemId(R.id.vipItem);
        } else if (type == 2) {
            bottomNavigationView.setSelectedItemId(R.id.cheItem);
        } else if (type == 3) {
            bottomNavigationView.setSelectedItemId(R.id.meItem);
        }
        BottomNavigationMenuView menuView = null;
        for (int i = 0; i < bottomNavigationView.getChildCount(); i++) {
            View child = bottomNavigationView.getChildAt(i);
            if (child instanceof BottomNavigationMenuView) {
                menuView = (BottomNavigationMenuView) child;
                break;
            }
        }
        ViewGroup viewGroup = (ViewGroup) menuView.getChildAt(1);
        redView = new TextView(this);
        redView.setText("");
        redView.setGravity(Gravity.CENTER);
        redView.setTextColor(Color.parseColor("#ffffff"));
        redView.setBackgroundResource(R.drawable.shape_red_dot);
        BottomNavigationItemView.LayoutParams params = new BottomNavigationItemView.LayoutParams(
                45, 45);
        params.gravity = Gravity.CENTER_HORIZONTAL;
        redView.setVisibility(View.INVISIBLE);
        params.leftMargin = 40;
        params.topMargin = 10;
        viewGroup.addView(redView, params);
    }

    private void initIMSetting() {
        registerBR();

        mMeTopicListener = new MeListener();
        TiSetting setting = SettingNewMsgNotifyActivity.getSettingNotify(this);
        if (setting == null) {
            setting = new TiSetting();
            setting.setNotification(AppConst.TRUE);
            setting.setVibrate(AppConst.TRUE);
            setting.setSound(AppConst.TRUE);
            setting.setNotification(AppConst.TRUE);
            setting.setAvServerAddr(AppConst.AV_ADDRESS);
            setting.setAvServerPort(AppConst.AV_PORT);
            setting.setAvQuality(AppConst.AV_QUALITY);
            SettingNewMsgNotifyActivity.save(this, setting);
        }
        //we can append av setting to settings
        TiSetting tmp_setting = SettingAVActivity.getSettingNotify(this);
        setting.setAvServerAddr(tmp_setting.getAvServerAddr());
        setting.setAvServerPort(tmp_setting.getAvServerPort());
        setting.setAvQuality(tmp_setting.getAvQuality());
        if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("提示")
                    .setMessage("检测到您没有打开通知权限，请设置")
                    .setPositiveButton("设置", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent localIntent = new Intent();
                            localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            if (Build.VERSION.SDK_INT >= 9) {
                                localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                                localIntent.setData(Uri.fromParts("package", ZHMainActivity.this.getPackageName(), null));
                            } else if (Build.VERSION.SDK_INT <= 8) {
                                localIntent.setAction(Intent.ACTION_VIEW);

                                localIntent.setClassName("com.android.settings",
                                        "com.android.settings.InstalledAppDetails");

                                localIntent.putExtra("com.android.settings.ApplicationPkgName",
                                        ZHMainActivity.this.getPackageName());
                            }
                            startActivity(localIntent);
                        }
                    }).show();
        }
    }


    @Override
    public void onResume() {
        super.onResume();

//        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//        nm.cancelAll();
        final Tinode tinode = Cache.getTinode();
        tinode.setListener(new UiUtils.EventListener(this, tinode.isConnected()));

        MeTopic me = tinode.getMeTopic();
        if (me == null) {
            // The very first launch of the app.
            me = new MeTopic<>(tinode, mMeTopicListener);
//			me.setTypes(VCard.class, String.class);
            Log.d(TAG, "Initialized NEW 'me' topic");
        } else {
            me.setListener(mMeTopicListener);
            Log.d(TAG, "Loaded existing 'me' topic");
        }

        subscribe(tinode, me);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(this, BackgroundService.class));
        } else {
            startService(new Intent(this, BackgroundService.class));
        }
        sendBroadcast(new Intent(AppConst.ACTION_NO_TINODE_LISTENER));
    }


    private void subscribe(Tinode tinode, MeTopic me) {
        if (!me.isAttached()) {
            try {
                Log.d(TAG, "Trying to subscribe to me");
                me.subscribe(null, me
                        .getMetaGetBuilder()
                        .withGetDesc()
                        .withGetSub()
//                        .withGetData()
                        .build());
            } catch (NotSynchronizedException ignored) {
                Log.d(TAG, "Trying to subscribe to me NotSynchronizedException");
            } catch (NotConnectedException ignored) {
                if (!isNetworkConnected(this)) {
                    showNetworkAlert(context);
                } else {
                    /* offline - ignored */
                    Toast.makeText(this, R.string.no_connection, Toast.LENGTH_SHORT).show();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Utils.reconnect(context, tinode, new Utils.ReconnectListener() {
                                @Override
                                public void after() {
                                    subscribe(tinode, me);
                                }
                            });
                        }
                    }).start();
                }
            } catch (Exception err) {
                Log.i(TAG, "Subscription failed " + err.getMessage());
                Toast.makeText(this,
                        "连接通讯服务器失败", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        Cache.getTinode().setListener(null);
        Log.e(TAG, "MainActivity onPause");
    }

    @Override
    public void onStop() {
        super.onStop();

        MeTopic me = Cache.getTinode().getMeTopic();
        if (me != null) {
            me.setListener(null);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(this, BackgroundService.class));
        } else {
            startService(new Intent(this, BackgroundService.class));
        }
        sendBroadcast(new Intent(AppConst.ACTION_NO_TINODE_LISTENER));
        Log.e(TAG, "MainActivity onStop");
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        AEvent.removeListener(AEvent.AEVENT_LOGOUT);
        AEvent.removeListener(AEvent.AEVENT_LOGOUT_2);
        fragments.clear();
        unRegisterBR();
        Log.e(TAG, "MainActivity onDestroy");
    }

    private long firstTime = 0;

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            long secondTime = System.currentTimeMillis();
            if (secondTime - firstTime > 2000) {
                Toast.makeText(this, "再点击一次退出", Toast.LENGTH_SHORT).show();
                firstTime = System.currentTimeMillis();
                return true;
            } else {
                this.finish();
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    private void registerBR() {
        BroadcastManager.getInstance(this).register(AppConst.FETCH_COMPLETE, new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                hideWaitingDialog();
            }
        });
    }

    private void unRegisterBR() {
        BroadcastManager.getInstance(this).unregister(AppConst.FETCH_COMPLETE);
    }

    private class MeListener extends Topic.Listener {

        @Override
        public void onData(MsgServerData data) {
            super.onData(data);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    datasetChanged(data.topic, data.ts, data.from, data.content.txt);
                }
            });
        }

        @Override
        public void onInfo(MsgServerInfo info) {
            LogUtil.d("Contacts got onInfo update '" + info.what + "'");
        }

        @Override
        public void onPres(MsgServerPres pres) {
            Log.d(TAG, "onPres, what=" + pres.what + ", topic=" + pres.topic+",act="+pres.act+",ct="+pres.ct);
//            TopicDb.updateLastUsed(BaseDb.getInstance().getWritableDatabase(), pres.topic);
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    if (pres.what.equals("msg")) {
//                        datasetChanged(pres.src, new Date(),"123");
//                    } else if (pres.what.equals("off") || pres.what.equals("on")) {
////                        datasetChanged();
//                    } else if (pres.what.equals("acs")) {
//                    }
//                }
//            });

            if (MsgServerPres.parseWhat(pres.what) == MsgServerPres.What.MSG) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        datasetChanged(pres.src, pres.ts, pres.act, pres.ct);
                    }
                });
            }
        }

        @Override
        public void onMetaSub(final Subscription sub) {
//            if (sub.pub != null) {
//                ((VCard) sub.pub).constructBitmap();
//            }
        }

        @Override
        public void onMetaDesc(final Description desc) {
            if (desc.pub != null) {
                ((VCard) desc.pub).constructBitmap();
            }

            //sdk升级后，p2p不再携带public信息。手动同步。
            SQLiteDatabase db = BaseDb.getInstance().getWritableDatabase();
            String uid = Cache.getTinode().getMyId();
            Log.d(TAG, "onMetaDesc: uid=" + uid);
            User me = UserDb.readOne(db, uid);
            if (me != null && me.pub == null) {
                Log.e(TAG, "onMetaDesc: User me.pub is null");
                me.setPub(Cache.getTinode().getMeTopic().getPub());
                UserDb.update(db, me);
            } else if (me != null && me.pub != null) {
                UserDb.update(db, me);
            } else {
                Log.e(TAG, "onMetaDesc: User me is null");
                UserDb.insert(db, uid, new Date(), Cache.getTinode().getMeTopic().getPub());
            }
        }


        @Override
        public void onSubsUpdated() {
            Log.d(TAG, "onSubsUpdated: datasetChanged");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
              //      datasetChanged(null,null,null,null);
                }
            });

        }
        @Override
        public void onContUpdate(final Subscription sub) {
            // Method makes no sense in context of MeTopic.
          // throw new UnsupportedOperationException();
        }
    }

    public  void datasetChanged(String topic, Date time, String fromId,String msg) {
        ((RecentMessageFragment) fragments.get(0)).datasetChanged(topic, time, fromId  ,msg);
    }


   /* public void datasetChanged() {
        try {
            ((RecentMessageFragment) fragments.get(1)).datasetChanged();
        } catch (Exception e){}

    }
    public int getUnReadCount() {
        try {
            return  ((RecentMessageFragment) fragments.get(1)).getUnReadCount();
        } catch (Exception e){
            return 0;
        }

    }*/

    public void setUnRead(){
    /*    int i = getUnReadCount();
        if (i == 0){
            redView.setVisibility(View.INVISIBLE);
        }else {
            redView.setVisibility(View.VISIBLE);
            redView.setText(""+i);
        }
        AEvent.RED = i;*/
        AEvent.notifyListener(AEvent.AEVENT_RED,true,"");
    }
    private Topic currentAttachedTopic = null;
    /**
     * 关闭监听的主题，该主题主要用于刷新最新消息
     */
    public void detachedTopic() {
        if (currentAttachedTopic != null && currentAttachedTopic.isAttached()) {
            currentAttachedTopic.leave();
        }
    }


}

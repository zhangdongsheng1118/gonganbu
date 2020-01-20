package com.yixin.tinode.ui.activity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.yixin.tinode.R;
import com.yixin.tinode.api.param.TiSetting;
import com.yixin.tinode.app.AppConst;
import com.yixin.tinode.db.tinode.BaseDb;
import com.yixin.tinode.db.tinode.UserDb;
import com.yixin.tinode.manager.BroadcastManager;
import com.yixin.tinode.tinode.Cache;
import com.yixin.tinode.tinode.util.UiUtils;
import com.yixin.tinode.ui.adapter.CommonFragmentPagerAdapter;
import com.yixin.tinode.ui.base.BaseActivity;
import com.yixin.tinode.ui.base.BaseFragment;
import com.yixin.tinode.ui.fragment.FragmentFactory;
import com.yixin.tinode.ui.fragment.RecentMessageFragment;
import com.yixin.tinode.ui.presenter.MainAtPresenter;
import com.yixin.tinode.ui.service.BackgroundService;
import com.yixin.tinode.ui.view.IMainAtView;
import com.yixin.tinode.util.PopupWindowUtils;
import com.yixin.tinode.util.UIUtils;
import com.yixin.tinode.util.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import co.tinode.tinodesdk.MeTopic;
import co.tinode.tinodesdk.NotConnectedException;
import co.tinode.tinodesdk.NotSynchronizedException;
import co.tinode.tinodesdk.Tinode;
import co.tinode.tinodesdk.Topic;
import co.tinode.tinodesdk.User;
import co.tinode.tinodesdk.model.Description;
import co.tinode.tinodesdk.model.MsgServerData;
import co.tinode.tinodesdk.model.MsgServerInfo;
import co.tinode.tinodesdk.model.MsgServerPres;
import co.tinode.tinodesdk.model.Subscription;
import co.tinode.tinodesdk.model.VCard;
import ezy.boost.update.UpdateManager;

import static com.yixin.tinode.util.Utils.isNetworkConnected;
import static com.yixin.tinode.util.Utils.showNetworkAlert;

public class MainActivity extends BaseActivity<IMainAtView, MainAtPresenter> implements ViewPager.OnPageChangeListener, IMainAtView {
    private static List<BaseFragment> mFragmentList = new ArrayList<>();

    @BindView(R.id.ibAddMenu)
    ImageButton mIbAddMenu;
    @BindView(R.id.iv_search)
    ImageView mIvSearch;
    @BindView(R.id.vpContent)
    ViewPager mVpContent;
    //底部
    @BindView(R.id.tvMessageNormal)
    TextView mTvMessageNormal;
    @BindView(R.id.tvMessagePress)
    TextView mTvMessagePress;
    @BindView(R.id.tvMessageTextNormal)
    TextView mTvMessageTextNormal;
    @BindView(R.id.tvMessageTextPress)
    TextView mTvMessageTextPress;
    @BindView(R.id.tvMessageCount)
    TextView mTvMessageCount;

    @BindView(R.id.tvContactsNormal)
    TextView mTvContactsNormal;
    @BindView(R.id.tvContactsPress)
    TextView mTvContactsPress;
    @BindView(R.id.tvContactsTextNormal)
    TextView mTvContactsTextNormal;
    @BindView(R.id.tvContactsTextPress)
    TextView mTvContactsTextPress;
    @BindView(R.id.tvContactCount)
    TextView mTvContactCount;
    @BindView(R.id.tvContactRedDot)
    public TextView mTvContactRedDot;

    @BindView(R.id.tvDiscoveryNormal)
    TextView mTvDiscoveryNormal;
    @BindView(R.id.tvDiscoveryPress)
    TextView mTvDiscoveryPress;
    @BindView(R.id.tvDiscoveryTextNormal)
    TextView mTvDiscoveryTextNormal;
    @BindView(R.id.tvDiscoveryTextPress)
    TextView mTvDiscoveryTextPress;
    @BindView(R.id.tvDiscoveryCount)
    TextView mTvDiscoveryCount;
    @BindView(R.id.tvMeNormal)
    TextView mTvMeNormal;
    @BindView(R.id.tvMePress)
    TextView mTvMePress;
    @BindView(R.id.tvMeTextNormal)
    TextView mTvMeTextNormal;
    @BindView(R.id.tvMeTextPress)
    TextView mTvMeTextPress;
    @BindView(R.id.tvMeCount)
    TextView mTvMeCount;

    private static final String TAG = MainActivity.class.getSimpleName();
    private MeListener mMeTopicListener = null;
    private Topic currentAttachedTopic = null;

    @Override
    public void init() {
        registerBR();
        UpdateManager.checkVersion(this);

        mMeTopicListener = new MeListener();
        initSetting();

        if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("提示")
                    .setMessage("检测到您没有打开通知权限，请设置")
                    .setPositiveButton("设置", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent localIntent = new Intent();
                            localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            if (Build.VERSION.SDK_INT >= 9) {
                                localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                                localIntent.setData(Uri.fromParts("package", MainActivity.this.getPackageName(), null));
                            } else if (Build.VERSION.SDK_INT <= 8) {
                                localIntent.setAction(Intent.ACTION_VIEW);

                                localIntent.setClassName("com.android.settings",
                                        "com.android.settings.InstalledAppDetails");

                                localIntent.putExtra("com.android.settings.ApplicationPkgName",
                                        MainActivity.this.getPackageName());
                            }
                            startActivity(localIntent);
                        }
                    }).show();
        }
    }

    private void initSetting() {
        TiSetting setting = SettingNewMsgNotifyActivity.getSettingNotify(context);
        if (setting == null) {
            setting = new TiSetting();
            setting.setNotification(AppConst.TRUE);
            setting.setVibrate(AppConst.TRUE);
            setting.setSound(AppConst.TRUE);
            setting.setNotification(AppConst.TRUE);
            setting.setAvServerAddr(AppConst.AV_ADDRESS);
            setting.setAvServerPort(AppConst.AV_PORT);
            setting.setAvQuality(AppConst.AV_QUALITY);
            SettingNewMsgNotifyActivity.save(context, setting);
        }
        //we can append av setting to settings
        TiSetting tmp_setting = SettingAVActivity.getSettingNotify(context);
        setting.setAvServerAddr(tmp_setting.getAvServerAddr());
        setting.setAvServerPort(tmp_setting.getAvServerPort());
        setting.setAvQuality(tmp_setting.getAvQuality());
    }

    @Override
    public void initView() {
        setToolbarTitle(UIUtils.getString(R.string.app_name));
        mIbAddMenu.setVisibility(View.VISIBLE);
        mIvSearch.setVisibility(View.VISIBLE);
        //等待全局数据获取完毕
//		showWaitingDialog(UIUtils.getString(R.string.please_wait));
        //默认选中第一个
        setTransparency();
        mTvMessagePress.getBackground().setAlpha(255);
        mTvMessageTextPress.setTextColor(Color.argb(255, 69, 192, 26));

        //设置ViewPager的最大缓存页面
        mVpContent.setOffscreenPageLimit(3);
        if (mFragmentList == null) {
            //页面被回收后，恢复现场时的bug
            mFragmentList = new ArrayList<>();
        }

        mFragmentList.clear();
        mFragmentList.add(FragmentFactory.getInstance().getRecentMessageFragment());
        mFragmentList.add(FragmentFactory.getInstance().getContactsFragment());
        mFragmentList.add(FragmentFactory.getInstance().getDiscoveryFragment());
        mFragmentList.add(FragmentFactory.getInstance().getMeFragment());
        mVpContent.setAdapter(new CommonFragmentPagerAdapter(getSupportFragmentManager(), mFragmentList));
    }

    @Override
    public void initListener() {
        mIvSearch.setOnClickListener(v -> {
            startActivity(new Intent(context, SearchGlobalActivity.class));
        });
        mIbAddMenu.setOnClickListener(v -> {
            //显示或隐藏popupwindow
            View menuView = View.inflate(MainActivity.this, R.layout.menu_main, null);
            PopupWindow popupWindow = PopupWindowUtils.getPopupWindowAtLocation(menuView, getWindow().getDecorView(), Gravity.TOP | Gravity.RIGHT, UIUtils.dip2Px(5), mAppBar.getHeight() + 30);
            menuView.findViewById(R.id.tvCreateGroup).setOnClickListener(v1 -> {
                jumpToActivity(CreateGroupActivity.class);
                popupWindow.dismiss();
            });
            menuView.findViewById(R.id.tvHelpFeedback).setOnClickListener(v1 -> {
                jumpToWebViewActivity(AppConst.WeChatUrl.HELP_FEED_BACK);
                popupWindow.dismiss();
            });
            menuView.findViewById(R.id.tvAddFriend).setOnClickListener(v1 -> {
                jumpToActivity(SearchUserActivity.class);
                popupWindow.dismiss();
            });
            menuView.findViewById(R.id.tvScan).setOnClickListener(v1 -> {
                jumpToActivity(ScanActivity.class);
                popupWindow.dismiss();
            });
        });

        mVpContent.setOnPageChangeListener(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        mVpContent.setCurrentItem(0, false);
    }

    @OnClick({R.id.llMessage, R.id.llContacts, R.id.llDiscovery, R.id.llMe})
    public void bottomBtnClick(View view) {
        setTransparency();
        switch (view.getId()) {
            case R.id.llMessage:
                if (mFragmentList.get(0) != null) {
                    mVpContent.setCurrentItem(0, false);
                } else {
                    mFragmentList.add(0, FragmentFactory.getInstance().getRecentMessageFragment());
                }
                mTvMessagePress.getBackground().setAlpha(255);
                mTvMessageTextPress.setTextColor(Color.argb(255, 69, 192, 26));
                break;
            case R.id.llContacts:
                if (mFragmentList.get(1) != null) {
                    mVpContent.setCurrentItem(1, false);
                } else {
                    mFragmentList.add(1, FragmentFactory.getInstance().getContactsFragment());
                }
                mTvContactsPress.getBackground().setAlpha(255);
                mTvContactsTextPress.setTextColor(Color.argb(255, 69, 192, 26));
                break;
            case R.id.llDiscovery:
                if (mFragmentList.get(2) != null) {
                    mVpContent.setCurrentItem(2, false);
                } else {
                    mFragmentList.add(2, FragmentFactory.getInstance().getDiscoveryFragment());
                }
                mTvDiscoveryPress.getBackground().setAlpha(255);
                mTvDiscoveryTextPress.setTextColor(Color.argb(255, 69, 192, 26));
                break;
            case R.id.llMe:
                if (mFragmentList.get(3) != null) {
                    mVpContent.setCurrentItem(3, false);
                } else {
                    mFragmentList.add(3, FragmentFactory.getInstance().getMeFragment());
                }
                mTvMePress.getBackground().setAlpha(255);
                mTvMeTextPress.setTextColor(Color.argb(255, 69, 192, 26));
                break;
        }
    }

    /**
     * 把press图片、文字全部隐藏(设置透明度)
     */
    private void setTransparency() {
        mTvMessageNormal.getBackground().setAlpha(255);
        mTvContactsNormal.getBackground().setAlpha(255);
        mTvDiscoveryNormal.getBackground().setAlpha(255);
        mTvMeNormal.getBackground().setAlpha(255);
        mTvMessagePress.getBackground().setAlpha(1);
        mTvContactsPress.getBackground().setAlpha(1);
        mTvDiscoveryPress.getBackground().setAlpha(1);
        mTvMePress.getBackground().setAlpha(1);
        mTvMessageTextNormal.setTextColor(Color.argb(255, 153, 153, 153));
        mTvContactsTextNormal.setTextColor(Color.argb(255, 153, 153, 153));
        mTvDiscoveryTextNormal.setTextColor(Color.argb(255, 153, 153, 153));
        mTvMeTextNormal.setTextColor(Color.argb(255, 153, 153, 153));
        mTvMessageTextPress.setTextColor(Color.argb(0, 69, 192, 26));
        mTvContactsTextPress.setTextColor(Color.argb(0, 69, 192, 26));
        mTvDiscoveryTextPress.setTextColor(Color.argb(0, 69, 192, 26));
        mTvMeTextPress.setTextColor(Color.argb(0, 69, 192, 26));
    }

    @Override
    protected MainAtPresenter createPresenter() {
        return new MainAtPresenter(this);
    }

    @Override
    protected int provideContentViewId() {
        return R.layout.activity_main;
    }

    @Override
    protected boolean isToolbarCanBack() {
        return false;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        //根据ViewPager滑动位置更改透明度
        int diaphaneity_one = (int) (255 * positionOffset);
        int diaphaneity_two = (int) (255 * (1 - positionOffset));
        switch (position) {
            case 0:
                mTvMessageNormal.getBackground().setAlpha(diaphaneity_one);
                mTvMessagePress.getBackground().setAlpha(diaphaneity_two);
                mTvContactsNormal.getBackground().setAlpha(diaphaneity_two);
                mTvContactsPress.getBackground().setAlpha(diaphaneity_one);
                mTvMessageTextNormal.setTextColor(Color.argb(diaphaneity_one, 153, 153, 153));
                mTvMessageTextPress.setTextColor(Color.argb(diaphaneity_two, 69, 192, 26));
                mTvContactsTextNormal.setTextColor(Color.argb(diaphaneity_two, 153, 153, 153));
                mTvContactsTextPress.setTextColor(Color.argb(diaphaneity_one, 69, 192, 26));
                break;
            case 1:
                mTvContactsNormal.getBackground().setAlpha(diaphaneity_one);
                mTvContactsPress.getBackground().setAlpha(diaphaneity_two);
                mTvDiscoveryNormal.getBackground().setAlpha(diaphaneity_two);
                mTvDiscoveryPress.getBackground().setAlpha(diaphaneity_one);
                mTvContactsTextNormal.setTextColor(Color.argb(diaphaneity_one, 153, 153, 153));
                mTvContactsTextPress.setTextColor(Color.argb(diaphaneity_two, 69, 192, 26));
                mTvDiscoveryTextNormal.setTextColor(Color.argb(diaphaneity_two, 153, 153, 153));
                mTvDiscoveryTextPress.setTextColor(Color.argb(diaphaneity_one, 69, 192, 26));
                break;
            case 2:
                mTvDiscoveryNormal.getBackground().setAlpha(diaphaneity_one);
                mTvDiscoveryPress.getBackground().setAlpha(diaphaneity_two);
                mTvMeNormal.getBackground().setAlpha(diaphaneity_two);
                mTvMePress.getBackground().setAlpha(diaphaneity_one);
                mTvDiscoveryTextNormal.setTextColor(Color.argb(diaphaneity_one, 153, 153, 153));
                mTvDiscoveryTextPress.setTextColor(Color.argb(diaphaneity_two, 69, 192, 26));
                mTvMeTextNormal.setTextColor(Color.argb(diaphaneity_two, 153, 153, 153));
                mTvMeTextPress.setTextColor(Color.argb(diaphaneity_one, 69, 192, 26));
                break;
        }
    }

    @Override
    public void onPageSelected(int position) {
        switch(position){
            case 0:
                this.setToolbarTitle("一信");
                break;
            case 1:
                this.setToolbarTitle("通讯录");
                break;
            case 2:
                this.setToolbarTitle("应用");
                break;
            case 3:
                this.setToolbarTitle("我");
                break;
        }

        if (position == 1) {
            //如果是“通讯录”页被选中，则显示快速导航条
            FragmentFactory.getInstance().getContactsFragment().showQuickIndexBar(true);
        } else {
            FragmentFactory.getInstance().getContactsFragment().showQuickIndexBar(false);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if (state != ViewPager.SCROLL_STATE_IDLE) {
            //滚动过程中隐藏快速导航条
            FragmentFactory.getInstance().getContactsFragment().showQuickIndexBar(false);
        } else {
            FragmentFactory.getInstance().getContactsFragment().showQuickIndexBar(true);
        }
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

    @Override
    public TextView getTvMessageCount() {
        return mTvMessageCount;
    }

    /**
     * onResume restores subscription to 'me' topic and sets listener.
     */
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
//                      .withGetData()
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
        mFragmentList.clear();
        unRegisterBR();
        Log.e(TAG, "MainActivity onDestroy");
    }

    public static void datasetChanged(String topic, Date time, String fromId,String msg) {
        ((RecentMessageFragment) mFragmentList.get(0)).datasetChanged(topic, time, fromId,msg);
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
            Log.d(TAG, "Contacts got onInfo update '" + info.what + "'");
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
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    datasetChanged(null,null,null,null);
//                }
//            });
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
                    datasetChanged(null,null,null,null);
                }
            });

        }

        @Override
        public void onContUpdate(final Subscription sub) {
            // Method makes no sense in context of MeTopic.
            //throw new UnsupportedOperationException();
        }

    }

    private long firstTime = 0;

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
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


    /**
     * 关闭监听的主题，该主题主要用于刷新最新消息
     */
    public void detachedTopic() {
        if (currentAttachedTopic != null && currentAttachedTopic.isAttached()) {
            currentAttachedTopic.leave();
        }
    }
}

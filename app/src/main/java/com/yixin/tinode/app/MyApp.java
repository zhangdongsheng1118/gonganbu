package com.yixin.tinode.app;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import com.blankj.utilcode.util.Utils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.lqr.emoji.LQREmotionKit;
import com.lqr.imagepicker.ImagePicker;
import com.lqr.imagepicker.loader.ImageLoader;
import com.lqr.imagepicker.view.CropImageView;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheEntity;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.cookie.CookieJarImpl;
import com.lzy.okgo.cookie.store.DBCookieStore;
import com.lzy.okgo.https.HttpsUtils;
import com.lzy.okgo.interceptor.HttpLoggingInterceptor;
import com.lzy.okserver.OkDownload;
import com.piasy.avconf.AvConf;
import com.tencent.bugly.crashreport.CrashReport;
import com.yixin.tinode.app.base.BaseApp;
import com.yixin.tinode.ui.service.BackgroundService;
import com.yixin.tinode.util.PathUtil;
import com.zuozhan.app.BaseIP;
import com.zuozhan.app.CrashHandler;
import com.zuozhan.app.FileUtil;

import org.litepal.LitePal;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import okhttp3.OkHttpClient;

/**
 * @创建者 CSDN_LQR
 * @描述 BaseApp的拓展，用于设置其他第三方的初始化
 */
public class MyApp extends BaseApp {

    private static MyApp context;

    public static MyApp getInstance() {
        return context;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        LitePal.initialize(this);
        //初始化仿一信控件ImagePicker
        initImagePicker();
        Utils.init(this);
        //初始化表情控件
        LQREmotionKit.init(this, (context, path, imageView) -> Glide.with(context).load(path).centerCrop().diskCacheStrategy(DiskCacheStrategy.SOURCE).into(imageView));

        initCrashReport();
        FileUtil.init();
        CrashHandler.getInstance().init(this);
        Utils.init(this);
        initOkGo();

        AvConf.initialize(this, true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(this, BackgroundService.class));
        } else {
            startService(new Intent(this, BackgroundService.class));
        }
    }

    @Override
    public void onTerminate() {
        stopService(new Intent(this, BackgroundService.class));
        super.onTerminate();
    }

    private void initCrashReport() {
        Context context = getApplicationContext();
// 获取当前包名
        String packageName = context.getPackageName();
// 获取当前进程名
        String processName = getProcessName(android.os.Process.myPid());
// 设置是否为上报进程
        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(context);
        strategy.setUploadProcess(processName == null || processName.equals(packageName));
//// 初始化Bugly
        CrashReport.initCrashReport(context, "7a689ae792", true, strategy);

        PathUtil.getInstance().initDirs(null, this);
    }

    /**
     * 获取进程号对应的进程名
     *
     * @param pid 进程号
     * @return 进程名
     */
    private static String getProcessName(int pid) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("/proc/" + pid + "/cmdline"));
            String processName = reader.readLine();
            if (!TextUtils.isEmpty(processName)) {
                processName = processName.trim();
            }
            return processName;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        return null;
    }
/*
    @Override
    public boolean onReceived(Message message, int i) {
        MessageContent messageContent = message.getContent();
        if (messageContent instanceof ContactNotificationMessage) {
            ContactNotificationMessage contactNotificationMessage = (ContactNotificationMessage) messageContent;
            if (contactNotificationMessage.getOperation().equals(ContactNotificationMessage.CONTACT_OPERATION_REQUEST)) {
                //对方发来好友邀请
                BroadcastManager.getInstance(UIUtils.getContext()).sendBroadcast(AppConst.UPDATE_RED_DOT);
            } else {
                //对方同意我的好友请求
                ContactNotificationMessageData c = null;
                try {
                    c = JsonMananger.jsonToBean(contactNotificationMessage.getExtra(), ContactNotificationMessageData.class);
                } catch (HttpException e) {
                    e.printStackTrace();
                    return false;
                } catch (JSONException e) {
                    e.printStackTrace();
                    return false;
                }
                if (c != null) {
                    if (DBManager.getInstance().isMyFriend(contactNotificationMessage.getSourceUserId()))
                        return false;
                    DBManager.getInstance().saveOrUpdateFriend(
                            new Friend(contactNotificationMessage.getSourceUserId(),
                                    c.getSourceUserNickname(),
                                    null, c.getSourceUserNickname(), null, null,
                                    null, null,
                                    PinyinUtils.getPinyin(c.getSourceUserNickname()),
                                    PinyinUtils.getPinyin(c.getSourceUserNickname())
                            )
                    );
                    BroadcastManager.getInstance(UIUtils.getContext()).sendBroadcast(AppConst.UPDATE_FRIEND);
                    BroadcastManager.getInstance(UIUtils.getContext()).sendBroadcast(AppConst.UPDATE_RED_DOT);
                }
            }
        } else if (messageContent instanceof DeleteContactMessage) {
            DeleteContactMessage deleteContactMessage = (DeleteContactMessage) messageContent;
            String contact_id = deleteContactMessage.getContact_id();
            RongIMClient.getInstance().getConversation(Conversation.ConversationType.PRIVATE, contact_id, new RongIMClient.ResultCallback<Conversation>() {
                @Override
                public void onSuccess(Conversation conversation) {
                    RongIMClient.getInstance().clearMessages(Conversation.ConversationType.PRIVATE, contact_id, new RongIMClient.ResultCallback<Boolean>() {
                        @Override
                        public void onSuccess(Boolean aBoolean) {
                            RongIMClient.getInstance().removeConversation(Conversation.ConversationType.PRIVATE, contact_id, null);
                            BroadcastManager.getInstance(getContext()).sendBroadcast(AppConst.UPDATE_CONVERSATIONS);
                        }

                        @Override
                        public void onError(RongIMClient.ErrorCode errorCode) {

                        }
                    });
                }

                @Override
                public void onError(RongIMClient.ErrorCode errorCode) {

                }
            });
            DBManager.getInstance().deleteFriendById(contact_id);
            BroadcastManager.getInstance(getContext()).sendBroadcast(AppConst.UPDATE_FRIEND);
        } else if (messageContent instanceof GroupNotificationMessage) {
            GroupNotificationMessage groupNotificationMessage = (GroupNotificationMessage) messageContent;
            String groupId = message.getTargetId();
            GroupNotificationMessageData data = null;
            try {
                String curUserId = UserCache.getId();
                try {
                    data = jsonToBean(groupNotificationMessage.getData());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (groupNotificationMessage.getOperation().equals(GroupNotificationMessage.GROUP_OPERATION_CREATE)) {
                    DBManager.getInstance().getGroups(groupId);
                    DBManager.getInstance().getGroupMember(groupId);
                } else if (groupNotificationMessage.getOperation().equals(GroupNotificationMessage.GROUP_OPERATION_DISMISS)) {
                    handleGroupDismiss(groupId);
                } else if (groupNotificationMessage.getOperation().equals(GroupNotificationMessage.GROUP_OPERATION_KICKED)) {
                    if (data != null) {
                        List<String> memberIdList = data.getTargetUserIds();
                        if (memberIdList != null) {
                            for (String userId : memberIdList) {
                                if (curUserId.equals(userId)) {
                                    RongIMClient.getInstance().removeConversation(Conversation.ConversationType.GROUP, message.getTargetId(), new RongIMClient.ResultCallback<Boolean>() {
                                        @Override
                                        public void onSuccess(Boolean aBoolean) {
                                            LogUtils.sf("Conversation remove successfully.");
                                        }

                                        @Override
                                        public void onError(RongIMClient.ErrorCode e) {

                                        }
                                    });
                                }
                            }
                        }
                        List<String> kickedUserIDs = data.getTargetUserIds();
                        DBManager.getInstance().deleteGroupMembers(groupId, kickedUserIDs);
                        //因为操作存在异步，故不在这里发送广播
//                        BroadcastManager.getInstance(getContext()).sendBroadcast(AppConst.UPDATE_GROUP_MEMBER, groupId);
//                        BroadcastManager.getInstance(getContext()).sendBroadcast(AppConst.UPDATE_CONVERSATIONS);
                    }
                } else if (groupNotificationMessage.getOperation().equals(GroupNotificationMessage.GROUP_OPERATION_ADD)) {
                    DBManager.getInstance().getGroups(groupId);
                    DBManager.getInstance().getGroupMember(groupId);
                    //因为操作存在异步，故不在这里发送广播
//                    BroadcastManager.getInstance(getContext()).sendBroadcast(AppConst.UPDATE_GROUP_MEMBER, groupId);
                } else if (groupNotificationMessage.getOperation().equals(GroupNotificationMessage.GROUP_OPERATION_QUIT)) {
                    if (data != null) {
                        List<String> quitUserIDs = data.getTargetUserIds();
                        DBManager.getInstance().deleteGroupMembers(groupId, quitUserIDs);
                        //因为操作存在异步，故不在这里发送广播
//                        BroadcastManager.getInstance(getContext()).sendBroadcast(AppConst.UPDATE_GROUP_MEMBER, groupId);
//                        BroadcastManager.getInstance(getContext()).sendBroadcast(AppConst.UPDATE_CONVERSATIONS);
                    }
                } else if (groupNotificationMessage.getOperation().equals(GroupNotificationMessage.GROUP_OPERATION_RENAME)) {
                    if (data != null) {
                        String targetGroupName = data.getTargetGroupName();
                        DBManager.getInstance().updateGroupsName(groupId, targetGroupName);
                        //更新群名
                        BroadcastManager.getInstance(getContext()).sendBroadcast(AppConst.UPDATE_CURRENT_SESSION_NAME);
                        BroadcastManager.getInstance(getContext()).sendBroadcast(AppConst.UPDATE_CONVERSATIONS);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            BroadcastManager.getInstance(getContext()).sendBroadcast(AppConst.UPDATE_CONVERSATIONS);
        } else {
            BroadcastManager.getInstance(getContext()).sendBroadcast(AppConst.UPDATE_CONVERSATIONS);
            BroadcastManager.getInstance(getContext()).sendBroadcast(AppConst.UPDATE_CURRENT_SESSION, message);
        }
        return false;
    }

    private void handleGroupDismiss(final String groupId) {
        RongIMClient.getInstance().getConversation(Conversation.ConversationType.GROUP, groupId, new RongIMClient.ResultCallback<Conversation>() {
            @Override
            public void onSuccess(Conversation conversation) {
                RongIMClient.getInstance().clearMessages(Conversation.ConversationType.GROUP, groupId, new RongIMClient.ResultCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean aBoolean) {
                        RongIMClient.getInstance().removeConversation(Conversation.ConversationType.GROUP, groupId, new RongIMClient.ResultCallback<Boolean>() {
                            @Override
                            public void onSuccess(Boolean aBoolean) {
                                DBManager.getInstance().deleteGroup(new Groups(groupId));
                                DBManager.getInstance().deleteGroupMembersByGroupId(groupId);
                                BroadcastManager.getInstance(getContext()).sendBroadcast(AppConst.UPDATE_CONVERSATIONS);
                                BroadcastManager.getInstance(getContext()).sendBroadcast(AppConst.GROUP_LIST_UPDATE);
                            }

                            @Override
                            public void onError(RongIMClient.ErrorCode errorCode) {

                            }
                        });
                    }

                    @Override
                    public void onError(RongIMClient.ErrorCode errorCode) {

                    }
                });
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {

            }
        });
    }

    private GroupNotificationMessageData jsonToBean(String data) {
        GroupNotificationMessageData dataEntity = new GroupNotificationMessageData();
        try {
            JSONObject jsonObject = new JSONObject(data);
            if (jsonObject.has("operatorNickname")) {
                dataEntity.setOperatorNickname(jsonObject.getString("operatorNickname"));
            }
            if (jsonObject.has("targetGroupName")) {
                dataEntity.setTargetGroupName(jsonObject.getString("targetGroupName"));
            }
            if (jsonObject.has("timestamp")) {
                dataEntity.setTimestamp(jsonObject.getLong("timestamp"));
            }
            if (jsonObject.has("targetUserIds")) {
                JSONArray jsonArray = jsonObject.getJSONArray("targetUserIds");
                for (int i = 0; i < jsonArray.length(); i++) {
                    dataEntity.getTargetUserIds().add(jsonArray.getString(i));
                }
            }
            if (jsonObject.has("targetUserDisplayNames")) {
                JSONArray jsonArray = jsonObject.getJSONArray("targetUserDisplayNames");
                for (int i = 0; i < jsonArray.length(); i++) {
                    dataEntity.getTargetUserDisplayNames().add(jsonArray.getString(i));
                }
            }
            if (jsonObject.has("oldCreatorId")) {
                dataEntity.setOldCreatorId(jsonObject.getString("oldCreatorId"));
            }
            if (jsonObject.has("oldCreatorName")) {
                dataEntity.setOldCreatorName(jsonObject.getString("oldCreatorName"));
            }
            if (jsonObject.has("newCreatorId")) {
                dataEntity.setNewCreatorId(jsonObject.getString("newCreatorId"));
            }
            if (jsonObject.has("newCreatorName")) {
                dataEntity.setNewCreatorName(jsonObject.getString("newCreatorName"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return dataEntity;
    }*/

    /**
     * 初始化仿一信控件ImagePicker
     */
    private void initImagePicker() {
        ImagePicker imagePicker = ImagePicker.getInstance();
        imagePicker.setImageLoader(new ImageLoader() {
            @Override
            public void displayImage(Activity activity, String path, ImageView imageView, int width, int height) {
                Glide.with(getContext()).load(Uri.parse("file://" + path).toString()).centerCrop().into(imageView);
            }

            @Override
            public void clearMemoryCache() {

            }
        });   //设置图片加载器

        imagePicker.setShowCamera(true);  //显示拍照按钮
        imagePicker.setCrop(true);        //允许裁剪（单选才有效）
        imagePicker.setSaveRectangle(true); //是否按矩形区域保存
        imagePicker.setSelectLimit(9);    //选中数量限制
        imagePicker.setStyle(CropImageView.Style.RECTANGLE);  //裁剪框的形状
        imagePicker.setFocusWidth(800);   //裁剪框的宽度。单位像素（圆形自动取宽高最小值）
        imagePicker.setFocusHeight(800);  //裁剪框的高度。单位像素（圆形自动取宽高最小值）
        imagePicker.setOutPutX(1000);//保存文件的宽度。单位像素
        imagePicker.setOutPutY(1000);//保存文件的高度。单位像素

        com.zuozhan.app.photo.imagepicker.ImagePicker imagePicker2 = com.zuozhan.app.photo.imagepicker.ImagePicker.getInstance();
        imagePicker2.setImageLoader(new com.zuozhan.app.photo.imagepicker.loader.ImageLoader() {
            @Override
            public void displayImage(Activity activity, String path, ImageView imageView, int width, int height) {
                Glide.with(getContext()).load(Uri.parse("file://" + path).toString()).centerCrop().into(imageView);
            }

            @Override
            public void clearMemoryCache() {

            }
        });   //设置图片加载器

        imagePicker2.setShowCamera(true);  //显示拍照按钮
        imagePicker2.setCrop(true);        //允许裁剪（单选才有效）
        imagePicker2.setSaveRectangle(true); //是否按矩形区域保存
        imagePicker2.setSelectLimit(9);    //选中数量限制
        imagePicker2.setStyle(com.zuozhan.app.photo.imagepicker.view.CropImageView.Style.RECTANGLE);  //裁剪框的形状
        imagePicker2.setFocusWidth(800);   //裁剪框的宽度。单位像素（圆形自动取宽高最小值）
        imagePicker2.setFocusHeight(800);  //裁剪框的高度。单位像素（圆形自动取宽高最小值）
        imagePicker2.setOutPutX(1000);//保存文件的宽度。单位像素
        imagePicker2.setOutPutY(1000);//保存文件的高度。单位像素
    }

    public static String getCurProcessName(Context context) {
        int pid = android.os.Process.myPid();

        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : activityManager
                .getRunningAppProcesses()) {
            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return null;
    }

    //上传及下载
    private void initOkGo() {
        //---------这里给出的是示例代码,告诉你可以这么传,实际使用的时候,根据需要传,不需要就不传-------------//
//        HttpHeaders headers = new HttpHeaders();
//        headers.put("commonHeaderKey1", "commonHeaderValue1");    //header不支持中文，不允许有特殊字符
//        headers.put("commonHeaderKey2", "commonHeaderValue2");
//        HttpParams params = new HttpParams();
//        params.put("commonParamsKey1", "commonParamsValue1");     //param支持中文,直接传,不要自己编码
//        params.put("commonParamsKey2", "这里支持中文参数");
        //----------------------------------------------------------------------------------------//

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        //log相关
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor("OkGo");
        loggingInterceptor.setPrintLevel(HttpLoggingInterceptor.Level.BODY);        //log打印级别，决定了log显示的详细程度
        loggingInterceptor.setColorLevel(Level.INFO);                               //log颜色级别，决定了log在控制台显示的颜色
        builder.addInterceptor(loggingInterceptor);                                 //添加OkGo默认debug日志
        //第三方的开源库，使用通知显示当前请求的log，不过在做文件下载的时候，这个库好像有问题，对文件判断不准确
        //builder.addInterceptor(new ChuckInterceptor(this));

        //超时时间设置，默认60秒
        builder.readTimeout(OkGo.DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS);      //全局的读取超时时间
        builder.writeTimeout(OkGo.DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS);     //全局的写入超时时间
        builder.connectTimeout(OkGo.DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS);   //全局的连接超时时间

        //自动管理cookie（或者叫session的保持），以下几种任选其一就行
        //builder.cookieJar(new CookieJarImpl(new SPCookieStore(this)));            //使用sp保持cookie，如果cookie不过期，则一直有效
        builder.cookieJar(new CookieJarImpl(new DBCookieStore(this)));              //使用数据库保持cookie，如果cookie不过期，则一直有效
        //builder.cookieJar(new CookieJarImpl(new MemoryCookieStore()));            //使用内存保持cookie，app退出后，cookie消失

        //https相关设置，以下几种方案根据需要自己设置
        //方法一：信任所有证书,不安全有风险
        HttpsUtils.SSLParams sslParams1 = HttpsUtils.getSslSocketFactory();
        //方法二：自定义信任规则，校验服务端证书
//        HttpsUtils.SSLParams sslParams2 = HttpsUtils.getSslSocketFactory(new SafeTrustManager());
        //方法三：使用预埋证书，校验服务端证书（自签名证书）
        //HttpsUtils.SSLParams sslParams3 = HttpsUtils.getSslSocketFactory(getAssets().open("srca.cer"));
        //方法四：使用bks证书和密码管理客户端证书（双向认证），使用预埋证书，校验服务端证书（自签名证书）
        //HttpsUtils.SSLParams sslParams4 = HttpsUtils.getSslSocketFactory(getAssets().open("xxx.bks"), "123456", getAssets().open("yyy.cer"));
        builder.sslSocketFactory(sslParams1.sSLSocketFactory, sslParams1.trustManager);
        //配置https的域名匹配规则，详细看demo的初始化介绍，不需要就不要加入，使用不当会导致https握手失败
//        builder.hostnameVerifier(new SafeHostnameVerifier());
//        第二步:
        builder.hostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                //强行返回true 即验证成功
                return true;
            }
        });

        // 其他统一的配置
        // 详细说明看GitHub文档：https://github.com/jeasonlzy/
        OkGo.getInstance().init(this)                           //必须调用初始化
                .setOkHttpClient(builder.build())               //建议设置OkHttpClient，不设置会使用默认的
                .setCacheMode(CacheMode.NO_CACHE)               //全局统一缓存模式，默认不使用缓存，可以不传
                .setCacheTime(CacheEntity.CACHE_NEVER_EXPIRE)   //全局统一缓存时间，默认永不过期，可以不传
                .setRetryCount(3);                               //全局统一超时重连次数，默认为三次，那么最差的情况会请求4次(一次原始请求，三次重连请求)，不需要可以设置为0
//                .addCommonHeaders(headers)                      //全局公共头
//                .addCommonParams(params);                       //全局公共参数

        OkDownload
                okDownload = OkDownload.getInstance();
        okDownload.setFolder(PathUtil.getInstance().getFilePath().getAbsolutePath());
        okDownload.getThreadPool().setCorePoolSize(3);
    }
}

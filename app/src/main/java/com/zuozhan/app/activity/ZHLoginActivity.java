package com.zuozhan.app.activity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.yixin.tinode.R;
import com.yixin.tinode.db.tinode.BaseDb;
import com.yixin.tinode.model.cache.UserCache;
import com.yixin.tinode.tinode.Cache;
import com.yixin.tinode.tinode.account.Utils;
import com.yixin.tinode.ui.activity.LoginActivity;
import com.yixin.tinode.ui.service.BackgroundService;
import com.yixin.tinode.util.LogUtils;
import com.zuozhan.app.AppEnvirment;
import com.zuozhan.app.BaseIP;
import com.zuozhan.app.DownloadUtil;
import com.zuozhan.app.RouterUtil;
import com.zuozhan.app.bean.MyUtil;
import com.zuozhan.app.bean.UserBean;
import com.zuozhan.app.bean.VersionBean;
import com.zuozhan.app.httpUtils.HttpUtil;
import com.zuozhan.app.httpUtils.MyService;
import com.zuozhan.app.inteface.LoginCallback;
import com.zuozhan.app.net.HttpService;
import com.zuozhan.app.util.LogUtil;
import com.zuozhan.app.util.ShareprefrensUtils;
import com.zuozhan.app.util.ToastUtils;

import co.tinode.tinodesdk.PromisedReply;
import co.tinode.tinodesdk.Tinode;
import co.tinode.tinodesdk.model.AuthScheme;
import co.tinode.tinodesdk.model.ServerMessage;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ZHLoginActivity extends AllBaseActivity implements View.OnClickListener {

    private EditText usernameEdit;
    private EditText passwordEdit;
    private TextView update_tv;
    private Button login_Btn, ip_set;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zh_activity_login);
        initView();
    }

    public void initView() {
        usernameEdit = (EditText) findViewById(R.id.usernameEdit);
        passwordEdit = (EditText) findViewById(R.id.passwordEdit);
        login_Btn = (Button) findViewById(R.id.login_Btn);
        ip_set = (Button) findViewById(R.id.ip_set);
        update_tv = findViewById(R.id.update_tv);

        update_tv.setOnClickListener(this);
        login_Btn.setOnClickListener(this);
        ip_set.setOnClickListener(this);
//        if (BaseIP.isDebug) {
        ip_set.setVisibility(View.VISIBLE);
//        } else {
//            ip_set.setVisibility(View.GONE);
//        }
       String name = ShareprefrensUtils.getSharePreferences("login_name",null);
       String pass = ShareprefrensUtils.getSharePreferences("login_pass",null);
       if (!TextUtils.isEmpty(name)){
           usernameEdit.setText(name);
       }
        if (!TextUtils.isEmpty(pass)){
            passwordEdit.setText(pass);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_Btn:
                String name = usernameEdit.getText().toString();
                String pass = passwordEdit.getText().toString();

                ShareprefrensUtils.setSharePreferences("login_name",name);
                ShareprefrensUtils.setSharePreferences("login_pass",pass);

                if (name.isEmpty() || pass.isEmpty()) {
                    submit();
                } else {
                    showLoading("登录中...");
                    HttpService.getApi(MyService.class)
                            .getServiceApi().getLogin(name, pass)
                            .enqueue(new Callback<UserBean>() {
                                @Override
                                public void onResponse(Call<UserBean> call, Response<UserBean> response) {
                                    try {
                                        UserBean UserBean = response.body();
                                        if (UserBean != null && UserBean.code == 1 && UserBean.data != null) {
                                            if (BaseIP.quchu_im) {
                                                ShareprefrensUtils.setSharePreferences(ShareprefrensUtils.TOKEN, UserBean.data.token);
                                                ShareprefrensUtils.setSharePreferencesBoolean(ShareprefrensUtils.ISLOGIN, true);
                                                AppEnvirment.token = UserBean.data.token;
                                                HttpUtil.getUserInfo(UserBean.data.token, new HttpUtil.Callback<com.zuozhan.app.bean.UserBean>() {
                                                    @Override
                                                    public void onResponse(com.zuozhan.app.bean.UserBean call) {
                                                        ToastUtils.showToast("登录完成");
                                                        dismissLoading();
                                                        goHome();
                                                        finish();
                                                    }
                                                });
                                                return;
                                            }
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                startForegroundService(new Intent(AppEnvirment.getApplication(), BackgroundService.class));
                                            } else {
                                                startService(new Intent(AppEnvirment.getApplication(), BackgroundService.class));
                                            }
                                            doIMLogin(name, "123456", new LoginCallback() {

                                                @Override
                                                public void onCallBack(boolean success, String result) {
                                                    if (success) {
                                                        showLoading("登录成功,数据同步中...");
                                                        runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                ShareprefrensUtils.setSharePreferences(ShareprefrensUtils.TOKEN, UserBean.data.token);
                                                                ShareprefrensUtils.setSharePreferencesBoolean(ShareprefrensUtils.ISLOGIN, true);
                                                                AppEnvirment.token = UserBean.data.token;
                                                                HttpUtil.getUserInfo(UserBean.data.token, new HttpUtil.Callback<com.zuozhan.app.bean.UserBean>() {
                                                                    @Override
                                                                    public void onResponse(com.zuozhan.app.bean.UserBean call) {
                                                                        if (success && call != null && call.data != null) {
                                                                            ToastUtils.showToast("登录完成");
                                                                            SharedPreferences mSharedPreferences = getSharedPreferences("login_passs", Context.MODE_PRIVATE);
                                                                            SharedPreferences.Editor editor = mSharedPreferences.edit();
                                                                            editor.putString("user_mobile",pass);
                                                                            editor.commit();
                                                                        } else {
                                                                            ToastUtils.showToast("数据同步失败");
                                                                        }
                                                                        dismissLoading();
                                                                        goHome();
                                                                        finish();
                                                                    }
                                                                });
                                                            }
                                                        });
                                                    } else {
                                                        dismissLoading();
                                                        runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                ToastUtils.showToast("聊天服务器登录失败" + result);
                                                            }
                                                        });

                                                    }

                                                }
                                            });

                                        } else {
                                            String message = null;
                                            if (UserBean != null) {
                                                message = UserBean.message;
                                            }
                                            ToastUtils.showToast(message + "");
                                            dismissLoading();
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        ToastUtils.showToast("登陆失败" + e.getMessage());
                                        dismissLoading();
                                    }
                                }

                                @Override
                                public void onFailure(Call<UserBean> call, Throwable t) {
                                    UserCache.clear();
                                    BaseDb.getInstance().logout();
                                    Cache.invalidate();
                                    ToastUtils.showToast("网络请求失败");
                                    dismissLoading();
                                }
                            });
//                    OkHttpClient client = new OkHttpClient.Builder().build();
//                    Retrofit retrofit=new Retrofit.Builder().client(client)
//                            .baseUrl(BaseUrlUtils.NET_BASE_URL)
//                            .addConverterFactory(GsonConverterFactory.create())
//                            .build();
//                    MyService service = retrofit.create(MyService.class);
//                    Call<ResponseBody> login = service.getLogin(name, pass);
//                    login
                }
                break;
            case R.id.ip_set:
                RouterUtil.goActivity(this, ZHServerHostActivity.class);
                break;
            case R.id.update_tv:
                HttpService.getApi(MyService.class)
                        .getServiceApi().selectVersion().enqueue(new Callback<VersionBean>() {
                    @Override
                    public void onResponse(Call<VersionBean> call, Response<VersionBean> response) {
                        if (response != null) {
                            VersionBean versionBean = response.body();
                            if (versionBean != null && versionBean.data != null) {
                                int sVersion = versionBean.data.versionNo;
                                int cVersion = MyUtil.getVersionCode(ZHLoginActivity.this);
                                if (sVersion >= cVersion) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(ZHLoginActivity.this);
                                    builder.setTitle("提示")
                                            .setMessage("存在最新版本" + versionBean.data.version + "，是否进行更新？")
                                            .setNegativeButton("否", null)
                                            .setPositiveButton("是", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    String url = versionBean.data.url;
                                                    start(url);
                                                }
                                            }).show();
                                } else {
                                    ToastUtils.showToast("当前版本已经是最新版本");
                                }
                            } else {
                                ToastUtils.showToast("当前版本已经是最新版本");
                            }
                        } else {
                            ToastUtils.showToast("当前版本已经是最新版本");
                        }
                    }

                    @Override
                    public void onFailure(Call<VersionBean> call, Throwable t) {
                        ToastUtils.showToast("" + t.getMessage());
                    }
                });
                break;
        }
    }

    private void submit() {
        // validate
        String usernameEditString = usernameEdit.getText().toString().trim();
        if (TextUtils.isEmpty(usernameEditString)) {
            Toast.makeText(this, "用户名不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        String passwordEditString = passwordEdit.getText().toString().trim();
        if (TextUtils.isEmpty(passwordEditString)) {
            Toast.makeText(this, "密码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

    }

    private void doIMLogin(String login, String password, LoginCallback loginCallback) {
        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String hostName = sharedPref.getString(Utils.PREFS_HOST_NAME, Cache.HOST_NAME);
        boolean tls = sharedPref.getBoolean(Utils.PREFS_USE_TLS, false);
        final Tinode tinode = Cache.getTinode();
        try {
            LogUtils.d("CONNECTING");
            // This is called on the websocket thread.
            tinode.connect(hostName, tls)
                    .thenApply(
                            new PromisedReply.SuccessListener<ServerMessage>() {
                                @Override
                                public PromisedReply<ServerMessage> onSuccess(ServerMessage ignored) throws Exception {
                                    return tinode.loginBasic(
                                            login,
                                            password);
                                }
                            },
                            new PromisedReply.FailureListener<ServerMessage>() {
                                @Override
                                public PromisedReply<ServerMessage> onFailure(Exception err) throws Exception {

                                    String message = err.getMessage();
//									Throwable cause = err;
//									while ((cause = cause.getCause()) != null) {
//										message = cause.getMessage();
//									}
                                    if (message.contains("409")) {
                                        final Account acc = addAndroidAccount(
                                                tinode.getMyId(),
                                                AuthScheme.basicInstance(login, password).toString(),
                                                tinode.getAuthToken());

                                        // Force immediate sync, otherwise Contacts tab may be unusable. syncadapter
                                        Bundle bundle = new Bundle();
                                        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
                                        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
                                        ContentResolver.requestSync(acc, Utils.SYNC_AUTHORITY, bundle);

                                        UserCache.save(tinode.getMyId(), login, tinode.getAuthToken());
                                        loginCallback.onCallBack(true, message);
                                    } else {
                                        loginCallback.onCallBack(false, message);
                                    }
                                    return null;
                                }
                            })
                    .thenApply(
                            new PromisedReply.SuccessListener<ServerMessage>() {
                                @Override
                                public PromisedReply<ServerMessage> onSuccess(ServerMessage ignored) throws Exception {
                                    sharedPref.edit().putString(LoginActivity.PREFS_LAST_LOGIN, login).apply();

                                    final Account acc = addAndroidAccount(
                                            tinode.getMyId(),
                                            AuthScheme.basicInstance(login, password).toString(),
                                            tinode.getAuthToken());

                                    // Force immediate sync, otherwise Contacts tab may be unusable. syncadapter
                                    Bundle bundle = new Bundle();
                                    bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
                                    bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
                                    ContentResolver.requestSync(acc, Utils.SYNC_AUTHORITY, bundle);

                                    UserCache.save(tinode.getMyId(), login, tinode.getAuthToken());
                                    loginCallback.onCallBack(true, "");
                                    return null;
                                }
                            },
                            new PromisedReply.FailureListener<ServerMessage>() {
                                @Override
                                public PromisedReply<ServerMessage> onFailure(Exception err) throws Exception {
                                    String message = err.getMessage();
//									Throwable cause = err;
//									while ((cause = cause.getCause()) != null) {
//										message = cause.getMessage();
//									}
//                                            if(message.contains("401")){
//                                                loginError(new ServerException(UIUtils.getString(R.string.error_login_failed) + "密码输入错误"));
//                                            }else if(message.contains("409")){
//
//                                            } else{
//                                                loginError(new ServerException(UIUtils.getString(R.string.error_login_failed) + message));
//                                                signIn.setEnabled(true);
//                                            }
                                    if (message.contains("409")) {
                                        final Account acc = addAndroidAccount(
                                                tinode.getMyId(),
                                                AuthScheme.basicInstance(login, password).toString(),
                                                tinode.getAuthToken());

                                        // Force immediate sync, otherwise Contacts tab may be unusable. syncadapter
                                        Bundle bundle = new Bundle();
                                        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
                                        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
                                        ContentResolver.requestSync(acc, Utils.SYNC_AUTHORITY, bundle);

                                        UserCache.save(tinode.getMyId(), login, tinode.getAuthToken());
                                        loginCallback.onCallBack(true, message);
                                    } else {
                                        loginCallback.onCallBack(false, message);
                                    }
                                    return null;
                                }
                            });
        } catch (Exception err) {
            Log.e(this.getClass().getSimpleName(), "Something went wrong", err);
            String message = err.getMessage();
            loginCallback.onCallBack(false, message);
        }
    }

    private Account addAndroidAccount(final String uid, final String secret, final String token) {
        final AccountManager am = AccountManager.get(this);
        final Account acc = Utils.createAccount(uid);
        am.addAccountExplicitly(acc, secret, null);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.notifyAccountAuthenticated(acc);
        }
        if (!TextUtils.isEmpty(token)) {
            am.setAuthToken(acc, Utils.TOKEN_TYPE, token);
        }
        return acc;
    }

    private void start(String url) {
//        startActivity(OpenFileUtil.openFile(Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+"/YiXin/com.yixin.tinode/file/1570590779753.apk"));
        LogUtil.d("开始下载");
        DownloadUtil.downFile(url, this,
                System.currentTimeMillis() + ".apk");
    }


}

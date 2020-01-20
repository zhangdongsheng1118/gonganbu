package com.yixin.tinode.ui.presenter;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;

import com.yixin.tinode.R;
import com.yixin.tinode.model.cache.UserCache;
import com.yixin.tinode.model.exception.ServerException;
import com.yixin.tinode.tinode.Cache;
import com.yixin.tinode.tinode.account.Utils;
import com.yixin.tinode.ui.activity.LoginActivity;
import com.yixin.tinode.ui.activity.MainActivity;
import com.yixin.tinode.ui.base.BaseActivity;
import com.yixin.tinode.ui.base.BasePresenter;
import com.yixin.tinode.ui.view.ILoginAtView;
import com.yixin.tinode.util.LogUtils;
import com.yixin.tinode.util.UIUtils;

import co.tinode.tinodesdk.PromisedReply;
import co.tinode.tinodesdk.Tinode;
import co.tinode.tinodesdk.model.AuthScheme;
import co.tinode.tinodesdk.model.ServerMessage;

public class LoginAtPresenter extends BasePresenter<ILoginAtView> {

    public LoginAtPresenter(BaseActivity context) {
        super(context);
    }

    public void login() {
        String phone = getView().getEtPhone().getText().toString().trim();
        String pwd = getView().getEtPwd().getText().toString().trim();

        if (TextUtils.isEmpty(phone)) {
            UIUtils.showToast(UIUtils.getString(R.string.phone_not_empty));
            return;
        }
        if (TextUtils.isEmpty(pwd)) {
            UIUtils.showToast(UIUtils.getString(R.string.password_not_empty));
            return;
        }

        mContext.showWaitingDialog(UIUtils.getString(R.string.please_wait));
        doLogin(phone, pwd);
    }

    private void loginError(Throwable throwable) {
        LogUtils.e(throwable.getLocalizedMessage());
        UIUtils.showToast(throwable.getLocalizedMessage());
        mContext.hideWaitingDialog();
    }

    private void doLogin(String login, String password) {
        final Button signIn = getView().getBtnLogin();
        signIn.setEnabled(false);

        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        String hostName = sharedPref.getString(Utils.PREFS_HOST_NAME, Cache.HOST_NAME);
        boolean tls = sharedPref.getBoolean(Utils.PREFS_USE_TLS, false);
        final Tinode tinode = Cache.getTinode();
        try {
            LogUtils.e("CONNECTING");
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
                                    mContext.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            loginError(new ServerException(UIUtils.getString(R.string.error_login_failed) + message));
                                            signIn.setEnabled(true);
                                        }
                                    });
                                    return null;
                                }
                            })
                    .thenApply(
                            new PromisedReply.SuccessListener<ServerMessage>() {
                                @Override
                                public PromisedReply<ServerMessage> onSuccess(ServerMessage ignored) throws Exception {
                                    mContext.hideWaitingDialog();
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
                                    mContext.jumpToActivityAndClearTask(MainActivity.class);
                                    mContext.finish();
                                    return null;
                                }
                            },
                            new PromisedReply.FailureListener<ServerMessage>() {
                                @Override
                                public PromisedReply<ServerMessage> onFailure(Exception err) throws Exception {
                                    String message = err.getMessage();
                                    mContext.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (message.contains("401")) {
                                                loginError(new ServerException(UIUtils.getString(R.string.error_login_failed) + "密码输入错误"));
                                            } else if (message.contains("409")) {
                                            } else {
                                                loginError(new ServerException(UIUtils.getString(R.string.error_login_failed) + message));
                                                signIn.setEnabled(true);
                                            }
                                        }
                                    });
                                    return null;
                                }
                            });
        } catch (Exception err) {
            Log.e(this.getClass().getSimpleName(), "Something went wrong", err);
            String message = err.getMessage();
            loginError(new ServerException(UIUtils.getString(R.string.error_login_failed) + message));
            signIn.setEnabled(true);
        }
    }

    private Account addAndroidAccount(final String uid, final String secret, final String token) {
        final AccountManager am = AccountManager.get(mContext);
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
}

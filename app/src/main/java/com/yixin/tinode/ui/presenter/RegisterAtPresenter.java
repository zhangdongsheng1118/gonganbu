package com.yixin.tinode.ui.presenter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;

import com.blankj.utilcode.util.ToastUtils;
import com.yixin.tinode.R;
import com.yixin.tinode.db.tinode.BaseDb;
import com.yixin.tinode.tinode.Cache;
import com.yixin.tinode.tinode.account.Utils;

import com.yixin.tinode.ui.activity.LoginActivity;
import com.yixin.tinode.ui.activity.RegisterActivity;
import com.yixin.tinode.ui.base.BaseActivity;
import com.yixin.tinode.ui.base.BasePresenter;
import com.yixin.tinode.ui.view.IRegisterAtView;
import com.yixin.tinode.util.LogUtils;
import com.yixin.tinode.util.RegularUtils;
import com.yixin.tinode.util.UIUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import co.tinode.tinodesdk.PromisedReply;
import co.tinode.tinodesdk.Tinode;
import co.tinode.tinodesdk.model.MetaSetDesc;
import co.tinode.tinodesdk.model.ServerMessage;
import co.tinode.tinodesdk.model.VCard;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class RegisterAtPresenter extends BasePresenter<IRegisterAtView> {

    int time = 0;
    private Timer mTimer;
    private Subscription mSubscription;

    public RegisterAtPresenter(BaseActivity context) {
        super(context);
    }

    public void sendCode() {
        String phone = getView().getEtPhone().getText().toString().trim();
        if (TextUtils.isEmpty(phone)) {
            UIUtils.showToast(UIUtils.getString(R.string.phone_not_empty));
            return;
        }

        if (!RegularUtils.isMobile(phone)) {
            UIUtils.showToast(UIUtils.getString(R.string.phone_format_error));
            return;
        }

        mContext.showWaitingDialog(UIUtils.getString(R.string.please_wait));
//        ApiRetrofit.getInstance().checkPhoneAvailable(AppConst.REGION, phone)
//                .subscribeOn(Schedulers.io())
//                .flatMap(new Func1<CheckPhoneResponse, Observable<SendCodeResponse>>() {
//                    @Override
//                    public Observable<SendCodeResponse> call(CheckPhoneResponse checkPhoneResponse) {
//                        int code = checkPhoneResponse.getCode();
//                        if (code == 200) {
//                            return ApiRetrofit.getInstance().sendCode(AppConst.REGION, phone);
//                        } else {
//                            return Observable.error(new ServerException(UIUtils.getString(R.string.phone_not_available)));
//                        }
//                    }
//                })
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(sendCodeResponse -> {
//                    mContext.hideWaitingDialog();
//                    int code = sendCodeResponse.getCode();
//                    if (code == 200) {
//                        changeSendCodeBtn();
//                    } else {
//                        sendCodeError(new ServerException(UIUtils.getString(R.string.send_code_error)));
//                    }
//                }, this::sendCodeError);
    }

    private void sendCodeError(Throwable throwable) {
        mContext.hideWaitingDialog();
        LogUtils.e(throwable.getLocalizedMessage());
        UIUtils.showToast(throwable.getLocalizedMessage());
    }

    private void changeSendCodeBtn() {
        //开始1分钟倒计时
        //每一秒执行一次Task
        mSubscription = Observable.create((Observable.OnSubscribe<Integer>) subscriber -> {
            time = 60;
            TimerTask mTask = new TimerTask() {
                @Override
                public void run() {
                    subscriber.onNext(--time);
                }
            };
            mTimer = new Timer();
            mTimer.schedule(mTask, 0, 1000);//每一秒执行一次Task
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(time -> {
                    if (getView().getBtnSendCode() != null) {
                        if (time >= 0) {
                            getView().getBtnSendCode().setEnabled(false);
                            getView().getBtnSendCode().setText(time + "");
                        } else {
                            getView().getBtnSendCode().setEnabled(true);
                            getView().getBtnSendCode().setText(UIUtils.getString(R.string.send_code_btn_normal_tip));
                        }
                    } else {
                        mTimer.cancel();
                    }
                }, throwable -> LogUtils.sf(throwable.getLocalizedMessage()));
    }

    public void register() {
        String userName = getView().getEtUserName().getText().toString().trim();
        String password = getView().getEtPwd().getText().toString().trim();
        String fullName = getView().getEtNickName().getText().toString().trim();

        String phone = getView().getEtPhone().getText().toString().trim();
        String email = getView().getEtEmail().getText().toString().trim();
        String depart = getView().getEtDepart().getText().toString().trim();

        if (TextUtils.isEmpty(userName)) {
            UIUtils.showToast("账号不能为空");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            UIUtils.showToast(UIUtils.getString(R.string.password_not_empty));
            return;
        }
        if (TextUtils.isEmpty(fullName)) {
            UIUtils.showToast(UIUtils.getString(R.string.nickname_not_empty));
            return;
        }
//        if (TextUtils.isEmpty(code)) {
//            UIUtils.showToast(UIUtils.getString(R.string.vertify_code_not_empty));
//            return;
//        }

        final Button signUp = (Button) getView().getBtnSendCode();
        signUp.setEnabled(false);
        final ImageView ivPhoto = (ImageView) getView().getIvPhoto();

        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        String hostName = sharedPref.getString(Utils.PREFS_HOST_NAME, Cache.HOST_NAME);
        boolean tls = sharedPref.getBoolean(Utils.PREFS_USE_TLS, false);
        final Tinode tinode = Cache.getTinode();
        try {
            // This is called on the websocket thread.
            tinode.connect(hostName, tls)
                    .thenApply(
                            new PromisedReply.SuccessListener<ServerMessage>() {
                                @Override
                                public PromisedReply<ServerMessage> onSuccess(ServerMessage ignored_msg) throws Exception {
                                    Bitmap bmp = null;
                                    try {
                                        bmp = ((BitmapDrawable) ivPhoto.getDrawable()).getBitmap();
                                    } catch (ClassCastException ignored) {
                                        // If image is not loaded, the drawable is a vector.
                                        // Ignore it.
                                    }

                                    VCard vcard = new VCard(fullName, bmp, phone, email, depart);
                                    List<String> tags = new ArrayList<String>();
                                    tags.add("name:" + userName);
                                    if (!TextUtils.isEmpty(phone)) {
                                        tags.add("tel:" + phone);
                                    }
                                    if (!TextUtils.isEmpty(email)) {
                                        tags.add("email:" + email);
                                    }
//                                    tags.toArray(new String[tags.size()])
                                    return tinode.createAccountBasic(
                                            userName, password, true,  tags.toArray(new String[tags.size()]),
                                            new MetaSetDesc<VCard, String>(vcard, null), null);
                                            //Credential.append(null, new Credential("email", email)));
                                }
                            }, null)
                    .thenApply(
                            new PromisedReply.SuccessListener<ServerMessage>() {
                                @Override
                                public PromisedReply<ServerMessage> onSuccess(ServerMessage ignored) throws Exception {
                                    // Flip back to login screen on success;
                                    mContext.runOnUiThread(new Runnable() {
                                        public void run() {
                                            signUp.setEnabled(true);
                                            ToastUtils.showShort("注册成功");

                                            BaseDb.getInstance().logout();
                                            Cache.invalidate();

                                            mContext.startActivity(new Intent(mContext, LoginActivity.class));
                                            mContext.finish();
                                        }
                                    });
                                    return null;
                                }
                            },
                            new PromisedReply.FailureListener<ServerMessage>() {
                                @Override
                                public PromisedReply<ServerMessage> onFailure(Exception err) throws Exception {
                                    registerError(err, signUp);
                                    return null;
                                }
                            });

        } catch (Exception e) {
            Log.e(RegisterActivity.class.getSimpleName(), "Something went wrong", e);
            mContext.runOnUiThread(new Runnable() {
                public void run() {
                    signUp.setEnabled(true);
                }
            });
        }
    }

    private void registerError(Throwable throwable, Button signUp) {
        LogUtils.sf(throwable.getLocalizedMessage());
        mContext.runOnUiThread(new Runnable() {
            public void run() {
                signUp.setEnabled(true);
                UIUtils.showToast(mContext.getString(R.string.register_error) + throwable.getLocalizedMessage());
            }
        });
    }

    public void unsubscribe() {
        if (mSubscription != null) {
            mSubscription.unsubscribe();
            mSubscription = null;
        }
    }

}

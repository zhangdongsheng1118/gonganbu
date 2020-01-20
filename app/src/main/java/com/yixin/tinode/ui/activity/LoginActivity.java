package com.yixin.tinode.ui.activity;


import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;

import com.yixin.tinode.R;
import com.yixin.tinode.ui.base.BaseActivity;
import com.yixin.tinode.ui.presenter.LoginAtPresenter;
import com.yixin.tinode.ui.view.ILoginAtView;
import com.yixin.tinode.util.UIUtils;

import butterknife.BindView;
import butterknife.OnClick;
import kr.co.namee.permissiongen.PermissionGen;

import static com.lqr.emoji.EmotionKeyboard.getSoftButtonsBarHeight;


/**
 * @创建者 liulei
 * @描述 登录界面
 */
public class LoginActivity extends BaseActivity<ILoginAtView, LoginAtPresenter> implements ILoginAtView {
    public static final String PREFS_LAST_LOGIN = "pref_lastLogin";

    @BindView(R.id.etPhone)
    EditText mEtPhone;
    @BindView(R.id.vLinePhone)
    View mVLinePhone;
    @BindView(R.id.etPwd)
    EditText mEtPwd;
    @BindView(R.id.vLinePwd)
    View mVLinePwd;
    @BindView(R.id.btnLogin)
    Button mBtnLogin;

    @OnClick({R.id.tvOtherLogin, R.id.tvIpSetUp})
    public void click(View v) {
        switch (v.getId()) {
            case R.id.tvOtherLogin:
                jumpToActivity(RegisterActivity.class);
                break;
            case R.id.tvIpSetUp:
                jumpToActivity(IpSetUpActivity.class);
                break;
        }
    }

    TextWatcher watcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            mBtnLogin.setEnabled(canLogin());
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    @Override
    public void initView() {
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        String login = pref.getString(LoginActivity.PREFS_LAST_LOGIN, null);
        if (!TextUtils.isEmpty(login)) {
            mEtPhone.setText(login);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            PermissionGen.with(this)
                    .addRequestCode(2)
                    .permissions(Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS)
                    .request();
        }

        getWindow().getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                //TODO 表情键盘高度计算，仍然有bug。后续修复
                Rect r = new Rect();
                // r will be populated with the coordinates of your view that area still visible.
                getWindow().getDecorView().getRootView().getWindowVisibleDisplayFrame(r);
                int screenHeight = getWindow().getDecorView().getHeight();//.getRootView().getHeight();
                int realKeyboardHeight = screenHeight - (r.bottom - r.top);
                if (Build.VERSION.SDK_INT >= 20) {
                    realKeyboardHeight -= getSoftButtonsBarHeight(context) / 2;
                }

                if (realKeyboardHeight > screenHeight / 5) {
                    getWindow().getDecorView().getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    getSharedPreferences("EmotionKeyBoard", MODE_PRIVATE).edit().putInt("sofe_input_height", realKeyboardHeight).apply();
                }
                Log.i("", "keyboard height = " + realKeyboardHeight);
            }
        });
    }

    @Override
    public void initListener() {
        mEtPwd.addTextChangedListener(watcher);
        mEtPhone.addTextChangedListener(watcher);
        mEtPwd.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                mVLinePwd.setBackgroundColor(UIUtils.getColor(R.color.green0));
            } else {
                mVLinePwd.setBackgroundColor(UIUtils.getColor(R.color.line));
            }
        });
        mEtPhone.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                mVLinePhone.setBackgroundColor(UIUtils.getColor(R.color.green0));
            } else {
                mVLinePhone.setBackgroundColor(UIUtils.getColor(R.color.line));
            }
        });

        mBtnLogin.setOnClickListener(v -> mPresenter.login());
    }

    private boolean canLogin() {
        int pwdLength = mEtPwd.getText().toString().trim().length();
        int phoneLength = mEtPhone.getText().toString().trim().length();
        if (pwdLength > 0 && phoneLength > 0) {
            return true;
        }
        return false;
    }


    @Override
    protected LoginAtPresenter createPresenter() {
        return new LoginAtPresenter(this);
    }

    @Override
    protected int provideContentViewId() {
        return R.layout.activity_login;
    }

    @Override
    public EditText getEtPhone() {
        return mEtPhone;
    }

    @Override
    public EditText getEtPwd() {
        return mEtPwd;
    }

    @Override
    public Button getBtnLogin() {
        return mBtnLogin;
    }
}
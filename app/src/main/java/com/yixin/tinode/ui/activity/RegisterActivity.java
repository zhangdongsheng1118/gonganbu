package com.yixin.tinode.ui.activity;

import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.yixin.tinode.R;
import com.yixin.tinode.tinode.util.UiUtils;
import com.yixin.tinode.ui.base.BaseActivity;
import com.yixin.tinode.ui.presenter.RegisterAtPresenter;
import com.yixin.tinode.ui.view.IRegisterAtView;
import com.yixin.tinode.util.UIUtils;

import butterknife.BindView;


public class RegisterActivity extends BaseActivity<IRegisterAtView, RegisterAtPresenter> implements IRegisterAtView {

	@BindView(R.id.etNick)
	EditText mEtNick;
	@BindView(R.id.vLineNick)
	View mVLineNick;

	@BindView(R.id.etPhone)
	EditText mEtPhone;
	@BindView(R.id.vLinePhone)
	View mVLinePhone;

	@BindView(R.id.etPwd)
	EditText mEtPwd;
	@BindView(R.id.ivSeePwd)
	ImageView mIvSeePwd;
	@BindView(R.id.vLinePwd)
	View mVLinePwd;

	@BindView(R.id.etVerifyCode)
	EditText mEtVerifyCode;
	@BindView(R.id.btnSendCode)
	Button mBtnSendCode;
	@BindView(R.id.vLineVertifyCode)
	View mVLineVertifyCode;


	@BindView(R.id.etEmail)
	EditText mEtEmail;
	@BindView(R.id.etUserName)
	EditText mEtUserName;
	@BindView(R.id.etDepart)
	EditText mEtDepart;

	@BindView(R.id.btnRegister)
	Button mBtnRegister;
	@BindView(R.id.ivAlbum)
	ImageView ivPhoto;

	TextWatcher watcher = new TextWatcher() {
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			mBtnRegister.setEnabled(canRegister());
		}

		@Override
		public void afterTextChanged(Editable s) {
		}
	};

	@Override
	public void initListener() {
		mEtNick.addTextChangedListener(watcher);
		mEtPwd.addTextChangedListener(watcher);
		mEtUserName.addTextChangedListener(watcher);

		mEtVerifyCode.addTextChangedListener(watcher);
//		mEtPhone.addTextChangedListener(watcher);

		mEtNick.setOnFocusChangeListener((v, hasFocus) -> {
			if (hasFocus) {
				mVLineNick.setBackgroundColor(UIUtils.getColor(R.color.green0));
			} else {
				mVLineNick.setBackgroundColor(UIUtils.getColor(R.color.line));
			}
		});
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
		mEtVerifyCode.setOnFocusChangeListener((v, hasFocus) -> {
			if (hasFocus) {
				mVLineVertifyCode.setBackgroundColor(UIUtils.getColor(R.color.green0));
			} else {
				mVLineVertifyCode.setBackgroundColor(UIUtils.getColor(R.color.line));
			}
		});

		mIvSeePwd.setOnClickListener(v -> {

			if (mEtPwd.getTransformationMethod() == HideReturnsTransformationMethod.getInstance()) {
				mEtPwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
			} else {
				mEtPwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
			}

			mEtPwd.setSelection(mEtPwd.getText().toString().trim().length());
		});

		mBtnSendCode.setOnClickListener(v -> {
			if (mBtnSendCode.isEnabled()) {
				mPresenter.sendCode();
			}
		});

		mBtnRegister.setOnClickListener(v -> {
			mPresenter.register();
		});

		ivPhoto.setOnClickListener(v->{
			UiUtils.requestAvatar(context);
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mPresenter.unsubscribe();
	}

	private boolean canRegister() {
		int nickNameLength = mEtNick.getText().toString().trim().length();
		int pwdLength = mEtPwd.getText().toString().trim().length();
		int userNameLength = mEtUserName.getText().toString().trim().length();

		int phoneLength = mEtPhone.getText().toString().trim().length();
		int codeLength = mEtVerifyCode.getText().toString().trim().length();
		if (nickNameLength > 1 && pwdLength > 2 && userNameLength > 2) {
			return true;
		}
		return false;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == UiUtils.SELECT_PICTURE && resultCode == RESULT_OK) {
			UiUtils.acceptAvatar(context, ivPhoto, data);
		}
	}

	@Override
	protected RegisterAtPresenter createPresenter() {
		return new RegisterAtPresenter(this);
	}

	@Override
	protected int provideContentViewId() {
		return R.layout.activity_register;
	}

	@Override
	public EditText getEtNickName() {
		return mEtNick;
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
	public EditText getEtVerifyCode() {
		return mEtVerifyCode;
	}

	@Override
	public Button getBtnSendCode() {
		return mBtnSendCode;
	}

	@Override
	public EditText getEtEmail() {
		return mEtEmail;
	}

	@Override
	public EditText getEtUserName() {
		return mEtUserName;
	}

	@Override
	public EditText getEtDepart() {
		return mEtDepart;
	}

	@Override
	public ImageView getIvPhoto() {
		return ivPhoto;
	}
}

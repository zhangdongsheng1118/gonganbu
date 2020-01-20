package com.yixin.tinode.ui.view;


import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

public interface IRegisterAtView {

    EditText getEtNickName();

    EditText getEtPhone();

    EditText getEtPwd();

    EditText getEtVerifyCode();


    EditText getEtEmail();
    EditText getEtUserName();
    EditText getEtDepart();

    Button getBtnSendCode();
    ImageView getIvPhoto();
}

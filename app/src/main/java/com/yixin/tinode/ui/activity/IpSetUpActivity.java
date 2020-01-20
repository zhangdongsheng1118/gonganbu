package com.yixin.tinode.ui.activity;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.preference.PreferenceManager;
import android.text.InputFilter;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.yixin.tinode.R;
import com.yixin.tinode.tinode.Cache;
import com.yixin.tinode.tinode.account.Utils;
import com.yixin.tinode.ui.base.BaseActivity;
import com.yixin.tinode.ui.base.BasePresenter;
import com.yixin.tinode.util.StringUtils;

import butterknife.BindView;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

public class IpSetUpActivity extends BaseActivity {
    SharedPreferences sharedPref ;
    @BindView(R.id.et_IM_IP)
    EditText etIMIP;
    @BindView(R.id.et_IM_Port)
    EditText etIMPort;
    @BindView(R.id.et_video_IP)
    EditText etVideoIP;
    @BindView(R.id.et_video_Port)
    EditText etVideoPort;
    @BindView(R.id.tls_is_enable)
    RadioGroup tlsGroup;
    @BindView(R.id.video_quality)
    RadioGroup videoQuality;
    @BindView(R.id.btn_save)
    Button btnSave;
    @OnCheckedChanged({R.id.tls_enable, R.id.tls_disable})
    public void onRadioButton2CheckChanged(CompoundButton button, boolean checked) {
        if(checked) {
            switch (button.getId()) {
                case R.id.tls_enable:
                    // do stuff
                    break;
                case R.id.tls_disable:
                    // do stuff
                    break;
            }
        }
    }
    @OnCheckedChanged({R.id.rb_high_quality, R.id.rb_middle_quality, R.id.rb_low_quality})
    public void onRadioButtonCheckChanged(CompoundButton button, boolean checked) {
        if(checked) {
            switch (button.getId()) {
                case R.id.rb_high_quality:
                    // do stuff
                    break;
                case R.id.rb_middle_quality:
                    // do stuff
                    break;
                case R.id.rb_low_quality:
                    // do stuff
                    break;
            }
        }
    }


    @Override
    public void initListener() {
        super.initListener();
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String IMIP = etIMIP.getText().toString();
                String IMport = etIMPort.getText().toString();
                String videoIP = etVideoIP.getText().toString();
                String videoPort = etVideoPort.getText().toString();
                if(StringUtils.isBlank(IMIP)||StringUtils.isBlank(IMport)){
                    Toast.makeText(IpSetUpActivity.this,"IM服务器IP地址或端口号不能为空",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(StringUtils.isBlank(videoIP)||StringUtils.isBlank(videoPort)){
                    Toast.makeText(IpSetUpActivity.this,"音视频IP地址或端口号不能为空",Toast.LENGTH_SHORT).show();
                    return;
                }

                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(Utils.PREFS_HOST_NAME,IMIP+":"+IMport);
                editor.putString(Utils.VIDEO_HOST_NAME,videoIP+":"+videoPort);
                editor.putInt(Utils.VIDEO_QUALITY,videoQuality.getCheckedRadioButtonId());

                //editor.putBoolean(Utils.PREFS_USE_TLS,true);
                editor.apply();
                Toast.makeText(IpSetUpActivity.this,"保存成功，请返回重试登录。",Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void initView() {
        super.initView();
        videoQuality.check(R.id.rb_low_quality);
        InputFilter[] filters = getInputFilters();
        etIMIP.setFilters(filters);
        etVideoIP.setFilters(filters);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        String IMHostName = sharedPref.getString(Utils.PREFS_HOST_NAME, Cache.HOST_NAME);
        String videoHostName = sharedPref.getString(Utils.VIDEO_HOST_NAME, Cache.HOST_NAME);
        //boolean IsTls=sharedPref.getBoolean(Utils.PREFS_USE_TLS,true);
        int buttonId = sharedPref.getInt(Utils.VIDEO_QUALITY,R.id.rb_low_quality);
        if(!StringUtils.isBlank(IMHostName)){
            String [] array = IMHostName.split(":");
            etIMIP.setText(array[0]);
            etIMPort.setText(array[1]);
        }
        if(!StringUtils.isBlank(videoHostName)){
            String [] array = videoHostName.split(":");
            etVideoIP.setText(array[0]);
            etVideoPort.setText(array[1]);
        }
        tlsGroup.setVisibility(View.INVISIBLE);
        //tlsGroup.check(IsTls?1:0);
        videoQuality.check(buttonId);

    }

    @NonNull
    private InputFilter[] getInputFilters() {
        InputFilter[] filters = new InputFilter[1];
        filters[0] = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end,
                                       android.text.Spanned dest, int dstart, int dend) {
                if (end > start) {
                    String destTxt = dest.toString();
                    String resultingTxt = destTxt.substring(0, dstart)
                            + source.subSequence(start, end)
                            + destTxt.substring(dend);
                    if (!resultingTxt
                            .matches("^\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3}(\\.(\\d{1,3})?)?)?)?)?)?")) {
                        return "";
                    } else {
                        String[] splits = resultingTxt.split("\\.");
                        for (int i = 0; i < splits.length; i++) {
                            if (Integer.valueOf(splits[i]) > 255) {
                                return "";
                            }
                        }
                    }
                }
                return null;
            }

        };
        return filters;
    }

    @Override
    protected BasePresenter createPresenter() {
        return null;
    }

    @Override
    protected int provideContentViewId() {
        return R.layout.activity_ip_setup;
    }
}

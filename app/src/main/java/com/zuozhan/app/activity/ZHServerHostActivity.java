package com.zuozhan.app.activity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.starrtc.demo.R;
import com.starrtc.demo.demo.MLOC;
import com.starrtc.demo.utils.AEvent;
import com.yixin.tinode.tinode.Cache;
import com.yixin.tinode.tinode.account.Utils;
import com.zuozhan.app.BaseIP;
import com.zuozhan.app.util.LogUtil;
import com.zuozhan.app.util.ServerHostShareprefrensUtils;
import com.zuozhan.app.util.ShareprefrensUtils;
import com.zuozhan.app.util.ToastUtils;

import butterknife.BindView;

public class ZHServerHostActivity extends AllBaseActivity {


    EditText ip1, ip2, ip3, ip4, ip5, ip6, guding_ptt, guding_meet, location_time, location_upload_time;
    View btn, btn2, btn3,btn4,btn5;

    RadioGroup group, group2;
    CheckBox quchu_im,quchu_map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zh_activity_ip_server_host);
        findViewById(R.id.title_left_btn).setVisibility(View.VISIBLE);
        findViewById(R.id.title_left_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        ((TextView) findViewById(R.id.title_text)).setText("IP配置");
        ip1 = findViewById(R.id.ip1);
        ip2 = findViewById(R.id.ip2);
        ip3 = findViewById(R.id.ip3);
        ip4 = findViewById(R.id.ip4);
        ip5 = findViewById(R.id.ip5);
        ip6 = findViewById(R.id.ip6);
        btn = findViewById(R.id.btn);
        btn5 = findViewById(R.id.btn5);
        guding_ptt = findViewById(R.id.guding_ptt);
        guding_meet = findViewById(R.id.guding_meet);
        quchu_im = findViewById(R.id.quchu_im);
        group = findViewById(R.id.group);
        group2 = findViewById(R.id.group2);
        btn2 = findViewById(R.id.btn2);
        btn3 = findViewById(R.id.btn3);
        btn4 = findViewById(R.id.btn4);
        quchu_map = findViewById(R.id.quchu_map);
        location_time = findViewById(R.id.location_time);
        location_upload_time = findViewById(R.id.location_upload_time);

        ip1.setText(BaseIP.map_ip);
        ip2.setText(BaseIP.base_ip);
        ip3.setText(BaseIP.rtc_ip);
        ip4.setText(BaseIP.rtc_push_ip);
        ip5.setText(BaseIP.IM_ip);
        ip6.setText(BaseIP.IM_ip2);
        guding_ptt.setText(BaseIP.GUDING_PTT);
        guding_meet.setText(BaseIP.GUDING_MEET);

        location_time.setText(BaseIP.location_time + "");
        location_upload_time.setText(BaseIP.location_upload_time + "");

        quchu_im.setChecked(BaseIP.quchu_im);
        quchu_map.setChecked(!BaseIP.isShowMap);

        group.check(BaseIP.isHttps ? R.id.rbtn2 : R.id.rbtn1);
        group2.check(BaseIP.camera == 0 ? R.id.rbtn1_2 : R.id.rbtn2_2);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String map_ip = ip1.getText().toString();
                BaseIP.map_ip = map_ip;
                BaseIP.IP_MAP = "http://" + map_ip + "/map?caseId=";

                String base_ip = ip2.getText().toString();
                BaseIP.base_ip = base_ip;
                if (group.getCheckedRadioButtonId() == R.id.rbtn1) {
                    BaseIP.isHttps = false;
                    BaseIP.NET_BASE_URL = "http://" + base_ip + "/";
                } else {
                    BaseIP.isHttps = true;
                    BaseIP.NET_BASE_URL = "https://" + base_ip + "/";
                }

                String rtc_ip = ip3.getText().toString();
                BaseIP.rtc_ip = rtc_ip;
                MLOC.IP = rtc_ip;
                String rtc_push_ip = ip4.getText().toString();
                BaseIP.rtc_push_ip = rtc_push_ip;
                MLOC.PUSH = "rtmp://" + rtc_push_ip + "/hls/";

                String IM_ip = ip5.getText().toString();
                BaseIP.IM_ip = IM_ip;
                BaseIP.IM_IP = IM_ip;

                String IM_ip2 = ip6.getText().toString();
                BaseIP.IM_ip2 = IM_ip2;
                BaseIP.IM_IP_2 = "http://" + IM_ip2;

                BaseIP.quchu_im = quchu_im.isChecked();

                BaseIP.GUDING_PTT = guding_ptt.getText().toString();
                BaseIP.GUDING_MEET = guding_meet.getText().toString();

                BaseIP.location_time = Integer.parseInt(location_time.getText().toString());
                BaseIP.location_upload_time = Integer.parseInt(location_upload_time.getText().toString());

                if (group2.getCheckedRadioButtonId() == R.id.rbtn1_2) {
                    BaseIP.camera = 0;
                } else {
                    BaseIP.camera = 1;
                }

                BaseIP.isShowMap = !quchu_map.isChecked();

                String IP = MLOC.IP;
                MLOC.VOIP_SERVER_URL          = IP+":10086";
                MLOC.IM_SERVER_URL            = IP+":19903";
                MLOC.CHATROOM_SERVER_URL      = IP+":19906";
                MLOC.LIVE_VDN_SERVER_URL      = IP+":19928";
                MLOC.LIVE_SRC_SERVER_URL      = IP+":19931";
                MLOC.LIVE_PROXY_SERVER_URL    = IP+":19932";

                Cache.HOST_NAME = BaseIP.IM_IP;
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ZHServerHostActivity.this);
                sharedPref.edit().putString(Utils.PREFS_HOST_NAME, Cache.HOST_NAME).commit();

                ServerHostShareprefrensUtils.save();
                ToastUtils.showToast("状态已经保存");
                finish();
                LogUtil.d(BaseIP.IP_MAP);
                LogUtil.d(BaseIP.NET_BASE_URL);
                LogUtil.d(MLOC.IP);
                LogUtil.d(MLOC.PUSH);
                LogUtil.d(BaseIP.IM_IP);
                LogUtil.d(BaseIP.IM_IP_2);
            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ip1.setText("47.111.133.91:3000");
                ip2.setText("39.98.37.28:8085");
                ip3.setText("command.yiqizhongbao.com");
                ip4.setText("47.111.141.221:10085");
                ip5.setText("47.94.235.90:6060");
                ip6.setText("47.94.235.90:8090");
                ToastUtils.showToast("请点击保存，完成配置");
            }
        });

        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ip1.setText("114.255.88.226:3000");
                ip2.setText("114.255.88.226:8085");
                ip3.setText("114.255.88.226");
                ip4.setText("114.255.88.226:10085");
                ip5.setText("114.255.88.226:6060");
                ip6.setText("114.255.88.226:8090");
                ToastUtils.showToast("请点击保存，完成配置");
            }
        });

        btn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ip1.setText("123.124.207.165:7780/#");
                ip2.setText("123.124.207.165:8085");
                ip3.setText("123.124.207.165");
                ip4.setText("123.124.207.165:10085");
                ip5.setText("123.124.207.165:6060");
                ip6.setText("123.124.207.165:8090");
                ToastUtils.showToast("请点击保存，完成配置");
            }
        });
        btn5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ip1.setText("20.60.0.62:7780/#");
                ip2.setText("20.60.0.62:8085");
                ip3.setText("20.60.0.62");
                ip4.setText("20.60.0.62:10085");
                ip5.setText("20.60.0.62:6060");
                ip6.setText("20.60.0.62:8090");
                ToastUtils.showToast("请点击保存，完成配置");
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

    }
}

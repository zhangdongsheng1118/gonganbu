package com.starrtc.demo.demo.voip;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.starrtc.demo.R;
import com.starrtc.demo.demo.BaseActivity;
import com.starrtc.demo.demo.MLOC;
import com.starrtc.demo.database.CoreDB;
import com.starrtc.demo.database.HistoryBean;
import com.starrtc.demo.demo.VoipHttpListener;
import com.starrtc.demo.demo.ZHHttpManager;
import com.starrtc.demo.ui.CircularCoverView;
import com.starrtc.demo.utils.AEvent;
import com.starrtc.demo.utils.ColorUtils;
import com.starrtc.demo.utils.DensityUtils;
import com.starrtc.starrtcsdk.api.XHClient;
import com.starrtc.starrtcsdk.apiInterface.IXHResultCallback;

import java.text.SimpleDateFormat;

public class VoipRingingActivity extends BaseActivity implements View.OnClickListener {

    private String targetId;
    private ImageView head_img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN ,
                WindowManager.LayoutParams. FLAG_FULLSCREEN);
        setContentView(R.layout.activity_voip_ringing);
        addListener();
        head_img = findViewById(R.id.head_img);
        targetId = getIntent().getStringExtra("targetId");
        findViewById(R.id.ring_hangoff).setOnClickListener(this);
        findViewById(R.id.ring_pickup).setOnClickListener(this);
        ((TextView)findViewById(R.id.targetid_text)).setText(targetId);
//        findViewById(R.id.head_bg).setBackgroundColor(ColorUtils.getColor(VoipRingingActivity.this,targetId));
        ((CircularCoverView)findViewById(R.id.head_cover)).setCoverColor(Color.parseColor("#000000"));
        int cint = DensityUtils.dip2px(VoipRingingActivity.this,45);
        ((CircularCoverView)findViewById(R.id.head_cover)).setRadians(cint, cint, cint, cint,0);

        HistoryBean historyBean = new HistoryBean();
        historyBean.setType(CoreDB.HISTORY_TYPE_VOIP);
        historyBean.setLastTime(new SimpleDateFormat("MM-dd HH:mm").format(new java.util.Date()));
        historyBean.setConversationId(targetId);
        historyBean.setNewMsgCount(1);
        MLOC.addHistory(historyBean,true);

        ZHHttpManager.getZhHttpManager().getHeadPicById(targetId, new VoipHttpListener() {
            @Override
            public void onUserInfo(String userID, String headpic) {
                try {
                    ((TextView)findViewById(R.id.targetid_text)).setText(userID);
                    Glide.with(VoipRingingActivity.this).load(headpic).into(head_img);
                } catch (Exception e){}

            }
        });
    }

    public void addListener(){
        AEvent.addListener(AEvent.AEVENT_VOIP_REV_HANGUP,this);
        AEvent.addListener(AEvent.AEVENT_VOIP_REV_ERROR,this);
    }

    public void removeListener(){
        AEvent.removeListener(AEvent.AEVENT_VOIP_REV_HANGUP,this);
        AEvent.removeListener(AEvent.AEVENT_VOIP_REV_ERROR,this);
    }

    @Override
    public void dispatchEvent(final String aEventID, boolean success, final Object eventObj) {
        super.dispatchEvent(aEventID,success,eventObj);
        switch (aEventID){
            case AEvent.AEVENT_VOIP_REV_HANGUP:
                MLOC.d("","对方已挂断");
                MLOC.showMsg(VoipRingingActivity.this,"对方已挂断");
                finishSeft();
                break;
            case AEvent.AEVENT_VOIP_REV_ERROR:
                MLOC.showMsg(VoipRingingActivity.this, (String) eventObj);
                finishSeft();
                break;
        }
    }

    @Override
    public void onRestart(){
        super.onRestart();
        addListener();
    }

    @Override
    public void onStop(){
        super.onStop();
        removeListener();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.ring_hangoff) {
            XHClient.getInstance().getVoipManager().refuse(new IXHResultCallback() {
                @Override
                public void success(Object data) {
                    finishSeft();
                }

                @Override
                public void failed(String errMsg) {
                    finishSeft();
                }
            });
        } else if (i == R.id.ring_pickup) {
            Intent intent = new Intent(VoipRingingActivity.this, VoipActivity.class);
            intent.putExtra("targetId", targetId);
            intent.putExtra(VoipActivity.ACTION, VoipActivity.RING);
            startActivity(intent);
            finish();
        }
    }

    private void finishSeft(){
        finish();
        AEvent.notifyListener(AEvent.AEVENT_VOIP_STOP,true,"");
    }
}

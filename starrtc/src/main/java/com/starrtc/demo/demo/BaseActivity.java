package com.starrtc.demo.demo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.starrtc.demo.R;
import com.starrtc.demo.demo.p2p.VoipP2PRingingActivity;
import com.starrtc.demo.demo.voip.VoipRingingActivity;
import com.starrtc.demo.utils.AEvent;
import com.starrtc.demo.utils.IEventListener;
import com.starrtc.starrtcsdk.api.XHClient;
import com.starrtc.starrtcsdk.core.im.message.XHIMMessage;

import org.json.JSONException;
import org.json.JSONObject;

public class BaseActivity extends AppCompatActivity implements IEventListener {

    @Override
    protected void onResume() {
        super.onResume();
        if(findViewById(R.id.c2c_new)!=null){
            findViewById(R.id.c2c_new).setVisibility(MLOC.hasNewC2CMsg?View.VISIBLE:View.INVISIBLE);
        }
        if(findViewById(R.id.im_new)!=null) {
            findViewById(R.id.im_new).setVisibility((MLOC.hasNewC2CMsg || MLOC.hasNewGroupMsg) ? View.VISIBLE : View.INVISIBLE);
        }
        if(findViewById(R.id.group_new)!=null){
            findViewById(R.id.group_new).setVisibility(MLOC.hasNewGroupMsg?View.VISIBLE:View.INVISIBLE);
        }
        if(findViewById(R.id.im_new)!=null) {
            findViewById(R.id.im_new).setVisibility((MLOC.hasNewC2CMsg || MLOC.hasNewGroupMsg) ? View.VISIBLE : View.INVISIBLE);
        }
        if(findViewById(R.id.voip_new)!=null){
            findViewById(R.id.voip_new).setVisibility(MLOC.hasNewVoipMsg?View.VISIBLE:View.INVISIBLE);
        }
        if(findViewById(R.id.loading)!=null) {
            if (XHClient.getInstance().getIsOnline()) {
                findViewById(R.id.loading).setVisibility(View.INVISIBLE);
            } else {
                findViewById(R.id.loading).setVisibility(View.VISIBLE);
            }
        }
        addListener();
    }
    @Override
    protected void onPause() {
        super.onPause();
        removeListener();
    }

    private void addListener(){
        AEvent.addListener(AEvent.AEVENT_VOIP_REV_CALLING,this);
        AEvent.addListener(AEvent.AEVENT_VOIP_P2P_REV_CALLING,this);
        AEvent.addListener(AEvent.AEVENT_C2C_REV_MSG,this);
        AEvent.addListener(AEvent.AEVENT_REV_SYSTEM_MSG,this);
        AEvent.addListener(AEvent.AEVENT_GROUP_REV_MSG,this);
        AEvent.addListener(AEvent.AEVENT_USER_ONLINE,this);
        AEvent.addListener(AEvent.AEVENT_USER_OFFLINE,this);
    }
    private void removeListener(){
        AEvent.removeListener(AEvent.AEVENT_VOIP_REV_CALLING,this);
        AEvent.removeListener(AEvent.AEVENT_VOIP_P2P_REV_CALLING,this);
        AEvent.removeListener(AEvent.AEVENT_C2C_REV_MSG,this);
        AEvent.removeListener(AEvent.AEVENT_REV_SYSTEM_MSG,this);
        AEvent.removeListener(AEvent.AEVENT_GROUP_REV_MSG,this);
        AEvent.removeListener(AEvent.AEVENT_USER_ONLINE,this);
        AEvent.removeListener(AEvent.AEVENT_USER_OFFLINE,this);
    }

    @Override
    public void dispatchEvent(String aEventID, boolean success, final Object eventObj) {
        switch (aEventID){
            case AEvent.AEVENT_VOIP_REV_CALLING:
                if(!MLOC.canPickupVoip){
                    MLOC.hasNewVoipMsg = true;
                    try {
                        JSONObject alertData = new JSONObject();
                        alertData.put("type",2);
                        alertData.put("farId",eventObj.toString());
                        alertData.put("msg","收到视频通话请求");
                        MLOC.showDialog(BaseActivity.this,alertData);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
//                else{
//                    Intent intent = new Intent(BaseActivity.this,VoipRingingActivity.class);
//                    intent.putExtra("targetId",eventObj.toString());
//                    startActivity(intent);
//                }
                break;
            case AEvent.AEVENT_VOIP_P2P_REV_CALLING:
//                if(MLOC.canPickupVoip){
//                    MLOC.hasNewVoipMsg = true;
//                    Intent intent = new Intent(BaseActivity.this,VoipP2PRingingActivity.class);
//                    intent.putExtra("targetId",eventObj.toString());
//                    startActivity(intent);
//                }
                break;
            case AEvent.AEVENT_C2C_REV_MSG:
                MLOC.hasNewC2CMsg = true;
                if(findViewById(R.id.c2c_new)!=null){
                    findViewById(R.id.c2c_new).setVisibility(MLOC.hasNewC2CMsg?View.VISIBLE:View.INVISIBLE);
                }
                if(findViewById(R.id.im_new)!=null) {
                    findViewById(R.id.im_new).setVisibility((MLOC.hasNewC2CMsg || MLOC.hasNewGroupMsg) ? View.VISIBLE : View.INVISIBLE);
                }
                try {
                    XHIMMessage revMsg = (XHIMMessage) eventObj;
                    JSONObject alertData = new JSONObject();
                    alertData.put("type",0);
                    alertData.put("farId",revMsg.fromId);
                    alertData.put("msg","收到一条新消息");
                    MLOC.showDialog(BaseActivity.this,alertData);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case AEvent.AEVENT_GROUP_REV_MSG:
                MLOC.hasNewGroupMsg = true;
                if(findViewById(R.id.group_new)!=null){
                    findViewById(R.id.group_new).setVisibility(MLOC.hasNewGroupMsg?View.VISIBLE:View.INVISIBLE);
                }
                if(findViewById(R.id.im_new)!=null) {
                    findViewById(R.id.im_new).setVisibility((MLOC.hasNewC2CMsg || MLOC.hasNewGroupMsg) ? View.VISIBLE : View.INVISIBLE);
                }
                try {
                    XHIMMessage revMsg = (XHIMMessage) eventObj;
                    JSONObject alertData = new JSONObject();
                    alertData.put("type",1);
                    alertData.put("farId",revMsg.targetId);
                    alertData.put("msg","收到一条群消息");
                    MLOC.showDialog(BaseActivity.this,alertData);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            case AEvent.AEVENT_USER_OFFLINE:
                MLOC.showMsg(BaseActivity.this,"服务已断开");
                if(findViewById(R.id.loading)!=null) {
                    if (XHClient.getInstance().getIsOnline()) {
                        findViewById(R.id.loading).setVisibility(View.INVISIBLE);
                    } else {
                        findViewById(R.id.loading).setVisibility(View.VISIBLE);
                    }
                }
                reconnectRTC();
                break;
            case AEvent.AEVENT_USER_ONLINE:
                if(findViewById(R.id.loading)!=null) {
                    if (XHClient.getInstance().getIsOnline()) {
                        findViewById(R.id.loading).setVisibility(View.INVISIBLE);
                    } else {
                        findViewById(R.id.loading).setVisibility(View.VISIBLE);
                    }
                }
                break;
        }
    }

    ProgressDialog pd;

    public void showLoading(String string) {
        try {
            if (pd == null) {
                pd = new ProgressDialog(this);
                pd.setCanceledOnTouchOutside(false);
                pd.setCancelable(false);
            }
            if (TextUtils.isEmpty(string)) {
                pd.setMessage("加载中...");
            } else {
                pd.setMessage("" + string);
            }
            if (!pd.isShowing()) {
                pd.show();
            }
        } catch (Throwable e) {
        }
    }

    public void showLoading(int string) {
        if (pd == null) {
            pd = new ProgressDialog(this);
            pd.setCanceledOnTouchOutside(false);
            pd.setCancelable(false);
        }
        pd.setMessage("" + getResources().getString(string));
        if (!pd.isShowing()) {
            pd.show();
        }
    }

    public void dismissLoading() {
        try {
            if (pd != null && pd.isShowing()) {
                pd.dismiss();
            }
        } catch (Exception e) {
        }
    }

    protected void connectRTC(){}


    protected void reconnectRTC(){}


    public boolean isLoginRTC(){

        return XHClient.getInstance().getIsOnline();
    }
}

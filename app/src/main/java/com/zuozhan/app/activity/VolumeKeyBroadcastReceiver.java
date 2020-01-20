package com.zuozhan.app.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

import com.starrtc.demo.utils.AEvent;
import com.zuozhan.app.util.LogUtil;

public  class VolumeKeyBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        LogUtil.d("action = "+action);
        if (action.equals(Intent.ACTION_MEDIA_BUTTON)) {
            KeyEvent ke = (KeyEvent)intent.getExtras().get(Intent.EXTRA_KEY_EVENT);
            LogUtil.d(ke.getRepeatCount()+"-"+ke.getAction()+"-"+ke.getKeyCode());

            // 获得按键码
            int keycode = ke.getKeyCode();
            switch (keycode) {
                case KeyEvent.KEYCODE_MEDIA_NEXT:
                    break;
                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                    break;
                case KeyEvent.KEYCODE_HEADSETHOOK:
                    if (ke.getAction() == KeyEvent.ACTION_DOWN){
                        AEvent.notifyListener(AEvent.AEVENT_PTT,true,"");
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
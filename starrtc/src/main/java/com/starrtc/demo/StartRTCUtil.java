package com.starrtc.demo;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;

import com.starrtc.demo.demo.MLOC;
import com.starrtc.demo.demo.service.FloatWindowsService;
import com.starrtc.demo.demo.videolive.VideoLiveActivity;
import com.starrtc.demo.utils.AEvent;
import com.starrtc.starrtcsdk.api.XHClient;
import com.starrtc.starrtcsdk.api.XHConstants;

public class StartRTCUtil {

    public static void startLiveActivity(Activity baseActivity, String mTargetId) {
        Intent intent = new Intent(baseActivity, VideoLiveActivity.class);
        intent.putExtra(VideoLiveActivity.LIVE_TYPE, XHConstants.XHLiveType.XHLiveTypeGlobalPublic);
        intent.putExtra(VideoLiveActivity.LIVE_NAME,mTargetId);
        intent.putExtra(VideoLiveActivity.CREATER_ID, MLOC.userId);
        baseActivity.startActivity(intent);
    }
    public static void startAudioLiveActivity(Activity baseActivity, String mTargetId) {
        startLiveActivity(baseActivity,mTargetId);
//        Intent intent = new Intent(baseActivity, AudioLiveActivity.class);
//        intent.putExtra(VideoLiveActivity.LIVE_TYPE,  XHConstants.XHLiveType.XHLiveTypeGlobalPublic);
//        intent.putExtra(VideoLiveActivity.LIVE_NAME,mTargetId);
//        intent.putExtra(VideoLiveActivity.CREATER_ID, MLOC.userId);
//        baseActivity.startActivity(intent);
    }


    public static void loginOut(FragmentActivity activity) {
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    XHClient.getInstance().getLoginManager().logout();
                }
            }).start();
            AEvent.notifyListener(AEvent.AEVENT_LOGOUT, true, null);
            activity.stopService(new Intent(activity, FloatWindowsService.class));
            MLOC.hasLogout = true;
        }catch (Throwable e){}

    }
}

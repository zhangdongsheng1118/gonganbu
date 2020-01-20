package com.zuozhan.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.starrtc.demo.StartRTCUtil;
import com.starrtc.demo.demo.MLOC;
import com.starrtc.demo.demo.voip.VoipActivity;
import com.starrtc.demo.demo.voip.VoipAudioActivity;
import com.zuozhan.app.activity.WebDataActivity;
import com.zuozhan.app.activity.ZHLoginActivity;
import com.zuozhan.app.activity.ZHMainActivity;
import com.zuozhan.app.activity.ZHVideoInfoActivity;
import com.zuozhan.app.activity.ZHVideoInfoActivity2;
import com.zuozhan.app.bean.RenWuLeiBean;
import com.zuozhan.app.util.ShareprefrensUtils;

import java.io.Serializable;

public class RouterUtil {

    public static void goHome(Activity baseActivity) {
        Intent i = new Intent(baseActivity, ZHMainActivity.class);
        baseActivity.startActivity(i);
    }
    public static void goHome(Activity baseActivity,int type) {
        Intent i = new Intent(baseActivity, ZHMainActivity.class);
        i.putExtra("type",type);
        baseActivity.startActivity(i);
    }
    public static void goHome(Activity baseActivity, int type, RenWuLeiBean.DataBean dataBean) {
        Intent i = new Intent(baseActivity, ZHMainActivity.class);
        i.putExtra("type",type);
        i.putExtra("isShowEx",dataBean);
        baseActivity.startActivity(i);
    }
    public static void goHome(Activity baseActivity, int type, RenWuLeiBean.DataBean dataBean,String topicTmp) {
        Intent i = new Intent(baseActivity, ZHMainActivity.class);
        i.putExtra("type",type);
        i.putExtra("isShowEx",dataBean);
        i.putExtra("topicTmp",topicTmp);
        baseActivity.startActivity(i);
    }
    public static void goLogin(Activity baseActivity) {
        Intent i = new Intent(baseActivity, ZHLoginActivity.class);
        baseActivity.startActivity(i);
    }

    public static void goActivity(Activity baseActivity,Class c) {
        Intent i = new Intent(baseActivity, c);
        baseActivity.startActivity(i);
    }
    public static void goActivity(Activity baseActivity, Class c, Serializable serializable) {
        Intent i = new Intent(baseActivity, c);
        i.putExtra("info",serializable);
        baseActivity.startActivity(i);
    }
    public static void goActivity(Activity baseActivity, Class c, Serializable serializable,String other) {
        Intent i = new Intent(baseActivity, c);
        i.putExtra("info",serializable);
        i.putExtra("other",other);
        baseActivity.startActivity(i);
    }
    public static void goActivity(Activity baseActivity, Class c, String info) {
        Intent i = new Intent(baseActivity, c);
        i.putExtra("info",info);
        baseActivity.startActivity(i);
    }


    public static void goActivity(Fragment baseActivity, Class c) {
        Intent i = new Intent(baseActivity.getActivity(), c);
        baseActivity.startActivity(i);
    }
    public static void goWebView(Activity baseActivity,String title,String data) {
        Intent i = new Intent(baseActivity, WebDataActivity.class);
        i.putExtra("title",title);
        i.putExtra("data",data);
        baseActivity.startActivity(i);
    }
    public static void goVideoPlay(Activity baseActivity,String title,String data,String image) {
        Intent i = new Intent(baseActivity, ZHVideoInfoActivity.class);
        i.putExtra("title",title);
        i.putExtra("image",image);
        i.putExtra("data",data);
        baseActivity.startActivity(i);
    }
    public static final int AUDIO_CONSTANS = 333;


    //视频通话
    public static void goVideoActivity(Activity baseActivity,String mTargetId) {
        MLOC.saveVoipUserId(baseActivity,mTargetId);
        Intent intent = new Intent(baseActivity, VoipActivity.class);
        intent.putExtra("targetId",mTargetId);
        Log.i("aaaa", "goVideoActivity: "+mTargetId);
        intent.putExtra(VoipActivity.ACTION,VoipActivity.CALLING);
        baseActivity.startActivity(intent);
    }


    public static void goAudioActivity(Activity baseActivity,String mTargetId) {
        MLOC.saveVoipUserId(baseActivity,mTargetId);
        Intent intent = new Intent(baseActivity, VoipAudioActivity.class);
        intent.putExtra("targetId",mTargetId);
        intent.putExtra(VoipAudioActivity.ACTION,VoipAudioActivity.CALLING);
        baseActivity.startActivity(intent);
    }

    public static void goLiveActivity(Activity baseActivity,String mTargetId) {
        StartRTCUtil.startLiveActivity(baseActivity,mTargetId);
    }
    public static void goLiveActivityAudio(Activity baseActivity,String mTargetId) {
        StartRTCUtil.startAudioLiveActivity(baseActivity,mTargetId);
    }

    public static void goLoginToClear(Activity baseActivity) {
        ShareprefrensUtils.setSharePreferencesBoolean(ShareprefrensUtils.ISLOGIN, false);
        ShareprefrensUtils.setSharePreferences(ShareprefrensUtils.TOKEN, "");
        ShareprefrensUtils.setSharePreferences(ShareprefrensUtils.USERINFO, "");
        Intent i = new Intent(baseActivity, ZHLoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        baseActivity.startActivity(i);
    }
    public static void goLoginToClear(Context baseActivity) {
        ShareprefrensUtils.setSharePreferencesBoolean(ShareprefrensUtils.ISLOGIN, false);
        ShareprefrensUtils.setSharePreferences(ShareprefrensUtils.TOKEN, "");
        ShareprefrensUtils.setSharePreferences(ShareprefrensUtils.USERINFO, "");
        Intent i = new Intent(baseActivity, ZHLoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        baseActivity.startActivity(i);
    }
    public static void goLoginToClear(Fragment baseActivity) {
        ShareprefrensUtils.setSharePreferencesBoolean(ShareprefrensUtils.ISLOGIN, false);
        ShareprefrensUtils.setSharePreferences(ShareprefrensUtils.TOKEN, "");
        ShareprefrensUtils.setSharePreferences(ShareprefrensUtils.USERINFO, "");
        Intent i = new Intent(baseActivity.getActivity(), ZHLoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        baseActivity.startActivity(i);
        baseActivity.getActivity().finish();
    }
}

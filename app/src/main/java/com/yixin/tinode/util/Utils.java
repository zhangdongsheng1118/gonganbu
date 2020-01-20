package com.yixin.tinode.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.v7.preference.PreferenceManager;

import com.blankj.utilcode.util.ToastUtils;
import com.yixin.tinode.R;
import com.yixin.tinode.tinode.Cache;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import co.tinode.tinodesdk.Tinode;

import static android.content.Context.VIBRATOR_SERVICE;

/**
 * Created by Administrator on 2018/7/3 0003.
 */

public class Utils {

    private static SoundPool mSoundPool;

    public static void soundNewMsg(Context context) {
        if (mSoundPool == null) {
            mSoundPool = new SoundPool(1, AudioManager.STREAM_ALARM, 0);
        }

        mSoundPool.play(R.raw.tip, 1, 1, 0, 0, 1);
    }

    public static void VibrateNewMsg(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(1000);
    }

    /**
     * 检测网络连接
     *
     * @param context
     * @return
     */
    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    public static void showNetworkAlert(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("提示")
                .setMessage("当前无网络连接，请检查网络连接后重试")
                .setPositiveButton("设置", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        context.startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                    }
                }).show();
    }

    public static interface ReconnectListener {
        public void after();
    }

    public static void reconnect(Context context, Tinode tinode, ReconnectListener listener) {
        try {
            if (!tinode.reconnectNow()) {
                final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
                String hostName = sharedPref.getString(com.yixin.tinode.tinode.account.Utils.PREFS_HOST_NAME, Cache.HOST_NAME);
                boolean tls = sharedPref.getBoolean(com.yixin.tinode.tinode.account.Utils.PREFS_USE_TLS, false);
                tinode.connect(hostName, tls).getResult();
                ToastUtils.showShort("后台自动重连2");
            } else {
                ToastUtils.showShort("后台自动重连1");
            }

            if (listener != null) {
                listener.after();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public final static AntiShake antiShakeUtil = new AntiShake();
    public static class AntiShake {
        private List<OneClickUtil> utils = new ArrayList<>();

        public boolean check(Object o) {
            String flag = null;
            if(o == null)
                flag = Thread.currentThread().getStackTrace()[2].getMethodName();
            else
                flag = o.toString();
            for (OneClickUtil util : utils) {
                if (util.getMethodName().equals(flag)) {
                    return util.check();
                }
            }
            OneClickUtil clickUtil = new OneClickUtil(flag);
            utils.add(clickUtil);
            return clickUtil.check();
        }

        public boolean check() {
            return check(null);
        }

        public class OneClickUtil {
            private String methodName;
            public static final int MIN_CLICK_DELAY_TIME = 1000;
            private long lastClickTime = 0;

            public OneClickUtil(String methodName) {
                this.methodName = methodName;
            }

            public String getMethodName() {
                return methodName;
            }

            public boolean check() {
                long currentTime = Calendar.getInstance().getTimeInMillis();
                if (currentTime - lastClickTime > MIN_CLICK_DELAY_TIME) {
                    lastClickTime = currentTime;
                    return false;
                } else {
                    return true;
                }
            }
        }
    }
}

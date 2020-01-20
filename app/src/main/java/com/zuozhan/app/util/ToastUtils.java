package com.zuozhan.app.util;

import android.text.TextUtils;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.starrtc.demo.HookToast;
import com.zuozhan.app.AppEnvirment;
import com.zuozhan.app.BaseIP;

public class ToastUtils {

    /** 之前显示的内容 */
    private static String oldMsg;
    /** Toast对象 */
    private static Toast toast = null;
    /** 第一次时间 */
    private static long oneTime = 0;
    /** 第二次时间 */
    private static long twoTime = 0;

    public static void showDebugToast(String message){
        if (BaseIP.isDebug){
            ToastUtils.showToast(message);
        }
    }

    /**
     * 显示Toast
     *
     * @param message
     */
    public static void showToast(String message) {
        try {
            if (TextUtils.isEmpty(message)) {
                return;
            }
            if (toast == null) {
                toast = Toast.makeText(AppEnvirment.getApplication(), "", Toast.LENGTH_SHORT);
                LinearLayout v = (LinearLayout) toast.getView();
                TextView tv = (TextView)v.getChildAt(0);
                tv.setTextSize(16);

                toast.setGravity(Gravity.CENTER,0,0);
                toast.setText(message);
                HookToast.hook(toast);
                toast.show();
                oneTime = System.currentTimeMillis();
                oldMsg = message;
            } else {
                twoTime = System.currentTimeMillis();
                if (message.equals(oldMsg)) {
                    if (twoTime - oneTime > 0) {
                        HookToast.hook(toast);
                        toast.show();
                    }
                } else {
                    oldMsg = message;
                    toast.setText(message);
                    HookToast.hook(toast);
                    toast.show();
                }
            }
            oneTime = twoTime;
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void showToast(int id) {
        String message = AppEnvirment.getApplication().getResources().getString(id);
        showToast(message);
    }
}

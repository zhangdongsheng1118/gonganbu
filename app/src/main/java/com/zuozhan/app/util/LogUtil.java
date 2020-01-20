package com.zuozhan.app.util;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

/**
 * Created by dq on 2018/3/21.
 */
public class LogUtil {

    public static final String TAG = "LogUtil";
    public static boolean DEBUG_MODE = true;

    public static void setDebugMode(boolean var0) {
        DEBUG_MODE = var0;
    }

    public static boolean isDebugMode() {
        return DEBUG_MODE;
    }

    public static void d(Object var0, String var1) {
        if (DEBUG_MODE && !TextUtils.isEmpty(var1)) {
            Log.d(TAG+"_"+var0.getClass().getName(), var1);
        }

    }

    public static void d(String var1) {
        if (DEBUG_MODE && !TextUtils.isEmpty(var1)) {
            Log.d(TAG+"_", var1);
        }

    }

    public static void e(Object var0, String var1) {
        if (DEBUG_MODE && !TextUtils.isEmpty(var1)) {
            Log.e(TAG+"_"+var0.getClass().getName(), var1);
        }

    }

    private static Process mLogProcess;
    public static void startDebug(){
        try {
            int pid = android.os.Process.myPid();
            LogUtil.d("MyApp", "PID is " + pid);
            Calendar calendar = Calendar.getInstance();
            File file=new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/fxxt/log/");
            if (!file.exists()){
                file.mkdirs();
            }
            String filename = String.format(file.getAbsolutePath()+"/MyApp_Log_%04d%02d%02d_%02d%02d%02d.txt",
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH),
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    calendar.get(Calendar.SECOND));
            mLogProcess = Runtime.getRuntime().exec(String.format("logcat -v time -f %s", filename));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void stopDebug(){
        if(mLogProcess != null ){
            mLogProcess.destroy();
        }
    }
}

package com.zuozhan.app.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.zuozhan.app.AppEnvirment;


/**
 *
 */
public class ShareprefrensUtils {

    private static String SHARE_KEY = "share_key";
    public static String ISLOGIN = "is_login";
    public static String TOKEN = "token";
    public static String USERINFO = "user_info";

    public static void setSharePreferences(String key, String value) {
        SharedPreferences shePreferences =
                AppEnvirment.getApplication().getSharedPreferences(SHARE_KEY, Context.MODE_PRIVATE);
        shePreferences.edit().putString(key, value).commit();
    }


    public static String getSharePreferences(String key, String defaultstr) {
        SharedPreferences sharedPreferences = AppEnvirment.getApplication().getSharedPreferences(SHARE_KEY, Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, defaultstr);
    }

    public static void setSharePreferencesInt(String key, int value) {
        SharedPreferences shePreferences = AppEnvirment.getApplication().getSharedPreferences(SHARE_KEY, Context.MODE_PRIVATE);
        shePreferences.edit().putInt(key, value).commit();
    }

    public static int getSharePreferencesInt(String key, int defaultstr) {
        SharedPreferences sharedPreferences = AppEnvirment.getApplication().getSharedPreferences(SHARE_KEY, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(key, defaultstr);
    }


    public static void setSharePreferencesBoolean(String key, boolean value) {
        SharedPreferences shePreferences = AppEnvirment.getApplication().getSharedPreferences(SHARE_KEY, Context.MODE_PRIVATE);
        shePreferences.edit().putBoolean(key, value).commit();
    }

    public static boolean getSharePreferencesBoolean(String key, boolean defaultstr) {
        SharedPreferences sharedPreferences = AppEnvirment.getApplication().getSharedPreferences(SHARE_KEY, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(key, defaultstr);
    }


    public static void setSharePreferencesLong(String key, long value) {
        SharedPreferences shePreferences = AppEnvirment.getApplication().getSharedPreferences(SHARE_KEY, Context.MODE_PRIVATE);
        shePreferences.edit().putLong(key, value).commit();
    }

    public static long getSharePreferencesLong(String key, long defaultstr) {
        SharedPreferences sharedPreferences = AppEnvirment.getApplication().getSharedPreferences(SHARE_KEY, Context.MODE_PRIVATE);
        return sharedPreferences.getLong(key, defaultstr);
    }

    /**
     * 获取不同名称xml的数据
     * @param key
     * @param defaultstr
     * @return
     */
    public static String getSharePreferences1(String key, String defaultstr) {
        SharedPreferences sharedPreferences = AppEnvirment.getApplication().getSharedPreferences(SHARE_KEY, Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, defaultstr);
    }

    public static void setSharePreferences1(String key, String value) {
        SharedPreferences shePreferences =
                AppEnvirment.getApplication().getSharedPreferences(SHARE_KEY, Context.MODE_PRIVATE);
        shePreferences.edit().putString(key, value).commit();
    }
}

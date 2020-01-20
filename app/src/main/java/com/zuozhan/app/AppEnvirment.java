package com.zuozhan.app;

import android.content.Context;
import android.text.TextUtils;

import com.yixin.tinode.app.MyApp;
import com.zuozhan.app.bean.UserBean;
import com.zuozhan.app.util.ShareprefrensUtils;


/**
 * app全局变量
 */
public class AppEnvirment {


    public static String token = "";
    public static UserBean userBean;
    public static Context getApplication() {
        return MyApp.getInstance().getApplicationContext();
    }

    public static boolean isLogin() {
        return ShareprefrensUtils.getSharePreferencesBoolean(ShareprefrensUtils.ISLOGIN,false);
    }

    public static String getToken(){
        if (TextUtils.isEmpty(token)){
            token = ShareprefrensUtils.getSharePreferences(ShareprefrensUtils.TOKEN,"");
        }
        return token;
    }

    public static UserBean getUserBean(){
        return userBean;
    }

    public static void LoginOut(){
        token = "";
    }
}

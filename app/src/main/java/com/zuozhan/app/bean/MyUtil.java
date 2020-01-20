package com.zuozhan.app.bean;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;

public class MyUtil {


    public static boolean copy(Context context,String copyStr) {
        try {
            //获取剪贴板管理器
            ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            // 创建普通字符型ClipData
            ClipData mClipData = ClipData.newPlainText("Label", copyStr);
            // 将ClipData内容放到系统剪贴板里。
            cm.setPrimaryClip(mClipData);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static void setText(TextView textView, String text) {
        if (TextUtils.isEmpty(text)) {
            textView.setText("");
        } else {
            textView.setText(text);
        }
    }

    public static void setText(TextView textView, String text,String def) {
        if (TextUtils.isEmpty(text)) {
            textView.setText(def+"");
        } else {
            textView.setText(def+text);
        }
    }

    public static void setSpanText(TextView textView, String text, String tag, int color) {
        if (TextUtils.isEmpty(text)) {
            setText(textView, text);
        } else {
            try {
                if (TextUtils.isEmpty(tag)) {
                    setText(textView, text);
                    return;
                }
                int start = text.indexOf(tag);
                if (start == -1) {
                    setText(textView, text);
                    return;
                }
                int end = start + tag.length();
                SpannableString spanstr = new SpannableString(text);
                spanstr.setSpan(
                        new ForegroundColorSpan(color),
                        start,
                        end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                textView.setText(spanstr);
            } catch (Exception e) {
                setText(textView, text);
            }
        }
    }

    public static int getVersionCode(Context context) {

        //获取包管理器
        PackageManager pm = context.getPackageManager();
        //获取包信息
        try {
            PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), 0);
            //返回版本号
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return 0;

    }

}

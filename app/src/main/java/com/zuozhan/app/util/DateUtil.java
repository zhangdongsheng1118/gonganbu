package com.zuozhan.app.util;

import android.text.TextUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {

    public static String timeStampDay(String time) {
        if (time == null || TextUtils.isEmpty(time)) {
            return "";
        }
        String format = "yyyy/MM/dd HH:mm:ss";
        if (!time.contains(":")) {
            time = time + " 00:00:00";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        try {
            Date date = sdf.parse(time);
            String day = sdf.format(date);
            LogUtil.d("当前日期 = " + day);
            return day;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String timeStampDay(long time) {
        String format = "yyyy/MM/dd HH:mm:ss";
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        try {
            String date = sdf.format(new Date(time));
            LogUtil.d("日期 = " + date);
            return date;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String timeStamp3Date(String seconds) {
        if (seconds == null || seconds.isEmpty() || seconds.equals("null")) {
            return "";
        }
        String format = "yyyy年MM月dd日 HH:mm:ss";
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date(Long.valueOf(seconds)));
    }

    public static String timeStampDate(String seconds) {
        if (seconds == null || seconds.isEmpty() || seconds.equals("null")) {
            return "";
        }
        String format = "MM-dd HH:mm";
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date(Long.valueOf(seconds)));
    }
}

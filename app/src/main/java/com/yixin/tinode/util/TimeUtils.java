package com.yixin.tinode.util;

import android.support.annotation.NonNull;

import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.DateTimeConstants;

/**
 * @创建者 CSDN_LQR
 * @描述 时间工具（需要joda-time）
 */
public class TimeUtils {

    /**
     * 得到仿一信日期格式输出
     *
     * @param msgTimeMillis
     * @return
     */
    public static String getMsgFormatTime(long msgTimeMillis, boolean timeVisible) {
        DateTime nowTime = DateTime.now();
        DateTime msgTime = new DateTime(msgTimeMillis);
        LocalDateTime msgLocalTime = msgTime.toLocalDateTime();
        LocalDateTime dayBaseTime = nowTime.toLocalDateTime().withTime(0,0,0,0);
        LocalDateTime yesterdayBaseTime = dayBaseTime.plusDays(-1);
        LocalDateTime weekBaseTime = dayBaseTime.withDayOfWeek(1);

        String time = "";
        if(timeVisible) time = " " + msgTime.toString("HH:mm");
        if (msgLocalTime.compareTo(dayBaseTime) >= 0) {
            return msgTime.toString("HH:mm");
        } else if(msgLocalTime.compareTo(yesterdayBaseTime) >= 0) {
            return "昨天" + time;
        } else if(msgLocalTime.compareTo(weekBaseTime) >= 0) {
            //星期
            switch (msgTime.getDayOfWeek()) {
                case DateTimeConstants.SUNDAY:
                    return "周日" + time;
                case DateTimeConstants.MONDAY:
                    return "周一" + time;
                case DateTimeConstants.TUESDAY:
                    return "周二" + time;
                case DateTimeConstants.WEDNESDAY:
                    return "周三" + time;
                case DateTimeConstants.THURSDAY:
                    return "周四" + time;
                case DateTimeConstants.FRIDAY:
                    return "周五" + time;
                case DateTimeConstants.SATURDAY:
                    return "周六" + time;
            }
            return time;
        } else {
            //12月22日
            return msgTime.toString("MM月dd日") + time;
        }
    }
}

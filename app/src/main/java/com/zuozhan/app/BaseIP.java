package com.zuozhan.app;

import com.starrtc.demo.demo.MLOC;
import com.zuozhan.app.util.ShareprefrensUtils;

import java.util.Map;

public class BaseIP {
    //114.251.158.54 新
    //39.98.37.28 老

//    public static String map_ip = "47.111.133.91:3000";
//    public static String base_ip = "39.98.37.28:8085";
//    public static String rtc_ip = "command.yiqizhongbao.com";
//    public static String rtc_push_ip = "47.111.141.221:10085";
//    public static String IM_ip = "47.94.235.90:6060";
//    public static String IM_ip2 = "47.94.235.90:8090";

    //新
    public static String map_ip = "20.60.0.62:7780/#";
    public static String base_ip = "20.60.0.62:8085";
    public static String rtc_ip = "20.60.0.62";
    public static String rtc_push_ip = "20.60.0.62:10085";
    public static String IM_ip = "20.60.0.62:6060";
    public static String IM_ip2 = "20.60.0.62:8090";

//    public static String map_ip = "123.124.207.165:7780/#";
//    public static String base_ip = "123.124.207.165:8085";
//    public static String rtc_ip = "123.124.207.165";
//    public static String rtc_push_ip = "127.0.0.1:10085";
//    public static String IM_ip = "123.124.207.165:6060";
//    public static String IM_ip2 = "123.124.207.165:8090";


    public static String GUDING_PTT = "Wz@NWuVjGiiga9oBaKa0Q8ONwWyGc7mY";
    public static String GUDING_MEET = "Wz@NWuVjGivxa9saa4akpNIPgWyGkaC9";

    public static boolean isHttps = false;
    public static boolean quchu_im = false;
    public static int location_time = 1;
    public static int location_upload_time = 5;
    public static int camera = 0;
    public static boolean isShowMap = true;



    public static  String IP_MAP = "http://"+ShareprefrensUtils.getSharePreferences1("ip1","")+"/map?caseId=";
    public static  String NET_BASE_URL = "http://"+ShareprefrensUtils.getSharePreferences1("ip2","")+"/";
    public static  String RTC_IP = MLOC.IP;
    public static  String RTC_PUSH = MLOC.PUSH;
    public static  String IM_IP = ShareprefrensUtils.getSharePreferences1("ip5","");
    public static  String IM_IP_2 = "http://"+ShareprefrensUtils.getSharePreferences1("ip6","");

   /* public static  String IP_MAP = "http://"+map_ip+"/map?caseId=";
    public static  String NET_BASE_URL = "http://"+base_ip+"/";
    public static  String RTC_IP = MLOC.IP;
    public static  String RTC_PUSH = MLOC.PUSH;
    public static  String IM_IP = IM_ip;
    public static  String IM_IP_2 = "http://"+IM_ip2;*/

    public static final boolean isDebug = false;


    //114.255.88.226
//    public static final String IP_MAP = "http://114.255.88.226:3000/map?caseId=";
//    public static final String NET_BASE_URL = "http://114.255.88.226:8085/";
//    public static final String RTC_IP = MLOC.IP;
//    public static final String RTC_PUSH = MLOC.PUSH;
//    public static final String IM_IP = "114.255.88.226:6060";
//    public static final String IM_IP_2 = "http://114.255.88.226:8090";

}

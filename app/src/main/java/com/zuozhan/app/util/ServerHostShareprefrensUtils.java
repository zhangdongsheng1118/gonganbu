package com.zuozhan.app.util;

import android.content.Context;

import com.starrtc.demo.demo.MLOC;
import com.zuozhan.app.BaseIP;

import java.io.BufferedWriter;
import java.io.FileOutputStream;

/**
 *
 */
public class ServerHostShareprefrensUtils {

    public static String map_ip = "map_ip";
    public static String base_ip = "base_ip";
    public static String rtc_ip = "rtc_ip";
    public static String rtc_push_ip = "rtc_push_ip";
    public static String IM_ip = "IM_ip";
    public static String IM_ip2 = "IM_ip2";
    public static String GUDING_PTT = "GUDING_PTT";
    public static String GUDING_MEET = "GUDING_MEET";

    public static String isHttps = "isHttps";
    public static String quchu_im = "quchu_im";
    public static String location_time = "location_time";
    public static String location_upload_time = "location_upload_time";
    public static String camera = "camera";
    public static String isShowMap = "isShowMap";

    public static void initSP() {
        BaseIP.map_ip = ShareprefrensUtils.getSharePreferences(map_ip, BaseIP.map_ip);
        BaseIP.base_ip = ShareprefrensUtils.getSharePreferences(base_ip, BaseIP.base_ip);
        BaseIP.rtc_ip = ShareprefrensUtils.getSharePreferences(rtc_ip, BaseIP.rtc_ip);
        BaseIP.rtc_push_ip = ShareprefrensUtils.getSharePreferences(rtc_push_ip, BaseIP.rtc_push_ip);
        BaseIP.IM_ip = ShareprefrensUtils.getSharePreferences(IM_ip, BaseIP.IM_ip);
        BaseIP.IM_ip2 = ShareprefrensUtils.getSharePreferences(IM_ip2, BaseIP.IM_ip2);
        BaseIP.GUDING_PTT = ShareprefrensUtils.getSharePreferences(GUDING_PTT, BaseIP.GUDING_PTT);
        BaseIP.GUDING_MEET = ShareprefrensUtils.getSharePreferences(GUDING_MEET, BaseIP.GUDING_MEET);
        BaseIP.isHttps = ShareprefrensUtils.getSharePreferencesBoolean(isHttps, BaseIP.isHttps);
        BaseIP.quchu_im = ShareprefrensUtils.getSharePreferencesBoolean(quchu_im, BaseIP.quchu_im);
        BaseIP.location_time = ShareprefrensUtils.getSharePreferencesInt(location_time, BaseIP.location_time);
        BaseIP.location_upload_time = ShareprefrensUtils.getSharePreferencesInt(location_upload_time, BaseIP.location_upload_time);
        BaseIP.camera = ShareprefrensUtils.getSharePreferencesInt(camera, BaseIP.camera);
        BaseIP.isShowMap = ShareprefrensUtils.getSharePreferencesBoolean(isShowMap, BaseIP.isShowMap);

        BaseIP.IP_MAP = "http://" + BaseIP.map_ip + "/map?caseId=";

        if (BaseIP.isHttps) {
            BaseIP.NET_BASE_URL = "https://" + BaseIP.base_ip + "/";
        } else {
            BaseIP.NET_BASE_URL = "http://" + BaseIP.base_ip + "/";
        }

        MLOC.IP = BaseIP.rtc_ip;
        MLOC.PUSH = "rtmp://" + BaseIP.rtc_push_ip + "/hls/";

        BaseIP.IM_IP = BaseIP.IM_ip;
        BaseIP.IM_IP_2 = "http://" + BaseIP.IM_ip2;

        String IP = MLOC.IP;
        MLOC.VOIP_SERVER_URL          = IP+":10086";
        MLOC.IM_SERVER_URL            = IP+":19903";
        MLOC.CHATROOM_SERVER_URL      = IP+":19906";
        MLOC.LIVE_VDN_SERVER_URL      = IP+":19928";
        MLOC.LIVE_SRC_SERVER_URL      = IP+":19931";
        MLOC.LIVE_PROXY_SERVER_URL    = IP+":19932";
    }

    public static void save() {
        ShareprefrensUtils.setSharePreferences(map_ip, BaseIP.map_ip);
        ShareprefrensUtils.setSharePreferences(base_ip, BaseIP.base_ip);
        ShareprefrensUtils.setSharePreferences(rtc_ip, BaseIP.rtc_ip);
        ShareprefrensUtils.setSharePreferences(rtc_push_ip, BaseIP.rtc_push_ip);
        ShareprefrensUtils.setSharePreferences(IM_ip, BaseIP.IM_ip);
        ShareprefrensUtils.setSharePreferences(IM_ip2, BaseIP.IM_ip2);
        ShareprefrensUtils.setSharePreferences(GUDING_PTT, BaseIP.GUDING_PTT);
        ShareprefrensUtils.setSharePreferences(GUDING_MEET, BaseIP.GUDING_MEET);
        ShareprefrensUtils.setSharePreferencesBoolean(isHttps, BaseIP.isHttps);
        ShareprefrensUtils.setSharePreferencesBoolean(quchu_im, BaseIP.quchu_im);
        ShareprefrensUtils.setSharePreferencesInt(location_time, BaseIP.location_time);
        ShareprefrensUtils.setSharePreferencesInt(location_upload_time, BaseIP.location_upload_time);
        ShareprefrensUtils.setSharePreferencesInt(camera, BaseIP.camera);
        ShareprefrensUtils.setSharePreferencesBoolean(isShowMap, BaseIP.isShowMap);
    }

}

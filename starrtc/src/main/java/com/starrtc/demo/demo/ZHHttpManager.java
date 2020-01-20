package com.starrtc.demo.demo;


public class ZHHttpManager {

    static ZHHttpManager zhHttpManager=new ZHHttpManager();


    public static ZHHttpManager getZhHttpManager() {
        return zhHttpManager;
    }

    public RTCHttpService getRtcHttpService() {
        return rtcHttpService;
    }

    public void setRtcHttpService(RTCHttpService rtcHttpService) {
        this.rtcHttpService = rtcHttpService;
    }

    public void getHeadPicById(String id,VoipHttpListener voipHttpListener){
        if (rtcHttpService!=null){
            rtcHttpService.getHeadPicById(id,voipHttpListener);
        }

    }


    RTCHttpService rtcHttpService;


}

package com.yixin.tinode.api.param;

import com.fasterxml.jackson.annotation.JsonIgnore;

import static com.yixin.tinode.app.AppConst.SEND_FILE_TYPE_SEAWEED;

/**
 * Created by Administrator on 2018/7/31 0031.
 */

public class TiSetting extends BaseReq {
    private long userId;
    private String topic;
    private int notification;
    private int sound;
    private int vibrate;
    private String updateTime;
    //AV
    private String avServerAddr;
    private String avServerPort;
    private String avQuality;//低 中 高
    //文件发送方式
    private String sendFileType;

    @JsonIgnore
    public boolean isSendFileByTinode() {
        return !SEND_FILE_TYPE_SEAWEED.equals(sendFileType);
    }

    public String getSendFileType() {
        return sendFileType;
    }

    public void setSendFileType(String sendFileType) {
        this.sendFileType = sendFileType;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public int getNotification() {
        return notification;
    }

    public void setNotification(int notification) {
        this.notification = notification;
    }

    public int getSound() {
        return sound;
    }

    public void setSound(int sound) {
        this.sound = sound;
    }

    public int getVibrate() {
        return vibrate;
    }

    public void setVibrate(int vibrate) {
        this.vibrate = vibrate;
    }

    public String getAvServerAddr() {
        return avServerAddr;
    }

    public void setAvServerAddr(String avServerAddr) {
        this.avServerAddr = avServerAddr;
    }

    public String getAvServerPort() {
        return avServerPort;
    }

    public void setAvServerPort(String avServerPort) {
        this.avServerPort = avServerPort;
    }

    public String getAvQuality() {
        return avQuality;
    }

    public void setAvQuality(String avQuality) {
        this.avQuality = avQuality;
    }
}

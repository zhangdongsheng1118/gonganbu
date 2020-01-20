package com.yixin.tinode.api.param;

/**
 * Created by Administrator on 2018/7/31 0031.
 * 通用数据
 */

public class TiVersion extends BaseReq {
    private String time;
    private String topic;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }
}

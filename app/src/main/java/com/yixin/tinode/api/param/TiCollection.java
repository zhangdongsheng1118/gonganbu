package com.yixin.tinode.api.param;

/**
 * Created by Administrator on 2018/7/31 0031.
 * 通用数据
 */

public class TiCollection extends BaseReq {
    private long id;
    private long msgId;
    private String tag;
    private String createTime;
    private String topic;
    private int type;
    private String userName;
    private String path;
    private String des;
    private String fromName;

    private int page;
    private int rows;

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }


    public long getMsgId() {
        return msgId;
    }

    public void setMsgId(long msgId) {
        this.msgId = msgId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDes() {
        return des;
    }

    public void setDes(String des) {
        this.des = des;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "TiCollection{" +
                "id=" + id +
                ", tag='" + tag + '\'' +
                ", createTime='" + createTime + '\'' +
                ", topic='" + topic + '\'' +
                ", type=" + type +
                ", userName=" + userName +
                ", path='" + path + '\'' +
                ", des='" + des + '\'' +
                ", fromName='" + fromName + '\'' +
                ", page=" + page +
                ", rows=" + rows +
                '}';
    }
}

package com.zuozhan.app.bean;

import java.util.List;

public class VideoSourceBean extends BaseBean {

    public List<DataBean> data;

    public static class DataBean{

        public int id;
        public String devId;
        public String name;
        public String status;
        public String type;
        public String remarks1;
        public String remarks2;
        public String startTime;
        public String endTime;
        public String createTime;
        public String description;
        public String videoUrl;
        public String photoUrl;
    }
}

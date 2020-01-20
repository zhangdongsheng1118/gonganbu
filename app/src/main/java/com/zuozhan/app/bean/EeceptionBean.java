package com.zuozhan.app.bean;

import java.io.Serializable;
import java.util.List;

public class EeceptionBean extends BaseBean {

    public List<DataBean> data;


    public static class DataBean implements Serializable {
        public int id;
        public String content;
        public String name;
        public String createTime;
        public String readTime;
        public String type;
        public int status;
        public String videoUrl;
        public String picUrl;
        public int createUserId;
        public String createUserName;
        public String createDeviceNo;
        public String removeTime;
        public String remarks1;
        public String remarks2;
    }
}

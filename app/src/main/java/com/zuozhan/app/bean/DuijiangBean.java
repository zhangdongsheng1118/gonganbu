package com.zuozhan.app.bean;

public class DuijiangBean extends BaseBean {
    public DataBean data;

    public static class DataBean{
        public int id;
        public String intercomId;
        public int userId;
        public String name;
        public String status;
        public String type;
        public String remarks1;
        public String remarks2;
        public String createTime;
        public int missionId;
    }
}

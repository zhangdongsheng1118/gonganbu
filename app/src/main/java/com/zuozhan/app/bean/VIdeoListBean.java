package com.zuozhan.app.bean;

import java.io.Serializable;
import java.util.List;

public class VIdeoListBean extends BaseBean {

    public List<DataBean> data;

    public static class DataBean implements Serializable {
        public int id;
        public int createUserId;
        public String caseId;
        public String caseName;
        public String number;
        public String name;
        public String means;
        public String location;
        public String filingTime;
        public Object finishTime;
        public int responsibleUserId;
        public String description;
        public String responsibleUserName;
        public String executeUserName;
        public String executeUserId;
        public String createTime;
        public String startTime;
        public String endTime;
        public String type;
        public String status;
        public String remarks1;
        public String actionObject;
        public String remarks2;
        public List<TeamListBean> teamList;


        public static class TeamListBean {
            public int id;
            public int TeamListBean;
            public String name;
            public String createTime;
            public String remarks1;
            public String remarks2;
            public String description;
            public List<DeviceBean> deviceList;
            public List<UBean> userList;
            public List<CarBean> carList;

        }
    }
}

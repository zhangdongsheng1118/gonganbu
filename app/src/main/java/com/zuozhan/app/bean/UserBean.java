package com.zuozhan.app.bean;

public class UserBean extends BaseBean {

    public DataBean data;

    public static class DataBean{

        public int id;
        public String username;
        public String password;
        public String token;
        public String createTime;
        public String lastLoginTime;
        public String updateTime;
        public String type;
        public String status;
        public String departmentId;
        public String departmentName;
        public String employer;
        public String phone;
        public String duty;
        public String remarks1;
        public String remarks2;
        public String headPic;
        public String sex;
        public String suid;
        public String realName;
    }
}

package com.zuozhan.app.bean;

import java.util.List;

public class JiaoLiuBean extends BaseBean{

    /**
     * code : 1
     * statusMsg : success
     * data : [{"id":1,"name":"系统通知","createTime":"2019-06-26 11:27:25","status":null,"type":null,"sort":null,"remarks1":null,"remarks2":null},{"id":2,"name":"音频设备使用手册","createTime":"2019-06-26 11:28:05","status":null,"type":null,"sort":null,"remarks1":null,"remarks2":null},{"id":3,"name":"视频设备使用手册","createTime":"2019-06-26 11:28:09","status":null,"type":null,"sort":null,"remarks1":null,"remarks2":null},{"id":4,"name":"文章评论","createTime":"2019-06-26 11:29:03","status":null,"type":null,"sort":null,"remarks1":null,"remarks2":null}]
     * status : 200
     */

    private List<DataBean> data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getStatusMsg() {
        return statusMsg;
    }

    public void setStatusMsg(String statusMsg) {
        this.statusMsg = statusMsg;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public List<DataBean> getData() {
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * id : 1
         * name : 系统通知
         * createTime : 2019-06-26 11:27:25
         * status : null
         * type : null
         * sort : null
         * remarks1 : null
         * remarks2 : null
         */

        private int id;
        private String name;
        private String createTime;
        private Object status;
        private Object type;
        private Object sort;
        private Object remarks1;
        private Object remarks2;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCreateTime() {
            return createTime;
        }

        public void setCreateTime(String createTime) {
            this.createTime = createTime;
        }

        public Object getStatus() {
            return status;
        }

        public void setStatus(Object status) {
            this.status = status;
        }

        public Object getType() {
            return type;
        }

        public void setType(Object type) {
            this.type = type;
        }

        public Object getSort() {
            return sort;
        }

        public void setSort(Object sort) {
            this.sort = sort;
        }

        public Object getRemarks1() {
            return remarks1;
        }

        public void setRemarks1(Object remarks1) {
            this.remarks1 = remarks1;
        }

        public Object getRemarks2() {
            return remarks2;
        }

        public void setRemarks2(Object remarks2) {
            this.remarks2 = remarks2;
        }
    }
}

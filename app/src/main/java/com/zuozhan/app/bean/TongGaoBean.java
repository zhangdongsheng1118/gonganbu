package com.zuozhan.app.bean;

import java.util.List;

public class TongGaoBean extends BaseBean{

    /**
     * code : 1
     * statusMsg : success
     * data : [{"id":2,"content":"紧急通知2紧急通知2紧急通知2紧急通知2紧急通知2紧急通知2紧急通知2紧急通知2紧急通知2紧急通知2紧急通知2紧急通知2","createTime":"2019-06-24 12:29:53","readTime":null,"type":null,"status":null,"remarks1":null,"remarks2":null},{"id":1,"content":"紧急通知1紧急通知1紧急通知1紧急通知1紧急通知1紧急通知1紧急通知1紧急通知1紧急通知1","createTime":"2019-06-24 12:29:38","readTime":null,"type":null,"status":null,"remarks1":null,"remarks2":null}]
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
         * id : 2
         * content : 紧急通知2紧急通知2紧急通知2紧急通知2紧急通知2紧急通知2紧急通知2紧急通知2紧急通知2紧急通知2紧急通知2紧急通知2
         * createTime : 2019-06-24 12:29:53
         * readTime : null
         * type : null
         * status : null
         * remarks1 : null
         * remarks2 : null
         */

        private int id;
        private String content;
        private String createTime;
        private Object readTime;
        private Object type;
        private Object status;
        private Object remarks1;
        private Object remarks2;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getCreateTime() {
            return createTime;
        }

        public void setCreateTime(String createTime) {
            this.createTime = createTime;
        }

        public Object getReadTime() {
            return readTime;
        }

        public void setReadTime(Object readTime) {
            this.readTime = readTime;
        }

        public Object getType() {
            return type;
        }

        public void setType(Object type) {
            this.type = type;
        }

        public Object getStatus() {
            return status;
        }

        public void setStatus(Object status) {
            this.status = status;
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

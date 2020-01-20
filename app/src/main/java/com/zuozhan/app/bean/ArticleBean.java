package com.zuozhan.app.bean;

import java.util.List;

public class ArticleBean extends BaseBean{

    /**
     * code : 1
     * statusMsg : success
     * data : [{"id":1,"name":"系统通知","createTime":"2019-06-26 11:27:25","status":null,"type":null,"sort":null,"remarks1":null,"remarks2":null},{"id":2,"name":"音频设备使用手册","createTime":"2019-06-26 11:28:05","status":null,"type":null,"sort":null,"remarks1":null,"remarks2":null},{"id":3,"name":"视频设备使用手册","createTime":"2019-06-26 11:28:09","status":null,"type":null,"sort":null,"remarks1":null,"remarks2":null},{"id":4,"name":"文章评论","createTime":"2019-06-26 11:29:03","status":null,"type":null,"sort":null,"remarks1":null,"remarks2":null}]
     * status : 200
     */

    public List<DataBean> data;


    public static class DataBean {

        public int id;
        public String title;
        public int type;
        public String status;
        public String coverPicture;
        public String createTime;
        public String sort;
        public String updateTime;
        public String likeCount;
        public String commentCount;
        public String remarks1;
        public String remarks2;
        public String article;
        public String summary;
        public String name;

           
    }
}

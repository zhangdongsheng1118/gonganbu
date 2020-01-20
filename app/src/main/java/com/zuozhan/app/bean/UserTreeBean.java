package com.zuozhan.app.bean;

import java.util.List;

public class UserTreeBean extends BaseBean {
    
    public List<ChildNode> data;

    public static class ChildNode {
        public int id;
        public String name;
        public int parentId;
        public String createTime;
        public String updateTime;
        public int count;
        public List<ChildNode> childNode;
    }

}


package com.zuozhan.app.bean;

public class VersionBean extends BaseBean{

    public Data data;
    public static class  Data{
        public int id ;
        public int versionNo;
        public String version;
        public String type;
        public String status;
        public String url;
    }
}

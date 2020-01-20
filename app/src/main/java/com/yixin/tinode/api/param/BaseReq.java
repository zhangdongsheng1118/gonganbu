package com.yixin.tinode.api.param;

/**
 * 手机端请求,不要在req中添加静态变量，会导致加密数据校验失败
 *
 * @author liulei
 * @version 1.0
 * @date 2017年7月6日 下午4:32:18
 */
public class BaseReq {
    // sign
    protected String token;
    protected long ts;

    public long getTs() {
        return ts;
    }

    public void setTs(long ts) {
        this.ts = ts;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}

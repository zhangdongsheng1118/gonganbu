package com.yixin.tinode.api.param;

import com.yixin.tinode.app.AppConst;

/**
 * 返回结果处理类
 *
 * @author liulei
 * @version 1.0
 * @date 2018年1月31日 下午4:26:04
 */
public class ReturnResult<T> {

    int result;
    String msg;
    T data;

    public boolean success() {
        return result == AppConst.RESULT_SUCCESS;
    }

    public ReturnResult() {
        this(AppConst.RESULT_ERROR);
    }

    public ReturnResult(boolean rsl) {
        this(rsl, null);
    }

    public ReturnResult(boolean rsl, String msg) {
        this(rsl, msg, null);
    }

    public ReturnResult(boolean rsl, String msg, T data) {
        result = rsl ? AppConst.RESULT_SUCCESS : AppConst.RESULT_ERROR;
        this.msg = msg;
        this.data = data;
    }

    public ReturnResult(int rsl) {
        this(rsl, null);
    }

    public ReturnResult(int rsl, String msg) {
        this(rsl, msg, null);
    }

    public ReturnResult(int rsl, String msg, T data) {
        result = rsl;
        this.msg = msg;
        this.data = data;
    }

    public void setResult(boolean success) {
        this.result = success ? AppConst.RESULT_SUCCESS : AppConst.RESULT_ERROR;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

}

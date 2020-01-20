package com.yixin.tinode.model.exception;

import com.yixin.tinode.R;
import com.yixin.tinode.util.UIUtils;

/**
 * @创建者 CSDN_LQR
 * @描述 服务器异常
 */
public class ServerException extends Exception {

    public ServerException(int errorCode) {
        this(UIUtils.getString(R.string.error_code) + errorCode);
    }

    public ServerException(String message) {
        super(message);
    }

}

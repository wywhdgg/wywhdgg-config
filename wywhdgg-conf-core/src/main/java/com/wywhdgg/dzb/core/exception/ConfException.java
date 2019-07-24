package com.wywhdgg.dzb.core.exception;

/**
 * @author: dongzhb
 * @date: 2019/7/24
 * @Description:
 */
public class ConfException extends RuntimeException {


    public ConfException(String msg) {
        super(msg);
    }

    public ConfException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public ConfException(Throwable cause) {
        super(cause);
    }
}

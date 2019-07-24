package com.wywhdgg.dzb.enums;

/**
 * @author: dongzhb
 * @date: 2019/7/24
 * @Description:  http请求操作
 */
public enum OperateEnums {
    /** http请求方式 */
    GET("GET"),
    POST("POST"),
    DELETE("DELETE"),
    PUT("PUT"),
    HEAD("HEAD"),
    PATCH("PATCH"),
    OPTIONS("OPTIONS"),
    TRACE("TRACE");

    OperateEnums(String value) {
        this.value = value;
    }

    private String value;

    public String getValue() {
        return value;
    }
}

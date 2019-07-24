package com.wywhdgg.dzb.core.listener;

/**
 * @author: dongzhb
 * @date: 2019/7/24
 * @Description:
 */
public interface ConfListener {
    /**
     * invoke when first-use or conf-change
     *
     * @param key
     */
    public void onChange(String key, String value) throws Exception;

}

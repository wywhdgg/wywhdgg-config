package com.wywhdgg.dzb.core.client;

import com.wywhdgg.dzb.core.cache.ConfLocalCache;
import com.wywhdgg.dzb.core.exception.ConfException;
import com.wywhdgg.dzb.core.listener.ConfListener;
import com.wywhdgg.dzb.core.listener.ConfListenerFactory;

/**
 * @author: dongzhb
 * @date: 2019/7/24
 * @Description:  client
 */
public class ConfClient {

    public static String get(String key, String defaultVal) {
        return ConfLocalCache.get(key, defaultVal);
    }

    /**
     * get conf (string)
     *
     * @param key
     * @return
     */
    public static String get(String key) {
        return get(key, null);
    }

    /**
     * get conf (boolean)
     *
     * @param key
     * @return
     */
    public static boolean getBoolean(String key) {
        String value = get(key, null);
        if (value == null) {
            throw new ConfException("config key [" + key + "] does not exist");
        }
        return Boolean.valueOf(value);
    }

    /**
     * get conf (short)
     *
     * @param key
     * @return
     */
    public static short getShort(String key) {
        String value = get(key, null);
        if (value == null) {
            throw new ConfException("config key [" + key + "] does not exist");
        }
        return Short.valueOf(value);
    }

    /**
     * get conf (int)
     *
     * @param key
     * @return
     */
    public static int getInt(String key) {
        String value = get(key, null);
        if (value == null) {
            throw new ConfException("config key [" + key + "] does not exist");
        }
        return Integer.valueOf(value);
    }

    /**
     * get conf (long)
     *
     * @param key
     * @return
     */
    public static long getLong(String key) {
        String value = get(key, null);
        if (value == null) {
            throw new ConfException("config key [" + key + "] does not exist");
        }
        return Long.valueOf(value);
    }

    /**
     * get conf (float)
     *
     * @param key
     * @return
     */
    public static float getFloat(String key) {
        String value = get(key, null);
        if (value == null) {
            throw new ConfException("config key [" + key + "] does not exist");
        }
        return Float.valueOf(value);
    }

    /**
     * get conf (double)
     *
     * @param key
     * @return
     */
    public static double getDouble(String key) {
        String value = get(key, null);
        if (value == null) {
            throw new ConfException("config key [" + key + "] does not exist");
        }
        return Double.valueOf(value);
    }

    /**
     * add listener with xxl conf change
     *
     * @param key
     * @param confListener
     * @return
     */
    public static boolean addListener(String key, ConfListener confListener){
        return ConfListenerFactory.addListener(key, confListener);
    }
}

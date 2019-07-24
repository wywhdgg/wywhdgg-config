package com.wywhdgg.dzb.core.listener;

import com.wywhdgg.dzb.core.client.ConfClient;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

/**
 * @author: dongzhb
 * @date: 2019/7/24
 * @Description:
 */
@Slf4j
public class ConfListenerFactory {
    /**
     * conf listener repository
     */
    private static ConcurrentHashMap<String, List<ConfListener>> keyListenerRepository = new ConcurrentHashMap<>();

    private static List<ConfListener> noKeyConfListener = Collections.synchronizedList(new ArrayList<ConfListener>());

    /**
     * 添加监听事件
     * @param key
     * @param confListener
     * */
    public static boolean addListener(String key, ConfListener confListener){
         if(confListener==null){
             return  false;
         }

         if(StringUtils.isBlank(key)){
             noKeyConfListener.add(confListener);
             return true;
         }

        // first use, invoke and watch this key
        try {
            String value = ConfClient.get(key);
            confListener.onChange(key, value);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        // listene this key
        List<ConfListener> listeners = keyListenerRepository.get(key);
        if (listeners == null) {
            listeners = new ArrayList<>();
            keyListenerRepository.put(key, listeners);
        }
        listeners.add(confListener);
        return true;
    }

   /**
    * 监听改变事件
    * @param key
    * @param value
    * */
    public static void onChange(String key, String value){
        if (StringUtils.isBlank(key)) {
            return;
        }

        List<ConfListener> keyListeners = keyListenerRepository.get(key);
        if (!CollectionUtils.isEmpty(keyListeners)) {
            for (ConfListener listener : keyListeners) {
                try {
                    listener.onChange(key, value);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
        if (noKeyConfListener.size() > 0) {
            for (ConfListener confListener: noKeyConfListener) {
                try {
                    confListener.onChange(key, value);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }
}

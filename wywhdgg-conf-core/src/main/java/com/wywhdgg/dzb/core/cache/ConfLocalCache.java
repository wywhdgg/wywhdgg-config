package com.wywhdgg.dzb.core.cache;

import com.wywhdgg.dzb.core.listener.ConfListenerFactory;
import com.wywhdgg.dzb.enums.SetTypeEnums;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

/**
 * @author: dongzhb
 * @date: 2019/7/24
 * @Description: 配置本地缓存
 */
@Slf4j
public class ConfLocalCache {
    /** 缓存存储 */
    private static ConcurrentHashMap<String, CacheNode> localCacheRepository = null;
    private static Thread refreshThread;
    private static boolean refreshThreadStop = false;

    public static void init() {
        localCacheRepository = new ConcurrentHashMap<String, CacheNode>(16);
        //预加载文件
        Map<String, String> preConfData = new HashMap<>();
        //获取远程配置文件
        Map<String, String> remoteConfData = null;
        //获取本地文件
        Map<String, String> mirrorConfData = ConfMirror.readConfMirror();

        if (!CollectionUtils.isEmpty(mirrorConfData)) {
            //DB文件预加载
            remoteConfData = ConfRemote.find(mirrorConfData.keySet());
            //本地文件
            preConfData.putAll(mirrorConfData);
        }

        if (remoteConfData != null && remoteConfData.size() > 0) {
            //
            preConfData.putAll(remoteConfData);
        }
        if (preConfData != null && preConfData.size() > 0) {
            for (String preKey : preConfData.keySet()) {
                set(preKey, preConfData.get(preKey), SetTypeEnums.PRELOAD);
            }
        }

        // refresh thread
        refreshThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!refreshThreadStop) {
                    try {
                        //开启保存方法，本地镜像双向保存
                        refreshCacheAndMirror();
                    } catch (Exception e) {
                        if (!refreshThreadStop && !(e instanceof InterruptedException)) {
                            log.error(">>>>>>>>>> wywhdgg-conf, refresh thread error.");
                            log.error(e.getMessage(), e);
                        }
                    }
                }
                log.info(">>>>>>>>>> wywhdgg-conf, refresh thread stoped.");
            }
        });
        /**
         1.Thread.setDaemon(boolean on):设置为守护线程或者用户线程。

         2.通过Thread.setDaemon(false)设置为用户线程,用于为系统中的其它对象和线程提供服务；
         通过Thread.setDaemon(true)设置为守护线程,在没有用户线程可服务时会自动离开;如果不设置此属性，默认为用户线程。

         3.setDaemon需要在start方法调用之前使用

         4.用Thread.isDaemon()来返回是否是守护线程
         **/
        refreshThread.setDaemon(true);
        refreshThread.start();

        log.info(">>>>>>>>>> wywhdgg-conf, ConfLocalCacheConf init success.");
    }

    /**
     * 刷新缓存，镜像，实时的监控 refresh Cache And Mirror, with real-time minitor
     */
    private static void refreshCacheAndMirror() throws InterruptedException {
        if (localCacheRepository.size() == 0) {
            TimeUnit.SECONDS.sleep(3);
            return;
        }

        // monitor
        boolean monitorRet = ConfRemote.monitor(localCacheRepository.keySet());
        // avoid fail-retry request too quick
        if (!monitorRet) {
            TimeUnit.SECONDS.sleep(10);
        }

        // refresh cache: remote > cache
        Set<String> keySet = localCacheRepository.keySet();
        if (keySet.size() > 0) {
            Map<String, String> remoteDataMap = ConfRemote.find(keySet);
            if (remoteDataMap != null && remoteDataMap.size() > 0) {
                for (String remoteKey : remoteDataMap.keySet()) {
                    String remoteData = remoteDataMap.get(remoteKey);
                    CacheNode existNode = localCacheRepository.get(remoteKey);
                    if (existNode != null && existNode.getValue() != null && existNode.getValue().equals(remoteData)) {
                        log.debug(">>>>>>>>>> wywhdgg-conf: RELOAD unchange-pass [{}].", remoteKey);
                    } else {
                        set(remoteKey, remoteData, SetTypeEnums.RELOAD);
                    }
                }
            }
        }

        // refresh mirror: cache > mirror
        Map<String, String> mirrorConfData = new HashMap<>();
        for (String key : keySet) {
            CacheNode existNode = localCacheRepository.get(key);
            mirrorConfData.put(key, existNode.getValue() != null ? existNode.getValue() : "");
        }
        ConfMirror.writeConfMirror(mirrorConfData);
        log.debug(">>>>>>>>>> wywhdgg-conf, refreshCacheAndMirror success.");
    }

    /**
     * set conf (invoke listener)
     */
    private static void set(String key, String value, SetTypeEnums optType) {
        localCacheRepository.put(key, new CacheNode(value));
        log.info(">>>wywhdgg-conf set: {}: [{}={}]", optType, key, value);

        // value updated, invoke listener
        if (optType == SetTypeEnums.RELOAD) {
            //change
            ConfListenerFactory.onChange(key, value);
        }

        // new conf, new monitor
        if (optType == SetTypeEnums.SET) {
            refreshThread.interrupt();
        }
    }

    /**
     * 线程销毁 refreshThread 内部通过minitor实时监听配置变更，但是只会监听LocalCache中存在的key列表。 当出现 SET 操作时，说明LocalCache中出现的新配置，但是该新配置key并没有纳入monitor实时监听， 因此需要中断线程，从而重新monitor全量的key列表。
     */
    public static void destroy() {
        if (refreshThread != null) {
            refreshThreadStop = true;
            refreshThread.interrupt();
        }
    }

    /**
     * local cache node
     */
    public static class CacheNode implements Serializable {
        private static final long serialVersionUID = -6702498337281454130L;
        private String value;

        public CacheNode() {
        }

        public CacheNode(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    /**
     * get conf
     */
    private static CacheNode get(String key) {
        if (localCacheRepository.containsKey(key)) {
            CacheNode cacheNode = localCacheRepository.get(key);
            return cacheNode;
        }
        return null;
    }

    /**
     * get conf
     * 1.缓存去数据。
     * 2.查询数据
     * 3.重新设置数据
     */
    public static String get(String key, String defaultVal) {
        // level 1: local cache
        ConfLocalCache.CacheNode cacheNode = ConfLocalCache.get(key);
        if (cacheNode != null) {
            return cacheNode.getValue();
        }
        // level 2	(get-and-watch, add-local-cache)
        String remoteData = null;
        try {
            remoteData = ConfRemote.find(key);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        /**support cache null value*/
        set(key, remoteData, SetTypeEnums.SET);
        if (remoteData != null) {
            return remoteData;
        }
        return defaultVal;
    }
}

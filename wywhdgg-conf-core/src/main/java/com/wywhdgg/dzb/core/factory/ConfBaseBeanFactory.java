package com.wywhdgg.dzb.core.factory;

import com.wywhdgg.dzb.core.cache.ConfLocalCache;
import com.wywhdgg.dzb.core.cache.ConfMirror;
import com.wywhdgg.dzb.core.cache.ConfRemote;
import com.wywhdgg.dzb.core.listener.ConfListenerFactory;
import com.wywhdgg.dzb.core.listener.impl.BeanRefreshConfListener;

/**
 * @author: dongzhb
 * @date: 2019/7/24
 * @Description: 初始化bean
 */
public class ConfBaseBeanFactory {
    /**
     * 初始化环境
     */
    public static void init(String adminAddress, String env, String accessToken, String mirrorfile) {
        /**初始化远程调用的地址*/
        ConfRemote.init(adminAddress, env, accessToken);
        /**初始化本地文件*/
        ConfMirror.init(mirrorfile);
        /**初始化缓存 */
        ConfLocalCache.init();
        /**init cache + thread, cycle refresh + monitor*/
        ConfListenerFactory.addListener(null, new BeanRefreshConfListener());
    }

    /**
     * bean销毁
     */
    public static void destroy() {
        ConfLocalCache.destroy();
    }
}

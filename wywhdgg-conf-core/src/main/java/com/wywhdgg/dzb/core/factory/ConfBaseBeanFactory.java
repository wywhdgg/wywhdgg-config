package com.wywhdgg.dzb.core.factory;

import com.wywhdgg.dzb.core.cache.ConfLocalCache;
import com.wywhdgg.dzb.core.cache.ConfMirror;
import com.wywhdgg.dzb.core.cache.ConfRemote;
import com.wywhdgg.dzb.core.listener.ConfListenerFactory;
import com.wywhdgg.dzb.core.listener.impl.BeanRefreshConfListener;

/**
 * @author: dongzhb
 * @date: 2019/7/24
 * @Description:
 */
public class ConfBaseBeanFactory {
    /**
     * 初始化环境
     */
    public static void init(String adminAddress, String env, String accessToken, String mirrorfile) {
        /**init remote util*/
        ConfRemote.init(adminAddress, env, accessToken);
        /**init mirror util*/
        ConfMirror.init(mirrorfile);
        /***/
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

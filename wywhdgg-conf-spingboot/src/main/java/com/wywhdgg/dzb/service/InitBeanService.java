package com.wywhdgg.dzb.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

/**
 * @author: dongzhb
 * @date: 2019/7/26
 * @Description:
 */
@Slf4j
@Service
public class InitBeanService implements InitializingBean, DisposableBean {
    private String messgae;

    public String getMessgae() {
        return messgae;
    }

    public void setMessgae(String messgae) {
        this.messgae = messgae;
    }

    /** 对于 Bean 实现了DisposableBean，它将运行 destroy()在 Spring 容器释放该 bean 之后。 */
    @Override
    public void destroy() throws Exception {
        log.info("Spring Container is destroy! initBean clean up ,message={}", messgae);
    }

    /** 对于Bean实现 InitializingBean，它将运行 afterPropertiesSet()在所有的 bean 属性被设置之后。 */
    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("Init method after properties are set message={}", messgae);
    }
}

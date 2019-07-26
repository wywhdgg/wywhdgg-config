package com.wywhdgg.dzb;

import com.wywhdgg.dzb.service.InitBeanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author: dongzhb
 * @date: 2019/7/26
 * @Description:
 */
@Slf4j
public class SpringBeanTest {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
        InitBeanService initBeanService = (InitBeanService) context.getBean("initBeanService");
        log.info("before message={}", initBeanService.getMessgae());
        context.close();
        log.info("after message={}", initBeanService.getMessgae());
    }
}

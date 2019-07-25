package com.wywhdgg.dzb.config;

import com.wywhdgg.dzb.core.spring.ConfFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * @author: dongzhb
 * @date: 2019/7/25
 * @Description:
 */
@Slf4j
@Configuration
public class ConfConfig {
    @Value("${conf.admin.address}")
    private String adminAddress;

    @Value("${conf.env}")
    private String env;

    @Value("${conf.access.token}")
    private String accessToken;

    @Value("${conf.mirrorfile}")
    private String mirrorfile;

   /**
    * 注入链接
    * */
    @Bean
    public ConfFactory initConfFactory() {
        ConfFactory confFactory = new ConfFactory();
        confFactory.setAdminAddress(adminAddress);
        confFactory.setEnv(env);
        confFactory.setAccessToken(accessToken);
        confFactory.setMirrorfile(mirrorfile);
        log.info(">>>>>>>>>>> conf config init.......");
        return confFactory;
    }

}

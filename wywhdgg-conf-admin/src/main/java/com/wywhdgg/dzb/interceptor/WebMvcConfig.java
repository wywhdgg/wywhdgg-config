package com.wywhdgg.dzb.interceptor;

import javax.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/***
 *@author dzb
 *@date 2019/7/21 22:27
 *@Description:  WebMvcConfigurerAdapter 废弃
 * 拦截器 -配置列表  跨域设置
 *@version 1.0
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Resource
    private PermissionInterceptor permissionInterceptor;
    @Resource
    private EnvInterceptor envInterceptor;
    @Resource
    private CookieInterceptor cookieInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //注册拦截器，添加匹配的资源
        registry.addInterceptor(permissionInterceptor).addPathPatterns("/**");
        registry.addInterceptor(envInterceptor).addPathPatterns("/**");
        registry.addInterceptor(cookieInterceptor).addPathPatterns("/**");
    }
}

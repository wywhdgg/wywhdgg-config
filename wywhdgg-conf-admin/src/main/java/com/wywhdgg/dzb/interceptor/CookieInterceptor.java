package com.wywhdgg.dzb.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.ModelAndView;
import javax.servlet.http.Cookie;
import java.util.HashMap;

/***
 *@author dzb
 *@date 2019/7/21 22:18
 *@Description:  cookie 处理
 *@version 1.0
 */
@Slf4j
@Component
public class CookieInterceptor extends HandlerInterceptorAdapter {
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        if (modelAndView != null && request.getCookies() != null && request.getCookies().length > 0) {
            log.info("getCookies={}",request.getCookies());
            HashMap<String, Cookie> cookieMap = new HashMap<String, Cookie>();
            for (Cookie ck : request.getCookies()) {
                cookieMap.put(ck.getName(), ck);
            }
            modelAndView.addObject("cookieMap", cookieMap);
            log.info("postHandle modelAndView={}",modelAndView);
        }
        super.postHandle(request, response, handler, modelAndView);
    }
}

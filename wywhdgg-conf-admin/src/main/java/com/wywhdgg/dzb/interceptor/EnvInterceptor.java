package com.wywhdgg.dzb.interceptor;

import com.wywhdgg.dzb.dao.ConfEnvDao;
import com.wywhdgg.dzb.entity.ConfEnv;
import com.wywhdgg.dzb.util.CookieUtil;
import java.util.List;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/***
 *@author dzb
 *@date 2019/7/21 22:21
 *@Description:  配置环境
 *@version 1.0
 */
@Component
public class EnvInterceptor extends HandlerInterceptorAdapter {

    public static final String CURRENT_ENV = "XXL_CONF_CURRENT_ENV";

    @Resource
    private ConfEnvDao confEnvDao;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // env list
        List<ConfEnv> envList = confEnvDao.findAll();
        if (envList==null || envList.size()==0) {
            throw new RuntimeException("系统异常，获取Env数据失败");
        }

        // current env
        String currentEnv = envList.get(0).getEnv();
        String currentEnvCookie = CookieUtil.getValue(request, CURRENT_ENV);
        if (currentEnvCookie!=null && currentEnvCookie.trim().length()>0) {
            for (ConfEnv envItem: envList) {
                if (currentEnvCookie.equals(envItem.getEnv())) {
                    currentEnv = envItem.getEnv();
                }
            }
        }

        request.setAttribute("envList", envList);
        request.setAttribute(CURRENT_ENV, currentEnv);
        return super.preHandle(request, response, handler);
    }
}

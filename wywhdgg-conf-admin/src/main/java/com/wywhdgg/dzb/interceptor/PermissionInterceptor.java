package com.wywhdgg.dzb.interceptor;

import com.wywhdgg.dzb.annotation.PermessionLimit;
import com.wywhdgg.dzb.config.LoginConfig;
import com.wywhdgg.dzb.constants.LoginConfigConstants;
import com.wywhdgg.dzb.entity.ConfUser;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/***
 *@author dzb
 *@date 2019/7/21 22:26
 *@Description: 权限拦截, 简易版
 *@version 1.0
 */
@Component
public class PermissionInterceptor extends HandlerInterceptorAdapter {

    @Resource
    private LoginConfig loginConfig;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return super.preHandle(request, response, handler);
        }

        // if need login
        boolean needLogin = true;
        boolean needAdminuser = false;
        HandlerMethod method = (HandlerMethod)handler;
        PermessionLimit permission = method.getMethodAnnotation(PermessionLimit.class);
        if (permission!=null) {
            needLogin = permission.limit();
            needAdminuser = permission.adminuser();
        }

        if (needLogin) {
            ConfUser loginUser = loginConfig.ifLogin(request);
            if (loginUser == null) {
                response.sendRedirect(request.getContextPath() + "/toLogin");
                //request.getRequestDispatcher("/toLogin").forward(request, response);
                return false;
            }
            if (needAdminuser && loginUser.getPermission()!=1) {
                throw new RuntimeException("权限拦截");
            }
            request.setAttribute(LoginConfigConstants.LOGIN_IDENTITY, loginUser);
        }
        return super.preHandle(request, response, handler);
    }


}

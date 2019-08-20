package com.wywhdgg.dzb.exception;

import com.wywhdgg.dzb.util.JacksonUtil;
import com.wywhdgg.dzb.util.Result;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

/***
 *@author dzb
 *@date 2019/7/21 23:34
 *@Description:
 * common exception resolver
 *
 * 		1、@ControllerAdvice：扫描所有Controller；
 * 		2、@ControllerAdvice(annotations=RestController.class)：扫描指定注解类型的Controller；
 * 		3、@ControllerAdvice(basePackages={"com.aaa","com.bbb"})：扫描指定package下的Controller
 *  统一处理异常
 *@version 1.0
 */
@Slf4j
@Component
public class WebExceptionResolver implements HandlerExceptionResolver {
    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        log.error("WebExceptionResolver:{}", ex);
        // if json
        boolean isJson = false;
        HandlerMethod method = (HandlerMethod) handler;
        ResponseBody responseBody = method.getMethodAnnotation(ResponseBody.class);
        if (responseBody != null) {
            isJson = true;
        }

        // error result
        Result<String> errorResult = new Result<String>(Result.FAIL.getCode(), ex.toString().replaceAll("\n", "<br/>"));

        // response
        ModelAndView mv = new ModelAndView();
        if (isJson) {
            try {
                response.setContentType("application/json;charset=utf-8");
                response.getWriter().print(JacksonUtil.writeValueAsString(errorResult));
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
            return mv;
        } else {

            mv.addObject("exceptionMsg", errorResult.getMsg());
            mv.setViewName("common/common.exception");
            return mv;
        }
    }
}

package com.wywhdgg.dzb.controller;


import com.wywhdgg.dzb.annotation.PermessionLimit;
import com.wywhdgg.dzb.config.LoginConfig;
import com.wywhdgg.dzb.util.Result;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by xuxueli on 16/7/30.
 */

@Controller
public class IndexController {
    private static Logger logger = LoggerFactory.getLogger(IndexController.class.getName());

    @Resource
    private LoginConfig  loginConfig;

    @RequestMapping("/")
    public String index(Model model, HttpServletRequest request) {
        return "redirect:/conf";
    }

    @RequestMapping("/toLogin")
    @PermessionLimit(limit=false)
    public String toLogin(Model model, HttpServletRequest request) {
        if (loginConfig.ifLogin(request) != null) {
            return "redirect:/";
        }
        return "login";
    }

    @RequestMapping(value="login", method= RequestMethod.POST)
    @ResponseBody
    @PermessionLimit(limit=false)
    public Result<String> loginDo(HttpServletRequest request, HttpServletResponse response, String userName, String password, String ifRemember){
        // valid
        if (loginConfig.ifLogin(request) != null) {
            return Result.SUCCESS;
        }

        // param
        if (StringUtils.isBlank(userName) || StringUtils.isBlank(password)){
            return new Result<String>(500, "账号或密码为空");
        }
        boolean ifRem = (StringUtils.isNotBlank(ifRemember) && "on".equals(ifRemember))?true:false;

        // do login
        return loginConfig.login(response, userName, password, ifRem);
    }

    @RequestMapping(value="logout", method=RequestMethod.POST)
    @ResponseBody
    @PermessionLimit(limit=false)
    public Result<String> logout(HttpServletRequest request, HttpServletResponse response){
        if (loginConfig.ifLogin(request) != null) {
            loginConfig.logout(request, response);
        }
        return Result.SUCCESS;
    }

    @RequestMapping("/help")
    public String help() {
        return "help";
    }

}

package com.wywhdgg.dzb.controller;

import com.wywhdgg.dzb.annotation.PermessionLimit;
import com.wywhdgg.dzb.constants.LoginConfigConstants;
import com.wywhdgg.dzb.dao.ConfEnvDao;
import com.wywhdgg.dzb.dao.ConfProjectDao;
import com.wywhdgg.dzb.dao.ConfUserDao;
import com.wywhdgg.dzb.entity.ConfEnv;
import com.wywhdgg.dzb.entity.ConfProject;
import com.wywhdgg.dzb.entity.ConfUser;
import com.wywhdgg.dzb.serivce.ConfEnvService;
import com.wywhdgg.dzb.serivce.ConfProjectService;
import com.wywhdgg.dzb.serivce.ConfUserService;
import com.wywhdgg.dzb.util.Result;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author xuxueli 2018-03-01
 */
@Controller
@RequestMapping("/user")
public class UserController {
    @Resource
    private ConfUserService confUserService;
    @Resource
    private ConfProjectService confProjectService;
    @Resource
    private ConfEnvService confEnvService;

    @RequestMapping("")
    @PermessionLimit(adminuser = true)
    public String index(Model model) {

        List<ConfProject> projectList = confProjectService.findAll();
        model.addAttribute("projectList", projectList);

        List<ConfEnv> envList = confEnvService.findAll();
        model.addAttribute("envList", envList);

        return "user/user.index";
    }

    @RequestMapping("/pageList")
    @PermessionLimit(adminuser = true)
    @ResponseBody
    public Map<String, Object> pageList(@RequestParam(required = false, defaultValue = "0") int start, @RequestParam(required = false, defaultValue = "10") int length, String username,
        int permission) {

        // confNode in mysql
        List<ConfUser> data = confUserService.pageList(start, length, username, permission);
        int list_count = confUserService.pageListCount(start, length, username, permission);

        // package result
        Map<String, Object> maps = new HashMap<String, Object>();
        maps.put("data", data);
        maps.put("recordsTotal", list_count);        // 总记录数
        maps.put("recordsFiltered", list_count);    // 过滤后的总记录数
        return maps;
    }

    /**
     * add
     */
    @RequestMapping("/add")
    @PermessionLimit(adminuser = true)
    @ResponseBody
    public Result<String> add(ConfUser confUser) {

        // valid
        if (StringUtils.isBlank(confUser.getUsername())) {
            return new Result<String>(Result.FAIL.getCode(), "用户名不可为空");
        }
        if (StringUtils.isBlank(confUser.getPassword())) {
            return new Result<String>(Result.FAIL.getCode(), "密码不可为空");
        }
        if (!(confUser.getPassword().length() >= 4 && confUser.getPassword().length() <= 100)) {
            return new Result<String>(Result.FAIL.getCode(), "密码长度限制为4~50");
        }

        // passowrd md5
        String md5Password = DigestUtils.md5DigestAsHex(confUser.getPassword().getBytes());
        confUser.setPassword(md5Password);

        int ret = confUserService.add(confUser);
        return ret > 0 ? Result.SUCCESS : Result.FAIL;
    }

    /**
     * delete
     */
    @RequestMapping("/delete")
    @PermessionLimit(adminuser = true)
    @ResponseBody
    public Result<String> delete(HttpServletRequest request, String username) {

        ConfUser loginUser = (ConfUser) request.getAttribute(LoginConfigConstants.LOGIN_IDENTITY);
        if (loginUser.getUsername().equals(username)) {
            return new Result<String>(Result.FAIL.getCode(), "禁止操作当前登录账号");
        }

        /*List<ConfUser> adminList = confUserDao.pageList(0, 1 , null, 1);
        if (adminList.size()<2) {

        }*/

        confUserService.delete(username);
        return Result.SUCCESS;
    }

    /**
     * update
     */
    @RequestMapping("/update")
    @PermessionLimit(adminuser = true)
    @ResponseBody
    public Result<String> update(HttpServletRequest request, ConfUser confUser) {

        ConfUser loginUser = (ConfUser) request.getAttribute(LoginConfigConstants.LOGIN_IDENTITY);
        if (loginUser.getUsername().equals(confUser.getUsername())) {
            return new Result<String>(Result.FAIL.getCode(), "禁止操作当前登录账号");
        }

        // valid
        if (StringUtils.isBlank(confUser.getUsername())) {
            return new Result<String>(Result.FAIL.getCode(), "用户名不可为空");
        }

        ConfUser existUser = confUserService.load(confUser.getUsername());
        if (existUser == null) {
            return new Result<String>(Result.FAIL.getCode(), "用户名非法");
        }

        if (StringUtils.isNotBlank(confUser.getPassword())) {
            if (!(confUser.getPassword().length() >= 4 && confUser.getPassword().length() <= 50)) {
                return new Result<String>(Result.FAIL.getCode(), "密码长度限制为4~50");
            }
            // passowrd md5
            String md5Password = DigestUtils.md5DigestAsHex(confUser.getPassword().getBytes());
            existUser.setPassword(md5Password);
        }
        existUser.setPermission(confUser.getPermission());

        int ret = confUserService.update(existUser);
        return ret > 0 ? Result.SUCCESS : Result.FAIL;
    }

    @RequestMapping("/updatePermissionData")
    @PermessionLimit(adminuser = true)
    @ResponseBody
    public Result<String> updatePermissionData(HttpServletRequest request, String username, @RequestParam(required = false) String[] permissionData) {

        ConfUser existUser = confUserService.load(username);
        if (existUser == null) {
            return new Result<String>(Result.FAIL.getCode(), "参数非法");
        }

        String permissionDataArrStr = permissionData != null ? StringUtils.join(permissionData, ",") : "";
        existUser.setPermissionData(permissionDataArrStr);
        confUserService.update(existUser);

        return Result.SUCCESS;
    }

    @RequestMapping("/updatePwd")
    @ResponseBody
    public Result<String> updatePwd(HttpServletRequest request, String password) {

        // new password(md5)
        if (StringUtils.isBlank(password)) {
            return new Result<String>(Result.FAIL.getCode(), "密码不可为空");
        }
        if (!(password.length() >= 4 && password.length() <= 100)) {
            return new Result<String>(Result.FAIL.getCode(), "密码长度限制为4~50");
        }
        String md5Password = DigestUtils.md5DigestAsHex(password.getBytes());

        // update pwd
        ConfUser loginUser = (ConfUser) request.getAttribute(LoginConfigConstants.LOGIN_IDENTITY);

        ConfUser existUser = confUserService.load(loginUser.getUsername());
        existUser.setPassword(md5Password);
        confUserService.update(existUser);

        return Result.SUCCESS;
    }
}

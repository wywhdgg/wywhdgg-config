package com.wywhdgg.dzb.controller;


import com.wywhdgg.dzb.annotation.PermessionLimit;
import com.wywhdgg.dzb.constants.LoginConfigConstants;
import com.wywhdgg.dzb.dao.ConfEnvDao;
import com.wywhdgg.dzb.dao.ConfProjectDao;
import com.wywhdgg.dzb.dao.ConfUserDao;
import com.wywhdgg.dzb.entity.ConfEnv;
import com.wywhdgg.dzb.entity.ConfProject;
import com.wywhdgg.dzb.entity.ConfUser;
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
    private ConfUserDao xxlConfUserDao;
    @Resource
    private ConfProjectDao xxlConfProjectDao;
    @Resource
    private ConfEnvDao xxlConfEnvDao;

    @RequestMapping("")
    @PermessionLimit(adminuser = true)
    public String index(Model model){

        List<ConfProject> projectList = xxlConfProjectDao.findAll();
        model.addAttribute("projectList", projectList);

        List<ConfEnv> envList = xxlConfEnvDao.findAll();
        model.addAttribute("envList", envList);

        return "user/user.index";
    }

    @RequestMapping("/pageList")
    @PermessionLimit(adminuser = true)
    @ResponseBody
    public Map<String, Object> pageList(@RequestParam(required = false, defaultValue = "0") int start,
                                        @RequestParam(required = false, defaultValue = "10") int length,
                                        String username,
                                        int permission) {

        // xxlConfNode in mysql
        List<ConfUser> data = xxlConfUserDao.pageList(start, length, username, permission);
        int list_count = xxlConfUserDao.pageListCount(start, length, username, permission);

        // package result
        Map<String, Object> maps = new HashMap<String, Object>();
        maps.put("data", data);
        maps.put("recordsTotal", list_count);		// 总记录数
        maps.put("recordsFiltered", list_count);	// 过滤后的总记录数
        return maps;
    }

    /**
     * add
     *
     * @return
     */
    @RequestMapping("/add")
    @PermessionLimit(adminuser = true)
    @ResponseBody
    public Result<String> add(ConfUser xxlConfUser){

        // valid
        if (StringUtils.isBlank(xxlConfUser.getUsername())){
            return new Result<String>(Result.FAIL.getCode(), "用户名不可为空");
        }
        if (StringUtils.isBlank(xxlConfUser.getPassword())){
            return new Result<String>(Result.FAIL.getCode(), "密码不可为空");
        }
        if (!(xxlConfUser.getPassword().length()>=4 && xxlConfUser.getPassword().length()<=100)) {
            return new Result<String>(Result.FAIL.getCode(), "密码长度限制为4~50");
        }

        // passowrd md5
        String md5Password = DigestUtils.md5DigestAsHex(xxlConfUser.getPassword().getBytes());
        xxlConfUser.setPassword(md5Password);

        int ret = xxlConfUserDao.add(xxlConfUser);
        return ret>0? Result.SUCCESS: Result.FAIL;
    }

    /**
     * delete
     *
     * @return
     */
    @RequestMapping("/delete")
    @PermessionLimit(adminuser = true)
    @ResponseBody
    public Result<String> delete(HttpServletRequest request, String username){

        ConfUser loginUser = (ConfUser) request.getAttribute(LoginConfigConstants.LOGIN_IDENTITY);
        if (loginUser.getUsername().equals(username)) {
            return new Result<String>(Result.FAIL.getCode(), "禁止操作当前登录账号");
        }

        /*List<ConfUser> adminList = xxlConfUserDao.pageList(0, 1 , null, 1);
        if (adminList.size()<2) {

        }*/

        xxlConfUserDao.delete(username);
        return Result.SUCCESS;
    }

    /**
     * update
     *
     * @return
     */
    @RequestMapping("/update")
    @PermessionLimit(adminuser = true)
    @ResponseBody
    public Result<String> update(HttpServletRequest request, ConfUser xxlConfUser){

        ConfUser loginUser = (ConfUser) request.getAttribute(LoginConfigConstants.LOGIN_IDENTITY);
        if (loginUser.getUsername().equals(xxlConfUser.getUsername())) {
            return new Result<String>(Result.FAIL.getCode(), "禁止操作当前登录账号");
        }

        // valid
        if (StringUtils.isBlank(xxlConfUser.getUsername())){
            return new Result<String>(Result.FAIL.getCode(), "用户名不可为空");
        }

        ConfUser existUser = xxlConfUserDao.load(xxlConfUser.getUsername());
        if (existUser == null) {
            return new Result<String>(Result.FAIL.getCode(), "用户名非法");
        }

        if (StringUtils.isNotBlank(xxlConfUser.getPassword())) {
            if (!(xxlConfUser.getPassword().length()>=4 && xxlConfUser.getPassword().length()<=50)) {
                return new Result<String>(Result.FAIL.getCode(), "密码长度限制为4~50");
            }
            // passowrd md5
            String md5Password = DigestUtils.md5DigestAsHex(xxlConfUser.getPassword().getBytes());
            existUser.setPassword(md5Password);
        }
        existUser.setPermission(xxlConfUser.getPermission());

        int ret = xxlConfUserDao.update(existUser);
        return ret>0? Result.SUCCESS: Result.FAIL;
    }

    @RequestMapping("/updatePermissionData")
    @PermessionLimit(adminuser = true)
    @ResponseBody
    public Result<String> updatePermissionData(HttpServletRequest request,
                                                    String username,
                                                    @RequestParam(required = false) String[] permissionData){

        ConfUser existUser = xxlConfUserDao.load(username);
        if (existUser == null) {
            return new Result<String>(Result.FAIL.getCode(), "参数非法");
        }

        String permissionDataArrStr = permissionData!=null?StringUtils.join(permissionData, ","):"";
        existUser.setPermissionData(permissionDataArrStr);
        xxlConfUserDao.update(existUser);

        return Result.SUCCESS;
    }

    @RequestMapping("/updatePwd")
    @ResponseBody
    public Result<String> updatePwd(HttpServletRequest request, String password){

        // new password(md5)
        if (StringUtils.isBlank(password)){
            return new Result<String>(Result.FAIL.getCode(), "密码不可为空");
        }
        if (!(password.length()>=4 && password.length()<=100)) {
            return new Result<String>(Result.FAIL.getCode(), "密码长度限制为4~50");
        }
        String md5Password = DigestUtils.md5DigestAsHex(password.getBytes());

        // update pwd
        ConfUser loginUser = (ConfUser) request.getAttribute(LoginConfigConstants.LOGIN_IDENTITY);

        ConfUser existUser = xxlConfUserDao.load(loginUser.getUsername());
        existUser.setPassword(md5Password);
        xxlConfUserDao.update(existUser);

        return Result.SUCCESS;
    }

}

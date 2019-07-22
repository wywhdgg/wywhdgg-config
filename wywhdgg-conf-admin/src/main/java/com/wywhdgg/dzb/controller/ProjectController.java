package com.wywhdgg.dzb.controller;

import com.wywhdgg.dzb.annotation.PermessionLimit;
import com.wywhdgg.dzb.dao.ConfNodeDao;
import com.wywhdgg.dzb.dao.ConfProjectDao;
import com.wywhdgg.dzb.entity.ConfProject;
import com.wywhdgg.dzb.serivce.ConfNodeService;
import com.wywhdgg.dzb.serivce.ConfProjectService;
import com.wywhdgg.dzb.util.Result;
import java.util.List;
import javax.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 项目管理
 *
 * @author xuxueli 2016-10-02 20:52:56
 */
@Controller
@RequestMapping("/project")
public class ProjectController {
    @Resource
    private ConfProjectService confProjectService;
    @Resource
    private ConfNodeService confNodeService;

    @RequestMapping
    @PermessionLimit(adminuser = true)
    public String index(Model model) {

        List<ConfProject> list = confProjectService.findAll();
        model.addAttribute("list", list);

        return "project/project.index";
    }

    @RequestMapping("/save")
    @PermessionLimit(adminuser = true)
    @ResponseBody
    public Result<String> save(ConfProject xxlConfProject) {

        // valid
        if (StringUtils.isBlank(xxlConfProject.getAppname())) {
            return new Result<String>(500, "AppName不可为空");
        }
        if (xxlConfProject.getAppname().length() < 4 || xxlConfProject.getAppname().length() > 100) {
            return new Result<String>(500, "Appname长度限制为4~100");
        }
        if (StringUtils.isBlank(xxlConfProject.getTitle())) {
            return new Result<String>(500, "请输入项目名称");
        }

        // valid repeat
        ConfProject existProject = confProjectService.load(xxlConfProject.getAppname());
        if (existProject != null) {
            return new Result<String>(500, "Appname已存在，请勿重复添加");
        }

        int ret = confProjectService.save(xxlConfProject);
        return (ret > 0) ? Result.SUCCESS : Result.FAIL;
    }

    @RequestMapping("/update")
    @PermessionLimit(adminuser = true)
    @ResponseBody
    public Result<String> update(ConfProject xxlConfProject) {

        // valid
        if (StringUtils.isBlank(xxlConfProject.getAppname())) {
            return new Result<String>(500, "AppName不可为空");
        }
        if (StringUtils.isBlank(xxlConfProject.getTitle())) {
            return new Result<String>(500, "请输入项目名称");
        }

        int ret = confProjectService.update(xxlConfProject);
        return (ret > 0) ? Result.SUCCESS : Result.FAIL;
    }

    @RequestMapping("/remove")
    @PermessionLimit(adminuser = true)
    @ResponseBody
    public Result<String> remove(String appname) {

        if (StringUtils.isBlank(appname)) {
            return new Result<String>(500, "参数AppName非法");
        }

        // valid
        int list_count = confNodeService.pageListCount(0, 10, null, appname, null);
        if (list_count > 0) {
            return new Result<String>(500, "拒绝删除，该项目下存在配置数据");
        }

        List<ConfProject> allList = confProjectService.findAll();
        if (allList.size() == 1) {
            return new Result<String>(500, "拒绝删除, 需要至少预留一个项目");
        }

        int ret = confProjectService.delete(appname);
        return (ret > 0) ? Result.SUCCESS : Result.FAIL;
    }
}

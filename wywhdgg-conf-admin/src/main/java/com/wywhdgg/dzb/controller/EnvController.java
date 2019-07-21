package com.wywhdgg.dzb.controller;


import com.wywhdgg.dzb.annotation.PermessionLimit;
import com.wywhdgg.dzb.dao.ConfEnvDao;
import com.wywhdgg.dzb.dao.ConfNodeDao;
import com.wywhdgg.dzb.entity.ConfEnv;
import com.wywhdgg.dzb.util.Result;
import java.util.List;
import javax.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 环境管理
 *
 * @author xuxueli 2018-05-30
 */
@Controller
@RequestMapping("/env")
public class EnvController {
	
	@Resource
	private ConfEnvDao xxlConfEnvDao;
    @Resource
    private ConfNodeDao xxlConfNodeDao;


	@RequestMapping
	@PermessionLimit(adminuser = true)
	public String index(Model model) {

		List<ConfEnv> list = xxlConfEnvDao.findAll();
		model.addAttribute("list", list);

		return "env/env.index";
	}

	@RequestMapping("/save")
	@PermessionLimit(adminuser = true)
	@ResponseBody
	public Result<String> save(ConfEnv xxlConfEnv){

		// valid
		if (StringUtils.isBlank(xxlConfEnv.getEnv())) {
			return new Result<String>(500, "Env不可为空");
		}
		if (xxlConfEnv.getEnv().length()<3 || xxlConfEnv.getEnv().length()>50) {
			return new Result<String>(500, "Env长度限制为4~50");
		}
		if (StringUtils.isBlank(xxlConfEnv.getTitle())) {
			return new Result<String>(500, "请输入Env名称");
		}

		// valid repeat
		ConfEnv existEnv = xxlConfEnvDao.load(xxlConfEnv.getEnv());
		if (existEnv != null) {
			return new Result<String>(500, "Env已存在，请勿重复添加");
		}

		int ret = xxlConfEnvDao.save(xxlConfEnv);
		return (ret>0)?Result.SUCCESS:Result.FAIL;
	}

	@RequestMapping("/update")
	@PermessionLimit(adminuser = true)
	@ResponseBody
	public Result<String> update(ConfEnv xxlConfEnv){

		// valid
		if (StringUtils.isBlank(xxlConfEnv.getEnv())) {
			return new Result<String>(500, "Env不可为空");
		}
		if (StringUtils.isBlank(xxlConfEnv.getTitle())) {
			return new Result<String>(500, "请输入Env名称");
		}

		int ret = xxlConfEnvDao.update(xxlConfEnv);
		return (ret>0)?Result.SUCCESS:Result.FAIL;
	}

	@RequestMapping("/remove")
	@PermessionLimit(adminuser = true)
	@ResponseBody
	public Result<String> remove(String env){

		if (StringUtils.isBlank(env)) {
			return new Result<String>(500, "参数Env非法");
		}

        // valid
        int list_count = xxlConfNodeDao.pageListCount(0, 10, env, null, null);
        if (list_count > 0) {
            return new Result<String>(500, "拒绝删除，该Env下存在配置数据");
        }

		// valid can not be empty
		List<ConfEnv> allList = xxlConfEnvDao.findAll();
		if (allList.size() == 1) {
			return new Result<String>(500, "拒绝删除, 需要至少预留一个Env");
		}

		int ret = xxlConfEnvDao.delete(env);
		return (ret>0)?Result.SUCCESS:Result.FAIL;
	}

}

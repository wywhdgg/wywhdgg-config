package com.wywhdgg.dzb.controller;


import com.wywhdgg.dzb.annotation.PermessionLimit;
import com.wywhdgg.dzb.constants.LoginConfigConstants;
import com.wywhdgg.dzb.entity.ConfNode;
import com.wywhdgg.dzb.entity.ConfProject;
import com.wywhdgg.dzb.entity.ConfUser;
import com.wywhdgg.dzb.serivce.ConfNodeService;
import com.wywhdgg.dzb.serivce.ConfProjectService;
import com.wywhdgg.dzb.util.JacksonUtil;
import com.wywhdgg.dzb.util.Result;
import com.wywhdgg.dzb.vo.ConfParamVO;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.DeferredResult;

/**
 * 配置管理
 *
 * @author xuxueli
 */
@Controller
@RequestMapping("/conf")
public class ConfController {

    public static final String CURRENT_ENV = "XXL_CONF_CURRENT_ENV";

    @Resource
    private ConfProjectService confProjectService;
    @Resource
    private ConfNodeService confNodeService;

    @RequestMapping("")
    public String index(HttpServletRequest request, Model model, String appname) {

        List<ConfProject> list = confProjectService.findAll();
        if (list == null || list.size() == 0) {
            throw new RuntimeException("系统异常，无可用项目");
        }

        ConfProject project = list.get(0);
        for (ConfProject item : list) {
            if (item.getAppname().equals(appname)) {
                project = item;
            }
        }

        boolean ifHasProjectPermission = confNodeService
            .ifHasProjectPermission((ConfUser) request.getAttribute(LoginConfigConstants.LOGIN_IDENTITY), (String) request.getAttribute(CURRENT_ENV), project.getAppname());

        model.addAttribute("ProjectList", list);
        model.addAttribute("project", project);
        model.addAttribute("ifHasProjectPermission", ifHasProjectPermission);

        return "conf/conf.index";
    }

    @RequestMapping("/pageList")
    @ResponseBody
    public Map<String, Object> pageList(HttpServletRequest request, @RequestParam(required = false, defaultValue = "0") int start, @RequestParam(required = false, defaultValue = "10") int length,
        String appname, String key) {

        ConfUser xxlConfUser = (ConfUser) request.getAttribute(LoginConfigConstants.LOGIN_IDENTITY);
        String loginEnv = (String) request.getAttribute(CURRENT_ENV);

        return confNodeService.pageList(start, length, appname, key, xxlConfUser, loginEnv);
    }

    /**
     * get
     */
    @RequestMapping("/delete")
    @ResponseBody
    public Result<String> delete(HttpServletRequest request, String key) {

        ConfUser xxlConfUser = (ConfUser) request.getAttribute(LoginConfigConstants.LOGIN_IDENTITY);
        String loginEnv = (String) request.getAttribute(CURRENT_ENV);

        return confNodeService.delete(key, xxlConfUser, loginEnv);
    }

    /**
     * create/update
     */
    @RequestMapping("/add")
    @ResponseBody
    public Result<String> add(HttpServletRequest request, ConfNode xxlConfNode) {

        ConfUser xxlConfUser = (ConfUser) request.getAttribute(LoginConfigConstants.LOGIN_IDENTITY);
        String loginEnv = (String) request.getAttribute(CURRENT_ENV);

        // fill env
        xxlConfNode.setEnv(loginEnv);

        return confNodeService.add(xxlConfNode, xxlConfUser, loginEnv);
    }

    /**
     * create/update
     */
    @RequestMapping("/update")
    @ResponseBody
    public Result<String> update(HttpServletRequest request, ConfNode xxlConfNode) {

        ConfUser xxlConfUser = (ConfUser) request.getAttribute(LoginConfigConstants.LOGIN_IDENTITY);
        String loginEnv = (String) request.getAttribute(CURRENT_ENV);

        // fill env
        xxlConfNode.setEnv(loginEnv);

        return confNodeService.update(xxlConfNode, xxlConfUser, loginEnv);
    }

	/*@RequestMapping("/syncConf")
	@ResponseBody
	public Result<String> syncConf(HttpServletRequest request,
										String appname) {

		ConfUser xxlConfUser = (ConfUser) request.getAttribute(LoginConfigConstants.LOGIN_IDENTITY);
		String loginEnv = (String) request.getAttribute(CURRENT_ENV);

		return confNodeService.syncConf(appname, xxlConfUser, loginEnv);
	}*/

    // ---------------------- rest api ----------------------
    @Value("${xxl.conf.access.token}")
    private String accessToken;

    /**
     * 配置查询 API
     *
     * 说明：查询配置数据；
     *
     * ------ 地址格式：{配置中心跟地址}/find
     *
     * 请求参数说明： 1、accessToken：请求令牌； 2、env：环境标识 3、keys：配置Key列表
     *
     * 请求数据格式如下，放置在 RequestBody 中，JSON格式：
     *
     * { "accessToken" : "xx", "env" : "xx", "keys" : [ "key01", "key02" ] }
     */
    @RequestMapping("/find")
    @ResponseBody
    @PermessionLimit(limit = false)
    public Result<Map<String, String>> find(@RequestBody(required = false) String data) {

        // parse data
        ConfParamVO confParamVO = null;
        try {
            confParamVO = (ConfParamVO) JacksonUtil.readValue(data, ConfParamVO.class);
        } catch (Exception e) {
        }

        // parse param
        String accessToken = null;
        String env = null;
        List<String> keys = null;
        if (confParamVO != null) {
            accessToken = confParamVO.getAccessToken();
            env = confParamVO.getEnv();
            keys = confParamVO.getKeys();
        }

        return confNodeService.find(accessToken, env, keys);
    }

    /**
     * 配置监控 API
     *
     * 说明：long-polling 接口，主动阻塞一段时间（默认30s）；直至阻塞超时或配置信息变动时响应；
     *
     * ------ 地址格式：{配置中心跟地址}/monitor
     *
     * 请求参数说明： 1、accessToken：请求令牌； 2、env：环境标识 3、keys：配置Key列表
     *
     * 请求数据格式如下，放置在 RequestBody 中，JSON格式：
     *
     * { "accessToken" : "xx", "env" : "xx", "keys" : [ "key01", "key02" ] }
     */
    @RequestMapping("/monitor")
    @ResponseBody
    @PermessionLimit(limit = false)
    public DeferredResult<Result<String>> monitor(@RequestBody(required = false) String data) {

        // parse data
        ConfParamVO confParamVO = null;
        try {
            confParamVO = (ConfParamVO) JacksonUtil.readValue(data, ConfParamVO.class);
        } catch (Exception e) {
        }

        // parse param
        String accessToken = null;
        String env = null;
        List<String> keys = null;
        if (confParamVO != null) {
            accessToken = confParamVO.getAccessToken();
            env = confParamVO.getEnv();
            keys = confParamVO.getKeys();
        }

        return confNodeService.monitor(accessToken, env, keys);
    }
}

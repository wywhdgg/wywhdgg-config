package com.wywhdgg.dzb.controller;

import com.wywhdgg.dzb.core.client.ConfClient;
import com.wywhdgg.dzb.core.listener.ConfListener;
import com.wywhdgg.dzb.demo.DemoConf;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author xuxueli 2018-02-04 01:27:30
 */
@Slf4j
@Controller
public class IndexController {
    static {
        /**
         * 配置变更监听示例：可开发Listener逻辑，监听配置变更事件；可据此实现动态刷新JDBC连接池等高级功能；
         */
        ConfClient.addListener("default.key01", new ConfListener() {
            @Override
            public void onChange(String key, String value) throws Exception {
                log.info("配置变更事件通知：{}={}", key, value);
            }
        });
    }

    @Resource
    private DemoConf demoConf;

    @RequestMapping("")
    @ResponseBody
    public List<String> index() {

        List<String> list = new LinkedList<>();

        /**
         * 方式1: API方式
         *
         * 		- 参考 "IndexController" 中 "ConfClient.get("key", null)" 即可；
         * 		- 用法：代码中直接调用API即可，示例代码 ""ConfClient.get("key", null)"";
         * 		- 优点：
         * 			- 配置从配置中心自动加载；
         * 			- 存在LocalCache，不用担心性能问题；；
         * 			- 支持动态推送更新；
         * 			- 支持多数据类型；
         */
        String paramByApi = ConfClient.get("default.key01", null);
        list.add("1、API方式: default.key01=" + paramByApi);

        /**
         * 方式2: @Conf 注解方式
         *
         * 		- 参考 "DemoConf.paramByAnno" 属性配置；示例代码 "@Conf("key") public String paramByAnno;"；
         * 		- 用法：对象Field上加注解 ""@Conf("default.key02")"，支持设置默认值，支持设置是否开启动态刷新；
         * 		- 优点：
         * 			- 配置从配置中心自动加载；
         * 			- 存在LocalCache，不用担心性能问题；
         * 			- 支持动态推送更新；
         * 			- 支持设置配置默认值；
         */
        list.add("2、@Conf 注解方式: default.key02=" + demoConf.paramByAnno);

        /**
         * 方式3: XML占位符方式
         *
         * 		- 参考 "applicationcontext-xxl-conf.xml" 中 "DemoConf.paramByXml" 属性配置；示例代码 "<property name="paramByXml" value="$Conf{key}" />"；
         * 		- 用法：占位符方式 "$Conf{key}"；
         * 		- 优点：
         * 			- 配置从配置中心自动加载；
         * 			- 存在LocalCache，不用担心性能问题；
         *
         */
        list.add("3、XML占位符方式: default.key03=" + demoConf.paramByXml);

        return list;
    }
}

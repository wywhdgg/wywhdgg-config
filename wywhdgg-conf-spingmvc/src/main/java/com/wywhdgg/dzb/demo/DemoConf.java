package com.wywhdgg.dzb.demo;

import com.wywhdgg.dzb.core.annotation.Conf;
import lombok.extern.slf4j.Slf4j;

/**
 *  测试示例（可删除）
 *
 *  @author xuxueli
 */
@Slf4j
public class DemoConf {

	/**
	 * XXL-CONF：@XxlConf 注解方式
	 */
	@Conf("default.key02")
	public String paramByAnno;


	/**
	 * XXL-CONF：$XxlConf{default.key03} XML占位符方式
	 */
	public String paramByXml;

	public void setParamByXml(String paramByXml) {
		this.paramByXml = paramByXml;
	}

}

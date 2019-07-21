package com.wywhdgg.dzb.entity;

import java.io.Serializable;
import java.util.List;
import lombok.Data;
import lombok.ToString;

/**
 *@author dzb
 *@date 2019/7/21 17:20
 *@Description:配置节点
 *@version 1.0
 */
@Data
@ToString
public class ConfNode implements Serializable {
	private static final long serialVersionUID = 2067112410346085734L;
	private String env;
	private String key;			// 配置Key
	private String appname; 	// 所属项目AppName
	private String title; 		// 配置描述
	private String value;		// 配置Value

	// plugin
	/*private String zkValue; 				// ZK中配置Value	// TODO, delete*/
	private List<ConfNodeLog> logList;	// 配置变更Log


}

package com.wywhdgg.dzb.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import lombok.ToString;

/***
 *@author dzb
 *@date 2019/7/21 17:20
 *@Description:
 *@version 1.0
 */
@Data
@ToString
public class ConfNodeLog implements Serializable {
	private static final long serialVersionUID = -3422237524774757075L;
	private String env;
	private String key;			// 配置Key
	private String title;		// 配置描述
	private String value;		// 配置Value
	private Date addtime;		// 操作时间
	private String optuser;		// 操作人


}

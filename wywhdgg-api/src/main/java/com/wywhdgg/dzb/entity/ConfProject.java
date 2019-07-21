package com.wywhdgg.dzb.entity;

import java.io.Serializable;
import lombok.Data;
import lombok.ToString;

/***
 *@author dzb
 *@date 2019/7/21 17:20
 *@Description:  项目配置信息
 *@version 1.0
 */
@Data
@ToString
public class ConfProject implements Serializable {
    private static final long serialVersionUID = 1414485301875915431L;
    private String appname;     // 项目AppName
    private String title;       // 项目名称



}

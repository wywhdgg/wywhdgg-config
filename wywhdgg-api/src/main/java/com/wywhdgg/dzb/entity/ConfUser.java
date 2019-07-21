package com.wywhdgg.dzb.entity;

import java.io.Serializable;
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
public class ConfUser implements Serializable {
    private static final long serialVersionUID = -4888843394701653402L;
    private String username;
    private String password;
    private int permission;             // 权限：0-普通用户、1-管理员
    private String permissionData;      // 权限配置数据, 格式 "appname#env,appname#env02"
}

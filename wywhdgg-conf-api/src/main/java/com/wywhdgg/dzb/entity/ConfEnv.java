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
public class ConfEnv  implements Serializable {
    private static final long serialVersionUID = -1120067710356065953L;
    private String env;         // Env
    private String title;       // 环境名称
    private int order;
}

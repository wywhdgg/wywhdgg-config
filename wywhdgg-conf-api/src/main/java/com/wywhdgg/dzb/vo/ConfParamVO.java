package com.wywhdgg.dzb.vo;

import java.util.List;
import lombok.Data;
import lombok.ToString;

/***
 *@author dzb
 *@date 2019/7/21 23:43
 *@Description:
 *@version 1.0
 */
@Data
@ToString
public class ConfParamVO {
    private String accessToken;
    private String env;
    private List<String> keys;
}

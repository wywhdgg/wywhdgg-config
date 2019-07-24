package com.wywhdgg.dzb.core.vo;

import java.util.List;
import lombok.Data;
import lombok.ToString;

/**
 * @author: dongzhb
 * @date: 2019/7/24
 * @Description:
 */
@Data
@ToString
public class ConfParamVO {
    private String accessToken;
    private String env;
    private List<String> keys;
}

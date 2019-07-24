package com.wywhdgg.dzb.core.cache;

import com.wywhdgg.dzb.core.exception.ConfException;
import com.wywhdgg.dzb.core.util.PropUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

/**
 * @author: dongzhb
 * @date: 2019/7/24
 * @Description:   本地文件
 */
@Slf4j
public class ConfMirror {
    private static String mirrorfile = null;

    public static void init(String mirrorfileParam) {
        // valid
        if (mirrorfileParam == null || mirrorfileParam.trim().length() == 0) {
            throw new ConfException("conf mirror mirrorfileParam can not be empty");
        }
        mirrorfile = mirrorfileParam;
    }

    /**
     * 获取本地文件内容
     */
    public static Map<String, String> readConfMirror() {
        Properties mirrorProp = PropUtil.loadFileProp(mirrorfile);
        log.info("mirrorProp={}", mirrorProp);
        if (Objects.nonNull(mirrorProp) && !CollectionUtils.isEmpty(mirrorProp.stringPropertyNames())) {
            Map<String, String> mirrorConfData = new HashMap<>();
            for (String key : mirrorProp.stringPropertyNames()) {
                mirrorConfData.put(key, mirrorProp.getProperty(key));
            }
            return mirrorConfData;
        }
        return null;
    }

    /**
     * 写入本地文件
     */
    public static void writeConfMirror(Map<String, String> mirrorConfDataParam) {
        Properties properties = new Properties();
        for (Map.Entry<String, String> confItem : mirrorConfDataParam.entrySet()) {
            properties.setProperty(confItem.getKey(), confItem.getValue());
        }
        PropUtil.writeFileProp(properties, mirrorfile);
    }
}

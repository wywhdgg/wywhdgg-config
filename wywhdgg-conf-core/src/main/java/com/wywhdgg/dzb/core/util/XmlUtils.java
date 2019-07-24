package com.wywhdgg.dzb.core.util;

import lombok.extern.slf4j.Slf4j;

/**
 * @author: dongzhb
 * @date: 2019/7/24
 * @Description:
 */
@Slf4j
public class XmlUtils {

    private static final String placeholderPrefix = "$XxlConf{";
    private static final String placeholderSuffix = "}";

    /**
     * valid xml
     *
     * @param originKey
     * @return
     */
    public static boolean xmlKeyValid(String originKey){
        boolean start = originKey.startsWith(placeholderPrefix);
        boolean end = originKey.endsWith(placeholderSuffix);
        if (start && end) {
            return true;
        }
        return false;
    }

    /**
     * parse xml
     *
     * @param originKey
     * @return
     */
    public static String xmlKeyParse(String originKey){
        if (xmlKeyValid(originKey)) {
            // replace by xxl-conf
            String key = originKey.substring(placeholderPrefix.length(), originKey.length() - placeholderSuffix.length());
            return key;
        }
        return null;
    }

}

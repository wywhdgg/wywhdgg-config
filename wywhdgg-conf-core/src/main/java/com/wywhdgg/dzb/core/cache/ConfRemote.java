package com.wywhdgg.dzb.core.cache;

import com.wywhdgg.dzb.core.exception.ConfException;
import com.wywhdgg.dzb.core.util.HttpUtil;
import com.wywhdgg.dzb.core.vo.ConfParamVO;
import com.wywhdgg.dzb.util.json.BasicJson;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

/**
 * @author: dongzhb
 * @date: 2019/7/24
 * @Description:
 */
@Slf4j
public class ConfRemote {
    private static String adminAddress;
    private static String env;
    private static String accessToken;
    private static List<String> adminAddressArr = null;

    public static void init(String adminAddress, String env, String accessToken) {
        // valid
        if (adminAddress == null || adminAddress.trim().length() == 0) {
            throw new ConfException("conf remote adminAddress can not be empty");
        }
        if (env == null || env.trim().length() == 0) {
            throw new ConfException(" conf remote env can not be empty");
        }
        ConfRemote.adminAddress = adminAddress;
        ConfRemote.env = env;
        ConfRemote.accessToken = accessToken;
        // parse
        ConfRemote.adminAddressArr = new ArrayList<>();
        if (adminAddress.contains(",")) {
            ConfRemote.adminAddressArr.add(adminAddress);
        } else {
            ConfRemote.adminAddressArr.addAll(Arrays.asList(adminAddress.split(",")));
        }
    }

    /**
     * 获取数据转换格式
     */
    private static Map<String, Object> getAndValid(String url, String requestBody, int timeout) {
        // resp json
        String respJson = HttpUtil.postBody(url, requestBody, timeout);
        if (respJson == null) {
            return null;
        }
        // parse obj
        Map<String, Object> respObj = BasicJson.parseMap(respJson);
        int code = Integer.valueOf(String.valueOf(respObj.get("code")));
        if (code != 200) {
            log.info("request fail, msg={}", (respObj.containsKey("msg") ? respObj.get("msg") : respJson));
            return null;
        }
        return respObj;
    }

    /**
     * find
     * @param keys
     */
    public static Map<String, String> find(Set<String> keys) {
        for (String adminAddressUrl : ConfRemote.adminAddressArr) {
            String url = adminAddressUrl + "/conf/find";
            ConfParamVO paramVO = new ConfParamVO();
            paramVO.setAccessToken(accessToken);
            paramVO.setEnv(env);
            paramVO.setKeys(new ArrayList<String>(keys));
            String paramsJson = BasicJson.toJson(paramVO);
            Map<String, Object> respObj = getAndValid(url, paramsJson, 5);
            // parse
            if (respObj != null && respObj.containsKey("data")) {
                Map<String, String> data = (Map<String, String>) respObj.get("data");
                return data;
            }
        }
        return null;
    }

    /**
     * find
     * @param key
     */
    public static String find(String key) {
        Map<String, String> result = find(new HashSet<String>(Arrays.asList(key)));
        if (result!=null) {
            return result.get(key);
        }
        return null;
    }

    /**
     * 监控显示
     */
    public static boolean monitor(Set<String> keys) {

        for (String adminAddressUrl : ConfRemote.adminAddressArr) {
            // url + param
            String url = adminAddressUrl + "/conf/monitor";
            ConfParamVO paramVO = new ConfParamVO();
            paramVO.setAccessToken(accessToken);
            paramVO.setEnv(env);
            paramVO.setKeys(new ArrayList<String>(keys));
            String paramsJson = BasicJson.toJson(paramVO);
            Map<String, Object> respObj = getAndValid(url, paramsJson, 60);
            return respObj != null ? true : false;
        }
        return false;
    }
}

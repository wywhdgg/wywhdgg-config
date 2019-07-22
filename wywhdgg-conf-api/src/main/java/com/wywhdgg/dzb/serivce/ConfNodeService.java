package com.wywhdgg.dzb.serivce;

import com.wywhdgg.dzb.entity.ConfNode;
import com.wywhdgg.dzb.entity.ConfUser;
import com.wywhdgg.dzb.util.Result;
import java.util.List;
import java.util.Map;
import org.springframework.web.context.request.async.DeferredResult;

/***
 *@author dzb
 *@date 2019/7/21 22:59
 *@Description:
 *@version 1.0
 */
public interface ConfNodeService {
    public boolean ifHasProjectPermission(ConfUser loginUser, String loginEnv, String appname);

    public int pageListCount(int offset, int pagesize, String env, String appname, String key);

    public Map<String, Object> pageList(int offset, int pagesize, String appname, String key, ConfUser loginUser, String loginEnv);

    public Result<String> delete(String key, ConfUser loginUser, String loginEnv);

    public Result<String> add(ConfNode xxlConfNode, ConfUser loginUser, String loginEnv);

    public Result<String> update(ConfNode xxlConfNode, ConfUser loginUser, String loginEnv);

    /*Result<String> syncConf(String appname, ConfUser loginUser, String loginEnv);*/

    // ---------------------- rest api ----------------------

    public Result<Map<String, String>> find(String accessToken, String env, List<String> keys);

    public DeferredResult<Result<String>> monitor(String accessToken, String env, List<String> keys);
}

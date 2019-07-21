package com.wywhdgg.dzb.dao;

import com.wywhdgg.dzb.entity.ConfNode;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/***
 *@author dzb
 *@date 2019/7/21 20:46
 *@Description: 配置节点
 *@version 1.0
 */
public interface ConfNodeDao {
    public List<ConfNode> pageList(@Param("offset") int offset, @Param("pagesize") int pagesize, @Param("env") String env, @Param("appname") String appname, @Param("key") String key);

    public int pageListCount(@Param("offset") int offset, @Param("pagesize") int pagesize, @Param("env") String env, @Param("appname") String appname, @Param("key") String key);

    public int delete(@Param("env") String env, @Param("key") String key);

    public void insert(ConfNode xxlConfNode);

    public ConfNode load(@Param("env") String env, @Param("key") String key);

    public int update(ConfNode xxlConfNode);
}

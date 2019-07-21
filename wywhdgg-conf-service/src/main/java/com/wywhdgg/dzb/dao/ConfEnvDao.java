package com.wywhdgg.dzb.dao;

import com.wywhdgg.dzb.entity.ConfEnv;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/***
 *@author dzb
 *@date 2019/7/21 20:46
 *@Description: 配置环境
 *@version 1.0
 */
public interface ConfEnvDao {
    public List<ConfEnv> findAll();

    public int save(ConfEnv xxlConfEnv);

    public int update(ConfEnv xxlConfEnv);

    public int delete(@Param("env") String env);

    public ConfEnv load(@Param("env") String env);
}
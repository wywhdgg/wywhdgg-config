package com.wywhdgg.dzb.dao;

import com.wywhdgg.dzb.entity.ConfNodeLog;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/***
 *@author dzb
 *@date 2019/7/21 20:46
 *@Description:
 *@version 1.0
 */
@Mapper
public interface ConfNodeLogDao {

	public List<ConfNodeLog> findByKey(@Param("env") String env, @Param("key") String key);

	public void add(ConfNodeLog xxlConfNode);

	public int deleteTimeout(@Param("env") String env, @Param("key") String key, @Param("length") int length);

}

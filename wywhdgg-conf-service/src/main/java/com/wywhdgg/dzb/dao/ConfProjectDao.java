package com.wywhdgg.dzb.dao;

import com.wywhdgg.dzb.entity.ConfProject;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/***
 *@author dzb
 *@date 2019/7/21 20:46
 *@Description:
 *@version 1.0
 */
public interface ConfProjectDao {

    public List<ConfProject> findAll();

    public int save(ConfProject xxlConfProject);

    public int update(ConfProject xxlConfProject);

    public int delete(@Param("appname") String appname);

    public ConfProject load(@Param("appname") String appname);

}
package com.wywhdgg.dzb.serivce;

import com.wywhdgg.dzb.entity.ConfProject;
import java.util.List;
import org.jboss.logging.Param;

/***
 *@author dzb
 *@date 2019/7/21 23:40
 *@Description:
 *@version 1.0
 */
public interface ConfProjectService {
    public List<ConfProject> findAll();

    public int save(ConfProject xxlConfProject);

    public int update(ConfProject xxlConfProject);

    public int delete( String appname);

    public ConfProject load( String appname);
}

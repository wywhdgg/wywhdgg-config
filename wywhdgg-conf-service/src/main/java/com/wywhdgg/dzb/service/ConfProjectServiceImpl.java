package com.wywhdgg.dzb.service;

import com.wywhdgg.dzb.dao.ConfProjectDao;
import com.wywhdgg.dzb.entity.ConfProject;
import com.wywhdgg.dzb.serivce.ConfProjectService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

/***
 *@author dzb
 *@date 2019/7/21 23:41
 *@Description:
 *@version 1.0
 */
public class ConfProjectServiceImpl  implements ConfProjectService {
    @Autowired
    private ConfProjectDao confProjectDao;
    @Override
    public List<ConfProject> findAll() {
        return confProjectDao.findAll();
    }
}

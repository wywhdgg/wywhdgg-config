package com.wywhdgg.dzb.service.impl;

import com.wywhdgg.dzb.dao.ConfProjectDao;
import com.wywhdgg.dzb.entity.ConfProject;
import com.wywhdgg.dzb.serivce.ConfProjectService;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/***
 *@author dzb
 *@date 2019/7/21 23:41
 *@Description:
 *@version 1.0
 */
@Slf4j
@Service
public class ConfProjectServiceImpl  implements ConfProjectService {
    @Autowired
    private ConfProjectDao confProjectDao;
    @Override
    public List<ConfProject> findAll() {
        return confProjectDao.findAll();
    }

    @Override
    public int save(ConfProject confProject) {
        return confProjectDao.save(confProject);
    }

    @Override
    public int update(ConfProject confProject) {
        return confProjectDao.update(confProject);
    }

    @Override
    public int delete(String appName) {
        return confProjectDao.delete(appName);
    }

    @Override
    public ConfProject load(String appName) {
        return confProjectDao.load(appName);
    }
}

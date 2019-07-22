package com.wywhdgg.dzb.service.impl;

import com.wywhdgg.dzb.dao.ConfEnvDao;
import com.wywhdgg.dzb.entity.ConfEnv;
import com.wywhdgg.dzb.serivce.ConfEnvService;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/***
 *@author dzb
 *@date 2019/7/22 22:49
 *@Description:
 *@version 1.0
 */
@Slf4j
@Service
public class ConfEnvServiceImpl implements ConfEnvService {

    @Autowired
    private ConfEnvDao confEnvDao;

    @Override
    public List<ConfEnv> findAll() {
        return confEnvDao.findAll();
    }

    @Override
    public int save(ConfEnv confEnv) {
        return confEnvDao.save(confEnv);
    }

    @Override
    public int update(ConfEnv confEnv) {
        return confEnvDao.update(confEnv);
    }

    @Override
    public int delete(String env) {
        return confEnvDao.delete(env);
    }

    @Override
    public ConfEnv load(String env) {
        return confEnvDao.load(env);
    }
}

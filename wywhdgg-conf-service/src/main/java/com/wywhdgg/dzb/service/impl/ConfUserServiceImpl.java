package com.wywhdgg.dzb.service.impl;

import com.wywhdgg.dzb.dao.ConfUserDao;
import com.wywhdgg.dzb.entity.ConfUser;
import com.wywhdgg.dzb.serivce.ConfUserService;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/***
 *@author dzb
 *@date 2019/7/21 22:41
 *@Description:
 *@version 1.0
 */
@Slf4j
@Service
public class ConfUserServiceImpl implements ConfUserService {
    @Autowired
    private ConfUserDao confUserDao;

    @Override
    public ConfUser findNameByConfUser(String usernameParam) {
        ConfUser confUser = confUserDao.load(usernameParam);
        if (Objects.isNull(confUser)) {
            return null;
        }
        return confUser;
    }

    @Override
    public List<ConfUser> pageList(int offset, int pagesize, String userName, int permission) {
        return confUserDao.pageList(offset,  pagesize,  userName,  permission);
    }

    @Override
    public int pageListCount(int offset, int pagesize, String userName, int permission) {
        return confUserDao.pageListCount(offset,  pagesize,  userName,  permission);
    }

    @Override
    public int add(ConfUser confUser) {
        return confUserDao.add(confUser);
    }

    @Override
    public int update(ConfUser confUser) {
        return confUserDao.update(confUser);
    }

    @Override
    public int delete(String userName) {
        return confUserDao.delete(userName);
    }

    @Override
    public ConfUser load(String userName) {
        return confUserDao.load(userName);
    }
}

package com.wywhdgg.dzb.service;

import com.wywhdgg.dzb.dao.ConfUserDao;
import com.wywhdgg.dzb.entity.ConfUser;
import com.wywhdgg.dzb.serivce.ConfUserService;
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
}

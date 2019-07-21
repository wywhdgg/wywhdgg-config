package com.wywhdgg.dzb;

import com.wywhdgg.dzb.dao.ConfEnvDao;
import com.wywhdgg.dzb.entity.ConfEnv;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class WywhdggConfAdminApplicationTests {

    @Autowired
    private ConfEnvDao confEnvDao;

    @Test
    public void findAll(){
        List<ConfEnv> list = confEnvDao.findAll();
        log.info("result={}",list);
    }
}

package com.wywhdgg.dzb;

import com.wywhdgg.dzb.core.client.ConfClient;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class WywhdggConfSpingbootApplicationTests {
    @Test
    public void contextLoads() {
        String value = ConfClient.get("default.key01");
        log.info("value={}", value);
    }
}

package com.wywhdgg.dzb;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.wywhdgg.dzb.dao")
public class WywhdggConfAdminApplication {
    public static void main(String[] args) {
        SpringApplication.run(WywhdggConfAdminApplication.class, args);
    }
}

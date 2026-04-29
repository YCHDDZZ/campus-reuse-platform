package com.campus.reuse;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
@MapperScan("com.campus.reuse.mapper")
public class CampusReuseApplication {
    public static void main(String[] args) {
        SpringApplication.run(CampusReuseApplication.class, args);
    }
}

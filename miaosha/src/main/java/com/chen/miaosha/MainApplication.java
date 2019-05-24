package com.chen.miaosha;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class MainApplication extends SpringBootServletInitializer {
    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
    }

    // 项目打成war时，继承 SpringBootServletInitializer，并重写  configure() 方法
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(MainApplication.class);
    }
}

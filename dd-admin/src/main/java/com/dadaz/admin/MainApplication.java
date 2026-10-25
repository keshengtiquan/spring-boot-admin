package com.dadaz.admin;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigurationExcludeFilter;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.TypeExcludeFilter;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;


@SpringBootApplication
@ComponentScan(MainApplication.COMPONENT_SCAN)
@MapperScan(value = MainApplication.COMPONENT_SCAN, annotationClass = Mapper.class)
public class MainApplication {

    /**
     * 统一包扫描根路径：同时覆盖 {@code com.dadaz.base}（基础设施）与 {@code com.dadaz.admin}（业务）
     */
    public static final String COMPONENT_SCAN = "com.dadaz";

    public static void main(String[] args) {
        // 必须透传 args，否则 --spring.profiles.active=prod、--server.port=xxx 等启动参数全部失效
        SpringApplication.run(MainApplication.class, args);
    }
}

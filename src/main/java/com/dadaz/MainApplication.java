package com.dadaz;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
@MapperScan(value = "com.dadaz", annotationClass = Mapper.class)
public class MainApplication
{
    public static void main( String[] args )
    {
        // 必须透传 args，否则 --spring.profiles.active=prod、--server.port=xxx 等启动参数全部失效
        SpringApplication.run(MainApplication.class, args);
    }
}

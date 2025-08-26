package com.picturebackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 图片管理系统后端主应用类
 * 负责启动Spring Boot应用程序
 */
@SpringBootApplication
@MapperScan("com.picturebackend.mapper")
@EnableAsync
@EnableAspectJAutoProxy(exposeProxy = true)
public class PictureBackendApplication {

    /**
     * 应用程序主入口方�?     * @param args 命令行参�?     */
    public static void main(String[] args) {
        SpringApplication.run(PictureBackendApplication.class, args);
    }

}

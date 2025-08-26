package com.px.picturebackend.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * packageName: com.px.picturebackend.config
 *
 * @author: idpeng
 * @version: 1.0
 * @className: MyBatisPlusConfig
 * @date: 2025/7/9 23:46
 * @description: mybatispuls 分页插件
 */
@Configuration
@MapperScan("com.px.picturebackend.mapper")
public class MyBatisPlusConfig {
    /**
     * 分页插件 
     *
     * @return {@link MybatisPlusInterceptor}
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 分页插件
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}


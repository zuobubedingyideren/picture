package com.picturebackend.manager.auth.annotation;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.strategy.SaAnnotationStrategy;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.PostConstruct;

/**
 * packageName: com.picturebackend.manager.auth.annotation
 *
 * @author: idpeng
 * @version: 1.0
 * @className: SaTokenConfigure
 * @date: 2025/8/26 11:35
 * @description: 新建 Sa-Token 配置类，开启注解鉴权和注解合并
 */
@Configuration
public class SaTokenConfigure implements WebMvcConfigurer {
    /**
     * 添加拦截器到Spring MVC注册表中
     * 注册Sa-Token拦截器以启用注解式鉴权功�?
     *
     * @param registry 拦截器注册表对象
     */
    // 注册 Sa-Token 拦截器，打开注解式鉴权功�?
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 Sa-Token 拦截器，打开注解式鉴权功�?
        registry.addInterceptor(new SaInterceptor()).addPathPatterns("/**");
    }

    /**
     * 重写Sa-Token的注解处理策�?
     * 通过@PostConstruct注解在Spring容器初始化时执行，增加注解合并功�?
     */
    @PostConstruct
    public void rewriteSaStrategy() {
        // 重写Sa-Token的注解处理器，增加注解合并功�?
        SaAnnotationStrategy.instance.getAnnotation = AnnotatedElementUtils::getMergedAnnotation;
    }
}

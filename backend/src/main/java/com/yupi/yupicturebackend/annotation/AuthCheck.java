package com.yupi.yupicturebackend.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * packageName: com.yupi.yupicturebackend.annotation
 *
 * @author: idpeng
 * @version: 1.0
 * @annotationTypeName: AuthCheck
 * @date: 2025/7/9 22:37
 * @description: 权限校验注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthCheck {
    /**
     * 必须有某个角色
     */
    String mustRole() default "";

}

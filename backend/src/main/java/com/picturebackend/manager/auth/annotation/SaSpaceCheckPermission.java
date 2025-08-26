package com.picturebackend.manager.auth.annotation;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import com.picturebackend.manager.auth.StpKit;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * packageName: com.picturebackend.manager.auth.annotation
 *
 * @author: idpeng
 * @version: 1.0
 * @className: SaSpaceCheckPermission
 * @date: 2025/8/26 11:41
 * @description: 必须具有指定权限才能进入该方�?
 */
@SaCheckPermission(type = StpKit.SPACE_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface SaSpaceCheckPermission {
    /**
     * 需要校验的权限�?
     *
     * @return 需要校验的权限�?
     */
    @AliasFor(annotation = SaCheckPermission.class)
    String[] value() default {};

    /**
     * 验证模式：AND | OR，默认AND
     *
     * @return 验证模式
     */
    @AliasFor(annotation = SaCheckPermission.class)
    SaMode mode() default SaMode.AND;

    /**
     * 在权限校验不通过时的次要选择，两者只要其一校验成功即可通过校验
     *
     * <p>
     * �?：@SaCheckPermission(value="user-add", orRole="admin")�?
     * 代表本次请求只要具有 user-add权限 �?admin角色 其一即可通过校验�?
     * </p>
     *
     * <p>
     * �?�?orRole = {"admin", "manager", "staff"}，具有三个角色其一即可�?<br>
     * �?�?orRole = {"admin, manager, staff"}，必须三个角色同时具备�?
     * </p>
     *
     * @return /
     */
    @AliasFor(annotation = SaCheckPermission.class)
    String[] orRole() default {};

}

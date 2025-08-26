package com.picturebackend.aop;

import com.picturebackend.annotation.AuthCheck;
import com.picturebackend.exception.BusinessException;
import com.picturebackend.exception.ErrorCode;
import com.picturebackend.model.entity.User;
import com.picturebackend.model.enums.UserRoleEnum;
import com.picturebackend.service.UserService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.condition.RequestConditionHolder;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * packageName: com.picturebackend.aop
 *
 * @author: idpeng
 * @version: 1.0
 * @className: AuthInterceptor
 * @date: 2025/7/9 22:40
 * @description: 权限校验切面
 */
@Aspect
@Component
public class AuthInterceptor {
    @Resource
    private UserService userService;

    /**
     * 权限校验环绕拦截�?
     * 对标记了@AuthCheck注解的方法进行权限校验，根据用户角色判断是否允许访问
     *
     * @param joinPoint 连接点对象，包含被拦截方法的信息
     * @param authCheck 权限校验注解，包含必须的角色信息
     * @return 被拦截方法的执行结果
     * @throws Throwable 方法执行异常或权限校验失败时抛出的异�?
     */
    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        String mustRole = authCheck.mustRole();
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        // 当前登录用户
        User loginUser = userService.getLoginUser(request);
        UserRoleEnum mustRoleEnum = UserRoleEnum.getEnumByValue(mustRole);

        // 不需要权�?
        if (mustRoleEnum == null) {
            return joinPoint.proceed();
        }
        // 以下为：必须有该权限才通过
        // 获取当前用户具有的权�?
        UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(loginUser.getUserRole());
        // 没有权限，拒�?
        if (userRoleEnum == null) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 要求必须有管理员权限，但用户没有管理员权限，拒绝
        if (UserRoleEnum.ADMIN.equals(mustRoleEnum) && !UserRoleEnum.ADMIN.equals(userRoleEnum)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 通过权限校验，放�?
        return joinPoint.proceed();

    }
}

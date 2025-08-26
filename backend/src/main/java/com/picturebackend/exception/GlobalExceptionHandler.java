package com.picturebackend.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import com.picturebackend.common.BaseResponse;
import com.picturebackend.common.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * packageName: com.picturebackend.exception
 *
 * @author: idpeng
 * @version: 1.0
 * @className: GlobalExceptionHandler
 * @date: 2025/7/7 22:14
 * @description: 利用aop切面对业务异常类和RuntimeException进行捕获
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     * 
     * @param e 业务异常对象
     * @return 包含错误码和错误信息的统一响应结果
     */
    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> businessExceptionHandler(BusinessException e) {
        log.error("BusinessException", e);
        return ResultUtils.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理运行时异�?
     * 
     * @param e 运行时异常对�?
     * @return 包含系统错误码和错误信息的统一响应结果
     */
    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<?> runtimeExceptionHandler(RuntimeException e) {
        log.error("RuntimeException", e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统错误");
    }

    /**
     * 处理未登录异�?
     * 
     * @param e 未登录异常对�?
     * @return 包含未登录错误码和错误信息的统一响应结果
     */
    @ExceptionHandler(NotLoginException.class)
    public BaseResponse<?> notLoginException(NotLoginException e) {
        log.error("NotLoginException", e);
        return ResultUtils.error(ErrorCode.NOT_LOGIN_ERROR, e.getMessage());
    }

    /**
     * 处理无权限异�?
     * 
     * @param e 无权限异常对�?
     * @return 包含无权限错误码和错误信息的统一响应结果
     */
    @ExceptionHandler(NotPermissionException.class)
    public BaseResponse<?> notPermissionExceptionHandler(NotPermissionException e) {
        log.error("NotPermissionException", e);
        return ResultUtils.error(ErrorCode.NO_AUTH_ERROR, e.getMessage());
    }

}

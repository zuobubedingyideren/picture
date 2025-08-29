package com.px.picturebackend.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import com.px.picturebackend.common.BaseResponse;
import com.px.picturebackend.common.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * packageName: com.px.picturebackend.exception
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
     * 根据错误码返回相应的HTTP状态码
     * 
     * @param e 业务异常对象
     * @return 包含错误码和错误信息的统一响应结果
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<BaseResponse<?>> businessExceptionHandler(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();
        
        // 根据错误码确定HTTP状态码
        HttpStatus status = determineHttpStatus(errorCode);
        
        // 根据错误类型调整日志级别
        if (isClientError(errorCode)) {
            log.warn("业务异常: code={}, message={}", errorCode.getCode(), e.getMessage());
        } else {
            log.error("业务异常: code={}, message={}", errorCode.getCode(), e.getMessage(), e);
        }
        
        return ResponseEntity.status(status)
            .body(ResultUtils.error(errorCode, e.getMessage()));
    }
    
    /**
     * 根据错误码确定HTTP状态码
     * 
     * @param errorCode 错误码枚举
     * @return HTTP状态码
     */
    private HttpStatus determineHttpStatus(ErrorCode errorCode) {
        if (errorCode == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        
        int code = errorCode.getCode();
        
        // 根据错误码确定HTTP状态码
        return switch (code / 100) {
            case 400 -> HttpStatus.BAD_REQUEST;  // 参数错误：400
            case 401 -> HttpStatus.UNAUTHORIZED;  // 未授权：401
            case 403 -> HttpStatus.FORBIDDEN;  // 禁止访问：403
            case 404 -> HttpStatus.NOT_FOUND;  // 资源不存在：404
            case 429 -> HttpStatus.TOO_MANY_REQUESTS;  // 请求过于频繁：429
            case 500 -> HttpStatus.INTERNAL_SERVER_ERROR;  // 系统错误：500
            default -> HttpStatus.INTERNAL_SERVER_ERROR;  // 默认：500
        };
    }
    
    /**
     * 判断是否为客户端错误（4xx）
     * 
     * @param errorCode 错误码枚举
     * @return 是否为客户端错误
     */
    private boolean isClientError(ErrorCode errorCode) {
        if (errorCode == null) {
            return false;
        }
        
        int code = errorCode.getCode();
        
        // 4xx错误码都属于客户端错误
        return code >= 40000 && code < 50000;
    }

    /**
     * 处理运行时异常
     * 
     * @param e 运行时异常对象
     * @return 包含系统错误码和错误信息的统一响应结果
     */
    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<?> runtimeExceptionHandler(RuntimeException e) {
        log.error("RuntimeException", e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统错误");
    }

    /**
     * 处理未登录异常
     * 
     * @param e 未登录异常对象
     * @return 包含未登录错误码和错误信息的统一响应结果
     */
    @ExceptionHandler(NotLoginException.class)
    public BaseResponse<?> notLoginException(NotLoginException e) {
        log.error("NotLoginException", e);
        return ResultUtils.error(ErrorCode.NOT_LOGIN_ERROR, e.getMessage());
    }

    /**
     * 处理无权限异常
     * 
     * @param e 无权限异常对象
     * @return 包含无权限错误码和错误信息的统一响应结果
     */
    @ExceptionHandler(NotPermissionException.class)
    public BaseResponse<?> notPermissionExceptionHandler(NotPermissionException e) {
        log.error("NotPermissionException", e);
        return ResultUtils.error(ErrorCode.NO_AUTH_ERROR, e.getMessage());
    }

}

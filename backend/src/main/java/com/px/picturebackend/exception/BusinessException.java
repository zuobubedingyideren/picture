package com.px.picturebackend.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * packageName: com.px.picturebackend.exception
 *
 * @author: idpeng
 * @version: 1.0
 * @className: BusinessException
 * @date: 2025/7/7 21:48
 * @description: 业务异常类，统一处理业务逻辑异常
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public class BusinessException extends RuntimeException{
    /**
     * 错误码
     */
    private final int code;
    
    /**
     * 错误码枚举
     */
    private final ErrorCode errorCode;
    
    /**
     * 格式化参数
     */
    private final Object[] args;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
        this.errorCode = null;
        this.args = null;
    }

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.errorCode = errorCode;
        this.args = null;
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
        this.errorCode = errorCode;
        this.args = null;
    }
    
    /**
     * 支持参数化消息的构造函数
     * @param errorCode 错误码枚举
     * @param args 格式化参数
     */
    public BusinessException(ErrorCode errorCode, Object... args) {
        super(String.format(errorCode.getMessage(), args));
        this.code = errorCode.getCode();
        this.errorCode = errorCode;
        this.args = args;
    }
}

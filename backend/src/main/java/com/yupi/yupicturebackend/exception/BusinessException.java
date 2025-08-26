package com.yupi.yupicturebackend.exception;

import lombok.Getter;

/**
 * packageName: com.yupi.yupicturebackend.exception
 *
 * @author: idpeng
 * @version: 1.0
 * @className: BusinessException
 * @date: 2025/7/7 21:48
 * @description: 自定义异常类
 */
@Getter
public class BusinessException extends RuntimeException{
    /**
     * 错误码
     */
    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }
}

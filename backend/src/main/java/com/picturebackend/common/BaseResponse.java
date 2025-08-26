package com.picturebackend.common;

import com.picturebackend.exception.ErrorCode;
import lombok.Getter;

/**
 * packageName: com.picturebackend.common
 *
 * @author: idpeng
 * @version: 1.0
 * @className: BaseResponse
 * @date: 2025/7/7 21:56
 * @description: 统一的响应结果类
 */
@Getter
public class BaseResponse<T> {
    private int code;
    private T data;
    private String message;

    public BaseResponse(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public BaseResponse(int code, T data) {
        this(code, data, "");
    }

    public BaseResponse (ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMessage());
    }
}

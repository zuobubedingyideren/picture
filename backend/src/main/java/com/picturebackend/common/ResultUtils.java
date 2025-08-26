package com.picturebackend.common;

import com.picturebackend.exception.ErrorCode;

/**
 * packageName: com.picturebackend.common
 *
 * @author: idpeng
 * @version: 1.0
 * @className: ResultUtils
 * @date: 2025/7/7 22:01
 * @description: 提供成功调用和失败调用的方法，支持灵活传参，简化调�?
 */
public class ResultUtils {

    /**
     * 成功
     * @param data 数据
     * @return 响应
     * @param <T> 数据类型
     */
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<T>(0, data, "ok");
    }


    /**
     *  失败
     * @param errorCode 错误�?
     * @return 响应
     */
    public static BaseResponse<?> error(ErrorCode errorCode) {
        return new BaseResponse<>(errorCode);
    }

    /**
     * 失败
     * @param code 错误�?
     * @param message 错误信息
     * @return 响应
     */
    public static BaseResponse<?> error(int code, String message) {
        return new BaseResponse<>(code, null, message);
    }

    /**
     * 失败
     * @param errorCode 错误�?
     * @param message 错误信息
     * @return 响应
     */
    public static BaseResponse<?> error(ErrorCode errorCode, String message) {
        return new BaseResponse<>(errorCode.getCode(), null, message);
    }
}

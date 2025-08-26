package com.yupi.yupicturebackend.exception;

import lombok.Getter;

/**
 * packageName: com.yupi.yupicturebackend.exception
 *
 * @author: idpeng
 * @version: 1.0
 * @enumName: ErrorCode
 * @date: 2025/7/7 21:43
 * @description: 定义错误码
 * 自定义错误码时，建议跟主流的错误码（比如 HTTP 错误码）的含义保持一致，比如 “未登录” 定义为 40100，和 HTTP 401 错误（用户需要进行身份认证）保持一致，会更容易理解。
 * 错误码不要完全连续，预留一些间隔，便于后续扩展。
 */
@Getter
public enum ErrorCode {
    SUCCESS(0, "OK"),
    PARAMS_ERROR(40000, "请求参数错误"),
    NULL_ERROR(40001, "请求数据为空"),
    NOT_LOGIN_ERROR(40100, "未登录"),
    NO_AUTH_ERROR(40101, "无权限"),
    NOT_FOUND_ERROR(40400, "请求数据不存在"),
    FORBIDDEN_ERROR(40300, "禁止访问"),
    SYSTEM_ERROR(50000, "系统内部异常"),
    OPERATION_ERROR(50001, "操作失败");

    /**
     * 状态码
     */
    private final int code;

    /**
     * 状态码信息
     */
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}

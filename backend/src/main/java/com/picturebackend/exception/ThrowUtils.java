package com.picturebackend.exception;

/**
 * packageName: com.picturebackend.exception
 *
 * @author: idpeng
 * @version: 1.0
 * @className: ThrowUtils
 * @date: 2025/7/7 21:51
 * @description: 封装抛异常的工具�?
 */
public class ThrowUtils {

    /**
     * 根据给定条件抛出异常
     * 此方法用于简化条件判断和异常抛出的逻辑，使调用处代码更清晰
     *
     * @param condition    决定是否抛出异常的条�?
     * @param runtimeException   如果条件为真时要抛出的异常实�?
     */
    public static void throwIf(boolean condition, RuntimeException runtimeException) {
        // 当条件为真时，抛出提供的异常
        if (condition) {
            throw runtimeException;
        }
    }

    /**
     * 根据给定条件抛出异常
     * 此方法用于简化条件判断和异常抛出的逻辑，使调用处代码更清晰
     *
     * @param condition    决定是否抛出异常的条�?
     * @param errorCode   如果条件为真时要抛出的异常码
     */
    public static void throwIf(boolean condition, ErrorCode errorCode) {
        throwIf(condition, new BusinessException(errorCode));
    }

    public static void throwIf(boolean condition, ErrorCode errorCode, String message) {
        throwIf(condition, new BusinessException(errorCode, message));
    }
}

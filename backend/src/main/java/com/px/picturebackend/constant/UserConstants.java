package com.px.picturebackend.constant;

import java.util.Arrays;
import java.util.List;

/**
 * 用户相关常量类
 * 集中管理用户服务中使用的所有常量值，提高代码的可维护性和可读性
 *
 * @author idpeng
 * @since 2025-01-01
 */
public class UserConstants {

    /**
     * 密码加密盐值
     * 用于用户密码MD5加密时的盐值，增强密码安全性
     */
    public static final String PASSWORD_SALT = "tiantianxiangshang";

    // 默认密码 12345678
    public static final String DEFAULT_PASSWORD = "12345678";

    /**
     * 用户账号最小长度
     * 用户注册和登录时账号的最小字符长度限制
     */
    public static final int MIN_ACCOUNT_LENGTH = 4;

    /**
     * 用户密码最小长度
     * 用户注册和登录时密码的最小字符长度限制
     */
    public static final int MIN_PASSWORD_LENGTH = 8;

    /**
     * 新用户默认用户名
     * 用户注册时的默认用户名
     */
    public static final String DEFAULT_USER_NAME = "新用户";

    /**
     * 头像文件最大大小（字节）
     * 用户上传头像时的文件大小限制，单位为字节（5MB）
     */
    public static final long MAX_AVATAR_SIZE = 5 * 1024 * 1024;

    /**
     * 头像文件最大大小描述
     * 用于错误提示信息的文件大小描述
     */
    public static final String MAX_AVATAR_SIZE_DESC = "5MB";

    /**
     * 支持的头像文件扩展名列表
     * 用户上传头像时允许的文件格式
     */
    public static final List<String> ALLOWED_AVATAR_EXTENSIONS = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "bmp", "webp"
    );

    /**
     * 头像上传路径前缀
     * 头像文件在对象存储中的路径前缀
     */
    public static final String AVATAR_UPLOAD_PATH_PREFIX = "/avatar/";

    /**
     * 临时文件前缀
     * 创建临时头像文件时使用的文件名前缀
     */
    public static final String TEMP_AVATAR_PREFIX = "avatar_";

    /**
     * 排序方式：升序
     * 用于查询条件中的排序方式标识
     */
    public static final String SORT_ORDER_ASC = "ascend";

    /**
     * 排序方式：降序
     * 用于查询条件中的排序方式标识
     */
    public static final String SORT_ORDER_DESC = "descend";

    /**
     * 允许的排序字段集合
     * 用于排序字段白名单校验，防止SQL注入
     */
    public static final List<String> ALLOWED_SORT_FIELDS = Arrays.asList(
            "id", "createTime", "updateTime", "userName", "userAccount"
    );

    /**
     * 随机字符串长度
     * 生成唯一文件名时使用的随机字符串长度
     */
    public static final int RANDOM_STRING_LENGTH = 16;

    /**
     * 私有构造函数，防止实例化
     * 常量类不应该被实例化
     */
    private UserConstants() {
        throw new UnsupportedOperationException("常量类不能被实例化");
    }
}
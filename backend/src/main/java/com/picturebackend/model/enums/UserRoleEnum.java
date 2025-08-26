package com.picturebackend.model.enums;

import cn.hutool.core.util.ObjectUtil;
import lombok.Getter;

/**
 * packageName: com.picturebackend.model.enums
 *
 * @author: idpeng
 * @version: 1.0
 * @enumName: UserRoleEnum
 * @date: 2025/7/9 19:24
 * @description: 用户权限
 */
@Getter
public enum UserRoleEnum {
    USER("用户", "user"),
    ADMIN("管理�?, "admin");

    private final String text;
    private final String value;

    UserRoleEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据字符串值获取对应的用户角色枚举
     *
     * @param value 用户角色的字符串表示
     * @return 如果找到对应的角色枚举则返回，否则返回null
     */
    public static UserRoleEnum getEnumByValue(String value) {
        // 检查输入值是否为空，为空则直接返回null
        if (ObjectUtil.isEmpty(value)) {
            return null;
        }
        // 遍历UserRoleEnum的所有可能�?
        for (UserRoleEnum anEnum : UserRoleEnum.values()) {
            // 检查当前枚举值是否与输入值相�?
            if (anEnum.value.equals(value)) {
                // 如果相等，返回当前枚举�?
                return anEnum;
            }
        }
        // 如果没有找到匹配的枚举值，返回null
        return null;
    }
}

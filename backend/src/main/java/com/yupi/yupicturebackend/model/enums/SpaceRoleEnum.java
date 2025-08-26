package com.yupi.yupicturebackend.model.enums;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * packageName: com.yupi.yupicturebackend.model.enums
 *
 * @author: idpeng
 * @version: 1.0
 * @enumName: SpaceRoleEnum
 * @date: 2025/8/25 20:11
 * @description: 空间角色枚举
 */
@Getter
public enum SpaceRoleEnum {
    VIEWER("浏览者", "viewer"),
    EDITOR("编辑者", "editor"),
    ADMIN("管理员", "admin");

    private final String text;

    private final String value;

    SpaceRoleEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据value值获取对应的枚举实例
     *
     * @param value 枚举的value值
     * @return 匹配的枚举实例，如果没有匹配项则返回null
     */
    public static SpaceRoleEnum getEnumByValue(String value) {
        if (StrUtil.isBlank(value)) {
            return null;
        }
        for (SpaceRoleEnum spaceRoleEnum : SpaceRoleEnum.values()) {
            if (spaceRoleEnum.value.equals(value)) {
                return spaceRoleEnum;
            }
        }
        return null;
    }

    /**
     * 获取所有枚举项的文本描述列表
     *
     * @return 包含所有枚举项文本描述的列表
     */
    public static List<String> getAllTexts() {
        return Arrays.stream(SpaceRoleEnum.values())
                .map(SpaceRoleEnum::getText)
                .toList();
    }

    /**
     * 获取所有枚举项的值列表
     *
     * @return 包含所有枚举项值的列表
     */
    public static List<String> getAllValues() {
        return Arrays.stream(SpaceRoleEnum.values())
                .map(SpaceRoleEnum::getValue)
                .toList();
    }
}

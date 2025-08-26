package com.picturebackend.model.enums;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import lombok.Getter;

/**
 * packageName: com.picturebackend.model.enums
 *
 * @author: idpeng
 * @version: 1.0
 * @enumName: SpaceTypeEnum
 * @date: 2025/8/25 19:35
 * @description: 空间类型枚举
 */
@Getter
public enum SpaceTypeEnum {

    PRIVATE("私有空间", 0),
    TEAM("团队空间", 1);

    private final String text;

    private final int value;

    SpaceTypeEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }
    /**
     * 根据枚举值获取对应的SpaceTypeEnum枚举实例
     *
     * @param value 枚举�?
     * @return 对应的SpaceTypeEnum枚举实例，如果未找到则返回null
     */
    public static SpaceTypeEnum getEnumByValue(Integer value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (SpaceTypeEnum spaceTypeEnum : SpaceTypeEnum.values()) {
            if (spaceTypeEnum.value == value) {
                return spaceTypeEnum;
            }
        }
        return null;
    }
}

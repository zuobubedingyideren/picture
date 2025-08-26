package com.yupi.yupicturebackend.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * packageName: com.yupi.yupicturebackend.model.enums
 *
 * @author: idpeng
 * @version: 1.0
 * @enumName: SpaceLevelEnum
 * @date: 2025/8/22 15:26
 * @description: 空间级别枚举，定义每个级别的空间对应的限额
 */
@Getter
public enum SpaceLevelEnum {
    COMMON("普通版", 0, 100, 100L * 1024 * 1024),
    PROFESSIONAL("专业版", 1, 1000, 1000L * 1024 * 1024),
    FLAGSHIP("旗舰版", 2, 10000, 10000L * 1024 * 1024);

    private final String text;

    private final int value;

    private final long maxCount;

    private final long maxSize;

    /**
     * 空间级别枚举构造函数
     *
     * @param text     空间级别描述文本
     * @param value    空间级别值
     * @param maxCount 最大文件数量限额
     * @param maxSize  最大存储空间大小（单位：字节）
     */
    SpaceLevelEnum(String text, int value, long maxCount, long maxSize) {
        this.text = text;
        this.value = value;
        this.maxCount = maxCount;
        this.maxSize = maxSize;
    }

    /**
     * 根据枚举值获取对应的SpaceLevelEnum枚举实例
     *
     * @param value 枚举值
     * @return 对应的SpaceLevelEnum枚举实例，如果未找到则返回null
     */
    public static SpaceLevelEnum getEnumByValue(Integer value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        
        for (SpaceLevelEnum spaceLevelEnum : SpaceLevelEnum.values()) {
            if (spaceLevelEnum.value == value) {
                return spaceLevelEnum;
            }
        }
        
        return null;
    }
}
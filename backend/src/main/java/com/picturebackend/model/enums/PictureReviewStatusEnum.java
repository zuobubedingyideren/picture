package com.picturebackend.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Data;
import lombok.Getter;

/**
 * packageName: com.picturebackend.model.enums
 *
 * @author: idpeng
 * @version: 1.0
 * @enumName: PictureReviewStatusEnum
 * @date: 2025/8/19 11:00
 * @description: 审核状态枚举类
 */
@Getter
public enum PictureReviewStatusEnum {
    REVIEWING("待审�?, 0),
    PASS("通过", 1),
    REJECT("拒绝", 2);

    private final String text;
    private final int value;

    PictureReviewStatusEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     */
    public static PictureReviewStatusEnum getEnumByValue(Integer value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (PictureReviewStatusEnum pictureReviewStatusEnum : PictureReviewStatusEnum.values()) {
            if (pictureReviewStatusEnum.value == value) {
                return pictureReviewStatusEnum;
            }
        }
        return null;
    }
}

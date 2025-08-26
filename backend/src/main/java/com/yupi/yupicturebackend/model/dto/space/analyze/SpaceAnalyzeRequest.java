package com.yupi.yupicturebackend.model.dto.space.analyze;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * packageName: com.yupi.yupicturebackend.model.dto.space.analyze
 *
 * @author: idpeng
 * @version: 1.0
 * @className: SpaceAnalyzeRequest
 * @date: 2025/8/24 23:31
 * @description: 通用空间分析请求
 */
@Data
@ApiModel(description = "通用空间分析请求")
public class SpaceAnalyzeRequest implements Serializable {
    /**
     * 空间ID，用于指定要分析的空间
     */
    @ApiModelProperty(value = "空间ID", example = "1")
    private Long spaceId;

    /**
     * 是否查询公开空间的数据
     */
    @ApiModelProperty(value = "是否查询公开空间", example = "false")
    private boolean queryPublic;

    /**
     * 是否查询所有数据（包括非公开空间）
     */
    @ApiModelProperty(value = "是否查询所有数据", example = "true")
    private boolean queryAll;

    @Serial
    private static final long serialVersionUID = 1L;
}

package com.picturebackend.model.vo.space.analyze;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * packageName: com.picturebackend.model.vo.space.analyze
 *
 * @author: idpeng
 * @version: 1.0
 * @className: SpaceUsageAnalyzeResponse
 * @date: 2025/8/24 23:56
 * @description: 响应视图类，用于将分析结果返回给前端
 */
@Data
@ApiModel(description = "响应视图�?)
public class SpaceUsageAnalyzeResponse implements Serializable {

    /**
     * 用户已使用的存储空间大小（字节）
     */
    @ApiModelProperty(value = "用户已使用的存储空间大小（字节）")
    private Long userSize;

    /**
     * 用户最大允许的存储空间大小（字节）
     */
    @ApiModelProperty(value = "用户最大允许的存储空间大小（字节）")
    private Long maxSize;

    /**
     * 存储空间使用比例
     */
    @ApiModelProperty(value = "存储空间使用比例")
    private Double sizeUsageRatio;

    /**
     * 用户已使用的文件数量
     */
    @ApiModelProperty(value = "用户已使用的文件数量")
    private Long usedCount;

    /**
     * 用户最大允许的文件数量
     */
    @ApiModelProperty(value = "用户最大允许的文件数量")
    private Long maxCount;

    /**
     * 文件数量使用比例
     */
    @ApiModelProperty(value = "文件数量使用比例")
    private Double countUsageRatio;

    @Serial
    private static final long serialVersionUID = 1L;
}

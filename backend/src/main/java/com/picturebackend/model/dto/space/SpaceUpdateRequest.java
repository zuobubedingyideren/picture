package com.picturebackend.model.dto.space;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * packageName: com.picturebackend.model.dto.space
 *
 * @author: idpeng
 * @version: 1.0
 * @className: SpaceUpdateRequest
 * @date: 2025/8/22 14:52
 * @description: 空间更新请求
 */
@Data
@ApiModel(description = "空间更新")
public class SpaceUpdateRequest implements Serializable {
    /**
     * id
     */
    @ApiModelProperty(value = "空间ID", required = true)
    private Long id;

    /**
     * 空间名称
     */
    @ApiModelProperty(value = "空间名称", required = true)
    private String spaceName;

    /**
     * 空间级别�?-普通版 1-专业�?2-旗舰�?
     */
    @ApiModelProperty(value = "空间级别�?-普通版 1-专业�?2-旗舰�?, required = true)
    private Integer spaceLevel;

    /**
     * 空间图片的最大总大�?
     */
    @ApiModelProperty(value = "空间图片的最大总大小（单位：字节）", required = true)
    private Long maxSize;

    /**
     * 空间图片的最大数�?
     */
    @ApiModelProperty(value = "空间图片的最大数�?, required = true)
    private Long maxCount;

    @Serial
    private static final long serialVersionUID = 1L;
}

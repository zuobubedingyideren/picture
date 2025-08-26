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
 * @className: SpaceEditRequest
 * @date: 2025/8/22 14:48
 * @description: 空间编辑请求
 */
@Data
@ApiModel(description = "空间编辑")
public class SpaceEditRequest implements Serializable {
    /**
     * 空间ID
     */
    @ApiModelProperty(value = "空间ID", required = true)
    private Long id;

    /**
     * 空间名称
     */
    @ApiModelProperty(value = "空间名称", required = true)
    private String spaceName;

    @Serial
    private static final long serialVersionUID = 1L;
}

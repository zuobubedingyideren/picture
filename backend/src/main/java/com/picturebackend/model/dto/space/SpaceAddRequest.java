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
 * @className: SpaceAddRequest
 * @date: 2025/8/22 11:44
 * @description: 空间创建
 */
@Data
@ApiModel(description = "空间创建")
public class SpaceAddRequest implements Serializable {

    /**
     * 空间名称
     */
    @ApiModelProperty(value = "空间名称")
    private String spaceName;

    /**
     * 空间等级 0-普通版 1-专业�?2-旗舰�?
     */
    @ApiModelProperty(value = "空间等级")
    private Integer spaceLevel;

    /**
     * 空间类型�?-私有 1-团队
     */
    @ApiModelProperty(value = "空间类型")
    private Integer spaceType;


    @Serial
    private static final long serialVersionUID = 1L;
}

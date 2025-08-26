package com.picturebackend.model.dto.picture;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * packageName: com.picturebackend.model.dto.picture
 *
 * @author: idpeng
 * @version: 1.0
 * @className: SearchPictureByColorRequest
 * @date: 2025/8/24 11:29
 * @description: 按照颜色搜索图片请求
 */
@Data
@ApiModel(description = "按照颜色搜索图片请求")
public class SearchPictureByColorRequest implements Serializable {

    /**
     * 图片颜色，用于搜索匹配颜色的图片
     */
    @ApiModelProperty(value = "图片颜色")
    private String picColor;

    /**
     * 空间ID，指定在哪个空间中搜索图�?
     */
    @ApiModelProperty(value = "空间ID")
    private Long spaceId;

    @Serial
    private static final long serialVersionUID = 1L;
}

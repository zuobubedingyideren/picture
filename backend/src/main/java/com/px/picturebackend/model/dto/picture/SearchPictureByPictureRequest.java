package com.px.picturebackend.model.dto.picture;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * packageName: com.px.picturebackend.model.dto.picture
 *
 * @author: idpeng
 * @version: 1.0
 * @className: SearchPictureByPictureRequest
 * @date: 2025/8/23 22:44
 * @description: 以图搜图请求
 */
@Data
@ApiModel(description = "以图搜图请求")
public class SearchPictureByPictureRequest implements Serializable {


    /**
     * 图片ID，用于标识要搜索的图片
     */
    @ApiModelProperty("图片id")
    private Long pictureId;

    @Serial
    private static final long serialVersionUID = 1L;
}

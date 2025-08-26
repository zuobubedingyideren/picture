package com.picturebackend.model.dto.picture;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * packageName: com.picturebackend.model.dto.picture
 *
 * @author: idpeng
 * @version: 1.0
 * @className: PictureUpdateRequest
 * @date: 2025/8/3 15:59
 * @description: 图片更新请求，给管理员使�?
 */
@Data
@ApiModel("图片更新")
public class PictureUpdateRequest implements Serializable {

    /**
     * 图片ID，用于唯一标识一张图�?
     */
    @ApiModelProperty("图片ID，用于唯一标识一张图�?)
    private Long id;

    /**
     * 图片名称
     */
    @ApiModelProperty("图片名称")
    private String name;

    /**
     * 图片简�?
     */
    @ApiModelProperty("图片简�?)
    private String introduction;

    /**
     * 图片分类
     */
    @ApiModelProperty("图片分类")
    private String category;

    /**
     * 图片标签列表
     */
    @ApiModelProperty("图片标签列表")
    private List<String> tags;
    
    private static final long serialVersionUID = 1L;
}

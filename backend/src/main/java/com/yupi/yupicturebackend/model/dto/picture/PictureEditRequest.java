package com.yupi.yupicturebackend.model.dto.picture;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * packageName: com.yupi.yupicturebackend.model.dto.picture
 *
 * @author: idpeng
 * @version: 1.0
 * @className: PictureEditRequest
 * @date: 2025/8/3 16:03
 * @description: 图片修改请求，一般给普通用户使用，可修改字段小于更新请求
 */
@Data
@ApiModel(description = "图片修改")
public class PictureEditRequest implements Serializable {

    /**
     * id
     */
    @ApiModelProperty(value = "图片ID")
    private Long id;

    /**
     * 图片名称
     */
    @ApiModelProperty(value = "图片名称")
    private String name;

    /**
     * 简介
     */
    @ApiModelProperty(value = "图片简介")
    private String introduction;

    /**
     * 分类
     */
    @ApiModelProperty(value = "图片分类")
    private String category;

    /**
     * 标签
     */
    @ApiModelProperty(value = "图片标签列表")
    private List<String> tags;

    private static final long serialVersionUID = 1L;
}


package com.yupi.yupicturebackend.model.dto.picture;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * packageName: com.yupi.yupicturebackend.model.dto.picture
 *
 * @author: idpeng
 * @version: 1.0
 * @className: PictureEditByBatchRequest
 * @date: 2025/8/24 14:25
 * @description: 图片批量编辑请求
 */
@Data
@ApiModel(description = "图片批量编辑请求")
public class PictureEditByBatchRequest implements Serializable {

    /**
     * 图片ID列表
     */
    @ApiModelProperty(value = "图片ID列表", required = true)
    private List<Long> pictureIdList;

    /**
     * 空间ID
     */
    @ApiModelProperty(value = "空间ID", required = true)
    private Long spaceId;

    /**
     * 分类
     */
    @ApiModelProperty(value = "分类")
    private String category;

    /**
     * 标签列表
     */
    @ApiModelProperty(value = "标签列表")
    private List<String> tags;

    /**
     * 命名规则
     */
    @ApiModelProperty(value = "命名规则")
    private String nameRule;

    @Serial
    private static final long serialVersionUID = 1L;
}

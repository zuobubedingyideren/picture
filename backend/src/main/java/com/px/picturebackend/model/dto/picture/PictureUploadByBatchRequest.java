package com.px.picturebackend.model.dto.picture;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * packageName: com.px.picturebackend.model.dto.picture
 *
 * @author: idpeng
 * @version: 1.0
 * @className: PictureUploadByBatchRequest
 * @date: 2025/8/20 14:31
 * @description: 批量抓取图片
 */
@Data
@ApiModel(description = "批量抓取图片")
public class PictureUploadByBatchRequest {
    /**
     * 搜索词，用于指定要抓取的图片内容
     */
    @ApiModelProperty("搜索词，用于指定要抓取的图片内容")
    private String searchText;

    /**
     * 名称前缀，用于指定图片名称的前缀部分
     */
    @ApiModelProperty("名称前缀")
    private String namePrefix;

    /**
     * 抓取数量，默认值为10
     */
    @ApiModelProperty("抓取数量")
    private Integer count = 10;
}

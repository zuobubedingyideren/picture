package com.picturebackend.api.imagesearch.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * packageName: com.picturebackend.api.imagesearch.model
 *
 * @author: idpeng
 * @version: 1.0
 * @className: ImageSearchResult
 * @date: 2025/8/23 20:20
 * @description: 图片搜索结果类，用于接受 API 的返回�?
 */
@Data
@ApiModel(description = "图片搜索结果�?)
public class ImageSearchResult {
    /**
     * 缩略图地址
     */
    @ApiModelProperty("缩略图地址")
    private String thumbUrl;
    
    /**
     * 来源地址
     */
    @ApiModelProperty("来源地址")
    private String fromUrl;
}

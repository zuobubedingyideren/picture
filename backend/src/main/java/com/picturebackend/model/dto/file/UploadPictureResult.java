package com.picturebackend.model.dto.file;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * packageName: com.picturebackend.model.dto.file
 *
 * @author: idpeng
 * @version: 1.0
 * @className: UploadPictureResult
 * @date: 2025/7/28 20:37
 * @description:
 */
@Data
@ApiModel(description = "图片上传实体")
public class UploadPictureResult {

    /**
     * 图片地址
     */
    @ApiModelProperty("图片地址")
    private String url;

    /**
     * 图片缩略图地址
     */
    @ApiModelProperty("图片缩略�?)
    private String thumbnailUrl;

    /**
     * 图片名称
     */
    @ApiModelProperty("图片名称")
    private String picName;

    /**
     * 图片主色�?
     */
    @ApiModelProperty("图片主色�?)
    private String picColor;


    /**
     * 文件体积
     */
    @ApiModelProperty("文件体积")
    private Long picSize;

    /**
     * 图片宽度
     */
    @ApiModelProperty("图片宽度")
    private int picWidth;

    /**
     * 图片高度
     */
    @ApiModelProperty("图片高度")
    private int picHeight;

    /**
     * 图片宽高�?
     */
    @ApiModelProperty("图片宽高�?)
    private Double picScale;

    /**
     * 图片格式
     */
    @ApiModelProperty("图片格式")
    private String picFormat;

}


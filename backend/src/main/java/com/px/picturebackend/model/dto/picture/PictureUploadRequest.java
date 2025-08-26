package com.px.picturebackend.model.dto.picture;

import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * packageName: com.px.picturebackend.model.dto.picture
 *
 * @author: idpeng
 * @version: 1.0
 * @className: PictureUploadRequest
 * @date: 2025/7/26 11:33
 * @description: 接受图片上传的请求参数，给普通用户使用
 */
@Data
@ApiOperation("接受图片上传")
public class PictureUploadRequest implements Serializable {
    /**
     * 图片id（用于修改）
     */
    @ApiModelProperty("图片ID")
    private Long id;

    /**
     * 文件地址
     */
    @ApiModelProperty("文件地址")
    private String fileUrl;

    /**
     * 图片名称
     */
    @ApiModelProperty("图片名称")
    private String picName;

    /**
     * 空间ID
     */
    @ApiModelProperty("空间ID")
    private Long spaceId;

    @Serial
    private static final long serialVersionUID = 1L;
}

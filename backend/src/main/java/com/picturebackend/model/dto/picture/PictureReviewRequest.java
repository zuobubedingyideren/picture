package com.picturebackend.model.dto.picture;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * packageName: com.picturebackend.model.dto.picture
 *
 * @author: idpeng
 * @version: 1.0
 * @className: PictureReviewRequest
 * @date: 2025/8/19 11:30
 * @description: 管理员审核的请求接受�?
 */
@Data
@ApiModel(description = "管理员审�?)
public class PictureReviewRequest implements Serializable {
    /**
     * 图片ID
     */
    @ApiModelProperty(value = "图片ID")
    private Long id;
    
    /**
     * 审核状态：0-待审�? 1-通过, 2-拒绝
     */
    @ApiModelProperty(value = "审核状态：0-待审�? 1-通过, 2-拒绝")
    private Integer reviewStatus;
    
    /**
     * 审核信息
     */
    @ApiModelProperty(value = "审核信息")
    private String reviewMessage;
    
    private static final long serialVersionUID = 1L;
}

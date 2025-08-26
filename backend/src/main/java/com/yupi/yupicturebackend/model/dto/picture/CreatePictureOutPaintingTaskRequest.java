package com.yupi.yupicturebackend.model.dto.picture;

import com.yupi.yupicturebackend.api.aliyunai.model.CreateOutPaintingTaskRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * packageName: com.yupi.yupicturebackend.model.dto.picture
 *
 * @author: idpeng
 * @version: 1.0
 * @className: CreatePictureOutPaintingTaskRequest
 * @date: 2025/8/24 16:42
 * @description: AI 扩图请求类，用于接受前端传来的参数并传递给 Service 服务层
 */
@Data
@ApiModel("AI 扩图请求类")
public class CreatePictureOutPaintingTaskRequest implements Serializable {
    /**
     * 图片ID，用于标识需要进行扩图处理的原始图片
     */
    @ApiModelProperty("图片ID，用于标识需要进行扩图处理的原始图片")
    private Long pictureId;

    /**
     * 扩图任务参数，包含具体的图像处理配置选项
     * 
     * @see CreateOutPaintingTaskRequest.Parameters
     */
    @ApiModelProperty("扩图任务参数，包含具体的图像处理配置选项")
    private CreateOutPaintingTaskRequest.Parameters parameters;

    @Serial
    private static final long serialVersionUID = 1L;
}

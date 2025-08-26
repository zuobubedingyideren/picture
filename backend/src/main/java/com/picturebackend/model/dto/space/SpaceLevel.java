package com.picturebackend.model.dto.space;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * packageName: com.picturebackend.model.dto.space
 *
 * @author: idpeng
 * @version: 1.0
 * @className: SpaceLevel
 * @date: 2025/8/22 23:47
 * @description: 空间级别
 */
@Data
@AllArgsConstructor
@ApiModel(description = "空间级别")
public class SpaceLevel {

    /**
     * 空间级别的�?
     */
    @ApiModelProperty("空间级别的�?)
    private int value;
    
    /**
     * 空间级别的描述文�?
     */
    @ApiModelProperty("空间级别的描述文�?)
    private String text;
    
    /**
     * 该级别下最大文件数量限�?
     */
    @ApiModelProperty("该级别下最大文件数量限�?)
    private long maxCount;
    
    /**
     * 该级别下最大存储空间大小（单位：字节）
     */
    @ApiModelProperty("该级别下最大存储空间大小（单位：字�?)
    private long maxSize;
}

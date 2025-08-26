package com.px.picturebackend.model.vo.space.analyze;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * packageName: com.px.picturebackend.model.vo.space.analyze
 *
 * @author: idpeng
 * @version: 1.0
 * @className: SpaceSizeAnalyzeResponse
 * @date: 2025/8/25 11:07
 * @description: 图片大小分析视图返回类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpaceSizeAnalyzeResponse implements Serializable {

    /**
     * 图片大小范围描述
     */
    private String sizeRange;
    
    /**
     * 该大小范围内的图片数
     */
    private Long count;

    @Serial
    private static final long serialVersionUID = 1L;
}

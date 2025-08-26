package com.picturebackend.model.vo.space.analyze;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * packageName: com.picturebackend.model.vo.space.analyze
 *
 * @author: idpeng
 * @version: 1.0
 * @className: SpaceTagAnalyzeResponse
 * @date: 2025/8/25 10:43
 * @description: 标签分析视图�?
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpaceTagAnalyzeResponse implements Serializable {

    /**
     * 标签名称
     */
    private String tag;
    
    /**
     * 标签出现次数
     */
    private Long count;

    @Serial
    private static final long serialVersionUID = 1L;
}

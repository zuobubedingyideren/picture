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
 * @className: SpaceCategoryAnalyzeResponse
 * @date: 2025/8/25 10:08
 * @description: 分类分析的视图返回类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpaceCategoryAnalyzeResponse implements Serializable {
    /**
     * 文件分类名称
     */
    private String category;
    
    /**
     * 该分类下的文件数
     */
    private Long count;
    
    /**
     * 该分类下的文件总大小（字节）
     */
    private Long totalSize;

    @Serial
    private static final long serialVersionUID = 1L;
}

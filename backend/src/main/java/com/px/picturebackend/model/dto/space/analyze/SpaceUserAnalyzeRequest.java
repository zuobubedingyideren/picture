package com.px.picturebackend.model.dto.space.analyze;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * packageName: com.px.picturebackend.model.dto.space.analyze
 *
 * @author: idpeng
 * @version: 1.0
 * @className: SpaceUserAnalyzeRequest
 * @date: 2025/8/25 14:16
 * @description: 用户上传行为分析请求
 *              用于分析指定用户在指定时间范围内的上传行为，如上传数量、上传大小、上传频率等。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SpaceUserAnalyzeRequest extends SpaceAnalyzeRequest{

    /**
     * 用户ID，用于指定要分析的用户
     */
    private Long userId;
    
    /**
     * 时间维度，用于指定分析的时间范围（如：day, week, month, year）
     * 可选值：day, week, month, year
     */
    private String timeDimension;
}

package com.yupi.yupicturebackend.model.vo.space.analyze;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * packageName: com.yupi.yupicturebackend.model.vo.space.analyze
 *
 * @author: idpeng
 * @version: 1.0
 * @className: SpaceUserAnalyzeResponse
 * @date: 2025/8/25 14:19
 * @description: 用户行为‌分析结果需要返回时؜间区间和对应的图片数量
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpaceUserAnalyzeResponse implements Serializable {

    /**
     * 时间区间，用于表示用户行为分析的时间段
     * 例如："2025-08-01 ~ 2025-08-31"
     */
    private String period;
    
    /**
     * 图片数量，表示在对应时间区间内的图片上传数量
     */
    private Long count;

    @Serial
    private static final long serialVersionUID = 1L;
}

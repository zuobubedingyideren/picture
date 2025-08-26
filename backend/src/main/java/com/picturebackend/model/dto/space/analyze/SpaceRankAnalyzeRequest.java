package com.picturebackend.model.dto.space.analyze;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * packageName: com.picturebackend.model.dto.space.analyze
 *
 * @author: idpeng
 * @version: 1.0
 * @className: SpaceRankAnalyzeRequest
 * @date: 2025/8/25 14:51
 * @description: 空间使用排行
 */
@Data
public class SpaceRankAnalyzeRequest implements Serializable {
    /**
     * 排行榜显示数量，默认�?0
     */
    private Integer topN = 10;

    @Serial
    private static final long serialVersionUID = 1L;
}

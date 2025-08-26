package com.px.picturebackend.common;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * packageName: com.px.picturebackend.common
 *
 * @author: idpeng
 * @version: 1.0
 * @className: DeleteRequest
 * @date: 2025/7/7 22:22
 * @description: 删除请求包装类，接受要删除数据的id作为参数
 */
@Data
public class DeleteRequest implements Serializable {
    /**
     * id
     */
    private Long id;

    @Serial
    private static final long serialVersionUID = 1L;
}

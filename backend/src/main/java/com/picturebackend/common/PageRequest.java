package com.picturebackend.common;

import lombok.Data;

/**
 * packageName: com.picturebackend.common
 *
 * @author: idpeng
 * @version: 1.0
 * @className: PageRequest
 * @date: 2025/7/7 22:20
 * @description: 统一的请求包装类 用于接受前端的传来的参数
 */
@Data
public class PageRequest {
    /**
     * 当前页号
     */
    private int current = 1;

    /**
     * 页面大小
     */
    private int pageSize = 10;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序顺序（默认降序）
     */
    private String sortOrder = "descend";
}

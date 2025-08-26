package com.picturebackend.model.vo.picture;

import lombok.Data;

import java.util.List;

/**
 * packageName: com.picturebackend.model.vo.picture
 *
 * @author: idpeng
 * @version: 1.0
 * @className: PictureTagCategory
 * @date: 2025/8/3 20:13
 * @description: 图片标签分类列表视图
 */
@Data
public class PictureTagCategory {

    /**
     * 标签列表
     */
    private List<String> tagList;

    /**
     * 分类列表
     */
    private List<String> categoryList;
}

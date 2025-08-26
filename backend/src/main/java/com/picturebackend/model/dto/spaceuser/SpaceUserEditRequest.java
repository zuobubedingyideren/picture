package com.picturebackend.model.dto.spaceuser;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * packageName: com.picturebackend.model.dto.spaceuser
 *
 * @author: idpeng
 * @version: 1.0
 * @className: SpaceUserEditRequest
 * @date: 2025/8/25 19:56
 * @description: 编辑空间成؜员请求，给空间管理‌员使用，可以设置空؜
 */
@Data
public class SpaceUserEditRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 空间角色：viewer/editor/admin
     */
    private String spaceRole;

    @Serial
    private static final long serialVersionUID = 1L;
}


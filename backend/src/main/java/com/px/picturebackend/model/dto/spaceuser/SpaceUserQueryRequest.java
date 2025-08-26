package com.px.picturebackend.model.dto.spaceuser;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * packageName: com.px.picturebackend.model.dto.spaceuser
 *
 * @author: idpeng
 * @version: 1.0
 * @className: SpaceUserQueryRequest
 * @date: 2025/8/25 19:57
 * @description: 查询空间成员请求，可以不用分页
 */
@Data
public class SpaceUserQueryRequest implements Serializable {

    /**
     * ID
     */
    private Long id;

    /**
     * 空间 ID
     */
    private Long spaceId;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 空间角色：viewer/editor/admin
     */
    private String spaceRole;

    @Serial
    private static final long serialVersionUID = 1L;
}


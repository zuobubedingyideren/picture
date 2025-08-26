package com.yupi.yupicturebackend.model.dto.spaceuser;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * packageName: com.yupi.yupicturebackend.model.dto.spaceuser
 *
 * @author: idpeng
 * @version: 1.0
 * @className: SpaceUserAddRequest
 * @date: 2025/8/25 19:55
 * @description: 添加空间成员请求，给空间管理员使用
 */
@Data
public class SpaceUserAddRequest implements Serializable {

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


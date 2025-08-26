package com.yupi.yupicturebackend.manager.auth.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * packageName: com.yupi.yupicturebackend.manager.auth.model
 *
 * @author: idpeng
 * @version: 1.0
 * @className: SpaceUserPermission
 * @date: 2025/8/26 09:59
 * @description: 空间成员权限
 */
@Data
public class SpaceUserPermission implements Serializable {

    /**
     * 权限键
     */
    private String key;

    /**
     * 权限名称
     */
    private String name;

    /**
     * 权限描述
     */
    private String description;

    @Serial
    private static final long serialVersionUID = 1L;

}


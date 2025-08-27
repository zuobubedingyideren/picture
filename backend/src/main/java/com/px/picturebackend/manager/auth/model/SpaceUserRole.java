package com.px.picturebackend.manager.auth.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * packageName: com.picturebackend.manager.auth.model
 *
 * @author: idpeng
 * @version: 1.0
 * @className: SpaceUserRole
 * @date: 2025/8/26 10:00
 * @description: 空间成员角色
 */
@Data
public class SpaceUserRole implements Serializable {

    /**
     * 角色键值
     */
    private String key;

    /**
     * 角色名称
     */
    private String name;

    /**
     * 权限键列表
     */
    private List<String> permissions;

    /**
     * 角色描述
     */
    private String description;

    @Serial
    private static final long serialVersionUID = 1L;
}

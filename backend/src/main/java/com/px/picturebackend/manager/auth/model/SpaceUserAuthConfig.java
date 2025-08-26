package com.px.picturebackend.manager.auth.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * packageName: com.px.picturebackend.manager.auth.model
 *
 * @author: idpeng
 * @version: 1.0
 * @className: SpaceUserAuthConfig
 * @date: 2025/8/26 09:58
 * @description: 权限配置类
 */
@Data
public class SpaceUserAuthConfig implements Serializable {

    /**
     * 权限列表
     */
    private List<SpaceUserPermission> permissions;

    /**
     * 角色列表
     */
    private List<com.px.picturebacked.manager.auth.model.SpaceUserRole> roles;

    @Serial
    private static final long serialVersionUID = 1L;
}


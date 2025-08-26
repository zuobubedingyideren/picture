package com.yupi.yupicturebackend.manager.auth.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * packageName: com.yupi.yupicturebackend.manager.auth.model
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
     * 角色键
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

    private static final long serialVersionUID = 1L;
}

package com.yupi.yupicturebackend.model.dto.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * packageName: com.yupi.yupicturebackend.model.dto.user
 *
 * @author: idpeng
 * @version: 1.0
 * @className: UserUpdateRequest
 * @date: 2025/7/9 23:07
 * @description: 更新用户请求类
 */
@Data
@ApiModel(description = "更新用户请求类")
public class UserUpdateRequest implements Serializable {

    /**
     * id
     */
    @ApiModelProperty(value = "用户ID")
    private Long id;

    /**
     * 用户昵称
     */
    @ApiModelProperty(value = "用户昵称")
    private String userName;

    /**
     * 用户头像
     */
    @ApiModelProperty(value = "用户头像")
    private String userAvatar;

    /**
     * 简介
     */
    @ApiModelProperty(value = "用户简介")
    private String userProfile;

    /**
     * 用户角色：user/admin
     */
    @ApiModelProperty(value = "用户角色：user/admin")
    private String userRole;

    private static final long serialVersionUID = 1L;
}


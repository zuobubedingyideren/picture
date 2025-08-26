package com.yupi.yupicturebackend.model.dto.user;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * packageName: com.yupi.yupicturebackend.model.dto.user
 *
 * @author: idpeng
 * @version: 1.0
 * @className: UserAddRequest
 * @date: 2025/7/9 23:06
 * @description: 增加用户的请求类
 */
@Data
@Api(tags = "增加用户")
public class UserAddRequest implements Serializable {

    /**
     * 用户昵称
     */
    @ApiModelProperty(value = "用户昵称")
    private String userName;

    /**
     * 账号
     */
    @ApiModelProperty(value = "账号")
    private String userAccount;

    /**
     * 用户头像
     */
    @ApiModelProperty(value = "用户头像")
    private String userAvatar;

    /**
     * 用户简介
     */
    @ApiModelProperty(value = "用户简介")
    private String userProfile;

    /**
     * 用户角色: user, admin
     */
    @ApiModelProperty(value = "用户角色: user, admin")
    private String userRole;

    private static final long serialVersionUID = 1L;
}



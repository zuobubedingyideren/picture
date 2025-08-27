package com.px.picturebackend.model.dto.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * packageName: com.px.picturebackend.model.dto.user
 *
 * @author: idpeng
 * @version: 1.0
 * @className: UserUpdateMyInfoRequest
 * @date: 2025/1/17
 * @description: 用户更新自己信息请求参数
 */
@Data
@ApiModel(description = "用户更新自己信息请求参数")
public class UserUpdateMyInfoRequest implements Serializable {

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

    @Serial
    private static final long serialVersionUID = 1L;
}
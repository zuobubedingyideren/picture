package com.yupi.yupicturebackend.model.dto.user;

import com.yupi.yupicturebackend.common.PageRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * packageName: com.yupi.yupicturebackend.model.dto.user
 *
 * @author: idpeng
 * @version: 1.0
 * @className: UserQueryRequest
 * @date: 2025/7/9 23:07
 * @description: 查询用户的请求类，支持分页查询
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel(description = "查询用户")
public class UserQueryRequest extends PageRequest implements Serializable {

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
     * 账号
     */
    @ApiModelProperty(value = "用户账号")
    private String userAccount;

    /**
     * 简介
     */
    @ApiModelProperty(value = "用户简介")
    private String userProfile;

    /**
     * 用户角色：user/admin/ban
     */
    @ApiModelProperty(value = "用户角色：user/admin/ban")
    private String userRole;

    private static final long serialVersionUID = 1L;
}



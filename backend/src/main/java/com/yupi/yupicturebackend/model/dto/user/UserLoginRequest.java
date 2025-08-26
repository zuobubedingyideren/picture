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
 * @className: UserLoginRequest
 * @date: 2025/7/9 20:29
 * @description: 用户登录
 */
@Data
@ApiModel(description = "用户登录")
public class UserLoginRequest implements Serializable {

    /**
     * 账号
     */
    @ApiModelProperty(value = "账号")
    private String userAccount;

    /**
     * 密码
     */
    @ApiModelProperty(value = "密码")
    private String userPassword;

    private static final long serialVersionUID = 3191241716373120793L;
}

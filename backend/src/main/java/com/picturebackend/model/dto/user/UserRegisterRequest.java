package com.picturebackend.model.dto.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * packageName: com.picturebackend.model.dto.user
 *
 * @author: idpeng
 * @version: 1.0
 * @className: UserRegisterRequest
 * @date: 2025/7/9 19:30
 * @description: 接受用户注册的请求参�?
 */
@Data
@ApiModel(description = "接受用户注册")
public class UserRegisterRequest implements Serializable {
        private static final long serialVersionUID = 2804408337121725132L;

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

        /**
         * 确认密码
         */
        @ApiModelProperty(value = "确认密码")
        private String checkPassword;

}

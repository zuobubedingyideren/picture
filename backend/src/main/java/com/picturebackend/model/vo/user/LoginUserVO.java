package com.picturebackend.model.vo.user;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * packageName: com.picturebackend.model.vo.user
 *
 * @author: idpeng
 * @version: 1.0
 * @className: LoginUserVO
 * @date: 2025/7/9 21:13
 * @description: 传给前段的用户信�?
 */
@Data
public class LoginUserVO implements Serializable {

    /**
     * 用户 id
     */
    private Long id;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简�?
     */
    private String userProfile;

    /**
     * 用户角色：user/admin
     */
    private String userRole;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    private static final long serialVersionUID = 1L;
}



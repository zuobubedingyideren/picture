package com.picturebackend.manager.auth.model;

/**
 * packageName: com.picturebackend.manager.auth.model
 *
 * @author: idpeng
 * @version: 1.0
 * @className: SpaceUserPermissionConstant
 * @date: 2025/8/26 10:02
 * @description: 空间成员权限常量类，便于后续校验权限时使�?
 */
public interface SpaceUserPermissionConstant {
    /**
     * 空间用户管理权限
     */
    String SPACE_USER_MANAGE = "spaceUser:manage";

    /**
     * 图片查看权限
     */
    String PICTURE_VIEW = "picture:view";

    /**
     * 图片上传权限
     */
    String PICTURE_UPLOAD = "picture:upload";

    /**
     * 图片编辑权限
     */
    String PICTURE_EDIT = "picture:edit";

    /**
     * 图片删除权限
     */
    String PICTURE_DELETE = "picture:delete";
}


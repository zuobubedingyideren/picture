package com.picturebackend.manager.auth;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.ServletUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.Header;
import cn.hutool.json.JSONUtil;
import com.picturebackend.model.entity.Picture;
import com.picturebackend.model.entity.Space;
import com.picturebackend.model.entity.SpaceUser;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * packageName: com.picturebackend.manager.auth
 *
 * @author: idpeng
 * @version: 1.0
 * @className: SpaceUserAuthContext
 * @date: 2025/8/26 10:38
 * @description: 表示用户在特定空间内的授权上下文，包括关联的图片、空间和用户信息
 */
@Data
public class SpaceUserAuthContext {
    /**
     * 授权上下文ID
     */
    private Long id;
    
    /**
     * 关联的图片ID
     */
    private Long pictureId;
    
    /**
     * 关联的空间ID
     */
    private Long spaceId;
    
    /**
     * 关联的空间用户ID
     */
    private Long spaceUserId;
    
    /**
     * 关联的图片实体对�?
     */
    private Picture picture;
    
    /**
     * 关联的空间实体对�?
     */
    private Space space;
    
    /**
     * 关联的空间用户实体对�?
     */
    private SpaceUser spaceUser;


}

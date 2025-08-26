package com.picturebackend.manager.auth;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.picturebackend.manager.auth.model.SpaceUserAuthConfig;
import com.picturebackend.manager.auth.model.SpaceUserRole;
import com.picturebackend.model.entity.Space;
import com.picturebackend.model.entity.SpaceUser;
import com.picturebackend.model.entity.User;
import com.picturebackend.model.enums.SpaceRoleEnum;
import com.picturebackend.model.enums.SpaceTypeEnum;
import com.picturebackend.service.SpaceUserService;
import com.picturebackend.service.UserService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * packageName: com.picturebackend.manager.auth
 *
 * @author: idpeng
 * @version: 1.0
 * @className: SpaceUserAuthManager
 * @date: 2025/8/26 10:03
 * @description: 可加载配置文件到对象，并提供根据角色获取权限列表的方�?
 */
@Component
public class SpaceUserAuthManager {

    @Resource
    private SpaceUserService spaceUserService;

    @Resource
    private UserService userService;

    public static final SpaceUserAuthConfig SPACE_USER_AUTH_CONFIG;

    static {
        String json = ResourceUtil.readUtf8Str("biz/spaceUserAuthConfig.json");
        SPACE_USER_AUTH_CONFIG = JSONUtil.toBean(json, SpaceUserAuthConfig.class);
    }

    /**
     * 根据空间用户角色获取对应的权限列�?
     *
     * @param spaceUserRole 空间用户角色标识
     * @return 权限字符串列表，如果未找到对应角色或参数为空则返回空列表
     */
    public List<String> getPermissionsByRole(String spaceUserRole) {
        // 如果角色为空，返回空列表
        if (StrUtil.isBlank(spaceUserRole)) {
            return new ArrayList<>();
        }
        // 找到匹配的角�?
        SpaceUserRole role = SPACE_USER_AUTH_CONFIG.getRoles().stream()
                .filter(r -> spaceUserRole.equals(r.getKey()))
                .findFirst()
                .orElse(null);
        // 如果未找到匹配角色，返回空列�?
        if (role == null) {
            return new ArrayList<>();
        }
        return role.getPermissions();
    }

    /**
     * 根据空间和登录用户获取权限列�?
     * 
     * @param space 空间对象，表示用户要访问的空间，如果为null表示公共图库
     * @param loginUser 登录用户对象，表示当前登录的用户
     * @return 用户在指定空间下的权限列�?
     */
    public List<String> getPermissionList(Space space, User loginUser) {
        if (loginUser == null) {
            return new ArrayList<>();
        }
        // 管理员权�?
        List<String> ADMIN_PERMISSIONS = getPermissionsByRole(SpaceRoleEnum.ADMIN.getValue());
        // 公共图库
        if (space == null) {
            if (userService.isAdmin(loginUser)) {
                return ADMIN_PERMISSIONS;
            }
            return new ArrayList<>();
        }
        SpaceTypeEnum spaceTypeEnum = SpaceTypeEnum.getEnumByValue(space.getSpaceType());
        if (spaceTypeEnum == null) {
            return new ArrayList<>();
        }
        // 根据空间获取对应的权�?
        switch (spaceTypeEnum) {
            case PRIVATE:
                // 私有空间，仅本人或管理员有所有权�?
                if (space.getUserId().equals(loginUser.getId()) || userService.isAdmin(loginUser)) {
                    return ADMIN_PERMISSIONS;
                } else {
                    return new ArrayList<>();
                }
            case TEAM:
                // 团队空间，查�?SpaceUser 并获取角色和权限
                SpaceUser spaceUser = spaceUserService.lambdaQuery()
                        .eq(SpaceUser::getSpaceId, space.getId())
                        .eq(SpaceUser::getUserId, loginUser.getId())
                        .one();
                if (spaceUser == null) {
                    return new ArrayList<>();
                } else {
                    return getPermissionsByRole(spaceUser.getSpaceRole());
                }
        }
        return new ArrayList<>();
    }


}

package com.px.picturebackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.px.picturebackend.exception.BusinessException;
import com.px.picturebackend.exception.ErrorCode;
import com.px.picturebackend.exception.ThrowUtils;
import com.px.picturebackend.model.dto.space.SpaceAddRequest;
import com.px.picturebackend.model.dto.spaceuser.SpaceUserAddRequest;
import com.px.picturebackend.model.dto.spaceuser.SpaceUserQueryRequest;
import com.px.picturebackend.model.entity.Space;
import com.px.picturebackend.model.entity.SpaceUser;    
import com.px.picturebackend.model.entity.User;
import com.px.picturebackend.model.enums.SpaceRoleEnum;
import com.px.picturebackend.model.enums.SpaceTypeEnum;
import com.px.picturebackend.model.vo.space.SpaceVO;
import com.px.picturebackend.model.vo.spaceuser.SpaceUserVO;
import com.px.picturebackend.model.vo.user.UserVO;
import com.px.picturebackend.service.SpaceService;
import com.px.picturebackend.service.SpaceUserService;
import com.px.picturebackend.mapper.SpaceUserMapper;
import com.px.picturebackend.service.UserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
* @author idpeng
* @description 针对表【space_user(空间用户关联)】的数据库操作Service实现
* @createDate 2025-08-25 19:28:45
*/
@Service
public class SpaceUserServiceImpl extends ServiceImpl<SpaceUserMapper, SpaceUser>
    implements SpaceUserService{

    @Resource
    private UserService userService;

    @Resource
    private SpaceService spaceService;

    /**
     * 添加空间用户关联
     *
     * @param spaceUserAddRequest 空间用户添加请求参数
     * @return 新增的空间用户关联记录的ID
     */
    @Override
    public long addSpaceUser(SpaceUserAddRequest spaceUserAddRequest) {
        // 参数校验
        ThrowUtils.throwIf(spaceUserAddRequest == null, ErrorCode.PARAMS_ERROR);
        SpaceUser spaceUser = new SpaceUser();
        BeanUtil.copyProperties(spaceUserAddRequest, spaceUser);
        validSpaceUser(spaceUser, true);
        // 数据库操作
        boolean result = this.save(spaceUser);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return spaceUser.getId();
    }

    /**
     * 校验空间用户关联信息
     *
     * @param spaceUser 空间用户关联信息
     * @param add       是否为添加操作
     */
    @Override
    public void validSpaceUser(SpaceUser spaceUser, boolean add) {
        // 校验参数
        ThrowUtils.throwIf(spaceUser == null, ErrorCode.PARAMS_ERROR);
        // 添加操作时需要校验用户和空间是否存在
        Long spaceId = spaceUser.getSpaceId();
        Long userId = spaceUser.getUserId();
        if (add) {
            ThrowUtils.throwIf(ObjectUtil.hasEmpty(spaceId, userId), ErrorCode.PARAMS_ERROR);
            User user = userService.getById(userId);
            ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        }
        // 校验空间角色是否有效
        String spaceRole = spaceUser.getSpaceRole();
        SpaceRoleEnum spaceRoleEnum = SpaceRoleEnum.getEnumByValue(spaceRole);
        if (spaceRole != null && spaceRoleEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间角色不存在");
        }
    }

    /**
     * 获取空间用户关联查询条件
     *
     * @param spaceUserQueryRequest 空间用户关联查询请求参数
     * @return 查询条件构造器
     */
    @Override
    public QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest) {
        QueryWrapper<SpaceUser> spaceUserQueryWrapper = new QueryWrapper<>();
        if (spaceUserQueryRequest == null) {
            return spaceUserQueryWrapper;
        }

        // 从对象中取出需要查询的字段
        Long id = spaceUserQueryRequest.getId();
        Long spaceId = spaceUserQueryRequest.getSpaceId();
        Long userId = spaceUserQueryRequest.getUserId();
        String spaceRole = spaceUserQueryRequest.getSpaceRole();
        
        spaceUserQueryWrapper.eq(ObjectUtil.isNotEmpty(id), "id", id)
                              .eq(ObjectUtil.isNotEmpty(spaceId), "spaceId", spaceId)
                              .eq(ObjectUtil.isNotEmpty(userId), "userId", userId)
                              .eq(ObjectUtil.isNotEmpty(spaceRole), "spaceRole", spaceRole);
        return spaceUserQueryWrapper;
    }

    /**
     * 获取空间用户关联的封装视图对象
     *
     * @param spaceUser 空间用户关联实体对象
     * @param request   HTTP请求对象
     * @return 空间用户关联的视图对象
     */
    @Override
    public SpaceUserVO getSpaceUserVO(SpaceUser spaceUser, HttpServletRequest request) {
        // 对象转封装类
        SpaceUserVO spaceUserVO = SpaceUserVO.objToVo(spaceUser);
        // 关联查询用户信息
        Long userId = spaceUser.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            spaceUserVO.setUser(userVO);
        }

        // 关联查询空间信息
        Long spaceId = spaceUser.getSpaceId();
        if (spaceId != null && spaceId > 0) {
            Space space = spaceService.getById(spaceId);
            SpaceVO spaceVO = spaceService.getSpaceVO(space, request);
            spaceUserVO.setSpace(spaceVO);
        }
        return spaceUserVO;
    }

    /**
     * 批量获取空间用户关联的封装视图对象列表
     *
     * @param spaceUserList 空间用户关联实体对象列表
     * @return 空间用户关联的视图对象列表
     */
    @Override
    public List<SpaceUserVO> getSpaceUserVOList(List<SpaceUser> spaceUserList) {
        // 判断输入列表是否为空
        if (CollUtil.isEmpty(spaceUserList)) {
            return Collections.emptyList();
        }

        // 对象列表 => 封装对象列表
        List<SpaceUserVO> spaceUserVOList = spaceUserList.stream()
                .map(SpaceUserVO::objToVo)
                .toList();

        // 1. 收集需要关联查询的用户 ID 和空间ID
        Set<Long> userIdSet = spaceUserList.stream()
                .map(SpaceUser::getUserId)
                .collect(Collectors.toSet());
        Set<Long> spaceIdSet = spaceUserList.stream()
                .map(SpaceUser::getSpaceId)
                .collect(Collectors.toSet());
        // 2. 批量查询用户和空间信息
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        Map<Long, List<Space>> spaceIdSpaceListMap = spaceService.listByIds(spaceIdSet).stream()
                .collect(Collectors.groupingBy(Space::getId));
        // 3. 填充 SpaceUserVO 的用户和空间信息
        spaceUserVOList.forEach(spaceUserVO -> {
            Long userId = spaceUserVO.getUserId();
            Long spaceId = spaceUserVO.getSpaceId();
            // 填充用户信息
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            spaceUserVO.setUser(userService.getUserVO(user));

            // 填充空间信息
            Space space = null;
            if (spaceIdSpaceListMap.containsKey(spaceId)) {
                space = spaceIdSpaceListMap.get(spaceId).get(0);
            }

            spaceUserVO.setSpace(SpaceVO.objToVo(space));
        });
        return spaceUserVOList;
    }
}





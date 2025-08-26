package com.picturebackend.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.picturebackend.common.BaseResponse;
import com.picturebackend.common.DeleteRequest;
import com.picturebackend.common.ResultUtils;
import com.picturebackend.exception.BusinessException;
import com.picturebackend.exception.ErrorCode;
import com.picturebackend.exception.ThrowUtils;
import com.picturebackend.manager.auth.annotation.SaSpaceCheckPermission;
import com.picturebackend.manager.auth.model.SpaceUserPermissionConstant;
import com.picturebackend.model.dto.spaceuser.SpaceUserAddRequest;
import com.picturebackend.model.dto.spaceuser.SpaceUserEditRequest;
import com.picturebackend.model.dto.spaceuser.SpaceUserQueryRequest;
import com.picturebackend.model.entity.SpaceUser;
import com.picturebackend.model.entity.User;
import com.picturebackend.model.vo.spaceuser.SpaceUserVO;
import com.picturebackend.service.SpaceUserService;
import com.picturebackend.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * packageName: com.picturebackend.controller
 *
 * @author: idpeng
 * @version: 1.0
 * @className: SpaceUserController
 * @date: 2025/8/25 21:06
 * @description: 团队空间接口
 */
@RestController
@RequestMapping("/spaceUser")
@Slf4j
@Api(tags = "团队空间接口")
public class SpaceUserController {

    @Resource
    private SpaceUserService spaceUserService;

    @Resource
    private UserService userService;

    /**
     * 添加空间成员
     *
     * @param spaceUserAddRequest 添加空间成员请求参数
     * @param request HTTP请求对象，用于获取当前登录用户信�?
     * @return 新增的空间成员关系ID
     */
    @ApiOperation("添加空间成员")
    @PostMapping("/add")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResponse<Long> addSpaceUser(@RequestBody SpaceUserAddRequest spaceUserAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceUserAddRequest == null, ErrorCode.PARAMS_ERROR);
        long id = spaceUserService.addSpaceUser(spaceUserAddRequest);
        return ResultUtils.success(id);
    }

    /**
     * 删除空间成员
     *
     * @param deleteRequest 删除请求参数，包含要删除的空间成员ID
     * @param request HTTP请求对象，用于权限验证等操作
     * @return 是否删除成功的结�?
     */
    @ApiOperation("删除空间成员")
    @PostMapping("/delete")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResponse<Boolean> deleteSpaceUser(@RequestBody DeleteRequest deleteRequest,
                                                 HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = deleteRequest.getId();
        // 判断是否存在
        SpaceUser oldSpaceUser = spaceUserService.getById(id);
        ThrowUtils.throwIf(oldSpaceUser == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据�?
        boolean result = spaceUserService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 获取空间成员信息
     *
     * @param spaceUserQueryRequest 空间成员查询请求参数
     * @return 空间成员信息
     */
    @ApiOperation("获取空间成员信息")
    @PostMapping("/get")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResponse<SpaceUser> getSpaceUser(@RequestBody SpaceUserQueryRequest spaceUserQueryRequest) {
        // 参数校验
        ThrowUtils.throwIf(spaceUserQueryRequest == null, ErrorCode.PARAMS_ERROR);
        Long spaceId = spaceUserQueryRequest.getSpaceId();
        Long userId = spaceUserQueryRequest.getUserId();
        ThrowUtils.throwIf(ObjectUtil.hasEmpty(spaceId, userId), ErrorCode.PARAMS_ERROR);
        // 查询数据�?
        SpaceUser spaceUser = spaceUserService.getOne(spaceUserService.getQueryWrapper(spaceUserQueryRequest));
        ThrowUtils.throwIf(spaceUser == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(spaceUser);
    }

    /**
     * 获取空间成员列表
     *
     * @param spaceUserQueryRequest 空间成员查询请求参数
     * @param request HTTP请求对象
     * @return 空间成员列表视图对象
     */
    @ApiOperation("获取空间成员列表")
    @PostMapping("/list")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResponse<List<SpaceUserVO>> listSpaceUser(@RequestBody SpaceUserQueryRequest spaceUserQueryRequest,
                                                         HttpServletRequest request) {
        ThrowUtils.throwIf(spaceUserQueryRequest == null, ErrorCode.PARAMS_ERROR);
        List<SpaceUser> spaceUserList = spaceUserService.list(
                spaceUserService.getQueryWrapper(spaceUserQueryRequest)
        );
        return ResultUtils.success(spaceUserService.getSpaceUserVOList(spaceUserList));
    }

    /**
     * 编辑空间成员信息(设置权限�?
     *
     * @param spaceUserEditRequest 空间成员编辑请求参数
     * @param request HTTP请求对象，用于权限验证等操作
     * @return 是否编辑成功的结�?
     */
    @ApiOperation("编辑空间成员信息(设置权限�?)
    @PostMapping("/edit")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResponse<Boolean> editSpaceUser(@RequestBody SpaceUserEditRequest spaceUserEditRequest,
                                               HttpServletRequest request) {
        if (spaceUserEditRequest == null || spaceUserEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 将实体类�?DTO 进行转换
        SpaceUser spaceUser = new SpaceUser();
        BeanUtil.copyProperties(spaceUserEditRequest, spaceUser);
        // 数据校验
        spaceUserService.validSpaceUser(spaceUser, false);
        // 判断是否存在
        Long id = spaceUserEditRequest.getId();
        SpaceUser oldSpaceUser = spaceUserService.getById(id);
        ThrowUtils.throwIf(oldSpaceUser == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据�?
        boolean result = spaceUserService.updateById(spaceUser);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 获取当前用户加入的团队空间列�?
     *
     * @param request HTTP请求对象，用于获取当前登录用户信�?
     * @return 当前用户加入的团队空间列�?
     */
    @ApiOperation("获取当前用户加入的团队空间列�?)
    @PostMapping("/list/my")
    public BaseResponse<List<SpaceUserVO>> listMyTeamSpace(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        SpaceUserQueryRequest spaceUserQueryRequest = new SpaceUserQueryRequest();
        spaceUserQueryRequest.setUserId(loginUser.getId());
        List<SpaceUser> spaceUserList = spaceUserService.list(
                spaceUserService.getQueryWrapper(spaceUserQueryRequest)
        );
        return ResultUtils.success(spaceUserService.getSpaceUserVOList(spaceUserList));
    }

}

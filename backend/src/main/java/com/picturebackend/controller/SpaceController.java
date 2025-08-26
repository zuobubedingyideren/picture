package com.picturebackend.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.picturebackend.annotation.AuthCheck;
import com.picturebackend.common.BaseResponse;
import com.picturebackend.common.DeleteRequest;
import com.picturebackend.common.ResultUtils;
import com.picturebackend.constant.UserConstant;
import com.picturebackend.exception.BusinessException;
import com.picturebackend.exception.ErrorCode;
import com.picturebackend.exception.ThrowUtils;
import com.picturebackend.manager.auth.SpaceUserAuthManager;
import com.picturebackend.model.dto.space.*;
import com.picturebackend.model.entity.Space;
import com.picturebackend.model.entity.User;
import com.picturebackend.model.enums.SpaceLevelEnum;
import com.picturebackend.model.vo.space.SpaceVO;
import com.picturebackend.service.SpaceService;
import com.picturebackend.service.SpaceUserService;
import com.picturebackend.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * packageName: com.picturebackend.controller
 *
 * @author: idpeng
 * @version: 1.0
 * @className: SpaceCon؜troller
 * @date: 2025/8/22 16:20
 * @description: 空间接口开�?
 */
@Slf4j
@RestController("/space")
@Api(tags = "空间接口")
public class SpaceController {

    @Resource
    private SpaceService spaceService;

    @Resource
    private UserService userService;

    @Resource
    private SpaceUserAuthManager spaceUserAuthManager;

    /**
     * 添加空间
     *
     * @param spaceAddRequest 空间添加请求参数
     * @param request         HTTP请求对象
     * @return 新创建空间的ID
     */
    @PostMapping("/add")
    @ApiOperation("添加空间")
    public BaseResponse<Long> addSpace(@RequestBody SpaceAddRequest spaceAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceAddRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Long newId = spaceService.addSpace(spaceAddRequest, loginUser);
        return ResultUtils.success(newId);
    }

    /**
     * 删除空间
     *
     * @param deleteRequest 删除请求参数，包含要删除的空间ID
     * @param request       HTTP请求对象，用于获取当前登录用户信�?
     * @return 删除结果，成功返回true
     */
    @PostMapping("/delete")
    @ApiOperation("删除空间")
    public BaseResponse<Boolean> deleteSpace(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(!spaceService.deleteSpace(deleteRequest, loginUser), ErrorCode.SYSTEM_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新空间信息
     *
     * @param spaceUpdateRequest 空间更新请求参数
     * @return 更新结果，成功返回true
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @ApiOperation("更新空间信息")
    public BaseResponse<Boolean> updateSpace(@RequestBody SpaceUpdateRequest spaceUpdateRequest) {
        if (spaceUpdateRequest == null || spaceUpdateRequest.getId() < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 将实体类�?DTO 进行转换
        Space space = new Space();
        BeanUtil.copyProperties(spaceUpdateRequest, space);

        // 自动填充数据
        spaceService.fillSpaceBySpaceLevel(space);

        // 数据校验
        spaceService.validSpace(space, false);

        // 判断是否存在
        Long id = spaceUpdateRequest.getId();
        Space oldSpace = spaceService.getById(id);
        ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR);

        // 操作数据�?
        boolean result = spaceService.updateById(space);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据ID获取空间信息
     *
     * @param id      空间ID
     * @param request HTTP请求对象
     * @return 空间信息
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @ApiOperation("根据id获取空间信息（管理员�?)
    public BaseResponse<Space> getSpaceById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据�?
        Space space = spaceService.getById(id);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装�?
        return ResultUtils.success(space);
    }

    /**
     * 根据ID获取空间VO信息
     *
     * @param id      空间ID
     * @param request HTTP请求对象
     * @return 空间VO信息
     */
    @GetMapping("/get/vo")
    @ApiOperation("根据id查询空间VO信息")
    public BaseResponse<SpaceVO> getSpaceVOById(Long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据�?
        Space space = spaceService.getById(id);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR);
        SpaceVO spaceVO = spaceService.getSpaceVO(space, request);
        User loginUser = userService.getLoginUser(request);
        List<String> permissionList = spaceUserAuthManager.getPermissionList(space, loginUser);
        spaceVO.setPermissionList(permissionList);
        return ResultUtils.success(spaceVO);
    }

    /**
     * 分页查询空间列表
     *
     * @param spaceQueryRequest 空间查询请求参数，包含分页信息和查询条件
     * @return 空间分页数据
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @ApiOperation("分页查询空间列表（管理员�?)
    public BaseResponse<Page<Space>> listSpaceByPage(@RequestBody SpaceQueryRequest spaceQueryRequest) {
        int current = spaceQueryRequest.getCurrent();
        int pageSize = spaceQueryRequest.getPageSize();
        // 查询数据�?
        Page<Space> spacePage = spaceService.page(new Page<>(current, pageSize), spaceService.getQueryWrapper(spaceQueryRequest));
        return ResultUtils.success(spacePage);
    }

    /**
     * 分页查询空间VO列表
     *
     * @param spaceQueryRequest 空间查询请求参数，包含分页信息和查询条件
     * @param request HTTP请求对象
     * @return 空间VO分页数据
     */
    @PostMapping("/list/page/vo")
    @ApiOperation("分页查询空间列表")
    public BaseResponse<Page<SpaceVO>> listSpaceVOByPage(@RequestBody SpaceQueryRequest spaceQueryRequest, HttpServletRequest request) {
        int current = spaceQueryRequest.getCurrent();
        int pageSize = spaceQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据�?
        Page<Space> spacePage = spaceService.page(new Page<>(current, pageSize), spaceService.getQueryWrapper(spaceQueryRequest));
        // 获取封装�?
        Page<SpaceVO> spaceVOPage = spaceService.getSpaceVOPage(spacePage, request);
        return ResultUtils.success(spaceVOPage);
    }

    /**
     * 编辑空间信息
     *
     * @param spaceEditRequest 空间编辑请求参数
     * @param request HTTP请求对象，用于获取当前登录用户信�?
     * @return 编辑结果，成功返回true
     */
    @PostMapping("/edit")
    @ApiOperation("编辑空间信息")
    public BaseResponse<Boolean> editSpace(@RequestBody SpaceEditRequest spaceEditRequest, HttpServletRequest request) {
        if (spaceEditRequest == null || spaceEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 在此处将实体类和 DTO 进行转换
        Space space = new Space();
        BeanUtil.copyProperties(spaceEditRequest, space);

        // 自动填充数据
        spaceService.fillSpaceBySpaceLevel(space);

        // 设置编辑时间
        space.setEditTime(new Date());

        // 数据校验
        spaceService.validSpace(space, false);
        User loginUser = userService.getLoginUser(request);

        // 判断是否存在
        Long id = spaceEditRequest.getId();
        Space oldSpace = spaceService.getById(id);
        ThrowUtils.throwIf(oldSpace == null, ErrorCode.NOT_FOUND_ERROR);

        // 仅本人或管理员可编辑
        spaceService.checkSpaceAuth(loginUser, oldSpace);

        // 操作数据�?
        boolean result = spaceService.updateById(space);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 获取所有空间级别信息列�?
     *
     * @return 空间级别信息列表
     */
    @GetMapping("/list/level")
    @ApiOperation("获取所有空间信息列�?)
    public BaseResponse<List<SpaceLevel>> listSpaceLevel() {
        List<SpaceLevel> spaceLevelList = Arrays.stream(SpaceLevelEnum.values())
                .map(spaceLevelEnum -> new SpaceLevel(
                spaceLevelEnum.getValue(),
                spaceLevelEnum.getText(),
                spaceLevelEnum.getMaxCount(),
                spaceLevelEnum.getMaxSize()))
                .collect(Collectors.toList());
        return ResultUtils.success(spaceLevelList);
    }
}

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
import com.picturebackend.model.dto.user.*;
import com.picturebackend.model.entity.User;
import com.picturebackend.model.vo.user.LoginUserVO;
import com.picturebackend.model.vo.user.UserVO;
import com.picturebackend.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * packageName: com.picturebackend.controller
 *
 * @author: idpeng
 * @version: 1.0
 * @className: UserController
 * @date: 2025/7/9 20:07
 * @description: 用户控制�?
 */
@Api(tags = "用户服务接口")
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 处理用户注册请求的端�?
     * 该方法使用@PostMapping注解，表明它处理POST请求中的/register路径
     * 请求体应包含用户注册所需的信息，如用户名和密�?
     *
     * @param userRegisterRequest 包含用户注册信息的请求体，包括用户账号和密码
     * @return 返回一个BaseResponse对象，其中包含新注册用户的ID
     */
    @ApiOperation("用户注册")
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        // 检查传入的注册请求是否为空，如果为空，则抛出参数错误异�?
        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR);

        // 从注册请求中提取用户账号、密码和确认密码
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();

        // 调用用户服务中的注册方法，传入用户账号、密码和确认密码，返回注册结�?
        long result = userService.userRegister(userAccount, userPassword, checkPassword);

        // 构建并返回一个成功的响应，其中包含新注册用户的ID
        return ResultUtils.success(result);
    }

    /**
     * 处理用户登录请求的控制器方法
     * <p>
     * 该方法接收用户登录信息，验证用户身份，并返回登录用户的相关信�?
     *
     * @param userLoginRequest 包含用户登录信息的请求对象，包括用户账号和密�?
     * @param request          HTTP请求对象，用于获取请求相关的信息
     * @return 返回一个包含登录用户信息的响应对象如果登录成功，否则返回相应的错误信息
     */
    @ApiOperation("用户登录")
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        // 检查登录请求是否为空，如果为空则抛出参数错误异�?
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);
        // 从注册请求中提取用户账号、密码和确认密码
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();

        // 调用用户服务中的登录方法，执行用户登录逻辑
        LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, request);

        // 构建并返回一个成功的响应，其中包含新注册用户的ID
        return ResultUtils.success(loginUserVO);
    }

    /**
     * 处理获取登录用户信息的HTTP GET请求
     * 该方法接收HttpServletRequest对象作为参数，用于获取当前请求的相关信息
     * 返回一个包装了登录用户信息的BaseResponse对象，其中包含了一个LoginUserVO对象
     *
     * @param request 用于获取当前HTTP请求信息的HttpServletRequest对象
     * @return 包含登录用户信息的BaseResponse对象
     */
    @ApiOperation("获取登陆用户信息")
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        // 调用userService的getLoginUser方法获取登录用户信息
        User user = userService.getLoginUser(request);
        // 使用获取到的用户信息调用userService的getLoginUserVO方法构建登录用户视图对象，并返回成功结果
        return ResultUtils.success(userService.getLoginUserVO(user));
    }


    /**
     * 用户登出
     *
     * @param request HTTP请求对象，包含用户会话信�?
     * @return Boolean 登出是否成功
     */
    @ApiOperation("用户登出")
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    /**
     * 管理员添加用�?
     *
     * @param userAddRequest 用户添加请求参数
     * @return Long 新增用户的ID
     */
    @ApiOperation("管理员添加用�?)
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest) {
        ThrowUtils.throwIf(userAddRequest == null, ErrorCode.PARAMS_ERROR);
        User user = new User();
        BeanUtil.copyProperties(userAddRequest, user);

        // 默认密码 12345678
        final String DEFAULT_PASSWORD = "123456";
        String encryptPassword = userService.getEncryptPassword(DEFAULT_PASSWORD);
        user.setUserPassword(encryptPassword);
        boolean save = userService.save(user);
        ThrowUtils.throwIf(!save, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(user.getId());
    }

    /**
     * 管理员根据ID获取用户信息
     *
     * @param id 用户ID
     * @return User 用户信息
     */
    @ApiOperation("管理员根据ID获取用户信息")
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUserById(long id) {
        // 参数校验
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        
        // 查询用户信息
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(user);
    }

    /**
     * 根据用户ID获取用户VO信息
     *
     * @param id 用户ID
     * @return 用户VO信息
     */
    @ApiOperation("根据用户ID获取用户VO信息")
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(long id) {
        BaseResponse<User> response = getUserById(id);
        User user = response.getData();
        return ResultUtils.success(userService.getUserVO(user));
    }

    /**
     * 删除用户（仅管理员）
     *
     * @param deleteRequest 删除请求参数，包含要删除的用户ID
     * @return 是否删除成功
     */
    @ApiOperation("删除用户（仅管理员）")
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(deleteRequest.getId());
        ThrowUtils.throwIf(!b, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(b);
    }

    /**
     * 更新用户
     */
    @ApiOperation("更新用户")
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 分页获取用户封装列表（仅管理员）
     *
     * @param userQueryRequest 查询请求参数
     */
    @ApiOperation("分页获取用户封装列表（仅管理员）")
    @PostMapping("/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long current = userQueryRequest.getCurrent();
        long pageSize = userQueryRequest.getPageSize();
        Page<User> userPage = userService.page(new Page<>(current, pageSize),
                userService.getQueryWrapper(userQueryRequest));
        Page<UserVO> userVOPage = new Page<>(current, pageSize, userPage.getTotal());
        List<UserVO> userVOList = userService.getUserVOList(userPage.getRecords());
        userVOPage.setRecords(userVOList);
        return ResultUtils.success(userVOPage);
    }
}

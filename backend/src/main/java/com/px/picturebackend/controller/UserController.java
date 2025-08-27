package com.px.picturebackend.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.px.picturebackend.annotation.AuthCheck;
import com.px.picturebackend.common.BaseResponse;
import com.px.picturebackend.common.DeleteRequest;
import com.px.picturebackend.common.ResultUtils;
import com.px.picturebackend.constant.UserConstant;
import com.px.picturebackend.exception.BusinessException;
import com.px.picturebackend.exception.ErrorCode;
import com.px.picturebackend.exception.ThrowUtils;
import com.px.picturebackend.model.dto.user.*;
import com.px.picturebackend.model.entity.User;
import com.px.picturebackend.model.vo.user.LoginUserVO;
import com.px.picturebackend.model.vo.user.UserVO;
import com.px.picturebackend.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.px.picturebackend.constant.UserConstants.DEFAULT_PASSWORD;

/**
 * packageName: com.px.picturebackend.controller    
 *
 * @author: idpeng
 * @version: 1.0
 * @className: UserController
 * @date: 2025/7/9 20:07
 * @description: 用户控制器
 */
@Api(tags = "用户服务接口")
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Resource
    private UserService userService;
    

    /**
     * 处理用户注册请求的端点
     * 该方法使用@PostMapping注解，表明它处理POST请求中的/register路径
     * 请求体应包含用户注册所需的信息，如用户名和密码
     *
     * @param userRegisterRequest 包含用户注册信息的请求体，包括用户账号和密码
     * @return 返回一个BaseResponse对象，其中包含新注册用户的ID
     */
    @ApiOperation("用户注册")
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        // 性能优化：快速参数校验，避免不必要的try-catch开销
        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR);

        // 提前提取参数，减少重复访问开销
        final String userAccount = userRegisterRequest.getUserAccount();
        final String userPassword = userRegisterRequest.getUserPassword();
        final String checkPassword = userRegisterRequest.getCheckPassword();

        // 轻量级非空检查，避免异常处理开销
        ThrowUtils.throwIf(StrUtil.hasBlank(userAccount, userPassword, checkPassword), ErrorCode.PARAMS_ERROR, "参数为空");

        // 核心业务逻辑：保留完整异常防护体系
        try {
            // 调用用户服务中的注册方法
            long result = userService.userRegister(userAccount, userPassword, checkPassword);
            
            // 性能优化：成功路径直接返回，减少日志开销
            if (log.isInfoEnabled()) {
                log.info("用户注册成功，用户账号: {}", userAccount);
            }
            return ResultUtils.success(result);
            
        } catch (BusinessException e) {
            // 安全优化：业务异常精确处理
            log.warn("用户注册业务异常，用户账号: {}, 错误: {}", userAccount, e.getMessage());
            throw e;
        } catch (Exception e) {
            // 安全优化：系统异常统一处理
            log.error("用户注册系统异常，用户账号: {}", userAccount, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册服务暂时不可用");
        }
    }

    /**
     * 处理用户登录请求的控制器方法
     * <p>
     * 该方法接收用户登录信息，验证用户身份，并返回登录用户的相关信息
     *
     * @param userLoginRequest 包含用户登录信息的请求对象，包括用户账号和密码
     * @param request          HTTP请求对象，用于获取请求相关的信息
     * @return 返回一个包含登录用户信息的响应对象
     */
    @ApiOperation("用户登录")
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        // 检查登录请求是否为空，如果为空则抛出参数错误异常
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);
        
        // 提前提取参数，避免重复访问
        final String userAccount = userLoginRequest.getUserAccount();
        final String userPassword = userLoginRequest.getUserPassword();

        // 轻量级参数检查，修正逻辑错误
        ThrowUtils.throwIf(StrUtil.hasBlank(userAccount, userPassword), ErrorCode.PARAMS_ERROR, "参数为空");

        // 登录是安全关键操作，保留完整异常防护体系
        try {
            LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, request);
            
            // 性能优化：条件日志记录
            if (log.isInfoEnabled()) {
                log.info("用户登录成功，用户账号: {}", userAccount);
            }
            return ResultUtils.success(loginUserVO);
            
        } catch (BusinessException e) {
            // 安全优化：登录业务异常精确处理（避免敏感信息泄露）
            log.warn("用户登录失败，用户账号: {}, 原因: {}", userAccount, e.getCode());
            throw e;
        } catch (Exception e) {
            // 安全优化：系统异常保护
            log.error("用户登录系统异常，用户账号: {}", userAccount, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "登录服务暂时不可用");
        }
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
        // 性能优化：高频接口减少try-catch开销，采用直接处理
        User user = userService.getLoginUser(request);
        
        // 快速路径：用户未登录情况
        if (user == null) {
            if (log.isDebugEnabled()) {
                log.debug("获取登录用户信息失败，用户未登录或会话已过期");
            }
            return ResultUtils.success(null);
        }
        try{
            // 使用获取到的用户信息调用userService的getLoginUserVO方法构建登录用户视图对象，并返回成功结果
            LoginUserVO loginUserVO = userService.getLoginUserVO(user);
            if (log.isDebugEnabled()) {
                log.debug("获取登录用户信息成功，用户ID: {}", user.getId());
            }
            return ResultUtils.success(loginUserVO);
        } catch (Exception e) {
            // 安全优化：仅在数据转换异常时进行保护
            log.error("用户信息转换异常，用户ID: {}", user.getId(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "用户信息获取失败");
        }
    }


    /**
     * 用户登出
     *
     * @param request HTTP请求对象，包含用户会话信息
     * @return Boolean 登出是否成功
     */
    @ApiOperation("用户登出")
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        // 性能优化：快速参数校验
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        
        // 性能优化：登出操作简单，减少异常处理开销
        boolean result = userService.userLogout(request);
        
        if (log.isDebugEnabled()) {
            log.debug("用户登出操作完成，结果: {}", result);
        }
        return ResultUtils.success(result);
    }

    /**
     * 管理员添加用户
     *
     * @param userAddRequest 用户添加请求参数
     * @return Long 新增用户的ID
     */
    @ApiOperation("管理员添加用户")
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest) {
        // 快速参数校验
        ThrowUtils.throwIf(userAddRequest == null, ErrorCode.PARAMS_ERROR);
        
        try {
            // 性能优化：减少对象创建开销
            User user = new User();
            BeanUtil.copyProperties(userAddRequest, user);
            
            // 性能优化：密码加密操作提前处理
            user.setUserPassword(userService.getEncryptPassword(DEFAULT_PASSWORD));
            
            boolean save = userService.save(user);
            ThrowUtils.throwIf(!save, ErrorCode.OPERATION_ERROR);
            
            if (log.isInfoEnabled()) {
                log.info("管理员添加用户成功，用户ID: {}", user.getId());
            }
            return ResultUtils.success(user.getId());
            
        } catch (BusinessException e) {
            // 安全优化：业务异常精确处理
            log.warn("管理员添加用户业务异常: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            // 安全优化：系统异常保护
            log.error("管理员添加用户系统异常", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "用户添加失败");
        }
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
        // 快速参数校验
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);

        
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        
        if (log.isDebugEnabled()) {
            log.debug("管理员获取用户信息成功，用户ID: {}", id);
        }
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
        // 快速参数校验
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);

            // 查询用户信息
            User user = userService.getById(id);
            ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");

        // 仅在数据转换时保护
        try {
            UserVO userVO = userService.getUserVO(user);
            if (log.isDebugEnabled()) {
                log.debug("获取用户VO信息成功，用户ID: {}", id);
            }
            return ResultUtils.success(userVO);
        } catch (Exception e) {
            log.error("用户VO转换异常，用户ID: {}", id, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "用户信息获取失败");
        }
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
        // 快速参数校验
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        final Long userId = deleteRequest.getId();
        
        try {
            boolean isDeleted = userService.removeById(userId);
            ThrowUtils.throwIf(!isDeleted, ErrorCode.OPERATION_ERROR);
            
            if (log.isInfoEnabled()) {
                log.info("管理员删除用户成功，用户ID: {}", userId);
            }
            return ResultUtils.success(isDeleted);
            
        } catch (BusinessException e) {
            // 安全优化：业务异常精确处理
            log.warn("管理员删除用户业务异常，用户ID: {}, 原因: {}", userId, e.getMessage());
            throw e;
        } catch (Exception e) {
            // 安全优化：系统异常保护
            log.error("管理员删除用户系统异常，用户ID: {}", userId, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "用户删除失败");
        }
    }



    /**
     * 更新用户信息（仅管理员）
     * 该方法允许管理员更新用户的基本信息，如用户名、用户角色等
     *
     * @param userUpdateRequest 用户更新请求对象，包含要更新的用户ID和新信息
     * @return Boolean 更新是否成功的结果
     */
    @ApiOperation("更新用户")
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        // 快速参数校验
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        final Long userId = userUpdateRequest.getId();
        
        try {
            User user = new User();
            BeanUtils.copyProperties(userUpdateRequest, user);
            
            boolean result = userService.updateById(user);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
            
            if (log.isInfoEnabled()) {
                log.info("管理员更新用户信息成功，用户ID: {}", userId);
            }
            return ResultUtils.success(result);
            
        } catch (BusinessException e) {
            // 安全优化：业务异常精确处理
            log.warn("管理员更新用户业务异常，用户ID: {}, 原因: {}", userId, e.getMessage());
            throw e;
        } catch (Exception e) {
            // 安全优化：系统异常保护
            log.error("管理员更新用户系统异常，用户ID: {}", userId, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "用户更新失败");
        }
    }



    /**
     * 用户更新自己的信息
     * 性能优化：用户操作频率中等，采用平衡的异常处理策略
     * 安全优化：个人信息更新，保持适度异常防护
     *
     * @param userUpdateMyInfoRequest 用户更新自己信息的请求参数
     * @param request HTTP请求对象，用于获取当前登录用户信息
     * @return 更新结果，成功返回true
     */
    @ApiOperation("用户更新自己的信息")
    @PostMapping("/update/my_info")
    public BaseResponse<Boolean> updateMyInfo(@RequestBody UserUpdateMyInfoRequest userUpdateMyInfoRequest, HttpServletRequest request) {
        // 快速参数校验
        ThrowUtils.throwIf(userUpdateMyInfoRequest == null, ErrorCode.PARAMS_ERROR);
        
        try {
            // 获取当前登录用户
            User loginUser = userService.getLoginUser(request);
            User user = new User();
            BeanUtils.copyProperties(userUpdateMyInfoRequest, user);
            user.setId(loginUser.getId());
            
            boolean result = userService.updateById(user);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
            
            if (log.isInfoEnabled()) {
                log.info("用户更新个人信息成功，用户ID: {}", loginUser.getId());
            }
            return ResultUtils.success(result);
            
        } catch (BusinessException e) {
            // 安全优化：业务异常精确处理
            log.warn("用户更新个人信息业务异常: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            // 安全优化：系统异常保护
            log.error("用户更新个人信息系统异常", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "个人信息更新失败");
        }
    }


    /**
     * 分页获取用户封装列表（仅管理员）
     * 性能优化：分页查询可能涉及大量数据，采用优化的异常处理
     * 安全优化：管理员查询，保持适度异常防护
     *
     * @param userQueryRequest 查询请求参数
     * @return 分页的用户VO列表
     */
    @ApiOperation("分页获取用户封装列表（仅管理员）")
    @PostMapping("/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest) {
        // 快速参数校验
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);

        // 性能优化：提前提取分页参数，减少重复访问
        final long current = userQueryRequest.getCurrent();
        final long pageSize = userQueryRequest.getPageSize();

        // 轻量级边界检查
        ThrowUtils.throwIf(current <= 0, ErrorCode.PARAMS_ERROR, "页码必须大于0");
        ThrowUtils.throwIf(pageSize <= 0 || pageSize > 100, ErrorCode.PARAMS_ERROR, "每页大小必须在1-100之间");

        try {
            // 性能优化：减少对象创建，复用Page对象
            Page<User> userPage = userService.page(new Page<>(current, pageSize),
                    userService.getQueryWrapper(userQueryRequest));
            
            // 性能优化：批量转换，减少单个转换开销
            Page<UserVO> userVOPage = new Page<>(current, pageSize, userPage.getTotal());
            List<UserVO> userVOList = userService.getUserVOList(userPage.getRecords());
            userVOPage.setRecords(userVOList);
            
            if (log.isDebugEnabled()) {
                log.debug("管理员分页获取用户列表成功，页码: {}, 每页大小: {}, 总数: {}", current, pageSize, userVOPage.getTotal());
            }
            return ResultUtils.success(userVOPage);
            
        } catch (Exception e) {
            // 安全优化：分页查询异常统一处理
            log.error("管理员分页获取用户列表失败，页码: {}, 每页大小: {}", current, pageSize, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "用户列表查询失败");
        }
    }


    /**
     * 用户头像上传接口
     * 普通用户上传头像，只需要登录即可，不需要管理员权限
     * 业务逻辑已抽取到AvatarService层，Controller层仅处理请求响应
     *
     * @param multipartFile 上传的头像文件
     * @param request HTTP请求对象，用于获取当前登录用户信息
     * @return 上传成功后返回文件访问路径
     */
    @ApiOperation("用户头像上传")
    @PostMapping("/upload/avatar")
    public BaseResponse<String> uploadAvatar(@RequestPart("file") MultipartFile multipartFile, HttpServletRequest request) {
        // 性能优化：快速用户验证
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        }
        try{
            // 调用AvatarService处理头像上传业务逻辑
            String fileUrl = userService.uploadAvatar(multipartFile, loginUser);
            
            if (log.isInfoEnabled()) {
                log.info("用户上传头像成功，用户ID: {}", loginUser.getId());
            }
            return ResultUtils.success(fileUrl);
            
        } catch (BusinessException e) {
            // 安全优化：文件上传业务异常精确处理
            log.warn("用户上传头像业务异常，用户ID: {}, 原因: {}", loginUser.getId(), e.getMessage());
            throw e;
        } catch (Exception e) {
            // 安全优化：文件上传系统异常保护
            log.error("用户上传头像系统异常，用户ID: {}", loginUser.getId(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "头像上传失败");
        }
    }

}

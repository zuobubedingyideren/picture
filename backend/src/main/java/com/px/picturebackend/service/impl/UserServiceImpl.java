package com.px.picturebackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.px.picturebackend.config.CosClientConfig;
import com.px.picturebackend.exception.BusinessException;
import com.px.picturebackend.exception.ErrorCode;
import com.px.picturebackend.exception.ThrowUtils;
import com.px.picturebackend.manager.CosManager;
import com.px.picturebackend.manager.auth.StpKit;
import com.px.picturebackend.mapper.UserMapper;
import com.px.picturebackend.model.dto.user.UserQueryRequest;
import com.px.picturebackend.model.entity.User;
import com.px.picturebackend.model.enums.UserRoleEnum;
import com.px.picturebackend.model.vo.user.LoginUserVO;
import com.px.picturebackend.model.vo.user.UserVO;
import com.px.picturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static com.px.picturebackend.constant.UserConstant.USER_LOGIN_STATE;


/**
* @author idpeng
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2025-07-09 19:20:03
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

    @Resource
    private CosManager cosManager;

    @Resource
    private CosClientConfig cosClientConfig;

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 确认密码
     * @return 用户ID
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1. 校验
        if (StrUtil.hasBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }

       /*  if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        } */

        ThrowUtils.throwIf(userAccount.length()< 4, ErrorCode.PARAMS_ERROR, "用户账号过短");

        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }

        /* if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        } */
        ThrowUtils.throwIf(!userPassword.equals(checkPassword), ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");

        // 2. 检查是否重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = this.baseMapper.selectCount(queryWrapper);
        /* if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
        } */
        ThrowUtils.throwIf(count > 0 , ErrorCode.PARAMS_ERROR, "账号");
        // 3. 加密
        String encryptPassword = getEncryptPassword(userPassword);
        // 4. 插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserName("无名");
        user.setUserProfile(UserRoleEnum.USER.getValue());
        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
        }

        return user.getId();
    }

    /**
     * 获取加密后的密码
     *
     * @param userPassword 用户原始密码
     * @return 加密后的密码
     */
    @Override
    public String getEncryptPassword(String userPassword) {
        final String SAL = "tiantianxiangshang";
        return DigestUtils.md5DigestAsHex((SAL + userPassword).getBytes());
    }

    /**
     * 用户登录服务
     * 根据用户账号和密码进行登录验证，登录成功后记录用户登录状态并返回登录用户信息
     *
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     * @param request HTTP请求对象，用于记录登录状态
     * @return 登录用户信息视图对象
     */
    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StrUtil.hasBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        // 2. 加密
        String encryptPassword = getEncryptPassword(userPassword);
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = this.baseMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或者密码错误");
        }
        // 3. 记录用户登录状态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        // 4. 记录用户登录态到 Sa-token，便于空间鉴权时使用，注意保证该用户信息与SpringSession 中的信息过期时间一致
        StpKit.SPACE.login(user.getId());
        StpKit.SPACE.getSession().set(USER_LOGIN_STATE, user);
        return this.getLoginUserVO(user);
    }

    /**
     * 获取登录用户信息视图对象
     * 将用户实体对象转换为登录用户信息视图对象
     *
     * @param user 用户实体对象
     * @return 登录用户信息视图对象，如果用户为空则返回null
     */
    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtil.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    /**
     * 获取当前登录用户
     * 从Session中获取登录用户信息，并从数据库查询最新用户信息
     *
     * @param request HTTP请求对象，用于获取Session中的用户信息
     * @return 当前登录用户信息
     * @throws BusinessException 未登录时抛出异常
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 判断有没有登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 从数据库查询（追求性能的话可以注释，直接返回上述结果）
        long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }

    /**
     * 用户注销登录
     * 清除Session中保存的用户登录状态信息
     *
     * @param request HTTP请求对象，用于获取Session并清除登录状态
     * @return 注销成功返回true
     * @throws BusinessException 未登录时抛出异常
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        // 判断有没有登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (userObj == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }
        // 注销登录
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        StpKit.SPACE.logout(((User) userObj).getId());
        return true;
    }

    /**
     * 将用户实体转换为用户视图对象
     * 用于将用户实体信息转换为对外展示的视图对象
     *
     * @param user 用户实体对象
     * @return 用户视图对象，如果用户实体为空则返回null
     */
    @Override
    public UserVO getUserVO(User user) {
       if (user == null) {
           return null;
       }
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);
        return userVO;
    }

    /**
     * 将用户实体列表转换为用户视图对象列表
     * 使用流式处理将用户实体集合批量转换为用户视图对象集合
     *
     * @param userList 用户实体列表
     * @return 用户视图对象列表，如果输入列表为空则返回空列表
     */
    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        if (CollectionUtils.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream()
                       .map(this::getUserVO)
                       .collect(Collectors.toList());
    }

    /**
     * 构造用户查询条件包装器
     * 根据用户查询请求参数构造MyBatis Plus的QueryWrapper查询条件
     *
     * @param userQueryRequest 用户查询请求参数
     * @return QueryWrapper<User> 查询条件包装对象
     * @throws BusinessException 请求参数为空时抛出异常
     */
    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String userAccount = userQueryRequest.getUserAccount();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(Objects.nonNull(id), "id", id);
        queryWrapper.eq(StrUtil.isNotBlank(userRole), "userRole", userRole);
        queryWrapper.like(StrUtil.isNotBlank(userAccount), "userAccount", userAccount);
        queryWrapper.like(StrUtil.isNotBlank(userName), "userName", userName);
        queryWrapper.like(StrUtil.isNotBlank(userProfile), "userProfile", userProfile);
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), "ascend".equals(sortOrder), sortField);
        return queryWrapper;
    }

    /**
     * 判断用户是否为管理员
     *
     * @param user 用户对象
     * @return 是否为管理员
     */
    @Override
    public boolean isAdmin(User user) {
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }

    /**
     * 上传用户头像
     * 处理头像文件的验证、上传和URL生成等业务逻辑
     *
     * @param multipartFile 上传的头像文件，包含文件内容和元数据
     * @param loginUser 当前登录用户信息，用于生成唯一文件名和权限验证
     * @return 上传成功后的头像访问URL地址
     * @throws BusinessException 当文件验证失败或上传失败时抛出业务异常
     */
    @Override
    public String uploadAvatar(MultipartFile multipartFile, User loginUser) {
        File tempFile = null;
        try {
            // 验证文件是否为空
            if (multipartFile == null || multipartFile.isEmpty()) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件不能为空");
            }

            // 验证文件类型
            String originalFilename = multipartFile.getOriginalFilename();
            if (originalFilename == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件名不能为空");
            }

            // 验证文件扩展名
            String fileExtension = FileUtil.getSuffix(originalFilename).toLowerCase();
            List<String> allowedExtensions = Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "webp");
            if (!allowedExtensions.contains(fileExtension)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的文件格式，仅支持: " + String.join(", ", allowedExtensions));
            }

            // 验证文件大小（限制为5MB）
            long maxSize = 5 * 1024 * 1024; // 5MB
            if (multipartFile.getSize() > maxSize) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小不能超过5MB");
            }

            // 生成唯一的文件名
            String uuid = RandomUtil.randomString(16);
            String uploadFileName = String.format("%s_%s_%s.%s",
                    DateUtil.formatDate(new Date()),
                    loginUser.getId(),
                    uuid,
                    fileExtension);
            String uploadPath = String.format("/avatar/%s", uploadFileName);

            // 创建临时文件
            tempFile = File.createTempFile("avatar_", "." + fileExtension);
            multipartFile.transferTo(tempFile);

            // 使用CosManager直接上传文件
            cosManager.putObject(uploadPath, tempFile);

            // 构造文件访问URL
            String fileUrl = cosClientConfig.getHost() + uploadPath;

            log.info("用户 {} 上传头像成功，文件路径: {}, 访问URL: {}", loginUser.getId(), uploadPath, fileUrl);
            return fileUrl;

        } catch (BusinessException e) {
            // 重新抛出业务异常
            throw e;
        } catch (Exception e) {
            log.error("头像上传失败，发生未知错误", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "头像上传失败: " + e.getMessage());
        } finally {
            // 清理临时文件
            if (tempFile != null && tempFile.exists()) {
                boolean deleted = tempFile.delete();
                if (!deleted) {
                    log.warn("临时文件删除失败: {}", tempFile.getAbsolutePath());
                }
            }
        }
    }
}





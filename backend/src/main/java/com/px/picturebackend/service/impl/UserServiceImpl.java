package com.px.picturebackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.px.picturebackend.constant.UserConstant.USER_LOGIN_STATE;
import static com.px.picturebackend.constant.UserConstants.*;


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
        // 1. 参数校验
        validateRegisterParams(userAccount, userPassword, checkPassword);
        
        // 2. 检查账号是否已存在
        checkAccountExists(userAccount);
        
        // 3. 创建用户
        User user = createNewUser(userAccount, userPassword);
        
        // 4. 保存用户
        boolean saveResult = this.save(user);
        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
        
        return user.getId();
    }

    /**
     * 验证用户注册参数
     * 对用户注册时提供的账号、密码和确认密码进行一系列验证
     *
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     * @param checkPassword 确认密码
     */
    private void validateRegisterParams(String userAccount, String userPassword, String checkPassword) {
        ThrowUtils.throwIf(StrUtil.hasBlank(userAccount, userPassword, checkPassword),
                ErrorCode.PARAMS_ERROR, "参数为空");
        ThrowUtils.throwIf(userAccount.length() < MIN_ACCOUNT_LENGTH,
                ErrorCode.PARAMS_ERROR, "用户账号过短");
        ThrowUtils.throwIf(userPassword.length() < MIN_PASSWORD_LENGTH,
                ErrorCode.PARAMS_ERROR, "用户密码过短");
        ThrowUtils.throwIf(checkPassword.length() < MIN_PASSWORD_LENGTH,
                ErrorCode.PARAMS_ERROR, "确认密码过短");
        ThrowUtils.throwIf(!userPassword.equals(checkPassword),
                ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
    }

    
    /**
     * 检查账号是否已存在
     * 通过查询数据库判断指定的用户账号是否已经被注册
     *
     * @param userAccount 用户账号
     */
    private void checkAccountExists(String userAccount) {
        Long count = this.lambdaQuery()
                .eq(User::getUserAccount, userAccount)
                .count();
        ThrowUtils.throwIf(count > 0, ErrorCode.PARAMS_ERROR, "账号已存在");
    }

    
    /**
     * 创建新用户对象
     * 根据提供的账号和密码创建一个新的用户实体对象，并设置默认属性
     *
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     * @return 初始化完成的User对象
     */
    private User createNewUser(String userAccount, String userPassword) {
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(getEncryptPassword(userPassword));
        user.setUserName(DEFAULT_USER_NAME);
        user.setUserRole(UserRoleEnum.USER.getValue());
        return user;
    }


    /**
     * 获取加密后的密码
     *
     * @param userPassword 用户原始密码
     * @return 加密后的密码
     */
    @Override
    public String getEncryptPassword(String userPassword) {
        return DigestUtils.md5DigestAsHex((PASSWORD_SALT + userPassword).getBytes());
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
        // 1. 参数校验
        validateLoginParams(userAccount, userPassword);
        
        // 2. 验证用户凭据
        User user = authenticateUser(userAccount, userPassword);
        
        // 3. 设置登录状态
        setLoginState(request, user);
        
        return getLoginUserVO(user);
    }
    
    /**
     * 校验登录参数
     * 对用户登录时提供的账号和密码进行格式验证
     *
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     */
    private void validateLoginParams(String userAccount, String userPassword) {
        ThrowUtils.throwIf(StrUtil.hasBlank(userAccount, userPassword),
                ErrorCode.PARAMS_ERROR, "参数为空");
        ThrowUtils.throwIf(userAccount.length() < MIN_ACCOUNT_LENGTH,
                ErrorCode.PARAMS_ERROR, "账号格式错误");
        ThrowUtils.throwIf(userPassword.length() < MIN_PASSWORD_LENGTH,
                ErrorCode.PARAMS_ERROR, "密码格式错误");
    }

    
    /**
     * 验证用户凭据
     * 通过账号和密码验证用户身份，如果验证成功返回用户对象，否则抛出异常
     *
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     * @return 验证成功的用户对象
     * @throws BusinessException 当用户不存在或密码错误时抛出
     */
    private User authenticateUser(String userAccount, String userPassword) {
        String encryptPassword = getEncryptPassword(userPassword);
        User user = this.lambdaQuery()
                .eq(User::getUserAccount, userAccount)
                .eq(User::getUserPassword, encryptPassword)
                .one();

        if (user == null) {
            log.info("用户登录失败，账号: {}", userAccount);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }

        return user;
    }

    
    /**
     * 设置用户登录状态
     * 在Session和Sa-Token中设置用户登录状态，用于后续请求的身份验证
     *
     * @param request HTTP请求对象，用于设置Session
     * @param user 登录的用户对象
     */
    private void setLoginState(HttpServletRequest request, User user) {
        // 设置Session状态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);

        // 设置Sa-Token状态
        StpKit.SPACE.login(user.getId());
        StpKit.SPACE.getSession().set(USER_LOGIN_STATE, user);
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
     * 优先从Sa-Token中获取登录用户信息，如果获取不到再从Session中获取并查询数据库
     *
     * @param request HTTP请求对象，用于获取Session中的用户信息
     * @return 当前登录用户信息
     * @throws BusinessException 未登录时抛出异常
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 优先从Sa-Token获取用户信息
        User user = getUserFromSaToken();
        if (user != null) {
            return user;
        }
        
        // 从Session获取用户信息
        user = getUserFromSession(request);
        if (user != null) {
            return user;
        }
        
        throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
    }
    
    /**
     * 从Sa-Token获取用户信息
     * 通过Sa-Token框架获取当前登录的用户信息，如果未登录或信息不完整则返回null
     *
     * @return 当前登录的用户对象，如果未登录则返回null
     */
    private User getUserFromSaToken() {
        if (!StpKit.SPACE.isLogin()) {
            return null;
        }

        Object loginId = StpKit.SPACE.getLoginId();
        if (loginId == null) {
            return null;
        }

        User currentUser = (User) StpKit.SPACE.getSession().get(USER_LOGIN_STATE);
        if (currentUser == null || currentUser.getId() == null) {
            return null;
        }

        // 从数据库获取最新用户信息（可根据性能需求选择是否启用）
        return this.getById(currentUser.getId());
    }

    
    /**
     * 从Session获取用户信息
     * 从HTTP请求的Session中获取当前登录的用户信息，并从数据库获取最新数据
     *
     * @param request HTTP请求对象，用于获取Session
     * @return 当前登录的用户对象，如果未登录或信息不完整则返回null
     */
    private User getUserFromSession(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (!(userObj instanceof User)) {
            return null;
        }

        User currentUser = (User) userObj;
        if (currentUser.getId() == null) {
            return null;
        }

        // 从数据库获取最新用户信息
        return this.getById(currentUser.getId());
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
        // 获取当前登录用户
        User user = getCurrentLoginUser(request);
        
        // 清除登录状态
        clearLoginState(request, user);
        
        return true;
    }
    
    /**
     * 获取当前登录用户（用于注销）
     * 从Sa-Token或Session中获取当前登录的用户信息，用于注销操作
     *
     * @param request HTTP请求对象，用于获取Session
     * @return 当前登录的用户对象
     * @throws BusinessException 当用户未登录或用户信息异常时抛出
     */
    private User getCurrentLoginUser(HttpServletRequest request) {
        Object userObj = null;

        // 优先从Sa-Token获取
        if (StpKit.SPACE.isLogin()) {
            userObj = StpKit.SPACE.getSession().get(USER_LOGIN_STATE);
        }

        // 从Session获取
        if (userObj == null) {
            userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        }

        ThrowUtils.throwIf(userObj == null, ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        ThrowUtils.throwIf(!(userObj instanceof User), ErrorCode.OPERATION_ERROR, "用户信息异常");

        return (User) userObj;
    }

    
    /**
     * 清除登录状态
     * 清除用户在Session和Sa-Token中的登录状态，完成注销操作
     *
     * @param request HTTP请求对象，用于清除Session状态
     * @param user 需要注销的用户对象
     */
    private void clearLoginState(HttpServletRequest request, User user) {
        // 清除Session状态
        request.getSession().removeAttribute(USER_LOGIN_STATE);

        // 清除Sa-Token状态
        if (StpKit.SPACE.isLogin()) {
            StpKit.SPACE.logout(user.getId());
            StpKit.SPACE.getSession().clear();
        }
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
        
        // 使用并行流提高大数据量处理性能
        return userList.parallelStream()
                .filter(Objects::nonNull)
                .map(this::getUserVO)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 构建用户查询条件包装器
     * 根据用户查询请求参数构建MyBatis-Plus的LambdaQueryWrapper
     *
     * @param userQueryRequest 用户查询请求参数
     * @return LambdaQueryWrapper<User> 查询条件包装器
     */
    @Override
    public LambdaQueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR, "请求参数为空");
        
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        
        // 构建查询条件（链式调用优化）
        queryWrapper
                .eq(ObjectUtil.isNotNull(userQueryRequest.getId()), User::getId, userQueryRequest.getId())
                .like(StrUtil.isNotBlank(userQueryRequest.getUserAccount()), User::getUserAccount, userQueryRequest.getUserAccount())
                .like(StrUtil.isNotBlank(userQueryRequest.getUserName()), User::getUserName, userQueryRequest.getUserName())
                .like(StrUtil.isNotBlank(userQueryRequest.getUserProfile()), User::getUserProfile, userQueryRequest.getUserProfile())
                .eq(StrUtil.isNotBlank(userQueryRequest.getUserRole()), User::getUserRole, userQueryRequest.getUserRole());

        // 处理排序逻辑
        handleSorting(queryWrapper, userQueryRequest.getSortField(), userQueryRequest.getSortOrder());
        
        return queryWrapper;
    }

    /**
     * 处理查询排序逻辑
     * 根据排序字段和排序方式设置查询条件的排序规则
     *
     * @param queryWrapper 查询条件包装器
     * @param sortField 排序字段
     * @param sortOrder 排序方式
     */
    private void handleSorting(LambdaQueryWrapper<User> queryWrapper, String sortField, String sortOrder) {
        // 如果排序字段为空或不在白名单中，使用默认排序
        if (StrUtil.isBlank(sortField) || !ALLOWED_SORT_FIELDS.contains(sortField)) {
            queryWrapper.orderByAsc(User::getId);
            return;
        }
        
        // 确定排序方向，默认为升序
        boolean isAsc = !SORT_ORDER_DESC.equals(sortOrder);
        
        // 使用Map优化switch语句
        switch (sortField) {
            case "id":
                queryWrapper.orderBy(true, isAsc, User::getId);
                break;
            case "createTime":
                queryWrapper.orderBy(true, isAsc, User::getCreateTime);
                break;
            case "updateTime":
                queryWrapper.orderBy(true, isAsc, User::getUpdateTime);
                break;
            case "userName":
                queryWrapper.orderBy(true, isAsc, User::getUserName);
                break;
            case "userAccount":
                queryWrapper.orderBy(true, isAsc, User::getUserAccount);
                break;
            default:
                queryWrapper.orderByAsc(User::getId);
                break;
        }
    }

    /**
     * 判断用户是否为管理员
     * 通过比较用户角色枚举来确定用户是否具有管理员权限
     *
     * @param user 待检查的用户对象，可以为null
     * @return 如果用户不为null且具有管理员权限则返回true，否则返回false
     */
    @Override
    public boolean isAdmin(User user) {
        if (user == null || StrUtil.isBlank(user.getUserRole())) {
            return false;
        }
        
        // 直接比较角色值，提高性能
        return UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
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
        // 验证上传文件
        validateUploadFile(multipartFile);
        
        // 获取文件扩展名
        String fileExtension = getFileExtension(multipartFile.getOriginalFilename());
        
        // 生成上传路径
        String uploadPath = generateUploadPath(loginUser.getId(), fileExtension);
        
        // 执行文件上传
        return executeFileUpload(multipartFile, uploadPath);
    }
    
    /**
     * 验证上传文件
     * 对用户上传的头像文件进行基本验证，包括文件是否存在、文件名和文件大小
     *
     * @param multipartFile 上传的文件对象
     * @throws BusinessException 当文件验证失败时抛出
     */
    private void validateUploadFile(MultipartFile multipartFile) {
        ThrowUtils.throwIf(multipartFile == null || multipartFile.isEmpty(),
                ErrorCode.PARAMS_ERROR, "文件不能为空");
        ThrowUtils.throwIf(multipartFile.getOriginalFilename() == null,
                ErrorCode.PARAMS_ERROR, "文件名不能为空");
        ThrowUtils.throwIf(multipartFile.getSize() > MAX_AVATAR_SIZE,
                ErrorCode.PARAMS_ERROR, "文件大小不能超过" + MAX_AVATAR_SIZE_DESC);
    }

    
    /**
     * 获取并验证文件扩展名
     * 从原始文件名中提取文件扩展名并验证是否为允许的格式
     *
     * @param originalFilename 原始文件名
     * @return 小写格式的文件扩展名
     * @throws BusinessException 当文件格式不被支持时抛出
     */
    private String getFileExtension(String originalFilename) {
        String fileExtension = FileUtil.getSuffix(originalFilename).toLowerCase();
        ThrowUtils.throwIf(!ALLOWED_AVATAR_EXTENSIONS.contains(fileExtension),
                ErrorCode.PARAMS_ERROR, "不支持的文件格式，仅支持: " + String.join(", ", ALLOWED_AVATAR_EXTENSIONS));
        return fileExtension;
    }

    
    /**
     * 生成上传路径
     * 根据用户ID和文件扩展名生成唯一的文件上传路径
     *
     * @param userId 用户ID
     * @param fileExtension 文件扩展名
     * @return 完整的上传路径
     */
    private String generateUploadPath(Long userId, String fileExtension) {
        String uuid = RandomUtil.randomString(RANDOM_STRING_LENGTH);
        String uploadFileName = String.format("%s_%s_%s.%s",
                DateUtil.formatDate(new Date()),
                userId,
                uuid,
                fileExtension);
        return AVATAR_UPLOAD_PATH_PREFIX + uploadFileName;
    }

    
    /**
     * 执行文件上传
     * 将MultipartFile保存为临时文件，上传到对象存储服务，并返回访问URL
     *
     * @param multipartFile 上传的文件
     * @param uploadPath 上传路径
     * @return 文件访问URL
     * @throws BusinessException 当上传失败时抛出
     */
    private String executeFileUpload(MultipartFile multipartFile, String uploadPath) {
        File tempFile = null;
        try {
            // 创建临时文件
            String fileExtension = FileUtil.getSuffix(multipartFile.getOriginalFilename());
            tempFile = File.createTempFile(TEMP_AVATAR_PREFIX, "." + fileExtension);
            multipartFile.transferTo(tempFile);

            // 上传到COS
            cosManager.putObject(uploadPath, tempFile);

            // 构造访问URL
            String fileUrl = cosClientConfig.getHost() + uploadPath;
            log.info("头像上传成功，路径: {}, URL: {}", uploadPath, fileUrl);

            return fileUrl;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("头像上传失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "头像上传失败: " + e.getMessage());
        } finally {
            // 清理临时文件
            cleanupTempFile(tempFile);
        }
    }

    
    /**
     * 清理临时文件
     * 删除上传过程中创建的临时文件，避免占用磁盘空间
     *
     * @param tempFile 需要删除的临时文件
     */
    private void cleanupTempFile(File tempFile) {
        if (tempFile != null && tempFile.exists()) {
            boolean deleted = tempFile.delete();
            if (!deleted) {
                log.warn("临时文件删除失败: {}", tempFile.getAbsolutePath());
            }
        }
    }

}





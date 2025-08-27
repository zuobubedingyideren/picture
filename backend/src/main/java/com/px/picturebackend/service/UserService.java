package com.px.picturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.px.picturebackend.exception.BusinessException;
import com.px.picturebackend.model.dto.user.UserQueryRequest;
import com.px.picturebackend.model.entity.User;
import com.px.picturebackend.model.vo.user.LoginUserVO;
import com.px.picturebackend.model.vo.user.UserVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 用户服务接口
 * 提供用户相关的核心业务功能，包括用户认证、授权、信息管理等
 * 
 * @author idpeng
 * @description 针对表【user(用户)】的数据库操作Service
 * @createDate 2025-07-09 19:20:03
 */
public interface UserService extends IService<User> {

    // ==================== 用户认证相关 ====================

    /**
     * 用户注册
     * 创建新用户账户，包含账户名唯一性验证、密码强度检查等
     * 
     * @param userAccount 用户账户名，必须唯一且符合格式要求
     * @param userPassword 用户密码，需要满足安全强度要求
     * @param checkPassword 确认密码，必须与用户密码一致
     * @return 新创建用户的唯一标识ID
     * @throws BusinessException 当账户已存在、密码不符合要求或两次密码不一致时抛出
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 密码加密
     * 使用安全的加密算法对用户密码进行加密处理
     * 
     * @param userPassword 用户原始明文密码
     * @return 加密后的密码字符串，用于数据库存储
     */
    String getEncryptPassword(String userPassword);

    /**
     * 用户登录
     * 验证用户凭据并建立登录会话
     * 
     * @param userAccount 用户账户名
     * @param userPassword 用户密码（明文）
     * @param request HTTP请求对象，用于会话管理
     * @return 登录成功后的用户信息（已脱敏）
     * @throws BusinessException 当账户不存在、密码错误或账户被禁用时抛出
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户注销
     * 清除用户登录状态和相关会话信息
     * 
     * @param request HTTP请求对象，包含当前会话信息
     * @return 注销操作结果，true表示成功，false表示失败
     */
    boolean userLogout(HttpServletRequest request);

    // ==================== 用户信息获取相关 ====================

    /**
     * 获取当前登录用户
     * 从会话中获取当前已登录的用户完整信息
     * 
     * @param request HTTP请求对象，用于获取会话信息
     * @return 当前登录用户的完整实体对象
     * @throws BusinessException 当用户未登录或会话已过期时抛出
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 获取登录用户脱敏信息
     * 将用户实体转换为登录用户视图对象，移除敏感信息
     * 
     * @param user 用户实体对象
     * @return 脱敏后的登录用户信息
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 获取用户脱敏信息
     * 将用户实体转换为用户视图对象，适用于公开展示
     * 
     * @param user 用户实体对象（包含完整信息）
     * @return 脱敏后的用户信息对象
     */
    UserVO getUserVO(User user);

    /**
     * 批量获取用户脱敏信息
     * 批量处理用户实体列表，转换为脱敏的用户视图对象列表
     * 
     * @param userList 用户实体对象列表
     * @return 脱敏后的用户信息对象列表
     */
    List<UserVO> getUserVOList(List<User> userList);

    // ==================== 查询和权限相关 ====================

    /**
     * 构建用户查询条件
     * 根据查询请求参数构建MyBatis-Plus查询包装器
     * 
     * @param userQueryRequest 用户查询请求对象，包含各种查询条件
     * @return 构建好的Lambda查询包装器，用于数据库查询
     */
    LambdaQueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);


    /**
     * 判断用户是否为管理员
     * 检查用户角色权限，确定是否具有管理员权限
     * 
     * @param user 待检查的用户对象
     * @return true表示是管理员，false表示非管理员
     */
    boolean isAdmin(User user);

    // ==================== 文件上传相关 ====================

    /**
     * 上传用户头像
     * 处理头像文件的验证、上传和URL生成等业务逻辑
     * 包括文件格式验证、大小限制检查、文件名生成、云存储上传等步骤
     * 
     * @param multipartFile 上传的头像文件，包含文件内容和元数据
     * @param loginUser 当前登录用户信息，用于生成唯一文件名和权限验证
     * @return 上传成功后的头像访问URL地址
     * @throws BusinessException 当文件格式不支持、文件过大、上传失败等情况时抛出
     */
    String uploadAvatar(MultipartFile multipartFile, User loginUser);
}

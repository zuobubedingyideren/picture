package com.px.picturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.px.picturebackend.model.dto.user.UserQueryRequest;
import com.px.picturebackend.model.entity.User;
import com.px.picturebackend.model.vo.user.LoginUserVO;
import com.px.picturebackend.model.vo.user.UserVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author idpeng
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2025-07-09 19:20:03
*/
public interface UserService extends IService<User> {

    /**
     * 用户注册
     * @param userAccount 用户账户
     * @param userPassword 用户密码
     * @param checkPassword 校验密码
     * @return 新用户id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 加密密码
     * @param userPassword 原始密码
     * @return 加密之后的密码
     */
    String getEncryptPassword(String userPassword);

    /**
     * 用户登录
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     * @param request 请求
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 获取脱敏的已登录用户信息
     * @param user 用户
     * @return 脱敏的已登录用户信息
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 获取当前登录用户
     * @param request request
     * @return 当前登录用户
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 用户注销
     * @param request request
     * @return 注销结果
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 获取用户脱敏信息
     * @param user 脱敏前的信息
     * @return 脱敏后的信息
     */
    UserVO getUserVO(User user);

    /**
     * 批量获取用户脱敏信息
     * @param userList 脱敏前的信息
     * @return 脱敏后的 List 列表
     */
    List<UserVO> getUserVOList(List<User> userList);

    /**
     * 获取查询条件
     *
     * @param userQueryRequest 查询条件
     * @return 查询条件
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);


    /**
     * 是否为管理员
     * @param user 用户
     * @return true为真，false为假
     */
    boolean isAdmin(User user);

    /**
     * 上传用户头像
     * 处理头像文件的验证、上传和URL生成等业务逻辑
     *
     * @param multipartFile 上传的头像文件，包含文件内容和元数据
     * @param loginUser 当前登录用户信息，用于生成唯一文件名和权限验证
     * @return 上传成功后的头像访问URL地址
     * @throws com.px.picturebackend.common.exception.BusinessException 当文件验证失败或上传失败时抛出业务异常
     */
    String uploadAvatar(MultipartFile multipartFile, User loginUser);
}

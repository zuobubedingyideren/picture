package com.yupi.yupicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yupi.yupicturebackend.model.dto.spaceuser.SpaceUserAddRequest;
import com.yupi.yupicturebackend.model.dto.spaceuser.SpaceUserQueryRequest;
import com.yupi.yupicturebackend.model.entity.SpaceUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.yupicturebackend.model.vo.spaceuser.SpaceUserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author idpeng
* @description 针对表【space_user(空间用户关联)】的数据库操作Service
* @createDate 2025-08-25 19:28:45
*/
public interface SpaceUserService extends IService<SpaceUser> {

    /**
     * 添加空间用户关联
     *
     * @param spaceUserAddRequest 空间用户添加请求参数
     * @return 新增记录的主键ID
     */
    long addSpaceUser(SpaceUserAddRequest spaceUserAddRequest);

    /**
     * 校验空间用户关联参数
     *
     * @param spaceUser 空间用户关联实体对象
     * @param add       是否为新增操作 true-新增 false-更新
     */
    void validSpaceUser(SpaceUser spaceUser, boolean add);

    /**
     * 构建空间用户关联查询条件包装器
     *
     * @param spaceUserQueryRequest 空间用户查询请求参数
     * @return QueryWrapper<SpaceUser> 查询条件包装器
     */
    QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest);

    /**
     * 将空间用户实体对象转换为视图对象
     *
     * @param spaceUser 空间用户实体对象
     * @param request   HTTP请求对象，用于获取当前用户信息等上下文
     * @return SpaceUserVO 空间用户视图对象
     */
    SpaceUserVO getSpaceUserVO(SpaceUser spaceUser, HttpServletRequest request);

    /**
     * 将空间用户实体对象列表转换为视图对象列表
     *
     * @param spaceUserList 空间用户实体对象列表
     * @return List<SpaceUserVO> 空间用户视图对象列表
     */
    List<SpaceUserVO> getSpaceUserVOList(List<SpaceUser> spaceUserList);
}

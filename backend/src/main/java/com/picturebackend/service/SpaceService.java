package com.picturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.picturebackend.common.DeleteRequest;
import com.picturebackend.model.dto.space.SpaceAddRequest;
import com.picturebackend.model.dto.space.SpaceQueryRequest;
import com.picturebackend.model.entity.Space;
import com.picturebackend.model.entity.User;
import com.picturebackend.model.vo.space.SpaceVO;

import javax.servlet.http.HttpServletRequest;

/**
* @author idpeng
* @description 针对表【space(空间)】的数据库操作Service
* @createDate 2025-08-22 11:31:51
*/
public interface SpaceService extends IService<Space> {

    /**
     * 校验空间信息
     *
     * @param space 空间实体对象
     * @param add   是否为新增操�?
     */
    void validSpace(Space space, boolean add);

    /**
     * 添加空间
     *
     * @param spaceAddRequest 空间添加请求参数
     * @param loginUser       登录用户信息
     * @return 新增空间的ID
     */
    Long addSpace(SpaceAddRequest spaceAddRequest, User loginUser);

    /**
     * 获取空间的VO视图对象
     *
     * @param space   空间实体对象
     * @param request HTTP请求对象，用于获取上下文信息
     * @return 空间VO视图对象
     */
    SpaceVO getSpaceVO(Space space, HttpServletRequest request);

    /**
     * 获取空间VO视图分页对象
     *
     * @param spacePage 空间分页数据
     * @param request   HTTP请求对象，用于获取上下文信息
     * @return 空间VO视图分页对象
     */
    Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request);

    /**
     * 构建空间查询条件包装�?
     *
     * @param spaceQueryRequest 空间查询请求参数
     * @return 查询条件包装�?
     */
    QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest);

    /**
     * 根据空间等级填充空间信息（如最大容量、最大数量等�?
     *
     * @param space 空间实体对象
     */
    void fillSpaceBySpaceLevel(Space space);

    /**
     * 检查用户对空间的操作权�?
     *
     * @param loginUser 登录用户信息
     * @param space     空间实体对象
     */
    void checkSpaceAuth(User loginUser, Space space);

    /**
     * 删除空间
     *
     * @param deleteRequest 删除请求参数，包含要删除的空间ID
     * @param loginUser     登录用户信息
     * @return 删除操作是否成功
     */
    Boolean deleteSpace(DeleteRequest deleteRequest, User loginUser);


}

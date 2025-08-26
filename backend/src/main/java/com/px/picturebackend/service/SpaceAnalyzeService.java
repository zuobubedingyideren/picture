package com.px.picturebackend.service;

import com.px.picturebackend.model.dto.space.analyze.*;
import com.px.picturebackend.model.entity.Space;
import com.px.picturebackend.model.entity.User;
import com.px.picturebackend.model.vo.space.analyze.*;

import java.util.List;

/**
 * packageName: com.px.picturebackend.service
 *
 * @author: idpeng
 * @version: 1.0
 * @interfaceName: SpaceAnalyzeService
 * @date: 2025/8/24 23:39
 * @description: 空间分析接口
 */
public interface SpaceAnalyzeService {
    /**
     * 获取空间使用情况分析
     *
     * @param spaceUsageAnalyzeRequest 空间使用分析请求参数
     * @param loginUser 当前登录用户信息
     * @return 空间使用情况分析响应结果
     */
    SpaceUsageAnalyzeResponse getSpaceUsageAnalyze(SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest, User loginUser);

    /**
     * 获取空间分类情况分析
     *
     * @param spaceCategoryAnalyzeRequest 空间分类分析请求参数
     * @param loginUser 当前登录用户信息
     * @return 空间分类情况分析响应结果列表
     */
    List<SpaceCategoryAnalyzeResponse> getSpaceCategoryAnalyze(
            SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest,
            User loginUser);

    /**
     * 获取空间标签情况分析
     *
     * @param spaceTagAnalyzeRequest 空间标签分析请求参数
     * @param loginUser 当前登录用户信息
     * @return 空间标签情况分析响应结果列表
     */
    List<SpaceTagAnalyzeResponse> getSpaceTagAnalyze(SpaceTagAnalyzeRequest spaceTagAnalyzeRequest, User loginUser);

    /**
     * 获取空间大小情况分析
     *
     * @param spaceSizeAnalyzeRequest 空间大小分析请求参数
     * @param loginUser 当前登录用户信息
     * @return 空间大小情况分析响应结果列表
     */
    List<SpaceSizeAnalyzeResponse> getSpaceSizeAnalyze(SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest, User loginUser);

    /**
     * 获取空间用户情况分析
     *
     * @param spaceUserAnalyzeRequest 空间用户分析请求参数
     * @param loginUser 当前登录用户信息
     * @return 空间用户情况分析响应结果列表
     */
    List<SpaceUserAnalyzeResponse> getSpaceUserAnalyze(SpaceUserAnalyzeRequest spaceUserAnalyzeRequest, User loginUser);

    /**
     * 获取空间排名情况分析
     *
     * @param spaceRankAnalyzeRequest 空间排名分析请求参数
     * @param loginUser 当前登录用户信息
     * @return 空间排名情况分析结果列表
     */
    List<Space> getSpaceRankAnalyze(SpaceRankAnalyzeRequest spaceRankAnalyzeRequest, User loginUser);
}

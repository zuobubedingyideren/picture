package com.px.picturebackend.controller;

import com.px.picturebackend.common.BaseResponse;
import com.px.picturebackend.common.ResultUtils;
import com.px.picturebackend.exception.ErrorCode;
import com.px.picturebackend.exception.ThrowUtils;
import com.px.picturebackend.model.dto.space.analyze.*;
import com.px.picturebackend.model.entity.Space;
import com.px.picturebackend.model.entity.User;
import com.px.picturebackend.model.vo.space.analyze.*;
import com.px.picturebackend.service.SpaceAnalyzeService;
import com.px.picturebackend.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * packageName: com.px.picturebackend.controller
 *
 * @author: idpeng
 * @version: 1.0
 * @className: SpaceAnalyzeController
 * @date: 2025/8/25 09:51
 * @description: 空间分析控制器
 */
@RestController
@RequestMapping("/space/analyze")
@Api(tags = "空间分析接口")
public class SpaceAnalyzeController {
    @Resource
    private SpaceAnalyzeService spaceAnalyzeService;

    @Resource
    private UserService userService;

    /**
     * 获取空间使用情况分析
     * 根据请求参数分析并返回用户空间的使用情况，包括存储空间和文件数量的使用比例
     *
     * @param spaceUsageAnalyzeRequest 空间分析请求参数，包含要分析的空间ID等信息
     * @param request                  HTTP请求对象，用于获取当前登录用户信息
     * @return 空间使用情况分析结果，包含已使用空间、最大空间、使用比例等信息
     */
    @ApiOperation("获取空间使用情况分析")
    @PostMapping("/usage")
    public BaseResponse<SpaceUsageAnalyzeResponse> getSpaceUsageAnalyze(
            @RequestBody SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest,
            HttpServletRequest request
    ) {
        ThrowUtils.throwIf(spaceUsageAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        SpaceUsageAnalyzeResponse spaceUsageAnalyze = spaceAnalyzeService.getSpaceUsageAnalyze(spaceUsageAnalyzeRequest, loginUser);
        return ResultUtils.success(spaceUsageAnalyze);
    }

    /**
     * 获取空间分类分析数据
     * 根据请求参数查询并返回指定空间或公共空间中各分类的图片数量和总大小
     *
     * @param spaceCategoryAnalyzeRequest 空间分类分析请求参数，包含查询范围等信息
     * @param request                     HTTP请求对象，用于获取当前登录用户信息
     * @return 分类分析结果列表，每个元素包含分类名称、图片数量和总大小
     */
    @ApiOperation("获取空间分类分析数据")
    @PostMapping("/category")
    public BaseResponse<List<SpaceCategoryAnalyzeResponse>> getSpaceCategoryAnalyze(
            @RequestBody SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest,
            HttpServletRequest request
    ) {
        ThrowUtils.throwIf(spaceCategoryAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        List<SpaceCategoryAnalyzeResponse> spaceCategoryAnalyze = spaceAnalyzeService.getSpaceCategoryAnalyze(spaceCategoryAnalyzeRequest, loginUser);
        return ResultUtils.success(spaceCategoryAnalyze);
    }

    /**
     * 获取空间标签分析数据
     * 根据请求参数查询并返回指定空间或公共空间中所有图片的标签使用情况统计
     *
     * @param spaceTagAnalyzeRequest 空间标签分析请求参数，包含查询范围等信息
     * @param request                HTTP请求对象，用于获取当前登录用户信息
     * @return 标签分析结果列表，每个元素包含标签名称和使用次数，按使用次数降序排列
     */
    @ApiOperation("获取空间标签分析数据")
    @PostMapping("/tags")
    public BaseResponse<List<SpaceTagAnalyzeResponse>> getSpaceTagAnalyze(
            @RequestBody SpaceTagAnalyzeRequest spaceTagAnalyzeRequest,
            HttpServletRequest request
    ) {
        ThrowUtils.throwIf(spaceTagAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        List<SpaceTagAnalyzeResponse> spaceTagAnalyze = spaceAnalyzeService.getSpaceTagAnalyze(spaceTagAnalyzeRequest, loginUser);
        return ResultUtils.success(spaceTagAnalyze);
    }

    /**
     * 获取空间图片大小分析数据
     * 根据请求参数查询并返回指定空间或公共空间中图片的大小分布情况
     *
     * @param spaceSizeAnalyzeRequest 空间图片大小分析请求参数，包含查询范围等信息
     * @param request                 HTTP请求对象，用于获取当前登录用户信息
     * @return 图片大小分析结果列表，每个元素包含大小区间名称和该区间的图片数量
     */
    @ApiOperation("获取空间图片大小分析数据")
    @PostMapping("/size")
    public BaseResponse<List<SpaceSizeAnalyzeResponse>> getSpaceSizeAnalyze(
            @RequestBody SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest,
            HttpServletRequest request
    ) {
        ThrowUtils.throwIf(spaceSizeAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        List<SpaceSizeAnalyzeResponse> spaceSizeAnalyze = spaceAnalyzeService.getSpaceSizeAnalyze(spaceSizeAnalyzeRequest, loginUser);
        return ResultUtils.success(spaceSizeAnalyze);
    }

    /**
     * 获取空间用户分析数据
     * 根据请求参数查询并返回指定空间或公共空间中用户的图片上传情况统计
     *
     * @param spaceUserAnalyzeRequest 空间用户分析请求参数，包含查询范围和时间维度等信息
     * @param request                 HTTP请求对象，用于获取当前登录用户信息
     * @return 用户分析结果列表，每个元素包含时间周期和该周期内的图片上传数量
     */
    @ApiOperation("获取空间用户分析数据")
    @PostMapping("/user")
    public BaseResponse<List<SpaceUserAnalyzeResponse>> getSpaceUserAnalyze(@RequestBody SpaceUserAnalyzeRequest spaceUserAnalyzeRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceUserAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        List<SpaceUserAnalyzeResponse> spaceUserAnalyze = spaceAnalyzeService.getSpaceUserAnalyze(spaceUserAnalyzeRequest, loginUser);
        return ResultUtils.success(spaceUserAnalyze);
    }

    /**
     * 获取空间排名分析数据
     * 根据请求参数查询并返回空间使用量排名
     *
     * @param spaceRankAnalyzeRequest 空间排名分析请求参数，包含要显示的排行榜数量
     * @param request                 HTTP请求对象，用于获取当前登录用户信息
     * @return 空间排名分析结果列表，包含各个空间的使用情况，按使用量排名
     */
    @ApiOperation("获取空间排名分析数据")
    @PostMapping("/rank")
    public BaseResponse<List<Space>> getSpaceRankAnalyze(
            @RequestBody SpaceRankAnalyzeRequest spaceRankAnalyzeRequest,
            HttpServletRequest request
    ) {
        ThrowUtils.throwIf(spaceRankAnalyzeRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        List<Space> spaceRankAnalyze = spaceAnalyzeService.getSpaceRankAnalyze(spaceRankAnalyzeRequest, loginUser);
        return ResultUtils.success(spaceRankAnalyze);
    }
}

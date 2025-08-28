package com.px.picturebackend.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.px.picturebackend.annotation.AuthCheck;
import com.px.picturebackend.api.aliyunai.AliYunAiApi;
import com.px.picturebackend.api.aliyunai.model.CreateOutPaintingTaskResponse;
import com.px.picturebackend.api.aliyunai.model.GetOutPaintingTaskResponse;
import com.px.picturebackend.api.imagesearch.ImageSearchApiFacade;
import com.px.picturebackend.api.imagesearch.model.ImageSearchResult;
import com.px.picturebackend.common.BaseResponse;
import com.px.picturebackend.common.DeleteRequest;
import com.px.picturebackend.common.ResultUtils;
import com.px.picturebackend.constant.UserConstant;
import com.px.picturebackend.exception.ErrorCode;
import com.px.picturebackend.exception.ThrowUtils;
import com.px.picturebackend.manager.auth.SpaceUserAuthManager;
import com.px.picturebackend.manager.auth.StpKit;
import com.px.picturebackend.manager.auth.annotation.SaSpaceCheckPermission;
import com.px.picturebackend.manager.auth.model.SpaceUserPermissionConstant;
import com.px.picturebackend.model.dto.picture.*;
import com.px.picturebackend.model.entity.Picture;
import com.px.picturebackend.model.entity.Space;
import com.px.picturebackend.model.entity.User;
import com.px.picturebackend.model.enums.PictureReviewStatusEnum;
import com.px.picturebackend.model.vo.picture.PictureTagCategory;
import com.px.picturebackend.model.vo.picture.PictureVO;
import com.px.picturebackend.service.PictureService;
import com.px.picturebackend.service.SpaceService;
import com.px.picturebackend.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.px.picturebackend.constant.PictureConstants.*;

/**
 * packageName: com.px.picturebackend.controller
 *
 * @author: idpeng
 * @version: 1.0
 * @className: PictureController
 * @date: 2025/7/28 23:13
 * @description: 图片上传接口
 */
@Api(tags = "图片管理接口")
@RestController
@Slf4j
@RequestMapping("/picture")
public class PictureController {



    // ==================== 依赖注入 ====================

    @Resource
    private UserService userService;

    @Resource
    private PictureService pictureService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private SpaceService spaceService;

    @Resource
    private AliYunAiApi aliYunAiApi;

    @Resource
    private SpaceUserAuthManager spaceUserAuthManager;

    /**
     * 本地缓存实例
     * 使用 Caffeine 缓存框架，提供高性能的本地缓存支持
     * 配置：初始容量1024，最大容量10000，写入后5分钟过期
     */
    private final Cache<String, String> localCache = Caffeine.newBuilder()
            .initialCapacity(LOCAL_CACHE_INITIAL_CAPACITY)
            .maximumSize(LOCAL_CACHE_MAXIMUM_SIZE)
            .expireAfterWrite(LOCAL_CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES)
            .build();

    // ==================== 图片上传相关接口 ====================

    /**
     * 上传图片（文件上传方式）
     * 支持多种图片格式，自动进行格式验证和大小限制
     *
     * @param multipartFile        上传的图片文件
     * @param pictureUploadRequest 图片上传请求参数，包含标题、描述、标签等信息
     * @param request              HTTP请求对象，用于获取当前登录用户信息
     * @return 上传成功的图片信息
     */
    @ApiOperation("上传图片（文件上传）")
    @PostMapping("/upload")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_UPLOAD)
    public BaseResponse<PictureVO> uploadPicture(
            @RequestPart("file") MultipartFile multipartFile,
            PictureUploadRequest pictureUploadRequest,
            HttpServletRequest request) {
        
        log.info("开始上传图片，文件名：{}", multipartFile != null ? multipartFile.getOriginalFilename() : "null");
        
        // 参数校验
        validateUploadFile(multipartFile);
        
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        
        // 执行图片上传
        PictureVO pictureVO = pictureService.uploadPicture(multipartFile, pictureUploadRequest, loginUser);
        
        log.info("图片上传成功，图片ID：{}", pictureVO.getId());
        return ResultUtils.success(pictureVO);
    }

    /**
     * 通过URL上传图片
     * 支持从网络URL下载图片并上传到系统中
     *
     * @param pictureUploadRequest 图片上传请求参数，必须包含有效的文件URL
     * @param request              HTTP请求对象，用于获取当前登录用户信息
     * @return 上传成功的图片信息
     */
    @ApiOperation("通过URL上传图片")
    @PostMapping("/upload/url")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_UPLOAD)
    public BaseResponse<PictureVO> uploadPictureByUrl(
            @RequestBody PictureUploadRequest pictureUploadRequest,
            HttpServletRequest request) {
        
        log.info("开始通过URL上传图片，URL：{}", pictureUploadRequest != null ? pictureUploadRequest.getFileUrl() : "null");
        
        // 参数校验
        validateUploadRequest(pictureUploadRequest);
        
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        
        // 执行图片上传
        String fileUrl = pictureUploadRequest.getFileUrl();
        PictureVO pictureVO = pictureService.uploadPicture(fileUrl, pictureUploadRequest, loginUser);
        
        log.info("URL图片上传成功，图片ID：{}", pictureVO.getId());
        return ResultUtils.success(pictureVO);
    }

    /**
     * 批量上传图片（管理员专用）
     * 通过爬取指定网站批量获取并上传图片
     *
     * @param pictureUploadByBatchRequest 批量上传请求参数
     * @param request                     HTTP请求对象，用于获取当前登录用户信息
     * @return 成功上传的图片数量
     */
    @ApiOperation("批量上传图片（管理员专用）")
    @PostMapping("/upload/batch")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Integer> uploadPictureByBatch(
            @RequestBody PictureUploadByBatchRequest pictureUploadByBatchRequest,
            HttpServletRequest request) {
        
        log.info("开始批量上传图片，搜索关键词：{}", 
                pictureUploadByBatchRequest != null ? pictureUploadByBatchRequest.getSearchText() : "null");
        
        // 参数校验
        ThrowUtils.throwIf(pictureUploadByBatchRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        
        // 执行批量上传
        Integer uploadCount = pictureService.uploadPictureByBatch(pictureUploadByBatchRequest, loginUser);
        
        log.info("批量上传完成，成功上传图片数量：{}", uploadCount);
        return ResultUtils.success(uploadCount);
    }

    // ==================== 图片删除相关接口 ====================

    /**
     * 删除图片
     * 支持物理删除和逻辑删除，会同时清理相关的缓存和文件
     *
     * @param deleteRequest 删除请求参数，包含要删除的图片ID
     * @param request       HTTP请求对象，用于获取当前登录用户信息
     * @return 删除操作结果
     */
    @ApiOperation("删除图片")
    @PostMapping("/delete")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_DELETE)
    public BaseResponse<Boolean> deletePicture(
            @RequestBody DeleteRequest deleteRequest, 
            HttpServletRequest request) {
        
        log.info("开始删除图片，图片ID：{}", deleteRequest != null ? deleteRequest.getId() : "null");
        
        // 参数校验
        validateDeleteRequest(deleteRequest);
        
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        
        // 执行删除操作
        Long pictureId = deleteRequest.getId();
        pictureService.deletePicture(pictureId, loginUser);
        
        log.info("图片删除成功，图片ID：{}", pictureId);
        return ResultUtils.success(true);
    }

    // ==================== 图片查询相关接口 ====================

    /**
     * 根据ID获取图片详细信息（管理员专用）
     * 返回完整的图片实体信息，包含所有字段
     *
     * @param id      图片ID
     * @param request HTTP请求对象
     * @return 图片详细信息
     */
    @ApiOperation("根据ID获取图片详细信息（管理员专用）")
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Picture> getPictureById(long id, HttpServletRequest request) {
        log.info("管理员查询图片详情，图片ID：{}", id);
        
        // 参数校验
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR, "图片ID必须大于0");
        
        // 查询图片信息
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        
        return ResultUtils.success(picture);
    }

    /**
     * 根据ID获取图片视图对象
     * 返回适合前端展示的图片信息，包含权限列表
     *
     * @param id      图片ID
     * @param request HTTP请求对象
     * @return 图片视图对象
     */
    @ApiOperation("根据ID获取图片视图对象")
    @GetMapping("/get/vo")
    public BaseResponse<PictureVO> getPictureVOById(long id, HttpServletRequest request) {
        log.info("查询图片视图对象，图片ID：{}", id);
        
        // 参数校验
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR, "图片ID必须大于0");
        
        // 查询图片信息
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        
        // 空间权限校验
        Space space = validateAndGetSpace(picture.getSpaceId());
        
        // 获取当前登录用户和权限列表
        User loginUser = userService.getLoginUser(request);
        List<String> permissionList = spaceUserAuthManager.getPermissionList(space, loginUser);
        
        // 构建视图对象
        PictureVO pictureVO = pictureService.getPictureVO(picture, request);
        pictureVO.setPermissionList(permissionList);
        
        return ResultUtils.success(pictureVO);
    }

    /**
     * 分页获取图片列表（管理员专用）
     * 返回完整的图片实体分页信息
     *
     * @param pictureQueryRequest 图片查询请求参数
     * @return 图片分页列表
     */
    @ApiOperation("分页获取图片列表（管理员专用）")
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Picture>> listPictureByPage(@RequestBody PictureQueryRequest pictureQueryRequest) {
        log.info("管理员分页查询图片列表，页码：{}, 页面大小：{}", 
                pictureQueryRequest.getCurrent(), pictureQueryRequest.getPageSize());
        
        // 构建分页对象
        int current = pictureQueryRequest.getCurrent();
        int size = pictureQueryRequest.getPageSize();
        Page<Picture> page = new Page<>(current, size);
        
        // 执行分页查询
        Page<Picture> picturePage = pictureService.page(page, pictureService.getQueryWrapper(pictureQueryRequest));
        
        return ResultUtils.success(picturePage);
    }

    /**
     * 分页获取图片视图对象列表
     * 支持公开图库和私有空间的权限控制
     *
     * @param pictureQueryRequest 图片查询请求参数
     * @param request             HTTP请求对象
     * @return 图片视图对象分页列表
     */
    @ApiOperation("分页获取图片视图对象列表")
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<PictureVO>> listPictureVOByPage(
            @RequestBody PictureQueryRequest pictureQueryRequest, 
            HttpServletRequest request) {
        
        log.info("分页查询图片视图对象列表，页码：{}, 页面大小：{}, 空间ID：{}", 
                pictureQueryRequest.getCurrent(), pictureQueryRequest.getPageSize(), pictureQueryRequest.getSpaceId());
        
        // 参数校验
        int current = pictureQueryRequest.getCurrent();
        int size = pictureQueryRequest.getPageSize();
        ThrowUtils.throwIf(size > MAX_PAGE_SIZE, ErrorCode.PARAMS_ERROR, "页面大小不能超过" + MAX_PAGE_SIZE);
        
        // 空间权限校验和查询条件设置
        validateSpacePermissionAndSetQuery(pictureQueryRequest);
        
        // 执行分页查询
        Page<Picture> page = new Page<>(current, size);
        Page<Picture> picturePage = pictureService.page(page, pictureService.getQueryWrapper(pictureQueryRequest));
        
        // 转换为视图对象
        Page<PictureVO> pictureVOPage = pictureService.getPictureVOPage(picturePage, request);
        
        return ResultUtils.success(pictureVOPage);
    }

    // ==================== 图片编辑相关接口 ====================

    /**
     * 编辑图片信息
     * 支持修改图片的标题、描述、标签、分类等信息
     *
     * @param pictureEditRequest 图片编辑请求参数
     * @param request            HTTP请求对象，用于获取当前登录用户信息
     * @return 编辑操作结果
     */
    @ApiOperation("编辑图片信息")
    @PostMapping("/edit")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_EDIT)
    public BaseResponse<Boolean> editPicture(
            @RequestBody PictureEditRequest pictureEditRequest, 
            HttpServletRequest request) {
        
        log.info("开始编辑图片，图片ID：{}", pictureEditRequest != null ? pictureEditRequest.getId() : "null");
        
        // 参数校验
        validateEditRequest(pictureEditRequest);
        
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        
        // 执行编辑操作
        pictureService.editPicture(pictureEditRequest, loginUser);
        
        log.info("图片编辑成功，图片ID：{}", pictureEditRequest.getId());
        return ResultUtils.success(true);
    }

    /**
     * 批量编辑图片信息
     * 支持同时编辑多张图片的共同属性
     *
     * @param pictureEditByBatchRequest 批量编辑请求参数
     * @param request                   HTTP请求对象，用于获取当前登录用户信息
     * @return 批量编辑操作结果
     */
    @ApiOperation("批量编辑图片信息")
    @PostMapping("/edit/batch")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_EDIT)
    public BaseResponse<Boolean> editPictureByBatch(
            @RequestBody PictureEditByBatchRequest pictureEditByBatchRequest, 
            HttpServletRequest request) {
        
        log.info("开始批量编辑图片，图片数量：{}", 
                pictureEditByBatchRequest != null && pictureEditByBatchRequest.getPictureIdList() != null 
                        ? pictureEditByBatchRequest.getPictureIdList().size() : 0);
        
        // 参数校验
        ThrowUtils.throwIf(pictureEditByBatchRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        
        // 执行批量编辑
        pictureService.editPictureByBatch(pictureEditByBatchRequest, loginUser);
        
        log.info("批量编辑图片成功");
        return ResultUtils.success(true);
    }

    /**
     * 更新图片信息（管理员专用）
     * 提供完整的图片信息更新功能，包含审核状态等敏感字段
     *
     * @param pictureUpdateRequest 图片更新请求参数
     * @param request              HTTP请求对象
     * @return 更新操作结果
     */
    @ApiOperation("更新图片信息（管理员专用）")
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updatePicture(
            @RequestBody PictureUpdateRequest pictureUpdateRequest,
            HttpServletRequest request) {
        
        log.info("管理员更新图片信息，图片ID：{}", 
                pictureUpdateRequest != null ? pictureUpdateRequest.getId() : "null");
        
        // 参数校验
        validateUpdateRequest(pictureUpdateRequest);
        
        // 构建更新对象
        Picture picture = buildPictureFromUpdateRequest(pictureUpdateRequest);
        
        // 数据校验
        pictureService.validPicture(picture);
        
        // 检查图片是否存在
        Long id = pictureUpdateRequest.getId();
        Picture oldPicture = pictureService.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        
        // 补充审核参数
        User loginUser = userService.getLoginUser(request);
        pictureService.fillReviewParams(picture, loginUser);
        
        // 执行更新操作
        boolean result = pictureService.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "更新失败");
        
        log.info("管理员更新图片成功，图片ID：{}", id);
        return ResultUtils.success(true);
    }

    // ==================== 图片审核相关接口 ====================

    /**
     * 图片审核（管理员专用）
     * 支持通过、拒绝等审核状态的设置
     *
     * @param pictureReviewRequest 图片审核请求参数
     * @param request              HTTP请求对象，用于获取当前登录用户信息
     * @return 审核操作结果
     */
    @ApiOperation("图片审核（管理员专用）")
    @PostMapping("/review")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> doPictureReview(
            @RequestBody PictureReviewRequest pictureReviewRequest,
            HttpServletRequest request) {
        
        log.info("开始审核图片，图片ID：{}, 审核状态：{}", 
                pictureReviewRequest != null ? pictureReviewRequest.getId() : "null",
                pictureReviewRequest != null ? pictureReviewRequest.getReviewStatus() : "null");
        
        // 参数校验
        ThrowUtils.throwIf(pictureReviewRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        
        // 执行审核操作
        pictureService.doPictureReview(pictureReviewRequest, loginUser);
        
        log.info("图片审核完成，图片ID：{}", pictureReviewRequest.getId());
        return ResultUtils.success(true);
    }

    // ==================== 图片搜索相关接口 ====================

    /**
     * 以图搜图
     * 根据指定图片查找相似的图片
     *
     * @param searchPictureByPictureRequest 以图搜图请求参数
     * @return 相似图片搜索结果列表
     */
    @ApiOperation("以图搜图")
    @PostMapping("/search/picture")
    public BaseResponse<List<ImageSearchResult>> searchPictureByPicture(
            @RequestBody SearchPictureByPictureRequest searchPictureByPictureRequest) {
        
        log.info("开始以图搜图，图片ID：{}", 
                searchPictureByPictureRequest != null ? searchPictureByPictureRequest.getPictureId() : "null");
        
        // 参数校验
        ThrowUtils.throwIf(searchPictureByPictureRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        Long pictureId = searchPictureByPictureRequest.getPictureId();
        ThrowUtils.throwIf(pictureId == null || pictureId <= 0, ErrorCode.PARAMS_ERROR, "图片ID无效");
        
        // 获取图片信息
        Picture picture = pictureService.getById(pictureId);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        
        // 执行图片搜索
        List<ImageSearchResult> resultList = ImageSearchApiFacade.searchImage(picture.getUrl());
        
        log.info("以图搜图完成，找到相似图片数量：{}", resultList.size());
        return ResultUtils.success(resultList);
    }

    /**
     * 根据颜色搜索图片
     * 在指定空间内搜索指定颜色的图片
     *
     * @param searchPictureByColorRequest 颜色搜索请求参数
     * @param request                     HTTP请求对象，用于获取当前登录用户信息
     * @return 颜色匹配的图片列表
     */
    @ApiOperation("根据颜色搜索图片")
    @PostMapping("/search/color")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_VIEW)
    public BaseResponse<List<PictureVO>> searchPictureByColor(
            @RequestBody SearchPictureByColorRequest searchPictureByColorRequest, 
            HttpServletRequest request) {
        
        log.info("开始根据颜色搜索图片，颜色：{}, 空间ID：{}", 
                searchPictureByColorRequest != null ? searchPictureByColorRequest.getPicColor() : "null",
                searchPictureByColorRequest != null ? searchPictureByColorRequest.getSpaceId() : "null");
        
        // 参数校验
        ThrowUtils.throwIf(searchPictureByColorRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        
        String picColor = searchPictureByColorRequest.getPicColor();
        Long spaceId = searchPictureByColorRequest.getSpaceId();
        
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        
        // 执行颜色搜索
        List<PictureVO> pictureVOList = pictureService.searchPictureByColor(spaceId, picColor, loginUser);
        
        log.info("颜色搜索完成，找到图片数量：{}", pictureVOList.size());
        return ResultUtils.success(pictureVOList);
    }

    // ==================== 图片AI处理相关接口 ====================

    /**
     * 创建图片外绘任务（AI扩图）
     * 使用AI技术对图片进行扩展处理
     *
     * @param createPictureOutPaintingTaskRequest 创建外绘任务请求参数
     * @param request                             HTTP请求对象，用于获取当前登录用户信息
     * @return 外绘任务创建结果
     */
    @ApiOperation("创建图片外绘任务（AI扩图）")
    @PostMapping("/out_painting/create_task")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_EDIT)
    public BaseResponse<CreateOutPaintingTaskResponse> createPictureOutPaintingTask(
            @RequestBody CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest,
            HttpServletRequest request) {
        
        log.info("开始创建图片外绘任务，图片ID：{}", 
                createPictureOutPaintingTaskRequest != null ? createPictureOutPaintingTaskRequest.getPictureId() : "null");
        
        // 参数校验
        validateOutPaintingTaskRequest(createPictureOutPaintingTaskRequest);
        
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        
        // 创建外绘任务
        CreateOutPaintingTaskResponse response = pictureService.createPictureOutPaintingTask(
                createPictureOutPaintingTaskRequest, loginUser);
        
        log.info("图片外绘任务创建成功，任务ID：{}", response.getOutput().getTaskId());
        return ResultUtils.success(response);
    }

    /**
     * 获取图片外绘任务结果
     * 查询AI扩图任务的执行状态和结果
     *
     * @param taskId 任务ID
     * @return 外绘任务执行结果
     */
    @ApiOperation("获取图片外绘任务结果")
    @GetMapping("/out_painting/get_task")
    public BaseResponse<GetOutPaintingTaskResponse> getPictureOutPaintingTask(String taskId) {
        log.info("查询图片外绘任务结果，任务ID：{}", taskId);
        
        // 参数校验
        ThrowUtils.throwIf(StrUtil.isBlank(taskId), ErrorCode.PARAMS_ERROR, "任务ID不能为空");
        
        // 查询任务结果
        GetOutPaintingTaskResponse task = aliYunAiApi.getOutPaintingTask(taskId);
        
        return ResultUtils.success(task);
    }

    // ==================== 其他辅助接口 ====================

    /**
     * 获取图片标签和分类列表
     * 返回系统预定义的标签和分类选项
     *
     * @return 标签和分类列表
     */
    @ApiOperation("获取图片标签和分类列表")
    @GetMapping("/tag_category")
    public BaseResponse<PictureTagCategory> listPictureTagCategory() {
        log.info("获取图片标签和分类列表");
        
        // 构建标签和分类数据
        PictureTagCategory pictureTagCategory = new PictureTagCategory();
        List<String> tagList = Arrays.asList("热门", "搞笑", "生活", "高清", "艺术", "校园", "背景", "简单", "创意");
        List<String> categoryList = Arrays.asList("模板", "电商", "表情", "素材", "海报");
        
        pictureTagCategory.setTagList(tagList);
        pictureTagCategory.setCategoryList(categoryList);
        
        return ResultUtils.success(pictureTagCategory);
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 校验上传文件参数
     *
     * @param multipartFile 上传的文件
     */
    private void validateUploadFile(MultipartFile multipartFile) {
        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR, "上传文件不能为空");
        ThrowUtils.throwIf(multipartFile.isEmpty(), ErrorCode.PARAMS_ERROR, "上传文件不能为空");
    }

    /**
     * 校验上传请求参数
     *
     * @param pictureUploadRequest 上传请求参数
     */
    private void validateUploadRequest(PictureUploadRequest pictureUploadRequest) {
        ThrowUtils.throwIf(pictureUploadRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(pictureUploadRequest.getFileUrl()), 
                ErrorCode.PARAMS_ERROR, "文件URL不能为空");
    }

    /**
     * 校验删除请求参数
     *
     * @param deleteRequest 删除请求参数
     */
    private void validateDeleteRequest(DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(deleteRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        ThrowUtils.throwIf(deleteRequest.getId() == null || deleteRequest.getId() <= 0, 
                ErrorCode.PARAMS_ERROR, "图片ID无效");
    }

    /**
     * 校验编辑请求参数
     *
     * @param pictureEditRequest 编辑请求参数
     */
    private void validateEditRequest(PictureEditRequest pictureEditRequest) {
        ThrowUtils.throwIf(pictureEditRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        ThrowUtils.throwIf(pictureEditRequest.getId() == null || pictureEditRequest.getId() <= 0, 
                ErrorCode.PARAMS_ERROR, "图片ID无效");
    }

    /**
     * 校验更新请求参数
     *
     * @param pictureUpdateRequest 更新请求参数
     */
    private void validateUpdateRequest(PictureUpdateRequest pictureUpdateRequest) {
        ThrowUtils.throwIf(pictureUpdateRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        ThrowUtils.throwIf(pictureUpdateRequest.getId() == null || pictureUpdateRequest.getId() <= 0, 
                ErrorCode.PARAMS_ERROR, "图片ID无效");
    }

    /**
     * 校验外绘任务请求参数
     *
     * @param createPictureOutPaintingTaskRequest 外绘任务请求参数
     */
    private void validateOutPaintingTaskRequest(CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest) {
        ThrowUtils.throwIf(createPictureOutPaintingTaskRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        ThrowUtils.throwIf(createPictureOutPaintingTaskRequest.getPictureId() == null, 
                ErrorCode.PARAMS_ERROR, "图片ID不能为空");
    }

    /**
     * 校验空间权限并设置查询条件
     *
     * @param pictureQueryRequest 图片查询请求参数
     */
    private void validateSpacePermissionAndSetQuery(PictureQueryRequest pictureQueryRequest) {
        Long spaceId = pictureQueryRequest.getSpaceId();
        
        if (spaceId == null) {
            // 公开图库：只能查看已过审的数据
            pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            pictureQueryRequest.setNullSpaceId(true);
        } else {
            // 私有空间：检查权限
            boolean hasPermission = StpKit.SPACE.hasPermission(SpaceUserPermissionConstant.PICTURE_VIEW);
            ThrowUtils.throwIf(!hasPermission, ErrorCode.NO_AUTH_ERROR, "没有空间访问权限");
        }
    }

    /**
     * 校验并获取空间信息
     *
     * @param spaceId 空间ID
     * @return 空间信息，如果spaceId为null则返回null
     */
    private Space validateAndGetSpace(Long spaceId) {
        if (spaceId == null) {
            return null;
        }
        
        // 检查空间权限
        boolean hasPermission = StpKit.SPACE.hasPermission(SpaceUserPermissionConstant.PICTURE_VIEW);
        ThrowUtils.throwIf(!hasPermission, ErrorCode.NO_AUTH_ERROR, "没有空间访问权限");
        
        // 获取空间信息
        Space space = spaceService.getById(spaceId);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        
        return space;
    }

    /**
     * 根据更新请求构建图片对象
     *
     * @param pictureUpdateRequest 更新请求参数
     * @return 构建的图片对象
     */
    private Picture buildPictureFromUpdateRequest(PictureUpdateRequest pictureUpdateRequest) {
        Picture picture = new Picture();
        BeanUtil.copyProperties(pictureUpdateRequest, picture);
        
        // 处理标签列表转换
        if (pictureUpdateRequest.getTags() != null) {
            picture.setTags(JSONUtil.toJsonStr(pictureUpdateRequest.getTags()));
        }
        
        return picture;
    }
}

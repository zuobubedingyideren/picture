package com.picturebackend.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.picturebackend.annotation.AuthCheck;
import com.picturebackend.api.aliyunai.AliYunAiApi;
import com.picturebackend.api.aliyunai.model.CreateOutPaintingTaskResponse;
import com.picturebackend.api.aliyunai.model.GetOutPaintingTaskResponse;
import com.picturebackend.api.imagesearch.ImageSearchApiFacade;
import com.picturebackend.api.imagesearch.model.ImageSearchResult;
import com.picturebackend.api.imagesearch.ImageSearchApiFacade;
import com.picturebackend.api.imagesearch.model.ImageSearchResult;
import com.picturebackend.common.BaseResponse;
import com.picturebackend.common.DeleteRequest;
import com.picturebackend.common.ResultUtils;
import com.picturebackend.constant.UserConstant;
import com.picturebackend.exception.BusinessException;
import com.picturebackend.exception.ErrorCode;
import com.picturebackend.exception.ThrowUtils;
import com.picturebackend.manager.auth.SpaceUserAuthManager;
import com.picturebackend.manager.auth.StpKit;
import com.picturebackend.manager.auth.annotation.SaSpaceCheckPermission;
import com.picturebackend.manager.auth.model.SpaceUserPermissionConstant;
import com.picturebackend.model.dto.picture.*;
import com.picturebackend.model.entity.Picture;
import com.picturebackend.model.entity.Space;
import com.picturebackend.model.entity.User;
import com.picturebackend.model.enums.PictureReviewStatusEnum;
import com.picturebackend.model.vo.picture.PictureTagCategory;
import com.picturebackend.model.vo.picture.PictureVO;
import com.picturebackend.service.PictureService;
import com.picturebackend.service.SpaceService;
import com.picturebackend.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.yaml.snakeyaml.events.Event;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * packageName: com.picturebackend.controller
 *
 * @author: idpeng
 * @version: 1.0
 * @className: PictureController
 * @date: 2025/7/28 23:13
 * @description: 图片上传接口
 */
@Api("图片上传接口")
@RestController
@Slf4j
@RequestMapping("/picture")
public class PictureController {

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
     * 本地缓存，用于存储图片相关的临时数据
     * 使用 Caffeine 缓存框架，初始容量为 1024，最大容量为 10000
     * 缓存项在写入 5 分钟后过期并被自动移�?
     */
    private final Cache<String, String> LOCAL_CACHE =
            Caffeine.newBuilder().initialCapacity(1024)
                    .maximumSize(10000L)
                    // 缓存 5 分钟移除
                    .expireAfterWrite(5L, TimeUnit.MINUTES)
                    .build();



    /**
     * 上传图片接口(可重新上传）
     *
     * @param multipartFile        图片文件
     * @param pictureUploadRequest 图片上传请求参数
     * @param request              HTTP请求对象，用于获取当前登录用户信�?
     * @return 图片信息的响应结�?
     */
    @ApiOperation("上传图片接口")
    @PostMapping("/upload")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_UPLOAD)
    // @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<PictureVO> uploadPicture(
            @RequestPart("file") MultipartFile multipartFile,
            PictureUploadRequest pictureUploadRequest,
            HttpServletRequest request
    ) {
        User loginUser = userService.getLoginUser(request);
        PictureVO pictureVO = pictureService.uploadPicture(multipartFile, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVO);
    }

    /**
     * 通过URL上传图片
     *
     * @param pictureUploadRequest 图片上传请求参数，包含文件URL等信�?
     * @param request              HTTP请求对象，用于获取当前登录用户信�?
     * @return 图片信息的响应结�?
     */
    @ApiOperation("通过url上传图片")
    @PostMapping("/upload/url")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_UPLOAD)
    public BaseResponse<PictureVO> uploadPictureByUrl(
            @RequestBody PictureUploadRequest pictureUploadRequest,
            HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        String fileUrl = pictureUploadRequest.getFileUrl();
        PictureVO pictureVO = pictureService.uploadPicture(fileUrl, pictureUploadRequest, loginUser);
        return ResultUtils.success(pictureVO);
    }


    /**
     * 删除图片
     *
     * @param deleteRequest 删除请求参数，包含要删除的图片ID
     * @param request       HTTP请求对象，用于获取当前登录用户信�?
     * @return Boolean 删除结果，成功返回true
     */
    @ApiOperation("删除图片")
    @PostMapping("/delete")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_DELETE)
    public BaseResponse<Boolean> deletePicture(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long pictureId = deleteRequest.getId();
        User loginUser = userService.getLoginUser(request);
        pictureService.deletePicture(pictureId, loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 更新图片信息（仅管理员可操作�?
     *
     * @param pictureUpdateRequest 图片更新请求参数
     * @return Boolean 更新结果，成功返回true
     */
    @ApiOperation("更新图片信息（管理员�?)
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updatePicture(@RequestBody PictureUpdateRequest pictureUpdateRequest,
                                               HttpServletRequest request) {
        if (pictureUpdateRequest == null || pictureUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 将实体类�?DTO 进行转换
        Picture picture = new Picture();
        BeanUtil.copyProperties(pictureUpdateRequest, picture);
        // 注意�?list 转为string
        picture.setTags(JSONUtil.toJsonStr(pictureUpdateRequest.getTags()));
        // 数据校验
        pictureService.validPicture(picture);
        // 判断是否存在
        Long id = pictureUpdateRequest.getId();
        Picture oldPicture = pictureService.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);
        // 补充审核参数
        User loginUser = userService.getLoginUser(request);
        pictureService.fillReviewParams(picture, loginUser);
        // 更新图片信息
        boolean result = pictureService.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }


    /**
     * 根据ID获取图片信息
     *
     * @param id      图片ID
     * @param request HTTP请求对象
     * @return 图片信息响应结果
     */
    @ApiOperation("根据ID获取图片信息（管理员�?)
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Picture> getPictureById(long id, HttpServletRequest request) {
        // 参数校验，ID必须大于0
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 根据ID查询图片信息
        Picture picture = pictureService.getById(id);
        // 数据校验，图片不存在则抛出异�?
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(picture);
    }


    /**
     * 根据ID获取图片VO信息
     *
     * @param id      图片ID
     * @param request HTTP请求对象
     * @return 图片VO信息响应结果
     */
    @ApiOperation("根据ID获取图片VO信息")
    @GetMapping("/get/vo")
    public BaseResponse<PictureVO> getPictureVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据�?
        Picture picture = pictureService.getById(id);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        // 空间权限校验
        Space space = null;
        Long spaceId = picture.getSpaceId();
        if (spaceId != null) {
            boolean hasPermission = StpKit.SPACE.hasPermission(SpaceUserPermissionConstant.PICTURE_VIEW);
            ThrowUtils.throwIf(!hasPermission, ErrorCode.NO_AUTH_ERROR);
            // 已经改为使用注解鉴权
            // User loginUser = userService.getLoginUser(request);
            // pictureService.checkPictureAuth(loginUser, picture);
            space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存�?);
        }
        // 获取权限列表
        User loginUser = userService.getLoginUser(request);
        List<String> permissionList = spaceUserAuthManager.getPermissionList(space, loginUser);
        PictureVO pictureVO = pictureService.getPictureVO(picture, request);
        pictureVO.setPermissionList(permissionList);
        return ResultUtils.success(pictureVO);
    }


    /**
     * 分页获取图片列表
     *
     * @param pictureQueryRequest 图片查询请求参数
     * @return 图片分页信息响应结果
     */
    @ApiOperation("分页获取图片列表（管理员�?)
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Picture>> listPictureByPage(@RequestBody PictureQueryRequest pictureQueryRequest) {
        int current = pictureQueryRequest.getCurrent();
        int size = pictureQueryRequest.getPageSize();

        Page<Picture> picturePage = pictureService.page(new Page<>(current, size), pictureService.getQueryWrapper(pictureQueryRequest));

        return ResultUtils.success(picturePage);
    }

    /**
     * 分页获取图片视图对象列表
     *
     * @param pictureQueryRequest 图片查询请求参数
     * @param request             HTTP请求对象
     * @return 图片视图对象分页结果
     */
    @ApiOperation("分页获取图片视图对象列表")
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<PictureVO>> listPictureVOByPage(@RequestBody PictureQueryRequest pictureQueryRequest, HttpServletRequest request) {
        int current = pictureQueryRequest.getCurrent();
        int size = pictureQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);

        // 空间权限校验
        Long spaceId = pictureQueryRequest.getSpaceId();
        // 公开图库
        if (spaceId == null) {
            // 普通用户默认只能查看已过审的公开数据
            pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            pictureQueryRequest.setNullSpaceId(true);
        } else {
            // 私有空间
            /* User loginUser = userService.getLoginUser(request);
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存�?);
            if (!loginUser.getId().equals(space.getUserId())) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有空间权限");
            } */
            boolean result = StpKit.SPACE.hasPermission(SpaceUserPermissionConstant.PICTURE_VIEW);
            ThrowUtils.throwIf(!result, ErrorCode.NO_AUTH_ERROR);
        }


        Page<Picture> picturePage = pictureService.page(new Page<>(current, size), pictureService.getQueryWrapper(pictureQueryRequest));

        return ResultUtils.success(pictureService.getPictureVOPage(picturePage, request));
    }

    /**
     * 分页获取图片视图对象列表(带缓存）
     *
     * @param pictureQueryRequest 图片查询请求参数
     * @param request HTTP请求对象，用于获取用户信�?
     * @return 分页图片视图对象列表
     */
    @Deprecated
    @ApiOperation("分页获取图片视图对象列表(带缓存）")
    @PostMapping("/list/page/vo/cache")
    public BaseResponse<Page<PictureVO>> listPictureVOByPageWithCache(@RequestBody PictureQueryRequest pictureQueryRequest, HttpServletRequest request) {
        int current = pictureQueryRequest.getCurrent();
        int size = pictureQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);

        // 默认只查询过�?
        pictureQueryRequest.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());

        // 构建缓存key
        String queryCondition = JSONUtil.toJsonStr(pictureQueryRequest);
        String hashKey = DigestUtils.md5DigestAsHex(queryCondition.getBytes());
        String cacheKey = "yupicture:listPictureVOByPage:" + hashKey;

        // 从本地缓存中查询
        String cachedValue = LOCAL_CACHE.getIfPresent(cacheKey);
        if (cachedValue != null) {
            // 本地缓存命中，返回结�?
            Page<PictureVO> cachedPage = JSONUtil.toBean(cachedValue, Page.class);
            return ResultUtils.success(cachedPage);
        }


        // �?Redis 缓存中查�?
        ValueOperations<String, String> valueOps = stringRedisTemplate.opsForValue();
        cachedValue = valueOps.get(cacheKey);
        if (cachedValue != null) {
            // 缓存命中，返回结�?
            LOCAL_CACHE.put(cacheKey, cachedValue);
            Page<PictureVO> cachedPage = JSONUtil.toBean(cachedValue, Page.class);
            return ResultUtils.success(cachedPage);
        }
        // 查询数据�?
        Page<Picture> picturePage = pictureService.page(new Page<>(current, size), pictureService.getQueryWrapper(pictureQueryRequest));

        // 获取封装�?
        Page<PictureVO> pictureVOPage = pictureService.getPictureVOPage(picturePage, request);


        // 存入redis数据�?
        String cacheValue = JSONUtil.toJsonStr(pictureVOPage);

        // 5-10分钟，防止缓存雪�?
        int cacheExpireTime = 300 + RandomUtil.randomInt(0, 300);
        LOCAL_CACHE.put(cacheKey, cacheValue);
        valueOps.set(cacheKey, cacheValue, cacheExpireTime, TimeUnit.SECONDS);
        return ResultUtils.success(pictureVOPage);
    }


    /**
     * 编辑图片信息
     *
     * @param pictureEditRequest 图片编辑请求参数
     * @param request            HTTP请求对象，用于获取当前登录用户信�?
     * @return Boolean 编辑是否成功
     */
    @ApiOperation("编辑图片信息")
    @PostMapping("/edit")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_EDIT)
    public BaseResponse<Boolean> editPicture(@RequestBody PictureEditRequest pictureEditRequest, HttpServletRequest request) {
        // 参数校验
        if (pictureEditRequest == null || pictureEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        pictureService.editPicture(pictureEditRequest, loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 批量编辑图片信息
     *
     * @param pictureEditByBatchRequest 批量编辑请求参数，包含需要编辑的图片ID列表、空间ID、分类、标签列表和命名规则等信�?
     * @param request HTTP请求对象，用于获取当前登录用户信�?
     * @return Boolean 编辑结果，成功返回true
     */
    @ApiOperation("批量编辑图片信息")
    @PostMapping("/edit/batch")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_EDIT)
    public BaseResponse<Boolean> editPictureByBatch(@RequestBody PictureEditByBatchRequest pictureEditByBatchRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(pictureEditByBatchRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        pictureService.editPictureByBatch(pictureEditByBatchRequest, loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 获取图片标签和分类列�?
     *
     * @return PictureTagCategory 包含标签列表和分类列表的视图对象
     */
    @ApiOperation("获取图片标签和分类列�?)
    @GetMapping("/tag_category")
    public BaseResponse<PictureTagCategory> listPictureTagCategory() {
        // 创建并初始化标签和分类数�?
        PictureTagCategory pictureTagCategory = new PictureTagCategory();
        List<String> tagList = Arrays.asList("热门", "搞笑", "生活", "高清", "艺术", "校园", "背景", "简�?, "创意");
        List<String> categoryList = Arrays.asList("模板", "电商", "表情�?, "素材", "海报");
        pictureTagCategory.setTagList(tagList);
        pictureTagCategory.setCategoryList(categoryList);
        return ResultUtils.success(pictureTagCategory);
    }

    /**
     * 对图片进行审核操�?
     *
     * @param pictureReviewRequest 图片审核请求参数，包含图片ID、审核状态和审核信息
     * @param request              HTTP请求对象，用于获取当前登录用户信�?
     * @return 审核结果，成功返回true，失败则抛出异常
     */
    @ApiOperation("对图片进行审核操作（管理员）")
    @PostMapping("/review")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> doPictureReview(@RequestBody PictureReviewRequest pictureReviewRequest,
                                                 HttpServletRequest request) {
        ThrowUtils.throwIf(pictureReviewRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        pictureService.doPictureReview(pictureReviewRequest, loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 管理员批量上传图片接�?
     * 仅管理员可调用，通过爬取必应图片网站批量上传图片
     *
     * @param pictureUploadByBatchRequest 批量上传图片请求参数
     * @param request HTTP请求对象，用于获取当前登录用户信�?
     * @return 成功上传的图片数�?
     */
    @ApiOperation("批量抓取图片上传（管理员�?)
    @PostMapping("/upload/batch")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Integer> uploadPictureByBatch(
            @RequestBody PictureUploadByBatchRequest pictureUploadByBatchRequest,
            HttpServletRequest request
    ) {
        ThrowUtils.throwIf(pictureUploadByBatchRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Integer uploadCount = pictureService.uploadPictureByBatch(pictureUploadByBatchRequest, loginUser);
        return ResultUtils.success(uploadCount);
    }



    /**
     * 以图搜图功能
     * 根据传入的图片ID，获取该图片并调用图片搜索引擎进行相似图片搜�?
     *
     * @param searchPictureByPictureRequest 以图搜图请求对象，包含要搜索的图片ID
     * @return BaseResponse<List<ImageSearchResult>> 搜索结果列表的响应对�?
     */
    @PostMapping("/search/picture")
    public BaseResponse<List<ImageSearchResult>> searchPictureByPicture(@RequestBody SearchPictureByPictureRequest searchPictureByPictureRequest) {
        // 参数校验
        ThrowUtils.throwIf(searchPictureByPictureRequest == null, ErrorCode.PARAMS_ERROR);
        Long pictureId = searchPictureByPictureRequest.getPictureId();
        ThrowUtils.throwIf(pictureId == null || pictureId <= 0, ErrorCode.PARAMS_ERROR);
        // 获取图片信息
        Picture picture = pictureService.getById(pictureId);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR);
        // 调用图片搜索引擎进行搜索
        List<ImageSearchResult> resultList = ImageSearchApiFacade.searchImage(picture.getUrl());
        return ResultUtils.success(resultList);
    }


    /**
     * 根据颜色搜索图片
     * 根据指定的颜色和空间ID搜索匹配的图片，并返回给用户
     *
     * @param searchPictureByColorRequest 搜索图片的请求参数，包含颜色和空间ID
     * @param request HTTP请求对象，用于获取当前登录用户信�?
     * @return BaseResponse<List<PictureVO>> 图片搜索结果的响应对�?
     */
    @PostMapping("/search/color")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_VIEW)
    public BaseResponse<List<PictureVO>> searchPictureByColor(@RequestBody SearchPictureByColorRequest searchPictureByColorRequest, HttpServletRequest request) {
        // 参数校验
        ThrowUtils.throwIf(searchPictureByColorRequest == null, ErrorCode.PARAMS_ERROR);
        String picColor = searchPictureByColorRequest.getPicColor();
        Long spaceId = searchPictureByColorRequest.getSpaceId();
        // 获取登录用户并执行搜�?
        User loginUser = userService.getLoginUser(request);
        List<PictureVO> pictureVOList = pictureService.searchPictureByColor(spaceId, picColor, loginUser);
        return ResultUtils.success(pictureVOList);
    }

    /**
     * 创建图片外绘任务（AI扩图任务�?
     *
     * @param createPictureOutPaintingTaskRequest 创建外绘任务请求参数，包含图片ID和扩图参�?
     * @param request HTTP请求对象，用于获取当前登录用户信�?
     * @return BaseResponse<CreateOutPaintingTaskResponse> 扩图任务创建结果，包含任务ID和状态信�?
     */
    @ApiOperation("创建图片外绘任务")
    @PostMapping("/out_painting/create_task")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_EDIT)
    public BaseResponse<CreateOutPaintingTaskResponse> createPictureOutPaintingTask(
            @RequestBody CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest,
            HttpServletRequest request) {
        if (createPictureOutPaintingTaskRequest == null || createPictureOutPaintingTaskRequest.getPictureId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        CreateOutPaintingTaskResponse response = pictureService.createPictureOutPaintingTask(createPictureOutPaintingTaskRequest, loginUser);
        return ResultUtils.success(response);
    }

    /**
     * 获取图片外绘任务（AI扩图任务）结�?
     *
     * @param taskId 任务ID，用于查询指定扩图任务的执行状态和结果
     * @return BaseResponse<GetOutPaintingTaskResponse> 任务查询结果，包含任务状态、执行时间和输出图像URL等信�?
     */
    @GetMapping("/out_painting/get_task")
    public BaseResponse<GetOutPaintingTaskResponse> getPictureOutPaintingTask(String taskId) {
        ThrowUtils.throwIf(StrUtil.isBlank(taskId), ErrorCode.PARAMS_ERROR);
        GetOutPaintingTaskResponse task = aliYunAiApi.getOutPaintingTask(taskId);
        return ResultUtils.success(task);
    }
}

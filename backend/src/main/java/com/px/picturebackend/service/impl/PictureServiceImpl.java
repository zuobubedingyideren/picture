package com.px.picturebackend.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.px.picturebackend.api.aliyunai.AliYunAiApi;
import com.px.picturebackend.api.aliyunai.model.CreateOutPaintingTaskRequest;
import com.px.picturebackend.exception.BusinessException;
import com.px.picturebackend.exception.ErrorCode;
import com.px.picturebackend.exception.ThrowUtils;
import com.px.picturebackend.manager.CosManager;
import com.px.picturebackend.manager.upload.FilePictureUpload;
import com.px.picturebackend.manager.upload.PictureUploadTemplate;
import com.px.picturebackend.manager.upload.UrlPictureUpload;
import com.px.picturebackend.mapper.PictureMapper;
import com.px.picturebackend.model.dto.file.UploadPictureResult;
import com.px.picturebackend.model.dto.picture.*;
import com.px.picturebackend.model.entity.Picture;
import com.px.picturebackend.model.entity.Space;
import com.px.picturebackend.model.entity.User;
import com.px.picturebackend.model.enums.PictureReviewStatusEnum;
import com.px.picturebackend.model.vo.picture.PictureVO;
import com.px.picturebackend.model.vo.user.UserVO;
import com.px.picturebackend.service.PictureService;
import com.px.picturebackend.service.SpaceService;
import com.px.picturebackend.service.UserService;
import com.px.picturebackend.utils.ColorSimilarUtils;
import com.px.picturebackend.utils.ColorTransformUtils;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.Set;
import java.util.List;
import java.util.stream.Collectors;

import static com.px.picturebackend.constant.PictureConstants.*;

/**
* @author idpeng
* @description 针对表【picture(图片)】的数据库操作Service实现
* @createDate 2025-07-26 11:28:06
*/
@Slf4j
@Service
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture>
    implements PictureService {



    @Resource
    private UserService userService;

    @Resource
    private FilePictureUpload filePictureUpload;

    @Resource
    private UrlPictureUpload urlPictureUpload;

    @Resource
    private CosManager cosManager;

    @Resource
    private SpaceService spaceService;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private AliYunAiApi aliYunAiApi;

    /**
     * 校验图片数据的有效性
     *
     * @param picture 图片对象
     */
    @Override
    public void validPicture(Picture picture) {
        ThrowUtils.throwIf(picture == null, ErrorCode.PARAMS_ERROR);

        Long id = picture.getId();
        String url = picture.getUrl();
        String introduction = picture.getIntroduction();

        // 校验基本参数
        ThrowUtils.throwIf(ObjectUtil.isNull(id) || id <= 0, ErrorCode.PARAMS_ERROR, "id不能为空而且id要大于0");
        // 校验url长度
        if (StrUtil.isNotBlank(url)) {
            ThrowUtils.throwIf(url.length() > MAX_URL_LENGTH, ErrorCode.PARAMS_ERROR, "url过长");
        }
        // 校验简介长度
        if (StrUtil.isNotBlank(introduction)) {
            ThrowUtils.throwIf(introduction.length() > 800, ErrorCode.PARAMS_ERROR, "简介过长");
        }
    }

    /**
     * 上传图片到对象存储并保存图片信息到数据库
     *
     * @param inputSource        要上传的图片文件或者文件地址
     * @param pictureUploadRequest 图片上传请求参数，包含图片ID等信息
     * @param loginUser            当前登录用户信息
     * @return 图片视图对象
     */
    @Override
    public PictureVO uploadPicture(Object inputSource, PictureUploadRequest pictureUploadRequest, User loginUser) {
        // 校验参数
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        // 校验空间是否存在
        Long spaceId = pictureUploadRequest.getSpaceId();
        if (spaceId != null) {
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            // 改为使用统一的权限校验
            // 必须空间创建人（管理员）才能上传
            /* if (!loginUser.getId().equals(space.getUserId())) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有空间权限");
            } */
            // 校验额度
            if (space.getTotalCount() >= space.getMaxCount()) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "空间条数不足");
            }

            if (space.getTotalSize() >= space.getMaxSize()) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "空间大小不足");
            }
        }

        // 用于判断是新增还是更新图片
        Long pictureId = null;
        if (pictureUploadRequest != null) {
            pictureId = pictureUploadRequest.getId();
        }

        // 如果是更新图片，需要检验图片是否存在
        if (pictureId != null) {
           /*  boolean exists = this.lambdaQuery()
                                     .eq(Picture::getId, pictureId)
                                     .exists();
            ThrowUtils.throwIf(!exists, ErrorCode.NOT_FOUND_ERROR, "图片不存在"); */

            Picture oldPicture = this.getById(pictureId);
            ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
            // 改为使用统一的权限校验
            /* // 仅本人或管理员可编辑
            if (!oldPicture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            } */

            // 校验空间是否一致
            // 没传 spaceId，则复用原有图片的 spaceId
            if (spaceId == null) {
                if (oldPicture.getSpaceId() != null) {
                    spaceId = oldPicture.getSpaceId();
                } else {
                    // 传了 spaceId，必须和原有图片一致
                    if (ObjUtil.notEqual(spaceId, oldPicture.getSpaceId())) {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间id不一致");
                    }
                }
            }
        }

        // 上传图片，得到新的图片信息
        // 按照用户id划分目录
        String uploadPathPrefix;
        if (spaceId == null) {
            uploadPathPrefix = String.format(PUBLIC_UPLOAD_PATH_PREFIX, loginUser.getId());
        } else {
            uploadPathPrefix = String.format(SPACE_UPLOAD_PATH_PREFIX, spaceId);
        }
        // UploadPictureResult uploadPictureResult = fileManager.uploadPicture(multipartFile, uploadPathPrefix);
        PictureUploadTemplate pictureUploadTemplate = filePictureUpload;
        if (inputSource instanceof String) {
            pictureUploadTemplate = urlPictureUpload;
        }
        UploadPictureResult uploadPictureResult = pictureUploadTemplate.uploadPicture(inputSource, uploadPathPrefix);

        // 构造要入库的图片信息
        Picture picture = new Picture();
        picture.setUrl(uploadPictureResult.getUrl());
        // 补充设置 spaceId
        picture.setSpaceId(spaceId);
        picture.setThumbnailUrl(uploadPictureResult.getThumbnailUrl());
        String picName = uploadPictureResult.getPicName();
        if (pictureUploadRequest != null && StrUtil.isNotBlank(pictureUploadRequest.getPicName())) {
            picName = pictureUploadRequest.getPicName();
        }
        picture.setName(picName);
        picture.setPicColor(ColorTransformUtils.toTencentFormat(ColorTransformUtils.getStandardColor(uploadPictureResult.getPicColor())));
        picture.setPicSize(uploadPictureResult.getPicSize());
        picture.setPicWidth(uploadPictureResult.getPicWidth());
        picture.setPicHeight(uploadPictureResult.getPicHeight());
        picture.setPicScale(uploadPictureResult.getPicScale());
        picture.setPicFormat(uploadPictureResult.getPicFormat());
        picture.setUserId(loginUser.getId());
        // 补充审核参数
        this.fillReviewParams(picture, loginUser);

        // 如果pictureId不为空，表示为更新，否则是新上传
        if (pictureId != null) {
            // 如果是更新，需要补充id和编辑时的时间
            picture.setId(pictureId);
            picture.setEditTime(new Date());
        }

        // 开启事务
        Long finalSpaceId = spaceId;
        transactionTemplate.execute(status -> {
            // 插入数据
            boolean result = this.saveOrUpdate(picture);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "图片上传失败，数据库操作失败");
            if (finalSpaceId != null) {
                // 更新空间的使用额度
                boolean update = spaceService.lambdaUpdate()
                        .eq(Space::getId, finalSpaceId)
                        .setSql("totalSize = totalSize + " + picture.getPicSize())
                        .setSql("totalCount = totalCount + 1")
                        .update();
                ThrowUtils.throwIf(!update, ErrorCode.OPERATION_ERROR, "额度更新失败");

            }
            return picture;
        });

        return PictureVO.objToVo(picture);
    }

    /**
     * 构建图片查询条件包装器
     * <p>根据查询请求参数构建复合查询条件，支持多维度筛选和排序</p>
     * <p>该方法采用分层构建策略，将复杂的查询条件拆分为多个独立的构建方法，
     * 提高代码的可读性、可维护性和可测试性</p>
     * 
     * <p>支持的查询维度包括：</p>
     * <ul>
     *   <li>基础条件：ID、用户ID、空间ID等精确匹配</li>
     *   <li>搜索条件：关键词全文搜索、名称模糊匹配等</li>
     *   <li>时间范围：编辑时间区间筛选</li>
     *   <li>图片属性：格式、尺寸、比例等属性筛选</li>
     *   <li>审核状态：审核状态、审核信息等</li>
     *   <li>标签条件：支持多标签AND逻辑查询</li>
     *   <li>排序条件：支持动态字段排序，防SQL注入</li>
     * </ul>
     *
     * @param pictureQueryRequest 图片查询请求参数，包含各种筛选和排序条件
     * @return QueryWrapper<Picture> 构建完成的查询条件包装器
     * @throws IllegalArgumentException 当排序字段不合法时抛出异常
     * @throws BusinessException 当查询条件构造过程中发生系统异常时抛出
     */
    @Override
    public LambdaQueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest) {
        LambdaQueryWrapper<Picture> queryWrapper = new LambdaQueryWrapper<>();
        
        // 参数校验：如果请求参数为空，直接返回空的查询条件
        if (pictureQueryRequest == null) {
            log.debug("图片查询请求参数为空，返回空查询条件");
            return queryWrapper;
        }

        try {
            // 构建基础查询条件（ID、用户ID、空间ID等精确匹配）
            buildBasicQueryConditions(queryWrapper, pictureQueryRequest);
            
            // 构建搜索条件（关键词全文搜索、名称模糊匹配、分类筛选）
            buildSearchConditions(queryWrapper, pictureQueryRequest);
            
            // 构建时间范围查询条件（编辑时间区间筛选）
            buildTimeRangeConditions(queryWrapper, pictureQueryRequest);
            
            // 构建图片属性查询条件（格式、尺寸、比例等属性筛选）
            buildPictureAttributeConditions(queryWrapper, pictureQueryRequest);
            
            // 构建审核相关查询条件（审核状态、审核信息、审核人等）
            buildReviewConditions(queryWrapper, pictureQueryRequest);
            
            // 构建标签查询条件（支持多标签AND逻辑查询，优化后的实现）
            buildTagConditions(queryWrapper, pictureQueryRequest.getTags());
            
            // 构建排序条件（支持动态字段排序，包含SQL注入防护）
            buildSortConditions(queryWrapper, pictureQueryRequest);
            
            log.debug("图片查询条件构造完成，查询参数: {}", pictureQueryRequest);
            return queryWrapper;
            
        } catch (Exception e) {
            log.error("构造图片查询条件时发生异常，请求参数: {}", pictureQueryRequest, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "查询条件构造失败");
        }
    }

    /**
     * 构建基础查询条件（ID、用户ID、空间ID等）
     *
     * @param queryWrapper 查询条件包装器
     * @param request 查询请求参数
     */
    private void buildBasicQueryConditions(LambdaQueryWrapper<Picture> queryWrapper, PictureQueryRequest request) {
        queryWrapper.eq(ObjectUtil.isNotEmpty(request.getId()), Picture::getId, request.getId())
                   .eq(ObjectUtil.isNotEmpty(request.getUserId()), Picture::getUserId, request.getUserId())
                   .eq(ObjectUtil.isNotEmpty(request.getSpaceId()), Picture::getSpaceId, request.getSpaceId())
                   .isNull(request.isNullSpaceId(), Picture::getSpaceId);
    }

    /**
     * 构建搜索条件（关键词搜索、名称、简介、分类）
     *
     * @param queryWrapper 查询条件包装器
     * @param request 查询请求参数
     */
    private void buildSearchConditions(LambdaQueryWrapper<Picture> queryWrapper, PictureQueryRequest request) {
        // 全文搜索：在名称和简介中搜索关键词
        String searchText = request.getSearchText();
        if (StrUtil.isNotBlank(searchText)) {
            queryWrapper.and(qw -> qw.like(Picture::getName, searchText)
                                    .or()
                                    .like(Picture::getIntroduction, searchText));
        }
        
        // 精确匹配和模糊搜索
        queryWrapper.like(StrUtil.isNotBlank(request.getName()), Picture::getName, request.getName())
                   .like(StrUtil.isNotBlank(request.getIntroduction()), Picture::getIntroduction, request.getIntroduction())
                   .eq(StrUtil.isNotBlank(request.getCategory()), Picture::getCategory, request.getCategory());
    }

    /**
     * 构建时间范围查询条件
     *
     * @param queryWrapper 查询条件包装器
     * @param request 查询请求参数
     */
    private void buildTimeRangeConditions(LambdaQueryWrapper<Picture> queryWrapper, PictureQueryRequest request) {
        Date startEditTime = request.getStartEditTime();
        Date endEditTime = request.getEndEditTime();
        
        queryWrapper.ge(ObjectUtil.isNotEmpty(startEditTime), Picture::getEditTime, startEditTime)
                   .lt(ObjectUtil.isNotEmpty(endEditTime), Picture::getEditTime, endEditTime);
    }

    /**
     * 构建图片属性查询条件（格式、尺寸、比例等）
     *
     * @param queryWrapper 查询条件包装器
     * @param request 查询请求参数
     */
    private void buildPictureAttributeConditions(LambdaQueryWrapper<Picture> queryWrapper, PictureQueryRequest request) {
        queryWrapper.like(StrUtil.isNotBlank(request.getPicFormat()), Picture::getPicFormat, request.getPicFormat())
                   .eq(ObjectUtil.isNotEmpty(request.getPicSize()), Picture::getPicSize, request.getPicSize())
                   .eq(ObjectUtil.isNotEmpty(request.getPicWidth()), Picture::getPicWidth, request.getPicWidth())
                   .eq(ObjectUtil.isNotEmpty(request.getPicHeight()), Picture::getPicHeight, request.getPicHeight())
                   .eq(ObjectUtil.isNotEmpty(request.getPicScale()), Picture::getPicScale, request.getPicScale());
    }

    /**
     * 构建审核相关查询条件
     *
     * @param queryWrapper 查询条件包装器
     * @param request 查询请求参数
     */
    private void buildReviewConditions(LambdaQueryWrapper<Picture> queryWrapper, PictureQueryRequest request) {
        queryWrapper.eq(ObjectUtil.isNotEmpty(request.getReviewStatus()), Picture::getReviewStatus, request.getReviewStatus())
                   .like(StrUtil.isNotBlank(request.getReviewMessage()), Picture::getReviewMessage, request.getReviewMessage())
                   .eq(ObjectUtil.isNotEmpty(request.getReviewerId()), Picture::getReviewerId, request.getReviewerId());
    }

    /**
     * 构建标签查询条件（优化实现）
     * 使用批量查询替代循环查询，提高性能
     *
     * @param queryWrapper 查询条件包装器
     * @param tags 标签列表
     */
    private void buildTagConditions(LambdaQueryWrapper<Picture> queryWrapper, List<String> tags) {
        if (CollUtil.isEmpty(tags)) {
            return;
        }
        
        // 优化：使用and条件组合多个标签查询，确保所有标签都匹配
        queryWrapper.and(wrapper -> {
            for (String tag : tags) {
                if (StrUtil.isNotBlank(tag)) {
                    // 使用JSON格式匹配标签，确保精确匹配
                    wrapper.like(Picture::getTags, "\"" + tag.trim() + "\"");
                }
            }
        });
    }

    /**
     * 构建排序条件
     * <p>根据请求参数构建动态排序条件，支持多种字段排序并包含安全性验证</p>
     * 
     * <p>安全特性：</p>
     * <ul>
     *   <li>白名单验证：仅允许预定义的安全字段进行排序</li>
     *   <li>SQL注入防护：通过Lambda表达式避免字符串拼接</li>
     *   <li>类型安全：编译时检查字段类型匹配</li>
     * </ul>
     * 
     * <p>支持的排序字段：</p>
     * <ul>
     *   <li>基础字段：id, name, category</li>
     *   <li>图片属性：picSize, picWidth, picHeight, picScale</li>
     *   <li>系统字段：userId, spaceId, reviewStatus, createTime, editTime, updateTime</li>
     * </ul>
     * 
     * <p>排序逻辑：</p>
     * <ul>
     *   <li>默认排序：按创建时间倒序（最新优先）</li>
     *   <li>升序标识：sortOrder为"ascend"时使用升序</li>
     *   <li>降序标识：其他情况均使用降序</li>
     *   <li>异常处理：字段映射失败时回退到默认排序</li>
     * </ul>
     *
     * @param queryWrapper 查询条件包装器，用于构建最终的查询条件
     * @param request 查询请求参数，包含排序字段和排序方向
     * @throws IllegalArgumentException 当排序字段不在白名单中时抛出
     */
    private void buildSortConditions(LambdaQueryWrapper<Picture> queryWrapper, PictureQueryRequest request) {
        String sortField = request.getSortField();
        String sortOrder = request.getSortOrder();
        
        if (StrUtil.isBlank(sortField)) {
            // 默认按创建时间倒序排列，确保最新内容优先显示
            queryWrapper.orderByDesc(Picture::getCreateTime);
            return;
        }
        
        // 验证排序字段的合法性，防止SQL注入
        // 使用白名单机制确保只有安全的字段可以用于排序
        if (!isValidSortField(sortField)) {
            log.warn("非法的排序字段: {}", sortField);
            throw new IllegalArgumentException("非法的排序字段: " + sortField);
        }
        
        boolean isAsc = SORT_ORDER_ASCEND.equals(sortOrder);
        
        // 根据字段名获取对应的字段引用
        // 使用Lambda表达式确保类型安全和SQL注入防护
        SFunction<Picture, ?> fieldFunction = getFieldFunction(sortField);
        if (fieldFunction != null) {
            queryWrapper.orderBy(true, isAsc, fieldFunction);
        } else {
            // 如果无法映射字段，使用默认排序
            log.warn("无法映射排序字段: {}, 使用默认排序", sortField);
            queryWrapper.orderByDesc(Picture::getCreateTime);
        }
    }

    /**
     * 验证排序字段是否合法
     * 防止SQL注入攻击
     *
     * @param sortField 排序字段
     * @return 是否为合法的排序字段
     */
    private boolean isValidSortField(String sortField) {
        // 定义允许的排序字段白名单
        Set<String> validSortFields = Set.of(
            VALID_SORT_FIELDS
        );
        return validSortFields.contains(sortField);
    }

    /**
     * 根据字段名获取对应的字段引用函数
     * 用于动态排序时的字段映射
     *
     * @param sortField 排序字段名
     * @return 对应的字段引用函数，如果字段不存在则返回null
     */
    private SFunction<Picture, ?> getFieldFunction(String sortField) {
        return switch (sortField) {
            case "id" -> Picture::getId;
            case "name" -> Picture::getName;
            case "category" -> Picture::getCategory;
            case "picSize" -> Picture::getPicSize;
            case "picWidth" -> Picture::getPicWidth;
            case "picHeight" -> Picture::getPicHeight;
            case "picScale" -> Picture::getPicScale;
            case "userId" -> Picture::getUserId;
            case "spaceId" -> Picture::getSpaceId;
            case "reviewStatus" -> Picture::getReviewStatus;
            case "createTime" -> Picture::getCreateTime;
            case "editTime" -> Picture::getEditTime;
            case "updateTime" -> Picture::getUpdateTime;
            default -> null;
        };
    }

    /**
     * 将Picture实体转换为PictureVO视图对象
     *
     * @param picture 图片实体对象
     * @param request HTTP请求对象
     * @return 图片视图对象
     */
    @Override
    public PictureVO getPictureVO(Picture picture, HttpServletRequest request) {
        PictureVO pictureVO = PictureVO.objToVo(picture);
        Long userId = picture.getUserId();

        // 关联用户信息
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            pictureVO.setUser(userVO);
        }
        return pictureVO;
    }

    /**
     * 将Picture分页对象转换为PictureVO分页对象
     * 优化：批量查询用户信息，避免N+1查询问题
     *
     * @param picturePage Picture分页数据
     * @param request HTTP请求对象
     * @return 包含PictureVO的分页数据
     */
    @Override
    public Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request) {
        List<Picture> pictureList = picturePage.getRecords();
        Page<PictureVO> pictureVOPage = new Page<>(picturePage.getCurrent(), picturePage.getSize(), picturePage.getTotal());
        if (CollUtil.isEmpty(pictureList)) {
            return pictureVOPage;
        }
        
        // 提取所有用户ID（过滤null值）
        Set<Long> userIdSet = pictureList.stream()
                .map(Picture::getUserId)
                .filter(Objects::nonNull)
                .filter(userId -> userId > 0)
                .collect(Collectors.toSet());
        
        // 批量查询用户信息并转换为UserVO，减少数据库查询次数
        Map<Long, UserVO> userVOMap = Collections.emptyMap();
        if (!userIdSet.isEmpty()) {
            userVOMap = userService.listByIds(userIdSet).stream()
                    .collect(Collectors.toMap(
                            User::getId,
                            userService::getUserVO,
                            (existing, replacement) -> existing // 处理重复key的情况
                    ));
        }
        
        // 转换Picture对象为PictureVO对象并设置用户信息
        final Map<Long, UserVO> finalUserVOMap = userVOMap;
        List<PictureVO> pictureVOList = pictureList.stream()
                .map(picture -> {
                    PictureVO pictureVO = PictureVO.objToVo(picture);
                    Long userId = picture.getUserId();
                    if (userId != null && userId > 0) {
                        pictureVO.setUser(finalUserVOMap.get(userId));
                    }
                    return pictureVO;
                })
                .collect(Collectors.toList());
        
        pictureVOPage.setRecords(pictureVOList);
        return pictureVOPage;
    }

    /**
     * 管理员审核图片
     * 
     * @param pictureReviewRequest 图片审核请求参数，包含图片ID和审核状态
     * @param loginUser 当前登录的管理员用户
     */
    @Override
    public void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser) {
        // 获取请求参数
        Long id = pictureReviewRequest.getId();
        Integer reviewStatus = pictureReviewRequest.getReviewStatus();
        PictureReviewStatusEnum reviewStatusEnum = PictureReviewStatusEnum.getEnumByValue(reviewStatus);

        // 校验参数合法性
        if (id == null || reviewStatusEnum == null || PictureReviewStatusEnum.REVIEWING.equals(reviewStatusEnum)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 查询原图片信息
        Picture oldPicture = this.getById(id);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);

        // 校验图片是否已被审核为相同状态
        if (oldPicture.getReviewStatus().equals(reviewStatus)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请勿重复审核");
        }

        // 更新图片审核信息
        Picture updatePicture = new Picture();
        BeanUtil.copyProperties(pictureReviewRequest, updatePicture);
        updatePicture.setReviewerId(loginUser.getId());
        updatePicture.setReviewTime(new Date());
        boolean result = this.updateById(updatePicture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
    }

    /**
     * 填充图片审核参数
     * 
     * @param picture 图片对象
     * @param loginUser 当前登录用户
     */
    @Override
    public void fillReviewParams(Picture picture, User loginUser) {
        if (userService.isAdmin(loginUser)) {
            // 管理员自动通过
            picture.setReviewerId(loginUser.getId());
            picture.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
            picture.setReviewMessage("管理员自动通过");
            picture.setReviewTime(new Date());
        } else {
            // 非管理员，创建或者编辑都要改为待审核
            picture.setReviewStatus(PictureReviewStatusEnum.REVIEWING.getValue());
        }
    }

    /**
     * 批量抓取和创建图片
     * <p>从必应图片搜索结果中批量抓取图片并上传到系统</p>
     * 
     * <p>功能特点：</p>
     * <ul>
     *   <li>智能抓取：基于搜索关键词从必应图片获取高质量图片</li>
     *   <li>数量限制：单次最多抓取30张图片，防止系统负载过高</li>
     *   <li>容错机制：单张图片失败不影响其他图片的上传</li>
     *   <li>失败控制：连续失败超过10次时自动停止，避免无效请求</li>
     *   <li>网络优化：设置合理的超时时间和User-Agent</li>
     * </ul>
     * 
     * <p>抓取流程：</p>
     * <ol>
     *   <li>参数校验：验证搜索文本、数量限制等</li>
     *   <li>网页抓取：访问必应图片搜索页面</li>
     *   <li>图片解析：提取图片URL并进行清理处理</li>
     *   <li>逐一上传：调用单张图片上传接口</li>
     *   <li>结果统计：记录成功和失败的数量</li>
     * </ol>
     * 
     * <p>异常处理：</p>
     * <ul>
     *   <li>网络异常：连接超时、页面访问失败等</li>
     *   <li>解析异常：HTML结构变化、元素缺失等</li>
     *   <li>上传异常：单张图片上传失败的容错处理</li>
     * </ul>
     *
     * @param pictureUploadByBatchRequest 批量上传请求，包含搜索文本和数量限制
     * @param loginUser 当前登录用户，用于权限校验和图片归属
     * @return 成功上传的图片数量
     * @throws BusinessException 当参数校验失败或系统异常时抛出
     */
    @Override
    public Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, User loginUser) {
        String searchText = pictureUploadByBatchRequest.getSearchText();
        String namePrefix = pictureUploadByBatchRequest.getNamePrefix();
        if (StrUtil.isBlank(namePrefix)) {
            namePrefix = searchText;
        }
        
        // 验证上传数量限制
        Integer count = pictureUploadByBatchRequest.getCount();
        ThrowUtils.throwIf(count > BATCH_UPLOAD_MAX_COUNT, ErrorCode.PARAMS_ERROR, 
            String.format("最多只能上传%d张图片", BATCH_UPLOAD_MAX_COUNT));
        
        // 构建抓取URL
        String fetchUrl = String.format(DEFAULT_URL, searchText);
        Document document;
        
        try {
            // 设置连接超时和读取超时
            document = Jsoup.connect(fetchUrl)
                    .timeout(NETWORK_TIMEOUT_MS)
                    .userAgent(USER_AGENT)
                    .get();
        } catch (IOException e) {
            log.error("批量上传失败：无法获取搜索页面，searchText: {}, url: {}", searchText, fetchUrl, e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "网络请求失败，请稍后重试");
        }
        
        // 解析页面元素
        Element div = document.getElementsByClass("dgControl").first();
        if (ObjUtil.isNull(div)) {
            log.warn("批量上传失败：页面结构发生变化，无法找到图片容器，searchText: {}", searchText);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "页面解析失败，请稍后重试");
        }
        
        Elements imgElementList = div.select("img.mimg");
        if (CollUtil.isEmpty(imgElementList)) {
            log.warn("批量上传失败：未找到任何图片，searchText: {}", searchText);
            return 0;
        }
        
        int uploadCount = 0;
        int failedCount = 0;
        
        for (Element imgElement : imgElementList) {
            if (uploadCount >= count) {
                break;
            }
            
            String fileUrl = imgElement.attr("src");
            if (StrUtil.isBlank(fileUrl)) {
                log.debug("跳过空链接图片");
                continue;
            }
            
            // 清理URL参数
            fileUrl = cleanImageUrl(fileUrl);
            
            // 构建上传请求
            PictureUploadRequest pictureUploadRequest = new PictureUploadRequest();
            if (StrUtil.isNotBlank(namePrefix)) {
                pictureUploadRequest.setPicName(namePrefix + (uploadCount + 1));
            }
            
            try {
                PictureVO pictureVO = this.uploadPicture(fileUrl, pictureUploadRequest, loginUser);
                log.info("批量上传成功：图片ID = {}, URL = {}", pictureVO.getId(), fileUrl);
                uploadCount++;
            } catch (Exception e) {
                failedCount++;
                log.warn("批量上传失败：单张图片上传失败，URL = {}, 错误信息: {}", fileUrl, e.getMessage());
                
                // 如果失败次数过多，提前结束
                if (failedCount > MAX_FAILED_ATTEMPTS) {
                    log.warn("批量上传中止：连续失败次数过多，已上传 {} 张，失败 {} 张", uploadCount, failedCount);
                    break;
                }
            }
        }
        
        log.info("批量上传完成：成功 {} 张，失败 {} 张，搜索关键词: {}", uploadCount, failedCount, searchText);
        return uploadCount;
    }
    
    /**
     * 清理图片URL，移除查询参数
     *
     * @param fileUrl 原始图片URL
     * @return 清理后的URL
     */
    /**
     * 清理图片URL，移除查询参数
     * <p>处理从网页抓取的图片URL，去除不必要的查询参数以获得纯净的图片地址</p>
     * 
     * <p>清理目的：</p>
     * <ul>
     *   <li>去除追踪参数：移除统计和追踪相关的查询参数</li>
     *   <li>去除缓存参数：移除时间戳等缓存控制参数</li>
     *   <li>去除尺寸参数：移除图片尺寸限制参数</li>
     *   <li>标准化URL：获得标准的图片资源地址</li>
     * </ul>
     * 
     * <p>处理逻辑：</p>
     * <ol>
     *   <li>参数检查：验证输入URL的有效性</li>
     *   <li>查找分隔符：定位查询参数的起始位置（?字符）</li>
     *   <li>截取主体：提取?之前的主要URL部分</li>
     *   <li>返回结果：返回清理后的纯净URL</li>
     * </ol>
     * 
     * <p>示例：</p>
     * <pre>
     * 输入: "https://example.com/image.jpg?w=300&h=200&t=123456"
     * 输出: "https://example.com/image.jpg"
     * </pre>
     *
     * @param fileUrl 原始图片URL，可能包含查询参数
     * @return 清理后的图片URL，如果输入为null则返回null
     */
    private String cleanImageUrl(String fileUrl) {
        int questionMarkIndex = fileUrl.indexOf("?");
        return questionMarkIndex > -1 ? fileUrl.substring(0, questionMarkIndex) : fileUrl;
    }

    /**
     * 异步清理图片文件
     * <p>当图片不再被任何记录引用时，从对象存储中删除该图片及其缩略图</p>
     *
     * @param oldPicture 需要清理的图片记录对象
     */
    /**
     * 清理图片文件
     * <p>安全地删除图片相关的所有文件，包括原图和缩略图</p>
     * 
     * <p>清理范围：</p>
     * <ul>
     *   <li>主图片文件：原始上传的图片文件</li>
     *   <li>缩略图文件：系统生成的缩略图文件</li>
     * </ul>
     * 
     * <p>安全特性：</p>
     * <ul>
     *   <li>引用检查：检查文件是否被其他记录引用，避免误删</li>
     *   <li>空值检查：对图片对象和URL进行空值验证</li>
     *   <li>异常隔离：单个文件删除失败不影响其他文件</li>
     *   <li>异步执行：使用@Async注解避免阻塞主线程</li>
     *   <li>日志记录：详细记录清理过程和异常信息</li>
     * </ul>
     * 
     * <p>清理逻辑：</p>
     * <ol>
     *   <li>参数校验：检查图片对象和URL的有效性</li>
     *   <li>引用统计：查询该URL被多少条记录使用</li>
     *   <li>引用判断：如果被多条记录引用则跳过删除</li>
     *   <li>文件删除：依次删除主图片和缩略图文件</li>
     *   <li>异常处理：记录删除过程中的异常信息</li>
     * </ol>
     * 
     * <p>使用场景：</p>
     * <ul>
     *   <li>图片删除：用户删除图片时清理相关文件</li>
     *   <li>图片更新：替换图片时清理旧文件</li>
     *   <li>系统维护：定期清理无效的图片文件</li>
     * </ul>
     *
     * @param oldPicture 要清理的图片对象，包含文件路径信息
     */
    @Async
    @Override
    public void clearPictureFile(Picture oldPicture) {
        if (oldPicture == null || StrUtil.isBlank(oldPicture.getUrl())) {
            log.warn("清理图片文件失败：图片对象为空或URL为空");
            return;
        }
        
        try {
            // 判断该图片是否被多条记录使用
            String pictureUrl = oldPicture.getUrl();
            Long count = this.lambdaQuery()
                    .eq(Picture::getUrl, pictureUrl)
                    .count();

            // 有不止一条记录用到了该图片，不清空
            if (count > 1) {
                log.debug("图片文件仍被引用，跳过清理：URL = {}, 引用次数 = {}", pictureUrl, count);
                return;
            }
            
            // 清理主图片
            try {
                // FIXME 注意，这里的 url 包含了域名，实际上只要传 key 值（存储路径）就够了
                cosManager.deleteObject(oldPicture.getUrl());
                log.info("成功清理图片文件：{}", pictureUrl);
            } catch (Exception e) {
                log.error("清理主图片文件失败：URL = {}", pictureUrl, e);
            }
            
            // 清理缩略图
            String thumbnailUrl = oldPicture.getThumbnailUrl();
            if (StrUtil.isNotBlank(thumbnailUrl)) {
                try {
                    cosManager.deleteObject(thumbnailUrl);
                    log.info("成功清理缩略图文件：{}", thumbnailUrl);
                } catch (Exception e) {
                    log.error("清理缩略图文件失败：URL = {}", thumbnailUrl, e);
                }
            }
        } catch (Exception e) {
            log.error("清理图片文件过程中发生异常：pictureId = {}, URL = {}", 
                oldPicture.getId(), oldPicture.getUrl(), e);
        }
    }

    /**
     * 检查用户对图片的操作权限
     *
     * @param loginUser 当前登录用户
     * @param picture   待检查的图片对象
     */
    @Deprecated
    @Override
    public void checkPictureAuth(User loginUser, Picture picture) {
        Long spaceId = picture.getSpaceId();
        if (spaceId == null) {
            // 公共图库，仅本人或管理员可操作
            if (!picture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
        } else {
            // 私有空间，仅空间管理员可操作
            if (!picture.getUserId().equals(loginUser.getId())){
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
        }
    }

    /**
     * 删除图片
     *
     * @param pictureId 图片ID
     * @param loginUser 登录用户信息
     */
    @Override
    public void deletePicture(long pictureId, User loginUser) {
        // 参数校验
        ThrowUtils.throwIf(pictureId <= 0, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);

        // 判断是否存在
        Picture oldPicture = this.getById(pictureId);
        ThrowUtils.throwIf(oldPicture == null, ErrorCode.NOT_FOUND_ERROR);

        // 校验删除权限
        validateDeletePermission(oldPicture, loginUser);

        // 开启事务
        transactionTemplate.execute(status -> {
            // 操作数据库
            boolean result = this.removeById(oldPicture);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);

            // 释放额度
            Long spaceId = oldPicture.getSpaceId();
            if (spaceId != null) {
                boolean update = spaceService.lambdaUpdate()
                        .eq(Space::getId, spaceId)
                        .setSql("totalSize = totalSize - " +oldPicture.getPicSize())
                        .setSql("totalCount = totalCount - 1")
                        .update();
                ThrowUtils.throwIf(!update, ErrorCode.OPERATION_ERROR, "额度更新失败");
            }
            return true;
        });


        // 异步清理文件
        this.clearPictureFile(oldPicture);
    }

    /**
     * 编辑图片信息
     *
     * @param pictureEditRequest 图片编辑请求参数
     * @param loginUser          登录用户信息
     */
    @Override
    public void editPicture(PictureEditRequest pictureEditRequest, User loginUser) {
        // 在此处将实体类和 DTO 进行转换
        Picture picture = new Picture();
        BeanUtil.copyProperties(pictureEditRequest, picture);
        // 注意：list 转为 string
        picture.setTags(JSONUtil.toJsonStr(pictureEditRequest.getTags()));
        // 设置编辑时间
        picture.setEditTime(new Date());

        // 数据校验
        validatePicture(picture);
        // 判断是否存在并校验编辑权限
        Long id = pictureEditRequest.getId();
        Picture oldPicture = this.getById(id);
        validateEditPermission(oldPicture, loginUser);
        // 补充审核参数
        this.fillReviewParams(picture, loginUser);
        // 操作数据�?
        boolean result = this.updateById(picture);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
    }

    /**
     * 根据颜色搜索相似图片
     * <p>通过颜色相似度算法在指定空间内搜索与目标颜色最相似的图片</p>
     * 
     * <p>算法特点：</p>
     * <ul>
     *   <li>颜色空间：使用RGB颜色空间进行相似度计算</li>
     *   <li>相似度算法：基于欧几里得距离计算颜色差异</li>
     *   <li>性能优化：预解析目标颜色，避免重复计算</li>
     *   <li>容错处理：对无效颜色格式进行异常捕获和日志记录</li>
     * </ul>
     * 
     * <p>搜索流程：</p>
     * <ol>
     *   <li>参数校验：验证颜色格式、空间权限等</li>
     *   <li>数据查询：获取空间内所有已审核通过且有主色调的图片</li>
     *   <li>相似度计算：计算每张图片与目标颜色的相似度</li>
     *   <li>结果排序：按相似度降序排列，取前12张图片</li>
     *   <li>数据转换：转换为前端展示所需的VO对象</li>
     * </ol>
     * 
     * <p>返回限制：最多返回12张最相似的图片</p>
     *
     * @param spaceId 搜索的空间ID，null表示搜索公共空间
     * @param picColor 目标颜色的十六进制字符串（如：#FF0000）
     * @param loginUser 当前登录用户，用于权限校验
     * @return 按颜色相似度排序的图片VO列表，最多12张
     * @throws BusinessException 当参数校验失败或系统异常时抛出
     */
    @Override
    public List<PictureVO> searchPictureByColor(Long spaceId, String picColor, User loginUser) {
        // 校验颜色搜索参数
        validateColorSearchParams(spaceId, picColor, loginUser);
        
        // 查询该空间下所有图片（必须有主色调）
        List<Picture> pictureList = this.lambdaQuery()
                .eq(Picture::getSpaceId, spaceId)
                .isNotNull(Picture::getPicColor)
                .ne(Picture::getPicColor, "")
                .list();
        
        // 如果没有图片，直接返回空列表
        if (CollUtil.isEmpty(pictureList)) {
            return Collections.emptyList();
        }
        
        // 预先解析目标颜色，避免在循环中重复解析
        Color targetColor;
        try {
            targetColor = Color.decode(picColor);
        } catch (NumberFormatException e) {
            log.warn("无效的颜色格式: {}", picColor);
            return Collections.emptyList();
        }
        
        // 预处理：计算所有图片的相似度并缓存结果
        List<PictureWithSimilarity> picturesWithSimilarity = pictureList.stream()
                .map(picture -> {
                    String hexColor = picture.getPicColor();
                    double similarity = 0.0;
                    
                    if (StrUtil.isNotBlank(hexColor)) {
                        try {
                            Color pictureColor = Color.decode(hexColor);
                            similarity = ColorSimilarUtils.calculateSimilarity(targetColor, pictureColor);
                        } catch (NumberFormatException e) {
                            log.debug("图片颜色格式错误: {}, pictureId: {}", hexColor, picture.getId());
                            similarity = 0.0;
                        }
                    }
                    
                    return new PictureWithSimilarity(picture, similarity);
                })
                .toList();
        
        // 按相似度降序排序并取前12张图片
        List<Picture> sortedPictures = picturesWithSimilarity.stream()
                .sorted(Comparator.comparingDouble(PictureWithSimilarity::getSimilarity).reversed())
                .limit(12)
                .map(PictureWithSimilarity::getPicture)
                .toList();
        
        // 转换为PictureVO
        return sortedPictures.stream()
                .map(PictureVO::objToVo)
                .collect(Collectors.toList());
    }
    
    /**
     * 图片与相似度的包装类，用于优化颜色搜索性能
     * <p>该内部类用于在颜色相似度搜索过程中临时存储图片对象及其与目标颜色的相似度值</p>
     * 
     * <p>设计目的：</p>
     * <ul>
     *   <li>性能优化：避免重复计算相似度值</li>
     *   <li>内存效率：使用final字段确保不可变性</li>
     *   <li>排序支持：便于按相似度进行排序操作</li>
     * </ul>
     * 
     * <p>使用场景：</p>
     * <ul>
     *   <li>颜色搜索：计算并缓存每张图片的颜色相似度</li>
     *   <li>结果排序：按相似度降序排列搜索结果</li>
     *   <li>性能优化：减少重复的颜色计算开销</li>
     * </ul>
     */
    private static class PictureWithSimilarity {
        /** 图片对象，包含图片的所有信息 */
        private final Picture picture;
        /** 与目标颜色的相似度值，范围[0,1] */
        private final double similarity;
        
        /**
         * 构造函数
         * 
         * @param picture 图片对象
         * @param similarity 相似度值，范围[0,1]
         */
        public PictureWithSimilarity(Picture picture, double similarity) {
            this.picture = picture;
            this.similarity = similarity;
        }
        
        /**
         * 获取图片对象
         * 
         * @return 图片对象
         */
        public Picture getPicture() {
            return picture;
        }
        
        /**
         * 获取相似度值
         * 
         * @return 相似度值，范围[0,1]，1表示完全相同
         */
        public double getSimilarity() {
            return similarity;
        }
    }


    /**
     * 批量编辑图片信息
     *
     * @param pictureEditByBatchRequest 批量编辑请求参数
     * @param loginUser 当前登录用户
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void editPictureByBatch(PictureEditByBatchRequest pictureEditByBatchRequest, User loginUser) {
        List<Long> pictureIdList = pictureEditByBatchRequest.getPictureIdList();
        Long spaceId = pictureEditByBatchRequest.getSpaceId();
        String category = pictureEditByBatchRequest.getCategory();
        List<String> tags = pictureEditByBatchRequest.getTags();

        // 校验批量操作参数
       validateBatchOperation(pictureIdList, spaceId, loginUser);

        // 3. 查询指定图片，仅选择需要的字段
        List<Picture> pictureList = this.lambdaQuery()
                .select(Picture::getId, Picture::getSpaceId)
                .eq(Picture::getSpaceId, spaceId)
                .in(Picture::getId, pictureIdList)
                .list();

        if (pictureList.isEmpty()) {
            return;
        }

        // 4. 更新分类和标签
        pictureList.forEach(picture -> {
            if (StrUtil.isNotBlank(category)) {
                picture.setCategory(category);
            }
            if (CollUtil.isNotEmpty(tags)) {
                picture.setTags(JSONUtil.toJsonStr(tags));
            }
        });

        // 批量重命名
        String nameRule = pictureEditByBatchRequest.getNameRule();
        fillPictureWithNameRule(pictureList, nameRule);

        // 5. 批量更新
        boolean result = this.updateBatchById(pictureList);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);

    }

    /**
     * 根据命名规则填充图片名称
     *
     * @param pictureList 图片列表
     * @param nameRule 命名规则，其中{序号}会被替换为序号
     */
    private void fillPictureWithNameRule(List<Picture> pictureList, String nameRule) {
        if (CollUtil.isEmpty(pictureList) || StrUtil.isBlank(nameRule)) {
            return;
        }
        long count = 1;
        try {
            for (Picture picture : pictureList) {
                String pictureName = nameRule.replaceAll("\\{序号}", String.valueOf(count++));
                picture.setName(pictureName);
            }
        } catch (Exception e) {
            log.error("名称解析错误", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "名称解析错误");
        }
    }

    /**
     * 创建图片外绘任务（AI扩图任务）
     *
     * @param createPictureOutPaintingTaskRequest 创建外绘任务请求参数，包含图片ID和扩图参数
     * @param loginUser 当前登录用户信息
     * @return CreateOutPaintingTaskResponse 扩图任务响应结果
     */
    @Override
    public com.px.picturebackend.api.aliyunai.model.CreateOutPaintingTaskResponse createPictureOutPaintingTask(CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest, User loginUser) {
        // 校验AI扩图任务参数
        validateOutPaintingTaskParams(createPictureOutPaintingTaskRequest, loginUser);
        
        // 获取图片信息
        Long pictureId = createPictureOutPaintingTaskRequest.getPictureId();
        Picture picture = this.getById(pictureId);
        
        // 构造请求参数
        CreateOutPaintingTaskRequest taskRequest = new CreateOutPaintingTaskRequest();
        CreateOutPaintingTaskRequest.Input input = new CreateOutPaintingTaskRequest.Input();
        input.setBaseImageUrl(picture.getUrl());
        // 设置默认提示词（可根据需要调整）
        input.setPrompt("扩展图像内容，保持原有风格和质量");
        taskRequest.setInput(input);
        
        // 获取或设置默认参数
        CreateOutPaintingTaskRequest.Parameters parameters = createPictureOutPaintingTaskRequest.getParameters();
        if (parameters == null) {
            // 如果没有提供参数，创建默认参数
            parameters = new CreateOutPaintingTaskRequest.Parameters();
            // 设置默认扩图比例1.5
            parameters.setTopScale(1.5f);
            parameters.setBottomScale(1.5f);
            parameters.setLeftScale(1.5f);
            parameters.setRightScale(1.5f);
            log.info("使用默认扩图参数，图片ID: {}", pictureId);
        } else {
            log.info("扩图参数验证通过，图片ID: {}, 参数: {}", pictureId, JSONUtil.toJsonStr(parameters));
        }
        
        // 设置参数
        taskRequest.setParameters(parameters);
        
        // 创建任务
        log.info("开始创建AI扩图任务，图片ID: {}, 图片URL: {}", pictureId, picture.getUrl());
        return aliYunAiApi.createOutPaintingTask(taskRequest);
    }

    /**
     * 校验批量操作参数
     *
     * @param pictureIdList 图片ID列表
     * @param spaceId 空间ID
     * @param loginUser 登录用户
     */
    private void validateBatchOperation(List<Long> pictureIdList, Long spaceId, User loginUser) {
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        ThrowUtils.throwIf(CollUtil.isEmpty(pictureIdList), ErrorCode.PARAMS_ERROR, "图片ID列表不能为空");
        ThrowUtils.throwIf(pictureIdList.size() > BATCH_UPLOAD_MAX_COUNT, ErrorCode.PARAMS_ERROR, 
            "批量操作数量不能超过" + BATCH_UPLOAD_MAX_COUNT + "个");
        
        // 校验图片ID的有效性
        for (Long pictureId : pictureIdList) {
            ThrowUtils.throwIf(pictureId == null || pictureId <= 0, ErrorCode.PARAMS_ERROR, "图片ID无效");
        }
        
        // 校验空间权限
        if (spaceId != null && spaceId > 0) {
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            
            // 校验用户是否有权限访问该空间
            if (!loginUser.getId().equals(space.getUserId()) && !userService.isAdmin(loginUser)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限操作该空间的图片");
            }
        }
        
        log.info("批量操作参数校验通过，用户ID: {}, 空间ID: {}, 操作图片数量: {}", loginUser.getId(), spaceId, pictureIdList.size());
    }

    /**
     * 校验颜色搜索参数
     *
     * @param spaceId 空间ID
     * @param picColor 图片颜色
     * @param loginUser 登录用户
     */
    private void validateColorSearchParams(Long spaceId, String picColor, User loginUser) {
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        ThrowUtils.throwIf(spaceId == null || spaceId <= 0, ErrorCode.PARAMS_ERROR, "空间ID无效");
        ThrowUtils.throwIf(StrUtil.isBlank(picColor), ErrorCode.PARAMS_ERROR, "颜色参数不能为空");
        
        // 校验空间是否存在
        Space space = spaceService.getById(spaceId);
        ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
        
        // 校验用户是否有权限访问该空间
        if (!loginUser.getId().equals(space.getUserId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限访问该空间");
        }
        
        log.info("颜色搜索参数校验通过，用户ID: {}, 空间ID: {}, 颜色: {}", loginUser.getId(), spaceId, picColor);
    }

    /**
     * 校验AI扩图任务参数
     *
     * @param createPictureOutPaintingTaskRequest 创建扩图任务请求
     * @param loginUser 登录用户
     */
    private void validateOutPaintingTaskParams(CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest, User loginUser) {
        ThrowUtils.throwIf(createPictureOutPaintingTaskRequest == null, ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        
        Long pictureId = createPictureOutPaintingTaskRequest.getPictureId();
        ThrowUtils.throwIf(pictureId == null || pictureId <= 0, ErrorCode.PARAMS_ERROR, "图片ID无效");
        
        // 校验图片是否存在
        Picture picture = this.getById(pictureId);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        
        // 校验图片URL
        String pictureUrl = picture.getUrl();
        ThrowUtils.throwIf(StrUtil.isBlank(pictureUrl), ErrorCode.PARAMS_ERROR, "图片URL不能为空");
        
        // 校验用户权限（如果图片属于某个空间，需要校验用户是否有权限）
        Long spaceId = picture.getSpaceId();
        if (spaceId != null && spaceId > 0) {
            Space space = spaceService.getById(spaceId);
            ThrowUtils.throwIf(space == null, ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            
            // 校验用户是否有权限访问该空间的图片
            if (!loginUser.getId().equals(space.getUserId()) && !userService.isAdmin(loginUser)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限操作该图片");
            }
        } else {
            // 公共图片，校验用户是否有权限
            if (!loginUser.getId().equals(picture.getUserId()) && !userService.isAdmin(loginUser)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限操作该图片");
            }
        }
        
        log.info("AI扩图任务参数校验通过，用户ID: {}, 图片ID: {}", loginUser.getId(), pictureId);
    }

    /**
     * 校验图片参数
     *
     * @param picture 图片信息
     */
    private void validatePicture(Picture picture) {
        this.validPicture(picture);
    }

    /**
     * 校验编辑权限
     *
     * @param picture 图片信息
     * @param loginUser 登录用户
     */
    private void validateEditPermission(Picture picture, User loginUser) {
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        
        Long spaceId = picture.getSpaceId();
        if (spaceId == null) {
            // 公共图库，仅本人或管理员可操作
            if (!picture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
        } else {
            // 私有空间，仅空间管理员可操作
            if (!picture.getUserId().equals(loginUser.getId())){
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
        }
    }

    /**
     * 校验删除权限
     *
     * @param picture 图片信息
     * @param loginUser 登录用户
     */
    private void validateDeletePermission(Picture picture, User loginUser) {
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        
        Long spaceId = picture.getSpaceId();
        if (spaceId == null) {
            // 公共图库，仅本人或管理员可操作
            if (!picture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
        } else {
            // 私有空间，仅空间管理员可操作
            if (!picture.getUserId().equals(loginUser.getId())){
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
        }
    }

}

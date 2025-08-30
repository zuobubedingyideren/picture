package com.px.picturebackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.px.picturebackend.common.DeleteRequest;
import com.px.picturebackend.exception.BusinessException;
import com.px.picturebackend.exception.ErrorCode;
import com.px.picturebackend.mapper.SpaceMapper;
import com.px.picturebackend.model.dto.space.SpaceAddRequest;
import com.px.picturebackend.model.dto.space.SpaceQueryRequest;
import com.px.picturebackend.model.entity.Space;
import com.px.picturebackend.model.entity.SpaceUser;
import com.px.picturebackend.model.entity.User;
import com.px.picturebackend.model.enums.SpaceLevelEnum;
import com.px.picturebackend.model.enums.SpaceRoleEnum;
import com.px.picturebackend.model.enums.SpaceTypeEnum;
import com.px.picturebackend.model.vo.space.SpaceVO;
import com.px.picturebackend.model.vo.user.UserVO;
import com.px.picturebackend.service.SpaceService;
import com.px.picturebackend.service.SpaceUserService;
import com.px.picturebackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static com.px.picturebackend.constant.SpaceConstants.MAX_SPACE_NAME_LENGTH;
import static com.px.picturebackend.constant.SpaceConstants.SORT_ORDER_ASC;

/**
* @author idpeng
* @description 针对表【space(空间)】的数据库操作Service实现
* @createDate 2025-08-22 11:31:51
*/
@Service
@Slf4j
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space>
    implements SpaceService {

    @Resource
    private UserService userService;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    @Lazy
    private SpaceUserService spaceUserService;

    // 用于用户级别的锁控制，确保线程安全
    private final Map<Long, ReentrantLock> userLockMap = new ConcurrentHashMap<>();
    


    /**
     * 校验空间数据的有效性
     *
     * @param space 空间实体对象
     * @param add   是否为新增操作，true表示新增，false表示更新
     */
    @Override
    public void validSpace(Space space, boolean add) {
        if (space == null) {
            log.error("空间校验失败：空间对象为空");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间对象不能为空");
        }

        try {
            // 校验空间名称
            validateSpaceName(space.getSpaceName(), add);
            
            // 校验空间级别
            validateSpaceLevel(space.getSpaceLevel(), add);
            
            // 校验空间类型
            validateSpaceType(space.getSpaceType(), add);
            
            log.debug("空间数据校验通过，spaceId: {}, add: {}", space.getId(), add);
        } catch (BusinessException e) {
            log.warn("空间数据校验失败，spaceId: {}, add: {}, error: {}", space.getId(), add, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("空间数据校验异常，spaceId: {}, add: {}", space.getId(), add, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "空间数据校验异常");
        }
    }
    
    /**
     * 校验空间名称
     */
    private void validateSpaceName(String spaceName, boolean add) {
        if (add && StrUtil.isBlank(spaceName)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间名称不能为空");
        }
        if (StrUtil.isNotBlank(spaceName) && spaceName.length() > MAX_SPACE_NAME_LENGTH) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间名称长度不能超过" + MAX_SPACE_NAME_LENGTH + "个字符");
        }
    }
    
    /**
     * 校验空间级别
     */
    private void validateSpaceLevel(Integer spaceLevel, boolean add) {
        if (add && spaceLevel == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间级别不能为空");
        }
        if (spaceLevel != null && SpaceLevelEnum.getEnumByValue(spaceLevel) == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间级别不存在");
        }
    }
    
    /**
     * 校验空间类型
     */
    private void validateSpaceType(Integer spaceType, boolean add) {
        if (add && spaceType == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间类型不能为空");
        }
        if (spaceType != null && SpaceTypeEnum.getEnumByValue(spaceType) == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间类型不存在");
        }
    }

    /**
     * 添加空间
     *
     * @param spaceAddRequest 空间添加请求参数
     * @param loginUser       当前登录用户
     * @return 新创建的空间ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addSpace(SpaceAddRequest spaceAddRequest, User loginUser) {
        log.info("开始创建空间，用户ID: {}, 空间类型: {}, 空间级别: {}", 
                loginUser.getId(), spaceAddRequest.getSpaceType(), spaceAddRequest.getSpaceLevel());
        
        try {
            // 1. 构建空间实体对象
            Space space = buildSpaceFromRequest(spaceAddRequest, loginUser);
            
            // 2. 权限校验
            validateCreatePermission(spaceAddRequest, loginUser);
            
            // 3. 使用用户级别锁确保线程安全
            ReentrantLock userLock = getUserLock(loginUser.getId());
            userLock.lock();
            try {
                // 4. 检查空间唯一性约束
                validateSpaceUniqueness(loginUser.getId(), spaceAddRequest.getSpaceType());
                
                // 5. 保存空间数据
                Long spaceId = saveSpaceWithRelations(space, loginUser.getId(), spaceAddRequest.getSpaceType());
                
                log.info("空间创建成功，空间ID: {}, 用户ID: {}, 空间类型: {}", 
                        spaceId, loginUser.getId(), spaceAddRequest.getSpaceType());
                return spaceId;
            } finally {
                userLock.unlock();
                // 清理锁映射，避免内存泄漏
                cleanupUserLock(loginUser.getId());
            }
        } catch (BusinessException e) {
            log.warn("空间创建失败，用户ID: {}, 错误: {}", loginUser.getId(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("空间创建异常，用户ID: {}", loginUser.getId(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "空间创建失败");
        }
    }
    
    /**
     * 从请求构建空间实体对象
     */
    private Space buildSpaceFromRequest(SpaceAddRequest spaceAddRequest, User loginUser) {
        Space space = new Space();
        BeanUtil.copyProperties(spaceAddRequest, space);
        
        // 设置默认值
        if (StrUtil.isBlank(space.getSpaceName())) {
            space.setSpaceName("默认空间");
        }
        if (space.getSpaceLevel() == null) {
            space.setSpaceLevel(SpaceLevelEnum.COMMON.getValue());
        }
        if (space.getSpaceType() == null) {
            space.setSpaceType(SpaceTypeEnum.PRIVATE.getValue());
        }
        
        // 设置用户ID
        space.setUserId(loginUser.getId());
        
        // 根据空间级别填充限额信息
        this.fillSpaceBySpaceLevel(space);
        
        // 数据校验
        this.validSpace(space, true);
        
        return space;
    }
    
    /**
     * 校验创建权限
     */
    private void validateCreatePermission(SpaceAddRequest spaceAddRequest, User loginUser) {
        // 非普通级别空间需要管理员权限
        if (spaceAddRequest.getSpaceLevel() != SpaceLevelEnum.COMMON.getValue() && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限创建指定空间级别");
        }
    }
    
    /**
     * 获取用户级别的锁
     */
    private ReentrantLock getUserLock(Long userId) {
        return userLockMap.computeIfAbsent(userId, k -> new ReentrantLock());
    }
    
    /**
     * 清理用户锁
     */
    private void cleanupUserLock(Long userId) {
        ReentrantLock lock = userLockMap.get(userId);
        if (lock != null && !lock.hasQueuedThreads()) {
            userLockMap.remove(userId);
        }
    }
    
    /**
     * 校验空间唯一性约束
     */
    private void validateSpaceUniqueness(Long userId, Integer spaceType) {
        // 私有空间每个用户只能有一个
        if (SpaceTypeEnum.PRIVATE.getValue() == spaceType) {
            boolean exists = this.lambdaQuery()
                    .eq(Space::getUserId, userId)
                    .eq(Space::getSpaceType, spaceType)
                    .exists();
            if (exists) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "每个用户仅能有一个私有空间");
            }
        }
    }
    
    /**
     * 保存空间及相关数据
     */
    private Long saveSpaceWithRelations(Space space, Long userId, Integer spaceType) {
        // 保存空间
        boolean saveResult = this.save(space);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "空间保存失败");
        }
        
        // 如果是团队空间，创建管理员成员记录
        if (SpaceTypeEnum.TEAM.getValue() == spaceType) {
            createTeamAdminRecord(space.getId(), userId);
        }
        
        return space.getId();
    }
    
    /**
     * 创建团队管理员记录
     */
    private void createTeamAdminRecord(Long spaceId, Long userId) {
        SpaceUser spaceUser = new SpaceUser();
        spaceUser.setSpaceId(spaceId);
        spaceUser.setUserId(userId);
        spaceUser.setSpaceRole(SpaceRoleEnum.ADMIN.getValue());
        
        boolean result = spaceUserService.save(spaceUser);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "创建团队成员记录失败");
        }
    }

    /**
     * 将Space对象转换为SpaceVO对象，并填充关联的用户信息
     *
     * @param space 空间实体对象
     * @param request HTTP请求对象
     * @return 包含用户信息的空间VO对象
     */
    @Override
    @Transactional(readOnly = true)
    public SpaceVO getSpaceVO(Space space, HttpServletRequest request) {
        if (space == null) {
            log.warn("获取空间VO失败，空间实体为空");
            return null;
        }
        
        try {
            SpaceVO spaceVO = SpaceVO.objToVo(space);
            Long userId = space.getUserId();
            
            // 关联用户信息
            if (userId != null && userId > 0) {
                try {
                    User user = userService.getById(userId);
                    if (user != null) {
                        UserVO userVO = userService.getUserVO(user);
                        spaceVO.setUser(userVO);
                    } else {
                        log.warn("空间关联的用户不存在，空间ID: {}, 用户ID: {}", space.getId(), userId);
                    }
                } catch (Exception e) {
                    log.error("获取空间关联用户信息失败，空间ID: {}, 用户ID: {}", space.getId(), userId, e);
                    // 不抛出异常，允许空间信息正常返回
                }
            }
            
            log.debug("成功获取空间VO，空间ID: {}", space.getId());
            return spaceVO;
        } catch (Exception e) {
            log.error("获取空间VO异常，空间ID: {}", space.getId(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取空间信息失败");
        }
    }

    /**
     * 将空间分页数据转换为视图对象分页数据
     *
     * @param spacePage 空间分页数据
     * @param request   HTTP请求对象
     * @return 空间视图对象分页数据
     */
    @Override
    @Transactional(readOnly = true)
    public Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request) {
        if (spacePage == null) {
            log.warn("获取空间VO分页失败，分页数据为空");
            return new Page<>();
        }
        
        List<Space> spaceList = spacePage.getRecords();
        Page<SpaceVO> spaceVOPage = new Page<>(spacePage.getCurrent(), spacePage.getSize(), spacePage.getTotal());
        
        if (CollUtil.isEmpty(spaceList)) {
            log.debug("空间列表为空，返回空分页结果");
            return spaceVOPage;
        }
        
        try {
            // 转换Space对象为SpaceVO对象
            List<SpaceVO> spaceVOList = spaceList.stream().map(SpaceVO::objToVo).toList();

            // 提取所有用户ID并获取对应的用户信息
            Set<Long> userIdSet = spaceList.stream()
                    .map(Space::getUserId)
                    .filter(userId -> userId != null && userId > 0)
                    .collect(Collectors.toSet());
            
            Map<Long, UserVO> userVOMap = batchGetUserVOMap(userIdSet);

            // 为每个SpaceVO设置用户信息
            spaceVOList.forEach(spaceVO -> {
                Long userId = spaceVO.getUserId();
                if (userId != null && userId > 0) {
                    UserVO userVO = userVOMap.get(userId);
                    if (userVO != null) {
                        spaceVO.setUser(userVO);
                    } else {
                        log.warn("用户映射表中未找到用户信息，空间ID: {}, 用户ID: {}", spaceVO.getId(), userId);
                    }
                }
            });
            
            spaceVOPage.setRecords(spaceVOList);
            log.debug("成功获取空间VO分页，总数: {}, 当前页: {}", spacePage.getTotal(), spacePage.getCurrent());
            return spaceVOPage;
        } catch (Exception e) {
            log.error("获取空间VO分页异常", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "获取空间列表失败");
        }
    }
    
    /**
     * 批量获取用户VO映射表
     */
    private Map<Long, UserVO> batchGetUserVOMap(Set<Long> userIds) {
        if (CollUtil.isEmpty(userIds)) {
            return new ConcurrentHashMap<>();
        }
        
        try {
            List<User> users = userService.listByIds(userIds);
            return users.stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toConcurrentMap(
                            User::getId,
                            userService::getUserVO,
                            (existing, replacement) -> existing // 处理重复key
                    ));
        } catch (Exception e) {
            log.error("批量获取用户信息失败，用户ID列表: {}", userIds, e);
            return new ConcurrentHashMap<>();
        }
    }

    /**
     * 根据空间查询请求构建查询条件
     *
     * @param spaceQueryRequest 空间查询请求参数
     * @return 查询包装对象
     */
    @Override
    public LambdaQueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest) {
        LambdaQueryWrapper<Space> spaceQueryWrapper = new LambdaQueryWrapper<>();
        
        if (spaceQueryRequest == null) {
            log.debug("查询请求参数为空，返回空查询条件");
            return spaceQueryWrapper;
        }
        
        try {
            // 从对象中取值
            Long id = spaceQueryRequest.getId();
            Long userId = spaceQueryRequest.getUserId();
            String spaceName = spaceQueryRequest.getSpaceName();
            Integer spaceLevel = spaceQueryRequest.getSpaceLevel();
            Integer spaceType = spaceQueryRequest.getSpaceType();
            int current = spaceQueryRequest.getCurrent();
            int pageSize = spaceQueryRequest.getPageSize();
            String sortField = spaceQueryRequest.getSortField();
            String sortOrder = spaceQueryRequest.getSortOrder();

            // 构建查询条件
            buildQueryConditions(spaceQueryWrapper, id, userId, spaceName, spaceLevel, spaceType);
            
            // 构建排序条件
            buildSortConditions(spaceQueryWrapper, sortField, sortOrder);
            
            log.debug("成功构建查询条件，查询参数: {}", spaceQueryRequest);
            return spaceQueryWrapper;
        } catch (Exception e) {
            log.error("构建查询条件异常，查询参数: {}", spaceQueryRequest, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "构建查询条件失败");
        }
    }
    
    /**
     * 构建查询条件
     */
    private void buildQueryConditions(LambdaQueryWrapper<Space> queryWrapper, Long id, Long userId,
                                      String spaceName, Integer spaceLevel, Integer spaceType) {
        // 精确匹配条件
        queryWrapper.eq(ObjectUtil.isNotEmpty(id), Space::getId, id);
        queryWrapper.eq(ObjectUtil.isNotEmpty(userId), Space::getUserId, userId);
        queryWrapper.eq(ObjectUtil.isNotEmpty(spaceLevel), Space::getSpaceLevel, spaceLevel);
        queryWrapper.eq(ObjectUtil.isNotEmpty(spaceType), Space::getSpaceType, spaceType);
        
        // 模糊匹配条件（空间名称）
        if (StrUtil.isNotBlank(spaceName)) {
            // 防止SQL注入，限制空间名称长度
            if (spaceName.length() > MAX_SPACE_NAME_LENGTH) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间名称查询条件过长");
            }
            queryWrapper.like(Space::getSpaceName, spaceName);
        }
    }
    
    /**
     * 构建排序条件
     */
    private void buildSortConditions(LambdaQueryWrapper<Space> queryWrapper, String sortField, String sortOrder) {
        // 默认排序
        if (StrUtil.isBlank(sortField)) {
            queryWrapper.orderByDesc(Space::getCreateTime);
            return;
        }
        
        // 校验排序字段安全性
        if (!isValidSortField(sortField)) {
            log.warn("无效的排序字段: {}", sortField);
            queryWrapper.orderByDesc(Space::getCreateTime);
            return;
        }
        
        // 校验排序方向
        boolean isAsc = SORT_ORDER_ASC.equals(sortOrder);
        
        // 根据字段名进行排序，使用方法引用避免SQL注入
        switch (sortField) {
            case "id":
                queryWrapper.orderBy(true, isAsc, Space::getId);
                break;
            case "spaceName":
                queryWrapper.orderBy(true, isAsc, Space::getSpaceName);
                break;
            case "spaceLevel":
                queryWrapper.orderBy(true, isAsc, Space::getSpaceLevel);
                break;
            case "spaceType":
                queryWrapper.orderBy(true, isAsc, Space::getSpaceType);
                break;
            case "createTime":
                queryWrapper.orderBy(true, isAsc, Space::getCreateTime);
                break;
            case "updateTime":
                queryWrapper.orderBy(true, isAsc, Space::getUpdateTime);
                break;
            default:
                // 默认按创建时间倒序排列
                log.warn("未知的排序字段: {}，使用默认排序", sortField);
                queryWrapper.orderByDesc(Space::getCreateTime);
                break;
        }
    }
    
    /**
     * 校验排序字段是否安全
     */
    private boolean isValidSortField(String sortField) {
        // 定义允许的排序字段
        Set<String> validFields = Set.of("id", "spaceName", "spaceLevel", "spaceType", "createTime", "updateTime");
        return validFields.contains(sortField);
    }

    /**
     * 根据空间级别自动填充空间的限额信息
     * 如果空间的最大大小或最大数量为null，则根据空间级别的枚举值进行自动填充
     *
     * @param space 空间实体对象
     */
    @Override
    public void fillSpaceBySpaceLevel(Space space) {
        // 根据空间级别，自动填充限额信息
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(space.getSpaceLevel());
        if (spaceLevelEnum != null) {
            long maxSize = spaceLevelEnum.getMaxSize();
            if (space.getMaxSize() == null) {
                space.setMaxSize(maxSize);
            }
            long maxCount = spaceLevelEnum.getMaxCount();
            if (space.getMaxCount() == null) {
                space.setMaxCount(maxCount);
            }
        }
    }

    /**
     * 检查用户对空间的访问权限
     * 
     * @param loginUser 当前登录用户
     * @param space 待检查的空间
     */
    @Override
    public void checkSpaceAuth(User loginUser, Space space) {
        if (loginUser == null) {
            log.warn("权限校验失败，用户未登录");
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        }
        
        if (space == null) {
            log.warn("权限校验失败，空间信息为空，用户ID: {}", loginUser.getId());
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间信息不存在");
        }
        
        try {
            Long spaceUserId = space.getUserId();
            Long loginUserId = loginUser.getId();
            
            // 检查是否为空间所有者或管理员
            boolean isOwner = spaceUserId != null && spaceUserId.equals(loginUserId);
            boolean isAdmin = userService.isAdmin(loginUser);
            
            if (!isOwner && !isAdmin) {
                log.warn("权限校验失败，用户ID: {}, 空间ID: {}, 空间所有者ID: {}", 
                        loginUserId, space.getId(), spaceUserId);
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限访问该空间");
            }
            
            log.debug("权限校验通过，用户ID: {}, 空间ID: {}, 是否所有者: {}, 是否管理员: {}", 
                    loginUserId, space.getId(), isOwner, isAdmin);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("权限校验异常，用户ID: {}, 空间ID: {}", loginUser.getId(), space.getId(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "权限校验失败");
        }
    }

    /**
     * 删除空间
     *
     * @param deleteRequest 删除请求
     * @param loginUser     当前登录用户
     * @return 是否删除成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteSpace(DeleteRequest deleteRequest, User loginUser) {
        if (deleteRequest == null) {
            log.warn("删除空间失败，删除请求为空，用户ID: {}", loginUser != null ? loginUser.getId() : null);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "删除请求不能为空");
        }
        
        Long spaceId = deleteRequest.getId();
        if (spaceId == null || spaceId <= 0) {
            log.warn("删除空间失败，空间ID无效: {}, 用户ID: {}", spaceId, loginUser.getId());
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间ID无效");
        }
        
        log.info("开始删除空间，空间ID: {}, 用户ID: {}", spaceId, loginUser.getId());
        
        try {
            // 1. 检查空间是否存在
            Space existingSpace = this.getById(spaceId);
            if (existingSpace == null) {
                log.warn("删除空间失败，空间不存在，空间ID: {}, 用户ID: {}", spaceId, loginUser.getId());
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "空间不存在");
            }
            
            // 2. 权限校验
            this.checkSpaceAuth(loginUser, existingSpace);
            
            // 3. 检查空间是否可以删除（业务规则）
            validateSpaceDeletion(existingSpace);
            
            // 4. 执行删除操作
            boolean deleteResult = performSpaceDeletion(spaceId, existingSpace);
            
            if (deleteResult) {
                log.info("空间删除成功，空间ID: {}, 空间名称: {}, 用户ID: {}", 
                        spaceId, existingSpace.getSpaceName(), loginUser.getId());
                return true;
            } else {
                log.error("空间删除失败，数据库操作失败，空间ID: {}, 用户ID: {}", spaceId, loginUser.getId());
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "删除空间失败");
            }
        } catch (BusinessException e) {
            log.warn("删除空间业务异常，空间ID: {}, 用户ID: {}, 错误: {}", spaceId, loginUser.getId(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("删除空间系统异常，空间ID: {}, 用户ID: {}", spaceId, loginUser.getId(), e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除空间失败");
        }
    }
    
    /**
     * 校验空间是否可以删除
     */
    private void validateSpaceDeletion(Space space) {
        // 可以在这里添加业务规则，比如检查空间是否有关联数据
        // 例如：检查空间下是否还有图片等资源
        
        // 示例：如果是团队空间，检查是否还有其他成员
        if (SpaceTypeEnum.TEAM.getValue() == (space.getSpaceType())) {
            // 这里可以添加检查团队成员的逻辑
            log.debug("校验团队空间删除条件，空间ID: {}", space.getId());
        }
    }
    
    /**
     * 执行空间删除操作
     */
    private boolean performSpaceDeletion(Long spaceId, Space space) {
        try {
            // 如果是团队空间，需要先删除相关的团队成员记录
            if (SpaceTypeEnum.TEAM.getValue() == (space.getSpaceType())) {
                deleteTeamMembers(spaceId);
            }
            
            // 删除空间记录
            boolean result = this.removeById(spaceId);
            
            if (result) {
                log.debug("空间数据删除成功，空间ID: {}", spaceId);
            } else {
                log.error("空间数据删除失败，空间ID: {}", spaceId);
            }
            
            return result;
        } catch (Exception e) {
            log.error("执行空间删除操作异常，空间ID: {}", spaceId, e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "删除空间操作失败");
        }
    }
    
    /**
     * 删除团队成员记录
     */
    private void deleteTeamMembers(Long spaceId) {
        try {
            // 删除团队空间的所有成员记录
            QueryWrapper<SpaceUser> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("spaceId", spaceId);
            boolean result = spaceUserService.remove(queryWrapper);
            
            if (result) {
                log.debug("团队成员记录删除成功，空间ID: {}", spaceId);
            } else {
                log.warn("团队成员记录删除失败或无成员记录，空间ID: {}", spaceId);
            }
        } catch (Exception e) {
            log.error("删除团队成员记录异常，空间ID: {}", spaceId, e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "删除团队成员记录失败");
        }
    }
}

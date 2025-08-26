package com.yupi.yupicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.yupicturebackend.api.aliyunai.model.CreateOutPaintingTaskResponse;
import com.yupi.yupicturebackend.model.dto.picture.*;
import com.yupi.yupicturebackend.model.entity.Picture;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.yupicturebackend.model.entity.Space;
import com.yupi.yupicturebackend.model.entity.User;
import com.yupi.yupicturebackend.model.vo.picture.PictureVO;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author idpeng
* @description 针对表【picture(图片)】的数据库操作Service
* @createDate 2025-07-26 11:28:06
*/
public interface PictureService extends IService<Picture> {

    /**
     * 校验图片
     *
     * @param picture
     */
    void validPicture(Picture picture);

    /**
     * 上传图片
     *
     * @param inputSource       用户上传的图片文件或者文件地址
     * @param pictureUploadRequest 图片上传请求参数（包含图片ID等信息）
     * @param loginUser            当前登录用户信息
     * @return 返回上传后的图片信息封装类
     */
    PictureVO uploadPicture(Object inputSource,
                            PictureUploadRequest pictureUploadRequest,
                            User loginUser);

    /**
     * 获取图片查询条件
     *
     * @param pictureQueryRequest 图片查询请求参数
     * @return 返回封装好的查询条件对象
     */
    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

    /**
     * 获取图片的VO对象
     *
     * @param picture 图片实体对象
     * @param request HTTP请求对象，用于获取上下文信息
     * @return 返回封装好的图片VO对象
     */
    PictureVO getPictureVO(Picture picture, HttpServletRequest request);

    /**
     * 获取图片的VO分页对象
     *
     * @param picturePage 图片分页数据
     * @param request     HTTP请求对象，用于获取上下文信息
     * @return 返回封装好的图片VO分页对象
     */
    Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request);

    /**
     * 图片审核
     *
     * @param pictureReviewRequest 图片审核请求
     * @param loginUser            当前登录的用户
     */
    void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser);

    /**
     * 填充审核参数，方便其他方法使用
     * @param picture 图片
     * @param loginUser 登陆用户
     */
    void fillReviewParams(Picture picture, User loginUser);

    /**
     * 批量抓取并上传图片
     *
     * @param pictureUploadByBatchRequest 批量抓取图片的请求参数，包含搜索词和抓取数量
     * @param loginUser                   当前登录的用户信息
     * @return 返回成功抓取并上传的图片数量
     */
    Integer uploadPictureByBatch(
            PictureUploadByBatchRequest pictureUploadByBatchRequest,
            User loginUser
    );


    /**
     * 清除图片文件
     *
     * @param oldPicture 旧的图片对象，包含需要被删除的图片信息
     */
    void clearPictureFile(Picture oldPicture);

    /**
     * 检查用户对图片的权限
     *
     * @param loginUser 当前登录的用户信息
     * @param picture   需要检查权限的图片对象
     */
    @Deprecated
    void checkPictureAuth(User loginUser, Picture picture);

    /**
     * 删除图片
     *
     * @param pictureId  图片ID
     * @param loginUser  当前登录的用户信息
     */
    void deletePicture(long pictureId, User loginUser);

    /**
     * 编辑图片信息
     *
     * @param pictureEditRequest 图片编辑请求参数，包含需要修改的图片信息
     * @param loginUser          当前登录的用户信息
     */
    void editPicture(PictureEditRequest pictureEditRequest, User loginUser);

    /**
     * 根据颜色搜索图片
     *
     * @param spaceId   空间ID，用于限定搜索范围
     * @param picColor  图片颜色，用于匹配图片的主要颜色
     * @param loginUser 当前登录的用户信息
     * @return 返回符合颜色条件的图片VO列表
     */
    List<PictureVO> searchPictureByColor(Long spaceId, String picColor, User loginUser);

    /**
     * 批量编辑图片信息
     *
     * @param pictureEditByBatchRequest 图片批量编辑请求参数，包含需要修改的图片ID列表、空间ID、分类、标签列表和命名规则等信息
     * @param loginUser                 当前登录的用户信息
     */
    @Transactional(rollbackFor = Exception.class)
    void editPictureByBatch(PictureEditByBatchRequest pictureEditByBatchRequest, User loginUser);

    /**
     * 创建图片外延绘制任务
     *
     * @param createPictureOutPaintingTaskRequest 创建图片外延绘制任务的请求参数，包含原始图片ID、绘制方向、绘制内容等信息
     * @param loginUser                           当前登录的用户信息
     * @return 返回创建的外延绘制任务响应结果，包含任务ID等信息
     */
    CreateOutPaintingTaskResponse createPictureOutPaintingTask(CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest, User loginUser);


}

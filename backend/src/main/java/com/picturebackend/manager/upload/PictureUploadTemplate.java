package com.picturebackend.manager.upload;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.CIObject;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.qcloud.cos.model.ciModel.persistence.ProcessResults;
import com.picturebackend.config.CosClientConfig;
import com.picturebackend.exception.BusinessException;
import com.picturebackend.exception.ErrorCode;
import com.picturebackend.manager.CosManager;
import com.picturebackend.model.dto.file.UploadPictureResult;
import lombok.extern.slf4j.Slf4j;


import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * packageName: com.picturebackend.manager.upload
 *
 * @author: idpeng
 * @version: 1.0
 * @className: PictureUploadTemplate
 * @date: 2025/8/20 10:32
 * @description: 图片上传的抽象类
 */
@Slf4j
public abstract class PictureUploadTemplate {

    @Resource
    protected CosManager cosManager;

    @Resource
    protected CosClientConfig cosClientConfig;

    /**
     * 上传图片的模板方�?
     *
     * @param inputSource      输入源，可以是MultipartFile或其他类型的文件对象
     * @param uploadPathPrefix 上传路径前缀
     * @return 上传结果，包含图片信息和访问URL
     */
    public UploadPictureResult uploadPicture(Object inputSource, String uploadPathPrefix) {
        // 校验图片
        validPicture(inputSource);

        // 图片上传地址
        String uuid = RandomUtil.randomString(16);
        String originalFilename = getOriginalFilename(inputSource);
        String uploadFileName = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, FileUtil.getSuffix(originalFilename));
        String uploadPath = String.format("/%s/%s", uploadPathPrefix, uploadFileName);
        File file = null;
        try {
            // 创建临时文件
            file = File.createTempFile(uploadPath, null);
            processFile(inputSource, file);

            // 上传文件
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath, file);
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            ProcessResults processResults = putObjectResult.getCiUploadResult().getProcessResults();
            List<CIObject> objectList = processResults.getObjectList();
            if (CollUtil.isNotEmpty(objectList)) {
                CIObject compressedCiObject = objectList.get(0);
                CIObject thumbnailCiObject = compressedCiObject;
                if (objectList.size() > 1) {
                    thumbnailCiObject = objectList.get(1);
                }
                return buildResult(originalFilename, compressedCiObject, thumbnailCiObject, imageInfo);
            }
            // 封装返回结果
            return buildResult(imageInfo, originalFilename, file, uploadPath);
        } catch (IOException e) {
            log.error("图片上传到对象存储失�?, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            this.deleteTempFile(file);
        }

    }

    private UploadPictureResult buildResult(String originalFilename, CIObject compressedCiObject, CIObject thumbnailCiObject, ImageInfo imageInfo) {
        UploadPictureResult uploadPictureResult = new UploadPictureResult();
        int picWidth = compressedCiObject.getWidth();
        int picHeight = compressedCiObject.getHeight();
        double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();
        uploadPictureResult.setPicName(FileUtil.mainName(originalFilename));
        uploadPictureResult.setPicColor(imageInfo.getAve());
        uploadPictureResult.setPicWidth(picWidth);
        uploadPictureResult.setPicHeight(picHeight);
        uploadPictureResult.setPicScale(picScale);
        uploadPictureResult.setPicFormat(compressedCiObject.getFormat());
        uploadPictureResult.setPicSize(compressedCiObject.getSize().longValue());
        // 设置图片为压缩后的地址
        uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + compressedCiObject.getKey());
        uploadPictureResult.setThumbnailUrl(cosClientConfig.getHost() + "/" + thumbnailCiObject.getKey());
        return uploadPictureResult;
    }

    /**
     * 处理输入源并将其写入到指定的临时文件�?
     *
     * @param inputSource 输入源，可以是MultipartFile或其他类型的文件对象
     * @param file        目标临时文件
     * @throws IOException 如果在处理文件时发生IO异常
     */
    protected abstract void processFile(Object inputSource, File file) throws IOException;

    /**
     * 获取原始文件�?
     *
     * @param inputSource 输入源，可以是MultipartFile或其他类型的文件对象
     * @return 原始文件�?
     */
    protected abstract String getOriginalFilename(Object inputSource);

    /**
     * 校验图片的有效�?
     *
     * @param inputSource 输入源，可以是MultipartFile或其他类型的文件对象
     * @throws BusinessException 如果图片不合法则抛出业务异常
     */
    protected abstract void validPicture(Object inputSource);

    /**
     * 删除临时文件
     *
     * @param file 要删除的文件对象
     */
    public void deleteTempFile(File file) {
        if (file == null) {
            return;
        }

        // 删除临时文件
        boolean deleteResult = file.delete();
        if (!deleteResult) {
            log.error("file delete error, filepath = {}", file.getAbsolutePath());
        }
    }

    /**
     * 构造图片上传结果对�?
     *
     * @param imageInfo        图片信息对象，包含宽度、高度等信息
     * @param originalFilename 原始文件�?
     * @param file             上传的文件对�?
     * @param uploadPath       上传路径
     * @return 包含图片详细信息的上传结果对�?
     */
    private UploadPictureResult buildResult(ImageInfo imageInfo, String originalFilename, File file, String uploadPath) {
        UploadPictureResult uploadPictureResult = new UploadPictureResult();
        int picWidth = imageInfo.getWidth();
        int picHeight = imageInfo.getHeight();
        double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();
        uploadPictureResult.setPicName(FileUtil.mainName(originalFilename));
        uploadPictureResult.setPicColor(imageInfo.getAve());
        uploadPictureResult.setPicWidth(picWidth);
        uploadPictureResult.setPicHeight(picHeight);
        uploadPictureResult.setPicScale(picScale);
        uploadPictureResult.setPicFormat(imageInfo.getFormat());
        uploadPictureResult.setPicSize(FileUtil.size(file));
        uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + uploadPath);
        return uploadPictureResult;
    }
}

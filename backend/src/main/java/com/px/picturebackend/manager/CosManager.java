package com.px.picturebackend.manager;

import cn.hutool.core.io.FileUtil;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.StorageClass;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import com.qcloud.cos.transfer.TransferManager;
import com.qcloud.cos.transfer.Upload;
import com.qcloud.cos.model.UploadResult;
import com.px.picturebackend.config.CosClientConfig;
import com.px.picturebackend.exception.BusinessException;
import com.px.picturebackend.exception.ErrorCode;
import com.px.picturebackend.utils.FileValidationUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

/**
 * packageName: com.px.picturebackend.manager
 *
 * @author: idpeng
 * @version: 1.0
 * @className: CosManager
 * @date: 2025/7/26 10:54
 * @description: 通用的对象存储操作类，支持智能上传和大文件分块上传
 */
@Component
@Slf4j
public class CosManager {
    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private COSClient cosClient;
    
    @Resource
    private TransferManager transferManager;
    
    // 文件大小阈值：10MB
    private static final long FILE_SIZE_THRESHOLD = 10 * 1024 * 1024L;

    /**
     * 上传对象
     *
     * @param key  唯一标识
     * @param file 文件
     */
    public PutObjectResult putObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key,
                file);
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 上传图片对象到COS
     *
     * @param key  对象的唯一标识
     * @param file 要上传的文件
     * @return 上传结果
     */
    public PutObjectResult putPictureObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);

        // 对图片进行处理（获取基本信息也被视为一种处理）
        PicOperations picOperations = new PicOperations();

        // 1 表示返回原图信息
        picOperations.setIsPicInfo(1);
        ArrayList<PicOperations.Rule> rules = new ArrayList<>();

        // 图片压缩（转成webp格式）
        String webpKey = FileUtil.mainName(key) + ".webp";
        PicOperations.Rule compressRule = new PicOperations.Rule();
        compressRule.setRule("imageMogr2/format/webp");
        compressRule.setBucket(cosClientConfig.getBucket());
        compressRule.setFileId(webpKey);
        rules.add(compressRule);
        // 缩略图处理
        // 缩略图处理，仅对 > 20 KB 的图片生成缩略图
        if (file.length() > 2 * 1024) {
            PicOperations.Rule thumbnailRule = new PicOperations.Rule();
            thumbnailRule.setBucket(cosClientConfig.getBucket());
            String thumbnailKey = FileUtil.mainName(key) + "_thumbnail." + FileUtil.getSuffix(key);
            thumbnailRule.setFileId(thumbnailKey);
            thumbnailRule.setRule(String.format("imageMogr2/thumbnail/%sx%s>", 256, 256));
            rules.add(thumbnailRule);
        }
        // 构造处理参数
        picOperations.setRules(rules);
        putObjectRequest.setPicOperations(picOperations);
        return cosClientConfig.cosClient().putObject(putObjectRequest);
    }

    /**
     * 下载对象
     *
     * @param key 唯一标识
     */
    public COSObject getObject(String key) {
        GetObjectRequest getObjectRequest = new GetObjectRequest(cosClientConfig.getBucket(), key);
        return cosClient.getObject(getObjectRequest);
    }

    /**
     * 删除COS中的对象
     *
     * @param key 对象的键
     * @throws CosClientException 当删除操作失败时抛出异常
     */
    public void deleteObject(String key) throws CosClientException {
        cosClient.deleteObject(cosClientConfig.getBucket(), key);
    }
    
    /**
     * 智能上传方法 - 根据文件大小自动选择上传方式
     * 小于10MB使用普通上传，大于等于10MB使用分块上传
     *
     * @param key 对象的唯一标识
     * @param file 要上传的文件
     * @return 上传结果
     * @throws BusinessException 当文件大小超过限制时抛出
     */
    @Retryable(value = {Exception.class}, maxAttempts = 3)
    public PutObjectResult putObjectSmart(String key, File file) throws BusinessException {
        return putObjectSmart(key, file, null, null);
    }
    
    /**
     * 智能上传方法 - 支持存储类型和自定义Headers
     *
     * @param key 对象的唯一标识
     * @param file 要上传的文件
     * @param storageClass 存储类型（可选）
     * @param customHeaders 自定义Headers（可选）
     * @return 上传结果
     * @throws BusinessException 当文件大小超过限制时抛出
     */
    @Retryable(value = {Exception.class}, maxAttempts = 3)
    public PutObjectResult putObjectSmart(String key, File file, StorageClass storageClass, Map<String, String> customHeaders) throws BusinessException {
        log.info("开始智能上传文件: key={}, fileSize={}", key, file.length());
        
        // 验证文件大小
        FileValidationUtil.validateFileSize(file);
        
        try {
            if (file.length() >= FILE_SIZE_THRESHOLD) {
                // 大文件使用分块上传
                log.info("文件大小超过阈值，使用分块上传: {}", FileValidationUtil.formatFileSize(file.length()));
                return putLargeObject(key, file, storageClass, customHeaders);
            } else {
                // 小文件使用普通上传
                log.info("文件大小未超过阈值，使用普通上传: {}", FileValidationUtil.formatFileSize(file.length()));
                PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
                
                // 设置存储类型
                if (storageClass != null) {
                    putObjectRequest.setStorageClass(storageClass);
                }
                
                // 设置自定义Headers
                if (customHeaders != null && !customHeaders.isEmpty()) {
                    customHeaders.forEach((headerKey, headerValue) -> 
                        putObjectRequest.putCustomRequestHeader(headerKey, headerValue));
                }
                
                PutObjectResult result = cosClient.putObject(putObjectRequest);
                log.info("普通上传完成: key={}, etag={}", key, result.getETag());
                return result;
            }
        } catch (Exception e) {
            log.error("智能上传失败: key={}, error={}", key, e.getMessage(), e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "文件上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 智能上传方法 - 支持MultipartFile
     *
     * @param key 对象的唯一标识
     * @param multipartFile 要上传的MultipartFile
     * @return 上传结果
     * @throws BusinessException 当文件大小超过限制时抛出
     */
    @Retryable(value = {Exception.class}, maxAttempts = 3)
    public PutObjectResult putObjectSmart(String key, MultipartFile multipartFile) throws BusinessException {
        return putObjectSmart(key, multipartFile, null, null);
    }
    
    /**
     * 智能上传方法 - 支持MultipartFile、存储类型和自定义Headers
     *
     * @param key 对象的唯一标识
     * @param multipartFile 要上传的MultipartFile
     * @param storageClass 存储类型（可选）
     * @param customHeaders 自定义Headers（可选）
     * @return 上传结果
     * @throws BusinessException 当文件大小超过限制时抛出
     */
    @Retryable(value = {Exception.class}, maxAttempts = 3)
    public PutObjectResult putObjectSmart(String key, MultipartFile multipartFile, StorageClass storageClass, Map<String, String> customHeaders) throws BusinessException {
        log.info("开始智能上传MultipartFile: key={}, fileSize={}", key, multipartFile.getSize());
        
        // 验证文件大小
        FileValidationUtil.validateFileSize(multipartFile);
        
        // 将MultipartFile转换为临时文件
        File tempFile = null;
        try {
            tempFile = File.createTempFile("upload_", "_" + multipartFile.getOriginalFilename());
            multipartFile.transferTo(tempFile);
            
            return putObjectSmart(key, tempFile, storageClass, customHeaders);
        } catch (IOException e) {
            log.error("MultipartFile转换失败: key={}, error={}", key, e.getMessage(), e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "文件处理失败: " + e.getMessage());
        } finally {
            // 清理临时文件
            if (tempFile != null && tempFile.exists()) {
                boolean deleted = tempFile.delete();
                if (!deleted) {
                    log.warn("临时文件删除失败: {}", tempFile.getAbsolutePath());
                }
            }
        }
    }
    
    /**
     * 大文件上传方法 - 使用TransferManager进行分块上传
     *
     * @param key 对象的唯一标识
     * @param file 要上传的文件
     * @return 上传结果
     * @throws BusinessException 当上传失败时抛出
     */
    @Retryable(value = {Exception.class}, maxAttempts = 3)
    public PutObjectResult putLargeObject(String key, File file) throws BusinessException {
        return putLargeObject(key, file, null, null);
    }
    
    /**
     * 大文件上传方法 - 支持存储类型和自定义Headers
     *
     * @param key 对象的唯一标识
     * @param file 要上传的文件
     * @param storageClass 存储类型（可选）
     * @param customHeaders 自定义Headers（可选）
     * @return 上传结果
     * @throws BusinessException 当上传失败时抛出
     */
    @Retryable(value = {Exception.class}, maxAttempts = 3)
    public PutObjectResult putLargeObject(String key, File file, StorageClass storageClass, Map<String, String> customHeaders) throws BusinessException {
        log.info("开始大文件分块上传: key={}, fileSize={}", key, FileValidationUtil.formatFileSize(file.length()));
        
        try {
            PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
            
            // 设置存储类型
            if (storageClass != null) {
                putObjectRequest.setStorageClass(storageClass);
            }
            
            // 设置自定义Headers
            if (customHeaders != null && !customHeaders.isEmpty()) {
                customHeaders.forEach((headerKey, headerValue) -> 
                    putObjectRequest.putCustomRequestHeader(headerKey, headerValue));
            }
            
            // 使用TransferManager进行分块上传
            Upload upload = transferManager.upload(putObjectRequest);
            
            // 等待上传完成
            UploadResult uploadResult = upload.waitForUploadResult();
            log.info("大文件分块上传完成: key={}, etag={}", key, uploadResult.getETag());
            
            // 转换为PutObjectResult
            PutObjectResult result = new PutObjectResult();
            result.setETag(uploadResult.getETag());
            result.setVersionId(uploadResult.getVersionId());
            
            return result;
        } catch (Exception e) {
            log.error("大文件上传失败: key={}, error={}", key, e.getMessage(), e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "大文件上传失败: " + e.getMessage());
        }
    }

}

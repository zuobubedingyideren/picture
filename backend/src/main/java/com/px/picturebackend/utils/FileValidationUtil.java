package com.px.picturebackend.utils;

import com.px.picturebackend.exception.BusinessException;
import com.px.picturebackend.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * packageName: com.px.picturebackend.utils
 *
 * @author: idpeng
 * @version: 1.0
 * @className: FileValidationUtil
 * @date: 2025/1/18
 * @description: 文件验证工具类，提供统一的文件大小验证功能
 */
@Slf4j
public class FileValidationUtil {
    
    /**
     * 默认最大文件大小限制：10MB
     */
    public static final long DEFAULT_MAX_FILE_SIZE = 10 * 1024 * 1024L;
    
    /**
     * 验证文件大小是否超过默认限制（10MB）
     * 
     * @param file 要验证的文件
     * @throws BusinessException 当文件大小超过限制时抛出
     */
    public static void validateFileSize(MultipartFile file) {
        validateFileSize(file, DEFAULT_MAX_FILE_SIZE);
    }
    
    /**
     * 验证文件大小是否超过指定限制
     * 
     * @param file 要验证的文件
     * @param maxSizeBytes 最大文件大小（字节）
     * @throws BusinessException 当文件大小超过限制时抛出
     */
    public static void validateFileSize(MultipartFile file, long maxSizeBytes) {
        if (file == null) {
            log.warn("文件验证失败：文件为空");
            throw new BusinessException(ErrorCode.NULL_ERROR, "文件不能为空");
        }
        
        long fileSize = file.getSize();
        if (fileSize > maxSizeBytes) {
            long maxSizeMB = maxSizeBytes / (1024 * 1024);
            log.warn("文件大小验证失败：文件大小{}字节，超过限制{}MB", fileSize, maxSizeMB);
            throw new BusinessException(ErrorCode.FILE_SIZE_EXCEEDED, maxSizeMB);
        }
        
        log.debug("文件大小验证通过：文件大小{}字节，限制{}字节", fileSize, maxSizeBytes);
    }
    
    /**
     * 验证本地文件大小是否超过默认限制（10MB）
     * 
     * @param file 要验证的本地文件
     * @throws BusinessException 当文件大小超过限制时抛出
     */
    public static void validateFileSize(File file) {
        validateFileSize(file, DEFAULT_MAX_FILE_SIZE);
    }
    
    /**
     * 验证本地文件大小是否超过指定限制
     * 
     * @param file 要验证的本地文件
     * @param maxSizeBytes 最大文件大小（字节）
     * @throws BusinessException 当文件大小超过限制时抛出
     */
    public static void validateFileSize(File file, long maxSizeBytes) {
        if (file == null || !file.exists()) {
            log.warn("文件验证失败：文件不存在");
            throw new BusinessException(ErrorCode.NULL_ERROR, "文件不存在");
        }
        
        long fileSize = file.length();
        if (fileSize > maxSizeBytes) {
            long maxSizeMB = maxSizeBytes / (1024 * 1024);
            log.warn("文件大小验证失败：文件大小{}字节，超过限制{}MB", fileSize, maxSizeMB);
            throw new BusinessException(ErrorCode.FILE_SIZE_EXCEEDED, maxSizeMB);
        }
        
        log.debug("文件大小验证通过：文件大小{}字节，限制{}字节", fileSize, maxSizeBytes);
    }
    
    /**
     * 验证文件大小是否超过指定限制（以MB为单位）
     * 
     * @param file 要验证的文件
     * @param maxSizeMB 最大文件大小（MB）
     * @throws BusinessException 当文件大小超过限制时抛出
     */
    public static void validateFileSizeInMB(MultipartFile file, long maxSizeMB) {
        validateFileSize(file, maxSizeMB * 1024 * 1024);
    }
    
    /**
     * 验证本地文件大小是否超过指定限制（以MB为单位）
     * 
     * @param file 要验证的本地文件
     * @param maxSizeMB 最大文件大小（MB）
     * @throws BusinessException 当文件大小超过限制时抛出
     */
    public static void validateFileSizeInMB(File file, long maxSizeMB) {
        validateFileSize(file, maxSizeMB * 1024 * 1024);
    }
    
    /**
     * 获取文件大小的可读格式
     * 
     * @param sizeBytes 文件大小（字节）
     * @return 格式化的文件大小字符串
     */
    public static String formatFileSize(long sizeBytes) {
        if (sizeBytes < 1024) {
            return sizeBytes + " B";
        } else if (sizeBytes < 1024 * 1024) {
            return String.format("%.2f KB", sizeBytes / 1024.0);
        } else if (sizeBytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", sizeBytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.2f GB", sizeBytes / (1024.0 * 1024.0 * 1024.0));
        }
    }
}
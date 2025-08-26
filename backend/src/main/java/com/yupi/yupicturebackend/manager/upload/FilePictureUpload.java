package com.yupi.yupicturebackend.manager.upload;

import cn.hutool.core.io.FileUtil;
import com.yupi.yupicturebackend.exception.ErrorCode;
import com.yupi.yupicturebackend.exception.ThrowUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * packageName: com.yupi.yupicturebackend.manager.upload
 *
 * @author: idpeng
 * @version: 1.0
 * @className: FilePictureUpload
 * @date: 2025/8/20 10:45
 * @description: 本地图片上传
 */
@Service
public class FilePictureUpload extends PictureUploadTemplate{
    /**
     * 校验上传的图片文件是否符合要求
     *
     * @param inputSource 输入源，应为MultipartFile类型的文件对象
     */
    @Override
    protected void validPicture(Object inputSource) {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR,"文件不能为空");
        // 1. 校验文件大小
        long fileSize = multipartFile.getSize();
        final long ONE_M = 1024 * 1024L;
        ThrowUtils.throwIf(fileSize > 7 * ONE_M, ErrorCode.PARAMS_ERROR, "文件大小不能大于2M");

        // 2. 校验文件后缀
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        // 允许上传的文件后缀
        final List<String> ALLOW_FORMAT_LIST = Arrays.asList("jpeg", "jpg", "png", "webp");
        ThrowUtils.throwIf(!ALLOW_FORMAT_LIST.contains(fileSuffix), ErrorCode.PARAMS_ERROR, "文件类型错误");
    }

    /**
     * 获取原始文件名
     *
     * @param inputSource 输入源，应为MultipartFile类型的文件对象
     * @return 原始文件名
     */
    @Override
    protected String getOriginalFilename(Object inputSource) {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        return multipartFile.getOriginalFilename();
    }

    /**
     * 处理文件，将输入源保存到指定文件中
     *
     * @param inputSource 输入源，应为MultipartFile类型的文件对象
     * @param file        目标文件对象
     * @throws IOException 当文件处理发生错误时抛出
     */
    @Override
    protected void processFile(Object inputSource, File file) throws IOException {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        multipartFile.transferTo(file);
    }
}

package com.yupi.yupicturebackend.manager.upload;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.yupi.yupicturebackend.exception.BusinessException;
import com.yupi.yupicturebackend.exception.ErrorCode;
import com.yupi.yupicturebackend.exception.ThrowUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * packageName: com.yupi.yupicturebackend.manager.upload
 *
 * @author: idpeng
 * @version: 1.0
 * @className: UrlPictureUpload
 * @date: 2025/8/20 10:53
 * @description: URL图片上传
 */
@Service
public class UrlPictureUpload extends PictureUploadTemplate{
    /**
     * 校验URL图片文件是否符合要求
     *
     * @param inputSource 输入源，应为String类型的URL地址
     */
    @Override
    protected void validPicture(Object inputSource) {
        String fileUrl = (String) inputSource;
        ThrowUtils.throwIf(StrUtil.isBlank(fileUrl), ErrorCode.PARAMS_ERROR, "文件地址不能为空");

        try {
            // 1. 验证URL格式
            new URL(fileUrl);
        } catch (MalformedURLException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件地址格式不正确");
        }

        // 2. 校验URL协议
        ThrowUtils.throwIf(!fileUrl.startsWith("http://") && !fileUrl.startsWith("https://"), ErrorCode.PARAMS_ERROR, "仅支持http和https协议的文件地址");
        // 3. 发送HEAD请求验证文件是否存在
        HttpResponse response = null;
        try {
            response = HttpUtil.createRequest(Method.HEAD, fileUrl).execute();
            // 未正常返回，无需执行其他判断
            if (response.getStatus() != HttpStatus.HTTP_OK) {
                return;
            }
            // 4. 校验文件类型
            String contentType = response.header("Content-Type");
            if (StrUtil.isNotBlank(contentType)) {
                // 允许的图片类型
                final List<String> ALLOW_CONTENT_TYPES = Arrays.asList("image/jpeg", "image/jpg", "image/png", "image/webp");
                ThrowUtils.throwIf(!ALLOW_CONTENT_TYPES.contains(contentType.toLowerCase()),
                        ErrorCode.PARAMS_ERROR, "文件类型错误");
            }
            // 5. 校验文件大小
            String contentLengthStr = response.header("Content-Length");
            if (StrUtil.isNotBlank(contentLengthStr)) {
                try {
                    long contentLength = Long.parseLong(contentLengthStr);
                    final long SEVEN_MB = 7 * 1024 * 1024L;
                    ThrowUtils.throwIf(contentLength > SEVEN_MB, ErrorCode.PARAMS_ERROR, "文件不能超过7MB");
                } catch (NumberFormatException e) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小格式错误");
                }
            }
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    /**
     * 从URL中提取文件名，如果URL中没有扩展名则根据Content-Type确定扩展名
     *
     * @param inputSource 输入源，应为String类型的URL地址
     * @return 包含扩展名的文件名
     */
    @Override
    protected String getOriginalFilename(Object inputSource) {
        String fileUrl = (String) inputSource;
        String mainName = FileUtil.mainName(fileUrl);
        String extName = FileUtil.extName(fileUrl);
        
        // 如果URL中没有扩展名，通过Content-Type获取
        if (StrUtil.isBlank(extName)) {
            extName = getExtensionFromContentType(fileUrl);
        }
        
        // 如果主文件名为空，使用默认名称
        if (StrUtil.isBlank(mainName)) {
            mainName = "image";
        }
        
        return mainName + "." + extName;
    }
    
    /**
     * 根据URL的Content-Type头信息获取文件扩展名
     *
     * @param fileUrl 图片文件URL
     * @return 文件扩展名，默认为jpg
     */
    private String getExtensionFromContentType(String fileUrl) {
        HttpResponse response = null;
        try {
            response = HttpUtil.createRequest(Method.HEAD, fileUrl).execute();
            if (response.getStatus() == HttpStatus.HTTP_OK) {
                String contentType = response.header("Content-Type");
                if (StrUtil.isNotBlank(contentType)) {
                    // 根据Content-Type映射到文件扩展名
                    switch (contentType.toLowerCase()) {
                        case "image/jpeg":
                        case "image/jpg":
                            return "jpg";
                        case "image/png":
                            return "png";
                        case "image/webp":
                            return "webp";
                        case "image/gif":
                            return "gif";
                        default:
                            return "jpg"; // 默认扩展名
                    }
                }
            }
        } catch (Exception e) {
            // 如果获取Content-Type失败，使用默认扩展名
            return "jpg";
        } finally {
            if (response != null) {
                response.close();
            }
        }
        return "jpg"; // 默认扩展名
    }

    /**
     * 处理文件，从URL下载文件并保存到指定文件中
     *
     * @param inputSource 输入源，应为String类型的URL地址
     * @param file        目标文件对象
     * @throws IOException 当文件处理发生错误时抛出
     */
    @Override
    protected void processFile(Object inputSource, File file) throws IOException {
        String fileUrl = (String) inputSource;
        HttpUtil.downloadFile(fileUrl, file);
    }
}

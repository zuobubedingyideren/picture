package com.picturebackend.api.aliyunai;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.ContentType;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.picturebackend.api.aliyunai.model.CreateOutPaintingTaskRequest;
import com.picturebackend.api.aliyunai.model.CreateOutPaintingTaskResponse;
import com.picturebackend.api.aliyunai.model.GetOutPaintingTaskResponse;
import com.picturebackend.exception.BusinessException;
import com.picturebackend.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * packageName: com.picturebackend.api.aliyunai
 *
 * @author: idpeng
 * @version: 1.0
 * @className: AliYunAiApi
 * @date: 2025/8/24 16:18
 * @description: API 调用类，通‌过 Hutool �?HTTP 请求工具类来调用阿里云‌百炼的 API
 */
@Slf4j
@Component
public class AliYunAiApi {
    // 读取配置文件
    @Value("${aliYunAi.apiKey}")
    private String apiKey;

    // 创建任务地址 - 使用图像编辑API进行扩图
    public static final String CREATE_OUT_PAINTING_TASK_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/image2image/image-synthesis";

    // 查询任务状�?
    public static final String GET_OUT_PAINTING_TASK_URL = "https://dashscope.aliyuncs.com/api/v1/tasks/%s";

    /**
     * 创建AI扩图任务
     *
     * @param createOutPaintingTaskRequest 扩图任务请求参数
     * @return CreateOutPaintingTaskResponse 扩图任务响应结果
     */
    public CreateOutPaintingTaskResponse createOutPaintingTask(CreateOutPaintingTaskRequest createOutPaintingTaskRequest) {
        // 参数校验
        if (createOutPaintingTaskRequest == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "扩图参数为空");
        }
        
        // 校验apiKey是否配置
        if (StrUtil.isBlank(apiKey)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未配置阿里云AI API密钥");
        }
        
        // 校验必要参数
        if (createOutPaintingTaskRequest.getInput() == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "输入参数不能为空");
        }
        
        if (StrUtil.isBlank(createOutPaintingTaskRequest.getInput().getBaseImageUrl())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "基础图像URL不能为空");
        }
        
        if (StrUtil.isBlank(createOutPaintingTaskRequest.getModel())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "模型名称不能为空");
        }

        // 发送请�?
        String requestBody = JSONUtil.toJsonStr(createOutPaintingTaskRequest);
        log.info("AI扩图请求URL: {}", CREATE_OUT_PAINTING_TASK_URL);
        log.info("AI扩图请求参数: {}", requestBody);
        
        HttpRequest httpRequest = HttpRequest.post(CREATE_OUT_PAINTING_TASK_URL)
                .header(Header.AUTHORIZATION, "Bearer " + apiKey)
                // 必须开启异步处理，设置为enable
                .header("X-DashScope-Async", "enable")
                .header(Header.CONTENT_TYPE, ContentType.JSON.getValue())
                .body(requestBody);
                
        log.info("请求头信�? Authorization=Bearer ***, X-DashScope-Async=enable, Content-Type={}", ContentType.JSON.getValue());
        
        try (HttpResponse httpResponse = httpRequest.execute()) {
            String responseBody = httpResponse.body();
            log.info("API响应状态码: {}", httpResponse.getStatus());
            log.info("API响应内容: {}", responseBody);
            
            if (!httpResponse.isOk()) {
                log.error("请求失败，状态码: {}，响应内�? {}", httpResponse.getStatus(), responseBody);
                // 根据HTTP状态码提供更详细的错误信息
                String errorMsg = switch (httpResponse.getStatus()) {
                    case 400 -> "请求参数错误，请检查请求体格式和参数�?;
                    case 401 -> "认证失败，请检查API密钥是否正确";
                    case 403 -> "权限不足，请检查API密钥权限";
                    case 429 -> "请求频率过高，请稍后重试";
                    case 500 -> "服务器内部错误，请稍后重�?;
                    default -> "请求失败";
                };
                throw new BusinessException(ErrorCode.OPERATION_ERROR, 
                    String.format("AI扩图失败，HTTP状态码: %d，错误信�? %s，响应内�? %s", 
                        httpResponse.getStatus(), errorMsg, responseBody));
            }
            
            CreateOutPaintingTaskResponse response = JSONUtil.toBean(responseBody, CreateOutPaintingTaskResponse.class);
            String errorCode = response.getCode();
            if (StrUtil.isNotBlank(errorCode)) {
                String errorMessage = response.getMessage();
                log.error("AI扩图失败，errorCode: {}, errorMessage: {}", errorCode, errorMessage);
                throw new BusinessException(ErrorCode.OPERATION_ERROR, 
                    String.format("AI扩图接口响应异常，错误码: %s，错误信�? %s", errorCode, errorMessage));
            }
            
            log.info("AI扩图任务创建成功，任务ID: {}", response.getOutput() != null ? response.getOutput().getTaskId() : "未知");
            return response;
        } catch (BusinessException e) {
            // 重新抛出业务异常
            throw e;
        } catch (Exception e) {
            log.error("AI扩图请求发生异常", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "AI扩图请求异常: " + e.getMessage());
        }
    }

    /**
     * 获取AI扩图任务结果
     *
     * @param taskId 任务ID
     * @return GetOutPaintingTaskResponse 扩图任务结果响应
     */
    public GetOutPaintingTaskResponse getOutPaintingTask(String taskId) {
        // 参数校验
        if (StrUtil.isBlank(taskId)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "任务ID不能为空");
        }
        
        // 校验apiKey是否配置
        if (StrUtil.isBlank(apiKey)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未配置阿里云AI API密钥");
        }

        String url = String.format(GET_OUT_PAINTING_TASK_URL, taskId);
        log.info("获取AI扩图任务结果，任务ID: {}, 请求URL: {}", taskId, url);
        
        HttpRequest httpRequest = HttpRequest.get(url)
                .header(Header.AUTHORIZATION, "Bearer " + apiKey)
                .header(Header.CONTENT_TYPE, ContentType.JSON.getValue());
                
        try (HttpResponse httpResponse = httpRequest.execute()) {
            String responseBody = httpResponse.body();
            log.info("获取AI扩图任务结果响应状态码: {}, 响应内容: {}", httpResponse.getStatus(), responseBody);
            
            if (!httpResponse.isOk()) {
                log.error("获取AI扩图任务结果失败，HTTP状态码: {}, 响应内容: {}", httpResponse.getStatus(), responseBody);
                
                // 根据HTTP状态码提供更具体的错误信息
                String errorMessage = switch (httpResponse.getStatus()) {
                    case 400 -> "请求参数错误，任务ID格式可能不正�?;
                    case 401 -> "API密钥无效或已过期，请检查配�?;
                    case 403 -> "访问被拒绝，请检查API权限";
                    case 404 -> "任务不存在或已过期，请检查任务ID";
                    case 429 -> "请求频率过高，请稍后重试";
                    case 500 -> "服务器内部错误，请稍后重�?;
                    default -> "获取AI扩图任务结果失败，HTTP状态码: " + httpResponse.getStatus();
                };
                
                throw new BusinessException(ErrorCode.OPERATION_ERROR, errorMessage + "，详细信�? " + responseBody);
            }
            
            // 解析响应
            GetOutPaintingTaskResponse response;
            try {
                response = JSONUtil.toBean(responseBody, GetOutPaintingTaskResponse.class);
            } catch (Exception e) {
                log.error("解析AI扩图任务结果响应失败，响应内�? {}", responseBody, e);
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "解析AI扩图任务结果响应失败: " + e.getMessage());
            }
            
            // 检查业务错误码
            String errorCode = response.getOutput() != null ? response.getOutput().getCode() : null;
            if (StrUtil.isNotBlank(errorCode)) {
                String errorMessage = response.getOutput() != null ? response.getOutput().getMessage() : "未知错误";
                log.error("获取AI扩图任务结果业务失败，errorCode: {}, errorMessage: {}", errorCode, errorMessage);
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取AI扩图任务结果异常: " + errorMessage + "(错误�? " + errorCode + ")");
            }
            
             // 输出图像URL已直接包含在response中，无需额外处理
            if (response.getOutput() != null && StrUtil.isNotBlank(response.getOutput().getOutputImageUrl())) {
                log.info("获取到输出图像URL: {}", response.getOutput().getOutputImageUrl());
            }
            
            log.info("获取AI扩图任务结果成功，任务状�? {}", response.getOutput() != null ? response.getOutput().getTaskStatus() : "未知");
            return response;
            
        } catch (BusinessException e) {
            // 重新抛出业务异常
            throw e;
        } catch (Exception e) {
            log.error("获取AI扩图任务结果请求发生未知异常，任务ID: {}", taskId, e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取AI扩图任务结果请求异常: " + e.getMessage());
        }
    }
}

package com.picturebackend.controller;

import com.picturebackend.common.BaseResponse;
import com.picturebackend.common.ResultUtils;
import com.picturebackend.exception.ErrorCode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * packageName: com.picturebackend.controller
 *
 * @author: idpeng
 * @version: 1.0
 * @className: MainController
 * @date: 2025/7/7 22:34
 * @description: 用于健康检查和测试
 */
@RestController
@RequestMapping("/")
@Api("健康测试")
public class MainController {

    /**
     * 健康检查接�?
     * 用于验证服务是否正常运行
     * @return 返回成功响应，包�?ok"消息
     */
    @ApiOperation("健康测试接口")
    @GetMapping("/health")
    public BaseResponse<String> health() {
        return ResultUtils.success("ok");
    }

}

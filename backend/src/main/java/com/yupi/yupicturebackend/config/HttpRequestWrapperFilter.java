package com.yupi.yupicturebackend.config;

import cn.hutool.http.ContentType;
import cn.hutool.http.Header;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;


/**
 * packageName: com.yupi.yupicturebackend.config
 *
 * @author: idpeng
 * @version: 1.0
 * @className: HttpRequestWrapperFilter
 * @date: 2025/8/26 10:55
 * @description: 请求包装过滤器
 */
@Order(1)
@Component
public class HttpRequestWrapperFilter implements Filter {
    /**
     * 过滤器核心方法，对JSON类型的请求进行包装处理
     * 用于提供对请求体内容的重复读取能力
     *
     * @param request  Servlet请求对象
     * @param response Servlet响应对象
     * @param chain    过滤器链对象
     * @throws ServletException Servlet异常
     * @throws IOException      IO异常
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
        if (request instanceof HttpServletRequest) {
            HttpServletRequest servletRequest = (HttpServletRequest) request;
            String contentType = servletRequest.getHeader(Header.CONTENT_TYPE.getValue());
            if (ContentType.JSON.getValue().equals(contentType)) {
                // 可以再细粒度一些，只有需要进行空间权限校验的接口才需要包一层
                chain.doFilter(new RequestWrapper(servletRequest), response);
            } else {
                chain.doFilter(request, response);
            }
        }
    }
}

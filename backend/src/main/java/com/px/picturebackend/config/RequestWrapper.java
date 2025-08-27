package com.px.picturebackend.config;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;

/**
 * packageName: com.px.picturebackend.config
 *
 * @author: idpeng
 * @version: 1.0
 * @className: RequestWrapper
 * @date: 2025/8/26 10:51
 * @description: RequestWrapper 请求包装类
 */
@Slf4j
public class RequestWrapper extends HttpServletRequestWrapper {

    private final String body;

    public RequestWrapper(HttpServletRequest request) {
        super(request);
        StringBuilder stringBuilder = new StringBuilder();
        try (InputStream inputStream = request.getInputStream(); BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))) {
            char[] charBuffer = new char[128];
            int bytesRead = -1;
            while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
                stringBuilder.append(charBuffer, 0, bytesRead);
            }
        } catch (IOException ignored) {
        }
        body = stringBuilder.toString();
    }

    /**
     * 重写getInputStream方法，返回自定义的ServletInputStream
     * 用于提供对请求体内容的重复读取能力
     *
     * @return ServletInputStream 自定义的输入流对象
     * @throws IOException IO异常
     */
    @Override
    public ServletInputStream getInputStream() throws IOException {
        // 创建基于请求体内容的字节数组输入流
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(body.getBytes("UTF-8"));
        return new ServletInputStream() {
            /**
             * 检查输入流是否已完成读取
             * 在这个自定义实现中，始终返回false表示流未完成
             *
             * @return boolean 始终返回false，表示流未完成读取
             */
            @Override
            public boolean isFinished() {
                return false;
            }

            /**
             * 检查输入流是否已准备好进行读取
             * 在这个自定义实现中，始终返回false表示流未准备好进行读取
             *
             * @return boolean 始终返回false，表示流未准备好进行读取
             */
            @Override
            public boolean isReady() {
                return false;
            }

            /**
             * 设置读取监听器，用于异步读取数据
             * 在这个自定义实现中，方法体为空，因为不需要异步处理   
             *
             * @param readListener 读取监听器对象
             */
            @Override
            public void setReadListener(ReadListener readListener) {
            }

            /**
             * 从输入流中读取下一个字节的数据
             * 从字节数组输入流中读取数据并返回
             *
             * @return int 下一个字节的数据，如果到达流末尾则返回-1
             * @throws IOException 当发生IO异常时抛出
             */
            @Override
            public int read() throws IOException {
                return byteArrayInputStream.read();
            }
        };

    }

    /**
     * 重写getReader方法，返回自定义的BufferedReader
     * 用于提供对请求体内容的字符流读取能力
     *
     * @return BufferedReader 自定义的字符缓冲读取器对象
     * @throws IOException IO异常
     */
    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(this.getInputStream(), "UTF-8"));
    }

    public String getBody() {
        return this.body;
    }

}

package com.px.picturebackend.config;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.region.Region;
import com.qcloud.cos.transfer.TransferManager;
import com.qcloud.cos.transfer.TransferManagerConfiguration;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * packageName: com.px.picturebackend.config
 *
 * @author: idpeng
 * @version: 1.0
 * @className: CosClientConfig
 * @date: 2025/7/26 10:34
 * @description: 读取配置文件，创建一个COS
 */
@Configuration
@ConfigurationProperties(prefix = "cos.client")
@Data
public class CosClientConfig {
    /**
     * 域名
     */
    private String host;

    /**
     * secretId
     */
    private String secretId;

    /**
     * 密钥（注意不要泄露）
     */
    private String secretKey;

    /**
     * 区域
     */
    private String region;

    /**
     * 桶名
     */
    private String bucket;

    /**
     * 分块上传阈值（字节），默认10MB
     */
    private long multipartUploadThreshold = 10 * 1024 * 1024L;

    /**
     * 分块大小（字节），默认2MB
     */
    private long partSize = 2 * 1024 * 1024L;

    /**
     * 线程池大小，默认5
     */
    private int threadPoolSize = 5;

    @Bean
    public COSClient cosClient() {
        // 初始化用户身份信息（secretId, secretKey）
        // 1. 登录访问密钥控制台 https://console.cloud.tencent.com/cam/capi
        // 2. 创建密钥对并下载密钥文件
        // 3. 填写密钥对信息
        // 初始化用户身份信息（secretId, secretKey）
        COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
        // 设置bucket的区域， COS地域的简称请参照 https://www.qcloud.com/document/product/436/6224
        ClientConfig clientConfig = new ClientConfig(new Region(region));
        // 生成cos客户端    
        return new COSClient(cred, clientConfig);
    }

    /**
     * 创建TransferManager Bean，用于大文件分块上传
     * 配置分块上传阈值、分块大小和线程池大小
     * 
     * @return TransferManager实例
     */
    @Bean
    public TransferManager transferManager() {
        // 创建线程池，用于并发上传分块
        ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(threadPoolSize);
        
        // 创建TransferManager实例
        TransferManager transferManager = new TransferManager(cosClient(), threadPool);
        
        // 配置TransferManager参数
        TransferManagerConfiguration transferConfig = new TransferManagerConfiguration();
        
        // 设置分块上传阈值：文件大小超过此值时使用分块上传
        transferConfig.setMultipartUploadThreshold(multipartUploadThreshold);
        
        // 设置分块大小：每个分块的大小
        transferConfig.setMinimumUploadPartSize(partSize);
        
        // 应用配置
        transferManager.setConfiguration(transferConfig);
        
        return transferManager;
    }
}

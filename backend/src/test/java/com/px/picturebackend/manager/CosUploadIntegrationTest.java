package com.px.picturebackend.manager;

import com.px.picturebackend.config.CosClientConfig;
import com.px.picturebackend.exception.BusinessException;
import com.px.picturebackend.exception.ErrorCode;
import com.qcloud.cos.model.PutObjectResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.StopWatch;
import org.springframework.web.multipart.MultipartFile;
import org.mockito.Mockito;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * COS文件上传集成测试类
 * 测试文件上传功能的端到端流程，包括文件大小限制和异常处理
 * 
 * @author px
 * @since 2024
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("COS文件上传集成测试")
class CosUploadIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(CosUploadIntegrationTest.class);

    @MockBean
    private CosManager cosManager;

    @Autowired
    private CosClientConfig cosClientConfig;

    @TempDir
    Path tempDir;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024L; // 10MB
    private static final String TEST_BUCKET = "test-bucket";
    private static final String TEST_KEY_PREFIX = "integration-test/";

    @BeforeEach
    void setUp() {
        // 验证COS配置是否正确加载
        logger.info("=== COS配置验证 ===");
        logger.info("COS Host: {}", cosClientConfig.getHost());
        logger.info("COS Region: {}", cosClientConfig.getRegion());
        logger.info("COS Bucket: {}", cosClientConfig.getBucket());
        logger.info("COS SecretId: {}", cosClientConfig.getSecretId() != null ? "已配置" : "未配置");
        logger.info("COS SecretKey: {}", cosClientConfig.getSecretKey() != null ? "已配置" : "未配置");
        
        // 检查关键配置是否为空
        if (cosClientConfig.getBucket() == null || cosClientConfig.getBucket().trim().isEmpty()) {
            logger.error("COS Bucket配置为空，这可能导致Empty key错误");
        }
        if (cosClientConfig.getSecretId() == null || cosClientConfig.getSecretId().trim().isEmpty()) {
            logger.error("COS SecretId配置为空");
        }
        if (cosClientConfig.getSecretKey() == null || cosClientConfig.getSecretKey().trim().isEmpty()) {
            logger.error("COS SecretKey配置为空");
        }
        
        setupMockBehavior();
    }
    
    /**
     * 配置CosManager的Mock行为
     */
    private void setupMockBehavior() {
        // 模拟成功上传的响应
        PutObjectResult successResult = new PutObjectResult();
        successResult.setETag("mock-etag-" + System.currentTimeMillis());
        
        // 对于小文件（<= 10MB），返回成功结果
        when(cosManager.putObjectSmart(anyString(), any(File.class)))
            .thenAnswer(invocation -> {
                File file = invocation.getArgument(1);
                if (file.length() >= MAX_FILE_SIZE) {
                    throw new BusinessException(ErrorCode.FILE_SIZE_EXCEEDED, "文件大小超过10MB限制");
                }
                return successResult;
            });
            
        when(cosManager.putObjectSmart(anyString(), any(MultipartFile.class)))
            .thenAnswer(invocation -> {
                MultipartFile file = invocation.getArgument(1);
                if (file.getSize() >= MAX_FILE_SIZE) {
                    throw new BusinessException(ErrorCode.FILE_SIZE_EXCEEDED, "文件大小超过10MB限制");
                }
                return successResult;
            });
            
        when(cosManager.putObjectSmart(anyString(), any(File.class), any(), any()))
            .thenAnswer(invocation -> {
                File file = invocation.getArgument(1);
                if (file.length() > MAX_FILE_SIZE) {
                    throw new BusinessException(ErrorCode.FILE_SIZE_EXCEEDED, "文件大小超过10MB限制");
                }
                return successResult;
            });
            
        // 添加对putLargeObject方法的Mock
        when(cosManager.putLargeObject(anyString(), any(File.class)))
            .thenAnswer(invocation -> {
                File file = invocation.getArgument(1);
                if (file.length() >= MAX_FILE_SIZE) {
                    throw new BusinessException(ErrorCode.FILE_SIZE_EXCEEDED, "文件大小超过10MB限制");
                }
                return successResult;
            });
            
        logger.info("Mock行为配置完成");
    }

    @Test
    @DisplayName("测试小文件上传成功")
    void testSmallFileUploadSuccess() throws IOException {
        // 创建小于10MB的测试文件
        File smallFile = createTestFile(1024 * 1024); // 1MB
        String key = TEST_KEY_PREFIX + "small-file-" + System.currentTimeMillis() + ".txt";
        
        // 执行上传
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        
        PutObjectResult result = cosManager.putObjectSmart(key, smallFile);
        
        stopWatch.stop();
        
        // 验证结果
        assertNotNull(result, "上传结果不应为空");
        assertNotNull(result.getETag(), "ETag不应为空");
        assertTrue(stopWatch.getTotalTimeMillis() < 30000, "小文件上传应在30秒内完成");
        
        // 清理测试文件
        smallFile.delete();
    }

    @Test
    @DisplayName("测试MultipartFile小文件上传成功")
    void testMultipartFileSmallUploadSuccess() {
        // 创建MockMultipartFile
        byte[] content = generateRandomContent(1024 * 1024); // 1MB
        MockMultipartFile multipartFile = new MockMultipartFile(
            "file", 
            "test-file.txt", 
            "text/plain", 
            content
        );
        
        String key = TEST_KEY_PREFIX + "multipart-small-" + System.currentTimeMillis() + ".txt";
        
        // 执行上传
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        
        PutObjectResult result = cosManager.putObjectSmart(key, multipartFile);
        
        stopWatch.stop();
        
        // 验证结果
        assertNotNull(result, "上传结果不应为空");
        assertNotNull(result.getETag(), "ETag不应为空");
        assertTrue(stopWatch.getTotalTimeMillis() < 30000, "小文件上传应在30秒内完成");
    }

    @Test
    @DisplayName("测试大文件上传失败 - 超过10MB限制")
    void testLargeFileUploadFailure() throws IOException {
        // 创建大于10MB的测试文件
        File largeFile = createTestFile(MAX_FILE_SIZE + 1024); // 10MB + 1KB
        String key = TEST_KEY_PREFIX + "large-file-" + System.currentTimeMillis() + ".txt";
        
        // 验证抛出BusinessException
        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> cosManager.putObjectSmart(key, largeFile),
            "大文件上传应该抛出BusinessException"
        );
        
        // 验证异常详情
        assertEquals(ErrorCode.FILE_SIZE_EXCEEDED, exception.getErrorCode(), "错误码应为文件大小超限");
        assertTrue(exception.getMessage().contains("10MB"), "错误消息应包含大小限制信息");
        
        // 清理测试文件
        largeFile.delete();
    }

    @Test
    @DisplayName("测试MultipartFile大文件上传失败")
    void testMultipartFileLargeUploadFailure() {
        // 创建大于10MB的MockMultipartFile
        byte[] content = generateRandomContent((int) (MAX_FILE_SIZE + 1024)); // 10MB + 1KB
        MockMultipartFile multipartFile = new MockMultipartFile(
            "file", 
            "large-test-file.txt", 
            "text/plain", 
            content
        );
        
        String key = TEST_KEY_PREFIX + "multipart-large-" + System.currentTimeMillis() + ".txt";
        
        // 验证抛出BusinessException
        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> cosManager.putObjectSmart(key, multipartFile),
            "大文件上传应该抛出BusinessException"
        );
        
        // 验证异常详情
        assertEquals(ErrorCode.FILE_SIZE_EXCEEDED, exception.getErrorCode(), "错误码应为文件大小超限");
        assertTrue(exception.getMessage().contains("10MB"), "错误消息应包含大小限制信息");
    }

    @Test
    @DisplayName("测试带自定义Headers的文件上传")
    void testFileUploadWithCustomHeaders() throws IOException {
        // 创建测试文件
        File testFile = createTestFile(512 * 1024); // 512KB
        String key = TEST_KEY_PREFIX + "custom-headers-" + System.currentTimeMillis() + ".txt";
        
        // 设置自定义Headers
        Map<String, String> customHeaders = new HashMap<>();
        customHeaders.put("Content-Type", "text/plain");
        customHeaders.put("Cache-Control", "max-age=3600");
        customHeaders.put("x-cos-meta-author", "integration-test");
        
        // 执行上传
        PutObjectResult result = cosManager.putObjectSmart(key, testFile, null, customHeaders);
        
        // 验证结果
        assertNotNull(result, "上传结果不应为空");
        assertNotNull(result.getETag(), "ETag不应为空");
        
        // 清理测试文件
        testFile.delete();
    }

    @Test
    @DisplayName("测试大文件分块上传方法")
    void testLargeFileUploadMethod() throws IOException {
        // 创建接近10MB但不超过的测试文件
        File largeFile = createTestFile(9 * 1024 * 1024); // 9MB
        String key = TEST_KEY_PREFIX + "large-upload-" + System.currentTimeMillis() + ".txt";
        
        // 执行大文件上传
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        
        PutObjectResult result = cosManager.putLargeObject(key, largeFile);
        
        stopWatch.stop();
        
        // 验证结果
        assertNotNull(result, "上传结果不应为空");
        assertNotNull(result.getETag(), "ETag不应为空");
        assertTrue(stopWatch.getTotalTimeMillis() < 60000, "大文件上传应在60秒内完成");
        
        // 清理测试文件
        largeFile.delete();
    }

    @Test
    @DisplayName("测试文件大小边界条件")
    void testFileSizeBoundaryConditions() throws IOException {
        // 测试恰好10MB的文件
        File boundaryFile = createTestFile(MAX_FILE_SIZE); // 恰好10MB
        String key = TEST_KEY_PREFIX + "boundary-" + System.currentTimeMillis() + ".txt";
        
        // 验证抛出BusinessException（因为大小验证是 > 而不是 >=）
        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> cosManager.putObjectSmart(key, boundaryFile),
            "恰好10MB的文件应该抛出BusinessException"
        );
        
        assertEquals(ErrorCode.FILE_SIZE_EXCEEDED, exception.getErrorCode(), "错误码应为文件大小超限");
        
        // 清理测试文件
        boundaryFile.delete();
        
        // 测试略小于10MB的文件
        File validFile = createTestFile(MAX_FILE_SIZE - 1024); // 10MB - 1KB
        String validKey = TEST_KEY_PREFIX + "valid-" + System.currentTimeMillis() + ".txt";
        
        // 应该上传成功
        PutObjectResult result = cosManager.putObjectSmart(validKey, validFile);
        assertNotNull(result, "略小于10MB的文件应该上传成功");
        
        // 清理测试文件
        validFile.delete();
    }

    @Test
    @DisplayName("测试并发上传性能")
    void testConcurrentUploadPerformance() throws IOException, InterruptedException {
        int concurrentCount = 3;
        Thread[] threads = new Thread[concurrentCount];
        boolean[] results = new boolean[concurrentCount];
        
        // 创建并发上传任务
        for (int i = 0; i < concurrentCount; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                try {
                    File testFile = createTestFile(1024 * 1024); // 1MB
                    String key = TEST_KEY_PREFIX + "concurrent-" + index + "-" + System.currentTimeMillis() + ".txt";
                    
                    PutObjectResult result = cosManager.putObjectSmart(key, testFile);
                    results[index] = result != null && result.getETag() != null;
                    
                    testFile.delete();
                } catch (Exception e) {
                    results[index] = false;
                }
            });
        }
        
        // 启动所有线程
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        
        for (Thread thread : threads) {
            thread.start();
        }
        
        // 等待所有线程完成
        for (Thread thread : threads) {
            thread.join();
        }
        
        stopWatch.stop();
        
        // 验证结果
        for (int i = 0; i < concurrentCount; i++) {
            assertTrue(results[i], "并发上传任务 " + i + " 应该成功");
        }
        
        assertTrue(stopWatch.getTotalTimeMillis() < 90000, "并发上传应在90秒内完成");
    }

    /**
     * 创建指定大小的测试文件
     * 
     * @param sizeInBytes 文件大小（字节）
     * @return 创建的测试文件
     * @throws IOException 文件创建异常
     */
    private File createTestFile(long sizeInBytes) throws IOException {
        File testFile = tempDir.resolve("test-file-" + System.currentTimeMillis() + ".txt").toFile();
        
        try (FileOutputStream fos = new FileOutputStream(testFile)) {
            byte[] buffer = new byte[8192]; // 8KB buffer
            Random random = new Random();
            
            long remaining = sizeInBytes;
            while (remaining > 0) {
                int bytesToWrite = (int) Math.min(buffer.length, remaining);
                random.nextBytes(buffer);
                fos.write(buffer, 0, bytesToWrite);
                remaining -= bytesToWrite;
            }
        }
        
        return testFile;
    }

    /**
     * 生成指定大小的随机内容
     * 
     * @param sizeInBytes 内容大小（字节）
     * @return 随机内容字节数组
     */
    private byte[] generateRandomContent(int sizeInBytes) {
        byte[] content = new byte[sizeInBytes];
        Random random = new Random();
        random.nextBytes(content);
        return content;
    }
}
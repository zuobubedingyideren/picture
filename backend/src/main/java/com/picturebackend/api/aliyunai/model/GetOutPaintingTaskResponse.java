package com.picturebackend.api.aliyunai.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * packageName: com.picturebackend.api.aliyunai.model
 *
 * @author: idpeng
 * @version: 1.0
 * @className: GetOutPaintingTaskResponse
 * @date: 2025/8/24 16:17
 * @description: 查询任务响应�?
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetOutPaintingTaskResponse {

    /**
     * 请求唯一标识
     */
    private String requestId;

    /**
     * 输出信息
     */
    private Output output;

    /**
     * 表示任务的输出信�?
     */
    @Data
    public static class Output {

        /**
         * 任务 ID
         */
        private String taskId;

        /**
         * 任务状�?
         * <ul>
         *     <li>PENDING：排队中</li>
         *     <li>RUNNING：处理中</li>
         *     <li>SUSPENDED：挂�?/li>
         *     <li>SUCCEEDED：执行成�?/li>
         *     <li>FAILED：执行失�?/li>
         *     <li>UNKNOWN：任务不存在或状态未�?/li>
         * </ul>
         */
        private String taskStatus;

        /**
         * 提交时间
         * 格式：YYYY-MM-DD HH:mm:ss.SSS
         */
        private String submitTime;

        /**
         * 调度时间
         * 格式：YYYY-MM-DD HH:mm:ss.SSS
         */
        private String scheduledTime;

        /**
         * 结束时间
         * 格式：YYYY-MM-DD HH:mm:ss.SSS
         */
        private String endTime;

        /**
         * 输出图像�?URL
         */
        private String outputImageUrl;

        /**
         * 接口错误�?
         * <p>接口成功请求不会返回该参�?/p>
         */
        private String code;

        /**
         * 接口错误信息
         * <p>接口成功请求不会返回该参�?/p>
         */
        private String message;

        /**
         * 任务指标信息
         */
        private TaskMetrics taskMetrics;
    }

    /**
     * 表示任务的统计信�?
     */
    @Data
    public static class TaskMetrics {

        /**
         * 总任务数
         */
        private Integer total;

        /**
         * 成功任务�?
         */
        private Integer succeeded;

        /**
         * 失败任务�?
         */
        private Integer failed;
    }
}


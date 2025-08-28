package com.px.picturebackend.constant;

/**
 * 图片服务相关常量定义
 * 包含文件处理、业务规则、权限控制、AI服务等相关常量
 *
 * @author idpeng
 * @version 2.0
 * @since 2025-01-20
 */
public final class PictureConstants {

    // ==================== 文件相关常量 ====================
    
    /**
     * 公共空间上传路径前缀格式
     */
    public static final String PUBLIC_UPLOAD_PATH_PREFIX = "public/%s";
    
    /**
     * 私有空间上传路径前缀格式
     */
    public static final String SPACE_UPLOAD_PATH_PREFIX = "space/%s";
    
    /**
     * JSON标签查询格式
     */
    public static final String JSON_TAG_FORMAT = "\"%s\"";
    
    /**
     * 图片文件最大大小（字节）- 2MB
     */
    public static final long MAX_FILE_SIZE = 2 * 1024 * 1024L;
    
    /**
     * 图片文件最小大小（字节）- 1KB
     */
    public static final long MIN_FILE_SIZE = 1024L;
    
    /**
     * 支持的图片格式
     */
    public static final String[] SUPPORTED_IMAGE_FORMATS = {
        "jpeg", "jpg", "png", "gif", "bmp", "webp"
    };
    
    /**
     * 图片MIME类型前缀
     */
    public static final String IMAGE_MIME_TYPE_PREFIX = "image/";


    /**
     * 默认抓取图片URL
     */
    public static final String DEFAULT_URL = "https://cn.bing.com/images/async?q=%s&mmasync=1";

    /**
     * 批量上传图片的最大数量限制
     */
    public static final int BATCH_UPLOAD_MAX_COUNT = 30;

    /**
     * 批量上传失败重试的最大次数
     */
    public static final int MAX_FAILED_ATTEMPTS = 10;
    /**
     * 网络请求超时时间（毫秒）
     */
    public static final int NETWORK_TIMEOUT_MS = 10000;

    /**
     * 默认用户代理
     */
    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36";

    /**
     * 默认图片格式
     */
    public static final String DEFAULT_IMAGE_FORMAT = "jpg";

    // ==================== 业务规则常量 ====================
    
    /**
     * 图片审核状态 - 待审核
     */
    public static final Integer REVIEW_STATUS_PENDING = 0;
    
    /**
     * 图片审核状态 - 通过
     */
    public static final Integer REVIEW_STATUS_PASS = 1;
    
    /**
     * 图片审核状态 - 拒绝
     */
    public static final Integer REVIEW_STATUS_REJECT = 2;
    
    /**
     * 分页查询最大页面大小限制，防止恶意请求
     */
    public static final int MAX_PAGE_SIZE = 20;
    
    /**
     * 默认分页大小
     */
    public static final long DEFAULT_PAGE_SIZE = 10L;
    
    /**
     * 批量操作最大数量
     */
    public static final int MAX_BATCH_SIZE = 100;
    
    /**
     * 标签最大数量
     */
    public static final int MAX_TAG_COUNT = 10;
    
    /**
     * 标签最大长度
     */
    public static final int MAX_TAG_LENGTH = 20;
    
    /**
     * 图片名称最大长度
     */
    public static final int MAX_PICTURE_NAME_LENGTH = 100;
    
    /**
     * 图片简介最大长度
     */
    public static final int MAX_PICTURE_INTRODUCTION_LENGTH = 500;
    
    /**
     * 图片URL最大长度
     */
    public static final int MAX_URL_LENGTH = 1024;
    
    /**
     * 图片简介最大长度（别名，用于工具类）
     */
    public static final int MAX_INTRODUCTION_LENGTH = MAX_PICTURE_INTRODUCTION_LENGTH;


    // ==================== 颜色搜索相关常量 ====================
    
    /**
     * 颜色相似度阈值
     */
    public static final double COLOR_SIMILARITY_THRESHOLD = 0.8;
    
    /**
     * 颜色搜索默认返回数量
     */
    public static final int DEFAULT_COLOR_SEARCH_LIMIT = 20;
    
    /**
     * RGB颜色最大值
     */
    public static final int RGB_MAX_VALUE = 255;
    
    /**
     * RGB颜色最小值
     */
    public static final int RGB_MIN_VALUE = 0;

    // ==================== 排序相关常量 ====================
    
    /**
     * 有效的排序字段
     */
    public static final String[] VALID_SORT_FIELDS = {
            "id", "name", "category", "picSize", "picWidth", "picHeight",
            "picScale", "userId", "spaceId", "reviewStatus", "createTime",
            "editTime", "updateTime"
    };
    
    /**
     * 默认排序字段
     */
    public static final String DEFAULT_SORT_FIELD = "createTime";
    
    /**
     * 默认排序方向
     */
    public static final String DEFAULT_SORT_ORDER = "desc";
    
    /**
     * 升序排序
     */
    public static final String SORT_ORDER_ASC = "asc";

    /**
     * 升序排序
     */
    public static final String SORT_ORDER_ASCEND = "ascend";
    
    /**
     * 降序排序
     */
    public static final String SORT_ORDER_DESC = "desc";

    // ==================== 用户权限相关常量 ====================
    
    /**
     * 管理员角色
     */
    public static final String ROLE_ADMIN = "admin";
    
    /**
     * 普通用户角色
     */
    public static final String ROLE_USER = "user";
    
    /**
     * VIP用户角色
     */
    public static final String ROLE_VIP = "vip";

    // ==================== AI扩图相关常量 ====================
    
    /**
     * AI扩图任务状态 - 等待中
     */
    public static final String OUTPAINTING_STATUS_WAITING = "WAITING";
    
    /**
     * AI扩图任务状态 - 运行中
     */
    public static final String OUTPAINTING_STATUS_RUNNING = "RUNNING";
    
    /**
     * AI扩图任务状态 - 成功
     */
    public static final String OUTPAINTING_STATUS_SUCCESS = "SUCCESS";
    
    /**
     * AI扩图任务状态 - 失败
     */
    public static final String OUTPAINTING_STATUS_FAILED = "FAILED";
    
    /**
     * AI扩图默认参数
     */
    public static final String DEFAULT_OUTPAINTING_PARAMETERS = "{}";

    // ==================== 缓存相关常量 ====================
    
    /**
     * 缓存键前缀
     */
    public static final String CACHE_KEY_PREFIX = "yupicture:listPictureVOByPage:";

    /**
     * 缓存过期时间基础值（秒）
     */
    public static final int CACHE_BASE_EXPIRE_TIME = 300;

    /**
     * 缓存过期时间随机范围（秒），用于防止缓存雪崩
     */
    public static final int CACHE_RANDOM_EXPIRE_TIME = 300;

    /**
     * 本地缓存初始容量
     */
    public static final int LOCAL_CACHE_INITIAL_CAPACITY = 1024;

    /**
     * 本地缓存最大容量
     */
    public static final long LOCAL_CACHE_MAXIMUM_SIZE = 10000L;

    /**
     * 本地缓存过期时间（分钟）
     */
    public static final long LOCAL_CACHE_EXPIRE_MINUTES = 5L;
    
    /**
     * 用户信息缓存过期时间（秒）- 30分钟
     */
    public static final long USER_CACHE_EXPIRE_TIME = 30 * 60L;
    
    /**
     * 图片信息缓存过期时间（秒）- 10分钟
     */
    public static final long PICTURE_CACHE_EXPIRE_TIME = 10 * 60L;

    // ==================== 正则表达式常量 ====================
    
    /**
     * 颜色值正则表达式（支持#RRGGBB格式）
     */
    public static final String COLOR_REGEX = "^#[0-9A-Fa-f]{6}$";
    
    /**
     * 标签名称正则表达式（中英文数字下划线）
     */
    public static final String TAG_NAME_REGEX = "^[\\u4e00-\\u9fa5a-zA-Z0-9_]+$";
    
    /**
     * 图片名称正则表达式（中英文数字空格点划线）
     */
    public static final String PICTURE_NAME_REGEX = "^[\\u4e00-\\u9fa5a-zA-Z0-9\\s._-]+$";

    // ==================== 默认值常量 ====================
    
    /**
     * 默认图片分类
     */
    public static final String DEFAULT_CATEGORY = "其他";
    
    /**
     * 默认图片标签
     */
    public static final String DEFAULT_TAG = "未分类";
    
    /**
     * 默认图片简介
     */
    public static final String DEFAULT_INTRODUCTION = "暂无简介";
    
    /**
     * 空字符串
     */
    public static final String EMPTY_STRING = "";
    
    /**
     * 逗号分隔符
     */
    public static final String COMMA_SEPARATOR = ",";
    
    /**
     * 空格分隔符
     */
    public static final String SPACE_SEPARATOR = " ";

    // ==================== 数值计算常量 ====================
    
    /**
     * 字节转KB的除数
     */
    public static final long BYTES_TO_KB = 1024L;
    
    /**
     * 字节转MB的除数
     */
    public static final long BYTES_TO_MB = 1024L * 1024L;
    
    /**
     * 百分比计算基数
     */
    public static final double PERCENTAGE_BASE = 100.0;
    
    /**
     * 颜色相似度计算精度
     */
    public static final double COLOR_SIMILARITY_PRECISION = 0.01;

    // ==================== 时间相关常量 ====================
    
    /**
     * 一天的毫秒数
     */
    public static final long ONE_DAY_MILLIS = 24 * 60 * 60 * 1000L;
    
    /**
     * 一小时的毫秒数
     */
    public static final long ONE_HOUR_MILLIS = 60 * 60 * 1000L;
    
    /**
     * 一分钟的毫秒数
     */
    public static final long ONE_MINUTE_MILLIS = 60 * 1000L;

    // ==================== 线程池相关常量 ====================
    
    /**
     * 异步任务线程池核心线程数
     */
    public static final int ASYNC_CORE_POOL_SIZE = 5;
    
    /**
     * 异步任务线程池最大线程数
     */
    public static final int ASYNC_MAX_POOL_SIZE = 20;
    
    /**
     * 异步任务线程池队列容量
     */
    public static final int ASYNC_QUEUE_CAPACITY = 100;
    
    /**
     * 异步任务线程池线程存活时间（秒）
     */
    public static final long ASYNC_KEEP_ALIVE_TIME = 60L;

    /**
     * 私有构造函数，防止实例化
     * 常量类不应该被实例化
     */
    private PictureConstants() {
        throw new UnsupportedOperationException("常量类不能被实例化");
    }
}

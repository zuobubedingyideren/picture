package com.picturebackend.manager.auth;

import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.stereotype.Component;

/**
 * packageName: com.picturebackend.manager.auth
 *
 * @author: idpeng
 * @version: 1.0
 * @className: StpKit
 * @date: 2025/8/26 10:14
 * @description: 空间账号体系
 * StpLogic 门面类，管理项目中所有的 StpLogic 账号体系
 */
@Component
public class StpKit {

    /**
     * 空间账号体系的类型标�?
     */
    public static final String SPACE_TYPE = "space";

    /**
     * 默认账号体系�?StpLogic 实例
     */
    public static final StpLogic DEFAULT = StpUtil.stpLogic;

    /**
     * 空间账号体系�?StpLogic 实例
     */
    public static final StpLogic SPACE = new StpLogic(SPACE_TYPE);
}

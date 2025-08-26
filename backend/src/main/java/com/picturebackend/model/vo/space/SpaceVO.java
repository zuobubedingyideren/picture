package com.picturebackend.model.vo.space;

import cn.hutool.core.bean.BeanUtil;
import com.picturebackend.model.entity.Space;
import com.picturebackend.model.vo.user.UserVO;
import lombok.Data;


import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * packageName: com.picturebackend.model.vo.space
 *
 * @author: idpeng
 * @version: 1.0
 * @className: SpaceVO
 * @date: 2025/8/22 14:59
 * @description: 空间的视图包装类，可以额外关联创建空间的用户信息�?Space 实体类和�?VO类的转换方法，便于后续快速传值�?
 */
@Data
public class SpaceVO implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 空间名称
     */
    private String spaceName;

    /**
     * 空间级别�?-普通版 1-专业�?2-旗舰�?
     */
    private Integer spaceLevel;

    /**
     * 空间类型�?-私有 1-团队
     */
    private Integer spaceType;

    /**
     * 权限列表
     */
    private List<String> permissionList = new ArrayList<>();


    /**
     * 空间图片的最大总大�?
     */
    private Long maxSize;

    /**
     * 空间图片的最大数�?
     */
    private Long maxCount;

    /**
     * 当前空间下图片的总大�?
     */
    private Long totalSize;

    /**
     * 当前空间下的图片数量
     */
    private Long totalCount;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 编辑时间
     */
    private Date editTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 创建用户信息
     */
    private UserVO user;

    @Serial
    private static final long serialVersionUID = 1L;


    /**
     * 将SpaceVO对象转换为Space对象
     *
     * @param spaceVO 空间VO对象
     * @return 转换后的Space实体对象，如果传入参数为空则返回null
     */
    public static Space voToObj(SpaceVO spaceVO) {
        if (spaceVO == null) {
            return null;
        }
        Space space = new Space();
        BeanUtil.copyProperties(spaceVO, space);
        return space;
    }
    
    /**
     * 将Space对象转换为SpaceVO对象
     *
     * @param space 空间实体对象
     * @return 转换后的SpaceVO视图对象，如果传入参数为空则返回null
     */
    public static SpaceVO objToVo(Space space) {
        if (space == null) {
            return null;
        }
        
        SpaceVO spaceVO = new SpaceVO();
        BeanUtil.copyProperties(space, spaceVO);
        return spaceVO;
    }

}

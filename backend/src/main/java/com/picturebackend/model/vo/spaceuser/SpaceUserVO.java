package com.picturebackend.model.vo.spaceuser;

import cn.hutool.core.bean.BeanUtil;
import com.picturebackend.model.entity.SpaceUser;
import com.picturebackend.model.vo.space.SpaceVO;
import com.picturebackend.model.vo.user.UserVO;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * packageName: com.picturebackend.model.vo.spaceuser
 *
 * @author: idpeng
 * @version: 1.0
 * @className: SpaceUserVO
 * @date: 2025/8/25 20:02
 * @description: 建空间成员的视图包装类，可以额外关联空间信息和创建空间的用户信息
 */
@Data
public class SpaceUserVO implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 空间 id
     */
    private Long spaceId;

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 空间角色：viewer/editor/admin
     */
    private String spaceRole;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 用户信息
     */
    private UserVO user;

    /**
     * 空间信息
     */
    private SpaceVO space;

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 视图对象转实体对�?
     *
     * @param spaceUserVO 空间用户VO对象
     * @return 转换后的SpaceUser实体对象，如果传入参数为空则返回null
     */
    public static SpaceUser voToObj(SpaceUserVO spaceUserVO) {
        if (spaceUserVO == null) {
            return null;
        }
        SpaceUser spaceUser = new SpaceUser();
        BeanUtil.copyProperties(spaceUserVO, spaceUser);
        return spaceUser;
    }

    /**
     * 实体对象转视图对�?
     *
     * @param spaceUser 空间用户实体对象
     * @return 转换后的SpaceUserVO视图对象，如果传入参数为空则返回null
     */
    public static SpaceUserVO objToVo(SpaceUser spaceUser) {
        if (spaceUser == null) {
            return null;
        }
        SpaceUserVO spaceUserVO = new SpaceUserVO();
        BeanUtil.copyProperties(spaceUser, spaceUserVO);
        return spaceUserVO;
    }
}

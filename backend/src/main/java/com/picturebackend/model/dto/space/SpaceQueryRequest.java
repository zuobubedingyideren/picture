package com.picturebackend.model.dto.space;

import com.picturebackend.common.PageRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * packageName: com.picturebackend.model.dto.space
 *
 * @author: idpeng
 * @version: 1.0
 * @className: SpaceQueryRequest
 * @date: 2025/8/22 14:54
 * @description: 空间查询
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel("空间查询")
public class SpaceQueryRequest extends PageRequest implements Serializable {
    /**
     * id
     */
    @ApiModelProperty("id")
    private Long id;

    /**
     * 用户 id
     */
    @ApiModelProperty("用户 id")
    private Long userId;

    /**
     * 空间名称
     */
    @ApiModelProperty("空间名称")
    private String spaceName;

    /**
     * 空间级别�?-普通版 1-专业�?2-旗舰�?
     */
    @ApiModelProperty("空间级别�?-普通版 1-专业�?2-旗舰�?)
    private Integer spaceLevel;

    /**
     * 空间类型�?-私有 1-团队
     */
    private Integer spaceType;


    @Serial
    private static final long serialVersionUID = 1L;
}

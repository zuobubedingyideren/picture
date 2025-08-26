package com.picturebackend.model.dto.picture;

import com.picturebackend.common.PageRequest;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * packageName: com.picturebackend.model.dto.picture
 *
 * @author: idpeng
 * @version: 1.0
 * @className: ere
 * @date: 2025/8/3 16:05
 * @description: 图片查询请求，需要继承公共包中的PageRequest来支持分页查�?
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel(description = "图片查询")
public class PictureQueryRequest extends PageRequest implements Serializable {

    /**
     * id
     */
    @ApiModelProperty(value = "图片ID")
    private Long id;

    /**
     * 图片名称
     */
    @ApiModelProperty(value = "图片名称")
    private String name;

    /**
     * 简�?
     */
    @ApiModelProperty(value = "图片简�?)
    private String introduction;

    /**
     * 分类
     */
    @ApiModelProperty(value = "图片分类")
    private String category;

    /**
     * 标签
     */
    @ApiModelProperty(value = "图片标签列表")
    private List<String> tags;

    /**
     * 文件体积
     */
    @ApiModelProperty(value = "文件大小（字节）")
    private Long picSize;

    /**
     * 图片宽度
     */
    @ApiModelProperty(value = "图片宽度（像素）")
    private Integer picWidth;

    /**
     * 图片高度
     */
    @ApiModelProperty(value = "图片高度（像素）")
    private Integer picHeight;

    /**
     * 图片比例
     */
    @ApiModelProperty(value = "图片宽高�?)
    private Double picScale;

    /**
     * 图片格式
     */
    @ApiModelProperty(value = "图片格式（如jpg、png等）")
    private String picFormat;

    /**
     * 搜索词（同时搜名称、简介等�?
     */
    @ApiModelProperty(value = "搜索关键词（支持名称和简介模糊搜索）")
    private String searchText;

    /**
     * 用户 id
     */
    @ApiModelProperty(value = "上传用户ID")
    private Long userId;

    /**
     * 空间 id
     */
    @ApiModelProperty(value = "空间ID")
    private Long spaceId;

    /**
     * 是否只查�?spaceId �?null 的数�?
     */
    @ApiModelProperty(value = "是否只查�?spaceId �?null 的数�?)
    private boolean nullSpaceId;


    /**
     * 编辑时间起始�?
     */
    @ApiModelProperty(value = "编辑时间起始�?)
    private Date startEditTime;

    /**
     * 编辑时间结束�?
     */
    @ApiModelProperty(value = "编辑时间结束�?)
    private Date endEditTime;

    /**
     * 状态：0-待审�? 1-通过; 2-拒绝
     */
    @ApiModelProperty(value = "审核状态：0-待审�? 1-通过; 2-拒绝")
    private Integer reviewStatus;

    /**
     * 审核信息
     */
    @ApiModelProperty(value = "审核反馈信息")
    private String reviewMessage;

    /**
     * 审核�?id
     */
    @ApiModelProperty(value = "审核人员ID")
    private Long reviewerId;

    @Serial
    private static final long serialVersionUID = 1L;
}


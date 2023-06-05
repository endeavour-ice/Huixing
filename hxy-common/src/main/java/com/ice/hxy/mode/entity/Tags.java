package com.ice.hxy.mode.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 标签表
 * </p>
 *
 * @author ice
 * @since 2023-05-27
 */
@ApiModel(value = "Tags对象", description = "标签表")
@Data
public class Tags implements Serializable {
    private static final long serialVersionUID = -8519634384649843371L;
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @ApiModelProperty("标签类型")
    @TableField("tag_type")
    private String tagType;

    @ApiModelProperty("标签")
    @TableField("tag")
    private String tag;

    @ApiModelProperty("创建人id")
    @TableField("creator_id")
    private Long creatorId;

    @ApiModelProperty("标签类别 0-通用 1-用户 2-队伍 3-文章 4-首页 其他")
    @TableField("category")
    private Long category;

    @ApiModelProperty("标签使用数")
    @TableField("tag_num")
    private Integer tagNum;

    @ApiModelProperty("创建时间")
    @TableField("create_time")
    private LocalDateTime createTime;

    @ApiModelProperty("修改时间")
    @TableField("update_time")
    private LocalDateTime updateTime;

    @ApiModelProperty("是否删除")
    @TableLogic
    private Byte isDelete;
}

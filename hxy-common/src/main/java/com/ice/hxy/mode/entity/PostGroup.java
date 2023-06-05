package com.ice.hxy.mode.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 帖子分组id
 * </p>
 *
 * @author ice
 * @since 2023-05-27
 */
@TableName("post_group")
@ApiModel(value = "PostGroup对象", description = "帖子分组id")
@Data
public class PostGroup implements Serializable {


    private static final long serialVersionUID = 7459783048734886740L;
    @ApiModelProperty("id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("文章id")
    private Long postId;

    @ApiModelProperty("分组id")
    private Long groupId;

    @ApiModelProperty("是否删除")
    @TableLogic
    private Byte isDelete;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("修改时间")
    private LocalDateTime updateTime;


}

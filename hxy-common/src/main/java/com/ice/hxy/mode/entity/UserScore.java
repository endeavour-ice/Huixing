package com.ice.hxy.mode.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 积分表
 * </p>
 *
 * @author ice
 * @since 2023-05-14
 */
@TableName("user_score")
@ApiModel(value = "UserScore对象", description = "积分表")
@Data
public class UserScore implements Serializable {

    private static final long serialVersionUID = 1L;
    @TableId(type = IdType.AUTO)
    private Long id;
    @ApiModelProperty("对外暴露编号")
    private Long scoreId;
    @ApiModelProperty("用户id")
    private Long userId;

    @ApiModelProperty("积分")
    private BigDecimal score;

    @ApiModelProperty("是否删除")
    @TableLogic
    private Integer isDelete;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("修改时间")
    private LocalDateTime updateTime;

}

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
 * 订单表
 * </p>
 *
 * @author ice
 * @since 2023-05-14
 */
@TableName("score_order")
@ApiModel(value = "ScoreOrder对象", description = "订单表")
@Data
public class ScoreOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("用户id")
    private Long userId;

    @ApiModelProperty("对外暴露id")
    private String orderId;

    private BigDecimal score;
    @ApiModelProperty("积分充值金额")
    private BigDecimal scoreAmount;
    /**
     * 0 未支付
     * 1 以支付
     */
    @ApiModelProperty("订单状态")
    private Byte orderStatus;

    @ApiModelProperty("是否删除")
    @TableLogic
    private Byte isDelete;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("修改时间")
    private LocalDateTime updateTime;


}

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
@TableName("score_payment")
@ApiModel(value = "ScorePayment对象", description = "订单表")
@Data
public class ScorePayment implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty("订单id")
    private Long orderId;

    @ApiModelProperty("对外暴露id")
    private Long paymentId;

    @ApiModelProperty("名称")
    private String subject;

    @ApiModelProperty("积分金额")
    private BigDecimal scoreAmount;

    @ApiModelProperty("凭证")
    private String tradeNo;

    @ApiModelProperty("支付金额")
    private BigDecimal buyerAmount;

    @ApiModelProperty("金额")
    private BigDecimal totalAmount;

    @ApiModelProperty("支付状态")
    private String paymentStatus;

    @ApiModelProperty("返回的信息")
    private String callbackContent;

    @ApiModelProperty("付款时间")
    private String gmtCreate;

    @ApiModelProperty("支付类型")
    private Byte paymentType;

    @ApiModelProperty("是否删除")
    @TableLogic
    private Byte isDelete;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("修改时间")
    private LocalDateTime updateTime;


}

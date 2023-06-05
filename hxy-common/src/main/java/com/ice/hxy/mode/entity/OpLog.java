package com.ice.hxy.mode.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 操作日志记录
 * </p>
 *
 * @author ice
 * @since 2023-05-08
 */
@TableName("op_log")
@ApiModel(value = "OpLog对象", description = "操作日志记录")
@Data
public class OpLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("日志主键")
    @TableId(value = "id", type = IdType.AUTO)
    @ExcelProperty("日志主键")
    private Long id;

    @ApiModelProperty("方法名称")
    @ExcelProperty("方法名称")
    private String methodName;

    @ApiModelProperty("请求方式")
    @ExcelProperty("请求方式")
    private String method;

    @ApiModelProperty("操作类别（0其它 1后台用户 2手机端用户）")
    @ExcelProperty("操作类别")
    private Integer opType;

    @ApiModelProperty("请求URL")
    @ExcelProperty("请求URL")
    private String opUrl;

    @ApiModelProperty("方法执行时间")
    @ExcelProperty("方法执行时间")
    private Long exTime;

    @ApiModelProperty("操作账号")
    @ExcelProperty("操作账号")
    private String opName;
    @ApiModelProperty("主机地址")
    @ExcelProperty("主机地址")
    private String opIp;

    @ApiModelProperty("操作地点")
    @ExcelProperty("操作地点")
    private String opLocation;

    @ApiModelProperty("请求参数")
    @ExcelProperty("请求参数")
    private String paramData;

    @ApiModelProperty("返回参数")
    @ExcelProperty("返回参数")
    private String resultData;

    @ApiModelProperty("操作状态（0正常 1异常）")
    @ExcelProperty("操作状态")
    private Integer status;

    @ApiModelProperty("错误消息")
    @ExcelProperty("错误消息")
    private String errorMsg;

    @ApiModelProperty("操作时间")
    @ExcelProperty("操作时间")
    private LocalDateTime opTime;


}

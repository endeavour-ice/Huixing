package com.ice.hxy.mode.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 图片
 * </p>
 *
 * @author ice
 * @since 2023-05-05
 */
@ApiModel(value = "Image对象", description = "图片")
@Data
public class Image implements Serializable {

    private static final long serialVersionUID = 1L;
    @TableId(type=IdType.AUTO)
    private Long id;

    @ApiModelProperty("图片url")
    private String imageUrl;
    private String name;
    @ApiModelProperty("下载数")
    private Long downloadNum;
    @ApiModelProperty("修改时间")
    private LocalDateTime updateTime;

    private LocalDateTime createTime;

    @ApiModelProperty("是否删除")
    private Byte isDelete;
}

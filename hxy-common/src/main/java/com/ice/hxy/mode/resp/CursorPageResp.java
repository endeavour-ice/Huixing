package com.ice.hxy.mode.resp;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author ice
 * @Date 2023/5/29 19:10
 * @Description: 游标返回的数据
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CursorPageResp<T> {
    @ApiModelProperty("游标（下次翻页带上这参数）")
    private Long cursor;

    @ApiModelProperty("是否最后一页")
    private Boolean isLast = Boolean.FALSE;

    @ApiModelProperty("数据列表")
    private T data;

}

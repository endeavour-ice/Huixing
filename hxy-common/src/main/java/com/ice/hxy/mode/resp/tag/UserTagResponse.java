package com.ice.hxy.mode.resp.tag;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ice
 * @date 2022/9/16 22:43
 */
@Data
public class UserTagResponse implements Serializable {
    private static final long serialVersionUID = -5798581433074712514L;

    @ApiModelProperty("标签类型")
    private String type;

    @ApiModelProperty("标签")
    private List<tag> tagList=new ArrayList<>();
    @Data
   public static class tag{
        private Long id;
        private String tag;
    }
}

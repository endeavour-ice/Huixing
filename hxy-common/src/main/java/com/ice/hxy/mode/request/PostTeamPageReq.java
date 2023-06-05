package com.ice.hxy.mode.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author ice
 * @Date 2023/5/30 16:47
 * @Description: 队伍文章
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PostTeamPageReq extends PostPageReq{
    private static final long serialVersionUID = -383485376099415623L;
    private Long tid;

}

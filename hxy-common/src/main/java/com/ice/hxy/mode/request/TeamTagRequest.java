package com.ice.hxy.mode.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author ice
 * @Date 2023/5/28 10:26
 * @Description: TODO
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TeamTagRequest extends TagRequest {
    private Long teamId;
}

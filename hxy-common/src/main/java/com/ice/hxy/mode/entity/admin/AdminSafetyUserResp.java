package com.ice.hxy.mode.entity.admin;

import com.ice.hxy.mode.resp.SafetyUserResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author ice
 * @Date 2023/7/23 11:04
 * @Description: TODO
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AdminSafetyUserResp extends SafetyUserResponse {
    private static final long serialVersionUID = 2112947811244272097L;
    private String role;
}

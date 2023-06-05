package com.ice.hxy.mode.request;

import com.ice.hxy.mode.dto.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * @Author ice
 * @Date 2023/4/22 19:05
 * @Description: TODO
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class LogRequest extends PageRequest {
    private static final long serialVersionUID = -1658162159521920328L;
    private Long exTime=0L;
    private String name;
    private boolean error = false;
    private LocalDateTime opTime;
}

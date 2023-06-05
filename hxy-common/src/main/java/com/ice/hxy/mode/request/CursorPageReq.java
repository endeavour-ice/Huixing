package com.ice.hxy.mode.request;

import com.ice.hxy.util.LongUtil;
import lombok.Data;

/**
 * @Author ice
 * @Date 2023/5/29 18:38
 * @Description: 聊天记录游标
 */
@Data
public class CursorPageReq {
    // 类型
    private Long id;
    // 大小
    private Integer count = 20;
    // 从什么时间开始找
    private Long cursor;

    public static boolean isEmpty(CursorPageReq cursorPageReq) {
        if (cursorPageReq == null) {
            return true;
        }
        if (cursorPageReq.getCount() != 20) {
            return true;
        }
        return LongUtil.isEmpty(cursorPageReq.getId());
    }
}

package com.ice.hxy.mode.request;

import com.ice.hxy.mode.dto.PageRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

/**
 * @Author ice
 * @Date 2023/5/30 10:53
 * @Description: 文章
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostPageReq extends PageRequest {
    private static final long serialVersionUID = -3102177518380062134L;
    // 类型
    private String scope;

    public static boolean isEmpty(PostPageReq postPageReq) {
        if (postPageReq == null) {
            return true;
        }
        return !StringUtils.hasText(postPageReq.getScope());
    }
}

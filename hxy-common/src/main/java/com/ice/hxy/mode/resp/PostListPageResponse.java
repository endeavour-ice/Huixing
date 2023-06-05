package com.ice.hxy.mode.resp;

import com.ice.hxy.mode.entity.vo.PostVo;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author ice
 * @Date 2023/5/18 19:41
 * @Description: postList返回值
 */
@Data
@Builder
public class PostListPageResponse implements Serializable {
    private static final long serialVersionUID = 5420818612475562993L;
    private long current;
    private long size;
    private List<PostVo> records;
    private long total;

}

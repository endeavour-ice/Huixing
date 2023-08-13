package com.ice.hxy.mode.resp.tag;

import lombok.Data;

import java.util.List;

/**
 * @Author ice
 * @Date 2023/7/30 16:01
 * @Description: TODO
 */
@Data
public class TagResp {
    private Long id;
    private String key;
    private String title;
    private List<TagResp> children;

    public TagResp(Long id, String title,String key) {
        this.id = id;
        this.title = title;
        this.key = key;
    }

    public TagResp() {
    }
}

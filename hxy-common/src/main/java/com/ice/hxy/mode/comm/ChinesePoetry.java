package com.ice.hxy.mode.comm;

import lombok.Data;

import java.util.List;

/**
 * @Author ice
 * @Date 2023/5/27 16:52
 * @Description: TODO
 */
@Data
public class ChinesePoetry {
    private String author;
    private List<String> paragraphs;
    private String rhythmic;

    @Override
    public String toString() {
        return rhythmic+" 作者:"+author+" "+String.join(" ",paragraphs);
    }
}

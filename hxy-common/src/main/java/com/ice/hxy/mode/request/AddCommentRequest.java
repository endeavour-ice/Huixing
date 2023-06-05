package com.ice.hxy.mode.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author ice
 * @Date 2023/2/16 10:03
 * @Description: TODO
 */
@Data
public class AddCommentRequest implements Serializable {

    private static final long serialVersionUID = 9029128488392994120L;
    private Long postId;
    private Long userId;
    private String content;
}

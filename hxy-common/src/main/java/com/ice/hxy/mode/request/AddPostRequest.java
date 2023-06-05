package com.ice.hxy.mode.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author ice
 * @Date 2023/2/13 21:37
 * @Description: TODO
 */
@Data
public class AddPostRequest implements Serializable {
    private static final long serialVersionUID = 1578557702138636466L;

    private String content;

    private Long groupId;

    private String tags;

}

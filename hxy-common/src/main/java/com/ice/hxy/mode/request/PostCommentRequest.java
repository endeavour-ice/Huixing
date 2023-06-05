package com.ice.hxy.mode.request;

import com.ice.hxy.mode.dto.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @Author ice
 * @Date 2023/2/15 16:52
 * @Description: TODO
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PostCommentRequest extends PageRequest implements Serializable {
    private static final long serialVersionUID = 4713058028102563717L;

    private String postId;

}

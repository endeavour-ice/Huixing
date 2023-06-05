package com.ice.hxy.designPatten.filter.article.FilterBase;

import com.ice.hxy.mode.request.AddCommentRequest;
import com.ice.hxy.mode.request.AddPostRequest;
import lombok.Data;

/**
 * @Author ice
 * @Date 2023/2/23 18:03
 * @Description: TODO
 */
@Data
public class ArticleContext {
    // 文章
    private AddPostRequest request;
    // 评论
    private AddCommentRequest addCommentRequest;
}

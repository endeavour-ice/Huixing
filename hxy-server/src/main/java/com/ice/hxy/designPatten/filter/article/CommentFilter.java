package com.ice.hxy.designPatten.filter.article;


import com.ice.hxy.designPatten.filter.article.FilterBase.ArticleContext;
import com.ice.hxy.designPatten.filter.article.FilterBase.BaseArticleFilter;
import com.ice.hxy.mode.request.AddCommentRequest;
import com.ice.hxy.util.SensitiveUtils;

/**
 * @Author ice
 * @Date 2023/5/6 17:01
 * @Description: 文章过滤
 */
public class CommentFilter extends BaseArticleFilter {
    @Override
    public boolean doFilter(ArticleContext articleContext) throws Exception {
        AddCommentRequest addCommentRequest = articleContext.getAddCommentRequest();
        if (addCommentRequest != null) {
            String content = addCommentRequest.getContent();
            addCommentRequest.setContent(SensitiveUtils.sensitive(content));
        }
        return true;
    }
}

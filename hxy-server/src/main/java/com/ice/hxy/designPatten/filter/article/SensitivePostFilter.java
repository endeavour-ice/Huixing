package com.ice.hxy.designPatten.filter.article;

import com.ice.hxy.designPatten.filter.article.FilterBase.ArticleContext;
import com.ice.hxy.designPatten.filter.article.FilterBase.BaseArticleFilter;
import com.ice.hxy.mode.request.AddPostRequest;
import com.ice.hxy.util.SensitiveUtils;
/**
 * @Author ice
 * @Date 2023/2/23 18:10
 * @Description: TODO
 */
public class SensitivePostFilter extends BaseArticleFilter {
    @Override
    public boolean doFilter(ArticleContext articleContext) {
        AddPostRequest request = articleContext.getRequest();
        if (request != null) {
            String content = request.getContent();
            request.setContent(SensitiveUtils.sensitive(content));
        }
        return true;
    }
}

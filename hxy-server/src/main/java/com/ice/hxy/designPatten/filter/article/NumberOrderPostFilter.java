package com.ice.hxy.designPatten.filter.article;

import com.ice.hxy.designPatten.filter.article.FilterBase.ArticleContext;
import com.ice.hxy.designPatten.filter.article.FilterBase.BaseArticleFilter;

/**
 * @Author ice
 * @Date 2023/2/23 18:45
 * @Description: TODO
 */
public class NumberOrderPostFilter extends BaseArticleFilter {
    @Override
    public boolean doFilter(ArticleContext articleContext) {
        if (articleContext.getRequest()!=null) {
            int length = articleContext.getRequest().getContent().length();
            return length >= 5 && length <= 2000;
        }
        return true;
    }
}

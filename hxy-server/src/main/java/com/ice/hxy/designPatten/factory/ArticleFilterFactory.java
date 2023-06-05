package com.ice.hxy.designPatten.factory;

import com.ice.hxy.designPatten.filter.article.CommentFilter;
import com.ice.hxy.designPatten.filter.article.FilterBase.ArticleFilterInterFace;
import com.ice.hxy.designPatten.filter.article.FilterBase.IArticleFilter;
import com.ice.hxy.designPatten.filter.article.NumberOrderPostFilter;
import com.ice.hxy.designPatten.filter.article.SensitivePostFilter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author ice
 * @Date 2023/2/23 18:16
 * @Description: 过滤器工厂
 */
public class ArticleFilterFactory {
    private static final Map<Integer, IArticleFilter> articleFilterMap = new HashMap<Integer, IArticleFilter>() {
        private static final long serialVersionUID = -8111296656606366039L;
        {
            put(ArticleFilterInterFace.NUMBER, new NumberOrderPostFilter());
            put(ArticleFilterInterFace.SENSITIVE, new SensitivePostFilter());
            put(ArticleFilterInterFace.COMMENTFILTER, new CommentFilter());
        }
    };

    public static List<IArticleFilter> createArticleFilter() {
        List<IArticleFilter> articleFilters = null;
        if (!articleFilterMap.isEmpty()) {
            articleFilters = new ArrayList<>(articleFilterMap.values());
        }
        return articleFilters;
    }

}

package com.ice.hxy.designPatten.filter.article;

import com.ice.hxy.designPatten.factory.ArticleFilterFactory;
import com.ice.hxy.designPatten.filter.article.FilterBase.ArticleContext;
import com.ice.hxy.designPatten.filter.article.FilterBase.IArticleFilter;
import com.ice.hxy.mode.request.AddCommentRequest;
import com.ice.hxy.mode.request.AddPostRequest;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author ice
 * @Date 2023/2/23 18:55
 * @Description: 过滤器
 */
public class FilterEntrance {
    private FilterEntrance() {
    }

    private static List<IArticleFilter> articleFilters;
    static {
        init();
    }
    private static void init() {
        articleFilters = ArticleFilterFactory.createArticleFilter();
    }



    /**
     *
     * @param request 要过滤后的文章
     * @param addCommentRequest 要过滤的评论
     * @return
     * @throws Exception
     */
    public static boolean doFilter(AddPostRequest request,AddCommentRequest addCommentRequest) throws Exception {
        ArticleContext articleContext = new ArticleContext();
        articleContext.setAddCommentRequest(addCommentRequest);
        articleContext.setRequest(request);
        return doFilter(articleContext);
    }
    public static List<AddPostRequest> doFilter(List<AddPostRequest> requests) {
        return requests.stream().filter(request -> {
            try {
                return doFilter(request, null);
            } catch (Exception ignored) {

            }
            return true;
        }).collect(Collectors.toList());
    }
    private  static boolean doFilter(ArticleContext articleContext) throws Exception {
        for (IArticleFilter articleFilter : articleFilters) {
            if (!articleFilter.doFilter(articleContext)) {
                return false;
            }
        }
        return true;
    }
}

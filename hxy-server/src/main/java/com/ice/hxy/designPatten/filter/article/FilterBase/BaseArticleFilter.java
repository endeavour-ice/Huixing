package com.ice.hxy.designPatten.filter.article.FilterBase;

import lombok.Data;

/**
 * @Author ice
 * @Date 2023/2/23 18:05
 * @Description: TODO
 */
@Data
public abstract class BaseArticleFilter implements IArticleFilter{

    /**
     * 可以自由进行扩展，列如文章的过滤
     */
    void sendMsg() {
        // 发送消息通知
    }

}

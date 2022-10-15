package com.heima.article.service;

import com.heima.model.user.pojos.ApArticle;

public interface ArticleFreemarkerService {
    /**
     * 生成文章内容的详情页面，上传到minio
     * @param apArticle
     * @param content
     */
    public void buildArticleToMinIO(ApArticle apArticle, String content);
}

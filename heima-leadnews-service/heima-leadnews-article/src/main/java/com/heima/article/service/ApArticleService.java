package com.heima.article.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.common.dtos.ResponseResult;
import com.heima.model.article.dtos.ApArticleDto;
import com.heima.model.user.dtos.ArticleDto;
import com.heima.model.user.pojos.ApArticle;

public interface ApArticleService extends IService<ApArticle> {
    ResponseResult loadApArticle(ArticleDto dto, int i);

    ResponseResult saveOrUpdateApArticle(ApArticleDto dto);
}

package com.heima.article.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.heima.model.user.dtos.ArticleDto;
import com.heima.model.user.pojos.ApArticle;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ApArticleMapper extends BaseMapper<ApArticle> {
    List<ApArticle> loadApArticle(@Param("dto") ArticleDto dto, @Param("type") int type);
}

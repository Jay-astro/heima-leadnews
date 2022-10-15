package com.heima.model.article.dtos;

import com.heima.model.user.pojos.ApArticle;
import lombok.Data;

/**
 * 在自媒体端传到App的数据对象
 */
@Data
public class ApArticleDto extends ApArticle {
    private String content;
}

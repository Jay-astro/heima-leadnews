package com.heima.model.search.dtos;

import com.heima.model.search.pojos.ArticleDoc;
import lombok.Data;

@Data
public class ArticleDocumentVo extends ArticleDoc {
    private String h_title;//高亮标题
}

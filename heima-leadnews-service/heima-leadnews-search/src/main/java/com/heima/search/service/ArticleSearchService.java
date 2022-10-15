package com.heima.search.service;

import com.heima.common.dtos.ResponseResult;
import com.heima.model.search.dtos.UserSearchDto;

public interface ArticleSearchService {
    ResponseResult search(UserSearchDto dto);

    void saveToEs(Long articleId);

    void removeFromES(Long articleId);
}

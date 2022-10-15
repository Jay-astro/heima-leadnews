package com.heima.search.service;

import com.heima.common.dtos.ResponseResult;
import com.heima.model.search.dtos.HistorySearchDto;

public interface ApUserSearchService {
    /**
     * 保存用户搜索记录
     */
    public void insert(Integer userId,String keyword);

    ResponseResult load();

    ResponseResult del(HistorySearchDto dto);
}

package com.heima.search.controller.v1;

import com.heima.common.dtos.ResponseResult;
import com.heima.model.search.dtos.UserSearchDto;
import com.heima.search.service.ArticleSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/article/search")
public class ApArticleSearchController {
    @Autowired
    private ArticleSearchService articleSearchService;

    /**
     *
     * @param dto
     * @return
     */
    @PostMapping("/search")
    public ResponseResult search(@RequestBody UserSearchDto dto){
        return articleSearchService.search(dto);

    }

}

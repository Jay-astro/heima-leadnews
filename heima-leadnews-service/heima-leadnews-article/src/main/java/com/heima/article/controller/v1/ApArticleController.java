package com.heima.article.controller.v1;

import com.heima.article.service.ApArticleService;
import com.heima.common.dtos.ResponseResult;
import com.heima.model.article.dtos.ApArticleDto;
import com.heima.model.user.dtos.ArticleDto;
import com.heima.model.user.pojos.ApArticle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/article")
public class ApArticleController {
    @Autowired
    private ApArticleService apArticleService;

    /**
     * 首页文章
     *
     * @param dto
     * @return
     */
    @PostMapping("/load")
    public ResponseResult load(@RequestBody ArticleDto dto) {
        return apArticleService.loadApArticle(dto, 1);
    }

    /**
     * 更多文章
     *
     * @param dto
     * @return
     */
    @PostMapping("/loadmore")
    public ResponseResult loadmore(@RequestBody ArticleDto dto) {
        return apArticleService.loadApArticle(dto, 1);
    }

    /**
     * 更新文章
     *
     * @param dto
     * @return
     */
    @PostMapping("/loadnew")
    public ResponseResult loadnew(@RequestBody ArticleDto dto) {
        return apArticleService.loadApArticle(dto, 2);
    }

    /**
     * 保存App文章
     * @param dto
     * @return
     */
    @PostMapping("/save")
    public ResponseResult save(@RequestBody ApArticleDto dto){
        return apArticleService.saveOrUpdateApArticle(dto);
    }

    /**
     * 查询所有App文章
     * @return
     */
    @GetMapping("findAllApArticles")
    public List<ApArticle> findAllApArticles(){

        return apArticleService.list();
    }

    /**
     * 根据ID查询文章
     */
    @GetMapping("/findByArticleId/{id}")
    public ResponseResult findByArticleId(@PathVariable("id") Long id){
        return ResponseResult.okResult(apArticleService.getById(id));
    }
}

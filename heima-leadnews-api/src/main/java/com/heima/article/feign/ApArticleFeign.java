package com.heima.article.feign;

import com.heima.common.dtos.ResponseResult;
import com.heima.model.article.dtos.ApArticleDto;
import com.heima.model.user.pojos.ApArticle;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;


/**
 * name:调用服务的名称
 * path:公共地址
 */
@FeignClient(name = "leadnews-article",path = "/api/v1/article")
public interface ApArticleFeign {
    /**
     * Feign接口非常注意的细节：
     * 1）检测feign接口方法路径是否完整（参考提供方）
     * 2）Feign的所有参数都必须添加注解
     * 3）Feign接口的方法有返回值必须带泛型
     */


    /**
     * 新增App文章
     */
    @PostMapping("/save")
    public ResponseResult<Long> save(@RequestBody ApArticleDto dto);

    /**
     * 查询所有App文章
     * @return
     */
    @GetMapping("findAllApArticles")
    public List<ApArticle> findAllApArticles();

    /**
     * 根据ID查询文章
     */
    @GetMapping("/findByArticleId/{id}")
    public ResponseResult<ApArticle> findByArticleId(@PathVariable("id") Long id);
}

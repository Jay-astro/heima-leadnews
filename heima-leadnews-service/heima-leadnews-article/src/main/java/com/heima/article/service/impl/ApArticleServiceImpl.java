package com.heima.article.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.article.mapper.ApArticleConfigMapper;
import com.heima.article.mapper.ApArticleContentMapper;
import com.heima.article.mapper.ApArticleMapper;
import com.heima.article.service.ApArticleService;
import com.heima.article.service.ArticleFreemarkerService;
import com.heima.common.dtos.ResponseResult;
import com.heima.model.article.dtos.ApArticleDto;
import com.heima.model.user.dtos.ArticleDto;
import com.heima.model.user.pojos.ApArticle;
import com.heima.model.user.pojos.ApArticleConfig;
import com.heima.model.user.pojos.ApArticleContent;
import com.heima.utils.common.BeanHelper;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Transactional
public class ApArticleServiceImpl extends ServiceImpl<ApArticleMapper, ApArticle> implements ApArticleService {

    @Autowired
    private ApArticleMapper apArticleMapper;

    @Autowired
    private ApArticleConfigMapper apArticleConfigMapper;

    @Autowired
    private ApArticleContentMapper apArticleContentMapper;

    @Autowired
    private ArticleFreemarkerService articleFreemarkerService;


    @Override
    public ResponseResult<List<ApArticle>> loadApArticle(ArticleDto dto, int type) {
        //校验参数
        if(dto.getMinBehotTime()==null) dto.setMinBehotTime(new Date());
        if(dto.getMaxBehotTime()==null) dto.setMaxBehotTime(new Date());
        if(dto.getSize()==null) dto.setSize(10);

        List<ApArticle> articleList = apArticleMapper.loadApArticle(dto,type);
        return ResponseResult.okResult(articleList);
    }

    @Override
    public ResponseResult saveOrUpdateApArticle(ApArticleDto dto) {

        ApArticle apArticle = BeanHelper.copyProperties(dto, ApArticle.class);

        //判断新增或修改
        if (dto.getId() == null){
            //新增
            save(apArticle);

            ApArticleConfig apArticleConfig = new ApArticleConfig();
            apArticleConfig.setArticleId(apArticle.getId());
            apArticleConfig.setIsComment(true);
            apArticleConfig.setIsForward(true);
            apArticleConfig.setIsDown(false);
            apArticleConfig.setIsDelete(false);
            apArticleConfigMapper.insert(apArticleConfig);

            ApArticleContent apArticleContent = new ApArticleContent();
            apArticleContent.setArticleId(apArticle.getId());
            apArticleContent.setContent(dto.getContent());
            apArticleContentMapper.insert(apArticleContent);
        }else {
            //修改
            updateById(apArticle);

            QueryWrapper<ApArticleContent> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("article_id",apArticle.getId());
            ApArticleContent apArticleContent = apArticleContentMapper.selectOne(queryWrapper);
            apArticleContent.setContent(dto.getContent());
            apArticleContentMapper.updateById(apArticleContent);
        }
        //文章静态化
        articleFreemarkerService.buildArticleToMinIO(apArticle, dto.getContent());
        //返回文章Id
        return ResponseResult.okResult(apArticle.getId());
    }
}

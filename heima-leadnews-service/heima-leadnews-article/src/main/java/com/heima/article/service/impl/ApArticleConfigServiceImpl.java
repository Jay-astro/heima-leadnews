package com.heima.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.article.mapper.ApArticleConfigMapper;
import com.heima.article.service.ApArticleConfigService;
import com.heima.model.user.pojos.ApArticleConfig;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ApArticleConfigServiceImpl extends ServiceImpl<ApArticleConfigMapper, ApArticleConfig> implements ApArticleConfigService {
    @Override
    public void updateIsDown(Map<String, Object> msg) {
        Long articleId = (Long) msg.get("articleId");
        Integer enable = (Integer) msg.get("enable");

        UpdateWrapper updateWrapper = new UpdateWrapper();
        updateWrapper.eq("article_id",articleId);
        updateWrapper.set("is_down",enable==1?0:1);
        update(updateWrapper);
    }
}

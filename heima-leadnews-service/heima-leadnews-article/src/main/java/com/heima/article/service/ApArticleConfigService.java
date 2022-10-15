package com.heima.article.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.user.pojos.ApArticleConfig;

import java.util.Map;

public interface ApArticleConfigService extends IService<ApArticleConfig> {

    void updateIsDown(Map<String, Object> msg);
}

package com.heima.search.listener;

import com.heima.common.constants.MQConstants;
import com.heima.search.service.ArticleSearchService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ArticleDocumentListener {
    @Autowired
    private ArticleSearchService articleSearchService;

    @KafkaListener(topics = MQConstants.WM_NEWS_UP_OR_DOWN_TOPIC)
    public void handlerUpES(String articleId){
        log.info("文章发布上架后导入ES数据...");
        if (StringUtils.isNotEmpty(articleId)){
            articleSearchService.saveToEs(Long.valueOf(articleId));
        }
    }


    /**
     * 文章下架后移除ES数据
     */
    @KafkaListener(topics = MQConstants.WM_NEW_DOWN_ES_TOPIC)
    public void handlerDownES(String articleId){
        log.info("文章下架后移除ES数据...");
        if(StringUtils.isNotEmpty(articleId)){
            articleSearchService.removeFromES(Long.valueOf(articleId));
        }

    }
}

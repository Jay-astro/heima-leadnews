package com.heima.article.listener;

import com.heima.article.service.ApArticleConfigService;
import com.heima.common.constants.MQConstants;
import com.heima.model.user.pojos.ApArticleConfig;
import com.heima.utils.common.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
/**
 * App文章上下架监听
 */
public class ArtilceDownOrUpListener {

    @Autowired
    private ApArticleConfigService apArticleConfigService;


    @KafkaListener(topics = MQConstants.WM_NEWS_UP_OR_DOWN_TOPIC)
    public void handleDownOrUp(String value){
        log.info("触发App端文章上下架监听,{}",value);
        if (StringUtils.isNotEmpty(value)){
            Map<String, Object> msg = JsonUtils.toMap(value, String.class, Object.class);
            apArticleConfigService.updateIsDown(msg);
        }
    }
}

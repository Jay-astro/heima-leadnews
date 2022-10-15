package com.heima.wemedia.job;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.heima.common.constants.ScheduleConstants;
import com.heima.model.schedule.dtos.Task;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.schedule.feign.TaskFeign;
import com.heima.utils.common.JsonUtils;
import com.heima.wemedia.mapper.WmNewsMapper;
import com.heima.wemedia.service.WmNewsAutoScanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class WmNewsTaskJob {
    @Autowired
    private TaskFeign taskFeign;
    @Autowired
    private WmNewsAutoScanService wmNewsAutoScanService;
    @Autowired
    private WmNewsMapper wmNewsMapper;


    @Scheduled(fixedDelay = 1000)
    public void pollWmNewsTask() {
        List<Task> taskList = taskFeign.pollTask(ScheduleConstants.TASK_TOPIC_NEWS_PUBLISH);
        if (CollectionUtils.isNotEmpty(taskList)) {
            for (Task task : taskList) {
                String json = task.getParameters();
                WmNews wmNews = JsonUtils.toBean(json, WmNews.class);

                wmNews = wmNewsMapper.selectById(wmNews.getId());
                wmNewsAutoScanService.publishApArticle(wmNews);
                log.info("文章已经成功发布到App端，ID：{}", wmNews.getId());
            }
        }
    }
}

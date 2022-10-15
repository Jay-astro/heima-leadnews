package com.heima.wemedia.service.impl;

import com.heima.common.constants.ScheduleConstants;
import com.heima.model.schedule.dtos.Task;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.schedule.feign.TaskFeign;
import com.heima.utils.common.JsonUtils;
import com.heima.wemedia.service.WmNewsTaskService;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;

public class WmNewsTaskServiceImpl implements WmNewsTaskService {
    @Autowired
    private TaskFeign taskFeign;
    @Override
    public Long addWmNewsTask(WmNews wmNews) {
        Task task = new Task();
        task.setTaskTopic(ScheduleConstants.TASK_TOPIC_NEWS_PUBLISH);
        task.setExecuteTime(wmNews.getPublishTime().getTime());

        WmNews news = new WmNews();
        news.setId(wmNews.getId());

        task.setParameters(JsonUtils.toString(news));
        Long taskId = taskFeign.addTask(task);
        return taskId;
    }
}

package com.heima.wemedia.service;

import com.heima.model.wemedia.pojos.WmNews;

public interface WmNewsTaskService {
    /**
     * 添加自媒体定时发布延迟任务
     */
    public Long addWmNewsTask(WmNews wmNews);
}

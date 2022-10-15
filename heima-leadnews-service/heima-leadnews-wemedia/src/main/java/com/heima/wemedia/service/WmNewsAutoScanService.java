package com.heima.wemedia.service;

import com.heima.model.wemedia.pojos.WmNews;

public interface WmNewsAutoScanService{
    public void autoScanWmNews(Integer id);

    public void publishApArticle(WmNews wmNews);
}

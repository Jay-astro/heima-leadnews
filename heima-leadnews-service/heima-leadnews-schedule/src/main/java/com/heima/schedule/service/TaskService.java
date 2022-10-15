package com.heima.schedule.service;

import com.heima.model.schedule.dtos.Task;

import java.util.List;

/**
 * 任务处理业务
 */
public interface TaskService {
    /**
     * 添加任务
     * @param task
     * @return
     */
    public long addTask(Task task);

    /**
     * 从延迟队列消费任务
     *  重点：从延迟队列取出符合条件（根据score查询，score小于或等于当前时间毫秒值）
     */
    public List<Task> pollTask(Integer taskTopic);
}

package com.heima.schedule.feign;

import com.heima.model.schedule.dtos.Task;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "leadnews-schedule",path = "/task")
public interface TaskFeign {
    /**
     * 添加延迟任务
     * 返回值：新增的任务ID （任务ID可以用作取消任务）
     * 参数：Task对象（封装延迟任务数据）
     */
    @PostMapping("/addTask")
    public Long addTask(@RequestBody Task task);

    /**
     * 消费延迟任务（该方法会每隔1秒执行1次）
     * 返回值：到期执行的任务列表
     * 参数：任务主题ID
     */
    @PostMapping("/pollTask/{taskTopic}")
    public List<Task> pollTask(@PathVariable("taskTopic") Integer taskTopic);
}

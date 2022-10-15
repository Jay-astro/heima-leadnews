package com.heima.schedule.controller;

import com.heima.model.schedule.dtos.Task;
import com.heima.schedule.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/task")
public class TaskController {
    @Autowired
    private TaskService taskService;


    /**
     * 添加延迟任务
     * 返回值：新增的任务ID （任务ID可以用作取消任务）
     * 参数：Task对象（封装延迟任务数据）
     */
    @PostMapping("/addTask")
    public Long addTask(@RequestBody Task task){
        return taskService.addTask(task);
    }

    /**
     * 消费延迟任务（该方法会每隔1秒执行1次）
     * 返回值：到期执行的任务列表
     * 参数：任务主题ID
     */
    @PostMapping("/pollTask/{taskTopic}")
    public List<Task> pollTask(@PathVariable("taskTopic") Integer taskTopic){
        return taskService.pollTask(taskTopic);
    }
}

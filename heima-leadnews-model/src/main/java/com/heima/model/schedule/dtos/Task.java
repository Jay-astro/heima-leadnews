package com.heima.model.schedule.dtos;

import lombok.Data;

@Data
public class Task{

    /**
     * 任务id
     */
    private Long taskId;

    /**
     * 类型
     */
    private Integer taskTopic;

    /**
     * 执行时间
     */
    private long executeTime;

    /**
     * task参数
     */
    private String parameters;

}

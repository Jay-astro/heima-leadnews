package com.heima.schedule.service.impl;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.heima.common.constants.RedisConstants;
import com.heima.common.constants.RedisContants;
import com.heima.common.constants.ScheduleConstants;
import com.heima.model.schedule.dtos.Task;
import com.heima.model.schedule.pojos.Taskinfo;
import com.heima.model.schedule.pojos.TaskinfoLogs;
import com.heima.schedule.mapper.TaskinfoLogsMapper;
import com.heima.schedule.mapper.TaskinfoMapper;
import com.heima.schedule.service.TaskService;
import com.heima.utils.common.BeanHelper;
import com.heima.utils.common.JsonUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Service
public class TaskServiceImpl implements TaskService {

    @Autowired
    private TaskinfoMapper taskinfoMapper;

    @Autowired
    private TaskinfoLogsMapper taskinfoLogsMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;


    @Override
    public long addTask(Task task) {

        addTaskToDb(task);
        addTaskToCache(task);
        return task.getTaskId();
    }


    /**
     * 添加任务至redis
     *
     * @param task
     */
    private void addTaskToCache(Task task) {
        long futureTime = DateTime.now().plusMinutes(5).getMillis();
        if (task.getExecuteTime() <= futureTime) {
            String key = RedisConstants.TASK_TOPIC_PREFIX + task.getTaskTopic();
            redisTemplate.opsForZSet().add(key, JsonUtils.toString(task), task.getExecuteTime());
        }
    }

    /**
     * 把任务 添加到DB
     *
     * @param task
     */
    private void addTaskToDb(Task task) {
        try {
            Taskinfo taskinfo = BeanHelper.copyProperties(task, Taskinfo.class);
            taskinfo.setExecuteTime(new Date(task.getExecuteTime()));
            taskinfoMapper.insert(taskinfo);

            //把新产生的任务ID赋值给Task对象
            task.setTaskId(taskinfo.getTaskId());

            //添加任务日志
            TaskinfoLogs taskinfoLogs = BeanHelper.copyProperties(taskinfo, TaskinfoLogs.class);
            taskinfoLogs.setVersion(1);
            taskinfoLogs.setStatus(ScheduleConstants.SCHEDULED);
            taskinfoLogsMapper.insert(taskinfoLogs);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Task> pollTask(Integer taskTopic) {
        String key = RedisConstants.TASK_TOPIC_PREFIX + taskTopic;
        //查询redis中符合执行条件的任务
        Set<String> taskSet = redisTemplate.opsForZSet().rangeByScore(key, 0, System.currentTimeMillis());

        List<Task> taskList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(taskList)){
            for (String taskJson : taskSet) {
                Task task = JsonUtils.toBean(taskJson, Task.class);

                //update DB
                updateTaskToDB(task);

                //delete redis data
                redisTemplate.opsForZSet().remove(key,taskJson);

                taskList.add(task);
            }
        }
        return taskList;
    }

    /**
     * update DB
     * @param task
     */
    private void updateTaskToDB(Task task) {
        try {
            //delete table of mission
            taskinfoMapper.deleteById(task.getTaskId());

            //Update the task log table
            TaskinfoLogs taskinfoLogs = taskinfoLogsMapper.selectById(task.getTaskId());
            taskinfoLogs.setStatus(ScheduleConstants.EXECUTED);//has been execute
            taskinfoLogsMapper.updateById(taskinfoLogs);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }


    }
}

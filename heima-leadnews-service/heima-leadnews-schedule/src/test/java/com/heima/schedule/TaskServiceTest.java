package com.heima.schedule;

import com.heima.common.constants.ScheduleConstants;
import com.heima.model.schedule.dtos.Task;
import com.heima.schedule.service.TaskService;
import com.heima.utils.common.JsonUtils;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ScheduleApplication.class)
public class TaskServiceTest {

    @Autowired
    private TaskService taskService;

    @Test
    public void testAddTask() {
        for (int i = 1; i <= 10; i++) {
            //模拟文章定时发布
            Task task = new Task();
            task.setTaskTopic(ScheduleConstants.TASK_TOPIC_NEWS_PUBLISH);
            Map map = new HashMap<>();
            map.put("id", 6294);
            task.setParameters(JsonUtils.toString(map));
            task.setExecuteTime(DateTime.now().plusMinutes(i).getMillis());
            taskService.addTask(task);
        }
    }


    /**
     * 消费延迟任务
     */
    @Test
    public void testPollTask(){
        List<Task> taskList =  taskService.pollTask(1);

    }
}

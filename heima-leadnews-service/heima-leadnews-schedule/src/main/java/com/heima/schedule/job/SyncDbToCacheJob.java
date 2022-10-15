package com.heima.schedule.job;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.heima.common.constants.RedisConstants;
import com.heima.model.schedule.dtos.Task;
import com.heima.model.schedule.pojos.Taskinfo;
import com.heima.schedule.mapper.TaskinfoMapper;
import com.heima.utils.common.BeanHelper;
import com.heima.utils.common.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * 定时同步MySQL任务数据到Redis缓存
 */
@Component
@Slf4j
public class SyncDbToCacheJob {
    @Autowired
    private TaskinfoMapper taskinfoMapper;
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Scheduled(fixedDelay = 10000)
    public void SyncData(){
        log.info("同步MySQL任务数据到Redis缓存");
        //查询符合条件的DB数据
        QueryWrapper<Taskinfo> queryWrapper = new QueryWrapper<>();
        Date futureTime = DateTime.now().plusMinutes(5).toDate();
        queryWrapper.le("execute_time",futureTime);
        List<Taskinfo> taskinfos = taskinfoMapper.selectList(queryWrapper);

        //导入到Redis缓存
        if (CollectionUtils.isNotEmpty(taskinfos)){
            for (Taskinfo taskinfo : taskinfos) {
                Task task = BeanHelper.copyProperties(taskinfo, Task.class);
                task.setExecuteTime(taskinfo.getExecuteTime().getTime());
                String key = RedisConstants.TASK_TOPIC_PREFIX + task.getTaskTopic();
                redisTemplate.opsForZSet().add(key, JsonUtils.toString(task),task.getExecuteTime());
            }
        }
    }
}

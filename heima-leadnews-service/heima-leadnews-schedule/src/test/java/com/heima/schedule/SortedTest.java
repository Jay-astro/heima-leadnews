package com.heima.schedule;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Random;
import java.util.Set;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ScheduleApplication.class)
public class SortedTest {
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    public void testAdd(){
        Random random = new Random();
        for (int i = 1; i <= 20 ; i++) {
            redisTemplate.opsForZSet().add("hello","jack:"+i,random.nextInt(50));
        }
    }

    /**
     * 查询SortedSet数据
     */
    @Test
    public void testRange(){
        //按下标位置查询
        /**
         * 参数一：key
         * 参数二：起始下标
         * 参数三：结束下标
         */
        Set<String> set = redisTemplate.opsForZSet().range("hello", 5, 10);
        System.out.println(set);

        /**
         * 按score值查询
         */
        /**
         * 参数一：key
         * 参数二：起始score值
         * 参数三：结束score值
         */
        Set<String> set2 = redisTemplate.opsForZSet().rangeByScore("hello",25,35);
        System.out.println(set2);

        /**
         * 查询score<=20
         */
        Set<String> set3 = redisTemplate.opsForZSet().rangeByScore("hello",0,20);
        System.out.println(set3);
    }

    /**
     * 删除SortedSet数据
     */
    @Test
    public void testRemove(){
        //按元素移除
        //redisTemplate.opsForZSet().remove("hello","jack:12");

        //score范围移除
        redisTemplate.opsForZSet().removeRangeByScore("hello",35,45);
    }


}

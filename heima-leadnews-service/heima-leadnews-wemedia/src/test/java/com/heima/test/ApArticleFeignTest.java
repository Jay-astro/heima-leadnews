package com.heima.test;


import com.heima.article.feign.ApArticleFeign;
import com.heima.common.dtos.ResponseResult;
import com.heima.model.article.dtos.ApArticleDto;
import com.heima.wemedia.WemediaApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = WemediaApplication.class)
public class ApArticleFeignTest {
    @Autowired
    private ApArticleFeign apArticleFeign;

    @Test
    public void testSaveApArticle(){
        ApArticleDto dto = new ApArticleDto();
        dto.setId(1551457450111143938L);
        dto.setTitle("测试App文章新增7777");
        dto.setContent("测试App文章新增内容88888");
        dto.setAuthorId(4L);
        dto.setAuthorName("李四");
        dto.setChannelId(1);
        dto.setLayout((short)1);
        dto.setFlag((byte)0);
        dto.setCreatedTime(new Date());
        dto.setPublishTime(new Date());
        ResponseResult<Long> responseResult = apArticleFeign.save(dto);
        if(responseResult.getCode().equals(200)){
            Long articleId = responseResult.getData();
            System.out.println("文章ID："+articleId);
        }
    }
}

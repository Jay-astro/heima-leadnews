package com.heima.article;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.heima.article.mapper.ApArticleContentMapper;
import com.heima.article.mapper.ApArticleMapper;
import com.heima.common.minio.MinIOFileStorageService;
import com.heima.model.user.pojos.ApArticle;
import com.heima.model.user.pojos.ApArticleContent;
import com.heima.utils.common.JsonUtils;
import freemarker.template.Template;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import freemarker.template.Configuration;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ArticleApplication.class)
public class ApArticleTest {
    @Autowired
    private ApArticleContentMapper apArticleContentMapper;
    @Autowired
    private Configuration configuration;
    @Autowired
    private MinIOFileStorageService storageService;
    @Autowired
    private ApArticleMapper apArticleMapper;


    @Test
    public void testCreateStaticPageByApArticle() throws Exception {
        Long id = 1383827787629252610L; //文章ID

        //根据文章ID查询文章数据
        QueryWrapper<ApArticleContent> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("article_id", id);
        ApArticleContent apArticleContent = apArticleContentMapper.selectOne(queryWrapper);

        if(apArticleContent != null && StringUtils.isNoneBlank(apArticleContent.getContent())){
            //读取文章详情页的模板页面
            Template template = configuration.getTemplate("article.ftl");
            //获取数据用于填充
            Map<String, Object> templateData = new HashMap<>();
            List<Map> content = JsonUtils.toList(apArticleContent.getContent(), Map.class);
            templateData.put("content", content);

            //生成静态页面
            StringWriter writer = new StringWriter();//字符串缓存流
            template.process(templateData, writer);//把静态页数据写入StringWriter对象缓存中

            //把静态页数据上传到MinIO，获取文件访问地址url
            String fileName = id + ".html";
            InputStream inputStream = new ByteArrayInputStream(writer.toString().getBytes());
            String url = storageService.uploadHtmlFile(null, fileName, inputStream);

            //把url地址写入文章的static_url字段
            ApArticle apArticle = new ApArticle();
            apArticle.setId(id);
            apArticle.setStaticUrl(url);
            apArticleMapper.updateById(apArticle);
        }
    }
}

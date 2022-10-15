package com.heima.article.service.impl;

import com.heima.article.mapper.ApArticleMapper;
import com.heima.article.service.ArticleFreemarkerService;
import com.heima.common.minio.MinIOFileStorageService;
import com.heima.model.user.pojos.ApArticle;
import com.heima.utils.common.JsonUtils;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ArticleFreemarkerServiceImpl implements ArticleFreemarkerService {
    @Autowired
    private Configuration configuration;
    @Autowired
    private MinIOFileStorageService storageService;
    @Autowired
    private ApArticleMapper apArticleMapper;

    @Override
    public void buildArticleToMinIO(ApArticle apArticle, String content) {
        if (content != null && StringUtils.isNotEmpty(content)){
            try {
                //读取文章详情模板页面
                Template template = configuration.getTemplate("article.ftl");
                //创建Map集合存储模板所需的数据
                Map<String,Object> data = new HashMap<>();
                List<Map> articleContent = JsonUtils.toList(content, Map.class);
                data.put("content",articleContent);

                //生成静态页面数据
                //临时字符串缓存流
                StringWriter writer = new StringWriter();
                template.process(data,writer);//将静态数据临时写入字符缓存流

                //把静态页面存储到minio，获取到页面的url地址
                String fileName = apArticle.getId() + ".html";
                InputStream inputStream = new ByteArrayInputStream(writer.toString().getBytes());
                String url = storageService.uploadHtmlFile("", fileName, inputStream);

                //把url地址更新到文章表的static_url字段上
                apArticle.setStaticUrl(url);
                apArticleMapper.updateById(apArticle);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("生成静态页面失败,{}",e.getMessage());
            }
        }
    }
}

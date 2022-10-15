package com.heima.search;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.heima.article.feign.ApArticleFeign;
import com.heima.model.search.pojos.ArticleDoc;
import com.heima.model.user.dtos.ArticleDto;
import com.heima.model.user.pojos.ApArticle;
import com.heima.utils.common.BeanHelper;
import com.heima.utils.common.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SearchApplication.class)
@Slf4j
public class ApArticleTest {
    @Autowired
    private RestHighLevelClient highLevelClient;
    @Autowired
    private ApArticleFeign apArticleFeign;
    private static final String INDEX_NAME = "article";

    @Test
    public void testImportData() throws IOException {
        List<ApArticle> allApArticles = apArticleFeign.findAllApArticles();

        if (CollectionUtils.isNotEmpty(allApArticles)){
            //批量写入ES
            BulkRequest bulkRequest = new BulkRequest();//缓存对象

            for (ApArticle apArticle : allApArticles) {
                ArticleDoc articleDoc = BeanHelper.copyProperties(apArticle, ArticleDoc.class);

                IndexRequest request = new IndexRequest(INDEX_NAME).id(articleDoc.getId().toString());
                String json = JsonUtils.toString(articleDoc);
                request.source(json, XContentType.JSON);

                //添加缓存
                bulkRequest.add(request);
            }

            //发送请求
            highLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
            log.info("完成批量导入数据到ES");
        }
    }
}

package com.heima.search.service.impl;

import com.heima.article.feign.ApArticleFeign;
import com.heima.common.dtos.ResponseResult;
import com.heima.model.search.dtos.ArticleDocumentVo;
import com.heima.model.search.dtos.UserSearchDto;
import com.heima.model.search.pojos.ArticleDoc;
import com.heima.model.user.pojos.ApArticle;
import com.heima.model.user.pojos.ApUser;
import com.heima.search.service.ApUserSearchService;
import com.heima.search.service.ArticleSearchService;
import com.heima.utils.common.BeanHelper;
import com.heima.utils.common.JsonUtils;
import com.heima.utils.common.ThreadLocalUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class ArticleSearchServiceImpl implements ArticleSearchService {
    private static final String INDEX_NAME = "article";
    //ES
    @Autowired
    private RestHighLevelClient highLevelClient;
    @Autowired
    private ApArticleFeign apArticleFeign;
    @Autowired
    private ApUserSearchService apUserSearchService;

    @Override
    public ResponseResult search(UserSearchDto dto) {
        //????????????
        if (dto.getPageSize() < 0 || dto.getPageSize() > 100) {
            dto.setPageSize(20);
        }
        if (dto.getMinBehotTime() == null) {
            dto.setMinBehotTime(new Date());
        }

        try {
            //??????????????????
            SearchRequest request = new SearchRequest(INDEX_NAME);

            //????????????
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

            //titli???????????????
            if (StringUtils.isNoneBlank(dto.getSearchWords())) {
                boolQueryBuilder.must(QueryBuilders.matchQuery("title", dto.getSearchWords()));
            } else {
                boolQueryBuilder.must(QueryBuilders.matchAllQuery());
            }

            //??????????????????
            boolQueryBuilder.filter(QueryBuilders.rangeQuery("publishTime").lt(dto.getMinBehotTime().getTime()));
            request.source().query(boolQueryBuilder);
            request.source().size(dto.getPageSize());
            request.source().sort(SortBuilders.fieldSort("publishTime").order(SortOrder.DESC));
            request.source().highlighter(new HighlightBuilder().field("title")
                    .preTags("<font style='color: red; font-size: inherit;'>")
                    .postTags("</front>"));

            //????????????
            SearchResponse response = highLevelClient.search(request, RequestOptions.DEFAULT);
            //????????????
            List<ArticleDocumentVo> articleDocumentVos = new ArrayList<>();
            SearchHits searchHits = response.getHits();
            for (SearchHit searchHit : searchHits) {
                String json = searchHit.getSourceAsString();
                ArticleDoc articleDoc = JsonUtils.toBean(json, ArticleDoc.class);
                ArticleDocumentVo articleDocumentVo = BeanHelper.copyProperties(articleDoc, ArticleDocumentVo.class);
                //????????????????????????
                HighlightField highlightField = searchHit.getHighlightFields().get("title");
                if (highlightField != null) {
                    articleDocumentVo.setH_title(highlightField.getFragments()[0].toString());
                }
                articleDocumentVos.add(articleDocumentVo);
            }

            //???????????????????????????????????????
            ApUser apUser = (ApUser) ThreadLocalUtils.get();
            if (apUser != null) {
                apUserSearchService.insert(apUser.getId(),dto.getSearchWords());
            }
            return ResponseResult.okResult(articleDocumentVos);

        } catch (IOException e) {
            e.printStackTrace();
            log.error("???????????????{}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void saveToEs(Long articleId) {
        //????????????ID????????????
        ResponseResult<ApArticle> responseResult = apArticleFeign.findByArticleId(articleId);

        if(responseResult.getCode().equals(200)){
            ApArticle apArticle = responseResult.getData();

            try {
                //??????????????????ES???
                IndexRequest request = new IndexRequest(INDEX_NAME);
                request.id(apArticle.getId().toString());

                ArticleDoc articleDoc = BeanHelper.copyProperties(apArticle,ArticleDoc.class);
                //??????json
                String json = JsonUtils.toString(articleDoc);
                request.source(json, XContentType.JSON);

                //????????????
                highLevelClient.index(request,RequestOptions.DEFAULT);

                log.info("????????????????????????ID???{}",articleId);
            } catch (IOException e) {
                e.printStackTrace();
                log.error("????????????????????????{}",e.getMessage());
            }
        }

    }

    @Override
    public void removeFromES(Long articleId) {
        try {
            //??????ES???????????????
            DeleteRequest request = new DeleteRequest(INDEX_NAME).id(articleId.toString());
            highLevelClient.delete(request,RequestOptions.DEFAULT);
            log.info("????????????????????????ID???{}",articleId);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("????????????????????????{}",e.getMessage());
        }

    }
}

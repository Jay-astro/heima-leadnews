package com.heima.search;


import com.heima.model.search.pojos.ApUserSearch;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SearchApplication.class)
public class MongoDBTest {
    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 新增/修改数据
     */
    @Test
    public void testSave(){
       /* Random random = new Random();
        for(int i=1;i<=5;i++){
            ApUserSearch userSearch = new ApUserSearch();
            userSearch.setUserId(2);
            userSearch.setCreatedTime(DateTime.now().plusMinutes(random.nextInt(10)).toDate());
            userSearch.setKeyword("瑞吉点餐");
            mongoTemplate.save(userSearch);//新增/修改
        }*/

        ApUserSearch userSearch = new ApUserSearch();
        userSearch.setId("6344d43d9b4a9e0d5f98c352");
        userSearch.setUserId(2);
        userSearch.setCreatedTime(new Date());
        userSearch.setKeyword("瑞吉点餐66");
        mongoTemplate.save(userSearch);//新增/修改
    }

    /**
     * 查询
     */
    @Test
    public void testQuery(){
        //需求：userId=1
        //Query query = Query.query(Criteria.where("userId").is(1)); // where userId=1

        //需求：userId=1 and keyword like '黑马%‘
        //MongoDB执行模糊查询使用正则表达式
        //Query query = Query.query(Criteria.where("userId").is(1).and("keyword").regex("^黑马.*"));

        //需求：分页展示
        Query query = Query.query(Criteria.where("userId").is(1));
        //query.limit(5);// 取前n条

        /*int page = 2;
        int size = 5;
        query.with(PageRequest.of(page-1,size)); // page从0开始计算，0代表第一页*/

        //需求：排序
        //按createdTime倒序
        query.with(Sort.by(Sort.Direction.DESC,"createdTime"));

        List<ApUserSearch> userSearchList = mongoTemplate.find(query,ApUserSearch.class);
        userSearchList.forEach(System.out::println);
    }

    /**
     * 删除
     */
    @Test
    public void testRemove(){
        //删除id=6344d43d9b4a9e0d5f98c352
        Query query = Query.query(Criteria.where("_id").is("6344d43d9b4a9e0d5f98c352"));
        mongoTemplate.remove(query, ApUserSearch.class);
    }

}

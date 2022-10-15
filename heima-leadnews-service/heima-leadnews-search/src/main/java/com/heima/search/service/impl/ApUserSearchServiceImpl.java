package com.heima.search.service.impl;

import com.heima.common.dtos.AppHttpCodeEnum;
import com.heima.common.dtos.ResponseResult;
import com.heima.common.exception.LeadNewsException;
import com.heima.model.search.dtos.HistorySearchDto;
import com.heima.model.search.pojos.ApUserSearch;
import com.heima.model.user.pojos.ApUser;
import com.heima.search.service.ApUserSearchService;
import com.heima.utils.common.ThreadLocalUtils;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.Date;
import java.util.List;

public class ApUserSearchServiceImpl implements ApUserSearchService {
    @Autowired
    private MongoTemplate mongoTemplate;
    @Override
    public void insert(Integer userId, String keyword) {
        //查询userId与keyword是否存在
        Query query = Query.query(Criteria.where("userId").is(userId).and("keyword").is(keyword));
        ApUserSearch userSearch = mongoTemplate.findOne(query,ApUserSearch.class);

        if (userSearch != null){
            //存在记录，更新时间
            userSearch.setCreatedTime(new Date());
            mongoTemplate.save(userSearch);
            return;
        }

        //不存在记录，判断userId的记录总数是否超过10个
        Query userQuery = Query.query(Criteria.where("userId").is(userId));
        //时间排序
        userQuery.with(Sort.by(Sort.Direction.ASC,"createTime"));
        List<ApUserSearch> userSearchList = mongoTemplate.find(userQuery, ApUserSearch.class);

        if (userSearchList == null || userSearchList.size() < 10){
            ApUserSearch newSearch = new ApUserSearch();
            newSearch.setUserId(userId);
            newSearch.setKeyword(keyword);
            newSearch.setCreatedTime(new Date());
            mongoTemplate.save(newSearch);
        } else {
            //获取第一条，更新数据
            ApUserSearch apUserSearch = userSearchList.get(0);
            apUserSearch.setKeyword(keyword);
            apUserSearch.setCreatedTime(new Date());
            mongoTemplate.save(apUserSearch);
        }
    }

    @Override
    public ResponseResult load() {
        ApUser apUser = (ApUser) ThreadLocalUtils.get();
        if (apUser == null){
            return ResponseResult.okResult(null);
        }
        Query query = Query.query(Criteria.where("userId").is(apUser.getId()));
        query.with(Sort.by(Sort.Direction.DESC,"createTime"));
        query.limit(10);
        return ResponseResult.okResult(mongoTemplate.find(query,ApUserSearch.class));
    }

    @Override
    public ResponseResult del(HistorySearchDto dto) {
        ApUser apUser = (ApUser)ThreadLocalUtils.get();
        if (apUser == null){
            throw  new LeadNewsException(AppHttpCodeEnum.NEED_LOGIN);
        }
        Query query = Query.query(Criteria.where("_id").is(dto.getId()));
        mongoTemplate.remove(query,ApUserSearch.class);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }
}

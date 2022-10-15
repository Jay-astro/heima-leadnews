package com.heima.wemedia.service.impl;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.common.constants.MQConstants;
import com.heima.common.dtos.AppHttpCodeEnum;
import com.heima.common.dtos.PageResponseResult;
import com.heima.common.dtos.ResponseResult;
import com.heima.common.exception.LeadNewsException;
import com.heima.model.wemedia.dtos.WmNewsDownUpDto;
import com.heima.model.wemedia.dtos.WmNewsSaveDto;
import com.heima.model.wemedia.dtos.WmNewsPageReqDto;
import com.heima.model.wemedia.pojos.WmMaterial;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.model.wemedia.pojos.WmNewsMaterial;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.utils.common.BeanHelper;
import com.heima.utils.common.JsonUtils;
import com.heima.utils.common.ThreadLocalUtils;
import com.heima.wemedia.mapper.WmMaterialMapper;
import com.heima.wemedia.mapper.WmNewsMapper;
import com.heima.wemedia.mapper.WmNewsMaterialMapper;
import com.heima.wemedia.service.WmNewsService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class WmNewsServiceImpl extends ServiceImpl<WmNewsMapper, WmNews> implements WmNewsService {

    @Autowired
    private WmNewsMaterialMapper wmNewsMaterialMapper;

    @Autowired
    private WmMaterialMapper wmMaterialMapper;

    @Autowired
    private KafkaTemplate kafkaTemplate;


    /**
     * @param dto
     * @return
     */
    @Override
    public PageResponseResult findList(WmNewsPageReqDto dto) {
        //校验参数
        dto.checkParam();

        //获取用户登录信息
        WmUser wmUser = (WmUser) ThreadLocalUtils.get();
        if (wmUser == null) {
            throw new LeadNewsException(AppHttpCodeEnum.NEED_LOGIN);
        }
        IPage<WmNews> iPage = new Page<>(dto.getPage(), dto.getSize());

        //拼接条件
        QueryWrapper<WmNews> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", wmUser.getId());

        //status
        if (dto.getStatus() != null) {
            queryWrapper.like("title", dto.getKeyword());
        }
        if (StringUtils.isNotEmpty(dto.getKeyword())) {
            queryWrapper.eq("channel_id", dto.getChannelId());
        }
        if (dto.getBeginPubDate() != null && dto.getEndPubDate() != null) {
            queryWrapper.between("publish_time", dto.getBeginPubDate(), dto.getEndPubDate());
        }
        //时间倒序
        queryWrapper.orderByDesc("create_time");

        iPage = page(iPage, queryWrapper);

        PageResponseResult pageResponseResult = new PageResponseResult(dto.getPage(), dto.getSize(), (int) iPage.getTotal());
        pageResponseResult.setErrorMessage("查询成功");
        pageResponseResult.setCode(200);
        pageResponseResult.setData(iPage.getRecords());
        return pageResponseResult;
    }


    /**
     * submit
     *
     * @param dto
     * @return
     */
    @Override
    public ResponseResult submit(WmNewsSaveDto dto) {

        WmNews wmNews = BeanHelper.copyProperties(dto, WmNews.class);

        //获取登录用户信息
        WmUser wmUser = (WmUser) ThreadLocalUtils.get();
        if (wmUser == null) {
            throw new LeadNewsException(AppHttpCodeEnum.NEED_LOGIN);
        }
        //发布人Id
        wmNews.setUserId(wmUser.getId());

        //处理自动封面
        //获取文章内容的图片
        List<String> contentImages = getContentImagesFromWmNews(wmNews.getContent());
        if (dto.getType() == -1) {
            //自动封面
            int size = contentImages.size();
            if (size == 0) {
                wmNews.setType((short) 0);
                wmNews.setImages(null);
            }
            if (size >= 1 && size <= 2) {
                //单图
                wmNews.setType((short) 1);
                wmNews.setImages(contentImages.get(0));
            }
            if (size >= 3) {
                wmNews.setType((short) 3);
                wmNews.setImages(contentImages.subList(0, 3).stream().collect(Collectors.joining(",")));
            }
        } else {
            //非自动封面
            if (CollectionUtils.isNotEmpty(dto.getImages())) {
                /**
                 * Collectors.joining(",")： 将集合的每个元素使用指定分隔符拼接成新的字符串返回
                 */
                String imageStr = dto.getImages().stream().collect(Collectors.joining(","));
                wmNews.setImages(imageStr);
            }
        }

        //判断操作
        if (dto.getId() == null) {
            //新增操作
            wmNews.setCreatedTime(new Date());
            save(wmNews);
        } else {
            //update
            updateById(wmNews);

            //删除当前文章的绑定记录
            QueryWrapper<WmNewsMaterial> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("news_is", wmNews.getId());
            wmNewsMaterialMapper.delete(queryWrapper);
        }

        //绑定文章和素材的关系
        if (CollectionUtils.isNotEmpty(contentImages)) {
            //更具素材的URL地址查询Id
            List<Integer> materialIds = queryMaterialIdsByUrls(contentImages);
            if (CollectionUtils.isNotEmpty(materialIds)) {
                wmNewsMaterialMapper.saveNewsMaterials(materialIds, wmNews.getId(), 0);
            }
        }

        //封面素材绑定
        if (StringUtils.isNotBlank(wmNews.getImages())) {
            List<String> images = Arrays.asList(wmNews.getImages().split(","));
            List<Integer> materialIds = queryMaterialIdsByUrls(images);
            if (CollectionUtils.isNotEmpty(materialIds)) {
                wmNewsMaterialMapper.saveNewsMaterials(materialIds, wmNews.getId(), 1);
            }
        }

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    @Override
    public ResponseResult downOrUp(WmNewsDownUpDto dto) {
        //查询文章是否存在
        WmNews wmNews = getById(dto.getId());
        if (wmNews == null) {
            throw new LeadNewsException(AppHttpCodeEnum.DATA_NOT_EXIST);
        }
        //查找文章是否已发布
        if (wmNews.getStatus() != 9) {
            throw new LeadNewsException(400, "文章未发布！");
        }
        //修改文章的enable值
        wmNews.setEnable(dto.getEnable());
        updateById(wmNews);

        //发送消息同步App端的上下架状态
        Map<String, Object> msg = new HashMap<>();
        msg.put("articleId", wmNews.getArticleId());
        msg.put("enable", dto.getEnable());

        kafkaTemplate.send(MQConstants.WM_NEWS_UP_OR_DOWN_TOPIC, JsonUtils.toString(msg));

        //文章上下架后同步更新索引库
        if (dto.getEnable().equals((short) 1)) {
            kafkaTemplate.send(MQConstants.WM_NEW_UP_ES_TOPIC, wmNews.getArticleId().toString());
        } else {
            kafkaTemplate.send(MQConstants.WM_NEW_DOWN_ES_TOPIC, wmNews.getArticleId().toString());
        }
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }


    private List<Integer> queryMaterialIdsByUrls(List<String> urls) {
        QueryWrapper<WmMaterial> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("url", urls);//where url in (1,2,3)
        List<WmMaterial> wmMaterials = wmMaterialMapper.selectList(queryWrapper);
        if (CollectionUtils.isNotEmpty(wmMaterials)) {
            return wmMaterials.stream().map(WmMaterial::getId).collect(Collectors.toList());
        }
        return null;
    }

    /**
     * 获取文章内容的图片
     *
     * @param content
     * @return
     */
    private List<String> getContentImagesFromWmNews(String content) {
        List<String> contentImages = new ArrayList<>();
        if (StringUtils.isNotBlank(content)) {
            List<Map> list = JsonUtils.toList(content, Map.class);

            contentImages = list.stream()
                    .filter(map -> map.get("type").equals("image"))
                    .map(map -> (String) map.get("value"))
                    .collect(Collectors.toList());
        }
        return contentImages;
    }
}

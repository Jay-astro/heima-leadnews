package com.heima.wemedia.service.impl;

import com.alibaba.nacos.common.utils.CollectionUtils;
import com.heima.article.feign.ApArticleFeign;
import com.heima.common.aliyun.GreenImageScan;
import com.heima.common.aliyun.GreenTextScan;
import com.heima.common.constants.MQConstants;
import com.heima.common.constants.RedisContants;
import com.heima.common.dtos.ResponseResult;
import com.heima.common.minio.MinIOFileStorageService;
import com.heima.model.article.dtos.ApArticleDto;
import com.heima.model.wemedia.pojos.WmChannel;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.model.wemedia.pojos.WmSensitive;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.utils.common.BeanHelper;
import com.heima.utils.common.JsonUtils;
import com.heima.utils.common.SensitiveWordUtil;
import com.heima.wemedia.mapper.WmChannelMapper;
import com.heima.wemedia.mapper.WmNewsMapper;
import com.heima.wemedia.mapper.WmSensitiveMapper;
import com.heima.wemedia.mapper.WmUserMapper;
import com.heima.wemedia.service.WmNewsAutoScanService;
import com.heima.wemedia.service.WmNewsTaskService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.ITesseract;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class WmNewsAutoScanServiceImpl implements WmNewsAutoScanService {

    @Autowired
    private MinIOFileStorageService storageService;

    @Autowired
    private GreenImageScan greenImageScan;

    @Autowired
    private GreenTextScan greenTextScan;

    @Autowired
    private WmNewsMapper wmNewsMapper;

    @Autowired
    private ApArticleFeign apArticleFeign;

    @Autowired
    private WmUserMapper wmUserMapper;

    @Autowired
    private WmChannelMapper wmChannelMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private WmSensitiveMapper sensitiveMapper;

    @Autowired
    private ITesseract tesseract;

    @Autowired
    private WmNewsTaskService wmNewsTaskService;

    @Autowired
    private KafkaTemplate kafkaTemplate;

    /**
     * ????????????
     *
     * @param id
     */
    @Override
    @Async
    @GlobalTransactional
    public void autoScanWmNews(Integer id) {
        WmNews wmNews = wmNewsMapper.selectById(id);

        if (wmNews == null){
            return;
        }
        //??????????????????
        if (wmNews.getStatus() != 1) {
            return;
        }

        //??????????????????
        //????????????
        List<String> textFromWmNews = getTextFromWmNews(wmNews);

        //????????????
        List<byte[]> imageFromWmNews = getImageFromWmNews(wmNews);

        //????????????????????????
        if (CollectionUtils.isNotEmpty(textFromWmNews)) {
            boolean flag = handleSensitiveScan(textFromWmNews, wmNews);
            if (!flag){
                return;
            }
        }

        //????????????
        //????????????
        if (CollectionUtils.isNotEmpty(textFromWmNews)) {
            try {
                Map result = greenTextScan.greeTextScan(textFromWmNews);
                boolean flag = handleScanResult(result, wmNews);
                if (!flag) {
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.error("??????aliyun????????????,{}", e.getMessage());
            }
        }

        //????????????
        if (CollectionUtils.isNotEmpty(imageFromWmNews)) {
            try {
                Map result = greenImageScan.imageScan(imageFromWmNews);
                boolean flag = handleScanResult(result, wmNews);
                if (!flag) {
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.error("??????????????????????????? ???{}", e.getMessage());
            }
        }

        //?????????????????????????????????????????????????????????
        if (wmNews.getPublishTime() != null && wmNews.getPublishTime().after(new Date())) {
            //??????????????????????????????(8)
            wmNews.setStatus(WmNews.Status.SUCCESS.getCode());
            wmNews.setReason("????????????");
            wmNewsMapper.updateById(wmNews);

            //????????????????????????????????????????????????????????????
            Long taskId = wmNewsTaskService.addWmNewsTask(wmNews);
            //??????wm_news??????task_id??????

            return;
        }
        //??????????????????App??????
        publishApArticle(wmNews);
        //ES???????????????
        //??????MQ???????????????????????????
        kafkaTemplate.send(MQConstants.WM_NEWS_UP_OR_DOWN_TOPIC,wmNews.getArticleId().toString());
    }


    /**
     * ????????????
     * @param content
     * @return
     */
    private String filterWords(String content){
        //????????????
        content = content.replaceAll(" ","");
        return content;
    }
    /**
     * ????????????????????????
     * @param textList
     * @param wmNews
     * @return
     */
    private boolean handleSensitiveScan(List<String> textList, WmNews wmNews) {
        boolean flag = true;
        //???redis???????????????
        List<String> wordList = null;
        String redisData = redisTemplate.opsForValue().get(RedisContants.SENSITIVE_WORD);
        if (StringUtils.isEmpty(redisData)) {
            List<WmSensitive> sensitiveList = sensitiveMapper.selectList(null);
            if (CollectionUtils.isNotEmpty(sensitiveList)) {
                wordList = sensitiveList.stream().map(WmSensitive::getSensitives).collect(Collectors.toList());
                //??????????????????redis
                redisTemplate.opsForValue().set(RedisContants.SENSITIVE_WORD, JsonUtils.toString(wordList));
            }
        } else {
            //????????????
            wordList = JsonUtils.toList(redisData, String.class);

        }

        //??????????????????
        SensitiveWordUtil.initMap(wordList);

        if (CollectionUtils.isNotEmpty(textList)) {
            //??????DFA??????
            SensitiveWordUtil.initMap(wordList);
            //??????????????????????????????DFA??????
            String content = filterWords(textList.stream().collect(Collectors.joining("")));
            Map<String, Integer> result = SensitiveWordUtil.matchWords(content);
            if (result != null && result.size() > 0) {
                //???????????????
                Set<String> keys = result.keySet();
                //??????????????????
                wmNews.setStatus(WmNews.Status.FAIL.getCode());
                wmNews.setReason("??????????????????" + keys);
                wmNewsMapper.updateById(wmNews);
                flag = false;
            }
        }
        return flag;
    }


    /**
     * ???????????????????????????App???
     *
     * @param wmNews
     */
    public void publishApArticle(WmNews wmNews) {
        ApArticleDto apArticleDto = BeanHelper.copyProperties(wmNews, ApArticleDto.class);
        //????????????ap_article??????Id
        apArticleDto.setId(wmNews.getArticleId());

        //????????????????????????
        WmUser wmUser = wmUserMapper.selectById(wmNews.getUserId());
        if (wmUser != null) {
            apArticleDto.setAuthorId(Long.valueOf(wmUser.getId()));
            apArticleDto.setAuthorName(wmUser.getNickname());
        }
        //????????????????????????
        WmChannel wmChannel = wmChannelMapper.selectById(wmNews.getChannelId());
        if (wmChannel != null) {
            apArticleDto.setChannelId(wmChannel.getId());
            apArticleDto.setChannelName(wmChannel.getName());
        }
        //??????????????????
        apArticleDto.setLayout(wmNews.getType());
        //??????????????????
        apArticleDto.setFlag((byte) 0);
        //??????????????????
        apArticleDto.setLikes(0);
        apArticleDto.setViews(0);
        apArticleDto.setComment(0);
        apArticleDto.setCollection(0);

        ResponseResult<Long> responseResult = apArticleFeign.save(apArticleDto);

        if (responseResult.getCode().equals(200)) {
            //??????App?????????ID
            Long articleId = responseResult.getData();

            //????????????????????????article_id
            wmNews.setArticleId(articleId);
            //?????????????????????9???????????????
            wmNews.setStatus(WmNews.Status.PUBLISHED.getCode());
            wmNews.setReason("????????????");
            wmNewsMapper.updateById(wmNews);
        }
    }

    /**
     * ???????????????????????????
     *
     * @param result
     * @param wmNews
     * @return
     */
    private boolean handleScanResult(Map result, WmNews wmNews) {
        boolean flag = false;
        String suggestion = (String) result.get("suggestion");
        if (suggestion.equals("pass")) {
            flag = true;
        }
        if (suggestion.equals("review")) {
            //????????????
            wmNews.setStatus(WmNews.Status.ADMIN_AUTH.getCode());
            wmNews.setReason("?????????????????????????????????????????????????????????");
            wmNewsMapper.updateById(wmNews);
        }
        if (suggestion.equals("block")) {
            //????????????
            wmNews.setStatus(WmNews.Status.FAIL.getCode());
            wmNews.setReason("?????????????????????");
            wmNewsMapper.updateById(wmNews);
        }
        return flag;
    }

    /**
     * ??????????????????
     *
     * @param wmNews
     * @return
     */
    private List<byte[]> getImageFromWmNews(WmNews wmNews) {
        List<byte[]> imageList = new ArrayList<>();

        //??????????????????????????????
        Set<String> urlSet = new HashSet<>();

        //????????????
        if (StringUtils.isNoneBlank(wmNews.getImages())) {
            String[] array = wmNews.getImages().split(",");
            urlSet.addAll(Arrays.asList(array));
        }
        //????????????
        if (StringUtils.isNotBlank(wmNews.getContent())) {
            List<Map> list = JsonUtils.toList(wmNews.getContent(), Map.class);
            List<String> tList = list.stream()
                    .filter(map -> map.get("type").equals("image"))
                    .map(map -> (String) map.get("value"))
                    .collect(Collectors.toList());
            urlSet.addAll(tList);
        }

        if (CollectionUtils.isNotEmpty(urlSet)) {
            for (String url : urlSet) {
                //MINIO????????????
                byte[] bytes = storageService.downLoadFile(url);
                imageList.add(bytes);
            }
        }
        return imageList;
    }

    /**
     * ??????????????????
     *
     * @param wmNews
     * @return
     */
    private List<String> getTextFromWmNews(WmNews wmNews) {
        List<String> stringList = new ArrayList<>();
        //????????????
        if (StringUtils.isNotBlank(wmNews.getTitle())) {
            stringList.add(wmNews.getTitle());
        }
        if (StringUtils.isNotBlank(wmNews.getLabels())) {
            stringList.add(wmNews.getLabels());
        }
        if (StringUtils.isNotBlank(wmNews.getContent())) {
            List<Map> mapList = JsonUtils.toList(wmNews.getContent(), Map.class);
            List<String> collect = mapList.stream()
                    .filter(map -> map.get("type").equals("text"))
                    .map(map -> (String) map.get("value"))
                    .collect(Collectors.toList());
            stringList.addAll(collect);
        }
        return stringList;
    }
}

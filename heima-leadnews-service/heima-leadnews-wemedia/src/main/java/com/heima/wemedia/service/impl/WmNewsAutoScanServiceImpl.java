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
     * 审核文章
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
        //判断文章状态
        if (wmNews.getStatus() != 1) {
            return;
        }

        //审核文章内容
        //提取文字
        List<String> textFromWmNews = getTextFromWmNews(wmNews);

        //提取图片
        List<byte[]> imageFromWmNews = getImageFromWmNews(wmNews);

        //检测自定义敏感词
        if (CollectionUtils.isNotEmpty(textFromWmNews)) {
            boolean flag = handleSensitiveScan(textFromWmNews, wmNews);
            if (!flag){
                return;
            }
        }

        //审核文章
        //文章审核
        if (CollectionUtils.isNotEmpty(textFromWmNews)) {
            try {
                Map result = greenTextScan.greeTextScan(textFromWmNews);
                boolean flag = handleScanResult(result, wmNews);
                if (!flag) {
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.error("调用aliyun接口失败,{}", e.getMessage());
            }
        }

        //图片审核
        if (CollectionUtils.isNotEmpty(imageFromWmNews)) {
            try {
                Map result = greenImageScan.imageScan(imageFromWmNews);
                boolean flag = handleScanResult(result, wmNews);
                if (!flag) {
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                log.error("调用阿里云接口失败 ，{}", e.getMessage());
            }
        }

        //判断用户选择的发布时间是否大于当前时间
        if (wmNews.getPublishTime() != null && wmNews.getPublishTime().after(new Date())) {
            //修改文章状态为待发布(8)
            wmNews.setStatus(WmNews.Status.SUCCESS.getCode());
            wmNews.setReason("待发布！");
            wmNewsMapper.updateById(wmNews);

            //把当前自媒体文章发布任务添加到延迟队列中
            Long taskId = wmNewsTaskService.addWmNewsTask(wmNews);
            //更新wm_news表的task_id字段

            return;
        }
        //调用接口保存App文章
        publishApArticle(wmNews);
        //ES的索引同步
        //采用MQ同步索引库文章数据
        kafkaTemplate.send(MQConstants.WM_NEWS_UP_OR_DOWN_TOPIC,wmNews.getArticleId().toString());
    }


    /**
     * 过滤内容
     * @param content
     * @return
     */
    private String filterWords(String content){
        //去掉空格
        content = content.replaceAll(" ","");
        return content;
    }
    /**
     * 自定义敏感词检测
     * @param textList
     * @param wmNews
     * @return
     */
    private boolean handleSensitiveScan(List<String> textList, WmNews wmNews) {
        boolean flag = true;
        //从redis中查询数据
        List<String> wordList = null;
        String redisData = redisTemplate.opsForValue().get(RedisContants.SENSITIVE_WORD);
        if (StringUtils.isEmpty(redisData)) {
            List<WmSensitive> sensitiveList = sensitiveMapper.selectList(null);
            if (CollectionUtils.isNotEmpty(sensitiveList)) {
                wordList = sensitiveList.stream().map(WmSensitive::getSensitives).collect(Collectors.toList());
                //把敏感词存入redis
                redisTemplate.opsForValue().set(RedisContants.SENSITIVE_WORD, JsonUtils.toString(wordList));
            }
        } else {
            //转换格式
            wordList = JsonUtils.toList(redisData, String.class);

        }

        //构建敏感词库
        SensitiveWordUtil.initMap(wordList);

        if (CollectionUtils.isNotEmpty(textList)) {
            //初始DFA词库
            SensitiveWordUtil.initMap(wordList);
            //使用文章文字内容匹配DFA词库
            String content = filterWords(textList.stream().collect(Collectors.joining("")));
            Map<String, Integer> result = SensitiveWordUtil.matchWords(content);
            if (result != null && result.size() > 0) {
                //获取违规词
                Set<String> keys = result.keySet();
                //修改文章状态
                wmNews.setStatus(WmNews.Status.FAIL.getCode());
                wmNews.setReason("包含违规词：" + keys);
                wmNewsMapper.updateById(wmNews);
                flag = false;
            }
        }
        return flag;
    }


    /**
     * 从自媒体文章发布到App端
     *
     * @param wmNews
     */
    public void publishApArticle(WmNews wmNews) {
        ApArticleDto apArticleDto = BeanHelper.copyProperties(wmNews, ApArticleDto.class);
        //重新绑定ap_article的表Id
        apArticleDto.setId(wmNews.getArticleId());

        //设置文章作者信息
        WmUser wmUser = wmUserMapper.selectById(wmNews.getUserId());
        if (wmUser != null) {
            apArticleDto.setAuthorId(Long.valueOf(wmUser.getId()));
            apArticleDto.setAuthorName(wmUser.getNickname());
        }
        //设置文章频道信息
        WmChannel wmChannel = wmChannelMapper.selectById(wmNews.getChannelId());
        if (wmChannel != null) {
            apArticleDto.setChannelId(wmChannel.getId());
            apArticleDto.setChannelName(wmChannel.getName());
        }
        //设置封面类型
        apArticleDto.setLayout(wmNews.getType());
        //设置文章类型
        apArticleDto.setFlag((byte) 0);
        //设置文章参数
        apArticleDto.setLikes(0);
        apArticleDto.setViews(0);
        apArticleDto.setComment(0);
        apArticleDto.setCollection(0);

        ResponseResult<Long> responseResult = apArticleFeign.save(apArticleDto);

        if (responseResult.getCode().equals(200)) {
            //获取App的文章ID
            Long articleId = responseResult.getData();

            //设置自媒体文章的article_id
            wmNews.setArticleId(articleId);
            //修改文章状态为9（已发布）
            wmNews.setStatus(WmNews.Status.PUBLISHED.getCode());
            wmNews.setReason("已发布！");
            wmNewsMapper.updateById(wmNews);
        }
    }

    /**
     * 处理阿里云检测结果
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
            //人工审核
            wmNews.setStatus(WmNews.Status.ADMIN_AUTH.getCode());
            wmNews.setReason("文章内容中有可疑内容，需人工进一步审查");
            wmNewsMapper.updateById(wmNews);
        }
        if (suggestion.equals("block")) {
            //审核失败
            wmNews.setStatus(WmNews.Status.FAIL.getCode());
            wmNews.setReason("包含违规内容！");
            wmNewsMapper.updateById(wmNews);
        }
        return flag;
    }

    /**
     * 提取文章图片
     *
     * @param wmNews
     * @return
     */
    private List<byte[]> getImageFromWmNews(WmNews wmNews) {
        List<byte[]> imageList = new ArrayList<>();

        //提取文章所有图片地址
        Set<String> urlSet = new HashSet<>();

        //封面图片
        if (StringUtils.isNoneBlank(wmNews.getImages())) {
            String[] array = wmNews.getImages().split(",");
            urlSet.addAll(Arrays.asList(array));
        }
        //内容图片
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
                //MINIO下载图片
                byte[] bytes = storageService.downLoadFile(url);
                imageList.add(bytes);
            }
        }
        return imageList;
    }

    /**
     * 提取文章文字
     *
     * @param wmNews
     * @return
     */
    private List<String> getTextFromWmNews(WmNews wmNews) {
        List<String> stringList = new ArrayList<>();
        //全部文字
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

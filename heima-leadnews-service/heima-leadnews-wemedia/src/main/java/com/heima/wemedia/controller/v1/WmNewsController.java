package com.heima.wemedia.controller.v1;

import com.heima.common.dtos.PageResponseResult;
import com.heima.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmNewsDownUpDto;
import com.heima.model.wemedia.dtos.WmNewsSaveDto;
import com.heima.model.wemedia.dtos.WmNewsPageReqDto;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.wemedia.service.WmNewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/news")
public class WmNewsController {

    @Autowired
    private WmNewsService wmNewsService;

    /**
     * 查询自媒体文章
     *
     * @param dto
     * @return
     */
    @PostMapping("/list")
    public PageResponseResult findList(@RequestBody WmNewsPageReqDto dto) {
        return wmNewsService.findList(dto);
    }


    /**
     * 文章发布
     *
     * @param dto
     * @return
     */
    @PostMapping("/submit")
    public ResponseResult submit(@RequestBody WmNewsSaveDto dto) {
        return wmNewsService.submit(dto);
    }


    /**
     * 修改回显文章
     * @param id
     * @return
     */
    @GetMapping("/one/{id}")
    public ResponseResult<WmNews> findOne(@PathVariable("id") Integer id){
        return ResponseResult.okResult(wmNewsService.getById(id));
    }

    /**
     * 文章上下架
     */
    @PostMapping("/down_or_up")
    public ResponseResult downOrUp(@RequestBody WmNewsDownUpDto dto){
        return wmNewsService.downOrUp(dto);
    }
}

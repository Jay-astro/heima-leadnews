package com.heima.model.user.dtos;

import lombok.Data;

import java.util.Date;

@Data
public class ArticleDto {

    // 最大时间
    Date maxBehotTime;
    // 最小时间
    Date minBehotTime;
    // 分页size
    Integer size;
    // 频道ID
    String tag;
}

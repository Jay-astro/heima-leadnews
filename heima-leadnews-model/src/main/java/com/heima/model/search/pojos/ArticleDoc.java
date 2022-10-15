package com.heima.model.search.pojos;

import lombok.Data;

import java.util.Date;

@Data
public class ArticleDoc {

    private Long id;

    private String title;

    private Integer authorId;

    private String authorName;

    private Integer channelId;

    private String channelName;

    private Integer layout;

    private String images;

    private Integer likes;

    private Integer collection;

    private Integer comment;

    private Integer views;

    private Date createdTime;

    private Date publishTime;

    private String staticUrl;
}

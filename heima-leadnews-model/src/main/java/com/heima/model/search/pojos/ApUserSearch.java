package com.heima.model.search.pojos;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

/**
 * 用户搜索历史
 */
@Data
@Document(collection = "ap_user_search") //声明集合名称
public class ApUserSearch {
    @Id  //声明为主键   如果不赋值，MongoDB自动生成uuid
    private String id; //主键

    //@Field("user_id")
    private Integer userId; //该记录的用户ID

    private String keyword;//搜索词

    private Date createdTime;//记录创建时间
}

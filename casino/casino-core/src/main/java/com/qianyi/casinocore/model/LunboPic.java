package com.qianyi.casinocore.model;

import lombok.Data;

import javax.persistence.Entity;

@Entity
@Data
public class LunboPic extends BaseEntity{
    //编号，1-5
    private Integer no;
    private String url;
    private String remark;
    /**
     * 展示端 1 web 2 app
     */
    private Integer theShowEnd;
    /**
     * 点击量
     */
    private Integer hits;
}

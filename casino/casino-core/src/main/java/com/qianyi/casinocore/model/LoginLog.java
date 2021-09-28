package com.qianyi.casinocore.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

/**
 * 登陆日志
 */
@Entity
@Data
@Table(indexes = {@Index(columnList = "userId"), @Index(columnList = "account"), @Index(columnList = "ip"), @Index(columnList = "createTime")})
public class LoginLog extends BaseEntity{
    private String ip;
    private Long userId;
    private String account;
    //帐号类型（归属）
    private String description;
    //IP归属地
    private String address;
    //类型 1 登录 2 注册
    private Integer type = 1;

}

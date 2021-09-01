package com.qianyi.casinocore.model;

import lombok.Data;

import javax.persistence.Entity;

/**
 * 登陆日志
 */
@Entity
@Data
public class LoginLog extends BaseEntity{

    private String ip;
    private Long userId;
    private String account;
    //帐号类型（归属）
    private String description;
    //IP归属地
    private String address;

}

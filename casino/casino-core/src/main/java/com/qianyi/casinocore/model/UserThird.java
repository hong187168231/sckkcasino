package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * 三方用户信息
 */
@Data
@Entity
@ApiModel("我方与三方帐户信息表")
public class UserThird extends BaseEntity{

    @ApiModelProperty(value = "我方用户id")
    private Long userId;
    @Column(unique = true)
    private String account;
    private String password;
    private String language;

}

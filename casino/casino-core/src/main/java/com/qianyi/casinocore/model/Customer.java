package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import javax.persistence.Entity;

@Entity
@Data
@ApiModel("联系客服")
public class Customer extends BaseEntity {
    private String qq;
    private String telegram;
    private String skype;
    private String whatsApp;
    private String facebook;
    private String onlineUrl;
    private String wechat;
    /**
     * 美洽
     */
    private String meiqia;

    private String telephone;

}

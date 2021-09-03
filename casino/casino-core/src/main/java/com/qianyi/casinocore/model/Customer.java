package com.qianyi.casinocore.model;

import lombok.Data;
import javax.persistence.Entity;

@Entity
@Data
public class Customer extends BaseEntity {
    private String qq;
    private String telegram;
    private String skype;
    private String whatsApp;
    private String facebook;
    private String onlineUrl;

}

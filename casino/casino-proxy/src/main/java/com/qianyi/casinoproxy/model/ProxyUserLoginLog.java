package com.qianyi.casinoproxy.model;

import com.qianyi.casinocore.model.BaseEntity;
import lombok.Data;
import javax.persistence.Entity;

@Entity
@Data
public class ProxyUserLoginLog extends BaseEntity {

    private String ip;
    private String userName;
    private Long userId;
    private String description;
    private String address;

    public ProxyUserLoginLog(String ip, String userName, Long userId, String description) {
        this.ip = ip;
        this.userName = userName;
        this.userId = userId;
        this.description = description;
    }

    public ProxyUserLoginLog() {
    }
}

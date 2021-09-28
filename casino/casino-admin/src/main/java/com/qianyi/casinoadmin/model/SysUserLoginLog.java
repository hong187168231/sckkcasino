package com.qianyi.casinoadmin.model;

import com.qianyi.casinocore.model.BaseEntity;
import lombok.Data;
import javax.persistence.Entity;

@Entity
@Data
public class SysUserLoginLog extends BaseEntity {

    private String ip;
    private String userName;
    private Long userId;
    private String description;
    private String address;

    public SysUserLoginLog(String ip, String userName, Long userId, String description) {
        this.ip = ip;
        this.userName = userName;
        this.userId = userId;
        this.description = description;
    }

    public SysUserLoginLog() {
    }
}

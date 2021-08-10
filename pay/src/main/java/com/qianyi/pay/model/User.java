package com.qianyi.pay.model;

import lombok.Data;

import javax.persistence.Entity;

@Data
@Entity
public class User extends BaseEntity{

    private String name;
    private String account;
    private String password;
    //帐号状态（1：启用，其他：禁用）
    private Integer state;

    //校验用户帐号权限
    public static boolean checkUser(User user) {
        if (user == null) {
            return false;
        }

        if (user.getState() != 1) {
            return false;
        }
        return true;
    }
}

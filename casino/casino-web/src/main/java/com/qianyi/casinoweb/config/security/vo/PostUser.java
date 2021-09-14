package com.qianyi.casinoweb.config.security.vo;

import lombok.Data;

@Data
public class PostUser {
    private String account;
    private String password;
    private String validate;
}

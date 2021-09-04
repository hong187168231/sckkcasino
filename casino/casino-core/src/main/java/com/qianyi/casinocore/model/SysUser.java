package com.qianyi.casinocore.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.Date;

/**
 * 管理后台用户表
 */
@Entity
@Data
@ApiModel("后台用户表")
public class SysUser extends BaseEntity {

    /**
     * 管理员账号
     */
    @Column(unique = true)
    private String userName;

    /**
     * 昵称
     */
    private String nickName;

    /**
     * 密码
     */
    private String passWord;

    /**
     * 随机盐，加密密码用
     */
    private String safeCode;

    /**
     * 手机号码
     */
    private String phone;

    /**
     * 电子邮箱
     */
    private String email;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 部门ID
     */
    private Integer deptId;

    /**
     * 是否锁定 0：正常 1：锁定
     */
    private Integer lockFlag;

    /**
     * 0：正常，1：删除
     */
    private Integer delFlag;

    /**
     * 在线状态 默认0离线，1：在线
     */
    private Integer currentState;

    /**
     * 谷歌状态 默认0开启， 1关闭
     */
    private Integer gaStatus;

    /**
     * 谷歌验证码KEY
     */
    private String gaKey;

    /**
     * 谷歌验证码是否绑定0=未绑定，1=已绑定
     */
    private String gaBind;

    /**
     * 最后登录时间
     */
    private String LastLoginIp;

    @CreatedDate
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lastLoginTime;

    /**
     * 最后登出时间
     */
    @CreatedDate
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lastLogoutTime;

    /**
     * 在线时长
     */
    private String onLineTime;
}

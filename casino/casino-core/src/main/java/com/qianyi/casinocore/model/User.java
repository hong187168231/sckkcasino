package com.qianyi.casinocore.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.qianyi.modulecommon.Constants;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.ObjectUtils;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Transient;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

@Data
@Entity
public class User extends BaseEntity implements UserDetails {
    @ApiModelProperty("名字")
    private String name;
    @Column(unique = true)
    @ApiModelProperty("账号")
    private String account;
    @ApiModelProperty("登录密码")
    private String password;
    @ApiModelProperty("手机")
    private String phone;
    @ApiModelProperty("语言")
    private Integer language;
    @ApiModelProperty("头像")
    private String headImg;
    //帐号状态（1：启用，其他：禁用）
    @ApiModelProperty("帐号状态（1：启用，其他：禁用）")
    private Integer state;
    @ApiModelProperty("注册ip")
    private String registerIp;
    @ApiModelProperty("收款卡张数")
    private Integer creditCard;
    @ApiModelProperty("配置收款卡等级")//null 完全随机  或者 A|B|C|D
    private String cardLevel;
    @ApiModelProperty("取款密码")
    private String withdrawPassword;
    @ApiModelProperty("邮箱")
    private String email;
    @ApiModelProperty("qq")
    private String qq;
    @ApiModelProperty("微信")
    private String webChat;
    @ApiModelProperty("真实姓名")
    private String realName;
    @JsonIgnore
    @Transient
    private String token;
    //校验用户帐号权限
    public static boolean checkUser(User user) {
        if (user == null) {
            return false;
        }

        if (!Constants.open.equals(user.getState())) {
            return false;
        }
        return true;
    }

    /**
     * 校验用户信息。长度3-15位
     *
     * @return
     */
    public static boolean checkLength(String... strs) {
        if (ObjectUtils.isEmpty(strs)) {
            return false;
        }
        for (String str : strs) {
            if (ObjectUtils.isEmpty(strs)) {
                return false;
            }

            int length = str.length();
            if (length < 3 || length > 15) {
                return false;
            }
        }

        return true;
    }

    public static boolean checkAccountLength(String account) {
        if (account == null) {
            return false;
        }

        int length = account.length();
        if (length < 6 || length > 15) {
            return false;
        }

        String regExp = "^[\\w]{6,15}$";
        if (!account.matches(regExp)) {
            return false;
        }

        return true;
    }

    public static boolean checkPasswordLength(String password) {
        if (password == null) {
            return false;
        }

        int length = password.length();
        if (length < 6 || length > 15) {
            return false;
        }

        String regExp = "^[\\w]{6,15}$";
        if (!password.matches(regExp)) {
            return false;
        }

        return true;
    }

    @JsonIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("admin");
        authorities.add(authority);
        return authorities;
    }

    @JsonIgnore
    @Override
    public String getUsername() {
        return getAccount();
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isEnabled() {
        return true;
    }
}

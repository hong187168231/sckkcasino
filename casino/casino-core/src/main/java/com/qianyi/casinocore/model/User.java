package com.qianyi.casinocore.model;

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

    private String name;
    @Column(unique = true)
    private String account;
    private String password;
    private String phone;
    private Integer language;
    private String headImg;

    //帐号状态（1：启用，其他：禁用）
    private Integer state;
    private String registerIp;
    @Column(columnDefinition = "Decimal(10,2) default '0.00'")
    private BigDecimal money;

    @ApiModelProperty(value = "打码量")
    @Column(columnDefinition = "Decimal(10,2) default '0.00'")
    private BigDecimal codeNum;

    @Column(columnDefinition = "Decimal(10,2) default '0.00'")
    private BigDecimal withdrawMoney;

    private String withdrawPassword;

    @Transient
    private String token;

    //校验用户帐号权限
    public static boolean checkUser(User user) {
        if (user == null) {
            return false;
        }

        if (!Constants.yes.equals(user.getState())) {
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

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("admin");
        authorities.add(authority);
        return authorities;
    }

    @Override
    public String getUsername() {
        return getAccount();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

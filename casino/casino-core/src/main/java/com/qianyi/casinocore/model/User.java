package com.qianyi.casinocore.model;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.ObjectUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
public class User extends BaseEntity {

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

    //校验用户帐号权限
    public static boolean checkUser(User user) {
        if (user == null) {
            return false;
        }

        if (1 != user.getState()) {
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
}

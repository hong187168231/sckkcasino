package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.Entity;

@Data
@Entity
@ApiModel("用户银行卡")
public class Bankcards extends BaseEntity{

    /**
     * 用户名
     */
    @ApiModelProperty(value = "用户id")
    private Long userId;

    /**
     * 银行卡id
     */
    @ApiModelProperty(value = "银行卡id")
    private String bankId;

    /**
     * 用户的银行/支付宝账号
     */
    @ApiModelProperty(value = "用户的银行账号")
    private String bankAccount;

    /**
     * 开户地址
     */
    @ApiModelProperty(value = "开户地址")
    private String address;

    /**
     * 开户名
     */
    @ApiModelProperty(value = "开户名")
    private String realName;

    /**
     * 0:未禁用 1：禁用
     */
    @ApiModelProperty(value = "0:未禁用 1：禁用")
    private Integer disable;

    /**
     * 默认卡，主卡
     */
    @ApiModelProperty(value = "默认卡，主卡= 1")
    private Integer defaultCard;

    /**
     *  针对绑定银行卡接口的参数合法性校验
     * @param bankId
     * @param bankAccount
     * @param address
     * @return
     */
    public static String checkParamFroBound(String accountName,String bankId, String bankAccount,
                                            String address) {
        if(StringUtils.isEmpty(accountName)){
            return "持卡人不能为空";
        }
        if (bankId == null) {
            return "银行id不能为空！";
        }
        if (StringUtils.isEmpty(address)) {
            return "开户地址不能为空！";
        }
        if (StringUtils.isEmpty(bankAccount)) {
            return "银行账号不能为空！";
        }
        if (bankAccount.length() > 20 || bankAccount.length() < 16) {
            return "长度只能在16~20位！";
        }
        return null;
    }
}

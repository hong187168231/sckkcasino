package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.util.StringUtils;

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
     * 银行卡序号
     */
    @ApiModelProperty(value = "银行卡序号")
    private Integer sort;

    /**
     *  针对绑定银行卡接口的参数合法性校验
     * @param bankId
     * @param bankAccount
     * @param address
     * @return
     */
    public static String checkParamFroBound(String accountName,String bankId, String bankAccount,
                                            String address,Integer sort) {
        if(!StringUtils.hasLength(accountName)){
            return "持卡人不能为空";
        }
        if (bankId == null) {
            return "银行id不能为空！";
        }
        if (!StringUtils.hasLength(address)) {
            return "开户地址不能为空！";
        }
        if (!StringUtils.hasLength(bankAccount)) {
            return "银行账号不能为空！";
        }
        if (bankAccount.length() > 20 || bankAccount.length() < 16) {
            return "长度只能在16~20位！";
        }
        if (sort == null) {
            return "银行卡序号不允许为空！";
        }
        if (sort < 1 || sort > 6) {
            return "银行卡序号只能再1~6之间！";
        }
        return null;
    }
}

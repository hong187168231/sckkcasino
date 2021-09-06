package com.qianyi.casinocore.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.Column;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class UserMoreDetailVo implements Serializable {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 用户名称
     */
    @Column(unique = true)
    private String userName;

    /**
     * 注册时间
     */
    @LastModifiedDate
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date registerTime;

    /**
     * 客户身份
     * :0：玩家 1：总代，2：一代 3：二代 4：三代
     */
    private Integer agentLevel;

    /**
     * 上级代理
     */
    private String parentAgent;

    /**
     * 所属总代
     */
    private String generalAgent;

    /**
     * 下线人数
     */
    private Integer downLineTotal;

    /**
     * 风险等级
     * 0黑名单，1：白名单，2：灰名单
     */
    private Integer riskLevel;

    /**
     * 返点比例
     */
    private Double rebateProportion;

    /**
     * VIP等级
     */
    private Integer vipLevel;

    /**
     * 用户状态， 1：正常，2：冻结资金，3：冻结账户
     */
    private Integer status;

    /**
     * 账户余额
     */
    private BigDecimal balance;

    /**
     * 可用余额
     */
    private BigDecimal availableBalance;

    /**
     * 冻结金额
     */
    private BigDecimal frozenBalance;

    /**
     * 代理线
     */
    private String agentLine;

    /**
     * 备注
     */
    private String remark;

    /**
     * 冻结备注
     */
    private String frozenRemark;

    /**
     * 手机号码
     */
    private String phone;

    /**
     * 下线人数
     */
    private Long downLineCount;
}

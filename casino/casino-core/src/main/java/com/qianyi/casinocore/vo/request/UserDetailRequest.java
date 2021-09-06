package com.qianyi.casinocore.vo.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

@Data
@ApiModel(value = "用户列表")
public class UserDetailRequest implements Serializable {

    private static final long serialVersionUID = -20227768637928998L;

    @ApiModelProperty(value = "分页-每页笔数", required = true)
    private Integer size;

    @ApiModelProperty(value = "分页-当前页", required = true)
    private Integer current;

    /**
     * 注册开始时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date registerStartTime;

    /**
     * 注册结束时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date registerEndTime;

    /**
     * 客户身份
     * 玩家，总代，一代，二代，三代
     */
    private Integer agentLevel;

    /**
     * 风险等级
     * 0黑名单，1：白名单，2：灰名单
     */
    private Integer riskLevel;

    /**
     * VIP等级
     */
    private Integer vipLevel;

    /**
     * 用户状态， 1：正常，2：冻结资金，3：冻结账户
     */
    private Integer status;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 用户名称
     */
    private String userName;


}

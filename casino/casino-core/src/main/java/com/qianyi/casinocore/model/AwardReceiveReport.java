//package com.qianyi.casinocore.model;
//
//import io.swagger.annotations.ApiModel;
//import io.swagger.annotations.ApiModelProperty;
//import lombok.Data;
//
//import javax.persistence.*;
//import java.math.BigDecimal;
//import java.util.Date;
//
//@Data
//@Entity
//@ApiModel("以代理为维度统计的报表")
//@Table(indexes = {@Index(columnList = "firstProxy"),@Index(columnList = "secondProxy"),@Index(columnList = "thirdProxy"),
//    @Index(columnList = "orderTimes")})
//public class AwardReceiveReport {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @ApiModelProperty("奖励类型 1 每日奖励 2 升级奖励")
//    private Integer awardType;
//
//    @ApiModelProperty("金额")
//    private BigDecimal amount;
//
//    @ApiModelProperty(value = "统计时间段yyyy-MM-dd(以美东时间为维度统计一天) 1 每日奖励create_time   2 升级奖励receiveTime")
//    @Temporal(value = TemporalType.DATE)
//    private Date orderTimes;
//
//    @ApiModelProperty("总代ID")
//    private Long firstProxy;
//
//    @ApiModelProperty("区域代理ID")
//    private Long secondProxy;
//
//    @ApiModelProperty("基层代理ID")
//    private Long thirdProxy;
//}

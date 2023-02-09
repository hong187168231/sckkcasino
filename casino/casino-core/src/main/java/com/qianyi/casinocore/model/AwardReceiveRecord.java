package com.qianyi.casinocore.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Entity
@Table(indexes = {@Index(columnList = "createTime"),@Index(columnList = "receiveTime"),@Index(columnList = "userId"),@Index(columnList = "thirdProxy")})
public class AwardReceiveRecord extends BaseEntity {

    @ApiModelProperty("会员id")
    private Long userId;

    @ApiModelProperty("奖励类型 1 每日奖励 2 升级奖励")
    private Integer awardType;

    @ApiModelProperty("奖励领取等级")
    private Integer level;

    @ApiModelProperty("领取状态 0 未领取 1 已领取")
    private Integer receiveStatus;

    @ApiModelProperty("金额")
    private BigDecimal amount;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date receiveTime;

    @ApiModelProperty("总代ID")
    private Long firstProxy;

    @ApiModelProperty("区域代理ID")
    private Long secondProxy;

    @ApiModelProperty("基层代理ID")
    private Long thirdProxy;

}

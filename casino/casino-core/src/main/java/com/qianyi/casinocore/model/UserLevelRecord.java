package com.qianyi.casinocore.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import java.util.Date;

@Data
@Entity
@Table(indexes = {@Index(columnList = "createTime")})
public class UserLevelRecord extends BaseEntity {

    @ApiModelProperty("会员id")
    private Long userId;

    @ApiModelProperty("变更前等级")
    private Integer beforeLevel;

    @ApiModelProperty("当前等级")
    private Integer level;

    @ApiModelProperty("1 升级 2 降级")
    private Integer changeType;

    @ApiModelProperty("流水进度")
    private String schedule;

    @ApiModelProperty("流水进度")
    private String keepSchedule;

    @ApiModelProperty("今日保级数据处理状态 0 未处理 1 已处理")
    private Integer todayKeepStatus;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date riseTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date dropTime;



}

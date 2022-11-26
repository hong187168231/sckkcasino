package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

@Data
@Entity
@Table(indexes = {@Index(columnList = "createTime")})
public class UserLevelDecline extends BaseEntity {

    @ApiModelProperty("用户id")
    private Long userId;

    @ApiModelProperty("今日保级数据处理状态 0 未处理 1 已处理")
    private Integer todayDeclineStatus;


}

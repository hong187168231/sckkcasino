package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.math.BigDecimal;

/**
 * @author jordan
 */
@Entity
@Data
@ApiModel("洗码明细表")
@Table(name ="level_water_change",uniqueConstraints={@UniqueConstraint(columnNames={"gameId","gameRecordId"})},
	indexes = {@Index(columnList = "userId,platform,createTime"),@Index(columnList = "platform"),@Index(columnList = "createTime")})
public class LevelWaterChange extends BaseEntity {

	@ApiModelProperty(value = "用户ID")
	private Long userId;

	@ApiModelProperty(value = "游戏记录ID")
	private Long gameRecordId;

	@ApiModelProperty(value = "平台:wm,PG,CQ9")
	private String platform;

	@ApiModelProperty(value = "游戏ID")
	private String gameId;

	@ApiModelProperty(value = "游戏名称")
	private String gameName;

	@ApiModelProperty(value = "有效投注流水")
	private BigDecimal betWater;



}
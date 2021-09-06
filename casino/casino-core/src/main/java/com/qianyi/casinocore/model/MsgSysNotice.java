package com.qianyi.casinocore.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import io.swagger.annotations.ApiModel;
import lombok.Data;

@Entity
@Data
@ApiModel("消息：系统公告消息")//用户消息表：MsgUserNotice 消息推送 起名MsgSysPush
public class MsgSysNotice {

	@Column(unique = true)
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	/**
	 * 标题
	 */
	private String title;
	
	/**
	 * 公告内容
	 */
	private String context;

	/**
	 * 开始时间
	 */
	@Temporal(TemporalType.TIMESTAMP)
	private Date startTime;

	/**
	 * 结束时间
	 */
	@Temporal(TemporalType.TIMESTAMP)
	private Date endTime;

	/**
	 * 弹窗类型 ： 0 公告 列表 1：弹窗
	 */
	private String showType;

	/**
	 * 消息状态 ： 1：开启 0：关闭
	 */
	private Integer status;
	
	/**
	 * 消息通知类型: 0：所有用户显示，1：指定的用户
	 */
	private Integer noticeType;

	/**
	 *创建人
	 */
	private String createBy;

	/**
	 * 创建时间/绑定时间
	 */
	@Temporal(TemporalType.TIMESTAMP)
	private Date createTime;
	
	/**
	 * 更新时间
	 */
	@Temporal(TemporalType.TIMESTAMP)
	private Date updateTime;

	/**
	 * 更新人
	 */
	private String updateBy;
	
}

package com.qianyi.casinocore.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.qianyi.casinocore.model.UserLevelRecord;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@AllArgsConstructor
public class LevelChangeBackVo implements Serializable {

    private static final long serialVersionUID = -6875617929250305179L;
    @ApiModelProperty(value = "ID")
    private Long id;
    @ApiModelProperty(value = "用户ID")
    private Long userId;
    @ApiModelProperty(value = "会员账号")
    private String account;

    @ApiModelProperty("变更前等级")
    private Integer beforeLevel;

    @ApiModelProperty("当前等级")
    private Integer level;

    @ApiModelProperty("1 升级 2 降级")
    private Integer changeType;

    @ApiModelProperty("流水进度")
    private String upSchedule;

    @ApiModelProperty("创建时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private Date createTime;
    @ApiModelProperty("创建人")
    private String createBy;
    @ApiModelProperty("最后修改时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
    @ApiModelProperty("最后修改人")
    private String updateBy;
    public LevelChangeBackVo(UserLevelRecord userLevel){
        this.id = userLevel.getId();
        this.userId = userLevel.getUserId();
        this.beforeLevel = userLevel.getBeforeLevel();
        this.level = userLevel.getLevel();
        this.changeType = userLevel.getChangeType();
        this.upSchedule = userLevel.getSchedule();
        this.createTime = userLevel.getCreateTime();
        this.createBy = userLevel.getCreateBy();
        this.updateTime = userLevel.getUpdateTime();
        this.updateBy = userLevel.getUpdateBy();
    }

}

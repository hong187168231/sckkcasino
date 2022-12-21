package com.qianyi.liveob.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 *
 */
@ApiModel(value = "ReqBetRecordDTO", description = "下注记录查询参数模型")
@EqualsAndHashCode(callSuper = true)
@Data
public class ReqBetRecordDTO extends BaseReqModel {

//    @ApiModelProperty(value = "游戏账号，可选", required = false)
//    private String loginName;


////////////////////增量拉单
    @ApiModelProperty(value = "查询开始id。传0即从0开始", required = true)
    private Long lastVersionId;

    @ApiModelProperty(value = "本次请求关联的日期（yyyyMMdd），requestVersionId为0时默认就是商户的开户日期", required = true)
    private Integer lastDate;
}

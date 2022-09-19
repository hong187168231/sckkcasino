package com.qianyi.casinocore.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@ApiModel("转账记录")
public class TransferVo {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @ApiModelProperty(value = "用户名")
    private String userName;

    @ApiModelProperty(value = "订单号")
    private String orderNo;

    @ApiModelProperty(value = "第三方交易编号")
    private String traceId;

    @ApiModelProperty(value = "账变金额")
    private BigDecimal money;

    @ApiModelProperty(value = "账变前金额")
    private BigDecimal beforeMoney;

    @ApiModelProperty(value = "账变后金额")
    private BigDecimal afterMoney;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "账变类型：8.加点，9.扣点")
    private Integer orderType;

}

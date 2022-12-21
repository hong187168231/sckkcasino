package com.qianyi.liveob.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author joy
 * @date 2019/9/12 17:30
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransferPlayerRespDTO {

    @ApiModelProperty("交易单号")
    private String tradeNo;

//    @ApiModelProperty("玩家id")
//    private Long playerId;

    @ApiModelProperty("转账金额")
    private BigDecimal amount;

    @ApiModelProperty("转账状态 0成功 1失败 2转账中")
    private Integer transferStatus;

    @ApiModelProperty("备注")
    private String remark;
}

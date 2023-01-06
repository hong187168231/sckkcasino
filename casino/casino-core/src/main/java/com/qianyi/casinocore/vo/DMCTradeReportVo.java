package com.qianyi.casinocore.vo;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@ApiModel("DMC交易记录")
public class DMCTradeReportVo {

    private Integer id; //大马彩客户ID
    private Integer member_id; //大马彩客户ID
    private Integer merchant_id; //商户ID
    private String bet_number;  //下注号码
    private Integer game_play_id; //下注游戏代号
    private Integer bet_type;   //下注方式 - 0 (Box), 1 (iBox), 2 (Reverse), 3 (Straight)
    private BigDecimal total_amount; //下注金额
    private BigDecimal net_amount; //有效下注金额
    private BigDecimal rebate_amount; //返水金额
    private BigDecimal winning_amount; //盈利金额
    private String lottery_number ; //期号
    private String currency_code ; //货币Code
    private Integer rebate_percentage; //返水百分点
    private Date betting_date;  //下注日期
    private String draw_date;  //开彩日期
    private String draw_number; //开彩期号
    private String ticket_status;   //注单状况- SETTLED (已结算)，UNSETTLED (未结算)
    private String progress_status; //注单进展- IN_PROGRESS (进行中)，ACCEPTED (已接受)，PARTIALLY_ACCEPTED (部分接受)，REJECTED (拒绝)，DELETED (删除)
    private String created_at;  //注单创建时间
    private String updated_at;  //注单修改时间
    private String customer_id; //客户ID
    private String customer_name;   //客户名称
    private String ticket_no;   //注单号


//    private List<TicketSlaves> ticket_slaves;
    private String ticket_slaves;

}

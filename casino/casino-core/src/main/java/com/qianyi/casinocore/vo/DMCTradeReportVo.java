package com.qianyi.casinocore.vo;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@ApiModel("DMC交易记录")
public class DMCTradeReportVo {

    private Integer merchant_id;
    private Long ticket_id;
    private String ticket_no;
    private String bet_number;
    private Integer bet_type;
    private BigDecimal total_amount;
    private BigDecimal net_amount;
    private BigDecimal rebate_amount;
    private Integer rebate_percentage;
    private String draw_date;
    private Integer draw_number;
    private String ticket_status;
    private String created_at;
    private String customer_id;
    private String customer_name;

    private List<TicketSlaves> ticket_slaves;

}

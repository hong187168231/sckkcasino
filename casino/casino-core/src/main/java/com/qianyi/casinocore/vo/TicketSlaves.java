package com.qianyi.casinocore.vo;

import lombok.Data;


@Data
public class TicketSlaves {

    private Integer merchant_id;
    private Integer game_play_id;
    private Integer child_ticket_no;
    private Integer lottery_number;
    private String big_bet_amount;
    private String small_bet_amount;
    private String three_a_amount;
    private String three_c_amount;
    private String bet_amount;
    private String bet_net_amount;
    private String rebate_amount;
    private Integer rebate_percentage;
    private String big_3a_amount;
    private String small_3c_amount;
    private Integer game_type;
    private String prize_type;
    private String winning_amount;
    private Integer progress_status;
    private String created_at;
    private String updated_at;
    private String odds;


}

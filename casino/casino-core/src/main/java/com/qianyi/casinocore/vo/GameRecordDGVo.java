package com.qianyi.casinocore.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class GameRecordDGVo implements Serializable {

    private String reference_number;
    private String customer_id;
    private String user_name;
    private String enterprise_id;
    private String company_name;
    private String commission;
    private String number_pattern;
    private String big_bet_amount;
    private String small_bet_amount;
    private String bet_type;
    private String bet_date;
    private String game_id;
    private String game_name;
    private String game_date;
    private String slave_lottery_number;
    private String slave_amount;
    private String bet_size;
    private String slave_status;
    private String slave_net_amount;
    private String lottery_slave_status;
    private String winning_amount;
    private String prize_type;
}

package com.qianyi.casinocore.vo;

import com.qianyi.casinocore.util.Md5Util;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;


@Data
public class TicketSlaves {

    private Integer ticket_id;  //三方子单序号
    private Integer merchant_id;    //商户ID
    private String game_play_id;   //下注游戏代号 1 (Magnum)，2 (Damacai)，3 (Toto)
    private String child_ticket_no;    //子单号
    private String big_bet_amount;      //大 - 下注金额
    private String small_bet_amount;    //小 - 下注金额
    private String three_a_bet_amount;      //3A - 下注金额
    private String three_c_bet_amount;  //3C - 下注金额
    private BigDecimal bet_amount;      //下注金额
    private BigDecimal bet_net_amount;  //bet_net_amount
    private String rebate_amount;   //返水金额
    private Integer rebate_percentage;  //返水百分点
    private String big_3a_amount;   //大3A - 下注金额
    private String small_3a_amount; //小3A - 下注金额
    private String game_type;  //下注模式 - 3D，4D
    private String prize_type;  //中奖奖项 - NO (没中奖)，P1 (首奖)，P2 (次奖)，P3 (三奖)，S (特别奖)，C (安慰奖)
    private String bet_size;    //玩法下注 (忽略) - S (小)，B (大)，3A，3C，Both (双边)
    private BigDecimal winning_amount;  //中奖金额
    private String status;  //状况 - in-process (进行中)，finished (完成)，deleted (删除)
    private String progress_status;    //注单进展- IN_PROGRESS (进行中)，ACCEPTED (已接受)，PARTIALLY_ACCEPTED (部分接受)，REJECTED (拒绝)，DELETED (删除
    private Date betting_date;  //下注日期
    private String lottery_number ; //子单下注号码
    private String draw_number ; //期号
    private String currency_code ; //货币Code
    private String created_at;  //注单创建时间
    private String updated_at;  //注单修改时间
    private String deleted_at;  //注单删除时间
    private BigDecimal odds;    //中奖赔率


    public String getMd5(){
        StringBuffer sb = new StringBuffer();
        sb.append(this.ticket_id).append(this.merchant_id).append(this.child_ticket_no)
                .append(this.big_bet_amount).append(this.small_bet_amount).append(this.three_a_bet_amount)
                .append(this.bet_amount).append(this.bet_net_amount).append(this.rebate_amount)
                .append(this.status).append(this.progress_status).append(this.betting_date)
                .append(this.lottery_number).append(this.draw_number).append(this.created_at)
                .append(this.updated_at).append(this.odds);

        return Md5Util.md5(sb.toString());
    }

}

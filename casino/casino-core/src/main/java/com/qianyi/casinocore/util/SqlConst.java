package com.qianyi.casinocore.util;

public class SqlConst {
    public static String dataSql = "select \n" + "u.account , \n" + "u.third_proxy , \n" + " u.id,\n"
        + " ifnull(main_t.num,0) num,\n" + " ifnull(main_t.bet_amount,0) bet_amount ,\n"
        + " ifnull(main_t.validbet,0) validbet ,\n" + " ifnull(main_t.win_loss,0) win_loss ,\n"
        + " ifnull(wash_t.wash_amount,0) wash_amount, \n"
        + " ifnull(withdraw_t.service_charge,0) service_charge, \n" + " ifnull(pr.amount,0) all_profit_amount, \n"
        + " -(ifnull(main_t.win_loss,0)+ifnull(wash_t.wash_amount,0)) avg_benefit, \n"
        + " -(ifnull(main_t.win_loss,0)+ifnull(wash_t.wash_amount,0))-ifnull(pr.amount,0)+ifnull(withdraw_t.service_charge,0) total_amount\n"
        + "from user u\n" + "left join ( \n" + "  select user_id , \n" + "  count(1) num, \n"
        + "  sum(bet) bet_amount, \n" + "  sum(validbet) validbet , \n" + "  sum(win_loss) win_loss  \n"
        + "  from game_record gr \n"
        + "  where bet_time >= {0} and bet_time <= {1}\n"
        + "  group by user_id \n" + " ) main_t on u.id = main_t.user_id\n" + " left join ( \n"
        + "  select user_id , sum(amount) wash_amount  \n" + "  from wash_code_change wcc  \n"
        + "  where create_time >= {0} and create_time <= {1}\n"
        + "  group by user_id \n" + " ) wash_t on u.id = wash_t.user_id\n" + " left join ( \n"
        + "  select user_id , sum(ifnull(service_charge,0)) service_charge  \n" + "  from withdraw_order wo  \n"
        + "  where update_time >= {0} and update_time <= {1}\n"
        + "  group by user_id \n" + " ) withdraw_t on u.id = withdraw_t.user_id\n" + " left join ( \n"
        + "  select user_id , sum(amount) amount from share_profit_change spc  \n"
        + "  where create_time >= {0} and create_time <= {1}\n"
        + "  group by user_id  \n" + " ) pr on u.id=pr.user_id \n" + " where 1=1 {2} \n" + "limit {3},{4}";
}

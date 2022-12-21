package com.qianyi.casinocore.util;

public class SqlSumConst {

    public static String WMSumSql = """
    SELECT
    count(1) num,
    ifnull(sum( bet ),0) bet_amount,
    ifnull(sum( validbet ),0) validbet,
    ifnull(sum( win_loss ),0) win_loss
        FROM
    game_record gr
    WHERE
    bet_time BETWEEN {0}
    AND {1}
        """;

    public static String obdjSumSql = """
    SELECT
    count(1) num,
    ifnull(sum( bet_amount ),0) bet_amount,
    ifnull(sum( bet_amount ),0) validbet,
    ifnull(sum( win_amount - bet_amount ),0) win_loss
        FROM
    game_record_obdj grg
    WHERE
    bet_status IN ( 5, 6, 8, 9, 10 )
    AND set_str_time BETWEEN {0}
    AND {1}
        """;

    public static String obtySumSql = """
    SELECT
    count(1) num,
    ifnull(sum( order_amount ),0) bet_amount,
    ifnull(sum( order_amount ),0) validbet,
    ifnull(sum( profit_amount ),0) win_loss
        FROM
    game_record_obty grg
    WHERE
    settle_str_time BETWEEN {0}
    AND {1}
        """;

    public static String PGAndCQ9SumSql = """
    SELECT
    count(1) num,
    ifnull(sum( bet_amount ),0) bet_amount,
    ifnull(sum( bet_amount ),0) validbet,
    ifnull(sum( win_amount - bet_amount ),0) win_loss
        FROM
    game_record_goldenf grg
    WHERE
        vendor_code = {2}
    AND create_at_str BETWEEN {0}
    AND {1}
        """;

    public static String aeSumMergeSql = """
    SELECT
    count(1) num,
    ifnull( sum( bet_amount ), 0 ) bet_amount,
    ifnull( sum( turnover ), 0 ) validbet,
    ifnull( sum( real_win_amount ), 0 )- ifnull( sum( real_bet_amount ), 0 ) win_loss
        FROM
    game_record_ae grg
    WHERE
        tx_status = 1
    AND tx_time BETWEEN {0}
    AND {1}
        """;

//    public static String sabasportSumSql = """
//    SELECT
//    count( DISTINCT sk.bet_id ) num,
//    ifnull( SUM( sk.bet_amount ), 0 ) bet_amount,
//    ifnull( SUM( sk.bet_amount ), 0 ) validbet,
//    ifnull(sum(off.win_amount), 0 )-ifnull(sum( sk.bet_amount ), 0 )+ifnull(sum(t3.win_amount), 0 ) win_loss
//    FROM
//        (
//            SELECT
//                bet_id bet_id,
//            SUM( win_amount ) win_amount
//    FROM
//    game_record_goldenf t1
//    WHERE
//    t1.vendor_code = {2}
//    AND t1.trans_type = {3}
//    AND t1.create_at_str BETWEEN {0}
//    AND {1}
//    GROUP BY
//    t1.bet_id
//	) off
//    LEFT JOIN ( SELECT bet_amount, bet_id FROM game_record_goldenf WHERE vendor_code = {2} AND trans_type = {4} ) sk ON off.bet_id = sk.bet_id
//    LEFT JOIN game_record_goldenf t3 ON off.bet_id = t3.bet_id
//    AND t3.trans_type = {5}
//        """;

    public static String sabasportSumSql = """
    SELECT
    count( DISTINCT sk.bet_id ) num,
    ifnull( SUM( sk.bet_amount ), 0 ) bet_amount,
    ifnull( SUM( sk.bet_amount ), 0 ) validbet,
    ifnull(sum(off.win_amount), 0 )-ifnull(sum( sk.bet_amount ), 0 )+ifnull(sum(t3.win_amount), 0 ) win_loss
    FROM
        (
            SELECT
                bet_id bet_id,
            SUM( win_amount ) win_amount
    FROM
    game_record_goldenf t1
    WHERE
    t1.vendor_code = {2}
    AND t1.trans_type = {3}
    AND t1.create_at_str BETWEEN {0}
    AND {1}
    GROUP BY
    t1.bet_id
	) off
    LEFT JOIN ( SELECT bet_amount, bet_id FROM game_record_goldenf WHERE vendor_code = {2} AND trans_type = {4} ) sk ON off.bet_id = sk.bet_id
    LEFT JOIN ( SELECT SUM(win_amount) win_amount, bet_id FROM game_record_goldenf WHERE vendor_code = {2} AND trans_type = {5} GROUP BY bet_id) t3
    ON off.bet_id = t3.bet_id
        """;

    public static String sumSql = """
    select
    wash_t.wash_amount as wash_amount,
    withdraw_t.service_charge as service_charge,
    pr.amount as all_profit_amount,
    ec.water as all_water ,td.todayAward,rs.riseAward from
        (select
            ifnull(sum(amount),0) wash_amount
    from wash_code_change wcc
    where create_time between {0} and {1}) wash_t,
        (select
    ifnull(sum(service_charge),0) service_charge
    from withdraw_order wo
    where status = 1 and withdraw_time between {0} and {1}) withdraw_t,
        (select
    ifnull(sum(amount),0) amount from share_profit_change spc
    where bet_time between {0} and {1}) pr,
        (SELECT
    ifnull(SUM(amount),0) as water
    FROM extract_points_change
    where create_time between {0} and {1}) ec,
    	(SELECT IFNULL(SUM(amount),0) as todayAward FROM
			 award_receive_record WHERE award_type = 1 and create_time between {0} and {1} ) td,
			(SELECT IFNULL(SUM(amount),0) as riseAward FROM
			 award_receive_record WHERE award_type = 2 and  receive_time between {0} and {1}) rs
    ;
        """;

    public static String sumRebateSql = """
    select
    sum(ifnull(rd_t.total_amount,0)) total_rebate,
    sum(ifnull(rd_t.user_amount,0)) user_amount,
    sum(ifnull(rd_t.surplus_amount,0)) surplus_amount,
    sum(ifnull(withdraw_t.service_charge,0)) service_charge from
        (select user_id ,
            sum(total_amount) total_amount,
    sum(user_amount) user_amount,
    sum(surplus_amount) surplus_amount
    from rebate_detail rd
    where create_time between {0} and {1}) rd_t,
        ( select user_id ,
    sum(ifnull(service_charge,0)) service_charge
    from withdraw_order wo
    where status = 1 and withdraw_time between {0} and {1}) withdraw_t;
        """;

    public static String vncSumMergeSql = """
    SELECT
    count(1) num,
    ifnull( sum( bet_money ), 0 ) bet_amount,
    ifnull( sum( real_money ), 0 ) validbet,
    ifnull( sum( win_money ), 0 )- ifnull( sum( real_money ), 0 ) win_loss
        FROM
    rpt_bet_info_detail grv
    WHERE
    settle_time BETWEEN {0}
    AND {1}
        """;
    public static String dmcSumMergeSql = """
    SELECT
    count(1) num,
    ifnull( sum( bet_money ), 0 ) bet_amount,
    ifnull( sum( real_money ), 0 ) validbet,
    ifnull( sum( win_money ), 0 )- ifnull( sum( real_money ), 0 ) win_loss
        FROM
    game_record_dmc grv
    WHERE
    bet_time BETWEEN {0}
    AND {1}
        """;
    public static String dgSumMergeSql = """
    SELECT
    count(1) num,
    ifnull( sum( bet_points ), 0 ) bet_amount,
    ifnull( sum( available_bet ), 0 ) validbet,
    ifnull( sum( win_money ), 0 )- ifnull( sum( real_money ), 0 ) win_loss
        FROM
    game_record_dg grv
    WHERE
    bet_time BETWEEN {0}
    AND {1}
        """;
}

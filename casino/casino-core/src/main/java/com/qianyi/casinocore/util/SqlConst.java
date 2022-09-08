package com.qianyi.casinocore.util;

public class SqlConst {
    public static String wmSql = """
    select
    u.account ,
    u.third_proxy ,
    u.id,
    ifnull(main_t.num,0) num,
    ifnull(main_t.bet_amount,0) bet_amount ,
    ifnull(main_t.validbet,0) validbet ,
    ifnull(main_t.win_loss,0) win_loss ,
    ifnull(wash_t.wash_amount,0) wash_amount,
    ifnull(withdraw_t.service_charge,0) service_charge,
    ifnull(pr.amount,0) all_profit_amount,
              -(ifnull(main_t.win_loss,0)+ifnull(wash_t.wash_amount,0)+ifnull(ec.water, 0)) avg_benefit,
        -(ifnull(main_t.win_loss,0)+ifnull(wash_t.wash_amount,0))-ifnull(pr.amount,0)-ifnull(ec.water,0)+ifnull(withdraw_t.service_charge,0) total_amount,
    ifnull(ec.water, 0) all_water
    from user u left join (
        select user_id ,
        count(1) num,
    sum(bet) bet_amount,
    sum(validbet) validbet ,
    sum(win_loss) win_loss
    from game_record gr
    where bet_time between {0} and {1}
    group by user_id
                  ) main_t on u.id = main_t.user_id
    left join (
        select user_id ,
        sum(amount) wash_amount
    from wash_code_change wcc
    where platform = {5} and create_time between {0} and {1}
    group by user_id
                ) wash_t on u.id = wash_t.user_id
    left join (
        select user_id ,
        sum(ifnull(service_charge,0)) service_charge
    from withdraw_order wo
    where status = 1 and withdraw_time between {0} and {1}
    group by user_id
                ) withdraw_t on u.id = withdraw_t.user_id
    left join (
        select user_id ,
        sum(amount) amount
    from share_profit_change spc
    where bet_time between {0} and {1}
    group by user_id
                ) pr on u.id=pr.user_id
    left join (
        SELECT user_id,
        SUM(amount) as water
    FROM extract_points_change
    where create_time between {0} and {1}
    and platform = {5}
    group by user_id
                ) ec on u.id = ec.user_id
    where 1=1{6} {2}
    limit {3},{4}
            """;

    public static String exportWmSql = """
    select
    u.account ,
    u.third_proxy ,
    u.id,
    ifnull(main_t.num,0) num,
    ifnull(main_t.bet_amount,0) bet_amount ,
    ifnull(main_t.validbet,0) validbet ,
    ifnull(main_t.win_loss,0) win_loss ,
    ifnull(wash_t.wash_amount,0) wash_amount,
    ifnull(withdraw_t.service_charge,0) service_charge,
    ifnull(pr.amount,0) all_profit_amount,
              -(ifnull(main_t.win_loss,0)+ifnull(wash_t.wash_amount,0)+ifnull(ec.water, 0)) avg_benefit,
        -(ifnull(main_t.win_loss,0)+ifnull(wash_t.wash_amount,0))-ifnull(pr.amount,0)-ifnull(ec.water,0)+ifnull(withdraw_t.service_charge,0) total_amount,
    ifnull(ec.water, 0) all_water
    from user u left join (
        select user_id ,
        count(1) num,
    sum(bet) bet_amount,
    sum(validbet) validbet ,
    sum(win_loss) win_loss
    from game_record gr
    where bet_time between {0} and {1}
    group by user_id
                  ) main_t on u.id = main_t.user_id
    left join (
        select user_id ,
        sum(amount) wash_amount
    from wash_code_change wcc
    where platform = {3} and create_time between {0} and {1}
    group by user_id
                ) wash_t on u.id = wash_t.user_id
    left join (
        select user_id ,
        sum(ifnull(service_charge,0)) service_charge
    from withdraw_order wo
    where status = 1 and withdraw_time between {0} and {1}
    group by user_id
                ) withdraw_t on u.id = withdraw_t.user_id
    left join (
        select user_id ,
        sum(amount) amount
    from share_profit_change spc
    where bet_time between {0} and {1}
    group by user_id
                ) pr on u.id=pr.user_id
    left join (
        SELECT user_id,
        SUM(amount) as water
    FROM extract_points_change
    where platform = {3} and create_time between {0} and {1}
    group by user_id
                ) ec on u.id = ec.user_id
    where 1=1{4} {2}
            """;

    public static String totalSqlReport = """
    select
    u.account ,
    u.third_proxy ,
    u.id,
    ifnull(main_t.num,0) num,
    ifnull(main_t.bet_amount,0) bet_amount ,
    ifnull(main_t.validbet,0) validbet ,
    ifnull(main_t.win_loss,0) win_loss ,
    ifnull(wash_t.wash_amount,0) wash_amount,
    ifnull(withdraw_t.service_charge,0) service_charge,
    ifnull(pr.amount,0) all_profit_amount,
    -(ifnull(main_t.win_loss,0)+ifnull(wash_t.wash_amount,0)+ifnull(ec.water,0)) avg_benefit,
        -(ifnull(main_t.win_loss,0)+ifnull(wash_t.wash_amount,0))-ifnull(pr.amount,0) -ifnull(ec.water,0) +ifnull(withdraw_t.service_charge,0) total_amount,
    ifnull(ec.water, 0) all_water
    from user u left join (
        select user_id ,
        SUM(betting_number) num,
    sum(bet_amount) bet_amount,
    sum(valid_amount) validbet ,
    sum(win_loss) win_loss
    from proxy_game_record_report gr
    where order_times between {5} and {6}
    group by user_id
                ) main_t on u.id = main_t.user_id
    left join (
        select user_id ,
        sum(amount) wash_amount
    from wash_code_change wcc
    where create_time between {0} and {1}
    group by user_id   ) wash_t on u.id = wash_t.user_id
    left join (
        select user_id ,
        sum(ifnull(service_charge,0)) service_charge
    from withdraw_order wo
    where status = 1 and withdraw_time between {0} and {1}
    group by user_id
                ) withdraw_t on u.id = withdraw_t.user_id
    left join (
        select user_id ,
        sum(amount) amount
    from share_profit_change spc
    where bet_time between {0} and {1}
    group by user_id
                ) pr on u.id=pr.user_id
    left join (
        SELECT user_id,
        SUM(amount) as water
    FROM extract_points_change
    where create_time between {0} and {1}
    group by user_id
              ) ec on u.id = ec.user_id
    where  1=1{7} {2}
    limit {3},{4}
            """;

    //    public static String totalSql = """
    //    select
    //    u.account ,
    //    u.third_proxy ,
    //    u.id,
    //    ifnull(main_t.num,0)+ifnull(goldenf_t.num,0)+ifnull(grobdj_t.num,0)+ifnull(grobty_t.num,0) num,
    //    ifnull(main_t.bet_amount,0)+ifnull(goldenf_t.bet_amount,0)+ifnull(grobdj_t.bet_amount,0)+ifnull(grobty_t.bet_amount,0) bet_amount ,
    //    ifnull(main_t.validbet,0)+ifnull(goldenf_t.bet_amount,0)+ifnull(grobdj_t.bet_amount,0)+ifnull(grobty_t.bet_amount,0) validbet ,
    //    ifnull(main_t.win_loss,0)+ifnull(goldenf_t.win_loss,0)+ifnull(grobdj_t.win_loss,0)+ifnull(grobty_t.win_loss,0) win_loss ,
    //    ifnull(wash_t.wash_amount,0) wash_amount,
    //    ifnull(withdraw_t.service_charge,0) service_charge,
    //    ifnull(pr.amount,0) all_profit_amount,
    //            -(ifnull(main_t.win_loss,0)+ifnull(goldenf_t.win_loss,0)+ifnull(grobdj_t.win_loss,0)+ifnull(grobty_t.win_loss,0)+ifnull(wash_t.wash_amount,0)+ifnull(ec.water,0)) avg_benefit,
    //        -(ifnull(main_t.win_loss,0)+ifnull(goldenf_t.win_loss,0)+ifnull(grobdj_t.win_loss,0)+ifnull(grobty_t.win_loss,0)+ifnull(wash_t.wash_amount,0))-ifnull(pr.amount,0) -ifnull(ec.water,0) +ifnull(withdraw_t.service_charge,0) total_amount,
    //    ifnull(ec.water, 0) all_water
    //    from user u left join (
    //        select user_id ,
    //        count(1) num,
    //    sum(bet) bet_amount,
    //    sum(validbet) validbet ,
    //    sum(win_loss) win_loss
    //    from game_record gr
    //    where bet_time >= {0} and bet_time <= {1}
    //    group by user_id
    //                ) main_t on u.id = main_t.user_id
    //    left join (
    //        select user_id ,
    //        count(1) num,
    //    sum(bet_amount) bet_amount,
    //    sum(win_amount-bet_amount) win_loss
    //    from game_record_goldenf grg
    //    where create_at_str >= {0} and create_at_str <= {1}
    //    group by user_id
    //                ) goldenf_t on u.id = goldenf_t.user_id
    //    left join (
    //        select user_id ,
    //        count(1) num,
    //    sum(bet_amount) bet_amount,
    //    sum(win_amount-bet_amount) win_loss
    //    from game_record_obdj grobdj
    //    where bet_status in (5,6,8,9,10) and set_str_time >= {0} and set_str_time <= {1}
    //    group by user_id
    //                ) grobdj_t on u.id = grobdj_t.user_id
    //    left join (
    //        select user_id ,
    //        count(1) num,
    //    sum(order_amount) bet_amount,
    //    sum(profit_amount) win_loss
    //    from game_record_obty grobty
    //    where settle_str_time >= {0} and settle_str_time <= {1}
    //    group by user_id
    //                ) grobty_t on u.id = grobty_t.user_id
    //    left join (
    //        select user_id ,
    //        sum(amount) wash_amount
    //    from wash_code_change wcc
    //    where create_time >= {0} and create_time <= {1}
    //    group by user_id   ) wash_t on u.id = wash_t.user_id
    //    left join (
    //        select user_id ,
    //        sum(ifnull(service_charge,0)) service_charge
    //    from withdraw_order wo
    //    where status = 1 and withdraw_time >= {0} and withdraw_time <= {1}
    //    group by user_id
    //                ) withdraw_t on u.id = withdraw_t.user_id
    //    left join (
    //        select user_id ,
    //        sum(amount) amount
    //    from share_profit_change spc
    //    where bet_time >= {0} and bet_time <= {1}
    //    group by user_id
    //                ) pr on u.id=pr.user_id
    //    left join (
    //        SELECT user_id,
    //        SUM(amount) as water
    //    FROM extract_points_change
    //    where create_time >= {0} and create_time <= {1}
    //    group by user_id
    //              ) ec on u.id = ec.user_id
    //    where  1=1{5} {2}
    //    limit {3},{4}
    //            """;

    public static String exportTotalSqlReport = """
    select
    u.account ,
    u.third_proxy ,
    u.id,
    ifnull(main_t.num,0) num,
    ifnull(main_t.bet_amount,0) bet_amount ,
    ifnull(main_t.validbet,0) validbet ,
    ifnull(main_t.win_loss,0) win_loss ,
    ifnull(wash_t.wash_amount,0) wash_amount,
    ifnull(withdraw_t.service_charge,0) service_charge,
    ifnull(pr.amount,0) all_profit_amount,
    -(ifnull(main_t.win_loss,0)+ifnull(wash_t.wash_amount,0)+ifnull(ec.water,0)) avg_benefit,
        -(ifnull(main_t.win_loss,0)+ifnull(wash_t.wash_amount,0))-ifnull(pr.amount,0) -ifnull(ec.water,0) +ifnull(withdraw_t.service_charge,0) total_amount,
    ifnull(ec.water, 0) all_water
    from user u left join (
        select user_id ,
        SUM(betting_number) num,
    sum(bet_amount) bet_amount,
    sum(valid_amount) validbet ,
    sum(win_loss) win_loss
    from proxy_game_record_report gr
    where order_times between {3} and {4}
    group by user_id
                ) main_t on u.id = main_t.user_id
    left join (
        select user_id ,
        sum(amount) wash_amount
    from wash_code_change wcc
    where create_time between {0} and {1}
    group by user_id   ) wash_t on u.id = wash_t.user_id
    left join (
        select user_id ,
        sum(ifnull(service_charge,0)) service_charge
    from withdraw_order wo
    where status = 1 and withdraw_time between {0} and {1}
    group by user_id
                ) withdraw_t on u.id = withdraw_t.user_id
    left join (
        select user_id ,
        sum(amount) amount
    from share_profit_change spc
    where bet_time between {0} and {1}
    group by user_id
                ) pr on u.id=pr.user_id
    left join (
        SELECT user_id,
        SUM(amount) as water
    FROM extract_points_change
    where create_time between {0} and {1}
    group by user_id
              ) ec on u.id = ec.user_id
    where  1=1{5} {2}
            """;

    //    public static String exportTotalSql = """
    //    select
    //    u.account ,
    //    u.third_proxy ,
    //    u.id,
    //    ifnull(main_t.num,0)+ifnull(goldenf_t.num,0)+ifnull(grobdj_t.num,0)+ifnull(grobty_t.num,0) num,
    //    ifnull(main_t.bet_amount,0)+ifnull(goldenf_t.bet_amount,0)+ifnull(grobdj_t.bet_amount,0)+ifnull(grobty_t.bet_amount,0) bet_amount ,
    //    ifnull(main_t.validbet,0)+ifnull(goldenf_t.bet_amount,0)+ifnull(grobdj_t.bet_amount,0)+ifnull(grobty_t.bet_amount,0) validbet ,
    //    ifnull(main_t.win_loss,0)+ifnull(goldenf_t.win_loss,0)+ifnull(grobdj_t.win_loss,0)+ifnull(grobty_t.win_loss,0) win_loss ,
    //    ifnull(wash_t.wash_amount,0) wash_amount,
    //    ifnull(withdraw_t.service_charge,0) service_charge,
    //    ifnull(pr.amount,0) all_profit_amount,
    //            -(ifnull(main_t.win_loss,0)+ifnull(goldenf_t.win_loss,0)+ifnull(grobdj_t.win_loss,0)+ifnull(grobty_t.win_loss,0)+ifnull(wash_t.wash_amount,0)+ifnull(ec.water,0)) avg_benefit,
    //        -(ifnull(main_t.win_loss,0)+ifnull(goldenf_t.win_loss,0)+ifnull(grobdj_t.win_loss,0)+ifnull(grobty_t.win_loss,0)+ifnull(wash_t.wash_amount,0))-ifnull(pr.amount,0) -ifnull(ec.water,0) +ifnull(withdraw_t.service_charge,0) total_amount,
    //    ifnull(ec.water, 0) all_water
    //    from user u left join (
    //        select user_id ,
    //        count(1) num,
    //    sum(bet) bet_amount,
    //    sum(validbet) validbet ,
    //    sum(win_loss) win_loss
    //    from game_record gr
    //    where bet_time >= {0} and bet_time <= {1}
    //    group by user_id
    //                ) main_t on u.id = main_t.user_id
    //    left join (
    //        select user_id ,
    //        count(1) num,
    //    sum(bet_amount) bet_amount,
    //    sum(win_amount-bet_amount) win_loss
    //    from game_record_goldenf grg
    //    where create_at_str >= {0} and create_at_str <= {1}
    //    group by user_id
    //                ) goldenf_t on u.id = goldenf_t.user_id
    //    left join (
    //        select user_id ,
    //        count(1) num,
    //    sum(bet_amount) bet_amount,
    //    sum(win_amount-bet_amount) win_loss
    //    from game_record_obdj grobdj
    //    where bet_status in (5,6,8,9,10) and set_str_time >= {0} and set_str_time <= {1}
    //    group by user_id
    //                ) grobdj_t on u.id = grobdj_t.user_id
    //    left join (
    //        select user_id ,
    //        count(1) num,
    //    sum(order_amount) bet_amount,
    //    sum(profit_amount) win_loss
    //    from game_record_obty grobty
    //    where settle_str_time >= {0} and settle_str_time <= {1}
    //    group by user_id
    //                ) grobty_t on u.id = grobty_t.user_id
    //    left join (
    //        select user_id ,
    //        sum(amount) wash_amount
    //    from wash_code_change wcc
    //    where create_time >= {0} and create_time <= {1}
    //    group by user_id   ) wash_t on u.id = wash_t.user_id
    //    left join (
    //        select user_id ,
    //        sum(ifnull(service_charge,0)) service_charge
    //    from withdraw_order wo
    //    where status = 1 and withdraw_time >= {0} and withdraw_time <= {1}
    //    group by user_id
    //                ) withdraw_t on u.id = withdraw_t.user_id
    //    left join (
    //        select user_id ,
    //        sum(amount) amount
    //    from share_profit_change spc
    //    where bet_time >= {0} and bet_time <= {1}
    //    group by user_id
    //                ) pr on u.id=pr.user_id
    //    left join (
    //        SELECT user_id,
    //        SUM(amount) as water
    //    FROM extract_points_change
    //    where create_time >= {0} and create_time <= {1}
    //    group by user_id
    //              ) ec on u.id = ec.user_id
    //    where  1=1{3} {2}
    //            """;

    //    public static String pgOrCq9Sql = """
    //    select
    //    u.account ,
    //    u.third_proxy ,
    //    u.id,
    //    ifnull(goldenf_t.num,0) num,
    //    ifnull(goldenf_t.bet_amount,0) bet_amount ,
    //    ifnull(goldenf_t.validbet,0) validbet ,
    //    ifnull(goldenf_t.win_loss,0) win_loss ,
    //    ifnull(wash_t.wash_amount,0) wash_amount,
    //    ifnull(withdraw_t.service_charge,0) service_charge,
    //    ifnull(pr.amount,0) all_profit_amount,
    //    -(ifnull(goldenf_t.win_loss,0)+ifnull(wash_t.wash_amount,0)+ifnull(ec.water, 0)) avg_benefit,
    //        -(ifnull(goldenf_t.win_loss,0)+ifnull(wash_t.wash_amount,0))-ifnull(pr.amount,0)-ifnull(ec.water,0)+ifnull(withdraw_t.service_charge,0) total_amount,
    //    ifnull(ec.water, 0) all_water
    //    from user u left join (
    //        select user_id ,
    //        SUM(betting_number) num,
    //    sum(bet_amount) bet_amount,
    //    sum(valid_amount) validbet ,
    //    sum(win_loss) win_loss
    //    from user_game_record_report grg
    //        where
    //    platform = {5}  and   order_times >= {6} and order_times <= {7}
    //    group by user_id
    //              ) goldenf_t on u.id = goldenf_t.user_id
    //    left join (
    //        select user_id ,
    //        sum(amount) wash_amount
    //    from wash_code_change wcc
    //    where create_time >= {0} and create_time <= {1}
    //    and platform = {5}
    //    group by user_id
    //                ) wash_t on u.id = wash_t.user_id
    //    left join (
    //        select user_id ,
    //        sum(ifnull(service_charge,0)) service_charge
    //    from withdraw_order wo
    //    where status = 1 and withdraw_time >= {0} and withdraw_time <= {1}
    //    group by user_id
    //                ) withdraw_t on u.id = withdraw_t.user_id
    //    left join (
    //        select user_id ,
    //        sum(amount) amount
    //    from share_profit_change spc
    //    where bet_time >= {0} and bet_time <= {1}
    //    group by user_id
    //                ) pr on u.id=pr.user_id
    //    left join (
    //        SELECT user_id,
    //        SUM(amount) as water
    //    FROM extract_points_change
    //    where create_time >= {0} and create_time <= {1}
    //    and platform = {5}
    //    group by user_id
    //              ) ec on u.id = ec.user_id
    //    where  1=1{8} {2}
    //    limit {3},{4}
    //            """;

    public static String pgOrCq9Sql = """
    select
    u.account ,
    u.third_proxy ,
    u.id,
    ifnull(goldenf_t.num,0) num,
    ifnull(goldenf_t.bet_amount,0) bet_amount ,
    ifnull(goldenf_t.bet_amount,0) validbet ,
    ifnull(goldenf_t.win_loss,0) win_loss ,
    ifnull(wash_t.wash_amount,0) wash_amount,
    ifnull(withdraw_t.service_charge,0) service_charge,
    ifnull(pr.amount,0) all_profit_amount,
              -(ifnull(goldenf_t.win_loss,0)+ifnull(wash_t.wash_amount,0)+ifnull(ec.water, 0)) avg_benefit,
        -(ifnull(goldenf_t.win_loss,0)+ifnull(wash_t.wash_amount,0))-ifnull(pr.amount,0)-ifnull(ec.water,0)+ifnull(withdraw_t.service_charge,0) total_amount,
    ifnull(ec.water, 0) all_water
    from user u left join (
        select user_id ,
        count(1) num,
    sum(bet_amount) bet_amount,
    sum(win_amount-bet_amount) win_loss
    from game_record_goldenf grg
        where
    vendor_code = {5}  and  create_at_str between {0} and {1}
    group by user_id
              ) goldenf_t on u.id = goldenf_t.user_id
    left join (
        select user_id ,
        sum(amount) wash_amount
    from wash_code_change wcc
    where platform = {5} and create_time between {0} and {1}
    group by user_id
                ) wash_t on u.id = wash_t.user_id
    left join (
        select user_id ,
        sum(ifnull(service_charge,0)) service_charge
    from withdraw_order wo
    where status = 1 and withdraw_time between {0} and {1}
    group by user_id
                ) withdraw_t on u.id = withdraw_t.user_id
    left join (
        select user_id ,
        sum(amount) amount
    from share_profit_change spc
    where bet_time between {0} and {1}
    group by user_id
                ) pr on u.id=pr.user_id
    left join (
        SELECT user_id,
        SUM(amount) as water
    FROM extract_points_change
    where platform = {5} and create_time between {0} and {1}
    group by user_id
              ) ec on u.id = ec.user_id
    where  1=1{6} {2}
    limit {3},{4}
            """;

    public static String sabasportSql = """
    select
    u.account ,
    u.third_proxy ,
    u.id,
    ifnull(goldenf_t.num,0) num,
    ifnull(goldenf_t.bet_amount,0) bet_amount ,
    ifnull(goldenf_t.bet_amount,0) validbet ,
    ifnull(goldenf_t.win_loss,0) win_loss ,
    ifnull(wash_t.wash_amount,0) wash_amount,
    ifnull(withdraw_t.service_charge,0) service_charge,
    ifnull(pr.amount,0) all_profit_amount,
              -(ifnull(goldenf_t.win_loss,0)+ifnull(wash_t.wash_amount,0)+ifnull(ec.water, 0)) avg_benefit,
        -(ifnull(goldenf_t.win_loss,0)+ifnull(wash_t.wash_amount,0))-ifnull(pr.amount,0)-ifnull(ec.water,0)+ifnull(withdraw_t.service_charge,0) total_amount,
    ifnull(ec.water, 0) all_water
    from user u left join (
        SELECT
            off.user_id user_id,
        count( 1 ) num,
    SUM( sk.bet_amount ) bet_amount,
    sum( off.win_amount - sk.bet_amount ) win_loss
    FROM
    game_record_goldenf off
    LEFT JOIN ( SELECT bet_amount, bet_id FROM game_record_goldenf WHERE vendor_code = {5} AND trans_type = {8} ) sk ON off.bet_id = sk.bet_id
        WHERE
    off.vendor_code = {5}
    AND off.trans_type = {7}
    AND off.create_at_str BETWEEN {0}
    AND {1}
    GROUP BY
    user_id) goldenf_t on u.id = goldenf_t.user_id
    left join (
        select user_id ,
        sum(amount) wash_amount
    from wash_code_change wcc
    where platform = {5} and create_time between {0} and {1}
    group by user_id
                ) wash_t on u.id = wash_t.user_id
    left join (
        select user_id ,
        sum(ifnull(service_charge,0)) service_charge
    from withdraw_order wo
    where status = 1 and withdraw_time between {0} and {1}
    group by user_id
                ) withdraw_t on u.id = withdraw_t.user_id
    left join (
        select user_id ,
        sum(amount) amount
    from share_profit_change spc
    where bet_time between {0} and {1}
    group by user_id
                ) pr on u.id=pr.user_id
    left join (
        SELECT user_id,
        SUM(amount) as water
    FROM extract_points_change
    where platform = {5} and create_time between {0} and {1}
    group by user_id
              ) ec on u.id = ec.user_id
    where  1=1{6} {2}
    limit {3},{4}
            """;

    public static String exportPgOrCq9Sql = """
    select
    u.account ,
    u.third_proxy ,
    u.id,
    ifnull(goldenf_t.num,0) num,
    ifnull(goldenf_t.bet_amount,0) bet_amount ,
    ifnull(goldenf_t.bet_amount,0) validbet ,
    ifnull(goldenf_t.win_loss,0) win_loss ,
    ifnull(wash_t.wash_amount,0) wash_amount,
    ifnull(withdraw_t.service_charge,0) service_charge,
    ifnull(pr.amount,0) all_profit_amount,
              -(ifnull(goldenf_t.win_loss,0)+ifnull(wash_t.wash_amount,0)+ifnull(ec.water, 0)) avg_benefit,
        -(ifnull(goldenf_t.win_loss,0)+ifnull(wash_t.wash_amount,0))-ifnull(pr.amount,0)-ifnull(ec.water,0)+ifnull(withdraw_t.service_charge,0) total_amount,
    ifnull(ec.water, 0) all_water
    from user u left join (
        select user_id ,
        count(1) num,
    sum(bet_amount) bet_amount,
    sum(win_amount-bet_amount) win_loss
    from game_record_goldenf grg
        where
    vendor_code = {3}  and create_at_str between {0} and {1}
    group by user_id
              ) goldenf_t on u.id = goldenf_t.user_id
    left join (
        select user_id ,
        sum(amount) wash_amount
    from wash_code_change wcc
    where platform = {3} and create_time between {0} and {1}
    group by user_id
                ) wash_t on u.id = wash_t.user_id
    left join (
        select user_id ,
        sum(ifnull(service_charge,0)) service_charge
    from withdraw_order wo
    where status = 1 and withdraw_time between {0} and {1}
    group by user_id
                ) withdraw_t on u.id = withdraw_t.user_id
    left join (
        select user_id ,
        sum(amount) amount
    from share_profit_change spc
    where bet_time between {0} and {1}
    group by user_id
                ) pr on u.id=pr.user_id
    left join (
        SELECT user_id,
        SUM(amount) as water
    FROM extract_points_change
    where platform = {3} and create_time between {0} and {1}
    group by user_id
              ) ec on u.id = ec.user_id
    where  1=1{4} {2}
            """;

    public static String exportAeSql = """
    select
    u.account ,
    u.third_proxy ,
    u.id,
    ifnull(goldenf_t.num,0) num,
    ifnull(goldenf_t.bet_amount,0) bet_amount ,
    ifnull(goldenf_t.validbet,0) validbet ,
    ifnull(goldenf_t.win_loss,0) win_loss ,
    ifnull(wash_t.wash_amount,0) wash_amount,
    ifnull(withdraw_t.service_charge,0) service_charge,
    ifnull(pr.amount,0) all_profit_amount,
              -(ifnull(goldenf_t.win_loss,0)+ifnull(wash_t.wash_amount,0)+ifnull(ec.water, 0)) avg_benefit,
        -(ifnull(goldenf_t.win_loss,0)+ifnull(wash_t.wash_amount,0))-ifnull(pr.amount,0)-ifnull(ec.water,0)+ifnull(withdraw_t.service_charge,0) total_amount,
    ifnull(ec.water, 0) all_water
    from user u left join (
        select user_id ,
        count(1) num,
    ifnull( sum( bet_amount ), 0 ) bet_amount,
    ifnull( sum( turnover ), 0 ) validbet,
    ifnull( sum( real_win_amount ), 0 )-ifnull( sum( real_bet_amount ), 0 ) win_loss
    from game_record_ae grg
    where  tx_status = 1 and
        platform = {3}  and bet_time between {0} and {1}
    group by user_id
              ) goldenf_t on u.id = goldenf_t.user_id
    left join (
        select user_id ,
        sum(amount) wash_amount
    from wash_code_change wcc
    where platform = {3} and create_time between {0} and {1}
    group by user_id
                ) wash_t on u.id = wash_t.user_id
    left join (
        select user_id ,
        sum(ifnull(service_charge,0)) service_charge
    from withdraw_order wo
    where status = 1 and withdraw_time between {0} and {1}
    group by user_id
                ) withdraw_t on u.id = withdraw_t.user_id
    left join (
        select user_id ,
        sum(amount) amount
    from share_profit_change spc
    where bet_time between {0} and {1}
    group by user_id
                ) pr on u.id=pr.user_id
    left join (
        SELECT user_id,
        SUM(amount) as water
    FROM extract_points_change
    where platform = {3} and create_time between {0} and {1}
    group by user_id
              ) ec on u.id = ec.user_id
    where  1=1{4} {2}
            """;

    public static String exportAeMergeSql = """
    select
    u.account ,
    u.third_proxy ,
    u.id,
    ifnull(goldenf_t.num,0) num,
    ifnull(goldenf_t.bet_amount,0) bet_amount ,
    ifnull(goldenf_t.validbet,0) validbet ,
    ifnull(goldenf_t.win_loss,0) win_loss ,
    ifnull(wash_t.wash_amount,0) wash_amount,
    ifnull(withdraw_t.service_charge,0) service_charge,
    ifnull(pr.amount,0) all_profit_amount,
              -(ifnull(goldenf_t.win_loss,0)+ifnull(wash_t.wash_amount,0)+ifnull(ec.water, 0)) avg_benefit,
        -(ifnull(goldenf_t.win_loss,0)+ifnull(wash_t.wash_amount,0))-ifnull(pr.amount,0)-ifnull(ec.water,0)+ifnull(withdraw_t.service_charge,0) total_amount,
    ifnull(ec.water, 0) all_water
    from user u left join (
        select user_id ,
        count(1) num,
    ifnull( sum( bet_amount ), 0 ) bet_amount,
    ifnull( sum( turnover ), 0 ) validbet,
    ifnull( sum( real_win_amount ), 0 )-ifnull( sum( real_bet_amount ), 0 ) win_loss
    from game_record_ae grg
        where
    tx_status = 1 and bet_time between {0} and {1}
    group by user_id
              ) goldenf_t on u.id = goldenf_t.user_id
    left join (
        select user_id ,
        sum(amount) wash_amount
    from wash_code_change wcc
    where platform = {3} and create_time between {0} and {1}
    group by user_id
                ) wash_t on u.id = wash_t.user_id
    left join (
        select user_id ,
        sum(ifnull(service_charge,0)) service_charge
    from withdraw_order wo
    where status = 1 and withdraw_time between {0} and {1}
    group by user_id
                ) withdraw_t on u.id = withdraw_t.user_id
    left join (
        select user_id ,
        sum(amount) amount
    from share_profit_change spc
    where bet_time between {0} and {1}
    group by user_id
                ) pr on u.id=pr.user_id
    left join (
        SELECT user_id,
        SUM(amount) as water
    FROM extract_points_change
    where platform = {3} and create_time between {0} and {1}
    group by user_id
              ) ec on u.id = ec.user_id
    where  1=1{4} {2}
            """;

    public static String exportSabasportSql = """
    select
    u.account ,
    u.third_proxy ,
    u.id,
    ifnull(goldenf_t.num,0) num,
    ifnull(goldenf_t.bet_amount,0) bet_amount ,
    ifnull(goldenf_t.bet_amount,0) validbet ,
    ifnull(goldenf_t.win_loss,0) win_loss ,
    ifnull(wash_t.wash_amount,0) wash_amount,
    ifnull(withdraw_t.service_charge,0) service_charge,
    ifnull(pr.amount,0) all_profit_amount,
              -(ifnull(goldenf_t.win_loss,0)+ifnull(wash_t.wash_amount,0)+ifnull(ec.water, 0)) avg_benefit,
        -(ifnull(goldenf_t.win_loss,0)+ifnull(wash_t.wash_amount,0))-ifnull(pr.amount,0)-ifnull(ec.water,0)+ifnull(withdraw_t.service_charge,0) total_amount,
    ifnull(ec.water, 0) all_water
    from user u left join (
        SELECT
            off.user_id user_id,
        count( DISTINCT sk.bet_id ) num,
    ifnull( SUM( sk.bet_amount ), 0 ) bet_amount,
    ifnull( SUM( sk.bet_amount ), 0 ) validbet,
    ifnull(sum(off.win_amount), 0 )-ifnull(sum( sk.bet_amount ), 0 )+ifnull(sum(t3.win_amount), 0 ) win_loss
    FROM
        (
            SELECT user_id user_id,vendor_code vendor_code,bet_id bet_id,SUM( win_amount ) win_amount
    FROM
    game_record_goldenf t1
    WHERE
    t1.vendor_code = {3}
    AND t1.trans_type = {5}
    AND t1.create_at_str BETWEEN {0}
    AND {1}
    GROUP BY
    t1.bet_id
) off
    LEFT JOIN ( SELECT bet_amount, bet_id FROM game_record_goldenf WHERE vendor_code = {3} AND trans_type = {6} ) sk ON off.bet_id = sk.bet_id
    LEFT JOIN game_record_goldenf t3 ON off.bet_id = t3.bet_id
    AND t3.trans_type = {7} GROUP BY off.user_id
    ) goldenf_t on u.id = goldenf_t.user_id
    left join (
        select user_id ,
        sum(amount) wash_amount
    from wash_code_change wcc
    where platform = {3} and create_time between {0} and {1}
    group by user_id
                ) wash_t on u.id = wash_t.user_id
    left join (
        select user_id ,
        sum(ifnull(service_charge,0)) service_charge
    from withdraw_order wo
    where status = 1 and withdraw_time between {0} and {1}
    group by user_id
                ) withdraw_t on u.id = withdraw_t.user_id
    left join (
        select user_id ,
        sum(amount) amount
    from share_profit_change spc
    where bet_time between {0} and {1}
    group by user_id
                ) pr on u.id=pr.user_id
    left join (
        SELECT user_id,
        SUM(amount) as water
    FROM extract_points_change
    where platform = {3} and create_time between {0} and {1}
    group by user_id
              ) ec on u.id = ec.user_id
    where  1=1{4} {2}
            """;

    public static String obdjSql = """
    select
    u.account ,
    u.third_proxy ,
    u.id,
    ifnull(goldenf_t.num,0) num,
    ifnull(goldenf_t.bet_amount,0) bet_amount ,
    ifnull(goldenf_t.bet_amount,0) validbet ,
    ifnull(goldenf_t.win_loss,0) win_loss ,
    ifnull(wash_t.wash_amount,0) wash_amount,
    ifnull(withdraw_t.service_charge,0) service_charge,
    ifnull(pr.amount,0) all_profit_amount,
              -(ifnull(goldenf_t.win_loss,0)+ifnull(wash_t.wash_amount,0)+ifnull(ec.water, 0)) avg_benefit,
        -(ifnull(goldenf_t.win_loss,0)+ifnull(wash_t.wash_amount,0))-ifnull(pr.amount,0)-ifnull(ec.water,0)+ifnull(withdraw_t.service_charge,0) total_amount,
    ifnull(ec.water, 0) all_water
    from user u left join (
        select user_id ,
        count(1) num,
    sum(bet_amount) bet_amount,
    sum(win_amount-bet_amount) win_loss
    from game_record_obdj grg
        where
    bet_status in (5,6,8,9,10) and set_str_time between {0} and {1}
    group by user_id
              ) goldenf_t on u.id = goldenf_t.user_id
    left join (
        select user_id ,
        sum(amount) wash_amount
    from wash_code_change wcc
    where platform = {5} and create_time between {0} and {1}
    group by user_id
                ) wash_t on u.id = wash_t.user_id
    left join (
        select user_id ,
        sum(ifnull(service_charge,0)) service_charge
    from withdraw_order wo
    where status = 1 and withdraw_time between {0} and {1}
    group by user_id
                ) withdraw_t on u.id = withdraw_t.user_id
    left join (
        select user_id ,
        sum(amount) amount
    from share_profit_change spc
    where bet_time between {0} and {1}
    group by user_id
                ) pr on u.id=pr.user_id
    left join (
        SELECT user_id,
        SUM(amount) as water
    FROM extract_points_change
    where platform = {5} and create_time between {0} and {1}
    group by user_id
              ) ec on u.id = ec.user_id
    where  1=1{6} {2}
    limit {3},{4}
            """;

    public static String exportObdjSql = """
    select
    u.account ,
    u.third_proxy ,
    u.id,
    ifnull(goldenf_t.num,0) num,
    ifnull(goldenf_t.bet_amount,0) bet_amount ,
    ifnull(goldenf_t.bet_amount,0) validbet ,
    ifnull(goldenf_t.win_loss,0) win_loss ,
    ifnull(wash_t.wash_amount,0) wash_amount,
    ifnull(withdraw_t.service_charge,0) service_charge,
    ifnull(pr.amount,0) all_profit_amount,
              -(ifnull(goldenf_t.win_loss,0)+ifnull(wash_t.wash_amount,0)+ifnull(ec.water, 0)) avg_benefit,
        -(ifnull(goldenf_t.win_loss,0)+ifnull(wash_t.wash_amount,0))-ifnull(pr.amount,0)-ifnull(ec.water,0)+ifnull(withdraw_t.service_charge,0) total_amount,
    ifnull(ec.water, 0) all_water
    from user u left join (
        select user_id ,
        count(1) num,
    sum(bet_amount) bet_amount,
    sum(win_amount-bet_amount) win_loss
    from game_record_obdj grg
        where
    bet_status in (5,6,8,9,10) and set_str_time between {0} and {1}
    group by user_id
              ) goldenf_t on u.id = goldenf_t.user_id
    left join (
        select user_id ,
        sum(amount) wash_amount
    from wash_code_change wcc
    where platform = {3} and create_time between {0} and {1}
    group by user_id
                ) wash_t on u.id = wash_t.user_id
    left join (
        select user_id ,
        sum(ifnull(service_charge,0)) service_charge
    from withdraw_order wo
    where status = 1 and withdraw_time between {0} and {1}
    group by user_id
                ) withdraw_t on u.id = withdraw_t.user_id
    left join (
        select user_id ,
        sum(amount) amount
    from share_profit_change spc
    where bet_time between {0} and {1}
    group by user_id
                ) pr on u.id=pr.user_id
    left join (
        SELECT user_id,
        SUM(amount) as water
    FROM extract_points_change
    where platform = {3} and create_time between {0} and {1}
    group by user_id
              ) ec on u.id = ec.user_id
    where  1=1{4} {2}
            """;

    public static String obtySql = """
    select
    u.account ,
    u.third_proxy ,
    u.id,
    ifnull(goldenf_t.num,0) num,
    ifnull(goldenf_t.bet_amount,0) bet_amount ,
    ifnull(goldenf_t.bet_amount,0) validbet ,
    ifnull(goldenf_t.win_loss,0) win_loss ,
    ifnull(wash_t.wash_amount,0) wash_amount,
    ifnull(withdraw_t.service_charge,0) service_charge,
    ifnull(pr.amount,0) all_profit_amount,
              -(ifnull(goldenf_t.win_loss,0)+ifnull(wash_t.wash_amount,0)+ifnull(ec.water, 0)) avg_benefit,
        -(ifnull(goldenf_t.win_loss,0)+ifnull(wash_t.wash_amount,0))-ifnull(pr.amount,0)-ifnull(ec.water,0)+ifnull(withdraw_t.service_charge,0) total_amount,
    ifnull(ec.water, 0) all_water
    from user u left join (
        select user_id ,
        count(1) num,
    sum(order_amount) bet_amount,
    sum(profit_amount) win_loss
    from game_record_obty grg
    where settle_str_time between {0} and {1}
    group by user_id
              ) goldenf_t on u.id = goldenf_t.user_id
    left join (
        select user_id ,
        sum(amount) wash_amount
    from wash_code_change wcc
    where platform = {5} and create_time between {0} and {1}
    group by user_id
                ) wash_t on u.id = wash_t.user_id
    left join (
        select user_id ,
        sum(ifnull(service_charge,0)) service_charge
    from withdraw_order wo
    where status = 1 and withdraw_time between {0} and {1}
    group by user_id
                ) withdraw_t on u.id = withdraw_t.user_id
    left join (
        select user_id ,
        sum(amount) amount
    from share_profit_change spc
    where bet_time between {0} and {1}
    group by user_id
                ) pr on u.id=pr.user_id
    left join (
        SELECT user_id,
        SUM(amount) as water
    FROM extract_points_change
    where platform = {5} and create_time between {0} and {1}
    group by user_id
              ) ec on u.id = ec.user_id
    where  1=1{6} {2}
    limit {3},{4}
            """;

    public static String exportObtySql = """
    select
    u.account ,
    u.third_proxy ,
    u.id,
    ifnull(goldenf_t.num,0) num,
    ifnull(goldenf_t.bet_amount,0) bet_amount ,
    ifnull(goldenf_t.bet_amount,0) validbet ,
    ifnull(goldenf_t.win_loss,0) win_loss ,
    ifnull(wash_t.wash_amount,0) wash_amount,
    ifnull(withdraw_t.service_charge,0) service_charge,
    ifnull(pr.amount,0) all_profit_amount,
              -(ifnull(goldenf_t.win_loss,0)+ifnull(wash_t.wash_amount,0)+ifnull(ec.water, 0)) avg_benefit,
        -(ifnull(goldenf_t.win_loss,0)+ifnull(wash_t.wash_amount,0))-ifnull(pr.amount,0)-ifnull(ec.water,0)+ifnull(withdraw_t.service_charge,0) total_amount,
    ifnull(ec.water, 0) all_water
    from user u left join (
        select user_id ,
        count(1) num,
    sum(order_amount) bet_amount,
    sum(profit_amount) win_loss
    from game_record_obty grg
    where settle_str_time between {0} and {1}
    group by user_id
              ) goldenf_t on u.id = goldenf_t.user_id
    left join (
        select user_id ,
        sum(amount) wash_amount
    from wash_code_change wcc
    where platform = {3} and create_time between {0} and {1}
    group by user_id
                ) wash_t on u.id = wash_t.user_id
    left join (
        select user_id ,
        sum(ifnull(service_charge,0)) service_charge
    from withdraw_order wo
    where status = 1 and withdraw_time between {0} and {1}
    group by user_id
                ) withdraw_t on u.id = withdraw_t.user_id
    left join (
        select user_id ,
        sum(amount) amount
    from share_profit_change spc
    where bet_time between {0} and {1}
    group by user_id
                ) pr on u.id=pr.user_id
    left join (
        SELECT user_id,
        SUM(amount) as water
    FROM extract_points_change
    where  platform = {3} and create_time between {0} and {1}
    group by user_id
              ) ec on u.id = ec.user_id
    where  1=1{4} {2}
            """;
    public static String seleOneTotalReport = """
    select
    u.account ,
    u.third_proxy ,
    u.id,
    ifnull(main_t.num,0) num,
    ifnull(main_t.bet_amount,0) bet_amount ,
    ifnull(main_t.validbet,0) validbet ,
    ifnull(main_t.win_loss,0) win_loss ,
    ifnull(wash_t.wash_amount,0) wash_amount,
    ifnull(withdraw_t.service_charge,0) service_charge,
    ifnull(pr.amount,0) all_profit_amount,
              -(ifnull(main_t.win_loss,0)+ifnull(wash_t.wash_amount,0)+ifnull(ec.water,0)) avg_benefit,
        -(ifnull(main_t.win_loss,0)+ifnull(wash_t.wash_amount,0))-ifnull(pr.amount,0)-ifnull(ec.water,0)+ifnull(withdraw_t.service_charge,0) total_amount,
    sum(ifnull(ec.water, 0)) all_water
    from user u left join (
        select user_id ,
        SUM(betting_number) num,
    sum(bet_amount) bet_amount,
    sum(valid_amount) validbet ,
    sum(win_loss) win_loss
    from proxy_game_record_report gr
    where order_times between {3} and {4}
    group by user_id
                  ) main_t on u.id = main_t.user_id
    left join (
        select user_id ,
        sum(amount) wash_amount
    from wash_code_change wcc
    where create_time between {0} and {1}
    group by user_id
                ) wash_t on u.id = wash_t.user_id
    left join (
        select user_id ,
        sum(ifnull(service_charge,0)) service_charge
    from withdraw_order wo
    where status = 1 and withdraw_time between {0} and {1}
    group by user_id
                ) withdraw_t on u.id = withdraw_t.user_id
    left join (
        select user_id ,
        sum(amount) amount
    from share_profit_change spc
    where bet_time between {0} and {1}
    group by user_id
                ) pr on u.id=pr.user_id
    left join (
        SELECT user_id,
        SUM(amount) as water
    FROM extract_points_change
    where create_time between {0} and {1}
    group by user_id
                ) ec on u.id = ec.user_id
    where u.id = {2}{5}
            """;

    //    public static String seleOneTotal = """
    //    select
    //    u.account ,
    //    u.third_proxy ,
    //    u.id,
    //    ifnull(main_t.num,0)+ifnull(goldenf_t.num,0)+ifnull(grobdj_t.num,0)+ifnull(grobty_t.num,0) num,
    //    ifnull(main_t.bet_amount,0)+ifnull(goldenf_t.bet_amount,0)+ifnull(grobdj_t.bet_amount,0)+ifnull(grobty_t.bet_amount,0) bet_amount ,
    //    ifnull(main_t.validbet,0)+ifnull(goldenf_t.bet_amount,0)+ifnull(grobdj_t.bet_amount,0)+ifnull(grobty_t.bet_amount,0) validbet ,
    //    ifnull(main_t.win_loss,0)+ifnull(goldenf_t.win_loss,0)+ifnull(grobdj_t.win_loss,0)+ifnull(grobty_t.win_loss,0) win_loss ,
    //    ifnull(wash_t.wash_amount,0) wash_amount,
    //    ifnull(withdraw_t.service_charge,0) service_charge,
    //    ifnull(pr.amount,0) all_profit_amount,
    //            -(ifnull(main_t.win_loss,0)+ifnull(goldenf_t.win_loss,0)+ifnull(grobdj_t.win_loss,0)+ifnull(grobty_t.win_loss,0)+ifnull(wash_t.wash_amount,0)+ifnull(ec.water,0)) avg_benefit,
    //        -(ifnull(main_t.win_loss,0)+ifnull(goldenf_t.win_loss,0)+ifnull(grobdj_t.win_loss,0)+ifnull(grobty_t.win_loss,0)+ifnull(wash_t.wash_amount,0))-ifnull(pr.amount,0) -ifnull(ec.water,0) +ifnull(withdraw_t.service_charge,0) total_amount,
    //    ifnull(ec.water, 0) all_water
    //    from user u left join (
    //        select user_id ,
    //        count(1) num,
    //    sum(bet) bet_amount,
    //    sum(validbet) validbet ,
    //    sum(win_loss) win_loss
    //    from game_record gr
    //    where bet_time >= {0} and bet_time <= {1}
    //    group by user_id
    //                ) main_t on u.id = main_t.user_id
    //    left join (
    //        select user_id ,
    //        count(1) num,
    //    sum(bet_amount) bet_amount,
    //    sum(win_amount-bet_amount) win_loss
    //    from game_record_goldenf grg
    //    where create_at_str >= {0} and create_at_str <= {1}
    //    group by user_id
    //                ) goldenf_t on u.id = goldenf_t.user_id
    //    left join (
    //        select user_id ,
    //        count(1) num,
    //    sum(bet_amount) bet_amount,
    //    sum(win_amount-bet_amount) win_loss
    //    from game_record_obdj grobdj
    //    where bet_status in (5,6,8,9,10) and set_str_time >= {0} and set_str_time <= {1}
    //    group by user_id
    //                ) grobdj_t on u.id = grobdj_t.user_id
    //    left join (
    //        select user_id ,
    //        count(1) num,
    //    sum(order_amount) bet_amount,
    //    sum(profit_amount) win_loss
    //    from game_record_obty grobty
    //    where settle_str_time >= {0} and settle_str_time <= {1}
    //    group by user_id
    //                ) grobty_t on u.id = grobty_t.user_id
    //    left join (
    //        select user_id ,
    //        sum(amount) wash_amount
    //    from wash_code_change wcc
    //    where create_time >= {0} and create_time <= {1}
    //    group by user_id
    //                ) wash_t on u.id = wash_t.user_id
    //    left join (
    //        select user_id ,
    //        sum(ifnull(service_charge,0)) service_charge
    //    from withdraw_order wo
    //    where status = 1 and withdraw_time >= {0} and withdraw_time <= {1}
    //    group by user_id
    //                ) withdraw_t on u.id = withdraw_t.user_id
    //    left join (
    //        select user_id ,
    //        sum(amount) amount
    //    from share_profit_change spc
    //    where bet_time >= {0} and bet_time <= {1}
    //    group by user_id
    //                ) pr on u.id=pr.user_id
    //    left join (
    //        SELECT user_id,
    //        SUM(amount) as water
    //    FROM extract_points_change
    //    where create_time >= {0} and create_time <= {1}
    //    group by user_id
    //                ) ec on u.id = ec.user_id
    //    where u.id = {2}{3}
    //            """;

    //    public static String seleOnePgOrCq9Sql = """ 走报表
    //    select
    //    u.account ,
    //    u.third_proxy ,
    //    u.id,
    //    ifnull(goldenf_t.num,0) num,
    //    ifnull(goldenf_t.bet_amount,0) bet_amount ,
    //    ifnull(goldenf_t.validbet,0) validbet ,
    //    ifnull(goldenf_t.win_loss,0) win_loss ,
    //    ifnull(wash_t.wash_amount,0) wash_amount,
    //    ifnull(withdraw_t.service_charge,0) service_charge,
    //    ifnull(pr.amount,0) all_profit_amount,
    //              -(ifnull(goldenf_t.win_loss,0)+ifnull(wash_t.wash_amount,0)+ifnull(ec.water,0)) avg_benefit,
    //        -(ifnull(goldenf_t.win_loss,0)+ifnull(wash_t.wash_amount,0))-ifnull(pr.amount,0)-ifnull(ec.water,0)+ifnull(withdraw_t.service_charge,0) total_amount,
    //    sum(ifnull(ec.water, 0)) all_water
    //    from user u
    //    left join (
    //        select user_id ,
    //        SUM(betting_number) num,
    //    sum(bet_amount) bet_amount,
    //    sum(valid_amount) validbet ,
    //    sum(win_loss) win_loss
    //    from user_game_record_report grg
    //        where
    //    platform = {3}  and   order_times >= {4} and order_times <= {5}
    //    group by user_id
    //                ) goldenf_t on u.id = goldenf_t.user_id
    //    left join (
    //        select user_id ,
    //        sum(amount) wash_amount
    //    from wash_code_change wcc
    //    where platform = {3} and  create_time >= {0} and create_time <= {1}
    //    group by user_id
    //                ) wash_t on u.id = wash_t.user_id
    //    left join (
    //        select user_id ,
    //        sum(ifnull(service_charge,0)) service_charge
    //    from withdraw_order wo
    //    where status = 1 and withdraw_time >= {0} and withdraw_time <= {1}
    //    group by user_id
    //                ) withdraw_t on u.id = withdraw_t.user_id
    //    left join (
    //        select user_id ,
    //        sum(amount) amount from share_profit_change spc
    //    where bet_time >= {0} and bet_time <= {1}
    //    group by user_id
    //                ) pr on u.id=pr.user_id
    //    left join (
    //        SELECT user_id,
    //        SUM(amount) as water
    //    FROM extract_points_change
    //    where create_time >= {0} and create_time <= {1}  and platform = {3}
    //    group by user_id
    //                ) ec on u.id = ec.user_id
    //    where u.id = {2}{6}
    //            """;

    public static String seleOnePgOrCq9Sql = """
    select
    u.account ,
    u.third_proxy ,
    u.id,
    ifnull(goldenf_t.num,0) num,
    ifnull(goldenf_t.bet_amount,0) bet_amount ,
    ifnull(goldenf_t.bet_amount,0) validbet ,
    ifnull(goldenf_t.win_loss,0) win_loss ,
    ifnull(wash_t.wash_amount,0) wash_amount,
    ifnull(withdraw_t.service_charge,0) service_charge,
    ifnull(pr.amount,0) all_profit_amount,
              -(ifnull(goldenf_t.win_loss,0)+ifnull(wash_t.wash_amount,0)+ifnull(ec.water,0)) avg_benefit,
        -(ifnull(goldenf_t.win_loss,0)+ifnull(wash_t.wash_amount,0))-ifnull(pr.amount,0)-ifnull(ec.water,0)+ifnull(withdraw_t.service_charge,0) total_amount,
    sum(ifnull(ec.water, 0)) all_water
    from user u
    left join (
        select user_id ,
        count(1) num,
    sum(bet_amount) bet_amount,
    sum(win_amount-bet_amount) win_loss
    from game_record_goldenf grg
    where vendor_code = {3} and create_at_str between {0} and {1}
    group by user_id
                ) goldenf_t on u.id = goldenf_t.user_id
    left join (
        select user_id ,
        sum(amount) wash_amount
    from wash_code_change wcc
    where platform = {3} and create_time between {0} and {1}
    group by user_id
                ) wash_t on u.id = wash_t.user_id
    left join (
        select user_id ,
        sum(ifnull(service_charge,0)) service_charge
    from withdraw_order wo
    where status = 1 and withdraw_time between {0} and {1}
    group by user_id
                ) withdraw_t on u.id = withdraw_t.user_id
    left join (
        select user_id ,
        sum(amount) amount from share_profit_change spc
    where bet_time between {0} and {1}
    group by user_id
                ) pr on u.id=pr.user_id
    left join (
        SELECT user_id,
        SUM(amount) as water
    FROM extract_points_change
    where platform = {3} and create_time between {0} and {1}
    group by user_id
                ) ec on u.id = ec.user_id
    where 1=1 and u.id = {2} {4}
            """;

    public static String seleOneAeSql = """
    select
    u.account ,
    u.third_proxy ,
    u.id,
    ifnull(goldenf_t.num,0) num,
    ifnull(goldenf_t.bet_amount,0) bet_amount ,
    ifnull(goldenf_t.validbet,0) validbet ,
    ifnull(goldenf_t.win_loss,0) win_loss ,
    ifnull(wash_t.wash_amount,0) wash_amount,
    ifnull(withdraw_t.service_charge,0) service_charge,
    ifnull(pr.amount,0) all_profit_amount,
              -(ifnull(goldenf_t.win_loss,0)+ifnull(wash_t.wash_amount,0)+ifnull(ec.water,0)) avg_benefit,
        -(ifnull(goldenf_t.win_loss,0)+ifnull(wash_t.wash_amount,0))-ifnull(pr.amount,0)-ifnull(ec.water,0)+ifnull(withdraw_t.service_charge,0) total_amount,
    sum(ifnull(ec.water, 0)) all_water
    from user u
    left join (
        select user_id ,
        count(1) num,
    ifnull( sum( bet_amount ), 0 ) bet_amount,
    ifnull( sum( turnover ), 0 ) validbet,
    ifnull( sum( real_win_amount ), 0 )-ifnull( sum( real_bet_amount ), 0 ) win_loss
    from game_record_ae grg
    where  tx_status = 1 and platform = {3} and bet_time between {0} and {1}
    group by user_id
                ) goldenf_t on u.id = goldenf_t.user_id
    left join (
        select user_id ,
        sum(amount) wash_amount
    from wash_code_change wcc
    where platform = {3} and create_time between {0} and {1}
    group by user_id
                ) wash_t on u.id = wash_t.user_id
    left join (
        select user_id ,
        sum(ifnull(service_charge,0)) service_charge
    from withdraw_order wo
    where status = 1 and withdraw_time between {0} and {1}
    group by user_id
                ) withdraw_t on u.id = withdraw_t.user_id
    left join (
        select user_id ,
        sum(amount) amount from share_profit_change spc
    where bet_time between {0} and {1}
    group by user_id
                ) pr on u.id=pr.user_id
    left join (
        SELECT user_id,
        SUM(amount) as water
    FROM extract_points_change
    where platform = {3} and create_time between {0} and {1}
    group by user_id
                ) ec on u.id = ec.user_id
    where 1=1 and u.id = {2} {4}
            """;

    public static String seleOneAeMergeSql = """
    select
    u.account ,
    u.third_proxy ,
    u.id,
    ifnull(goldenf_t.num,0) num,
    ifnull(goldenf_t.bet_amount,0) bet_amount ,
    ifnull(goldenf_t.validbet,0) validbet ,
    ifnull(goldenf_t.win_loss,0) win_loss ,
    ifnull(wash_t.wash_amount,0) wash_amount,
    ifnull(withdraw_t.service_charge,0) service_charge,
    ifnull(pr.amount,0) all_profit_amount,
              -(ifnull(goldenf_t.win_loss,0)+ifnull(wash_t.wash_amount,0)+ifnull(ec.water,0)) avg_benefit,
        -(ifnull(goldenf_t.win_loss,0)+ifnull(wash_t.wash_amount,0))-ifnull(pr.amount,0)-ifnull(ec.water,0)+ifnull(withdraw_t.service_charge,0) total_amount,
    sum(ifnull(ec.water, 0)) all_water
    from user u
    left join (
        select user_id ,
        count(1) num,
    ifnull( sum( bet_amount ), 0 ) bet_amount,
    ifnull( sum( turnover ), 0 ) validbet,
    ifnull( sum( real_win_amount ), 0 )-ifnull( sum( real_bet_amount ), 0 ) win_loss
    from game_record_ae grg
    where tx_status = 1 and bet_time between {0} and {1}
    group by user_id
                ) goldenf_t on u.id = goldenf_t.user_id
    left join (
        select user_id ,
        sum(amount) wash_amount
    from wash_code_change wcc
    where platform = {3} and create_time between {0} and {1}
    group by user_id
                ) wash_t on u.id = wash_t.user_id
    left join (
        select user_id ,
        sum(ifnull(service_charge,0)) service_charge
    from withdraw_order wo
    where status = 1 and withdraw_time between {0} and {1}
    group by user_id
                ) withdraw_t on u.id = withdraw_t.user_id
    left join (
        select user_id ,
        sum(amount) amount from share_profit_change spc
    where bet_time between {0} and {1}
    group by user_id
                ) pr on u.id=pr.user_id
    left join (
        SELECT user_id,
        SUM(amount) as water
    FROM extract_points_change
    where platform = {3} and create_time between {0} and {1}
    group by user_id
                ) ec on u.id = ec.user_id
    where 1=1 and u.id = {2} {4}
            """;

    public static String seleOneSabasportSql = """
    select
    u.account ,
    u.third_proxy ,
    u.id,
    ifnull(goldenf_t.num,0) num,
    ifnull(goldenf_t.bet_amount,0) bet_amount ,
    ifnull(goldenf_t.bet_amount,0) validbet ,
    ifnull(goldenf_t.win_loss,0) win_loss ,
    ifnull(wash_t.wash_amount,0) wash_amount,
    ifnull(withdraw_t.service_charge,0) service_charge,
    ifnull(pr.amount,0) all_profit_amount,
              -(ifnull(goldenf_t.win_loss,0)+ifnull(wash_t.wash_amount,0)+ifnull(ec.water,0)) avg_benefit,
        -(ifnull(goldenf_t.win_loss,0)+ifnull(wash_t.wash_amount,0))-ifnull(pr.amount,0)-ifnull(ec.water,0)+ifnull(withdraw_t.service_charge,0) total_amount,
    sum(ifnull(ec.water, 0)) all_water
    from user u
    left join (
        SELECT
            off.user_id user_id,
        count( DISTINCT sk.bet_id ) num,
    ifnull( SUM( sk.bet_amount ), 0 ) bet_amount,
    ifnull( SUM( sk.bet_amount ), 0 ) validbet,
    ifnull(sum(off.win_amount), 0 )-ifnull(sum( sk.bet_amount ), 0 )+ifnull(sum(t3.win_amount), 0 ) win_loss
    FROM
        (
            SELECT user_id user_id,vendor_code vendor_code,bet_id bet_id,SUM( win_amount ) win_amount
    FROM
    game_record_goldenf t1
    WHERE
    t1.vendor_code = {3}
    AND t1.trans_type = {5}
    AND t1.create_at_str BETWEEN {0}
    AND {1}
    GROUP BY
    t1.bet_id
) off
    LEFT JOIN ( SELECT bet_amount, bet_id FROM game_record_goldenf WHERE vendor_code = {3} AND trans_type = {6} ) sk ON off.bet_id = sk.bet_id
    LEFT JOIN game_record_goldenf t3 ON off.bet_id = t3.bet_id
    AND t3.trans_type = {7} GROUP BY off.user_id
    ) goldenf_t on u.id = goldenf_t.user_id
    left join (
        select user_id ,
        sum(amount) wash_amount
    from wash_code_change wcc
    where platform = {3} and create_time between {0} and {1}
    group by user_id
                ) wash_t on u.id = wash_t.user_id
    left join (
        select user_id ,
        sum(ifnull(service_charge,0)) service_charge
    from withdraw_order wo
    where status = 1 and withdraw_time between {0} and {1}
    group by user_id
                ) withdraw_t on u.id = withdraw_t.user_id
    left join (
        select user_id ,
        sum(amount) amount from share_profit_change spc
    where bet_time between {0} and {1}
    group by user_id
                ) pr on u.id=pr.user_id
    left join (
        SELECT user_id,
        SUM(amount) as water
    FROM extract_points_change
    where platform = {3} and create_time between {0} and {1}
    group by user_id
                ) ec on u.id = ec.user_id
    where 1=1 and u.id = {2} {4}
            """;

    public static String seleOneObdj = """
    select
    u.account ,
    u.third_proxy ,
    u.id,
    ifnull(goldenf_t.num,0) num,
    ifnull(goldenf_t.bet_amount,0) bet_amount ,
    ifnull(goldenf_t.bet_amount,0) validbet ,
    ifnull(goldenf_t.win_loss,0) win_loss ,
    ifnull(wash_t.wash_amount,0) wash_amount,
    ifnull(withdraw_t.service_charge,0) service_charge,
    ifnull(pr.amount,0) all_profit_amount,
              -(ifnull(goldenf_t.win_loss,0)+ifnull(wash_t.wash_amount,0)+ifnull(ec.water,0)) avg_benefit,
        -(ifnull(goldenf_t.win_loss,0)+ifnull(wash_t.wash_amount,0))-ifnull(pr.amount,0)-ifnull(ec.water,0)+ifnull(withdraw_t.service_charge,0) total_amount,
    sum(ifnull(ec.water, 0)) all_water
    from user u
    left join (
        select user_id ,
        count(1) num,
    sum(bet_amount) bet_amount,
    sum(win_amount-bet_amount) win_loss
    from game_record_obdj grg
    where bet_status in (5,6,8,9,10) and set_str_time between {0} and {1}
    group by user_id
                ) goldenf_t on u.id = goldenf_t.user_id
    left join (
        select user_id ,
        sum(amount) wash_amount
    from wash_code_change wcc
    where platform = {3} and create_time between {0} and {1}
    group by user_id
                ) wash_t on u.id = wash_t.user_id
    left join (
        select user_id ,
        sum(ifnull(service_charge,0)) service_charge
    from withdraw_order wo
    where status = 1 and withdraw_time between {0} and {1}
    group by user_id
                ) withdraw_t on u.id = withdraw_t.user_id
    left join (
        select user_id ,
        sum(amount) amount from share_profit_change spc
    where bet_time between {0} and {1}
    group by user_id
                ) pr on u.id=pr.user_id
    left join (
        SELECT user_id,
        SUM(amount) as water
    FROM extract_points_change
    where platform = {3} and create_time between {0} and {1}
    group by user_id
                ) ec on u.id = ec.user_id
    where 1=1 and u.id = {2} {4}
            """;

    public static String seleOneObty = """
    select
    u.account ,
    u.third_proxy ,
    u.id,
    ifnull(goldenf_t.num,0) num,
    ifnull(goldenf_t.bet_amount,0) bet_amount ,
    ifnull(goldenf_t.bet_amount,0) validbet ,
    ifnull(goldenf_t.win_loss,0) win_loss ,
    ifnull(wash_t.wash_amount,0) wash_amount,
    ifnull(withdraw_t.service_charge,0) service_charge,
    ifnull(pr.amount,0) all_profit_amount,
              -(ifnull(goldenf_t.win_loss,0)+ifnull(wash_t.wash_amount,0)+ifnull(ec.water,0)) avg_benefit,
        -(ifnull(goldenf_t.win_loss,0)+ifnull(wash_t.wash_amount,0))-ifnull(pr.amount,0)-ifnull(ec.water,0)+ifnull(withdraw_t.service_charge,0) total_amount,
    sum(ifnull(ec.water, 0)) all_water
    from user u
    left join (
        select user_id ,
        count(1) num,
    sum(order_amount) bet_amount,
    sum(profit_amount) win_loss
    from game_record_obty grg
    where settle_str_time between {0} and {1}
    group by user_id
                ) goldenf_t on u.id = goldenf_t.user_id
    left join (
        select user_id ,
        sum(amount) wash_amount
    from wash_code_change wcc
    where platform = {3} and create_time between {0} and {1}
    group by user_id
                ) wash_t on u.id = wash_t.user_id
    left join (
        select user_id ,
        sum(ifnull(service_charge,0)) service_charge
    from withdraw_order wo
    where status = 1 and withdraw_time between {0} and {1}
    group by user_id
                ) withdraw_t on u.id = withdraw_t.user_id
    left join (
        select user_id ,
        sum(amount) amount from share_profit_change spc
    where bet_time between {0} and {1}
    group by user_id
                ) pr on u.id=pr.user_id
    left join (
        SELECT user_id,
        SUM(amount) as water
    FROM extract_points_change
    where platform = {3} and create_time between {0} and {1}
    group by user_id
                ) ec on u.id = ec.user_id
    where 1=1 and u.id = {2} {4}
            """;

    public static String seleOneWm = """
    select
    u.account ,
    u.third_proxy ,
    u.id,
    ifnull(main_t.num,0) num,
    ifnull(main_t.bet_amount,0) bet_amount ,
    ifnull(main_t.validbet,0) validbet ,
    ifnull(main_t.win_loss,0) win_loss ,
    ifnull(wash_t.wash_amount,0) wash_amount,
    ifnull(withdraw_t.service_charge,0) service_charge,
    ifnull(pr.amount,0) all_profit_amount,
              -(ifnull(main_t.win_loss,0)+ifnull(wash_t.wash_amount,0)+ifnull(ec.water,0)) avg_benefit,
        -(ifnull(main_t.win_loss,0)+ifnull(wash_t.wash_amount,0))-ifnull(pr.amount,0)-ifnull(ec.water,0)+ifnull(withdraw_t.service_charge,0) total_amount,
    sum(ifnull(ec.water, 0)) all_water
    from user u
    left join (
        select
            user_id ,
        count(1) num,
    sum(bet) bet_amount,
    sum(validbet) validbet ,
    sum(win_loss) win_loss
    from game_record gr
    where bet_time between {0} and {1}
    group by user_id
                ) main_t on u.id = main_t.user_id
    left join (
        select user_id ,
        sum(amount) wash_amount
    from wash_code_change wcc
    where platform = {3} and create_time between {0} and {1}
    group by user_id
                ) wash_t on u.id = wash_t.user_id
    left join (
        select user_id ,
        sum(ifnull(service_charge,0)) service_charge
    from withdraw_order wo
    where status = 1 and withdraw_time between {0} and {1}
    group by user_id
                ) withdraw_t on u.id = withdraw_t.user_id
    left join (
        select user_id ,
        sum(amount) amount
    from share_profit_change spc
    where bet_time between {0} and {1}
    group by user_id
                ) pr on u.id=pr.user_id
    left join (
        SELECT
            user_id,
        SUM(amount) as water
    FROM extract_points_change
    where platform = {3} and create_time between {0} and {1}
    group by user_id
                ) ec on u.id = ec.user_id
    where u.id = {2}{4}
            """;

    public static String sumSqlReport = """
    select
    sum(ifnull(main_t.num,0)) num,
    sum(ifnull(main_t.bet_amount,0)) bet_amount ,
    sum(ifnull(main_t.validbet,0)) validbet ,
    sum(ifnull(main_t.win_loss,0)) win_loss ,
    sum(ifnull(wash_t.wash_amount,0)) wash_amount,
    sum(ifnull(withdraw_t.service_charge,0)) service_charge,
    sum(ifnull(pr.amount,0)) all_profit_amount,
    sum(-(ifnull(main_t.win_loss,0)+ifnull(wash_t.wash_amount,0)+ifnull(ec.water,0))) avg_benefit,
    sum(-(ifnull(main_t.win_loss,0)+ifnull(wash_t.wash_amount,0))-ifnull(pr.amount,0)-ifnull(ec.water,0)+ifnull(withdraw_t.service_charge,0)) total_amount,
    sum(ifnull(ec.water, 0)) all_water
    from user u left join (
        select user_id ,
        SUM(betting_number) num,
    sum(bet_amount) bet_amount,
    sum(valid_amount) validbet ,
    sum(win_loss) win_loss
    from proxy_game_record_report gr
    where order_times between {2} and {3}
    group by user_id
               ) main_t on u.id = main_t.user_id
    left join (
        select
            user_id ,
        sum(amount) wash_amount
    from wash_code_change wcc
    where create_time between {0} and {1}
    group by user_id
               ) wash_t on u.id = wash_t.user_id
    left join (
        select user_id ,
        sum(ifnull(service_charge,0)) service_charge
    from withdraw_order wo
    where status = 1 and withdraw_time between {0} and {1}
    group by user_id
               ) withdraw_t on u.id = withdraw_t.user_id
    left join (
        select user_id ,
        sum(amount) amount from share_profit_change spc
    where bet_time between {0} and {1}
    group by user_id
               ) pr on u.id=pr.user_id
    left join (
        SELECT user_id,
        SUM(amount) as water
    FROM extract_points_change
    where create_time between {0} and {1}
    group by user_id
                ) ec on u.id = ec.user_id{4}
            """;

    //    public static String sumSql = """
    //    select
    //    sum(ifnull(main_t.num,0)) + sum(ifnull(goldenf_t.num,0))+ sum(ifnull(grobdj_t.num,0))+ sum(ifnull(grobty_t.num,0)) num,
    //    sum(ifnull(main_t.bet_amount,0)) + sum(ifnull(goldenf_t.bet_amount,0))+ sum(ifnull(grobdj_t.bet_amount,0))+ sum(ifnull(grobty_t.bet_amount,0)) bet_amount ,
    //    sum(ifnull(main_t.validbet,0)) + sum(ifnull(goldenf_t.bet_amount,0))+ sum(ifnull(grobdj_t.bet_amount,0))+ sum(ifnull(grobty_t.bet_amount,0)) validbet ,
    //    sum(ifnull(main_t.win_loss,0)) + sum(ifnull(goldenf_t.win_loss,0))+ sum(ifnull(grobdj_t.win_loss,0))+ sum(ifnull(grobty_t.win_loss,0)) win_loss ,
    //    sum(ifnull(wash_t.wash_amount,0)) wash_amount,
    //    sum(ifnull(withdraw_t.service_charge,0)) service_charge,
    //    sum(ifnull(pr.amount,0)) all_profit_amount,
    //    sum(-(ifnull(main_t.win_loss,0)+ifnull(goldenf_t.win_loss,0)+ifnull(grobdj_t.win_loss,0)+ifnull(grobty_t.win_loss,0)+ifnull(wash_t.wash_amount,0)+ifnull(ec.water,0))) avg_benefit,
    //    sum(-(ifnull(main_t.win_loss,0)+ifnull(goldenf_t.win_loss,0)+ifnull(grobdj_t.win_loss,0)+ifnull(grobty_t.win_loss,0)+ifnull(wash_t.wash_amount,0))-ifnull(pr.amount,0)-ifnull(ec.water,0)+ifnull(withdraw_t.service_charge,0)) total_amount,
    //    sum(ifnull(ec.water, 0)) all_water
    //    from user u left join (
    //        select user_id ,
    //        count(1) num,
    //    sum(bet) bet_amount,
    //    sum(validbet) validbet ,
    //    sum(win_loss) win_loss
    //    from game_record gr
    //    where bet_time >= {0} and bet_time <= {1}
    //    group by user_id
    //               ) main_t on u.id = main_t.user_id
    //    left join (
    //        select
    //            user_id ,
    //        count(1) num,
    //    sum(bet_amount) bet_amount,
    //    sum(win_amount-bet_amount) win_loss
    //    from game_record_goldenf grg
    //    where create_at_str >= {0} and create_at_str <= {1}
    //    group by user_id
    //               ) goldenf_t on u.id = goldenf_t.user_id
    //    left join (
    //        select user_id ,
    //        count(1) num,
    //    sum(bet_amount) bet_amount,
    //    sum(win_amount-bet_amount) win_loss
    //    from game_record_obdj grobdj
    //    where bet_status in (5,6,8,9,10) and set_str_time >= {0} and set_str_time <= {1}
    //    group by user_id
    //                ) grobdj_t on u.id = grobdj_t.user_id
    //    left join (
    //        select user_id ,
    //        count(1) num,
    //    sum(order_amount) bet_amount,
    //    sum(profit_amount) win_loss
    //    from game_record_obty grobty
    //    where settle_str_time >= {0} and settle_str_time <= {1}
    //    group by user_id
    //                ) grobty_t on u.id = grobty_t.user_id
    //    left join (
    //        select
    //            user_id ,
    //        sum(amount) wash_amount
    //    from wash_code_change wcc
    //    where create_time >= {0} and create_time <= {1}
    //    group by user_id
    //               ) wash_t on u.id = wash_t.user_id
    //    left join (
    //        select user_id ,
    //        sum(ifnull(service_charge,0)) service_charge
    //    from withdraw_order wo
    //    where status = 1 and withdraw_time >= {0} and withdraw_time <= {1}
    //    group by user_id
    //               ) withdraw_t on u.id = withdraw_t.user_id
    //    left join (
    //        select user_id ,
    //        sum(amount) amount from share_profit_change spc
    //    where bet_time >= {0} and bet_time <= {1}
    //    group by user_id
    //               ) pr on u.id=pr.user_id
    //    left join (
    //        SELECT user_id,
    //        SUM(amount) as water
    //    FROM extract_points_change
    //    where create_time >= {0} and create_time <= {1}
    //    group by user_id
    //                ) ec on u.id = ec.user_id{2}
    //            """;


    public static String WMSumSql = """
    select
    sum(ifnull(main_t.num,0)) num,
    sum(ifnull(main_t.bet_amount,0)) bet_amount ,
    sum(ifnull(main_t.validbet,0)) validbet ,
    sum(ifnull(main_t.win_loss,0)) win_loss ,
    sum(ifnull(wash_t.wash_amount,0)) wash_amount,
    sum(ifnull(withdraw_t.service_charge,0)) service_charge,
    sum(ifnull(pr.amount,0)) all_profit_amount,
    sum(-(ifnull(main_t.win_loss,0)+ifnull(wash_t.wash_amount,0)+ifnull(ec.water, 0))) avg_benefit,
    sum(-(ifnull(main_t.win_loss,0)+ifnull(wash_t.wash_amount,0))-ifnull(pr.amount,0)-ifnull(ec.water,0) +ifnull(withdraw_t.service_charge,0)) total_amount,
    ifnull(ec.water, 0) all_water
    from user u
    left join (
        select user_id ,
        count(1) num,
    sum(bet) bet_amount,
    sum(validbet) validbet ,
    sum(win_loss) win_loss
    from game_record gr
    where bet_time between {0} and {1}
    group by user_id
                   ) main_t on u.id = main_t.user_id
    left join (
        select user_id ,
        sum(amount) wash_amount
    from wash_code_change wcc
    where platform = {2}
    and create_time between {0} and {1}
    group by user_id
                   ) wash_t on u.id = wash_t.user_id
    left join (
        select user_id ,
        sum(ifnull(service_charge,0)) service_charge
    from withdraw_order wo
    where status = 1 and withdraw_time between {0} and {1}
    group by user_id
                   ) withdraw_t on u.id = withdraw_t.user_id
    left join (
        select user_id ,
        sum(amount) amount
    from share_profit_change spc
    where bet_time between {0} and {1}
    group by user_id
                   ) pr on u.id=pr.user_id
    left join (
        SELECT user_id,
        SUM(amount) as water
    FROM extract_points_change
    where platform = {2} and create_time between {0} and {1}
    group by user_id
                    ) ec on u.id = ec.user_id{3}
            """;

    //    public static String PGAndCQ9SumSql = """
    //    select
    //    sum(ifnull(goldenf_t.num,0)) num,
    //    sum(ifnull(goldenf_t.bet_amount,0)) bet_amount ,
    //    sum(ifnull(goldenf_t.validbet,0)) validbet ,
    //    sum(ifnull(goldenf_t.win_loss,0)) win_loss ,
    //    sum(ifnull(wash_t.wash_amount,0)) wash_amount,
    //    sum(ifnull(withdraw_t.service_charge,0)) service_charge,
    //    sum(ifnull(pr.amount,0)) all_profit_amount,
    //    sum(-(ifnull(goldenf_t.win_loss,0)+ifnull(wash_t.wash_amount,0)+ifnull(ec.water, 0))) avg_benefit,
    //    sum(-(ifnull(goldenf_t.win_loss,0)+ifnull(wash_t.wash_amount,0))-ifnull(pr.amount,0)-ifnull(ec.water,0)+ifnull(withdraw_t.service_charge,0)) total_amount,
    //    sum(ifnull(ec.water, 0)) all_water
    //    from user u
    //    left join (
    //        select user_id ,
    //        SUM(betting_number) num,
    //    sum(bet_amount) bet_amount,
    //    sum(valid_amount) validbet ,
    //    sum(win_loss) win_loss
    //    from user_game_record_report grg
    //        where
    //    platform = {2}  and   order_times >= {3} and order_times <= {4}
    //    group by user_id
    //                   ) goldenf_t on u.id = goldenf_t.user_id
    //    left join (
    //        select user_id ,
    //        sum(amount) wash_amount
    //    from wash_code_change wcc
    //    where platform = {2} and create_time >= {0} and create_time <= {1}
    //    group by user_id
    //                   ) wash_t on u.id = wash_t.user_id
    //    left join (
    //        select user_id ,
    //        sum(ifnull(service_charge,0)) service_charge
    //    from withdraw_order wo
    //    where status = 1 and withdraw_time >= {0} and withdraw_time <= {1}
    //    group by user_id
    //                   ) withdraw_t on u.id = withdraw_t.user_id
    //    left join (
    //        select user_id ,
    //        sum(amount) amount
    //    from share_profit_change spc
    //    where bet_time >= {0} and bet_time <= {1}
    //    group by user_id
    //                   ) pr on u.id=pr.user_id
    //    left join (
    //        SELECT
    //            user_id,
    //        SUM(amount) as water
    //    FROM extract_points_change
    //    where create_time >= {0} and create_time <= {1}
    //    and platform = {2}
    //    group by user_id
    //                    ) ec on u.id = ec.user_id{5}
    //            """;

    public static String PGAndCQ9SumSql = """
    select
    sum(ifnull(goldenf_t.num,0)) num,
    sum(ifnull(goldenf_t.bet_amount,0)) bet_amount ,
    sum(ifnull(goldenf_t.bet_amount,0)) validbet ,
    sum(ifnull(goldenf_t.win_loss,0)) win_loss ,
    sum(ifnull(wash_t.wash_amount,0)) wash_amount,
    sum(ifnull(withdraw_t.service_charge,0)) service_charge,
    sum(ifnull(pr.amount,0)) all_profit_amount,
    sum(-(ifnull(goldenf_t.win_loss,0)+ifnull(wash_t.wash_amount,0)+ifnull(ec.water, 0))) avg_benefit,
    sum(-(ifnull(goldenf_t.win_loss,0)+ifnull(wash_t.wash_amount,0))-ifnull(pr.amount,0)-ifnull(ec.water,0)+ifnull(withdraw_t.service_charge,0)) total_amount,
    sum(ifnull(ec.water, 0)) all_water
    from user u
    left join (
        select user_id ,
        count(1) num,
    sum(bet_amount) bet_amount,
    sum(win_amount-bet_amount) win_loss
    from game_record_goldenf grg
    where vendor_code = {2}
    and create_at_str between {0} and {1}
    group by user_id
                   ) goldenf_t on u.id = goldenf_t.user_id
    left join (
        select user_id ,
        sum(amount) wash_amount
    from wash_code_change wcc
    where platform = {2} and create_time between {0} and {1}
    group by user_id
                   ) wash_t on u.id = wash_t.user_id
    left join (
        select user_id ,
        sum(ifnull(service_charge,0)) service_charge
    from withdraw_order wo
    where status = 1 and withdraw_time between {0} and {1}
    group by user_id
                   ) withdraw_t on u.id = withdraw_t.user_id
    left join (
        select user_id ,
        sum(amount) amount
    from share_profit_change spc
    where bet_time between {0} and {1}
    group by user_id
                   ) pr on u.id=pr.user_id
    left join (
        SELECT
            user_id,
        SUM(amount) as water
    FROM extract_points_change
    where platform = {2} and create_time between {0} and {1}
    group by user_id
                    ) ec on u.id = ec.user_id{3}
            """;

    public static String aeSumSql = """
    select
    sum(ifnull(goldenf_t.num,0)) num,
    sum(ifnull(goldenf_t.bet_amount,0)) bet_amount ,
    sum(ifnull(goldenf_t.validbet,0)) validbet ,
    sum(ifnull(goldenf_t.win_loss,0)) win_loss ,
    sum(ifnull(wash_t.wash_amount,0)) wash_amount,
    sum(ifnull(withdraw_t.service_charge,0)) service_charge,
    sum(ifnull(pr.amount,0)) all_profit_amount,
    sum(-(ifnull(goldenf_t.win_loss,0)+ifnull(wash_t.wash_amount,0)+ifnull(ec.water, 0))) avg_benefit,
    sum(-(ifnull(goldenf_t.win_loss,0)+ifnull(wash_t.wash_amount,0))-ifnull(pr.amount,0)-ifnull(ec.water,0)+ifnull(withdraw_t.service_charge,0)) total_amount,
    sum(ifnull(ec.water, 0)) all_water
    from user u
    left join (
        select user_id ,
        count(1) num,
    ifnull( sum( bet_amount ), 0 ) bet_amount,
    ifnull( sum( turnover ), 0 ) validbet,
    ifnull( sum( real_win_amount ), 0 )-ifnull( sum( real_bet_amount ), 0 ) win_loss
    from game_record_ae grg
    where platform = {2}
    and bet_time between {0} and {1}
    group by user_id
                   ) goldenf_t on u.id = goldenf_t.user_id
    left join (
        select user_id ,
        sum(amount) wash_amount
    from wash_code_change wcc
    where platform = {2} and create_time between {0} and {1}
    group by user_id
                   ) wash_t on u.id = wash_t.user_id
    left join (
        select user_id ,
        sum(ifnull(service_charge,0)) service_charge
    from withdraw_order wo
    where status = 1 and withdraw_time between {0} and {1}
    group by user_id
                   ) withdraw_t on u.id = withdraw_t.user_id
    left join (
        select user_id ,
        sum(amount) amount
    from share_profit_change spc
    where bet_time between {0} and {1}
    group by user_id
                   ) pr on u.id=pr.user_id
    left join (
        SELECT
            user_id,
        SUM(amount) as water
    FROM extract_points_change
    where platform = {2} and create_time between {0} and {1}
    group by user_id
                    ) ec on u.id = ec.user_id{3}
            """;

    public static String sabasportSumSql = """
    select
    sum(ifnull(goldenf_t.num,0)) num,
    sum(ifnull(goldenf_t.bet_amount,0)) bet_amount ,
    sum(ifnull(goldenf_t.bet_amount,0)) validbet ,
    sum(ifnull(goldenf_t.win_loss,0)) win_loss ,
    sum(ifnull(wash_t.wash_amount,0)) wash_amount,
    sum(ifnull(withdraw_t.service_charge,0)) service_charge,
    sum(ifnull(pr.amount,0)) all_profit_amount,
    sum(-(ifnull(goldenf_t.win_loss,0)+ifnull(wash_t.wash_amount,0)+ifnull(ec.water, 0))) avg_benefit,
    sum(-(ifnull(goldenf_t.win_loss,0)+ifnull(wash_t.wash_amount,0))-ifnull(pr.amount,0)-ifnull(ec.water,0)+ifnull(withdraw_t.service_charge,0)) total_amount,
    sum(ifnull(ec.water, 0)) all_water
    from user u
    left join (
        SELECT
            user_id,
        count( DISTINCT sk.bet_id ) num,
    SUM( sk.bet_amount ) bet_amount,
    SUM( sk.bet_amount ) validbet,
    sum( off.win_amount )- sum( sk.bet_amount ) win_loss
    FROM
        (
            SELECT
                bet_id bet_id,
            user_id user_id,
            SUM( win_amount ) win_amount
    FROM
    game_record_goldenf t1
    WHERE
    t1.vendor_code = {2}
    AND t1.trans_type = {4}
    AND t1.create_at_str BETWEEN {0}
    AND {1}
    GROUP BY
    t1.bet_id
	) off
    LEFT JOIN ( SELECT bet_amount, bet_id FROM game_record_goldenf WHERE vendor_code = {2} AND trans_type = {5} ) sk ON off.bet_id = sk.bet_id  GROUP BY user_id) goldenf_t on u.id = goldenf_t.user_id
    left join (
        select user_id ,
        sum(amount) wash_amount
    from wash_code_change wcc
    where platform = {2} and create_time between {0} and {1}
    group by user_id
                   ) wash_t on u.id = wash_t.user_id
    left join (
        select user_id ,
        sum(ifnull(service_charge,0)) service_charge
    from withdraw_order wo
    where status = 1 and withdraw_time between {0} and {1}
    group by user_id
                   ) withdraw_t on u.id = withdraw_t.user_id
    left join (
        select user_id ,
        sum(amount) amount
    from share_profit_change spc
    where bet_time between {0} and {1}
    group by user_id
                   ) pr on u.id=pr.user_id
    left join (
        SELECT
            user_id,
        SUM(amount) as water
    FROM extract_points_change
    where platform = {2} and create_time between {0} and {1}
    group by user_id
                    ) ec on u.id = ec.user_id{3}
            """;

    public static String obdjSumSql = """
    select
    sum(ifnull(goldenf_t.num,0)) num,
    sum(ifnull(goldenf_t.bet_amount,0)) bet_amount ,
    sum(ifnull(goldenf_t.bet_amount,0)) validbet ,
    sum(ifnull(goldenf_t.win_loss,0)) win_loss ,
    sum(ifnull(wash_t.wash_amount,0)) wash_amount,
    sum(ifnull(withdraw_t.service_charge,0)) service_charge,
    sum(ifnull(pr.amount,0)) all_profit_amount,
    sum(-(ifnull(goldenf_t.win_loss,0)+ifnull(wash_t.wash_amount,0)+ifnull(ec.water, 0))) avg_benefit,
    sum(-(ifnull(goldenf_t.win_loss,0)+ifnull(wash_t.wash_amount,0))-ifnull(pr.amount,0)-ifnull(ec.water,0)+ifnull(withdraw_t.service_charge,0)) total_amount,
    sum(ifnull(ec.water, 0)) all_water
    from user u
    left join (
        select user_id ,
        count(1) num,
    sum(bet_amount) bet_amount,
    sum(win_amount-bet_amount) win_loss
    from game_record_obdj grg
    where bet_status in (5,6,8,9,10) and set_str_time between {0} and {1}
    group by user_id
                   ) goldenf_t on u.id = goldenf_t.user_id
    left join (
        select user_id ,
        sum(amount) wash_amount
    from wash_code_change wcc
    where platform = {2} and create_time between {0} and {1}
    group by user_id
                   ) wash_t on u.id = wash_t.user_id
    left join (
        select user_id ,
        sum(ifnull(service_charge,0)) service_charge
    from withdraw_order wo
    where status = 1 and withdraw_time between {0} and {1}
    group by user_id
                   ) withdraw_t on u.id = withdraw_t.user_id
    left join (
        select user_id ,
        sum(amount) amount
    from share_profit_change spc
    where bet_time between {0} and {1}
    group by user_id
                   ) pr on u.id=pr.user_id
    left join (
        SELECT
            user_id,
        SUM(amount) as water
    FROM extract_points_change
    where platform = {2} and create_time between {0} and {1}
    group by user_id
                    ) ec on u.id = ec.user_id{3}
            """;

    public static String obtySumSql = """
    select
    sum(ifnull(goldenf_t.num,0)) num,
    sum(ifnull(goldenf_t.bet_amount,0)) bet_amount ,
    sum(ifnull(goldenf_t.bet_amount,0)) validbet ,
    sum(ifnull(goldenf_t.win_loss,0)) win_loss ,
    sum(ifnull(wash_t.wash_amount,0)) wash_amount,
    sum(ifnull(withdraw_t.service_charge,0)) service_charge,
    sum(ifnull(pr.amount,0)) all_profit_amount,
    sum(-(ifnull(goldenf_t.win_loss,0)+ifnull(wash_t.wash_amount,0)+ifnull(ec.water, 0))) avg_benefit,
    sum(-(ifnull(goldenf_t.win_loss,0)+ifnull(wash_t.wash_amount,0))-ifnull(pr.amount,0)-ifnull(ec.water,0)+ifnull(withdraw_t.service_charge,0)) total_amount,
    sum(ifnull(ec.water, 0)) all_water
    from user u
    left join (
        select user_id ,
        count(1) num,
    sum(order_amount) bet_amount,
    sum(profit_amount) win_loss
    from game_record_obty grg
    where settle_str_time between {0} and {1}
    group by user_id
                   ) goldenf_t on u.id = goldenf_t.user_id
    left join (
        select user_id ,
        sum(amount) wash_amount
    from wash_code_change wcc
    where platform = {2} and create_time between {0} and {1}
    group by user_id
                   ) wash_t on u.id = wash_t.user_id
    left join (
        select user_id ,
        sum(ifnull(service_charge,0)) service_charge
    from withdraw_order wo
    where status = 1 and withdraw_time between {0} and {1}
    group by user_id
                   ) withdraw_t on u.id = withdraw_t.user_id
    left join (
        select user_id ,
        sum(amount) amount
    from share_profit_change spc
    where bet_time between {0} and {1}
    group by user_id
                   ) pr on u.id=pr.user_id
    left join (
        SELECT
            user_id,
        SUM(amount) as water
    FROM extract_points_change
    where platform = {2} and create_time between {0} and {1}
    group by user_id
                    ) ec on u.id = ec.user_id{3}
            """;
}

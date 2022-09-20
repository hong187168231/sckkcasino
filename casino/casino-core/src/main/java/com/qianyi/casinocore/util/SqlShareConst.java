package com.qianyi.casinocore.util;

public class SqlShareConst {

    public static String sqlShareProfit = """
    SELECT
    u.account,
    u.third_proxy,
    u.id,
    ifnull( spc_t.amount, 0 ) all_profit_amount
    FROM
    USER u
    LEFT JOIN ( SELECT user_id, sum( amount ) amount FROM share_profit_change spc WHERE bet_time BETWEEN {0} AND {1} GROUP BY user_id ) spc_t ON u.id = spc_t.user_id
        WHERE
	1 = 1{5} {2}
    LIMIT {3},{4}
        """;

    public static String reportAllSql = """
    SELECT
    t1.num num,
    t1.bet_amount bet_amount,
    t1.validbet validbet,
    t1.win_loss win_loss,
    t2.service_charge service_charge,
    t3.wash_amount wash_amount,
    t4.water all_water
    FROM
        (
            SELECT
                ifnull( sum( betting_number ), 0 ) num,
    ifnull( sum( bet_amount ), 0 ) bet_amount,
    ifnull( sum( valid_amount ), 0 ) validbet,
    ifnull( sum( win_loss ), 0 ) win_loss
        FROM
    proxy_game_record_report
        WHERE
    user_id = {2}
    AND order_times BETWEEN {4}
    AND {5}
	) t1,
        (
    SELECT
    ifnull( sum( service_charge ), 0 ) service_charge
        FROM
    withdraw_order wo
    WHERE
        user_id = {2}
    AND STATUS = 1
    AND withdraw_time BETWEEN {0}
    AND {1}
	) t2,
        (
    SELECT
    ifnull( sum( amount ), 0 ) wash_amount
        FROM
    wash_code_change wcc
    WHERE
        user_id = {2}{3}
    AND create_time BETWEEN {0}
    AND {1}
	) t3,
        (
    SELECT
    ifnull( sum( amount ), 0 ) AS water
    FROM
        extract_points_change
    WHERE
        user_id = {2}{3}
    AND create_time BETWEEN {0}
    AND {1}
	) t4
        """;

    public static String reportWmSql = """
    SELECT
    t1.num num,
    t1.bet_amount bet_amount,
    t1.validbet validbet,
    t1.win_loss win_loss,
    t2.service_charge service_charge,
    t3.wash_amount wash_amount,
    t4.water all_water
    FROM
        (
            select
                count(1) num,
    ifnull( sum( bet ), 0 ) bet_amount,
    ifnull( sum( validbet ), 0 ) validbet ,
    ifnull( sum( win_loss ), 0 ) win_loss
    from game_record gr
    where user_id = {2}
    AND bet_time between {0} and {1}
	) t1,
        (
    SELECT
    ifnull( sum( service_charge ), 0 ) service_charge
        FROM
    withdraw_order wo
    WHERE
        user_id = {2}
    AND STATUS = 1
    AND withdraw_time BETWEEN {0}
    AND {1}
	) t2,
        (
    SELECT
    ifnull( sum( amount ), 0 ) wash_amount
        FROM
    wash_code_change wcc
    WHERE
        user_id = {2}{3}
    AND create_time BETWEEN {0}
    AND {1}
	) t3,
        (
    SELECT
    ifnull( sum( amount ), 0 ) AS water
    FROM
        extract_points_change
    WHERE
        user_id = {2}{3}
    AND create_time BETWEEN {0}
    AND {1}
	) t4
        """;

    public static String reportObdjSql = """
    SELECT
    t1.num num,
    t1.bet_amount bet_amount,
    t1.validbet validbet,
    t1.win_loss win_loss,
    t2.service_charge service_charge,
    t3.wash_amount wash_amount,
    t4.water all_water
    FROM
        (
            select
                count(1) num,
    ifnull( sum( bet_amount ), 0 ) bet_amount,
    ifnull( sum( bet_amount ), 0 ) validbet,
    ifnull( sum(win_amount-bet_amount), 0 ) win_loss
    from game_record_obdj grg
    where user_id = {2}
    AND bet_status in (5,6,8,9,10) and set_str_time between {0} and {1}
	) t1,
        (
    SELECT
    ifnull( sum( service_charge ), 0 ) service_charge
        FROM
    withdraw_order wo
    WHERE
        user_id = {2}
    AND STATUS = 1
    AND withdraw_time BETWEEN {0}
    AND {1}
	) t2,
        (
    SELECT
    ifnull( sum( amount ), 0 ) wash_amount
        FROM
    wash_code_change wcc
    WHERE
        user_id = {2}{3}
    AND create_time BETWEEN {0}
    AND {1}
	) t3,
        (
    SELECT
    ifnull( sum( amount ), 0 ) AS water
    FROM
        extract_points_change
    WHERE
        user_id = {2}{3}
    AND create_time BETWEEN {0}
    AND {1}
	) t4
        """;

    public static String reportObtySql = """
    SELECT
    t1.num num,
    t1.bet_amount bet_amount,
    t1.validbet validbet,
    t1.win_loss win_loss,
    t2.service_charge service_charge,
    t3.wash_amount wash_amount,
    t4.water all_water
    FROM
        (
            select user_id ,
            count(1) num,
    ifnull( sum( order_amount ), 0 ) bet_amount,
    ifnull( sum( order_amount ), 0 ) validbet,
    ifnull( sum( profit_amount ), 0 ) win_loss
    from game_record_obty grg
    where user_id = {2} AND settle_str_time between {0} and {1}
	) t1,
        (
    SELECT
    ifnull( sum( service_charge ), 0 ) service_charge
        FROM
    withdraw_order wo
    WHERE
        user_id = {2}
    AND STATUS = 1
    AND withdraw_time BETWEEN {0}
    AND {1}
	) t2,
        (
    SELECT
    ifnull( sum( amount ), 0 ) wash_amount
        FROM
    wash_code_change wcc
    WHERE
        user_id = {2}{3}
    AND create_time BETWEEN {0}
    AND {1}
	) t3,
        (
    SELECT
    ifnull( sum( amount ), 0 ) AS water
    FROM
        extract_points_change
    WHERE
        user_id = {2}{3}
    AND create_time BETWEEN {0}
    AND {1}
	) t4
        """;

    public static String reportPgOrCq9Sql = """
    SELECT
    t1.num num,
    t1.bet_amount bet_amount,
    t1.validbet validbet,
    t1.win_loss win_loss,
    t2.service_charge service_charge,
    t3.wash_amount wash_amount,
    t4.water all_water
    FROM
        (
            select
                count(1) num,
    ifnull( sum( bet_amount ), 0 ) bet_amount,
    ifnull( sum( bet_amount ), 0 ) validbet,
    ifnull( sum( win_amount ), 0 )-ifnull( sum( bet_amount ), 0 ) win_loss
    from game_record_goldenf grg
    where user_id = {2}
    AND vendor_code = {4} and create_at_str between {0} and {1}
	) t1,
        (
    SELECT
    ifnull( sum( service_charge ), 0 ) service_charge
        FROM
    withdraw_order wo
    WHERE
        user_id = {2}
    AND STATUS = 1
    AND withdraw_time BETWEEN {0}
    AND {1}
	) t2,
        (
    SELECT
    ifnull( sum( amount ), 0 ) wash_amount
        FROM
    wash_code_change wcc
    WHERE
        user_id = {2}{3}
    AND create_time BETWEEN {0}
    AND {1}
	) t3,
        (
    SELECT
    ifnull( sum( amount ), 0 ) AS water
    FROM
        extract_points_change
    WHERE
        user_id = {2}{3}
    AND create_time BETWEEN {0}
    AND {1}
	) t4
        """;

    public static String reportSabasportSql = """
    SELECT
    t1.num num,
    t1.bet_amount bet_amount,
    t1.validbet validbet,
    t1.win_loss win_loss,
    t2.service_charge service_charge,
    t3.wash_amount wash_amount,
    t4.water all_water
    FROM
        (
            SELECT
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
    t1.vendor_code = {4}
    AND t1.trans_type = {5}
    AND t1.user_id = {2}
    AND t1.create_at_str BETWEEN {0}
    AND {1}
    GROUP BY
    t1.bet_id
) off
    LEFT JOIN ( SELECT bet_amount, bet_id FROM game_record_goldenf WHERE vendor_code = {4} AND trans_type = {6} ) sk ON off.bet_id = sk.bet_id
    LEFT JOIN game_record_goldenf t3 ON off.bet_id = t3.bet_id
    AND t3.trans_type = {7}
	) t1,
        (
    SELECT
    ifnull( sum( service_charge ), 0 ) service_charge
        FROM
    withdraw_order wo
    WHERE
        user_id = {2}
    AND STATUS = 1
    AND withdraw_time BETWEEN {0}
    AND {1}
	) t2,
        (
    SELECT
    ifnull( sum( amount ), 0 ) wash_amount
        FROM
    wash_code_change wcc
    WHERE
        user_id = {2}{3}
    AND create_time BETWEEN {0}
    AND {1}
	) t3,
        (
    SELECT
    ifnull( sum( amount ), 0 ) AS water
    FROM
        extract_points_change
    WHERE
        user_id = {2}{3}
    AND create_time BETWEEN {0}
    AND {1}
	) t4
        """;

    public static String reportAeMergeSql = """
    SELECT
    t1.num num,
    t1.bet_amount bet_amount,
    t1.validbet validbet,
    t1.win_loss win_loss,
    t2.service_charge service_charge,
    t3.wash_amount wash_amount,
    t4.water all_water
    FROM
        (
            select
                count(1) num,
    ifnull( sum( bet_amount ), 0 ) bet_amount,
    ifnull( sum( turnover ), 0 ) validbet,
    ifnull( sum( real_win_amount ), 0 )-ifnull( sum( real_bet_amount ), 0 ) win_loss
    from game_record_ae grg
    where user_id = {2}
    AND  tx_status = 1 and  bet_time between {0} and {1}
	) t1,
        (
    SELECT
    ifnull( sum( service_charge ), 0 ) service_charge
        FROM
    withdraw_order wo
    WHERE
        user_id = {2}
    AND STATUS = 1
    AND withdraw_time BETWEEN {0}
    AND {1}
	) t2,
        (
    SELECT
    ifnull( sum( amount ), 0 ) wash_amount
        FROM
    wash_code_change wcc
    WHERE
        user_id = {2}{3}
    AND create_time BETWEEN {0}
    AND {1}
	) t3,
        (
    SELECT
    ifnull( sum( amount ), 0 ) AS water
    FROM
        extract_points_change
    WHERE
        user_id = {2}{3}
    AND create_time BETWEEN {0}
    AND {1}
	) t4
        """;
}

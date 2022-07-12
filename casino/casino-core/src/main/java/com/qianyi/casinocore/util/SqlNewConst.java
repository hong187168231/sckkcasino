package com.qianyi.casinocore.util;

public class SqlNewConst {
    public static String totalSqlReport = """
    SELECT
    u.account,
    u.third_proxy,
    u.id,
    ifnull( main_t.num, 0 ) num,
    ifnull( main_t.bet_amount, 0 ) bet_amount,
    ifnull( main_t.validbet, 0 ) validbet,
    ifnull( main_t.win_loss, 0 ) win_loss
    FROM
    USER u
    LEFT JOIN (
        SELECT
            user_id,
        SUM( betting_number ) num,
    sum( bet_amount ) bet_amount,
    sum( valid_amount ) validbet,
    sum( win_loss ) win_loss
    FROM
    proxy_game_record_report gr
    WHERE
    order_times BETWEEN {0}
    AND {1}
    GROUP BY
    user_id
	) main_t ON u.id = main_t.user_id
        WHERE
	1 = 1{5} {2}
    LIMIT {3},{4}
            """;

    public static String wmSql = """
    SELECT
    u.account,
    u.third_proxy,
    u.id,
    ifnull( main_t.num, 0 ) num,
    ifnull( main_t.bet_amount, 0 ) bet_amount,
    ifnull( main_t.validbet, 0 ) validbet,
    ifnull( main_t.win_loss, 0 ) win_loss
    FROM
    USER u
    LEFT JOIN (
        SELECT
            user_id,
        count( 1 ) num,
    sum( bet ) bet_amount,
    sum( validbet ) validbet,
    sum( win_loss ) win_loss
    FROM
    game_record gr
    WHERE
    bet_time BETWEEN {0}
    AND {1}
    GROUP BY
    user_id
	) main_t ON u.id = main_t.user_id
        WHERE
	1 = 1{5} {2}
    LIMIT {3},{4}
        """;

    public static String obdjSql = """
    SELECT
    u.account,
    u.third_proxy,
    u.id,
    ifnull( goldenf_t.num, 0 ) num,
    ifnull( goldenf_t.bet_amount, 0 ) bet_amount,
    ifnull( goldenf_t.bet_amount, 0 ) validbet,
    ifnull( goldenf_t.win_loss, 0 ) win_loss
    FROM
    USER u
    LEFT JOIN (
        SELECT
            user_id,
        count( 1 ) num,
    sum( bet_amount ) bet_amount,
    sum( win_amount - bet_amount ) win_loss
    FROM
    game_record_obdj grg
    WHERE
    bet_status IN ( 5, 6, 8, 9, 10 )
    AND set_str_time BETWEEN {0}
    AND {1}
    GROUP BY
    user_id
	) goldenf_t ON u.id = goldenf_t.user_id
        WHERE
	1 = 1{5} {2}
    LIMIT {3},{4}
        """;

    public static String obtySql = """
    SELECT
    u.account,
    u.third_proxy,
    u.id,
    ifnull( goldenf_t.num, 0 ) num,
    ifnull( goldenf_t.bet_amount, 0 ) bet_amount,
    ifnull( goldenf_t.bet_amount, 0 ) validbet,
    ifnull( goldenf_t.win_loss, 0 ) win_loss
    FROM
    USER u
    LEFT JOIN (
        SELECT
            user_id,
        count( 1 ) num,
    sum( order_amount ) bet_amount,
    sum( profit_amount ) win_loss
    FROM
    game_record_obty grg
    WHERE
    settle_str_time BETWEEN {0}
    AND {1}
    GROUP BY
    user_id
	) goldenf_t ON u.id = goldenf_t.user_id
        WHERE
	1 = 1{5} {2}
    LIMIT {3},{4}
        """;

    public static String pgOrCq9Sql = """
    SELECT
    u.account,
    u.third_proxy,
    u.id,
    ifnull( goldenf_t.num, 0 ) num,
    ifnull( goldenf_t.bet_amount, 0 ) bet_amount,
    ifnull( goldenf_t.bet_amount, 0 ) validbet,
    ifnull( goldenf_t.win_loss, 0 ) win_loss
    FROM
    USER u
    LEFT JOIN (
        SELECT
            user_id,
        count( 1 ) num,
    sum( bet_amount ) bet_amount,
    sum( win_amount - bet_amount ) win_loss
    FROM
    game_record_goldenf grg
    WHERE
        vendor_code = {5}
    AND create_at_str BETWEEN {0}
    AND {1}
    GROUP BY
    user_id
	) goldenf_t ON u.id = goldenf_t.user_id
        WHERE
	1 = 1{6} {2}
    LIMIT {3},{4}
        """;

    public static String sabasportSql = """
    SELECT
    u.account,
    u.third_proxy,
    u.id,
    ifnull( goldenf_t.num, 0 ) num,
    ifnull( goldenf_t.bet_amount, 0 ) bet_amount,
    ifnull( goldenf_t.bet_amount, 0 ) validbet,
    ifnull( goldenf_t.win_loss, 0 ) win_loss
    FROM
    USER u
    LEFT JOIN (
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
    user_id
	) goldenf_t ON u.id = goldenf_t.user_id
        WHERE
	1 = 1{6} {2}
    LIMIT {3},{4}
        """;

    public static String reportSql = """
    SELECT
    t1.wash_amount wash_amount,
    t2.service_charge service_charge,
    t3.all_profit_amount all_profit_amount,
    t4.water all_water
    FROM
        (
            SELECT
                ifnull( sum( amount ), 0 ) wash_amount
        FROM
    wash_code_change wcc
    WHERE
        user_id = {2}{3}
    AND create_time BETWEEN {0}
    AND {1}) t1,
        (
    SELECT
    ifnull( sum( service_charge ), 0 ) service_charge
        FROM
    withdraw_order wo
    WHERE
        user_id = {2}
    AND STATUS = 1
    AND withdraw_time BETWEEN {0}
    AND {1}) t2,
        (
    SELECT
    ifnull( sum( amount ), 0 ) all_profit_amount
        FROM
     share_profit_change spc
        WHERE user_id = {2}
    AND bet_time BETWEEN {0}
    AND {1}) t3,
        (
    SELECT
    ifnull( sum( amount ), 0 ) AS water
    FROM
        extract_points_change
    WHERE
        user_id = {2}{3}
    AND create_time BETWEEN {0}
    AND {1}) t4
        """;

    public static String totalSqlWash = """
    SELECT
    u.account,
    u.third_proxy,
    u.id,
    ifnull( wash_t.wash_amount, 0 ) wash_amount
    FROM
    USER u
    LEFT JOIN ( SELECT user_id, sum( amount ) wash_amount FROM wash_code_change wcc WHERE{6} create_time BETWEEN {0} AND {1} GROUP BY user_id ) wash_t ON u.id = wash_t.user_id
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
    t3.all_profit_amount all_profit_amount,
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
    ifnull( sum( amount ), 0 ) all_profit_amount
        FROM
    share_profit_change spc
    WHERE
        user_id = {2}
    AND bet_time BETWEEN {0}
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
    t3.all_profit_amount all_profit_amount,
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
    ifnull( sum( amount ), 0 ) all_profit_amount
        FROM
    share_profit_change spc
    WHERE
        user_id = {2}
    AND bet_time BETWEEN {0}
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
    t3.all_profit_amount all_profit_amount,
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
    ifnull( sum( amount ), 0 ) all_profit_amount
        FROM
    share_profit_change spc
    WHERE
        user_id = {2}
    AND bet_time BETWEEN {0}
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
    t3.all_profit_amount all_profit_amount,
    t4.water all_water
    FROM
        (
    SELECT
    count( 1 ) num,
    ifnull( SUM( sk.bet_amount ), 0 ) bet_amount,
    ifnull( SUM( sk.bet_amount ), 0 ) validbet,
    ifnull( sum( off.win_amount - sk.bet_amount ), 0 ) win_loss
        FROM
    game_record_goldenf off
    LEFT JOIN ( SELECT bet_amount, bet_id FROM game_record_goldenf WHERE user_id = {2}
    AND vendor_code = {4} AND trans_type = {6} ) sk ON off.bet_id = sk.bet_id
        WHERE off.user_id = {2}
    AND off.vendor_code = {4}
    AND off.trans_type = {5}
    AND off.create_at_str BETWEEN {0}
    AND {1}
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
    ifnull( sum( amount ), 0 ) all_profit_amount
        FROM
    share_profit_change spc
    WHERE
        user_id = {2}
    AND bet_time BETWEEN {0}
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
    t3.all_profit_amount all_profit_amount,
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
    ifnull( sum( amount ), 0 ) all_profit_amount
        FROM
    share_profit_change spc
    WHERE
        user_id = {2}
    AND bet_time BETWEEN {0}
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
    t3.all_profit_amount all_profit_amount,
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
    ifnull( sum( amount ), 0 ) all_profit_amount
        FROM
    share_profit_change spc
    WHERE
        user_id = {2}
    AND bet_time BETWEEN {0}
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

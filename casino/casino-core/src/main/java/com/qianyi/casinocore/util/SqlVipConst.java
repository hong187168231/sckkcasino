package com.qianyi.casinocore.util;

public class SqlVipConst {



    public static String vipTotalReport = "SELECT " +
            " u.*, k.* " +
            "FROM " +
            " ( " +
            "  SELECT " +
            "   account, " +
            "   id, " +
            "   `level`, " +
            "   create_time " +
            "  FROM " +
            "   `user` " +
            "  WHERE " +
            "   `level` IN (1) " +
            " ) u " +
            "LEFT JOIN ( " +
            " SELECT " +
            "  user_id, " +
            "  SUM(betting_number) num, " +
            "  sum(bet_amount) bet_amount, " +
            "  sum(valid_amount) validbet, " +
            "  sum(win_loss) win_loss " +
            " FROM " +
            "  proxy_game_record_report gr " +
            " GROUP BY " +
            "  user_id " +
            ") k ON k.user_id = u.id " +
            "LEFT JOIN ( " +
            " SELECT " +
            "  user_id, " +
            "  SUM(amount) AS todayAmount " +
            " FROM " +
            "  award_receive_record " +
            " WHERE " +
            "  1 = 1 " +
            " AND award_type = 1 " +
            " AND create_time BETWEEN {0}  AND {1} "+
            " GROUP BY " +
            "  user_id " +
            ") td ON u.id = td.user_id " +
            "LEFT JOIN ( " +
            " SELECT " +
            "  user_id, " +
            "  SUM(amount) AS riseAmount " +
            " FROM " +
            "  award_receive_record " +
            " WHERE " +
            "  1 = 1 " +
            " AND award_type = 2 " +
            " AND receive_time BETWEEN {0}  AND {1} and receive_status = 1"+
            " GROUP BY " +
            "  user_id " +
            ") rs ON u.id = td.user_id " +
            "ORDER BY " +
            " u.create_time DESC " +
            "LIMIT 10";



}
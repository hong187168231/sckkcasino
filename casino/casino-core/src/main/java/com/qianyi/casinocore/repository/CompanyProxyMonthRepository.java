package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.CompanyProxyMonth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface CompanyProxyMonthRepository extends JpaRepository<CompanyProxyMonth,Long>, JpaSpecificationExecutor<CompanyProxyMonth> {

    void deleteByStaticsTimes(String staticsTimes);

    @Query(value = "select user_id,max(proxy_role) proxy_role ,\n" +
            "max(first_proxy) first_proxy ,max(second_proxy) second_proxy ,max(third_proxy) third_proxy ,\n" +
            "max(substr( ?1 ,1,7)) statics_times, \n" +
            "sum(player_num) player_num ,sum(group_bet_amount) group_bet_amount ,max(profit_level) profit_level ,\n" +
            "max(profit_rate) profit_rate ,sum(group_totalprofit) group_totalprofit ,max(benefit_rate)benefit_rate ,\n" +
            "sum(profit_amount) profit_amount ,max(settle_status) settle_status \n" +
            "from company_proxy_detail cpd \n" +
            "where bet_time between ?1 and ?2\n" +
            "group by user_id",nativeQuery = true)
    List<Map<String,Object>> queryMonthData(String startTime, String endTime);

    @Query(value = "select \n" +
            "u.account ,\n" +
            "main_t.*,\n" +
            "ifnull(wash_t.wash_amount,0) wash_amount,\n" +
            "ifnull(withdraw_t.service_charge,0) service_charge,\n" +
            "ifnull(pr.amount,0) all_profit_amount,\n" +
            "-(main_t.win_loss+ifnull(wash_t.wash_amount,0)) avg_benefit,\n" +
            "-(main_t.win_loss+ifnull(wash_t.wash_amount,0))-ifnull(pr.amount,0)+ifnull(withdraw_t.service_charge,0) total_amount\n" +
            "from (\n" +
            "\tselect user_id ,\n" +
            "\tcount(1) num,\n" +
            "\tsum(bet) bet_amount,\n" +
            "\tsum(validbet) validbet ,\n" +
            "\tsum(win_loss) win_loss \n" +
            "\tfrom game_record gr\n" +
            "\twhere create_time > ?1 and create_time < ?2 \n" +
            "\tgroup by user_id\n" +
            ") main_t\n" +
            "left join (\n" +
            "\tselect user_id , sum(amount) wash_amount \n" +
            "\tfrom wash_code_change wcc \n" +
            "\twhere create_time > ?1 and create_time < ?2 \n" +
            "\tgroup by user_id\n" +
            ") wash_t on main_t.user_id = wash_t.user_id \n" +
            "left join (\n" +
            "\tselect user_id , sum(ifnull(service_charge,0)) service_charge \n" +
            "\tfrom withdraw_order wo \n" +
            "\twhere create_time > ?1 and create_time < ?2 \n" +
            "\tgroup by user_id\n" +
            ") withdraw_t on main_t.user_id = withdraw_t.user_id\n" +
            "left join (\n" +
            "\tselect user_id , sum(amount) amount from share_profit_change spc \n" +
            "\twhere  create_time > ?1 and create_time < ?2 \n" +
            "\tgroup by user_id \n" +
            ") pr on main_t.user_id=pr.user_id \n" +
            "left join `user` u on main_t.user_id = u.id \n" +
            "limit ?3,?4",nativeQuery = true)
    List<Map<String,Object>> queryAllPersonReport(String startTime,String endTime,int page,int count);

    @Query(value = "select \n" +
            "u.account ,\n" +
            "main_t.*,\n" +
            "ifnull(wash_t.wash_amount,0) wash_amount,\n" +
            "ifnull(withdraw_t.service_charge,0) service_charge,\n" +
            "ifnull(pr.amount,0) all_profit_amount,\n" +
            "-(main_t.win_loss+ifnull(wash_t.wash_amount,0)) avg_benefit,\n" +
            "-(main_t.win_loss+ifnull(wash_t.wash_amount,0))-ifnull(pr.amount,0)+ifnull(withdraw_t.service_charge,0) total_amount \n"+
            "from (\n" +
            "\tselect user_id ,\n" +
            "\tcount(1) num,\n" +
            "\tsum(bet) bet_amount,\n" +
            "\tsum(validbet) validbet ,\n" +
            "\tsum(win_loss) win_loss \n" +
            "\tfrom game_record gr\n" +
            "\twhere user_id = ?1 and create_time > ?2 and create_time < ?3\n" +
            "\tgroup by user_id\n" +
            ") main_t\n" +
            "left join (\n" +
            "\tselect user_id , sum(amount) wash_amount \n" +
            "\tfrom wash_code_change wcc \n" +
            "\twhere user_id = ?1 and create_time > ?2 and create_time < ?3\n" +
            "\tgroup by user_id\n" +
            ") wash_t on main_t.user_id = wash_t.user_id \n" +
            "left join (\n" +
            "\tselect user_id , sum(ifnull(service_charge,0)) service_charge \n" +
            "\tfrom withdraw_order wo \n" +
            "\twhere user_id = ?1 and create_time > ?2 and create_time < ?3\n" +
            "\tgroup by user_id\n" +
            ") withdraw_t on main_t.user_id = withdraw_t.user_id\n" +
            "left join (\n" +
            "\tselect user_id , sum(amount) amount from share_profit_change spc \n" +
            "\twhere user_id =?1 and create_time > ?2 and create_time < ?3\n" +
            "\tgroup by user_id \n" +
            ") pr on main_t.user_id=pr.user_id \n" +
            "left join `user` u on main_t.user_id = u.id \n"
            ,nativeQuery = true)
    List<Map<String,Object>> queryPersonReport(long user_id ,String startTime,String endTime);

    @Query(value = "select \n" +
            "sum(main_t.num) num,\n" +
            "sum(main_t.bet_amount) bet_amount,\n" +
            "sum(main_t.validbet) validbet ,\n" +
            "sum(main_t.win_loss) win_loss ,\n" +
            "sum(ifnull(wash_t.wash_amount,0)) wash_amount,\n" +
            "sum(ifnull(withdraw_t.service_charge,0)) service_charge,\n" +
            "sum(ifnull(pr.amount,0)) all_profit_amount,\n" +
            "sum(-(main_t.win_loss+ifnull(wash_t.wash_amount,0))) avg_benefit,\n" +
            "sum(-(main_t.win_loss+ifnull(wash_t.wash_amount,0))-ifnull(pr.amount,0)+ifnull(withdraw_t.service_charge,0)) total_amount \n" +
            " from (\n" +
            "\tselect user_id ,\n" +
            "\tcount(1) num,\n" +
            "\tsum(bet) bet_amount,\n" +
            "\tsum(validbet) validbet ,\n" +
            "\tsum(win_loss) win_loss \n" +
            "\tfrom game_record gr\n" +
            "\twhere create_time > ?1 and create_time < ?2\n" +
            "\tgroup by user_id\n" +
            ") main_t\n" +
            "left join (\n" +
            "\tselect user_id , sum(amount) wash_amount \n" +
            "\tfrom wash_code_change wcc \n" +
            "\twhere create_time > ?1 and create_time < ?2\n" +
            "\tgroup by user_id\n" +
            ") wash_t on main_t.user_id = wash_t.user_id \n" +
            "left join (\n" +
            "\tselect user_id , sum(ifnull(service_charge,0)) service_charge \n" +
            "\tfrom withdraw_order wo \n" +
            "\twhere create_time > ?1 and create_time < ?2\n" +
            "\tgroup by user_id\n" +
            ") withdraw_t on main_t.user_id = withdraw_t.user_id\n" +
            "left join (\n" +
            "\tselect user_id , sum(amount) amount from share_profit_change spc \n" +
            "\twhere  create_time > ?1 and create_time < ?2\n" +
            "\tgroup by user_id \n" +
            ") pr on main_t.user_id=pr.user_id",nativeQuery = true)
    Map<String,Object> queryAllTotal(String startTime,String endTime);

    @Query(value = "select \n" +
            "count(1)\n" +
            "from (\n" +
            "\tselect user_id ,\n" +
            "\tcount(1) num\n" +
            "\tfrom game_record gr\n" +
            "\twhere create_time > ?1 and create_time < ?2\n" +
            "\tgroup by user_id\n" +
            ") main_t",nativeQuery = true)
    int queryAllTotalElement(String startTime,String endTime);
}

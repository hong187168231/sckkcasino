package com.qianyi.casinocore.repository;

import com.qianyi.casinocore.model.GameRecordReportNew;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;

public interface GameRecordReportNewRepository  extends JpaRepository<GameRecordReportNew,Long>, JpaSpecificationExecutor<GameRecordReportNew> {

    @Modifying
    @Query(value = "INSERT INTO game_record_report_new (game_record_report_id,statics_times,bet_amount,valid_amount,"
        + "win_loss_amount,amount,betting_number,first_proxy,second_proxy,third_proxy,platform,surplus_amount,user_amount,new_amount,new_surplus_amount,new_user_amount,today_award,rise_award) " +
        "VALUES (?1,?2,?3,?4,?5,?6,?7,?8,?9,?10,?11,?12,?13,0,0,0,0,0) ON DUPLICATE KEY UPDATE bet_amount=bet_amount + ?3,"
        + "valid_amount=valid_amount + ?4,win_loss_amount=win_loss_amount + ?5,amount=amount + ?6,betting_number=betting_number + ?7 ,surplus_amount=surplus_amount + ?12,user_amount=user_amount + ?13 ;",nativeQuery = true)
    void updateKey(Long gameRecordReportId,String staticsTimes,BigDecimal betAmount,BigDecimal validAmount,BigDecimal winLossAmount,
        BigDecimal amount,Integer bettingNumber,Long firstProxy,Long secondProxy,Long thirdProxy,String platform,BigDecimal surplusAmount,BigDecimal userAmount);

    @Modifying
    @Query(value = "DELETE from game_record_report_new where platform = ?1 ;",nativeQuery = true)
    void deleteByPlatform(String platform);

    @Modifying
    @Query(value = "DELETE from game_record_report_new where platform = ?1 and betting_number > 0 ;",nativeQuery = true)
    void deleteDataByPlatform(String platform);

    @Modifying
    @Query(value = "INSERT INTO game_record_report_new (game_record_report_id,statics_times,bet_amount,valid_amount,"
        + "win_loss_amount,amount,betting_number,first_proxy,second_proxy,third_proxy,platform,surplus_amount,user_amount,new_amount,new_surplus_amount,new_user_amount,today_award,rise_award) " +
        "VALUES (?1,?2,0,0,0,0,0,?3,?4,?5,?6,0,0,?7,0,0,0,0) ON DUPLICATE KEY UPDATE new_amount = ?7 ;",nativeQuery = true)
    void updateKeyWashCode(Long gameRecordReportId,String staticsTimes,Long firstProxy,Long secondProxy,Long thirdProxy,String platform,BigDecimal newAmount);

    @Modifying
    @Query(value = "INSERT INTO game_record_report_new (game_record_report_id,statics_times,bet_amount,valid_amount,"
        + "win_loss_amount,amount,betting_number,first_proxy,second_proxy,third_proxy,platform,surplus_amount,user_amount,new_amount,new_surplus_amount,new_user_amount,today_award,rise_award) " +
        "VALUES (?1,?2,0,0,0,0,0,?3,?4,?5,?6,0,0,0,?7,?8,0,0) ON DUPLICATE KEY UPDATE new_surplus_amount = ?7,new_user_amount = ?8 ;",nativeQuery = true)
    void updateKeyRebate(Long gameRecordReportId,String staticsTimes,Long firstProxy,Long secondProxy,Long thirdProxy,String platform,BigDecimal newSurplusAmount,BigDecimal newUserAmount);

    @Modifying
    @Query(value = "INSERT INTO game_record_report_new (game_record_report_id,statics_times,bet_amount,valid_amount,"
        + "win_loss_amount,amount,betting_number,first_proxy,second_proxy,third_proxy,platform,surplus_amount,user_amount,new_amount,new_surplus_amount,new_user_amount,today_award,rise_award) " +
        "VALUES (?1,?2,0,0,0,0,0,?3,?4,?5,?6,0,0,0,0,0,?7,0) ON DUPLICATE KEY UPDATE today_award = ?7 ;",nativeQuery = true)
    void updateKeyTodayAward(Long gameRecordReportId,String staticsTimes,Long firstProxy,Long secondProxy,Long thirdProxy,String platform,BigDecimal todayAward);

    @Modifying
    @Query(value = "INSERT INTO game_record_report_new (game_record_report_id,statics_times,bet_amount,valid_amount,"
        + "win_loss_amount,amount,betting_number,first_proxy,second_proxy,third_proxy,platform,surplus_amount,user_amount,new_amount,new_surplus_amount,new_user_amount,today_award,rise_award) " +
        "VALUES (?1,?2,0,0,0,0,0,?3,?4,?5,?6,0,0,0,0,0,0,?7) ON DUPLICATE KEY UPDATE rise_award = ?7 ;",nativeQuery = true)
    void updateKeyRiseAward(Long gameRecordReportId,String staticsTimes,Long firstProxy,Long secondProxy,Long thirdProxy,String platform,BigDecimal riseAward);
}

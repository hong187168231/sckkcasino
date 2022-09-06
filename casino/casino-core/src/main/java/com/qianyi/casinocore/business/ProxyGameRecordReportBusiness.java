package com.qianyi.casinocore.business;

import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.util.CommonUtil;
import com.qianyi.casinocore.util.RedisLockUtil;
import com.qianyi.casinocore.vo.ProxyGameRecordReportVo;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Objects;

@Slf4j
@Service
public class ProxyGameRecordReportBusiness {

    @Autowired
    private ProxyGameRecordReportService proxyGameRecordReportService;

    @Autowired
    private GameRecordService gameRecordService;

    @Autowired
    private GameRecordGoldenFService gameRecordGoldenFService;

    @Autowired
    private UserGameRecordReportService userGameRecordReportService;

    @Autowired
    private GameRecordObdjService gameRecordObdjService;

    @Autowired
    private GameRecordObtyService gameRecordObtyService;

    @Autowired
    private GameRecordAeService gameRecordAeService;

    @Autowired
    private RedisLockUtil redisLockUtil;

    //统计时间超过24小时记录日志
    public final static int hour = 12;


    @Transactional
    public void saveOrUpdate(ProxyGameRecordReportVo proxyGameRecordReportVo) {
        if (proxyGameRecordReportVo==null|| proxyGameRecordReportVo.getPlatform() == null || proxyGameRecordReportVo.getGameRecordId() == null){
            log.error("代理报表异步处理平台编码为空{}",proxyGameRecordReportVo);
            return;
        }
        String value = proxyGameRecordReportVo.getPlatform() + proxyGameRecordReportVo.getGameRecordId().toString();
        String key = MessageFormat.format(RedisLockUtil.PROXY_GAME_RECORD_REPORT_BUSINESS,value);
        Boolean lock = false;
        try {
            lock = redisLockUtil.getLock(key, value);
            if (lock){
                log.info("取到redis锁{}",key);
                if (proxyGameRecordReportVo.getPlatform().equals(Constants.PLATFORM_WM)){
                    GameRecord gameRecord = gameRecordService.findGameRecordById(proxyGameRecordReportVo.getGameRecordId());
                    if (gameRecord == null || gameRecord.getGameRecordStatus() == Constants.yes){
                        log.error("wm注单状态异常{}",proxyGameRecordReportVo.getGameRecordId());
                        return;
                    }
                }else if (proxyGameRecordReportVo.getPlatform().equals(Constants.PLATFORM_OBDJ)){
                    GameRecordObdj gameRecordById = gameRecordObdjService.findGameRecordById(proxyGameRecordReportVo.getGameRecordId());
                    if (gameRecordById == null || gameRecordById.getGameRecordStatus() == Constants.yes){
                        log.error("OB电竞注单状态异常{}",proxyGameRecordReportVo.getGameRecordId());
                        return;
                    }
                }else if (proxyGameRecordReportVo.getPlatform().equals(Constants.PLATFORM_OBTY)){
                    GameRecordObty gameRecordById =
                        gameRecordObtyService.findGameRecordById(proxyGameRecordReportVo.getGameRecordId());
                    if (gameRecordById == null || gameRecordById.getGameRecordStatus() == Constants.yes){
                        log.error("OB体育注单状态异常{}",proxyGameRecordReportVo.getGameRecordId());
                        return;
                    }
                }else if (proxyGameRecordReportVo.getPlatform().equals(Constants.PLATFORM_AE_HORSEBOOK) || proxyGameRecordReportVo.getPlatform().equals(Constants.PLATFORM_AE_SV388) ||
                    proxyGameRecordReportVo.getPlatform().equals(Constants.PLATFORM_AE_E1SPORT) || proxyGameRecordReportVo.getPlatform().equals(Constants.PLATFORM_AE)){
                    GameRecordAe gameRecordById = gameRecordAeService.findGameRecordById(proxyGameRecordReportVo.getGameRecordId());
                    if (gameRecordById == null){
                        log.error("AE注单状态异常{}",proxyGameRecordReportVo.getGameRecordId());
                        return;
                    }
                    if (proxyGameRecordReportVo.getIsAdd() == null){
                        proxyGameRecordReportVo.setIsAdd(1);
                    }
                    if (gameRecordById.getGameRecordStatus() == Constants.yes && proxyGameRecordReportVo.getIsAdd() == 1){
                        log.error("AE注单状态异常{}",proxyGameRecordReportVo.getGameRecordId());
                        return;
                    }
                    if (gameRecordById.getGameRecordStatus() == Constants.yes && proxyGameRecordReportVo.getIsAdd() == 0){
                        this.updateAe(gameRecordById,proxyGameRecordReportVo);
                        return;
                    }
                    proxyGameRecordReportVo.setBetAmount(gameRecordById.getBetAmount());
                }else {
                    GameRecordGoldenF gameRecordById = gameRecordGoldenFService.findGameRecordById(proxyGameRecordReportVo.getGameRecordId());
                    if (gameRecordById == null || gameRecordById.getGameRecordStatus() == Constants.yes){
                        log.error("电子注单状态异常{}",proxyGameRecordReportVo.getGameRecordId());
                        return;
                    }
                    if (gameRecordById.getVendorCode().equals(Constants.PLATFORM_SABASPORT)){
                        if (gameRecordById.getTransType().equals("Payoff")){
                            GameRecordGoldenF stake = gameRecordGoldenFService
                                .findByBetIdAndTransTypeAndVendorCode(gameRecordById.getBetId(), "Stake",
                                    Constants.PLATFORM_SABASPORT);
                            if (Objects.isNull(stake) || stake.getGameRecordStatus() == Constants.yes){
                                log.error("沙巴体育注单Stake状态异常{}",proxyGameRecordReportVo.getGameRecordId());
                                return;
                            }
                            proxyGameRecordReportVo = getProxyGameRecordReportVo(gameRecordById,stake);
                        }else {
                            GameRecordGoldenF payoff = gameRecordGoldenFService
                                .findByBetIdAndTransTypeAndVendorCode(gameRecordById.getBetId(), "Payoff",
                                    Constants.PLATFORM_SABASPORT);
                            if (Objects.isNull(payoff) || payoff.getGameRecordStatus() == Constants.yes){
                                log.info("沙巴体育注单未结算不计算{}",proxyGameRecordReportVo.getGameRecordId());
                                return;
                            }
                            proxyGameRecordReportVo = getProxyGameRecordReportVo(payoff,gameRecordById);
                        }
                    }
                }
                Date date = DateUtil.getSimpleDateFormat().parse(proxyGameRecordReportVo.getOrderTimes());
                int hours = DateUtil.differentDaysByMillisecond(new Date(), date);
                if (hours >= hour){
                    log.error("代理报表异步处理超时注单,注单标识{}={}={}",proxyGameRecordReportVo.getPlatform(),proxyGameRecordReportVo.getGameRecordId(),proxyGameRecordReportVo.getOrderTimes());
                }
                Date americaDate = cn.hutool.core.date.DateUtil.offsetHour(date, -12);//转为美东时间保存,代理报表全部用美东时间
                String orderTimes = DateUtil.dateToPatten1(americaDate);
                Long proxyGameRecordReportId = CommonUtil.toHash(orderTimes+proxyGameRecordReportVo.getUserId().toString());
                if (proxyGameRecordReportVo.getThirdProxy() == null){
                    proxyGameRecordReportService.updateKey(proxyGameRecordReportId,proxyGameRecordReportVo.getUserId(),
                        orderTimes,proxyGameRecordReportVo.getValidAmount(),proxyGameRecordReportVo.getWinLoss(),0L,
                        0L,0L,proxyGameRecordReportVo.getBetAmount());
                }else {
                    proxyGameRecordReportService.updateKey(proxyGameRecordReportId,proxyGameRecordReportVo.getUserId(),
                        orderTimes,proxyGameRecordReportVo.getValidAmount(),proxyGameRecordReportVo.getWinLoss(),proxyGameRecordReportVo.getFirstProxy(),
                        proxyGameRecordReportVo.getSecondProxy(),proxyGameRecordReportVo.getThirdProxy(),proxyGameRecordReportVo.getBetAmount());
                }

                String userGameTimes = DateUtil.dateToPatten1(date);
                Long userGameRecordReportId = CommonUtil.toHash(userGameTimes+proxyGameRecordReportVo.getUserId().toString()+proxyGameRecordReportVo.getPlatform());
                userGameRecordReportService.updateKey(userGameRecordReportId,proxyGameRecordReportVo.getUserId(),userGameTimes,
                    proxyGameRecordReportVo.getValidAmount(),proxyGameRecordReportVo.getWinLoss(),proxyGameRecordReportVo.getBetAmount(),proxyGameRecordReportVo.getPlatform());

                //更新注单状态
                if (proxyGameRecordReportVo.getPlatform().equals(Constants.PLATFORM_WM)){
                    gameRecordService.updateGameRecordStatus(proxyGameRecordReportVo.getGameRecordId(),Constants.yes);
                }else if (proxyGameRecordReportVo.getPlatform().equals(Constants.PLATFORM_OBDJ)){
                    gameRecordObdjService.updateGameRecordStatus(proxyGameRecordReportVo.getGameRecordId(),Constants.yes);
                }else if (proxyGameRecordReportVo.getPlatform().equals(Constants.PLATFORM_OBTY)){
                    gameRecordObtyService.updateGameRecordStatus(proxyGameRecordReportVo.getGameRecordId(),Constants.yes);
                }else if (proxyGameRecordReportVo.getPlatform().equals(Constants.PLATFORM_SABASPORT)){
                    gameRecordGoldenFService.updateGameRecordStatus(proxyGameRecordReportVo.getBetId(),Constants.PLATFORM_SABASPORT,Constants.yes);
                }else if (proxyGameRecordReportVo.getPlatform().equals(Constants.PLATFORM_AE_HORSEBOOK) ||
                    proxyGameRecordReportVo.getPlatform().equals(Constants.PLATFORM_AE_SV388) ||
                    proxyGameRecordReportVo.getPlatform().equals(Constants.PLATFORM_AE_E1SPORT) ||
                    proxyGameRecordReportVo.getPlatform().equals(Constants.PLATFORM_AE)){
                    gameRecordAeService.updateGameRecordStatus(proxyGameRecordReportVo.getGameRecordId(),Constants.yes);
                }else {
                    gameRecordGoldenFService.updateGameRecordStatus(proxyGameRecordReportVo.getGameRecordId(),Constants.yes);
                }
            }
        }catch (Exception ex){
            log.error("代理报表异步处理异常需要人工补单,注单标识{}={}",proxyGameRecordReportVo.getPlatform(),proxyGameRecordReportVo.getGameRecordId());
        }finally {
            if (lock){
                log.info("释放redis锁{}",key);
                redisLockUtil.releaseLock(key, value);
            }
        }
    }

    private void updateAe(GameRecordAe gameRecordAe,ProxyGameRecordReportVo proxyGameRecordReportVo) throws ParseException {
        log.info("修改AE注单报表,注单标识{}={}={}",gameRecordAe.getPlatform(),gameRecordAe.getId(),gameRecordAe.getBetTime());
        Date date = DateUtil.getSimpleDateFormat().parse(gameRecordAe.getBetTime());
        int hours = DateUtil.differentDaysByMillisecond(new Date(), date);
        if (hours >= hour){
            log.error("代理报表异步处理超时注单,注单标识{}={}={}",gameRecordAe.getPlatform(),gameRecordAe.getId(),gameRecordAe.getBetTime());
        }
        Date americaDate = cn.hutool.core.date.DateUtil.offsetHour(date, -12);//转为美东时间保存,代理报表全部用美东时间
        String orderTimes = DateUtil.dateToPatten1(americaDate);
        Long proxyGameRecordReportId = CommonUtil.toHash(orderTimes+gameRecordAe.getUserId().toString());
        if (gameRecordAe.getThirdProxy() == null){
            proxyGameRecordReportService.updateBet(proxyGameRecordReportId,gameRecordAe.getUserId(),
                orderTimes,proxyGameRecordReportVo.getValidAmount(),proxyGameRecordReportVo.getWinLoss(),0L,
                0L,0L,proxyGameRecordReportVo.getBetAmount());
        }else {
            proxyGameRecordReportService.updateBet(proxyGameRecordReportId,gameRecordAe.getUserId(),
                orderTimes,proxyGameRecordReportVo.getValidAmount(),proxyGameRecordReportVo.getWinLoss(),proxyGameRecordReportVo.getFirstProxy(),
                proxyGameRecordReportVo.getSecondProxy(),proxyGameRecordReportVo.getThirdProxy(),proxyGameRecordReportVo.getBetAmount());
        }

        String userGameTimes = DateUtil.dateToPatten1(date);
        Long userGameRecordReportId = CommonUtil.toHash(userGameTimes+proxyGameRecordReportVo.getUserId().toString()+proxyGameRecordReportVo.getPlatform());
        userGameRecordReportService.updateBet(userGameRecordReportId,proxyGameRecordReportVo.getUserId(),userGameTimes,
            proxyGameRecordReportVo.getValidAmount(),proxyGameRecordReportVo.getWinLoss(),proxyGameRecordReportVo.getBetAmount(),proxyGameRecordReportVo.getPlatform());
    }

    private ProxyGameRecordReportVo getProxyGameRecordReportVo(GameRecordGoldenF payoff,GameRecordGoldenF stake){
        ProxyGameRecordReportVo vo = new ProxyGameRecordReportVo();
        vo.setGameRecordId(payoff.getId());
        vo.setOrderId(payoff.getTraceId());
        vo.setFirstProxy(payoff.getFirstProxy());
        vo.setSecondProxy(payoff.getSecondProxy());
        vo.setThirdProxy(payoff.getThirdProxy());
        vo.setOrderTimes(payoff.getCreateAtStr());
        vo.setUserId(payoff.getUserId());
        vo.setValidAmount(stake.getBetAmount());
        vo.setWinLoss(payoff.getWinAmount().subtract(stake.getBetAmount()));
        vo.setBetAmount(stake.getBetAmount());
        vo.setPlatform(payoff.getVendorCode());
        vo.setBetId(payoff.getBetId());
        return vo;
    }
}

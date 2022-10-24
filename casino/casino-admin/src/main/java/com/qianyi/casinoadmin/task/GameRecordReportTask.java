package com.qianyi.casinoadmin.task;

import com.qianyi.casinocore.business.ProxyGameRecordReportBusiness;
import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.util.CommonUtil;
import com.qianyi.casinocore.util.TaskConst;
import com.qianyi.casinocore.vo.ProxyGameRecordReportVo;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;

/**
 * 会员报表补单定时任务，主要处理mq异步订单没有处理的注单
 *
 */
@Slf4j
@Component
public class GameRecordReportTask {

    @Autowired
    private GameRecordService gameRecordService;

    @Autowired
    private GameRecordObdjService gameRecordObdjService;

    @Autowired
    private GameRecordObtyService gameRecordObtyService;

    @Autowired
    private GameRecordGoldenFService gameRecordGoldenFService;

    @Autowired
    private GameRecordAeService gameRecordAeService;

    @Autowired
    private RptBetInfoDetailService rptBetInfoDetailService;

    public static final List<Integer> betStatus = new ArrayList<>();

    static {
        betStatus.add(5);
        betStatus.add(6);
        betStatus.add(8);
        betStatus.add(9);
        betStatus.add(10);
    }

    public final static String start = " 12:00:00";

    public final static String end = " 11:59:59";

    @Autowired
    private ProxyGameRecordReportBusiness proxyGameRecordReportBusiness;

    @Autowired
    private ProxyGameRecordReportService proxyGameRecordReportService;

    @Autowired
    private UserGameRecordReportService userGameRecordReportService;

    @Scheduled(cron = TaskConst.GAME_RECORD_REPORT_TASK)
    public void begin() {
        this.replacementOrder();
        this.comparison();
    }

    private void comparison(){
        log.info("每日会员报表计算定时任务开始start=============================================》");
        long startTime = System.currentTimeMillis();
        Calendar nowTime = Calendar.getInstance();
        //计算最近十天注单
        nowTime.add(Calendar.DATE, -10);
        Date startDate = nowTime.getTime();
        //        Map<Integer,String> mapDate = CommonUtil.findDates("D", startDate, DateUtil.getYesterday());
        //        mapDate.forEach((k,v)->{
        //            userGameRecordReportService.comparison(v);
        //            proxyGameRecordReportService.comparison(v);
        //        });
        String startDay = DateUtil.getSimpleDateFormat(DateUtil.patten1).format(startDate);
        String yesterday = DateUtil.getSimpleDateFormat(DateUtil.patten1).format(DateUtil.getYesterday());

        this.delete(yesterday);

        List<String> betweenDate = DateUtil.getBetweenDate(startDay, yesterday);
        for (String str:betweenDate){
            userGameRecordReportService.comparison(str);
            proxyGameRecordReportService.comparison(str);
        }
        log.info("每日会员报表计算定时任务结束耗时{}=============================================》",System.currentTimeMillis()-startTime);
    }

    private void delete(String yesterday){
        try {
            log.info("删除昨日数据{}",yesterday);
            proxyGameRecordReportService.deleteByOrderTimes(yesterday);

            userGameRecordReportService.deleteByOrderTimes(yesterday);
        }catch (Exception ex){
            log.error("删除昨日数据失败{}",yesterday);
        }
    }

    private void replacementOrder() {
        log.info("每日会员报表补单定时任务开始start=============================================》");
        Calendar nowTime = Calendar.getInstance();
        String today = DateUtil.getSimpleDateFormat1().format(nowTime.getTime());
        nowTime.add(Calendar.DATE, -1);
        String yesterday = DateUtil.getSimpleDateFormat1().format(nowTime.getTime());
        String startTime = yesterday + start;
        String endTime = today + end;
        log.info("每日会员报表补单定时startTime:{}-endTime:{}", startTime, endTime);
        List<ProxyGameRecordReportVo> proxyGameRecordReportVos = new ArrayList<>();
        GameRecord gameRecord = new GameRecord();
        gameRecord.setGameRecordStatus(0);
        List<GameRecord> gameRecords = gameRecordService.findGameRecord(gameRecord, startTime, endTime);
        if (gameRecords != null && gameRecords.size() >= 1) {
            proxyGameRecordReportVos = assemblyGameRecord(proxyGameRecordReportVos, gameRecords);
        }
        GameRecordGoldenF pg = new GameRecordGoldenF();
        pg.setGameRecordStatus(0);
        pg.setVendorCode(Constants.PLATFORM_PG);
        List<GameRecordGoldenF> pgs =
            gameRecordGoldenFService.findGameRecord(pg, startTime, endTime);
        if (pgs != null && pgs.size() >= 1) {
            proxyGameRecordReportVos = assemblyGameRecordGoldenF(proxyGameRecordReportVos, pgs);
        }

        GameRecordGoldenF cq9 = new GameRecordGoldenF();
        cq9.setGameRecordStatus(0);
        cq9.setVendorCode(Constants.PLATFORM_CQ9);
        List<GameRecordGoldenF> cq9s =
            gameRecordGoldenFService.findGameRecord(cq9, startTime, endTime);
        if (cq9s != null && cq9s.size() >= 1) {
            proxyGameRecordReportVos = assemblyGameRecordGoldenF(proxyGameRecordReportVos, cq9s);
        }

        GameRecordGoldenF sb = new GameRecordGoldenF();
        sb.setGameRecordStatus(0);
        sb.setVendorCode(Constants.PLATFORM_SABASPORT);
        sb.setTransType("Payoff");
        List<GameRecordGoldenF> sbs =
            gameRecordGoldenFService.findGameRecord(sb, startTime, endTime);
        if (sbs != null && sbs.size() >= 1) {
            proxyGameRecordReportVos = assemblyGameRecordGoldenF(proxyGameRecordReportVos, sbs);
        }

        GameRecordObdj gameRecordObdj = new GameRecordObdj();
        gameRecordObdj.setGameRecordStatus(0);
        List<GameRecordObdj> gameRecordObdjs =
            gameRecordObdjService.findGameRecord(gameRecordObdj, startTime, endTime, betStatus);
        if (gameRecordObdjs != null && gameRecordObdjs.size() >= 1) {
            proxyGameRecordReportVos = assemblyGameRecordObdj(proxyGameRecordReportVos, gameRecordObdjs);
        }
        GameRecordObty gameRecordObty = new GameRecordObty();
        gameRecordObty.setGameRecordStatus(0);
        List<GameRecordObty> gameRecordObtys = gameRecordObtyService.findGameRecord(gameRecordObty, startTime, endTime);
        if (gameRecordObtys != null && gameRecordObtys.size() >= 1) {
            proxyGameRecordReportVos = assemblyGameRecordObty(proxyGameRecordReportVos, gameRecordObtys);
        }

        GameRecordAe gameRecordAe = new GameRecordAe();
        gameRecordAe.setTxStatus(1);
        gameRecordAe.setGameRecordStatus(0);
        List<GameRecordAe> gameRecordAes = gameRecordAeService.findGameRecordAe(gameRecordAe, startTime, endTime);
        if (gameRecordAes != null && gameRecordAes.size() >= 1) {
            proxyGameRecordReportVos = assemblyGameRecordAe(proxyGameRecordReportVos, gameRecordAes);
        }

        RptBetInfoDetail gameRecordVNC = new RptBetInfoDetail();
        gameRecordVNC.setGameRecordStatus(0);
        List<RptBetInfoDetail> gameRecordVNCS = rptBetInfoDetailService.findRptBetInfoDetail(gameRecordVNC, startTime, endTime);
        if (gameRecordVNCS != null && gameRecordVNCS.size() >= 1) {
            proxyGameRecordReportVos = assemblyGameRecordVNC(proxyGameRecordReportVos, gameRecordVNCS);
        }

        if (proxyGameRecordReportVos.size() >= 1) {
            log.info("统计到未处理的注单{}==========================================>", proxyGameRecordReportVos.size());
            for (ProxyGameRecordReportVo proxyGameRecordReportVo : proxyGameRecordReportVos) {
                proxyGameRecordReportBusiness.saveOrUpdate(proxyGameRecordReportVo);
            }
            log.info("处理结束，任务结束end==========================================>");
        } else {
            log.info("没有未处理的注单，任务结束end==========================================>");
        }
    }

    private List<ProxyGameRecordReportVo> assemblyGameRecord(List<ProxyGameRecordReportVo> proxyGameRecordReportVos,
        List<GameRecord> gameRecords) {
        try {
            for (GameRecord gameRecord : gameRecords) {
                ProxyGameRecordReportVo vo = new ProxyGameRecordReportVo();
                vo.setGameRecordId(gameRecord.getId());
                vo.setOrderId(gameRecord.getBetId());
                vo.setFirstProxy(gameRecord.getFirstProxy());
                vo.setSecondProxy(gameRecord.getSecondProxy());
                vo.setThirdProxy(gameRecord.getThirdProxy());
                vo.setOrderTimes(gameRecord.getBetTime());
                vo.setUserId(gameRecord.getUserId());
                vo.setValidAmount(new BigDecimal(gameRecord.getValidbet()));
                vo.setWinLoss(new BigDecimal(gameRecord.getWinLoss()));
                vo.setBetAmount(new BigDecimal(gameRecord.getBet()));
                vo.setPlatform(Constants.PLATFORM_WM);
                proxyGameRecordReportVos.add(vo);
            }
        } catch (Exception ex) {
            log.error("组装wm注单异常{}", ex);
        }
        return proxyGameRecordReportVos;
    }

    private List<ProxyGameRecordReportVo> assemblyGameRecordGoldenF(
        List<ProxyGameRecordReportVo> proxyGameRecordReportVos, List<GameRecordGoldenF> gameRecordGoldenFS) {
        try {
            for (GameRecordGoldenF gameRecord : gameRecordGoldenFS) {
                ProxyGameRecordReportVo vo = new ProxyGameRecordReportVo();
                vo.setGameRecordId(gameRecord.getId());
                vo.setOrderId(gameRecord.getTraceId());
                vo.setFirstProxy(gameRecord.getFirstProxy());
                vo.setSecondProxy(gameRecord.getSecondProxy());
                vo.setThirdProxy(gameRecord.getThirdProxy());
                vo.setOrderTimes(gameRecord.getCreateAtStr());
                vo.setUserId(gameRecord.getUserId());
                vo.setValidAmount(gameRecord.getBetAmount());
                vo.setWinLoss(gameRecord.getWinAmount().subtract(gameRecord.getBetAmount()));
                vo.setBetAmount(gameRecord.getBetAmount());
                vo.setPlatform(gameRecord.getVendorCode());
                proxyGameRecordReportVos.add(vo);
            }
        } catch (Exception ex) {
            log.error("组装PG注单异常{}", ex);
        }

        return proxyGameRecordReportVos;
    }

    private List<ProxyGameRecordReportVo> assemblyGameRecordObdj(List<ProxyGameRecordReportVo> proxyGameRecordReportVos,
        List<GameRecordObdj> gameRecords) {
        try {
            for (GameRecordObdj gameRecord : gameRecords) {
                ProxyGameRecordReportVo vo = new ProxyGameRecordReportVo();
                vo.setGameRecordId(gameRecord.getId());
                vo.setOrderId(gameRecord.getBetId().toString());
                vo.setFirstProxy(gameRecord.getFirstProxy());
                vo.setSecondProxy(gameRecord.getSecondProxy());
                vo.setThirdProxy(gameRecord.getThirdProxy());
                vo.setOrderTimes(gameRecord.getSetStrTime());
                vo.setUserId(gameRecord.getUserId());
                vo.setValidAmount(gameRecord.getBetAmount());
                vo.setWinLoss(BigDecimal.ZERO);
                if (gameRecord.getWinAmount() != null && gameRecord.getBetAmount() != null) {
                    BigDecimal winLoss = gameRecord.getWinAmount().subtract(gameRecord.getBetAmount());
                    vo.setWinLoss(winLoss);
                }
                vo.setBetAmount(gameRecord.getBetAmount());
                vo.setPlatform(Constants.PLATFORM_OBDJ);
                proxyGameRecordReportVos.add(vo);
            }
        } catch (Exception ex) {
            log.error("组装OBDJ注单异常{}", ex);
        }

        return proxyGameRecordReportVos;
    }

    private List<ProxyGameRecordReportVo> assemblyGameRecordObty(List<ProxyGameRecordReportVo> proxyGameRecordReportVos,
        List<GameRecordObty> gameRecords) {
        try {
            for (GameRecordObty gameRecord : gameRecords) {
                ProxyGameRecordReportVo vo = new ProxyGameRecordReportVo();
                vo.setGameRecordId(gameRecord.getId());
                vo.setOrderId(gameRecord.getOrderNo());
                vo.setFirstProxy(gameRecord.getFirstProxy());
                vo.setSecondProxy(gameRecord.getSecondProxy());
                vo.setThirdProxy(gameRecord.getThirdProxy());
                vo.setOrderTimes(gameRecord.getSettleStrTime());
                vo.setUserId(gameRecord.getUserId());
                vo.setValidAmount(gameRecord.getOrderAmount());
                vo.setWinLoss(BigDecimal.ZERO);
                if (gameRecord.getProfitAmount() != null) {
                    vo.setWinLoss(gameRecord.getProfitAmount());
                }
                vo.setBetAmount(gameRecord.getOrderAmount());
                vo.setPlatform(Constants.PLATFORM_OBTY);
                proxyGameRecordReportVos.add(vo);
            }
        } catch (Exception ex) {
            log.error("组装OBTY注单异常{}", ex);
        }

        return proxyGameRecordReportVos;
    }

    private List<ProxyGameRecordReportVo> assemblyGameRecordAe(List<ProxyGameRecordReportVo> proxyGameRecordReportVos,
        List<GameRecordAe> gameRecordAes) {
        try {
            for (GameRecordAe gameRecordAe : gameRecordAes) {
                ProxyGameRecordReportVo vo = new ProxyGameRecordReportVo();
                vo.setGameRecordId(gameRecordAe.getId());
                vo.setOrderId(gameRecordAe.getPlatformTxId());
                vo.setFirstProxy(gameRecordAe.getFirstProxy());
                vo.setSecondProxy(gameRecordAe.getSecondProxy());
                vo.setThirdProxy(gameRecordAe.getThirdProxy());
                vo.setOrderTimes(gameRecordAe.getBetTime());
                vo.setUserId(gameRecordAe.getUserId());
                vo.setValidAmount(gameRecordAe.getTurnover());
                vo.setWinLoss(BigDecimal.ZERO);
                if (gameRecordAe.getRealWinAmount() != null) {
                    vo.setWinLoss(gameRecordAe.getRealWinAmount().subtract(gameRecordAe.getRealBetAmount()));
                }
                vo.setBetAmount(gameRecordAe.getBetAmount());
                //                vo.setPlatform(gameRecordAe.getPlatform());
                vo.setPlatform(Constants.PLATFORM_AE);
                proxyGameRecordReportVos.add(vo);
            }
        } catch (Exception ex) {
            log.error("组装AE注单异常{}", ex);
        }

        return proxyGameRecordReportVos;
    }

    private List<ProxyGameRecordReportVo> assemblyGameRecordVNC(List<ProxyGameRecordReportVo> proxyGameRecordReportVos,
        List<RptBetInfoDetail> gameRecordVNCS) {
        try {
            for (RptBetInfoDetail gameRecordVNC : gameRecordVNCS) {
                ProxyGameRecordReportVo vo = new ProxyGameRecordReportVo();
                vo.setGameRecordId(gameRecordVNC.getId());
                vo.setOrderId(gameRecordVNC.getBetOrder());
                vo.setFirstProxy(gameRecordVNC.getFirstProxy());
                vo.setSecondProxy(gameRecordVNC.getSecondProxy());
                vo.setThirdProxy(gameRecordVNC.getThirdProxy());
                vo.setOrderTimes(cn.hutool.core.date.DateUtil.formatDateTime(gameRecordVNC.getSettleTime()));
                vo.setUserId(gameRecordVNC.getUserId());
                vo.setValidAmount(gameRecordVNC.getRealMoney());
                vo.setWinLoss(BigDecimal.ZERO.subtract(gameRecordVNC.getRealMoney()));
                if (gameRecordVNC.getWinMoney() != null) {
                    vo.setWinLoss(gameRecordVNC.getWinMoney().subtract(gameRecordVNC.getRealMoney()));
                }
                vo.setBetAmount(gameRecordVNC.getBetMoney());
                vo.setPlatform(Constants.PLATFORM_VNC);
                proxyGameRecordReportVos.add(vo);
            }
        } catch (Exception ex) {
            log.error("组装VNC注单异常{}", ex);
        }

        return proxyGameRecordReportVos;
    }
}

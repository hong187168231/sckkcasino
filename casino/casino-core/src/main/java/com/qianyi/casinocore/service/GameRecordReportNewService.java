package com.qianyi.casinocore.service;

import cn.hutool.core.collection.CollUtil;
import com.qianyi.casinocore.model.GameRecordEndIndex;
import com.qianyi.casinocore.model.GameRecordReportNew;
import com.qianyi.casinocore.repository.GameRecordReportNewRepository;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinocore.util.CommonUtil;
import com.qianyi.modulecommon.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class GameRecordReportNewService {
    @Autowired
    private GameRecordReportNewRepository gameRecordReport01Repository;

    @Autowired
    private GameRecordService gameRecordService;

    @Autowired
    private GameRecordGoldenFService gameRecordGoldenFService;

    @Autowired
    private GameRecordEndIndexService gameRecordEndIndexService;

    @Autowired
    private GameRecordObdjService gameRecordObdjService;

    @Autowired
    private GameRecordObtyService gameRecordObtyService;

    @Autowired
    private GameRecordObzrService gameRecordObzrService;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private RebateDetailService rebateDetailService;

    @Autowired
    private WashCodeChangeService washCodeChangeService;

    @Autowired
    public GameRecordAeService gameRecordAeService;

    @Autowired
    public GameRecordDMCService gameRecordDMCService;

    @Autowired
    private RptBetInfoDetailService rptBetInfoDetailService;

    @Autowired
    private GameRecordDGService gameRecordDGService;

    @Autowired
    private AwardReceiveRecordService awardReceiveRecordService;

    @Transactional
    public void saveGameRecordReportWM() {
        GameRecordEndIndex first = gameRecordEndIndexService.findUGameRecordEndIndexUseLock();
        if (first == null) {
            return;
        }
        log.info("得到wm注单下标{}", first.getGameRecordId());
        List<Map<String, Object>> reportResult = gameRecordService.queryGameRecords(first.getGameRecordId(), 13);
        try {
            if (reportResult == null || reportResult.size() == CommonConst.NUMBER_0) {
                return;
            }
            if (reportResult.get(0).get("num") == null
                || Integer.parseInt(reportResult.get(0).get("num").toString()) == CommonConst.NUMBER_0) {
                return;
            }
            Long max = 0L;
            for (Map<String, Object> map : reportResult) {
                GameRecordReportNew gameRecordReport = new GameRecordReportNew();
                Long maxId = Long.parseLong(map.get("maxId").toString());
                if (maxId > max)
                    max = maxId;
                gameRecordReport.setStaticsTimes(map.get("set_time").toString());
                gameRecordReport.setAmount(new BigDecimal(map.get("amount").toString()));
                gameRecordReport.setBetAmount(new BigDecimal(map.get("bet").toString()));
                gameRecordReport.setValidAmount(new BigDecimal(map.get("validbet").toString()));
                gameRecordReport.setWinLossAmount(new BigDecimal(map.get("win_loss").toString()));
                gameRecordReport.setBettingNumber(Integer.parseInt(map.get("num").toString()));
                gameRecordReport.setUserAmount(new BigDecimal(map.get("user_amount").toString()));
                gameRecordReport.setSurplusAmount(new BigDecimal(map.get("surplus_amount").toString()));
                gameRecordReport.setPlatform("WM");
                gameRecordReport.setFirstProxy(Long.parseLong(map.get("first_proxy").toString()));
                gameRecordReport.setSecondProxy(Long.parseLong(map.get("second_proxy").toString()));
                gameRecordReport.setThirdProxy(Long.parseLong(map.get("third_proxy").toString()));
                gameRecordReport.setGameRecordReportId(CommonUtil.toHash(gameRecordReport.getStaticsTimes()
                    + gameRecordReport.getThirdProxy() + gameRecordReport.getPlatform()));
                gameRecordReport01Repository.updateKey(gameRecordReport.getGameRecordReportId(),
                    gameRecordReport.getStaticsTimes(), gameRecordReport.getBetAmount(),
                    gameRecordReport.getValidAmount(), gameRecordReport.getWinLossAmount(),
                    gameRecordReport.getAmount(), gameRecordReport.getBettingNumber(), gameRecordReport.getFirstProxy(),
                    gameRecordReport.getSecondProxy(), gameRecordReport.getThirdProxy(), gameRecordReport.getPlatform(),
                    gameRecordReport.getSurplusAmount(), gameRecordReport.getUserAmount());
            }
            first.setGameRecordId(max);
            log.info("保存wm注单下标{}", first.getGameRecordId());
            gameRecordEndIndexService.save(first);
        } catch (Exception ex) {
            log.error("每小时报表统计失败", ex);
        }
    }

    @Transactional
    public void saveGameRecordReportPG() {
        GameRecordEndIndex first = gameRecordEndIndexService.findUGameRecordEndIndexUseLock();
        if (first == null) {
            return;
        }
        log.info("得到PG注单下标{}", first.getPGMaxId());
        List<Map<String, Object>> reportResult =
            gameRecordGoldenFService.queryGameRecords(first.getPGMaxId(), 13, "PG");
        try {
            if (reportResult == null || reportResult.size() == CommonConst.NUMBER_0) {
                return;
            }
            if (reportResult.get(0).get("num") == null
                || Integer.parseInt(reportResult.get(0).get("num").toString()) == CommonConst.NUMBER_0) {
                return;
            }
            Long max = 0L;
            for (Map<String, Object> map : reportResult) {
                GameRecordReportNew gameRecordReport = new GameRecordReportNew();
                Long maxId = Long.parseLong(map.get("maxId").toString());
                if (maxId > max)
                    max = maxId;
                gameRecordReport.setStaticsTimes(map.get("set_time").toString());
                gameRecordReport.setAmount(new BigDecimal(map.get("amount").toString()));
                gameRecordReport.setBetAmount(new BigDecimal(map.get("bet").toString()));
                gameRecordReport.setValidAmount(new BigDecimal(map.get("validbet").toString()));
                gameRecordReport.setWinLossAmount(
                    new BigDecimal(map.get("win_loss").toString()).subtract(gameRecordReport.getBetAmount()));
                gameRecordReport.setBettingNumber(Integer.parseInt(map.get("num").toString()));
                gameRecordReport.setUserAmount(new BigDecimal(map.get("user_amount").toString()));
                gameRecordReport.setSurplusAmount(new BigDecimal(map.get("surplus_amount").toString()));
                gameRecordReport.setPlatform("PG");
                gameRecordReport.setFirstProxy(Long.parseLong(map.get("first_proxy").toString()));
                gameRecordReport.setSecondProxy(Long.parseLong(map.get("second_proxy").toString()));
                gameRecordReport.setThirdProxy(Long.parseLong(map.get("third_proxy").toString()));
                gameRecordReport.setGameRecordReportId(CommonUtil.toHash(gameRecordReport.getStaticsTimes()
                    + gameRecordReport.getThirdProxy() + gameRecordReport.getPlatform()));
                gameRecordReport01Repository.updateKey(gameRecordReport.getGameRecordReportId(),
                    gameRecordReport.getStaticsTimes(), gameRecordReport.getBetAmount(),
                    gameRecordReport.getValidAmount(), gameRecordReport.getWinLossAmount(),
                    gameRecordReport.getAmount(), gameRecordReport.getBettingNumber(), gameRecordReport.getFirstProxy(),
                    gameRecordReport.getSecondProxy(), gameRecordReport.getThirdProxy(), gameRecordReport.getPlatform(),
                    gameRecordReport.getSurplusAmount(), gameRecordReport.getUserAmount());
            }
            first.setPGMaxId(max);
            log.info("保存PG注单下标{}", first.getPGMaxId());
            gameRecordEndIndexService.save(first);
        } catch (Exception ex) {
            log.error("每小时PG报表统计失败", ex);
        }
    }

    @Transactional
    public void saveGameRecordReportCQ9() {
        GameRecordEndIndex first = gameRecordEndIndexService.findUGameRecordEndIndexUseLock();
        if (first == null) {
            return;
        }
        log.info("得到CQ9注单下标{}", first.getCQ9MaxId());
        List<Map<String, Object>> reportResult =
            gameRecordGoldenFService.queryGameRecords(first.getCQ9MaxId(), 13, "CQ9");
        try {
            if (reportResult == null || reportResult.size() == CommonConst.NUMBER_0) {
                return;
            }
            if (reportResult.get(0).get("num") == null
                || Integer.parseInt(reportResult.get(0).get("num").toString()) == CommonConst.NUMBER_0) {
                return;
            }
            Long max = 0L;
            for (Map<String, Object> map : reportResult) {
                GameRecordReportNew gameRecordReport = new GameRecordReportNew();
                Long maxId = Long.parseLong(map.get("maxId").toString());
                if (maxId > max)
                    max = maxId;
                gameRecordReport.setStaticsTimes(map.get("set_time").toString());
                gameRecordReport.setAmount(new BigDecimal(map.get("amount").toString()));
                gameRecordReport.setBetAmount(new BigDecimal(map.get("bet").toString()));
                gameRecordReport.setValidAmount(new BigDecimal(map.get("validbet").toString()));
                gameRecordReport.setWinLossAmount(
                    new BigDecimal(map.get("win_loss").toString()).subtract(gameRecordReport.getBetAmount()));
                gameRecordReport.setBettingNumber(Integer.parseInt(map.get("num").toString()));
                gameRecordReport.setUserAmount(new BigDecimal(map.get("user_amount").toString()));
                gameRecordReport.setSurplusAmount(new BigDecimal(map.get("surplus_amount").toString()));
                gameRecordReport.setPlatform("CQ9");
                gameRecordReport.setFirstProxy(Long.parseLong(map.get("first_proxy").toString()));
                gameRecordReport.setSecondProxy(Long.parseLong(map.get("second_proxy").toString()));
                gameRecordReport.setThirdProxy(Long.parseLong(map.get("third_proxy").toString()));
                gameRecordReport.setGameRecordReportId(CommonUtil.toHash(gameRecordReport.getStaticsTimes()
                    + gameRecordReport.getThirdProxy() + gameRecordReport.getPlatform()));
                gameRecordReport01Repository.updateKey(gameRecordReport.getGameRecordReportId(),
                    gameRecordReport.getStaticsTimes(), gameRecordReport.getBetAmount(),
                    gameRecordReport.getValidAmount(), gameRecordReport.getWinLossAmount(),
                    gameRecordReport.getAmount(), gameRecordReport.getBettingNumber(), gameRecordReport.getFirstProxy(),
                    gameRecordReport.getSecondProxy(), gameRecordReport.getThirdProxy(), gameRecordReport.getPlatform(),
                    gameRecordReport.getSurplusAmount(), gameRecordReport.getUserAmount());
            }
            first.setCQ9MaxId(max);
            log.info("保存CQ9注单下标{}", first.getCQ9MaxId());
            gameRecordEndIndexService.save(first);
        } catch (Exception ex) {
            log.error("每小时CQ9报表统计失败", ex);
        }
    }

    @Transactional
    public void saveGameRecordReportOBDJ() {
        GameRecordEndIndex first = gameRecordEndIndexService.findUGameRecordEndIndexUseLock();
        if (first == null) {
            return;
        }
        log.info("得到OBDJ注单下标{}", first.getOBDJMaxId());
        List<Map<String, Object>> reportResult = gameRecordObdjService.queryGameRecords(first.getOBDJMaxId(), 13);
        try {
            if (reportResult == null || reportResult.size() == CommonConst.NUMBER_0) {
                return;
            }
            if (reportResult.get(0).get("num") == null
                || Integer.parseInt(reportResult.get(0).get("num").toString()) == CommonConst.NUMBER_0) {
                return;
            }
            Long max = 0L;
            for (Map<String, Object> map : reportResult) {
                GameRecordReportNew gameRecordReport = new GameRecordReportNew();
                Long maxId = Long.parseLong(map.get("maxId").toString());
                if (maxId > max)
                    max = maxId;

                if (map.get("set_time") == null) {
                    continue;
                }
                gameRecordReport.setStaticsTimes(map.get("set_time").toString());
                gameRecordReport.setAmount(new BigDecimal(map.get("amount").toString()));
                gameRecordReport.setBetAmount(new BigDecimal(map.get("bet").toString()));
                gameRecordReport.setValidAmount(new BigDecimal(map.get("validbet").toString()));
                gameRecordReport.setWinLossAmount(new BigDecimal(map.get("win_loss").toString()));
                gameRecordReport.setBettingNumber(Integer.parseInt(map.get("num").toString()));
                gameRecordReport.setUserAmount(new BigDecimal(map.get("user_amount").toString()));
                gameRecordReport.setSurplusAmount(new BigDecimal(map.get("surplus_amount").toString()));
                gameRecordReport.setPlatform(Constants.PLATFORM_OBDJ);
                gameRecordReport.setFirstProxy(Long.parseLong(map.get("first_proxy").toString()));
                gameRecordReport.setSecondProxy(Long.parseLong(map.get("second_proxy").toString()));
                gameRecordReport.setThirdProxy(Long.parseLong(map.get("third_proxy").toString()));
                gameRecordReport.setGameRecordReportId(CommonUtil.toHash(gameRecordReport.getStaticsTimes()
                    + gameRecordReport.getThirdProxy() + gameRecordReport.getPlatform()));
                gameRecordReport01Repository.updateKey(gameRecordReport.getGameRecordReportId(),
                    gameRecordReport.getStaticsTimes(), gameRecordReport.getBetAmount(),
                    gameRecordReport.getValidAmount(), gameRecordReport.getWinLossAmount(),
                    gameRecordReport.getAmount(), gameRecordReport.getBettingNumber(), gameRecordReport.getFirstProxy(),
                    gameRecordReport.getSecondProxy(), gameRecordReport.getThirdProxy(), gameRecordReport.getPlatform(),
                    gameRecordReport.getSurplusAmount(), gameRecordReport.getUserAmount());
            }
            first.setOBDJMaxId(max);
            log.info("保存OBDJ注单下标{}", first.getOBDJMaxId());
            gameRecordEndIndexService.save(first);
        } catch (Exception ex) {
            log.error("每小时OBDJ报表统计失败", ex);
        }
    }

    @Transactional
    public void saveGameRecordReportOBTY() {
        GameRecordEndIndex first = gameRecordEndIndexService.findUGameRecordEndIndexUseLock();
        if (first == null) {
            return;
        }
        log.info("得到OBTY注单下标{}", first.getOBTYMaxId());
        List<Map<String, Object>> reportResult = gameRecordObtyService.queryGameRecords(first.getOBTYMaxId(), 13);
        try {
            if (reportResult == null || reportResult.size() == CommonConst.NUMBER_0) {
                return;
            }
            if (reportResult.get(0).get("num") == null
                || Integer.parseInt(reportResult.get(0).get("num").toString()) == CommonConst.NUMBER_0) {
                return;
            }
            Long max = 0L;
            for (Map<String, Object> map : reportResult) {

                GameRecordReportNew gameRecordReport = new GameRecordReportNew();
                Long maxId = Long.parseLong(map.get("maxId").toString());
                if (maxId > max)
                    max = maxId;

                if (map.get("set_time") == null) {
                    continue;
                }

                gameRecordReport.setStaticsTimes(map.get("set_time").toString());
                gameRecordReport.setAmount(new BigDecimal(map.get("amount").toString()));
                gameRecordReport.setBetAmount(new BigDecimal(map.get("bet").toString()));
                gameRecordReport.setValidAmount(new BigDecimal(map.get("validbet").toString()));
                gameRecordReport.setWinLossAmount(new BigDecimal(map.get("win_loss").toString()));
                gameRecordReport.setBettingNumber(Integer.parseInt(map.get("num").toString()));
                gameRecordReport.setUserAmount(new BigDecimal(map.get("user_amount").toString()));
                gameRecordReport.setSurplusAmount(new BigDecimal(map.get("surplus_amount").toString()));
                gameRecordReport.setPlatform(Constants.PLATFORM_OBTY);
                gameRecordReport.setFirstProxy(Long.parseLong(map.get("first_proxy").toString()));
                gameRecordReport.setSecondProxy(Long.parseLong(map.get("second_proxy").toString()));
                gameRecordReport.setThirdProxy(Long.parseLong(map.get("third_proxy").toString()));
                gameRecordReport.setGameRecordReportId(CommonUtil.toHash(gameRecordReport.getStaticsTimes()
                    + gameRecordReport.getThirdProxy() + gameRecordReport.getPlatform()));
                gameRecordReport01Repository.updateKey(gameRecordReport.getGameRecordReportId(),
                    gameRecordReport.getStaticsTimes(), gameRecordReport.getBetAmount(),
                    gameRecordReport.getValidAmount(), gameRecordReport.getWinLossAmount(),
                    gameRecordReport.getAmount(), gameRecordReport.getBettingNumber(), gameRecordReport.getFirstProxy(),
                    gameRecordReport.getSecondProxy(), gameRecordReport.getThirdProxy(), gameRecordReport.getPlatform(),
                    gameRecordReport.getSurplusAmount(), gameRecordReport.getUserAmount());
            }
            first.setOBTYMaxId(max);
            log.info("保存OBTY注单下标{}", first.getOBTYMaxId());
            gameRecordEndIndexService.save(first);
        } catch (Exception ex) {
            log.error("每小时OBTY报表统计失败", ex);
        }
    }

    @Transactional
    public void saveGameRecordReportOBZR() {
        GameRecordEndIndex first = gameRecordEndIndexService.findUGameRecordEndIndexUseLock();
        if (first == null) {
            return;
        }
        log.info("得到OBZR注单下标{}", first.getOBZRMaxId());
        List<Map<String, Object>> reportResult = gameRecordObzrService.queryGameRecords(first.getOBZRMaxId(), 13);
        try {
            if (reportResult == null || reportResult.size() == CommonConst.NUMBER_0) {
                return;
            }
            if (reportResult.get(0).get("num") == null
                || Integer.parseInt(reportResult.get(0).get("num").toString()) == CommonConst.NUMBER_0) {
                return;
            }
            Long max = 0L;
            for (Map<String, Object> map : reportResult) {

                GameRecordReportNew gameRecordReport = new GameRecordReportNew();
                Long maxId = Long.parseLong(map.get("maxId").toString());
                if (maxId > max)
                    max = maxId;

                if (map.get("set_time") == null) {
                    continue;
                }

                gameRecordReport.setStaticsTimes(map.get("set_time").toString());
                gameRecordReport.setAmount(new BigDecimal(map.get("amount").toString()));
                gameRecordReport.setBetAmount(new BigDecimal(map.get("bet").toString()));
                gameRecordReport.setValidAmount(new BigDecimal(map.get("validbet").toString()));
                gameRecordReport.setWinLossAmount(new BigDecimal(map.get("win_loss").toString()));
                gameRecordReport.setBettingNumber(Integer.parseInt(map.get("num").toString()));
                gameRecordReport.setUserAmount(new BigDecimal(map.get("user_amount").toString()));
                gameRecordReport.setSurplusAmount(new BigDecimal(map.get("surplus_amount").toString()));
                gameRecordReport.setPlatform(Constants.PLATFORM_OBZR);
                gameRecordReport.setFirstProxy(Long.parseLong(map.get("first_proxy").toString()));
                gameRecordReport.setSecondProxy(Long.parseLong(map.get("second_proxy").toString()));
                gameRecordReport.setThirdProxy(Long.parseLong(map.get("third_proxy").toString()));
                gameRecordReport.setGameRecordReportId(CommonUtil.toHash(gameRecordReport.getStaticsTimes()
                    + gameRecordReport.getThirdProxy() + gameRecordReport.getPlatform()));
                gameRecordReport01Repository.updateKey(gameRecordReport.getGameRecordReportId(),
                    gameRecordReport.getStaticsTimes(), gameRecordReport.getBetAmount(),
                    gameRecordReport.getValidAmount(), gameRecordReport.getWinLossAmount(),
                    gameRecordReport.getAmount(), gameRecordReport.getBettingNumber(), gameRecordReport.getFirstProxy(),
                    gameRecordReport.getSecondProxy(), gameRecordReport.getThirdProxy(), gameRecordReport.getPlatform(),
                    gameRecordReport.getSurplusAmount(), gameRecordReport.getUserAmount());
            }
            first.setOBZRMaxId(max);
            log.info("保存OBZR注单下标{}", first.getOBZRMaxId());
            gameRecordEndIndexService.save(first);
        } catch (Exception ex) {
            log.error("每小时OBZR报表统计失败", ex);
        }
    }

    @Transactional
    public void saveGameRecordReportSABASPORT() {
        GameRecordEndIndex first = gameRecordEndIndexService.findUGameRecordEndIndexUseLock();
        if (first == null) {
            return;
        }
        log.info("得到SABASPORT注单下标{}", first.getSABASPORTMaxId());
        List<Map<String, Object>> reportResult =
            gameRecordGoldenFService.queryGameRecordsBySb(first.getSABASPORTMaxId(), 13, Constants.PLATFORM_SABASPORT);
        try {
            if (reportResult == null || reportResult.size() == CommonConst.NUMBER_0) {
                return;
            }
            if (reportResult.get(0).get("num") == null
                || Integer.parseInt(reportResult.get(0).get("num").toString()) == CommonConst.NUMBER_0) {
                return;
            }
            Long max = 0L;
            for (Map<String, Object> map : reportResult) {
                GameRecordReportNew gameRecordReport = new GameRecordReportNew();
                Long maxId = Long.parseLong(map.get("maxId").toString());
                if (maxId > max)
                    max = maxId;
                gameRecordReport.setStaticsTimes(map.get("set_time").toString());
                gameRecordReport.setAmount(new BigDecimal(map.get("amount").toString()));
                gameRecordReport.setBetAmount(new BigDecimal(map.get("bet").toString()));
                gameRecordReport.setValidAmount(new BigDecimal(map.get("validbet").toString()));
                gameRecordReport.setWinLossAmount(
                    new BigDecimal(map.get("win_loss").toString()).subtract(gameRecordReport.getBetAmount()));
                gameRecordReport.setBettingNumber(Integer.parseInt(map.get("num").toString()));
                gameRecordReport.setUserAmount(new BigDecimal(map.get("user_amount").toString()));
                gameRecordReport.setSurplusAmount(new BigDecimal(map.get("surplus_amount").toString()));
                gameRecordReport.setPlatform(Constants.PLATFORM_SABASPORT);
                gameRecordReport.setFirstProxy(Long.parseLong(map.get("first_proxy").toString()));
                gameRecordReport.setSecondProxy(Long.parseLong(map.get("second_proxy").toString()));
                gameRecordReport.setThirdProxy(Long.parseLong(map.get("third_proxy").toString()));
                gameRecordReport.setGameRecordReportId(CommonUtil.toHash(gameRecordReport.getStaticsTimes()
                    + gameRecordReport.getThirdProxy() + gameRecordReport.getPlatform()));
                gameRecordReport01Repository.updateKey(gameRecordReport.getGameRecordReportId(),
                    gameRecordReport.getStaticsTimes(), gameRecordReport.getBetAmount(),
                    gameRecordReport.getValidAmount(), gameRecordReport.getWinLossAmount(),
                    gameRecordReport.getAmount(), gameRecordReport.getBettingNumber(), gameRecordReport.getFirstProxy(),
                    gameRecordReport.getSecondProxy(), gameRecordReport.getThirdProxy(), gameRecordReport.getPlatform(),
                    gameRecordReport.getSurplusAmount(), gameRecordReport.getUserAmount());
            }
            first.setSABASPORTMaxId(max);
            log.info("保存SABASPORT注单下标{}", first.getSABASPORTMaxId());
            gameRecordEndIndexService.save(first);
        } catch (Exception ex) {
            log.error("每小时SABASPORT报表统计失败", ex);
        }
    }

    @Transactional
    public void saveGameRecordReportHORSEBOOK() {
        GameRecordEndIndex first = gameRecordEndIndexService.findUGameRecordEndIndexUseLock();
        if (first == null) {
            return;
        }
        log.info("得到HORSEBOOK注单下标{}", first.getHORSEBOOKMaxId());
        List<Map<String, Object>> reportResult =
            gameRecordAeService.queryGameRecords(first.getHORSEBOOKMaxId(), 13, Constants.PLATFORM_AE_HORSEBOOK);
        try {
            if (reportResult == null || reportResult.size() == CommonConst.NUMBER_0) {
                return;
            }
            if (reportResult.get(0).get("num") == null
                || Integer.parseInt(reportResult.get(0).get("num").toString()) == CommonConst.NUMBER_0) {
                return;
            }
            Long max = 0L;
            for (Map<String, Object> map : reportResult) {
                GameRecordReportNew gameRecordReport = new GameRecordReportNew();
                Long maxId = Long.parseLong(map.get("maxId").toString());
                if (maxId > max)
                    max = maxId;
                gameRecordReport.setStaticsTimes(map.get("set_time").toString());
                gameRecordReport.setAmount(new BigDecimal(map.get("amount").toString()));
                gameRecordReport.setBetAmount(new BigDecimal(map.get("bet").toString()));
                gameRecordReport.setValidAmount(new BigDecimal(map.get("validbet").toString()));
                gameRecordReport.setWinLossAmount(new BigDecimal(map.get("win_loss").toString()));
                gameRecordReport.setBettingNumber(Integer.parseInt(map.get("num").toString()));
                gameRecordReport.setUserAmount(new BigDecimal(map.get("user_amount").toString()));
                gameRecordReport.setSurplusAmount(new BigDecimal(map.get("surplus_amount").toString()));
                gameRecordReport.setPlatform(Constants.PLATFORM_AE_HORSEBOOK);
                gameRecordReport.setFirstProxy(Long.parseLong(map.get("first_proxy").toString()));
                gameRecordReport.setSecondProxy(Long.parseLong(map.get("second_proxy").toString()));
                gameRecordReport.setThirdProxy(Long.parseLong(map.get("third_proxy").toString()));
                gameRecordReport.setGameRecordReportId(CommonUtil.toHash(gameRecordReport.getStaticsTimes()
                    + gameRecordReport.getThirdProxy() + gameRecordReport.getPlatform()));
                gameRecordReport01Repository.updateKey(gameRecordReport.getGameRecordReportId(),
                    gameRecordReport.getStaticsTimes(), gameRecordReport.getBetAmount(),
                    gameRecordReport.getValidAmount(), gameRecordReport.getWinLossAmount(),
                    gameRecordReport.getAmount(), gameRecordReport.getBettingNumber(), gameRecordReport.getFirstProxy(),
                    gameRecordReport.getSecondProxy(), gameRecordReport.getThirdProxy(), gameRecordReport.getPlatform(),
                    gameRecordReport.getSurplusAmount(), gameRecordReport.getUserAmount());
            }
            first.setHORSEBOOKMaxId(max);
            log.info("保存HORSEBOOK注单下标{}", first.getHORSEBOOKMaxId());
            gameRecordEndIndexService.save(first);
        } catch (Exception ex) {
            log.error("每小时HORSEBOOK报表统计失败", ex);
        }
    }

    @Transactional
    public void saveGameRecordReportSV388() {
        GameRecordEndIndex first = gameRecordEndIndexService.findUGameRecordEndIndexUseLock();
        if (first == null) {
            return;
        }
        log.info("得到SV388注单下标{}", first.getSV388MaxId());
        List<Map<String, Object>> reportResult =
            gameRecordAeService.queryGameRecords(first.getSV388MaxId(), 13, Constants.PLATFORM_AE_SV388);
        try {
            if (reportResult == null || reportResult.size() == CommonConst.NUMBER_0) {
                return;
            }
            if (reportResult.get(0).get("num") == null
                || Integer.parseInt(reportResult.get(0).get("num").toString()) == CommonConst.NUMBER_0) {
                return;
            }
            Long max = 0L;
            for (Map<String, Object> map : reportResult) {
                GameRecordReportNew gameRecordReport = new GameRecordReportNew();
                Long maxId = Long.parseLong(map.get("maxId").toString());
                if (maxId > max)
                    max = maxId;
                gameRecordReport.setStaticsTimes(map.get("set_time").toString());
                gameRecordReport.setAmount(new BigDecimal(map.get("amount").toString()));
                gameRecordReport.setBetAmount(new BigDecimal(map.get("bet").toString()));
                gameRecordReport.setValidAmount(new BigDecimal(map.get("validbet").toString()));
                gameRecordReport.setWinLossAmount(new BigDecimal(map.get("win_loss").toString()));
                gameRecordReport.setBettingNumber(Integer.parseInt(map.get("num").toString()));
                gameRecordReport.setUserAmount(new BigDecimal(map.get("user_amount").toString()));
                gameRecordReport.setSurplusAmount(new BigDecimal(map.get("surplus_amount").toString()));
                gameRecordReport.setPlatform(Constants.PLATFORM_AE_SV388);
                gameRecordReport.setFirstProxy(Long.parseLong(map.get("first_proxy").toString()));
                gameRecordReport.setSecondProxy(Long.parseLong(map.get("second_proxy").toString()));
                gameRecordReport.setThirdProxy(Long.parseLong(map.get("third_proxy").toString()));
                gameRecordReport.setGameRecordReportId(CommonUtil.toHash(gameRecordReport.getStaticsTimes()
                    + gameRecordReport.getThirdProxy() + gameRecordReport.getPlatform()));
                gameRecordReport01Repository.updateKey(gameRecordReport.getGameRecordReportId(),
                    gameRecordReport.getStaticsTimes(), gameRecordReport.getBetAmount(),
                    gameRecordReport.getValidAmount(), gameRecordReport.getWinLossAmount(),
                    gameRecordReport.getAmount(), gameRecordReport.getBettingNumber(), gameRecordReport.getFirstProxy(),
                    gameRecordReport.getSecondProxy(), gameRecordReport.getThirdProxy(), gameRecordReport.getPlatform(),
                    gameRecordReport.getSurplusAmount(), gameRecordReport.getUserAmount());
            }
            first.setSV388MaxId(max);
            log.info("保存SV388注单下标{}", first.getSV388MaxId());
            gameRecordEndIndexService.save(first);
        } catch (Exception ex) {
            log.error("每小时SV388报表统计失败", ex);
        }
    }

    @Transactional
    public void saveGameRecordReportE1SPORT() {
        GameRecordEndIndex first = gameRecordEndIndexService.findUGameRecordEndIndexUseLock();
        if (first == null) {
            return;
        }
        log.info("得到E1SPORT注单下标{}", first.getE1SPORTMaxId());
        List<Map<String, Object>> reportResult =
            gameRecordAeService.queryGameRecords(first.getE1SPORTMaxId(), 13, Constants.PLATFORM_AE_E1SPORT);
        try {
            if (reportResult == null || reportResult.size() == CommonConst.NUMBER_0) {
                return;
            }
            if (reportResult.get(0).get("num") == null
                || Integer.parseInt(reportResult.get(0).get("num").toString()) == CommonConst.NUMBER_0) {
                return;
            }
            Long max = 0L;
            for (Map<String, Object> map : reportResult) {
                GameRecordReportNew gameRecordReport = new GameRecordReportNew();
                Long maxId = Long.parseLong(map.get("maxId").toString());
                if (maxId > max)
                    max = maxId;
                gameRecordReport.setStaticsTimes(map.get("set_time").toString());
                gameRecordReport.setAmount(new BigDecimal(map.get("amount").toString()));
                gameRecordReport.setBetAmount(new BigDecimal(map.get("bet").toString()));
                gameRecordReport.setValidAmount(new BigDecimal(map.get("validbet").toString()));
                gameRecordReport.setWinLossAmount(new BigDecimal(map.get("win_loss").toString()));
                gameRecordReport.setBettingNumber(Integer.parseInt(map.get("num").toString()));
                gameRecordReport.setUserAmount(new BigDecimal(map.get("user_amount").toString()));
                gameRecordReport.setSurplusAmount(new BigDecimal(map.get("surplus_amount").toString()));
                gameRecordReport.setPlatform(Constants.PLATFORM_AE_E1SPORT);
                gameRecordReport.setFirstProxy(Long.parseLong(map.get("first_proxy").toString()));
                gameRecordReport.setSecondProxy(Long.parseLong(map.get("second_proxy").toString()));
                gameRecordReport.setThirdProxy(Long.parseLong(map.get("third_proxy").toString()));
                gameRecordReport.setGameRecordReportId(CommonUtil.toHash(gameRecordReport.getStaticsTimes()
                    + gameRecordReport.getThirdProxy() + gameRecordReport.getPlatform()));
                gameRecordReport01Repository.updateKey(gameRecordReport.getGameRecordReportId(),
                    gameRecordReport.getStaticsTimes(), gameRecordReport.getBetAmount(),
                    gameRecordReport.getValidAmount(), gameRecordReport.getWinLossAmount(),
                    gameRecordReport.getAmount(), gameRecordReport.getBettingNumber(), gameRecordReport.getFirstProxy(),
                    gameRecordReport.getSecondProxy(), gameRecordReport.getThirdProxy(), gameRecordReport.getPlatform(),
                    gameRecordReport.getSurplusAmount(), gameRecordReport.getUserAmount());
            }
            first.setE1SPORTMaxId(max);
            log.info("保存E1SPORT注单下标{}", first.getE1SPORTMaxId());
            gameRecordEndIndexService.save(first);
        } catch (Exception ex) {
            log.error("每小时E1SPORT报表统计失败", ex);
        }
    }

    @Transactional
    public void saveGameRecordReportAE() {
        GameRecordEndIndex first = gameRecordEndIndexService.findUGameRecordEndIndexUseLock();
        if (first == null) {
            return;
        }
        log.info("得到AE注单下标{}", first.getAEMaxId());
        List<Map<String, Object>> reportResult =
            gameRecordAeService.queryGameRecordsMerge(first.getAEMaxId(), 13, Constants.PLATFORM_AE);
        try {
            if (reportResult == null || reportResult.size() == CommonConst.NUMBER_0) {
                return;
            }
            if (reportResult.get(0).get("num") == null
                || Integer.parseInt(reportResult.get(0).get("num").toString()) == CommonConst.NUMBER_0) {
                return;
            }
            Long max = 0L;
            for (Map<String, Object> map : reportResult) {
                GameRecordReportNew gameRecordReport = new GameRecordReportNew();
                Long maxId = Long.parseLong(map.get("maxId").toString());
                if (maxId > max)
                    max = maxId;
                gameRecordReport.setStaticsTimes(map.get("set_time").toString());
                gameRecordReport.setAmount(new BigDecimal(map.get("amount").toString()));
                gameRecordReport.setBetAmount(new BigDecimal(map.get("bet").toString()));
                gameRecordReport.setValidAmount(new BigDecimal(map.get("validbet").toString()));
                gameRecordReport.setWinLossAmount(new BigDecimal(map.get("win_loss").toString()));
                gameRecordReport.setBettingNumber(Integer.parseInt(map.get("num").toString()));
                gameRecordReport.setUserAmount(new BigDecimal(map.get("user_amount").toString()));
                gameRecordReport.setSurplusAmount(new BigDecimal(map.get("surplus_amount").toString()));
                gameRecordReport.setPlatform(Constants.PLATFORM_AE);
                gameRecordReport.setFirstProxy(Long.parseLong(map.get("first_proxy").toString()));
                gameRecordReport.setSecondProxy(Long.parseLong(map.get("second_proxy").toString()));
                gameRecordReport.setThirdProxy(Long.parseLong(map.get("third_proxy").toString()));
                gameRecordReport.setGameRecordReportId(CommonUtil.toHash(gameRecordReport.getStaticsTimes()
                    + gameRecordReport.getThirdProxy() + gameRecordReport.getPlatform()));
                gameRecordReport01Repository.updateKey(gameRecordReport.getGameRecordReportId(),
                    gameRecordReport.getStaticsTimes(), gameRecordReport.getBetAmount(),
                    gameRecordReport.getValidAmount(), gameRecordReport.getWinLossAmount(),
                    gameRecordReport.getAmount(), gameRecordReport.getBettingNumber(), gameRecordReport.getFirstProxy(),
                    gameRecordReport.getSecondProxy(), gameRecordReport.getThirdProxy(), gameRecordReport.getPlatform(),
                    gameRecordReport.getSurplusAmount(), gameRecordReport.getUserAmount());
            }
            first.setAEMaxId(max);
            log.info("保存AE注单下标{}", first.getAEMaxId());
            gameRecordEndIndexService.save(first);
        } catch (Exception ex) {
            log.error("每小时AE报表统计失败", ex);
        }
    }

    @Transactional
    public void saveGameRecordReportVNC() {
        GameRecordEndIndex first = gameRecordEndIndexService.findUGameRecordEndIndexUseLock();
        if (first == null) {
            return;
        }
        log.info("得到VNC注单下标{}", first.getVNCMaxId());
        List<Map<String, Object>> reportResult =
            rptBetInfoDetailService.queryGameRecords(first.getVNCMaxId(), 13, Constants.PLATFORM_VNC);
        try {
            if (reportResult == null || reportResult.size() == CommonConst.NUMBER_0) {
                return;
            }
            if (reportResult.get(0).get("num") == null
                || Integer.parseInt(reportResult.get(0).get("num").toString()) == CommonConst.NUMBER_0) {
                return;
            }
            Long max = 0L;
            for (Map<String, Object> map : reportResult) {
                GameRecordReportNew gameRecordReport = new GameRecordReportNew();
                Long maxId = Long.parseLong(map.get("maxId").toString());
                if (maxId > max)
                    max = maxId;
                gameRecordReport.setStaticsTimes(map.get("set_time").toString());
                gameRecordReport.setAmount(new BigDecimal(map.get("amount").toString()));
                gameRecordReport.setBetAmount(new BigDecimal(map.get("bet").toString()));
                gameRecordReport.setValidAmount(new BigDecimal(map.get("validbet").toString()));
                gameRecordReport.setWinLossAmount(new BigDecimal(map.get("win_loss").toString()));
                gameRecordReport.setBettingNumber(Integer.parseInt(map.get("num").toString()));
                gameRecordReport.setUserAmount(new BigDecimal(map.get("user_amount").toString()));
                gameRecordReport.setSurplusAmount(new BigDecimal(map.get("surplus_amount").toString()));
                gameRecordReport.setPlatform(Constants.PLATFORM_VNC);
                gameRecordReport.setFirstProxy(Long.parseLong(map.get("first_proxy").toString()));
                gameRecordReport.setSecondProxy(Long.parseLong(map.get("second_proxy").toString()));
                gameRecordReport.setThirdProxy(Long.parseLong(map.get("third_proxy").toString()));
                gameRecordReport.setGameRecordReportId(CommonUtil.toHash(gameRecordReport.getStaticsTimes()
                    + gameRecordReport.getThirdProxy() + gameRecordReport.getPlatform()));
                gameRecordReport01Repository.updateKey(gameRecordReport.getGameRecordReportId(),
                    gameRecordReport.getStaticsTimes(), gameRecordReport.getBetAmount(),
                    gameRecordReport.getValidAmount(), gameRecordReport.getWinLossAmount(),
                    gameRecordReport.getAmount(), gameRecordReport.getBettingNumber(), gameRecordReport.getFirstProxy(),
                    gameRecordReport.getSecondProxy(), gameRecordReport.getThirdProxy(), gameRecordReport.getPlatform(),
                    gameRecordReport.getSurplusAmount(), gameRecordReport.getUserAmount());
            }
            first.setVNCMaxId(max);
            log.info("保存VNC注单下标{}", first.getVNCMaxId());
            gameRecordEndIndexService.save(first);
        } catch (Exception ex) {
            log.error("每小时VNC报表统计失败", ex);
        }
    }

    @Transactional
    public void saveGameRecordReportDmc() {
        GameRecordEndIndex first = gameRecordEndIndexService.findUGameRecordEndIndexUseLock();
        if (first == null) {
            return;
        }
        log.info("得到DMC注单下标{}", first.getDMCMaxId());
        List<Map<String, Object>> reportResult =
            gameRecordDMCService.queryGameRecords(first.getDMCMaxId(), 13, Constants.PLATFORM_DMC);
        try {
            if (reportResult == null || reportResult.size() == CommonConst.NUMBER_0) {
                return;
            }
            if (reportResult.get(0).get("num") == null
                || Integer.parseInt(reportResult.get(0).get("num") + "") == CommonConst.NUMBER_0) {
                return;
            }
            Long max = 0L;
            for (Map<String, Object> map : reportResult) {
                GameRecordReportNew gameRecordReport = new GameRecordReportNew();
                Long maxId = Long.parseLong(map.get("maxId").toString());
                if (maxId > max)
                    max = maxId;
                gameRecordReport.setStaticsTimes(map.get("set_time").toString());
                gameRecordReport.setAmount(new BigDecimal(map.get("amount").toString()));
                gameRecordReport.setBetAmount(new BigDecimal(map.get("bet").toString()));
                gameRecordReport.setValidAmount(new BigDecimal(map.get("validbet").toString()));
                gameRecordReport.setWinLossAmount(new BigDecimal(map.get("win_loss").toString()));
                gameRecordReport.setBettingNumber(Integer.parseInt(map.get("num").toString()));
                gameRecordReport.setUserAmount(new BigDecimal(map.get("user_amount").toString()));
                gameRecordReport.setSurplusAmount(new BigDecimal(map.get("surplus_amount").toString()));
                gameRecordReport.setPlatform(Constants.PLATFORM_DMC);
                gameRecordReport.setFirstProxy(Long.parseLong(map.get("first_proxy").toString()));
                gameRecordReport.setSecondProxy(Long.parseLong(map.get("second_proxy").toString()));
                gameRecordReport.setThirdProxy(Long.parseLong(map.get("third_proxy").toString()));
                gameRecordReport.setGameRecordReportId(CommonUtil.toHash(gameRecordReport.getStaticsTimes()
                    + gameRecordReport.getThirdProxy() + gameRecordReport.getPlatform()));
                gameRecordReport01Repository.updateKey(gameRecordReport.getGameRecordReportId(),
                    gameRecordReport.getStaticsTimes(), gameRecordReport.getBetAmount(),
                    gameRecordReport.getValidAmount(), gameRecordReport.getWinLossAmount(),
                    gameRecordReport.getAmount(), gameRecordReport.getBettingNumber(), gameRecordReport.getFirstProxy(),
                    gameRecordReport.getSecondProxy(), gameRecordReport.getThirdProxy(), gameRecordReport.getPlatform(),
                    gameRecordReport.getSurplusAmount(), gameRecordReport.getUserAmount());
            }
            first.setDMCMaxId(max);
            log.info("保存DMC注单下标{}", first.getDMCMaxId());
            gameRecordEndIndexService.save(first);
        } catch (Exception ex) {
            log.error("每小时DMC报表统计失败", ex);
        }
    }

    @Transactional
    public void saveGameRecordReportDG() {
        GameRecordEndIndex first = gameRecordEndIndexService.findUGameRecordEndIndexUseLock();
        if (first == null) {
            return;
        }
        log.info("得到DG注单下标{}", first.getDGMaxId());
        List<Map<String, Object>> reportResult = gameRecordDGService.queryGameRecords(first.getDGMaxId(), 13);
        try {
            if (reportResult == null || reportResult.size() == CommonConst.NUMBER_0) {
                return;
            }
            if (reportResult.get(0).get("num") == null
                || Integer.parseInt(reportResult.get(0).get("num").toString()) == CommonConst.NUMBER_0) {
                return;
            }
            Long max = 0L;
            for (Map<String, Object> map : reportResult) {
                GameRecordReportNew gameRecordReport = new GameRecordReportNew();
                Long maxId = Long.parseLong(map.get("maxId").toString());
                if (maxId > max)
                    max = maxId;
                gameRecordReport.setStaticsTimes(map.get("set_time").toString());
                gameRecordReport.setAmount(new BigDecimal(map.get("amount").toString()));
                gameRecordReport.setBetAmount(new BigDecimal(map.get("bet").toString()));
                gameRecordReport.setValidAmount(new BigDecimal(map.get("validbet").toString()));
                gameRecordReport.setWinLossAmount(new BigDecimal(map.get("win_loss").toString()));
                gameRecordReport.setBettingNumber(Integer.parseInt(map.get("num").toString()));
                gameRecordReport.setUserAmount(new BigDecimal(map.get("user_amount").toString()));
                gameRecordReport.setSurplusAmount(new BigDecimal(map.get("surplus_amount").toString()));
                gameRecordReport.setPlatform(Constants.PLATFORM_DG);
                gameRecordReport.setFirstProxy(Long.parseLong(map.get("first_proxy").toString()));
                gameRecordReport.setSecondProxy(Long.parseLong(map.get("second_proxy").toString()));
                gameRecordReport.setThirdProxy(Long.parseLong(map.get("third_proxy").toString()));
                gameRecordReport.setGameRecordReportId(CommonUtil.toHash(gameRecordReport.getStaticsTimes()
                    + gameRecordReport.getThirdProxy() + gameRecordReport.getPlatform()));
                gameRecordReport01Repository.updateKey(gameRecordReport.getGameRecordReportId(),
                    gameRecordReport.getStaticsTimes(), gameRecordReport.getBetAmount(),
                    gameRecordReport.getValidAmount(), gameRecordReport.getWinLossAmount(),
                    gameRecordReport.getAmount(), gameRecordReport.getBettingNumber(), gameRecordReport.getFirstProxy(),
                    gameRecordReport.getSecondProxy(), gameRecordReport.getThirdProxy(), gameRecordReport.getPlatform(),
                    gameRecordReport.getSurplusAmount(), gameRecordReport.getUserAmount());
            }
            first.setDGMaxId(max);
            log.info("保存DG注单下标{}", first.getDGMaxId());
            gameRecordEndIndexService.save(first);
        } catch (Exception ex) {
            log.error("每小时DG报表统计失败", ex);
        }
    }

    /**
     * ’ 分组分页
     */
    public Page<GameRecordReportNew> findGameRecordReportPage(Pageable page, GameRecordReportNew gameRecordReport,
        String startSetTime, String endSetTime, Long proxyId, Integer proxyRole, Boolean agentMark, Integer agentId) {
        // criteriaBuilder用于构建CriteriaQuery的构建器对象
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        // criteriaQuery包含查询语句的各个部分，如where、max、sum、groupBy、orderBy等
        CriteriaQuery<GameRecordReportNew> criteriaQuery = criteriaBuilder.createQuery(GameRecordReportNew.class);
        // 获取查询实例的属性，select * from books
        Root<GameRecordReportNew> root = criteriaQuery.from(GameRecordReportNew.class);
        List<Predicate> predicates = new ArrayList();
        // 相当于select type,max(price) maxPrice,sum(price) sumPrice from books中select 与 from之间的部分

        if (!com.qianyi.modulecommon.util.CommonUtil.checkNull(gameRecordReport.getPlatform())) {
            predicates
                .add(criteriaBuilder.equal(root.get("platform").as(String.class), gameRecordReport.getPlatform()));
        }
        if (!ObjectUtils.isEmpty(startSetTime) && !ObjectUtils.isEmpty(endSetTime)) {
            predicates
                .add(criteriaBuilder.between(root.get("staticsTimes").as(String.class), startSetTime, endSetTime));
        }
        if (proxyRole == null) {
            criteriaQuery.multiselect(root.get("firstProxy"), criteriaBuilder.sum(root.get("bettingNumber")),
                criteriaBuilder.sum(root.get("amount")), criteriaBuilder.sum(root.get("betAmount")),
                criteriaBuilder.sum(root.get("validAmount")), criteriaBuilder.sum(root.get("winLossAmount")),
                criteriaBuilder.sum(root.get("userAmount")), criteriaBuilder.sum(root.get("surplusAmount")),
                criteriaBuilder.sum(root.get("newAmount")), criteriaBuilder.sum(root.get("newUserAmount")),
                criteriaBuilder.sum(root.get("newSurplusAmount")),
                criteriaBuilder.sum(root.get("todayAward")),
                criteriaBuilder.sum(root.get("riseAward")));
            predicates.add(criteriaBuilder.notEqual(root.get("firstProxy").as(Long.class), 0L));
            // group by type
            criteriaQuery.groupBy(root.get("firstProxy"));
        } else if (proxyRole == CommonConst.NUMBER_1) {
            if (agentMark) {
                criteriaQuery.multiselect(root.get("secondProxy"), criteriaBuilder.sum(root.get("bettingNumber")),
                    criteriaBuilder.sum(root.get("amount")), criteriaBuilder.sum(root.get("betAmount")),
                    criteriaBuilder.sum(root.get("validAmount")), criteriaBuilder.sum(root.get("winLossAmount")),
                    criteriaBuilder.sum(root.get("userAmount")), criteriaBuilder.sum(root.get("surplusAmount")),
                    criteriaBuilder.sum(root.get("newAmount")), criteriaBuilder.sum(root.get("newUserAmount")),
                    criteriaBuilder.sum(root.get("newSurplusAmount")),
                    criteriaBuilder.sum(root.get("todayAward")),
                    criteriaBuilder.sum(root.get("riseAward")));
                predicates.add(criteriaBuilder.equal(root.get("firstProxy").as(Long.class), proxyId));
                criteriaQuery.groupBy(root.get("secondProxy"));
            } else {
                criteriaQuery.multiselect(root.get("firstProxy"), criteriaBuilder.sum(root.get("bettingNumber")),
                    criteriaBuilder.sum(root.get("amount")), criteriaBuilder.sum(root.get("betAmount")),
                    criteriaBuilder.sum(root.get("validAmount")), criteriaBuilder.sum(root.get("winLossAmount")),
                    criteriaBuilder.sum(root.get("userAmount")), criteriaBuilder.sum(root.get("surplusAmount")),
                    criteriaBuilder.sum(root.get("newAmount")), criteriaBuilder.sum(root.get("newUserAmount")),
                    criteriaBuilder.sum(root.get("newSurplusAmount")),
                    criteriaBuilder.sum(root.get("todayAward")),
                    criteriaBuilder.sum(root.get("riseAward")));
                predicates.add(criteriaBuilder.equal(root.get("firstProxy").as(Long.class), proxyId));
                predicates.add(criteriaBuilder.notEqual(root.get("firstProxy").as(Long.class), 0L));
                // group by type
                criteriaQuery.groupBy(root.get("firstProxy"));
            }
        } else if (proxyRole == CommonConst.NUMBER_2) {
            if (agentMark) {
                criteriaQuery.multiselect(root.get("thirdProxy"), criteriaBuilder.sum(root.get("bettingNumber")),
                    criteriaBuilder.sum(root.get("amount")), criteriaBuilder.sum(root.get("betAmount")),
                    criteriaBuilder.sum(root.get("validAmount")), criteriaBuilder.sum(root.get("winLossAmount")),
                    criteriaBuilder.sum(root.get("userAmount")), criteriaBuilder.sum(root.get("surplusAmount")),
                    criteriaBuilder.sum(root.get("newAmount")), criteriaBuilder.sum(root.get("newUserAmount")),
                    criteriaBuilder.sum(root.get("newSurplusAmount")),
                    criteriaBuilder.sum(root.get("todayAward")),
                    criteriaBuilder.sum(root.get("riseAward")));
                predicates.add(criteriaBuilder.equal(root.get("secondProxy").as(Long.class), proxyId));
                predicates.add(criteriaBuilder.equal(root.get("firstProxy").as(Long.class), agentId));
                criteriaQuery.groupBy(root.get("thirdProxy"));
            } else {
                criteriaQuery.multiselect(root.get("secondProxy"), criteriaBuilder.sum(root.get("bettingNumber")),
                    criteriaBuilder.sum(root.get("amount")), criteriaBuilder.sum(root.get("betAmount")),
                    criteriaBuilder.sum(root.get("validAmount")), criteriaBuilder.sum(root.get("winLossAmount")),
                    criteriaBuilder.sum(root.get("userAmount")), criteriaBuilder.sum(root.get("surplusAmount")),
                    criteriaBuilder.sum(root.get("newAmount")), criteriaBuilder.sum(root.get("newUserAmount")),
                    criteriaBuilder.sum(root.get("newSurplusAmount")),
                    criteriaBuilder.sum(root.get("todayAward")),
                    criteriaBuilder.sum(root.get("riseAward")));
                predicates.add(criteriaBuilder.equal(root.get("secondProxy").as(Long.class), proxyId));
                predicates.add(criteriaBuilder.notEqual(root.get("firstProxy").as(Long.class), 0L));
                // group by type
                criteriaQuery.groupBy(root.get("secondProxy"));
            }
        } else {
            criteriaQuery.multiselect(root.get("thirdProxy"), criteriaBuilder.sum(root.get("bettingNumber")),
                criteriaBuilder.sum(root.get("amount")), criteriaBuilder.sum(root.get("betAmount")),
                criteriaBuilder.sum(root.get("validAmount")), criteriaBuilder.sum(root.get("winLossAmount")),
                criteriaBuilder.sum(root.get("userAmount")), criteriaBuilder.sum(root.get("surplusAmount")),
                criteriaBuilder.sum(root.get("newAmount")), criteriaBuilder.sum(root.get("newUserAmount")),
                criteriaBuilder.sum(root.get("newSurplusAmount")),
                criteriaBuilder.sum(root.get("todayAward")),
                criteriaBuilder.sum(root.get("riseAward")));
            predicates.add(criteriaBuilder.equal(root.get("thirdProxy").as(Long.class), proxyId));
            predicates.add(criteriaBuilder.notEqual(root.get("firstProxy").as(Long.class), 0L));
            // group by type
            criteriaQuery.groupBy(root.get("thirdProxy"));
        }
        criteriaQuery.where(predicates.toArray(new Predicate[predicates.size()]));
        // criteriaQuery拼成的sql是select type,max(price) maxPrice,sum(price) sumPrice from books group by
        // type；查询出的列与对象BookInfo的属性对应
        // 记录当前sql查询结果总条数
        List<GameRecordReportNew> counts = entityManager.createQuery(criteriaQuery).getResultList();
        // sql查询对象
        TypedQuery<GameRecordReportNew> createQuery = entityManager.createQuery(criteriaQuery);
        // 设置分页参数
        createQuery.setFirstResult(page.getPageNumber() * page.getPageSize());
        createQuery.setMaxResults(page.getPageSize());
        // 返回查询的分页结果，createQuery.getResultList()为分页查询的结果对象，counts.size()为设置分页参数之前查询的总数
        return new PageImpl<GameRecordReportNew>(createQuery.getResultList(), page, counts.size());
    }

    public GameRecordReportNew findRecordRecordSum(GameRecordReportNew gameRecordReport, String startSetTime,
        String endSetTime, Long proxyId, Integer proxyRole) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<GameRecordReportNew> query = builder.createQuery(GameRecordReportNew.class);
        Root<GameRecordReportNew> root = query.from(GameRecordReportNew.class);

        query.multiselect(builder.sum(root.get("bettingNumber").as(Integer.class)).alias("bettingNumber"),
            builder.sum(root.get("amount").as(BigDecimal.class)).alias("amount"),
            builder.sum(root.get("betAmount").as(BigDecimal.class)).alias("betAmount"),
            builder.sum(root.get("validAmount").as(BigDecimal.class)).alias("validAmount"),
            builder.sum(root.get("winLossAmount").as(BigDecimal.class)).alias("winLossAmount"),
            builder.sum(root.get("userAmount").as(BigDecimal.class)).alias("userAmount"),
            builder.sum(root.get("surplusAmount").as(BigDecimal.class)).alias("surplusAmount"),
            builder.sum(root.get("newAmount").as(BigDecimal.class)).alias("newAmount"),
            builder.sum(root.get("newUserAmount").as(BigDecimal.class)).alias("newUserAmount"),
            builder.sum(root.get("newSurplusAmount").as(BigDecimal.class)).alias("newSurplusAmount"),
            builder.sum(root.get("todayAward").as(BigDecimal.class)).alias("todayAward"),
            builder.sum(root.get("riseAward").as(BigDecimal.class)).alias("riseAward"));

        List<Predicate> predicates = new ArrayList();
        if (proxyRole != null && proxyRole == CommonConst.NUMBER_1) {
            predicates.add(builder.equal(root.get("firstProxy").as(Long.class), proxyId));
        } else if (proxyRole != null && proxyRole == CommonConst.NUMBER_2) {
            predicates.add(builder.equal(root.get("secondProxy").as(Long.class), proxyId));
        } else if (proxyRole != null && proxyRole == CommonConst.NUMBER_3) {
            predicates.add(builder.equal(root.get("thirdProxy").as(Long.class), proxyId));
        }

        if (!com.qianyi.modulecommon.util.CommonUtil.checkNull(gameRecordReport.getPlatform())) {
            predicates.add(builder.equal(root.get("platform").as(String.class), gameRecordReport.getPlatform()));
        }
        if (!ObjectUtils.isEmpty(startSetTime) && !ObjectUtils.isEmpty(endSetTime)) {
            predicates.add(builder.between(root.get("staticsTimes").as(String.class), startSetTime, endSetTime));
        }
        predicates.add(builder.notEqual(root.get("firstProxy").as(Long.class), 0L));
        query.where(predicates.toArray(new Predicate[predicates.size()]));
        GameRecordReportNew singleResult = entityManager.createQuery(query).getSingleResult();
        return singleResult;
    }

    public GameRecordReportNew findRecordRecordSum(GameRecordReportNew gameRecordReport, String startSetTime,
        String endSetTime) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<GameRecordReportNew> query = builder.createQuery(GameRecordReportNew.class);
        Root<GameRecordReportNew> root = query.from(GameRecordReportNew.class);

        query.multiselect(builder.sum(root.get("bettingNumber").as(Integer.class)).alias("bettingNumber"),
            builder.sum(root.get("amount").as(BigDecimal.class)).alias("amount"),
            builder.sum(root.get("betAmount").as(BigDecimal.class)).alias("betAmount"),
            builder.sum(root.get("validAmount").as(BigDecimal.class)).alias("validAmount"),
            builder.sum(root.get("winLossAmount").as(BigDecimal.class)).alias("winLossAmount"),
            builder.sum(root.get("userAmount").as(BigDecimal.class)).alias("userAmount"),
            builder.sum(root.get("surplusAmount").as(BigDecimal.class)).alias("surplusAmount"),
            builder.sum(root.get("newAmount").as(BigDecimal.class)).alias("newAmount"),
            builder.sum(root.get("newUserAmount").as(BigDecimal.class)).alias("newUserAmount"),
            builder.sum(root.get("newSurplusAmount").as(BigDecimal.class)).alias("newSurplusAmount"),
            builder.sum(root.get("todayAward").as(BigDecimal.class)).alias("todayAward"),
            builder.sum(root.get("riseAward").as(BigDecimal.class)).alias("riseAward"));

        List<Predicate> predicates = new ArrayList();
        if (!com.qianyi.modulecommon.util.CommonUtil.checkNull(gameRecordReport.getPlatform())) {
            predicates.add(builder.equal(root.get("platform").as(String.class), gameRecordReport.getPlatform()));
        }
        if (!ObjectUtils.isEmpty(startSetTime) && !ObjectUtils.isEmpty(endSetTime)) {
            predicates.add(builder.between(root.get("staticsTimes").as(String.class), startSetTime, endSetTime));
        }
        predicates.add(builder.equal(root.get("firstProxy").as(Long.class), 0L));
        query.where(predicates.toArray(new Predicate[predicates.size()]));
        GameRecordReportNew singleResult = entityManager.createQuery(query).getSingleResult();
        return singleResult;
    }

    @Transactional
    public void deleteByPlatform(String platform) {
        gameRecordReport01Repository.deleteByPlatform(platform);
    }

    @Transactional
    public void deleteDataByPlatform(String platform) {
        gameRecordReport01Repository.deleteDataByPlatform(platform);
    }

    public GameRecordReportNew findGameRecordReportNewSum(GameRecordReportNew game, String startBetTime,
        String endBetTime) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<GameRecordReportNew> query = builder.createQuery(GameRecordReportNew.class);
        Root<GameRecordReportNew> root = query.from(GameRecordReportNew.class);

        query.multiselect(builder.sum(root.get("newAmount").as(BigDecimal.class)).alias("newAmount"));

        List<Predicate> predicates = new ArrayList();
        if (game.getFirstProxy() != null) {
            predicates.add(builder.equal(root.get("firstProxy").as(Long.class), game.getFirstProxy()));
        }
        if (game.getSecondProxy() != null) {
            predicates.add(builder.equal(root.get("secondProxy").as(Long.class), game.getSecondProxy()));
        }
        if (game.getThirdProxy() != null) {
            predicates.add(builder.equal(root.get("thirdProxy").as(Long.class), game.getThirdProxy()));
        }
        if (!ObjectUtils.isEmpty(startBetTime) && !ObjectUtils.isEmpty(endBetTime)) {
            predicates.add(builder.between(root.get("staticsTimes").as(String.class), startBetTime, endBetTime));
        }
        query.where(predicates.toArray(new Predicate[predicates.size()]));
        GameRecordReportNew singleResult = entityManager.createQuery(query).getSingleResult();
        return singleResult;
    }

    @Transactional
    public void statisticsWashCode(String platform, String search, String staticsTimes, String startTime,
        String endTime) {
        try {
            log.info("开始统计代理洗码报表platform:{} staticsTimes:{} startTime:{} endTime:{}", platform, staticsTimes, startTime,
                endTime);
            List<Map<String, Object>> washCodeChangeList =
                washCodeChangeService.getMapSumAmount(search, startTime, endTime);
            if (CollUtil.isNotEmpty(washCodeChangeList)) {
                washCodeChangeList.forEach(map -> {
                    BigDecimal newAmount = new BigDecimal(map.get("amount").toString());
                    Long firstProxy = Long.parseLong(map.get("first_proxy").toString());
                    Long secondProxy = Long.parseLong(map.get("second_proxy").toString());
                    Long thirdProxy = Long.parseLong(map.get("third_proxy").toString());
                    Long gameRecordReportId =
                        CommonUtil.toHash("statisticsWashCode" + staticsTimes + thirdProxy.toString() + platform);
                    gameRecordReport01Repository.updateKeyWashCode(gameRecordReportId, staticsTimes, firstProxy,
                        secondProxy, thirdProxy, platform, newAmount);

                });
            }

            List<Map<String, Object>> rebateDetailList =
                rebateDetailService.getMapSumAmount(search, startTime, endTime);
            if (CollUtil.isNotEmpty(rebateDetailList)) {
                rebateDetailList.forEach(map -> {
                    BigDecimal newUserAmount = new BigDecimal(map.get("user_amount").toString());
                    BigDecimal newSurplusAmount = new BigDecimal(map.get("surplus_amount").toString());
                    Long firstProxy = Long.parseLong(map.get("first_proxy").toString());
                    Long secondProxy = Long.parseLong(map.get("second_proxy").toString());
                    Long thirdProxy = Long.parseLong(map.get("third_proxy").toString());
                    Long gameRecordReportId =
                        CommonUtil.toHash("statisticsWashCode" + staticsTimes + thirdProxy.toString() + platform);
                    gameRecordReport01Repository.updateKeyRebate(gameRecordReportId, staticsTimes, firstProxy,
                        secondProxy, thirdProxy, platform, newSurplusAmount, newUserAmount);
                });
            }
            log.info("统计代理洗码报表结束platform:{} staticsTimes:{} startTime:{} endTime:{}", platform, staticsTimes, startTime,
                endTime);
        } catch (Exception ex) {
            log.error("统计代理洗码报表失败platform:{} staticsTimes:{} startTime:{} endTime:{}", platform, staticsTimes,
                startTime, endTime);
        }

    }

    @Transactional
    public void statisticsAward(String staticsTimes, String startTime, String endTime) {
        try {
            log.info("开始统计代理VIP报表staticsTimes:{} startTime:{} endTime:{}", staticsTimes, startTime, endTime);
            List<Map<String, Object>> todayAwards = awardReceiveRecordService.getMapSumTodayAward(startTime, endTime);
            if (CollUtil.isNotEmpty(todayAwards)) {
                todayAwards.forEach(map -> {
                    BigDecimal amount = new BigDecimal(map.get("amount").toString());
                    Long firstProxy = Long.parseLong(map.get("first_proxy").toString());
                    Long secondProxy = Long.parseLong(map.get("second_proxy").toString());
                    Long thirdProxy = Long.parseLong(map.get("third_proxy").toString());
                    Long gameRecordReportId =
                        CommonUtil.toHash("statisticsTodayAward" + staticsTimes + thirdProxy.toString() + "VIP");
                    gameRecordReport01Repository.updateKeyTodayAward(gameRecordReportId, staticsTimes, firstProxy,
                        secondProxy, thirdProxy, "VIP", amount);

                });
            }

            List<Map<String, Object>> riseAwards = awardReceiveRecordService.getMapSumRiseAward(startTime, endTime);
            if (CollUtil.isNotEmpty(riseAwards)) {
                riseAwards.forEach(map -> {
                    BigDecimal amount = new BigDecimal(map.get("amount").toString());
                    Long firstProxy = Long.parseLong(map.get("first_proxy").toString());
                    Long secondProxy = Long.parseLong(map.get("second_proxy").toString());
                    Long thirdProxy = Long.parseLong(map.get("third_proxy").toString());
                    Long gameRecordReportId =
                        CommonUtil.toHash("statisticsRiseAward" + staticsTimes + thirdProxy.toString() + "VIP");
                    gameRecordReport01Repository.updateKeyRiseAward(gameRecordReportId, staticsTimes, firstProxy,
                        secondProxy, thirdProxy, "VIP", amount);
                });
            }
            log.info("开始统计代理VIP报表结束staticsTimes:{} startTime:{} endTime:{}", staticsTimes, startTime, endTime);
        } catch (Exception ex) {
            log.error("统计代理VIP报表失败staticsTimes:{} startTime:{} endTime:{}", staticsTimes, startTime, endTime);
        }

    }



    /**
     * ’ 代理分组分页
     */
    public Page<GameRecordReportNew> findGameRecordReportPageProxy(Pageable page, GameRecordReportNew gameRecordReport,
                                                              String startSetTime, String endSetTime, Long proxyId, Integer proxyRole, Boolean agentMark, Integer agentId, Integer currentRole,Long currentId) {
        // criteriaBuilder用于构建CriteriaQuery的构建器对象
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        // criteriaQuery包含查询语句的各个部分，如where、max、sum、groupBy、orderBy等
        CriteriaQuery<GameRecordReportNew> criteriaQuery = criteriaBuilder.createQuery(GameRecordReportNew.class);
        // 获取查询实例的属性，select * from books
        Root<GameRecordReportNew> root = criteriaQuery.from(GameRecordReportNew.class);
        List<Predicate> predicates = new ArrayList();
        // 相当于select type,max(price) maxPrice,sum(price) sumPrice from books中select 与 from之间的部分

        if (!com.qianyi.modulecommon.util.CommonUtil.checkNull(gameRecordReport.getPlatform())) {
            predicates
                    .add(criteriaBuilder.equal(root.get("platform").as(String.class), gameRecordReport.getPlatform()));
        }
        if (!ObjectUtils.isEmpty(startSetTime) && !ObjectUtils.isEmpty(endSetTime)) {
            predicates
                    .add(criteriaBuilder.between(root.get("staticsTimes").as(String.class), startSetTime, endSetTime));
        }
        if (proxyRole == null) {
            if (currentRole!=null){
                //总代
                if (currentRole.equals( CommonConst.NUMBER_1)){
                    criteriaQuery.multiselect(root.get("firstProxy"), criteriaBuilder.sum(root.get("bettingNumber")),
                            criteriaBuilder.sum(root.get("amount")), criteriaBuilder.sum(root.get("betAmount")),
                            criteriaBuilder.sum(root.get("validAmount")), criteriaBuilder.sum(root.get("winLossAmount")),
                            criteriaBuilder.sum(root.get("userAmount")), criteriaBuilder.sum(root.get("surplusAmount")),
                            criteriaBuilder.sum(root.get("newAmount")), criteriaBuilder.sum(root.get("newUserAmount")),
                            criteriaBuilder.sum(root.get("newSurplusAmount")),
                            criteriaBuilder.sum(root.get("todayAward")),
                            criteriaBuilder.sum(root.get("riseAward")));
                    predicates.add(criteriaBuilder.notEqual(root.get("firstProxy").as(Long.class), 0L));
                    predicates.add(criteriaBuilder.equal(root.get("firstProxy").as(Long.class), currentId));
                    // group by type
                    criteriaQuery.groupBy(root.get("firstProxy"));
                }
                //区域代
                else  if (currentRole.equals( CommonConst.NUMBER_2)){
                    criteriaQuery.multiselect(root.get("secondProxy"), criteriaBuilder.sum(root.get("bettingNumber")),
                            criteriaBuilder.sum(root.get("amount")), criteriaBuilder.sum(root.get("betAmount")),
                            criteriaBuilder.sum(root.get("validAmount")), criteriaBuilder.sum(root.get("winLossAmount")),
                            criteriaBuilder.sum(root.get("userAmount")), criteriaBuilder.sum(root.get("surplusAmount")),
                            criteriaBuilder.sum(root.get("newAmount")), criteriaBuilder.sum(root.get("newUserAmount")),
                            criteriaBuilder.sum(root.get("newSurplusAmount")),
                            criteriaBuilder.sum(root.get("todayAward")),
                            criteriaBuilder.sum(root.get("riseAward")));
                   predicates.add(criteriaBuilder.equal(root.get("secondProxy").as(Long.class), currentId));
                    // group by type
                    criteriaQuery.groupBy(root.get("secondProxy"));

                }
            }

        } else if (proxyRole == CommonConst.NUMBER_1) {
            if (agentMark) {
                criteriaQuery.multiselect(root.get("secondProxy"), criteriaBuilder.sum(root.get("bettingNumber")),
                        criteriaBuilder.sum(root.get("amount")), criteriaBuilder.sum(root.get("betAmount")),
                        criteriaBuilder.sum(root.get("validAmount")), criteriaBuilder.sum(root.get("winLossAmount")),
                        criteriaBuilder.sum(root.get("userAmount")), criteriaBuilder.sum(root.get("surplusAmount")),
                        criteriaBuilder.sum(root.get("newAmount")), criteriaBuilder.sum(root.get("newUserAmount")),
                        criteriaBuilder.sum(root.get("newSurplusAmount")),
                        criteriaBuilder.sum(root.get("todayAward")),
                        criteriaBuilder.sum(root.get("riseAward")));
                predicates.add(criteriaBuilder.equal(root.get("firstProxy").as(Long.class), proxyId));
                criteriaQuery.groupBy(root.get("secondProxy"));
            } else {
                criteriaQuery.multiselect(root.get("firstProxy"), criteriaBuilder.sum(root.get("bettingNumber")),
                        criteriaBuilder.sum(root.get("amount")), criteriaBuilder.sum(root.get("betAmount")),
                        criteriaBuilder.sum(root.get("validAmount")), criteriaBuilder.sum(root.get("winLossAmount")),
                        criteriaBuilder.sum(root.get("userAmount")), criteriaBuilder.sum(root.get("surplusAmount")),
                        criteriaBuilder.sum(root.get("newAmount")), criteriaBuilder.sum(root.get("newUserAmount")),
                        criteriaBuilder.sum(root.get("newSurplusAmount")),
                        criteriaBuilder.sum(root.get("todayAward")),
                        criteriaBuilder.sum(root.get("riseAward")));
                predicates.add(criteriaBuilder.equal(root.get("firstProxy").as(Long.class), proxyId));
                if (currentId!=null){
                    predicates.add(criteriaBuilder.equal(root.get("firstProxy").as(Long.class), currentId));
                }
                // group by type
                criteriaQuery.groupBy(root.get("firstProxy"));
            }
        } else if (proxyRole == CommonConst.NUMBER_2) {
            if (agentMark) {
                criteriaQuery.multiselect(root.get("thirdProxy"), criteriaBuilder.sum(root.get("bettingNumber")),
                        criteriaBuilder.sum(root.get("amount")), criteriaBuilder.sum(root.get("betAmount")),
                        criteriaBuilder.sum(root.get("validAmount")), criteriaBuilder.sum(root.get("winLossAmount")),
                        criteriaBuilder.sum(root.get("userAmount")), criteriaBuilder.sum(root.get("surplusAmount")),
                        criteriaBuilder.sum(root.get("newAmount")), criteriaBuilder.sum(root.get("newUserAmount")),
                        criteriaBuilder.sum(root.get("newSurplusAmount")),
                        criteriaBuilder.sum(root.get("todayAward")),
                        criteriaBuilder.sum(root.get("riseAward")));
                predicates.add(criteriaBuilder.equal(root.get("secondProxy").as(Long.class), proxyId));
                predicates.add(criteriaBuilder.equal(root.get("firstProxy").as(Long.class), agentId));
                criteriaQuery.groupBy(root.get("thirdProxy"));
            } else {
                criteriaQuery.multiselect(root.get("secondProxy"), criteriaBuilder.sum(root.get("bettingNumber")),
                        criteriaBuilder.sum(root.get("amount")), criteriaBuilder.sum(root.get("betAmount")),
                        criteriaBuilder.sum(root.get("validAmount")), criteriaBuilder.sum(root.get("winLossAmount")),
                        criteriaBuilder.sum(root.get("userAmount")), criteriaBuilder.sum(root.get("surplusAmount")),
                        criteriaBuilder.sum(root.get("newAmount")), criteriaBuilder.sum(root.get("newUserAmount")),
                        criteriaBuilder.sum(root.get("newSurplusAmount")),
                        criteriaBuilder.sum(root.get("todayAward")),
                        criteriaBuilder.sum(root.get("riseAward")));
                predicates.add(criteriaBuilder.equal(root.get("secondProxy").as(Long.class), proxyId));
                predicates.add(criteriaBuilder.notEqual(root.get("firstProxy").as(Long.class), 0L));
                if (currentId!=null){
                    //总代
                    if (currentRole.equals( CommonConst.NUMBER_1)){
                        predicates.add(criteriaBuilder.equal(root.get("firstProxy").as(Long.class), currentId));
                    }
                    //区域代
                    else  if (currentRole.equals( CommonConst.NUMBER_2)){
                        predicates.add(criteriaBuilder.equal(root.get("secondProxy").as(Long.class), currentId));

                    }
                }
                // group by type
                criteriaQuery.groupBy(root.get("secondProxy"));
            }
        } else {
            criteriaQuery.multiselect(root.get("thirdProxy"), criteriaBuilder.sum(root.get("bettingNumber")),
                    criteriaBuilder.sum(root.get("amount")), criteriaBuilder.sum(root.get("betAmount")),
                    criteriaBuilder.sum(root.get("validAmount")), criteriaBuilder.sum(root.get("winLossAmount")),
                    criteriaBuilder.sum(root.get("userAmount")), criteriaBuilder.sum(root.get("surplusAmount")),
                    criteriaBuilder.sum(root.get("newAmount")), criteriaBuilder.sum(root.get("newUserAmount")),
                    criteriaBuilder.sum(root.get("newSurplusAmount")),
                    criteriaBuilder.sum(root.get("todayAward")),
                    criteriaBuilder.sum(root.get("riseAward")));
            predicates.add(criteriaBuilder.equal(root.get("thirdProxy").as(Long.class), proxyId));
            predicates.add(criteriaBuilder.notEqual(root.get("firstProxy").as(Long.class), 0L));
            if (currentId!=null){
                //总代
                if (currentRole.equals( CommonConst.NUMBER_1)){
                    predicates.add(criteriaBuilder.equal(root.get("firstProxy").as(Long.class), currentId));
                }
                //区域代
                else  if (currentRole.equals( CommonConst.NUMBER_2)){
                    predicates.add(criteriaBuilder.equal(root.get("secondProxy").as(Long.class), currentId));

                }
            }


            // group by type
            criteriaQuery.groupBy(root.get("thirdProxy"));
        }
        criteriaQuery.where(predicates.toArray(new Predicate[predicates.size()]));
        // criteriaQuery拼成的sql是select type,max(price) maxPrice,sum(price) sumPrice from books group by
        // type；查询出的列与对象BookInfo的属性对应
        // 记录当前sql查询结果总条数
        List<GameRecordReportNew> counts = entityManager.createQuery(criteriaQuery).getResultList();
        // sql查询对象
        TypedQuery<GameRecordReportNew> createQuery = entityManager.createQuery(criteriaQuery);
        // 设置分页参数
        createQuery.setFirstResult(page.getPageNumber() * page.getPageSize());
        createQuery.setMaxResults(page.getPageSize());
        // 返回查询的分页结果，createQuery.getResultList()为分页查询的结果对象，counts.size()为设置分页参数之前查询的总数
        return new PageImpl<GameRecordReportNew>(createQuery.getResultList(), page, counts.size());
    }



    public GameRecordReportNew findRecordRecordSumProxy(GameRecordReportNew gameRecordReport, String startSetTime,
                                                   String endSetTime, Long proxyId, Integer proxyRole,Integer currentRole,Long currentAgentId) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<GameRecordReportNew> query = builder.createQuery(GameRecordReportNew.class);
        Root<GameRecordReportNew> root = query.from(GameRecordReportNew.class);

        query.multiselect(builder.sum(root.get("bettingNumber").as(Integer.class)).alias("bettingNumber"),
                builder.sum(root.get("amount").as(BigDecimal.class)).alias("amount"),
                builder.sum(root.get("betAmount").as(BigDecimal.class)).alias("betAmount"),
                builder.sum(root.get("validAmount").as(BigDecimal.class)).alias("validAmount"),
                builder.sum(root.get("winLossAmount").as(BigDecimal.class)).alias("winLossAmount"),
                builder.sum(root.get("userAmount").as(BigDecimal.class)).alias("userAmount"),
                builder.sum(root.get("surplusAmount").as(BigDecimal.class)).alias("surplusAmount"),
                builder.sum(root.get("newAmount").as(BigDecimal.class)).alias("newAmount"),
                builder.sum(root.get("newUserAmount").as(BigDecimal.class)).alias("newUserAmount"),
                builder.sum(root.get("newSurplusAmount").as(BigDecimal.class)).alias("newSurplusAmount"),
                builder.sum(root.get("todayAward").as(BigDecimal.class)).alias("todayAward"),
                builder.sum(root.get("riseAward").as(BigDecimal.class)).alias("riseAward"));

        List<Predicate> predicates = new ArrayList();
        if (proxyRole != null && proxyRole == CommonConst.NUMBER_1) {
            predicates.add(builder.equal(root.get("firstProxy").as(Long.class), proxyId));
        } else if (proxyRole != null && proxyRole == CommonConst.NUMBER_2) {
            predicates.add(builder.equal(root.get("secondProxy").as(Long.class), proxyId));
        } else if (proxyRole != null && proxyRole == CommonConst.NUMBER_3) {
            predicates.add(builder.equal(root.get("thirdProxy").as(Long.class), proxyId));
        }

        //总代
        if (currentRole.equals( CommonConst.NUMBER_1)){
            predicates.add(builder.equal(root.get("firstProxy").as(Long.class), currentAgentId));
        }
        //区域代
        else  if (currentRole.equals( CommonConst.NUMBER_2)){
            predicates.add(builder.equal(root.get("secondProxy").as(Long.class), currentAgentId));
        }

        if (!com.qianyi.modulecommon.util.CommonUtil.checkNull(gameRecordReport.getPlatform())) {
            predicates.add(builder.equal(root.get("platform").as(String.class), gameRecordReport.getPlatform()));
        }
        if (!ObjectUtils.isEmpty(startSetTime) && !ObjectUtils.isEmpty(endSetTime)) {
            predicates.add(builder.between(root.get("staticsTimes").as(String.class), startSetTime, endSetTime));
        }
        predicates.add(builder.notEqual(root.get("firstProxy").as(Long.class), 0L));
        query.where(predicates.toArray(new Predicate[predicates.size()]));
        GameRecordReportNew singleResult = entityManager.createQuery(query).getSingleResult();
        return singleResult;
    }
}
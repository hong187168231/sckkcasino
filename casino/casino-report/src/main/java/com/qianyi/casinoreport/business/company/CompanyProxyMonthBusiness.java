package com.qianyi.casinoreport.business.company;

import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinocore.vo.CompanyOrderAmountVo;
import com.qianyi.casinocore.vo.CompanyProxyMonthVo;
import com.qianyi.casinoreport.vo.CompanyLevelBO;
import com.qianyi.modulecommon.util.MessageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class CompanyProxyMonthBusiness {

    @Autowired
    private CompanyProxyMonthService companyProxyMonthService;

    @Autowired
    private ProxyCommissionService proxyCommissionService;

    @Autowired
    private ProxyRebateConfigService proxyRebateConfigService;

    @Autowired
    private GameRecordService gameRecordService;
    @Autowired
    private GameRecordGoldenFService gameRecordGoldenFService;

    @Autowired
    private CompanyLevelProcessBusiness companyLevelProcessBusiness;

    @Autowired
    private RebateConfigService rebateConfigService;

    @Autowired
    private ProxyUserService proxyUserService;


    @Autowired
    private MessageUtil messageUtil;


    //传入计算当天的时间  yyyy-MM-dd 格式
    @Transactional
    public void processMonthReport(String dayTime){
        String startTime = getStartTime(dayTime);
        String endTime = getEndTime(dayTime);
        //删除月
        companyProxyMonthService.deleteAllMonth(getMonthTime(dayTime));
        log.info("processDailyReport start startTime:{} endTime:{}",startTime,endTime);
        //查询当天游戏记录(third_proxy is not null)
        List<CompanyOrderAmountVo> companyOrderAmountVoList = gameRecordService.getStatisticsResult(startTime,endTime);
        List<CompanyProxyMonth> firstList= new ArrayList<>();
        List<CompanyProxyMonth> secondeList= new ArrayList<>();
        List<CompanyProxyMonth> thirdList= new ArrayList<>();
        companyOrderAmountVoList.forEach(item->processOrder(item,firstList,secondeList,thirdList));
        if(firstList.size() == 0) {
            log.info("first level is no user");
            return;
        }
        //查询电子游戏数据
        List<CompanyProxyMonth> firstList1= new ArrayList<>();
        List<CompanyProxyMonth> secondeList1= new ArrayList<>();
        List<CompanyProxyMonth> thirdList1= new ArrayList<>();
        List<CompanyOrderAmountVo> statisticsResult = gameRecordGoldenFService.getStatisticsResult(startTime, endTime);
        statisticsResult.forEach(item->processOrder(item,firstList1,secondeList1,thirdList1));
        if(firstList.size() == 0) {
            log.info("first level is no user");
            return;
        }
        firstList.addAll(firstList1);
        secondeList.addAll(secondeList1);
        thirdList.addAll(thirdList1);

        List<CompanyProxyMonth> firstResultList = proxyList(firstList);
        List<CompanyProxyMonth> secondeResultList = proxyList(secondeList);
        List<CompanyProxyMonth> thirdResultList = proxyList(thirdList);

        List<CompanyProxyMonth> resultList = processingData(firstResultList, secondeResultList, thirdResultList);

        log.info("save all proxyDetail data");
        log.info("resultList:{}",resultList);
        companyProxyMonthService.saveAll(resultList);
        log.info("save all proxyDetail data finish");
    }

    /**
     * 合并代理数据
     * @param proxyInfoList
     * @return
     */
    public   List<CompanyProxyMonth> proxyList(List<CompanyProxyMonth>  proxyInfoList){
        Map<Long, CompanyProxyMonth> proxyMap = new HashMap<>();
        proxyInfoList.forEach(info ->{
            CompanyProxyMonth last = proxyMap.get(info.getUserId());
            if (null !=last ){
                if (last.getUserIdTemp().equals(info.getUserIdTemp())){
                    info.setPlayerNum(info.getPlayerNum());
                }else {
                    info.setPlayerNum(info.getPlayerNum()+last.getPlayerNum());
                }
                info.setGroupBetAmount(info.getGroupBetAmount().add(last.getGroupBetAmount()));
                info.setGroupTotalprofit(info.getGroupTotalprofit().add(last.getGroupTotalprofit()));
                info.setProfitAmount(info.getProfitAmount().add(last.getProfitAmount()));
                proxyMap.put(info.getUserId(),info);
            }else {
                proxyMap.put(info.getUserId(),info);
            }
        });
       return proxyMap.values().stream().collect(Collectors.toList());
    }


    //处理总返佣
    public List<CompanyProxyMonth>  processingData (List<CompanyProxyMonth> firstList,  List<CompanyProxyMonth> secondeList,  List<CompanyProxyMonth> thirdList){
        log.info("firstList size is {}, secondeList size is {}, thirdList size is {}",firstList.size(),secondeList.size(),thirdList.size());
        // 处理总返佣：总返佣group_totalprofit = 下级profit_amount总计
        List<CompanyProxyMonth> secondeCompanyProxyDetail = processSec(secondeList,thirdList,2);
        List<CompanyProxyMonth> firstCompanyProxyDetail = processSec(firstList,secondeCompanyProxyDetail,1);

        List<CompanyProxyMonth> resultList = Stream.concat(thirdList.stream(),secondeCompanyProxyDetail.stream()).collect(Collectors.toList());
        resultList.addAll(firstCompanyProxyDetail);
        return resultList;
    }

    private List<CompanyProxyMonth> processSec(List<CompanyProxyMonth> firstList,List<CompanyProxyMonth> secList,int level) {
        List<CompanyProxyMonth> companyProxyDetailList = new ArrayList<>();
        Map<Long,List<CompanyProxyMonth>> groupSec = firstList.stream().collect(Collectors.groupingBy(CompanyProxyMonth::getUserId));
        List<CompanyProxyMonth> subThird = new ArrayList<>();
        for (Long userId : groupSec.keySet()) {
            List<CompanyProxyMonth> subList = groupSec.get(userId);
            if(level==2)
                subThird = secList.stream().filter(x->x.getSecondProxy().equals(userId)).collect(Collectors.toList());
            else
                subThird = secList.stream().filter(x->x.getFirstProxy().equals(userId)).collect(Collectors.toList());


            if(userId == 4 && level== 1){
                log.info("++++++++++++");
                log.info("subList is {}",subList);
                log.info("subThird is {}",subThird);
                log.info("++++++++++++");
            }

            CompanyProxyMonth secondeItem = processfirst(subList,subThird);
            companyProxyDetailList.add(secondeItem);
        }

        return companyProxyDetailList;
    }

    private CompanyProxyMonth processfirst(List<CompanyProxyMonth> firstList,List<CompanyProxyMonth> subList) {
        BigDecimal groupBetAmount = firstList.stream().map(x->x.getGroupBetAmount()).reduce(BigDecimal.ZERO,BigDecimal::add);
        BigDecimal profitAmount = firstList.stream().map(x->x.getProfitAmount()).reduce(BigDecimal.ZERO,BigDecimal::add);
        BigDecimal groupTotalProfit = subList.stream().map(x->x.getGroupTotalprofit()).reduce(BigDecimal.ZERO,BigDecimal::add);
        Integer playnums = subList.stream().map(x-> x.getPlayerNum()).reduce(0,Integer::sum);


        CompanyProxyMonth item = firstList.get(0);

        CompanyProxyMonth actItem = (CompanyProxyMonth) item.clone();
        actItem.setGroupBetAmount(groupBetAmount);
        actItem.setProfitAmount(profitAmount);
        actItem.setGroupTotalprofit(groupTotalProfit);
        actItem.setPlayerNum(playnums);
        return actItem;
    }


    /**
     * 月结表：分别计算各级代理的分佣
     * @param companyOrderAmountVo
     * @param firstList
     * @param secondeList
     * @param thirdList
     */
    public void processOrder(CompanyOrderAmountVo companyOrderAmountVo,List<CompanyProxyMonth> firstList,List<CompanyProxyMonth> secondeList,List<CompanyProxyMonth> thirdList){
        //返佣比例
        CompanyLevelBO companyLevelBO = companyLevelProcessBusiness.getLevelData(new BigDecimal(companyOrderAmountVo.getValidbet()),companyOrderAmountVo.getFirstProxy(),companyOrderAmountVo.getGameType());
        //根据基层代查询代理佣金配置表
        ProxyCommission proxyCommission = proxyCommissionService.findByProxyUserId(companyOrderAmountVo.getThirdProxy());

        log.info("companyLevelBO:{}",companyLevelBO);
        log.info("proxyCommission:{}",proxyCommission);


        firstList.add(calculateDetail(companyLevelBO,companyOrderAmountVo,companyOrderAmountVo.getFirstProxy(),proxyCommission.getFirstCommission(),1));
        secondeList.add(calculateDetail(companyLevelBO,companyOrderAmountVo,companyOrderAmountVo.getSecondProxy(),proxyCommission.getSecondCommission(),2));
        thirdList.add(calculateDetail(companyLevelBO,companyOrderAmountVo,companyOrderAmountVo.getThirdProxy(),proxyCommission.getThirdCommission(),3));
    }

    public CompanyProxyMonth calculateDetail(CompanyLevelBO companyLevelBO,CompanyOrderAmountVo companyOrderAmountVo,Long userid,BigDecimal profitRate,Integer proxyType){
        log.info("companyLevelBO:{}",companyLevelBO);
        //个人佣金结算:返佣金额(如:达到1w返佣10元) * 实际倍数(下注金额/配置得返佣金额线)
        BigDecimal totalAmount = companyLevelBO.getProfitAmount().multiply(BigDecimal.valueOf(companyLevelBO.getProfitActTimes()));
        CompanyProxyMonth companyProxyMonth= CompanyProxyMonth.builder()
                .benefitRate(profitRate)
                .firstProxy(companyOrderAmountVo.getFirstProxy())
                .secondProxy(companyOrderAmountVo.getSecondProxy())
                .thirdProxy(companyOrderAmountVo.getThirdProxy())
                .proxyRole(proxyType)
                .userId(userid)
                .userIdTemp(companyOrderAmountVo.getUserId())
                .groupBetAmount(new BigDecimal(companyOrderAmountVo.getValidbet()))
                .playerNum(companyOrderAmountVo.getPlayerNum())
                // 返佣金额(如:达到1w返佣10元) * 实际倍数(下注金额/10000) * 代理佣金配置值
                .profitAmount(totalAmount.multiply(profitRate))
//                .groupTotalprofit(proxyType==3? totalAmount:BigDecimal.ZERO)
                .groupTotalprofit(totalAmount)
                .settleStatus(0)
                .staticsTimes(companyOrderAmountVo.getBetTime().substring(0,7))
//                .betTime(LocalDateTime.parse(companyOrderAmountVo.getBetTime().replace(' ','T')))
                .build();
        log.info("companyProxyMonth:{}",companyProxyMonth);
        return companyProxyMonth;
    }

    /**
     * 根据返佣金额查询当前返佣级别
     * @return
     */
    public String queryRebateLevel(String profitAmount,Long proxyUserId){
        //ProxyUser proxyUser = proxyUserService.findById(proxyUserId);
        //查询父级
        ProxyRebateConfig proxyRebateConfig = proxyRebateConfigService.findByProxyUserIdAndGameType(proxyUserId,null);
        RebateConfig rebateConfig = rebateConfigService.findFirst();
        String profit= profitAmount;
        //L1
        Integer firstMoney=proxyRebateConfig!=null ?proxyRebateConfig.getFirstMoney() :rebateConfig.getFirstMoney();
        if(profit.equals(String.valueOf(firstMoney))){
            return CommonConst.REBATE_LEVEL_1;
        }
        Integer secondMoney=proxyRebateConfig!=null ?proxyRebateConfig.getSecondMoney() :rebateConfig.getSecondMoney();
        if(profit.equals(String.valueOf(secondMoney))){
            return CommonConst.REBATE_LEVEL_2;
        }
        Integer thirdMoney=proxyRebateConfig!=null ?proxyRebateConfig.getThirdMoney() :rebateConfig.getThirdMoney();
        if(profit.equals(String.valueOf(thirdMoney))){
            return CommonConst.REBATE_LEVEL_3;
        }

        Integer fourMoney=proxyRebateConfig!=null ?proxyRebateConfig.getFourMoney() :rebateConfig.getFourMoney();
        if(profit.equals(String.valueOf(fourMoney))){
            return CommonConst.REBATE_LEVEL_4;
        }
        Integer fiveMoney=proxyRebateConfig!=null ?proxyRebateConfig.getFiveMoney() :rebateConfig.getFiveMoney();
        if(profit.equals(String.valueOf(fiveMoney))){
            return CommonConst.REBATE_LEVEL_5;
        }
        Integer sixMoney=proxyRebateConfig!=null ?proxyRebateConfig.getSixMoney() :rebateConfig.getSixMoney();
        if(profit.equals(String.valueOf(sixMoney))){
            return CommonConst.REBATE_LEVEL_6;
        }
        Integer sevenMoney=proxyRebateConfig!=null ?proxyRebateConfig.getSevenMoney() :rebateConfig.getSevenMoney();
        if(profit.equals(String.valueOf(sevenMoney))){
            return CommonConst.REBATE_LEVEL_7;
        }
        Integer eightMoney=proxyRebateConfig!=null ?proxyRebateConfig.getEightMoney() :rebateConfig.getEightMoney();
        if(profit.equals(String.valueOf(eightMoney))){
            return CommonConst.REBATE_LEVEL_8;
        }
        String rebateLevel=messageUtil.get(CommonConst.REBATE_LEVEL);
        return rebateLevel;
    }


    ////////////////////////////////////////////////////////////////////////////

    //传入计算当天的时间  yyyy-MM-dd 格式
//    @Transactional
//    public void processCompanyMonth(String dayTime){
//        String starTime = getStartTime(dayTime);
//        String endTime = getEndTime(dayTime);
//
//        //删除当月的数据
//        companyProxyMonthService.deleteAllMonth(getMonthTime(dayTime));
//        //统计当月的数据
//        List<CompanyProxyMonth> companyProxyMonthList = companyProxyMonthService.queryMonthByDay(starTime,endTime);
//        //插入当月的数据
//        companyProxyMonthService.saveAll(companyProxyMonthList);
//    }

    public String getMonthTime(String dayTime){
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime localtime = LocalDateTime.parse(dayTime+"T00:00:00");
        String strLocalTime = df.format(localtime.plusDays(-1));
        return strLocalTime.substring(0,7);
    }

    public String getStartTime(String dayTime){
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime startTime = LocalDateTime.parse(dayTime+"T00:00:00");
        startTime = startTime.plusDays(-1).with(TemporalAdjusters.firstDayOfMonth());
        return df.format(startTime);
    }

    public String getEndTime(String dayTime){
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime endTime = LocalDateTime.parse(dayTime+"T00:00:00");
        endTime=endTime.plusSeconds(-1);
        return df.format(endTime);
    }
}

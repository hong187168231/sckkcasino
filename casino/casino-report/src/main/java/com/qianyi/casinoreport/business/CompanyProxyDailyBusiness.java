package com.qianyi.casinoreport.business;

import com.qianyi.casinocore.model.CompanyProxyDetail;
import com.qianyi.casinocore.model.ProxyCommission;
import com.qianyi.casinocore.model.ProxyRebateConfig;
import com.qianyi.casinocore.repository.CompanyProxyDetailRepository;
import com.qianyi.casinocore.service.CompanyProxyDetailService;
import com.qianyi.casinocore.service.GameRecordService;
import com.qianyi.casinocore.service.ProxyCommissionService;
import com.qianyi.casinocore.service.ProxyRebateConfigService;
import com.qianyi.casinocore.vo.CompanyOrderAmountVo;
import com.qianyi.casinoreport.vo.CompanyLevelBO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.web.servlet.oauth2.resourceserver.OAuth2ResourceServerSecurityMarker;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class CompanyProxyDailyBusiness {

    @Autowired
    private CompanyProxyDetailService companyProxyDetailService;

    @Autowired
    private ProxyCommissionService proxyCommissionService;

    @Autowired
    private ProxyRebateConfigService proxyRebateConfigService;

    @Autowired
    private GameRecordService gameRecordService;

    @Autowired
    private CompanyLevelProcessBusiness companyLevelProcessBusiness;

    @Autowired
    private CompanyProxyMonthBusiness companyProxyMonthBusiness;

    //传入计算当天的时间  yyyy-MM-dd 格式
    @Transactional
    public void processDailyReport(String dayTime){
        String startTime = getStartTime(dayTime);
        String endTime = getEndTime(dayTime);
        companyProxyDetailService.deleteByDayTime(startTime.substring(0,10));
        log.info("processDailyReport start startTime:{} endTime:{}",startTime,endTime);
        List<CompanyOrderAmountVo> companyOrderAmountVoList = gameRecordService.getStatisticsResult(startTime,endTime);

        List<CompanyProxyDetail> firstList= new ArrayList<>();
        List<CompanyProxyDetail> secondeList= new ArrayList<>();
        List<CompanyProxyDetail> thirdList= new ArrayList<>();

        companyOrderAmountVoList.forEach(item->processOrder(item,firstList,secondeList,thirdList));

        if(firstList.size() == 0) {
            log.info("first level is no user");
            return;
        }

        log.info("firstList size is {}, secondeList size is {}, thirdList size is {}",firstList.size(),secondeList.size(),thirdList.size());

        List<CompanyProxyDetail> secondeCompanyProxyDetail = processSec(secondeList,thirdList);
        List<CompanyProxyDetail> firstCompanyProxyDetail = processSec(firstList,secondeCompanyProxyDetail);

        List<CompanyProxyDetail> resultList = Stream.concat(thirdList.stream(),secondeCompanyProxyDetail.stream()).collect(Collectors.toList());
        resultList.addAll(firstCompanyProxyDetail);

        log.info("save all proxyDetail data");
        companyProxyDetailService.saveAll(resultList);
        log.info("save all proxyDetail data finish");

        log.info("start process month report");
        companyProxyMonthBusiness.processCompanyMonth(dayTime);
        log.info("start process month report finish");
    }

    private List<CompanyProxyDetail> processSec(List<CompanyProxyDetail> firstList,List<CompanyProxyDetail> secList) {
        List<CompanyProxyDetail> companyProxyDetailList = new ArrayList<>();
        Map<Long,List<CompanyProxyDetail>> groupSec = firstList.stream().collect(Collectors.groupingBy(CompanyProxyDetail::getUserId));

        for (Long userId : groupSec.keySet()) {
            List<CompanyProxyDetail> subList = groupSec.get(userId);
            List<CompanyProxyDetail> subThird = secList.stream().filter(x->x.getUserId()==userId).collect(Collectors.toList());
            CompanyProxyDetail secondeItem = processfirst(subList,subThird);
            companyProxyDetailList.add(secondeItem);
        }

        return companyProxyDetailList;
    }

    private CompanyProxyDetail processfirst(List<CompanyProxyDetail> firstList,List<CompanyProxyDetail> subList) {
        BigDecimal groupBetAmount = firstList.stream().map(x->x.getGroupBetAmount()).reduce(BigDecimal.ZERO,BigDecimal::add);
        BigDecimal profitAmount = firstList.stream().map(x->x.getProfitAmount()).reduce(BigDecimal.ZERO,BigDecimal::add);
        CompanyProxyDetail item = firstList.get(0);

        CompanyProxyDetail actItem = (CompanyProxyDetail) item.clone();
        actItem.setGroupBetAmount(groupBetAmount);
        actItem.setProfitAmount(profitAmount);
        actItem.setGroupTotalprofit(subList.stream().map(x->x.getProfitAmount()).reduce(BigDecimal.ZERO,BigDecimal::add));
        return actItem;
    }


    /**
     * 分别计算各级代理的分佣
     * @param companyOrderAmountVo
     * @param firstList
     * @param secondeList
     * @param thirdList
     */
    public void processOrder(CompanyOrderAmountVo companyOrderAmountVo,List<CompanyProxyDetail> firstList,List<CompanyProxyDetail> secondeList,List<CompanyProxyDetail> thirdList){
        CompanyLevelBO companyLevelBO = companyLevelProcessBusiness.getLevelData(new BigDecimal(companyOrderAmountVo.getValidbet()));
        ProxyCommission proxyCommission = proxyCommissionService.findByProxyUserId(companyOrderAmountVo.getThirdProxy());

        log.info("companyLevelBO:{}",companyLevelBO);
        log.info("proxyCommission:{}",proxyCommission);


        firstList.add(calculateDetail(companyLevelBO,companyOrderAmountVo,companyOrderAmountVo.getFirstProxy(),proxyCommission.getFirstCommission(),1));
        secondeList.add(calculateDetail(companyLevelBO,companyOrderAmountVo,companyOrderAmountVo.getSecondProxy(),proxyCommission.getSecondCommission(),2));
        thirdList.add(calculateDetail(companyLevelBO,companyOrderAmountVo,companyOrderAmountVo.getThirdProxy(),proxyCommission.getThirdCommission(),3));
    }

    public CompanyProxyDetail calculateDetail(CompanyLevelBO companyLevelBO,CompanyOrderAmountVo companyOrderAmountVo,Long userid,BigDecimal profitRate,Integer proxyType){
        BigDecimal totalAmount = companyLevelBO.getProfitAmount().multiply(BigDecimal.valueOf(companyLevelBO.getProfitActTimes()));
        return CompanyProxyDetail.builder()
                .benefitRate(profitRate)
                .firstProxy(companyOrderAmountVo.getFirstProxy())
                .secondProxy(companyOrderAmountVo.getSecondProxy())
                .thirdProxy(companyOrderAmountVo.getThirdProxy())
                .proxyRole(proxyType)
                .userId(userid)
                .profitLevel(companyLevelBO.getProfitLevel()+"")
                .profitRate(companyLevelBO.getProfitAmount().toString())
                .groupBetAmount(new BigDecimal(companyOrderAmountVo.getValidbet()))
                .playerNum(companyOrderAmountVo.getPlayerNum())
                .profitAmount(totalAmount.multiply(profitRate))
                .groupTotalprofit(totalAmount.multiply(profitRate))
                .settleStatus(0)
                .staticsTimes(companyOrderAmountVo.getBetTime().substring(0,10))
                .betTime(LocalDateTime.parse(companyOrderAmountVo.getBetTime().replace(' ','T')))
                .build();
    }


    public String getStartTime(String dayTime){
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime startTime = LocalDateTime.parse(dayTime+"T00:00:00");
        return df.format(startTime.plusDays(-1));
    }

    public String getEndTime(String dayTime){
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime endTime = LocalDateTime.parse(dayTime+"T00:00:00");
        endTime=endTime.plusSeconds(-1);
        return df.format(endTime);
    }


}

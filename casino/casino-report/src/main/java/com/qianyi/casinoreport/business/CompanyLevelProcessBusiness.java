package com.qianyi.casinoreport.business;

import com.qianyi.casinocore.model.ProxyRebateConfig;
import com.qianyi.casinocore.model.RebateConfig;
import com.qianyi.casinocore.service.ProxyRebateConfigService;
import com.qianyi.casinocore.service.RebateConfigService;
import com.qianyi.casinoreport.vo.CompanyLevelBO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CompanyLevelProcessBusiness {

    @Autowired
    private RebateConfigService rebateConfigService;

    public CompanyLevelBO getLevelData(BigDecimal amount){
        RebateConfig rebateConfig = rebateConfigService.findFirst();
        log.info("rebateConfig:{}",rebateConfig);
        List<Integer> profitLevelList = getProfitLevelList(rebateConfig);
        Map<Integer,BigDecimal> profitLevelMap = getProfitLevelMap(rebateConfig);
        return getProfitLevel(amount,profitLevelList,profitLevelMap);
    }

    public CompanyLevelBO getProfitLevel(BigDecimal amount, List<Integer> profitLevelList,Map<Integer,BigDecimal> profitLevelMap) {
//        BigDecimal result = amount.divide(BigDecimal.valueOf(10000));
//        Integer level = getLevel(result.intValue(),profitLevelList);
        BigDecimal result = amount.divide(BigDecimal.valueOf(100));
        Integer level = getLevel(result.multiply(BigDecimal.valueOf(100)).intValue(),profitLevelList);
        log.info("level:{},amount:{}",level,amount);

        BigDecimal profitAmount = profitLevelMap.containsKey(level)?profitLevelMap.get(level):BigDecimal.valueOf(0);

        return CompanyLevelBO.builder().profitLevel(level).profitAmount(profitAmount).profitActTimes(result.intValue()).build();
    }

    private Integer getLevel(int compareInt,List<Integer> profitLevelList){
        Integer level = 0;
        for (Integer item : profitLevelList) {
            if(compareInt>item){
                level=item;
            }else if (compareInt<item)
                break;
        }
        return level;
    }

    private Map<Integer, BigDecimal> getProfitLevelMap(RebateConfig RebateConfig) {
        Map<Integer,BigDecimal> profitLevelMap = new HashMap<>();
        profitLevelMap.put(RebateConfig.getFirstMoney(),RebateConfig.getFirstProfit());
        profitLevelMap.put(RebateConfig.getSecondMoney(),RebateConfig.getSecondProfit());
        profitLevelMap.put(RebateConfig.getThirdMoney(),RebateConfig.getThirdProfit());
        profitLevelMap.put(RebateConfig.getFourMoney(),RebateConfig.getFourProfit());
        profitLevelMap.put(RebateConfig.getFiveMoney(),RebateConfig.getFiveProfit());
        profitLevelMap.put(RebateConfig.getSixMoney(),RebateConfig.getSixProfit());
        profitLevelMap.put(RebateConfig.getSevenMoney(),RebateConfig.getSevenProfit());
        profitLevelMap.put(RebateConfig.getEightMoney(),RebateConfig.getEightProfit());
        return profitLevelMap;
    }

    private List<Integer> getProfitLevelList(RebateConfig RebateConfig){
        List<Integer> profitLevelList = new ArrayList<>();
        profitLevelList.add(RebateConfig.getFirstMoney());
        profitLevelList.add(RebateConfig.getSecondMoney());
        profitLevelList.add(RebateConfig.getThirdMoney());
        profitLevelList.add(RebateConfig.getFourMoney());
        profitLevelList.add(RebateConfig.getFiveMoney());
        profitLevelList.add(RebateConfig.getSixMoney());
        profitLevelList.add(RebateConfig.getSevenMoney());
        profitLevelList.add(RebateConfig.getEightMoney());
        return profitLevelList.stream().sorted().collect(Collectors.toList());
    }
}

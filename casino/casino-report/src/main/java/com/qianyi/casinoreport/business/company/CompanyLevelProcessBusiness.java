package com.qianyi.casinoreport.business.company;

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
        Map<Integer,Integer> profitLevelList = getProfitLevelList(rebateConfig);
        Map<Integer,BigDecimal> profitLevelMap = getProfitLevelMap(rebateConfig);
        return getProfitLevel(amount,profitLevelList,profitLevelMap);
    }

    public CompanyLevelBO getProfitLevel(BigDecimal amount,  Map<Integer,Integer> profitLevelList,Map<Integer,BigDecimal> profitLevelMap) {
        //取代理推广返佣配置里的比例
        BigDecimal result = amount;
        Integer level = getLevel(result.intValue(),profitLevelList);
        log.info("level:{},amount:{}",level,amount);

        BigDecimal profitAmount = profitLevelMap.containsKey(level)?profitLevelMap.get(level):BigDecimal.valueOf(0);

        return CompanyLevelBO.builder().profitLevel(level).profitAmount(profitAmount).profitActTimes(result.intValue()).build();
    }

    private Integer getLevel(int compareInt, Map<Integer,Integer> profitLevelList){
        Integer level = 0;
        for (Integer item : profitLevelList.keySet()) {
            if(compareInt>item){
                level=item.intValue();
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

    private  Map<Integer,Integer> getProfitLevelList(RebateConfig RebateConfig){
        Map<Integer,Integer> profitLevelList =  new HashMap<>();
/*        profitLevelList.add(RebateConfig.getFirstMoney());
        profitLevelList.add(RebateConfig.getSecondMoney());
        profitLevelList.add(RebateConfig.getThirdMoney());
        profitLevelList.add(RebateConfig.getFourMoney());
        profitLevelList.add(RebateConfig.getFiveMoney());
        profitLevelList.add(RebateConfig.getSixMoney());
        profitLevelList.add(RebateConfig.getSevenMoney());
        profitLevelList.add(RebateConfig.getEightMoney());*/

        profitLevelList.put(RebateConfig.getFirstAmountLine().intValue(),RebateConfig.getFirstMoney());
        profitLevelList.put(RebateConfig.getSecondAmountLine().intValue(),RebateConfig.getSecondMoney());
        profitLevelList.put(RebateConfig.getThirdAmountLine().intValue(),RebateConfig.getThirdMoney());
        profitLevelList.put(RebateConfig.getFourAmountLine().intValue(),RebateConfig.getFourMoney());
        profitLevelList.put(RebateConfig.getFiveAmountLine().intValue(),RebateConfig.getFiveMoney());
        profitLevelList.put(RebateConfig.getSixAmountLine().intValue(),RebateConfig.getSixMoney());
        profitLevelList.put(RebateConfig.getSevenAmountLine().intValue(),RebateConfig.getSevenMoney());
        profitLevelList.put(RebateConfig.getEightAmountLine().intValue(),RebateConfig.getEightMoney());
        return profitLevelList;
    }
}

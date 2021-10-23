package com.qianyi.casinoreport.business;

import com.qianyi.casinocore.model.ProxyRebateConfig;
import com.qianyi.casinocore.service.ProxyRebateConfigService;
import com.qianyi.casinoreport.vo.CompanyLevelBO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CompanyLevelProcessBusiness {

    @Autowired
    private ProxyRebateConfigService proxyRebateConfigService;

    public CompanyLevelBO getLevelData(BigDecimal amount){
        ProxyRebateConfig proxyRebateConfig = proxyRebateConfigService.findFirst();
        List<Integer> profitLevelList = getProfitLevelList(proxyRebateConfig);
        Map<Integer,BigDecimal> profitLevelMap = getProfitLevelMap(proxyRebateConfig);
        return getProfitLevel(amount,profitLevelList,profitLevelMap);
    }

    public CompanyLevelBO getProfitLevel(BigDecimal amount, List<Integer> profitLevelList,Map<Integer,BigDecimal> profitLevelMap) {
        BigDecimal result = amount.divide(BigDecimal.valueOf(10000));
        Integer level = getLevel(result.intValue(),profitLevelList);
        BigDecimal profitAmount = profitLevelMap.get(level);

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

    private Map<Integer, BigDecimal> getProfitLevelMap(ProxyRebateConfig proxyRebateConfig) {
        Map<Integer,BigDecimal> profitLevelMap = new HashMap<>();
        profitLevelMap.put(proxyRebateConfig.getFirstMoney(),proxyRebateConfig.getFirstProfit());
        profitLevelMap.put(proxyRebateConfig.getSecondMoney(),proxyRebateConfig.getSecondProfit());
        profitLevelMap.put(proxyRebateConfig.getThirdMoney(),proxyRebateConfig.getThirdProfit());
        profitLevelMap.put(proxyRebateConfig.getFourMoney(),proxyRebateConfig.getFourProfit());
        profitLevelMap.put(proxyRebateConfig.getFiveMoney(),proxyRebateConfig.getFiveProfit());
        return profitLevelMap;
    }

    private List<Integer> getProfitLevelList(ProxyRebateConfig proxyRebateConfig){
        List<Integer> profitLevelList = new ArrayList<>();
        profitLevelList.add(proxyRebateConfig.getFirstMoney());
        profitLevelList.add(proxyRebateConfig.getSecondMoney());
        profitLevelList.add(proxyRebateConfig.getThirdMoney());
        profitLevelList.add(proxyRebateConfig.getFourMoney());
        profitLevelList.add(proxyRebateConfig.getFiveMoney());
        return profitLevelList.stream().sorted().collect(Collectors.toList());
    }
}

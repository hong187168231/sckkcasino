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
import java.util.*;
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
        Map<String,Integer> level = getLevel(result.intValue(),profitLevelList);
        log.info("level:{},amount:{}",level,amount);

        BigDecimal profitAmount =BigDecimal.ZERO;

        //金额线
        BigDecimal profitAmountLine = BigDecimal.valueOf(level.get("key"));
        // 判断金额是否大于等于金额线
        if(amount.compareTo(profitAmountLine)>=0){
            //返佣金额
            profitAmount = profitLevelMap.containsKey(level.get("level"))?profitLevelMap.get(level.get("level")):BigDecimal.valueOf(0);
        }

        return CompanyLevelBO.builder().profitLevel(level.get("level")).profitAmount(profitAmount).profitActTimes(level.get("profitActTimes") ).profitAmountLine(profitAmountLine).build();
    }
    private  Map<String,Integer> getLevel(int compareInt, Map<Integer,Integer> profitLevelList){
        Map<String,Integer> ms=new HashMap<>();
        Integer level = 0;
        Integer profitActTimes = 0;
        Integer key = 0;
        for (Integer item : profitLevelList.keySet()) {
            if(compareInt>item){
                level=item;
                profitActTimes=compareInt/profitLevelList.get(item).intValue();;
                key=profitLevelList.get(item).intValue();
            }else if (compareInt<item)
                break;
        }
        //等级
        ms.put("level",level);
        //金额线
        ms.put("key",key);
        //倍数
        ms.put("profitActTimes",profitActTimes);
        return ms;
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
        Map<Integer,Integer> profitLevelList =   new TreeMap<Integer, Integer>();
/*        profitLevelList.add(RebateConfig.getFirstMoney());
        profitLevelList.add(RebateConfig.getSecondMoney());
        profitLevelList.add(RebateConfig.getThirdMoney());
        profitLevelList.add(RebateConfig.getFourMoney());
        profitLevelList.add(RebateConfig.getFiveMoney());
        profitLevelList.add(RebateConfig.getSixMoney());
        profitLevelList.add(RebateConfig.getSevenMoney());
        profitLevelList.add(RebateConfig.getEightMoney());*/

        profitLevelList.put(RebateConfig.getFirstMoney(),RebateConfig.getFirstAmountLine().intValue());
        profitLevelList.put(RebateConfig.getSecondMoney(),RebateConfig.getSecondAmountLine().intValue());
        profitLevelList.put(RebateConfig.getThirdMoney(),RebateConfig.getThirdAmountLine().intValue());
        profitLevelList.put(RebateConfig.getFourMoney(),RebateConfig.getFourAmountLine().intValue());
        profitLevelList.put(RebateConfig.getFiveMoney(),RebateConfig.getFiveAmountLine().intValue());
        profitLevelList.put(RebateConfig.getSixMoney(),RebateConfig.getSixAmountLine().intValue());
        profitLevelList.put(RebateConfig.getSevenMoney(),RebateConfig.getSevenAmountLine().intValue());
        profitLevelList.put(RebateConfig.getEightMoney(),RebateConfig.getEightAmountLine().intValue());
        return profitLevelList;
    }
}

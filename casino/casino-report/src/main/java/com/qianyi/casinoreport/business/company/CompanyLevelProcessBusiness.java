package com.qianyi.casinoreport.business.company;

import com.qianyi.casinocore.model.ProxyRebateConfig;
import com.qianyi.casinocore.model.RebateConfig;
import com.qianyi.casinocore.service.ProxyRebateConfigService;
import com.qianyi.casinocore.service.RebateConfigService;
import com.qianyi.casinoreport.vo.CompanyLevelBO;
import com.qianyi.modulecommon.reponse.ResponseUtil;
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

    @Autowired
    private ProxyRebateConfigService proxyRebateConfigService;

    public CompanyLevelBO getLevelData(BigDecimal amount,Long firstProxy){
        RebateConfig rebateConfig=null;
        ProxyRebateConfig proxyRebateConfig = proxyRebateConfigService.findById(firstProxy);
        if (proxyRebateConfig!=null){
            rebateConfig=queryProxyRebateConfig(proxyRebateConfig);
        }else {
            rebateConfig=rebateConfigService.findFirst();
        }
        log.info("rebateConfig:{}",rebateConfig);
        Map<Integer,Integer> profitLevelList = getProfitLevelList(rebateConfig);
        Map<Integer,BigDecimal> profitLevelMap = getProfitLevelMap(rebateConfig);
        return getProfitLevel(amount,profitLevelList,profitLevelMap);
    }

    public RebateConfig queryProxyRebateConfig(ProxyRebateConfig proxyRebateConfig){
        RebateConfig rebateConfig=new RebateConfig();
        rebateConfig.setFirstMoney(proxyRebateConfig.getFirstMoney());
        rebateConfig.setFirstAmountLine(proxyRebateConfig.getFirstAmountLine());
        rebateConfig.setFirstProfit(proxyRebateConfig.getFirstProfit());

        rebateConfig.setSecondMoney(proxyRebateConfig.getSecondMoney());
        rebateConfig.setSecondAmountLine(proxyRebateConfig.getSecondAmountLine());
        rebateConfig.setSecondProfit(proxyRebateConfig.getSecondProfit());

        rebateConfig.setThirdMoney(proxyRebateConfig.getThirdMoney());
        rebateConfig.setThirdAmountLine(proxyRebateConfig.getThirdAmountLine());
        rebateConfig.setThirdProfit(proxyRebateConfig.getThirdProfit());

        rebateConfig.setFourMoney(proxyRebateConfig.getFourMoney());
        rebateConfig.setFourAmountLine(proxyRebateConfig.getFourAmountLine());
        rebateConfig.setFourProfit(proxyRebateConfig.getFourProfit());

        rebateConfig.setFiveMoney(proxyRebateConfig.getFiveMoney());
        rebateConfig.setFiveAmountLine(proxyRebateConfig.getFiveAmountLine());
        rebateConfig.setFiveProfit(proxyRebateConfig.getFiveProfit());

        rebateConfig.setSixMoney(proxyRebateConfig.getSixMoney());
        rebateConfig.setSixAmountLine(proxyRebateConfig.getSixAmountLine());
        rebateConfig.setSixProfit(proxyRebateConfig.getSixProfit());

        rebateConfig.setSevenMoney(proxyRebateConfig.getSevenMoney());
        rebateConfig.setSevenAmountLine(proxyRebateConfig.getSevenAmountLine());
        rebateConfig.setSevenProfit(proxyRebateConfig.getSevenProfit());

        rebateConfig.setEightMoney(proxyRebateConfig.getEightMoney());
        rebateConfig.setEightAmountLine(proxyRebateConfig.getEightAmountLine());
        rebateConfig.setEightProfit(proxyRebateConfig.getEightProfit());

        return rebateConfig;
    }


    public CompanyLevelBO getProfitLevel(BigDecimal amount,  Map<Integer,Integer> profitLevelList,Map<Integer,BigDecimal> profitLevelMap) {
        //取代理推广返佣配置里的比例
        BigDecimal result = amount;
        Map<String,Integer> level = getLevel(result.intValue(),profitLevelList);
        log.info("level:{},amount:{}",level,amount);

        //金额线
        BigDecimal profitAmountLine = BigDecimal.valueOf(level.get("key"));

        //返佣金额
        BigDecimal  profitAmount = profitLevelMap.containsKey(level.get("level"))?profitLevelMap.get(level.get("level")):BigDecimal.valueOf(0);

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
                if (profitLevelList.get(item).intValue()>0){
                    profitActTimes=compareInt/profitLevelList.get(item).intValue();
                }
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

        profitLevelList.put(RebateConfig.getFirstMoney(),RebateConfig.getFirstAmountLine()==null ? 0:RebateConfig.getFirstAmountLine().intValue());
        profitLevelList.put(RebateConfig.getSecondMoney(),RebateConfig.getSecondAmountLine()==null ? 0:RebateConfig.getSecondAmountLine().intValue());
        profitLevelList.put(RebateConfig.getThirdMoney(),RebateConfig.getThirdAmountLine()==null ? 0:RebateConfig.getThirdAmountLine().intValue());
        profitLevelList.put(RebateConfig.getFourMoney(),RebateConfig.getFourAmountLine()==null ? 0:RebateConfig.getFourAmountLine().intValue());
        profitLevelList.put(RebateConfig.getFiveMoney(),RebateConfig.getFiveAmountLine()==null ? 0:RebateConfig.getFiveAmountLine().intValue());
        profitLevelList.put(RebateConfig.getSixMoney(),RebateConfig.getSixAmountLine()==null ? 0:RebateConfig.getSixAmountLine().intValue());
        profitLevelList.put(RebateConfig.getSevenMoney(),RebateConfig.getSevenAmountLine()==null ? 0:RebateConfig.getSevenAmountLine().intValue());
        profitLevelList.put(RebateConfig.getEightMoney(),RebateConfig.getEightAmountLine()==null ? 0:RebateConfig.getEightAmountLine().intValue());
        return profitLevelList;
    }
}

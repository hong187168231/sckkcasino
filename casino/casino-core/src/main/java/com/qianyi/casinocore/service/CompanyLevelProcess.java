package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.ProxyRebateConfig;
import com.qianyi.casinocore.model.RebateConfig;
import com.qianyi.casinocore.model.RebateConfigLog;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinocore.vo.CompanyLevelVo;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.util.MessageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@Slf4j
@Service
public class CompanyLevelProcess {

    @Autowired
    private RebateConfigService rebateConfigService;

    @Autowired
    private RebateConfigLogService rebateConfigLogService;

    @Autowired
    private ProxyRebateConfigService proxyRebateConfigService;


    @Autowired
    private MessageUtil messageUtil;


    public CompanyLevelVo getLevelData(BigDecimal amount,Long firstProxy,String startDate){
        //个人
        RebateConfigLog personal = rebateConfigLogService.findByTypeAndStaticsTimesAndProxyUserId(Constants.no, startDate, firstProxy);
        ProxyRebateConfig proxyRebateConfig = proxyRebateConfigService.findById(firstProxy);
        RebateConfig  personalConfig=personal!=null?queryPersonalProxyRebateConfig(personal):queryProxyRebateConfig(proxyRebateConfig);

        //全局
        RebateConfigLog overallSituation = rebateConfigLogService.findByTypeAndStaticsTimes(Constants.yes, startDate);
        RebateConfig  rebate=rebateConfigService.findFirst();
        RebateConfig overallSituationConfig=overallSituation!=null?queryPersonalProxyRebateConfig(overallSituation):rebate;

        RebateConfig rebateConfig=null;
        if (personalConfig!=null){
            rebateConfig=personalConfig;
        }else {
            rebateConfig=overallSituationConfig;
        }
        log.info("rebateConfig:{}",rebateConfig);
        Map<Integer,Integer> profitLevelList = getProfitLevelList(rebateConfig);
        Map<Integer,BigDecimal> profitLevelMap = getProfitLevelMap(rebateConfig);
        return getProfitLevel(amount,profitLevelList,profitLevelMap,rebateConfig);
    }

    public RebateConfig queryProxyRebateConfig(ProxyRebateConfig proxyRebateConfig){
        if(proxyRebateConfig==null){
            return null;
        }
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


    public RebateConfig queryPersonalProxyRebateConfig(RebateConfigLog proxyRebateConfig){
        if(proxyRebateConfig==null){
            return null;
        }
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

    public CompanyLevelVo getProfitLevel(BigDecimal amount, Map<Integer,Integer> profitLevelList, Map<Integer,BigDecimal> profitLevelMap, RebateConfig rebateConfig) {
        //取代理推广返佣配置里的比例
        BigDecimal result = amount;
        Map<String,Integer> level = getLevel(result.intValue(),profitLevelList);
        log.info("level:{},amount:{}",level,amount);
        //金额线
        BigDecimal profitAmountLine = BigDecimal.valueOf(level.get("key"));
        //返佣金额
        BigDecimal  profitAmount = profitLevelMap.containsKey(level.get("level"))?profitLevelMap.get(level.get("level")):BigDecimal.valueOf(0);

        //等级
        String profitLevel = queryRebateLevel(level.get("level").toString(),rebateConfig);


        return CompanyLevelVo.builder().profitLevel(profitLevel).profitAmount(profitAmount).profitAmountLine(profitAmountLine).build();
    }
    private  Map<String,Integer> getLevel(int compareInt, Map<Integer,Integer> profitLevelList){
        Map<String,Integer> ms=new HashMap<>();
        Integer level = 0;
        Integer key = 0;
        for (Integer item : profitLevelList.keySet()) {
            if(compareInt>item){
                level=item;
                key=profitLevelList.get(item).intValue();
            }else if (compareInt<item)
                break;
        }
        //等级：业绩额度
        ms.put("level",level);
        //金额线
        ms.put("key",key);
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

    /**
     * 根据返佣金额查询当前返佣级别
     * @return
     */
    public String queryRebateLevel(String profit, RebateConfig rebateConfig){
        //L1
        Integer firstMoney=rebateConfig.getFirstMoney();
        if(profit.equals(String.valueOf(firstMoney))){
            return CommonConst.REBATE_LEVEL_1;
        }
        Integer secondMoney=rebateConfig.getSecondMoney();
        if(profit.equals(String.valueOf(secondMoney))){
            return CommonConst.REBATE_LEVEL_2;
        }
        Integer thirdMoney=rebateConfig.getThirdMoney();
        if(profit.equals(String.valueOf(thirdMoney))){
            return CommonConst.REBATE_LEVEL_3;
        }

        Integer fourMoney=rebateConfig.getFourMoney();
        if(profit.equals(String.valueOf(fourMoney))){
            return CommonConst.REBATE_LEVEL_4;
        }
        Integer fiveMoney=rebateConfig.getFiveMoney();
        if(profit.equals(String.valueOf(fiveMoney))){
            return CommonConst.REBATE_LEVEL_5;
        }
        Integer sixMoney=rebateConfig.getSixMoney();
        if(profit.equals(String.valueOf(sixMoney))){
            return CommonConst.REBATE_LEVEL_6;
        }
        Integer sevenMoney=rebateConfig.getSevenMoney();
        if(profit.equals(String.valueOf(sevenMoney))){
            return CommonConst.REBATE_LEVEL_7;
        }
        Integer eightMoney=rebateConfig.getEightMoney();
        if(profit.equals(String.valueOf(eightMoney))){
            return CommonConst.REBATE_LEVEL_8;
        }
        String rebateLevel=messageUtil.get(CommonConst.REBATE_LEVEL);
        return rebateLevel;
    }
}

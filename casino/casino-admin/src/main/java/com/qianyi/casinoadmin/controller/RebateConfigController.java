package com.qianyi.casinoadmin.controller;

import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.ProxyRebateConfig;
import com.qianyi.casinocore.model.RebateConfig;
import com.qianyi.casinocore.model.RebateConfigLog;
import com.qianyi.casinocore.service.RebateConfigLogService;
import com.qianyi.casinocore.service.RebateConfigService;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.reponse.ResponseCode;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.DateUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@RestController
@RequestMapping("/proxyRebate")
@Api(tags = "运营中心")
public class RebateConfigController {

    @Autowired
    private RebateConfigService rebateConfigService;

    @Autowired
    private RebateConfigLogService rebateConfigLogService;

    @ApiOperation("查询全局代理返佣等级配置")
    @GetMapping("/findAll")
    public ResponseEntity<RebateConfig> findRegisterSwitchVo(){
        RebateConfig first = rebateConfigService.findFirst();
        return new ResponseEntity(ResponseCode.SUCCESS, first);
    }

    @ApiOperation("编辑全局代理返佣等级配置")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "level", value = "返佣等级", required = true),
            @ApiImplicitParam(name = "money", value = "业绩额度(整数)", required = true),
            @ApiImplicitParam(name = "profit", value = "返佣比例", required = true),
    })
    @PostMapping("/updateProxyRebate")
    public ResponseEntity updateRegisterSwitch(RebateConfig rebateConfig){
        if (DateUtil.verifyTime()){
            return ResponseUtil.custom("0点到1点不能修改该配置");
        }
        if (LoginUtil.checkNull(rebateConfig)){
            return ResponseUtil.custom("参数必填");
        }
        if (this.check(rebateConfig)){
            return ResponseUtil.custom("参数必填");
        }
        if (this.verify(rebateConfig)){
            return ResponseUtil.custom("返佣不能大于30块");
        }
        if (this.verifySize(rebateConfig)){
            return ResponseUtil.custom("低级别值不能大于高级别");
        }
        if (this.verifyAmountLineSize(rebateConfig)){
            return ResponseUtil.custom("低级别值不能大于高级别");
        }
        RebateConfig config = rebateConfigService.findFirst();
        if (!LoginUtil.checkNull(rebateConfig)){
            rebateConfig.setId(config.getId());
        }

        rebateConfigService.save(rebateConfig);
        //添加返佣日志,每次修改都记录一次
        addRebateConfigLog(rebateConfig);
        return ResponseUtil.success();
    }

    /**
     * 添加全局返佣日志
     * @param rebateConfig
     */
    public void addRebateConfigLog(RebateConfig rebateConfig){
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM");
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM");
        String dayTime = df.format(LocalDateTime.now());
        dayTime = dayTime.substring(0,7);
        RebateConfigLog byTypeAndStaticsTimes = rebateConfigLogService.findByTypeAndStaticsTimes(Constants.yes, dayTime);
        if (byTypeAndStaticsTimes!=null){
            rebateConfigLogService.delete(byTypeAndStaticsTimes.getId());
        }
        rebateConfigLogService.save(queryRebateConfigLog(rebateConfig,dayTime));
    }



    public RebateConfigLog queryRebateConfigLog(RebateConfig proxyRebateConfig,String dayTime){
        RebateConfigLog rebateConfig=new RebateConfigLog();
        rebateConfig.setType(Constants.yes);
        rebateConfig.setStaticsTimes(dayTime);
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

    private Boolean verify(RebateConfig rebateConfig){
        if (!LoginUtil.checkNull(rebateConfig.getFirstProfit()) && rebateConfig.getFirstProfit().compareTo(new BigDecimal(CommonConst.NUMBER_30)) > CommonConst.NUMBER_0){
            return true;
        }
        if (!LoginUtil.checkNull(rebateConfig.getSecondProfit()) && rebateConfig.getSecondProfit().compareTo(new BigDecimal(CommonConst.NUMBER_30)) > CommonConst.NUMBER_0){
            return true;
        }
        if (!LoginUtil.checkNull(rebateConfig.getThirdProfit()) && rebateConfig.getThirdProfit().compareTo(new BigDecimal(CommonConst.NUMBER_30)) > CommonConst.NUMBER_0){
            return true;
        }
        if (!LoginUtil.checkNull(rebateConfig.getFourProfit()) && rebateConfig.getFourProfit().compareTo(new BigDecimal(CommonConst.NUMBER_30)) > CommonConst.NUMBER_0){
            return true;
        }
        if (!LoginUtil.checkNull(rebateConfig.getFiveProfit()) && rebateConfig.getFiveProfit().compareTo(new BigDecimal(CommonConst.NUMBER_30)) > CommonConst.NUMBER_0){
            return true;
        }
        if (!LoginUtil.checkNull(rebateConfig.getSixProfit()) && rebateConfig.getSixProfit().compareTo(new BigDecimal(CommonConst.NUMBER_30)) > CommonConst.NUMBER_0){
            return true;
        }
        if (!LoginUtil.checkNull(rebateConfig.getSevenProfit()) && rebateConfig.getSevenProfit().compareTo(new BigDecimal(CommonConst.NUMBER_30)) > CommonConst.NUMBER_0){
            return true;
        }
        if (!LoginUtil.checkNull(rebateConfig.getEightProfit()) && rebateConfig.getEightProfit().compareTo(new BigDecimal(CommonConst.NUMBER_30)) > CommonConst.NUMBER_0){
            return true;
        }
        return false;
    }
    private Boolean verifySize(RebateConfig rebateConfig){
        if (rebateConfig.getFirstProfit().compareTo(rebateConfig.getSecondProfit()) >= CommonConst.NUMBER_0 || rebateConfig.getFirstMoney() >= rebateConfig.getSecondMoney() ){
            return true;
        }
        if (rebateConfig.getSecondProfit().compareTo(rebateConfig.getThirdProfit()) >= CommonConst.NUMBER_0 || rebateConfig.getSecondMoney() >= rebateConfig.getThirdMoney() ){
            return true;
        }
        if (rebateConfig.getThirdProfit().compareTo(rebateConfig.getFourProfit()) >= CommonConst.NUMBER_0 || rebateConfig.getThirdMoney() >= rebateConfig.getFourMoney() ){
            return true;
        }
        if (rebateConfig.getFourProfit().compareTo(rebateConfig.getFiveProfit()) >= CommonConst.NUMBER_0 || rebateConfig.getFourMoney() >= rebateConfig.getFiveMoney() ){
            return true;
        }
        if (rebateConfig.getFiveProfit().compareTo(rebateConfig.getSixProfit()) >= CommonConst.NUMBER_0 || rebateConfig.getFiveMoney() >= rebateConfig.getSixMoney() ){
            return true;
        }
        if (rebateConfig.getSixProfit().compareTo(rebateConfig.getSevenProfit()) >= CommonConst.NUMBER_0 || rebateConfig.getSixMoney() >= rebateConfig.getSevenMoney() ){
            return true;
        }
        if (rebateConfig.getSevenProfit().compareTo(rebateConfig.getEightProfit()) >= CommonConst.NUMBER_0 || rebateConfig.getSevenMoney() >= rebateConfig.getEightMoney() ){
            return true;
        }
        return false;
    }
    private Boolean check(RebateConfig rebateConfig){
        if (LoginUtil.checkNull(rebateConfig.getFirstMoney()) || LoginUtil.checkNull(rebateConfig.getFirstProfit())){
            return true;
        }
        if (LoginUtil.checkNull(rebateConfig.getSecondMoney()) || LoginUtil.checkNull(rebateConfig.getSecondProfit())){
            return true;
        }
        if (LoginUtil.checkNull(rebateConfig.getThirdMoney()) || LoginUtil.checkNull(rebateConfig.getThirdProfit())){
            return true;
        }
        if (LoginUtil.checkNull(rebateConfig.getFourMoney()) || LoginUtil.checkNull(rebateConfig.getFourProfit())){
            return true;
        }
        if (LoginUtil.checkNull(rebateConfig.getFiveMoney()) || LoginUtil.checkNull(rebateConfig.getFiveProfit())){
            return true;
        }
        if (LoginUtil.checkNull(rebateConfig.getSixMoney()) || LoginUtil.checkNull(rebateConfig.getSixProfit())){
            return true;
        }
        if (LoginUtil.checkNull(rebateConfig.getSevenMoney()) || LoginUtil.checkNull(rebateConfig.getSevenProfit())){
            return true;
        }
        if (LoginUtil.checkNull(rebateConfig.getEightMoney()) || LoginUtil.checkNull(rebateConfig.getEightProfit())){
            return true;
        }
        return false;
    }


    private Boolean verifyAmountLineSize(RebateConfig rebateConfig){
        if (rebateConfig.getFirstAmountLine().compareTo(rebateConfig.getSecondAmountLine())>=CommonConst.NUMBER_0){
            return true;
        }
        if (rebateConfig.getSecondAmountLine().compareTo(rebateConfig.getThirdAmountLine())>=CommonConst.NUMBER_0){
            return true;
        }
        if ( rebateConfig.getThirdAmountLine().compareTo(rebateConfig.getFourAmountLine())>=CommonConst.NUMBER_0){
            return true;
        }
        if (rebateConfig.getFourAmountLine().compareTo(rebateConfig.getFiveAmountLine())>=CommonConst.NUMBER_0 ){
            return true;
        }
        if (rebateConfig.getFiveAmountLine().compareTo(rebateConfig.getSixAmountLine())>=CommonConst.NUMBER_0){
            return true;
        }
        if (rebateConfig.getSixAmountLine().compareTo(rebateConfig.getSevenAmountLine())>=CommonConst.NUMBER_0){
            return true;
        }
        if ( rebateConfig.getSevenAmountLine().compareTo(rebateConfig.getEightAmountLine())>=CommonConst.NUMBER_0){
            return true;
        }
        return false;
    }
}

package com.qianyi.casinoproxy.controller;

import com.qianyi.casinocore.model.CompanyProxyMonth;
import com.qianyi.casinocore.model.ProxyRebateConfig;
import com.qianyi.casinocore.model.ProxyUser;
import com.qianyi.casinocore.model.RebateConfig;
import com.qianyi.casinocore.service.CompanyProxyMonthService;
import com.qianyi.casinocore.service.ProxyRebateConfigService;
import com.qianyi.casinocore.service.ProxyUserService;
import com.qianyi.casinocore.service.RebateConfigService;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinocore.util.CommonUtil;
import com.qianyi.casinocore.vo.CompanyProxyMonthVo;
import com.qianyi.casinocore.vo.PageResultVO;
import com.qianyi.casinocore.vo.PageVo;
import com.qianyi.casinoproxy.util.CasinoProxyUtil;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Api(tags = "代理中心")
@RestController
@RequestMapping("companyProxyMonth")
public class CompanyProxyMonthController {
    @Autowired
    private CompanyProxyMonthService companyProxyMonthService;
    @Autowired
    private ProxyRebateConfigService proxyRebateConfigService;

    @Autowired
    private RebateConfigService rebateConfigService;
    @Autowired
    private ProxyUserService proxyUserService;

    @ApiOperation("查询代理月结表")
    @GetMapping("/find")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageSize", value = "每页大小(默认10条)", required = false),
            @ApiImplicitParam(name = "pageCode", value = "当前页(默认第一页)", required = false),
            @ApiImplicitParam(name = "proxyRole", value = "代理级别1：总代理 2：区域代理 3：基层代理", required = false),
            @ApiImplicitParam(name = "userName", value = "账号", required = false),
            @ApiImplicitParam(name = "tag", value = "1：含下级 0：不包含", required = false),
            @ApiImplicitParam(name = "staticsTimes", value = "统计时段", required = true),
    })
    public ResponseEntity<CompanyProxyMonthVo> find(Integer pageSize, Integer pageCode, Integer proxyRole, Integer tag, String userName,
                                                    String staticsTimes){
        CompanyProxyMonth companyProxyMonth = new CompanyProxyMonth();
        Sort sort=Sort.by("proxyRole").ascending();
        companyProxyMonth.setProxyRole(proxyRole);
        companyProxyMonth.setStaticsTimes(staticsTimes);
        if (CasinoProxyUtil.setParameter(companyProxyMonth)){
            return ResponseUtil.custom(CommonConst.NETWORK_ANOMALY);
        }
        Long authId = CasinoProxyUtil.getAuthId();
        ProxyUser byId = proxyUserService.findById(authId);
        List<CompanyProxyMonth> companyProxyMonths = new ArrayList<>();
        if (this.findCompanyProxyMonths(tag,userName,companyProxyMonth,sort,companyProxyMonths,byId)){
            return ResponseUtil.custom("参数不合法");
        }
        PageVo pageVO = new PageVo(pageCode,pageSize);
        PageResultVO<CompanyProxyMonth> pageResultVO = (PageResultVO<CompanyProxyMonth>) CommonUtil.handlePageResult(companyProxyMonths, pageVO);
        List<CompanyProxyMonth> content = (List<CompanyProxyMonth>) pageResultVO.getContent();
        PageResultVO<CompanyProxyMonthVo> pageResult = new PageResultVO(pageResultVO);
        if (content.size() > CommonConst.NUMBER_0){
            List<CompanyProxyMonthVo> accountChangeVoList =new LinkedList<>();
            List<Long> proxyUserIds = content.stream().map(CompanyProxyMonth::getUserId).collect(Collectors.toList());
            List<ProxyUser> proxyUser = proxyUserService.findProxyUser(proxyUserIds);
            if(proxyUser != null){
                content.stream().forEach(proxyDetail ->{

                    CompanyProxyMonthVo vo = new CompanyProxyMonthVo(proxyDetail);
                    proxyUser.stream().forEach(proxy->{
                        if (proxy.getId().equals(proxyDetail.getUserId())){
                            vo.setUserName(proxy.getUserName());
                            vo.setNickName(proxy.getNickName());
                        }
                    });
                    //全线返佣比列
                    String profitRate="— —";
                    //全线返佣级别
                    String profitLevel="— —";
                    //只有基层代理才需要展示级别、比例
                    if (vo.getProxyRole().equals(CommonConst.NUMBER_3)){
                        //返佣级别:根据返佣金额查询当前返佣级别
                        profitLevel = queryRebateLevel(vo.getProfitLevel(), vo.getProxyUserId());
                        if (!vo.getProfitRate().equals(CommonConst.STRING_0)){
                            profitRate=CommonConst.REMARKS+ Double.valueOf(vo.getProfitRate()).intValue()+CommonConst.COMPANY;
                        }
                    }
                    vo.setProfitLevel(profitLevel);
                    vo.setProfitRate(profitRate);
                    accountChangeVoList.add(vo);
                });
                content.clear();
                proxyUser.clear();
            }
            pageResult.setContent(accountChangeVoList);
        }
        return ResponseUtil.success(pageResult);
    }

    /**
     * 根据返佣金额查询当前返佣级别
     * @return
     */
    public String queryRebateLevel(String profitAmount,Long proxyUserId){
        ProxyUser proxyUser = proxyUserService.findById(proxyUserId);
        //查询父级
        ProxyRebateConfig proxyRebateConfig = proxyRebateConfigService.findById(proxyUser.getFirstProxy());
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
        return CommonConst.REBATE_LEVEL;
    }

    @ApiOperation("统计代理月结表")
    @GetMapping("/findSum")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "proxyRole", value = "代理级别1：总代理 2：区域代理 3：基层代理", required = false),
            @ApiImplicitParam(name = "userName", value = "账号", required = false),
            @ApiImplicitParam(name = "tag", value = "1：含下级 0：不包含", required = false),
            @ApiImplicitParam(name = "staticsTimes", value = "统计时段", required = true),
    })
    public ResponseEntity<CompanyProxyMonth> findSum(Integer proxyRole, Integer tag, String userName,String staticsTimes){
        CompanyProxyMonth companyProxyMonth = new CompanyProxyMonth();
        companyProxyMonth.setProxyRole(proxyRole);
        companyProxyMonth.setStaticsTimes(staticsTimes);
        if (CasinoProxyUtil.setParameter(companyProxyMonth)){
            return ResponseUtil.custom(CommonConst.NETWORK_ANOMALY);
        }
        List<CompanyProxyMonth> companyProxyMonths = new ArrayList<>();
        Long authId = CasinoProxyUtil.getAuthId();
        ProxyUser byId = proxyUserService.findById(authId);
        if (this.findCompanyProxyMonths(tag,userName,companyProxyMonth,null,companyProxyMonths,byId)){
            return ResponseUtil.custom("参数不合法");
        }
        this.assemble(companyProxyMonths,companyProxyMonth);
        return ResponseUtil.success(companyProxyMonth);
    }
    private void assemble(List<CompanyProxyMonth> list,CompanyProxyMonth vo){
        if (CasinoProxyUtil.checkNull(list) && list.size() == CommonConst.NUMBER_0)
            return;
        BigDecimal groupBetAmount = list.stream().map(CompanyProxyMonth::getGroupBetAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal groupTotalprofit = list.stream().map(CompanyProxyMonth::getGroupTotalprofit).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal profitAmount = list.stream().map(CompanyProxyMonth::getProfitAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        Integer playerNum = list.stream().mapToInt(CompanyProxyMonth::getPlayerNum).sum();
        vo.setPlayerNum(playerNum);
        vo.setGroupBetAmount(groupBetAmount);
        vo.setGroupTotalprofit(groupTotalprofit);
        vo.setProfitAmount(profitAmount);
    }
    private Boolean findCompanyProxyMonths(Integer tag, String userName, CompanyProxyMonth companyProxyMonth, Sort sort,
                                           List<CompanyProxyMonth> companyProxyMonthList,ProxyUser byId){
        List<CompanyProxyMonth> companyProxyMonths;
        if (!CasinoProxyUtil.checkNull(userName)){
            if (CasinoProxyUtil.checkNull(tag)){
                return true;
            }
            ProxyUser byUserName = proxyUserService.findByUserName(userName);
            if (CasinoProxyUtil.checkNull(byUserName)){
                return false;
            }
            if (byId.getProxyRole() == byUserName.getProxyRole() && byId.getId() != byUserName.getId()){//不能搜同级别的
                return false;
            }
            if (byId.getProxyRole() > byUserName.getProxyRole()){//不能搜上级的
                return false;
            }
            if (tag == CommonConst.NUMBER_1){//包含下级
                if (byUserName.getProxyRole() == CommonConst.NUMBER_1){
                    companyProxyMonth.setFirstProxy(byUserName.getId());
                    companyProxyMonths = companyProxyMonthService.findAll(companyProxyMonth, sort);
                }else if(byUserName.getProxyRole() == CommonConst.NUMBER_2){
                    companyProxyMonth.setSecondProxy(byUserName.getId());
                    companyProxyMonths = companyProxyMonthService.findAll(companyProxyMonth, sort);
                    companyProxyMonths = companyProxyMonths.stream().filter(item -> (item.getProxyRole() != CommonConst.NUMBER_1)).collect(Collectors.toList());
                }else {
                    companyProxyMonth.setThirdProxy(byUserName.getId());
                    companyProxyMonths = companyProxyMonthService.findAll(companyProxyMonth, sort);
                    companyProxyMonths = companyProxyMonths.stream().filter(item -> (item.getProxyRole() == CommonConst.NUMBER_3)).collect(Collectors.toList());
                }
            }else {
                companyProxyMonth.setUserId(byUserName.getId());
                companyProxyMonths = companyProxyMonthService.findAll(companyProxyMonth, sort);
            }
        }else {
            companyProxyMonths = companyProxyMonthService.findAll(companyProxyMonth, sort);
            if (byId.getProxyRole() == CommonConst.NUMBER_2){
                companyProxyMonths = companyProxyMonths.stream().filter(item -> (item.getProxyRole() != CommonConst.NUMBER_1)).collect(Collectors.toList());
            }else if(byId.getProxyRole() == CommonConst.NUMBER_3){
                companyProxyMonths = companyProxyMonths.stream().filter(item -> (item.getProxyRole() == CommonConst.NUMBER_3)).collect(Collectors.toList());
            }
        }
        companyProxyMonthList.addAll(companyProxyMonths);
        return false;
    }
}

package com.qianyi.casinoproxy.controller.jiceng;

import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinocore.vo.CompanyProxyReportVo;
import com.qianyi.casinocore.model.ProxyHomePageReport;
import com.qianyi.casinocore.service.ProxyHomePageReportService;
import com.qianyi.casinoproxy.task.ProxyHomePageReportTask;
import com.qianyi.casinoproxy.util.CasinoProxyUtil;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.DateUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

@Api(tags = "基层代理代理中心")
@RestController
@Slf4j
@RequestMapping("companyProxyDetail/jiceng")
public class ThridCompanyProxyDetailController {

    @Autowired
    private ProxyUserService proxyUserService;

    @Autowired
    private ProxyHomePageReportService proxyHomePageReportService;

    public final static String start = " 00:00:00";

    public final static String end = " 23:59:59";
    @ApiOperation("查询代理报表")
    @GetMapping("/find")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "startDate", value = "起始时间查询", required = false),
            @ApiImplicitParam(name = "endDate", value = "结束时间查询", required = false),
    })
    public ResponseEntity<CompanyProxyReportVo> find(@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
                                                     @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endDate){
        List<CompanyProxyReportVo> list = new LinkedList<>();
        ProxyHomePageReport proxyHomeReport = new  ProxyHomePageReport();
        Long authId = CasinoProxyUtil.getAuthId();
        proxyHomeReport.setProxyUserId(authId);
        ProxyUser proxy = proxyUserService.findById(authId);
        if (CasinoProxyUtil.checkNull(proxy)){
            return ResponseUtil.success(list);
        }
        try {
            CompanyProxyReportVo companyProxyReportVo = null;
            if ((CasinoProxyUtil.checkNull(startDate,endDate)) || DateUtil.isEffectiveDate(new Date(),startDate,endDate)){
                try {
                    companyProxyReportVo = this.assemble(proxy);
                } catch (ParseException e) {
                    log.error("代理当日报表统计失败",e);
                }
            }else {
                companyProxyReportVo = this.assemble(new CompanyProxyReportVo(),proxy);
            }
            list.add(companyProxyReportVo);
            String startTime = startDate==null? null:DateUtil.getSimpleDateFormat1().format(startDate);
            String endTime =  endDate==null? null:DateUtil.getSimpleDateFormat1().format(endDate);
            List<ProxyHomePageReport> proxyHomePageReports = proxyHomePageReportService.findHomePageReports(proxyHomeReport,startTime,endTime);
            if (CasinoProxyUtil.checkNull(proxyHomePageReports) || proxyHomePageReports.size() == CommonConst.NUMBER_0){
                return ResponseUtil.success(this.getCompanyProxyReportVos(list));
            }
            Map<Long, List<ProxyHomePageReport>> firstMap = proxyHomePageReports.stream().collect(Collectors.groupingBy(ProxyHomePageReport::getProxyUserId));
            list.forEach(vo->{
                List<ProxyHomePageReport> proxyHomes = firstMap.get(vo.getId());
                if (!CasinoProxyUtil.checkNull(proxyHomes) && proxyHomes.size() > CommonConst.NUMBER_0){
                    BigDecimal chargeAmount = proxyHomes.stream().map(ProxyHomePageReport::getChargeAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal withdrawMoney = proxyHomes.stream().map(ProxyHomePageReport::getWithdrawMoney).reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal validbetAmount = proxyHomes.stream().map(ProxyHomePageReport::getValidbetAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                    Integer activeUsers = proxyHomes.stream().mapToInt(ProxyHomePageReport::getActiveUsers).sum();
                    Integer newUsers = proxyHomes.stream().mapToInt(ProxyHomePageReport::getNewUsers).sum();
                    Integer newSecondProxys = proxyHomes.stream().mapToInt(ProxyHomePageReport::getNewSecondProxys).sum();
                    Integer newThirdProxys = proxyHomes.stream().mapToInt(ProxyHomePageReport::getNewThirdProxys).sum();
                    vo.setChargeAmount(vo.getChargeAmount().add(chargeAmount));
                    vo.setWithdrawMoney(vo.getWithdrawMoney().add(withdrawMoney));
                    vo.setGroupPerformance(vo.getGroupPerformance().add(validbetAmount));
                    vo.setActiveUsers(activeUsers + vo.getActiveUsers());
                    vo.setGroupNewUsers(newUsers + vo.getGroupNewUsers());
                    vo.setGroupNewProxyUsers(vo.getGroupNewProxyUsers() + newSecondProxys + newThirdProxys);
                }
            });
        }catch (Exception ex){
            log.error("代理报表统计失败",ex);
        }
        return ResponseUtil.success(this.getCompanyProxyReportVos(list));
    }
    @ApiOperation("每日结算细节")
    @GetMapping("/findDailyDetails")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "startDate", value = "起始时间查询", required = false),
            @ApiImplicitParam(name = "endDate", value = "结束时间查询", required = false),
    })
    public ResponseEntity<CompanyProxyReportVo> findDailyDetails(@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
                                                                 @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endDate){

        ProxyHomePageReport proxyHomeReport = new  ProxyHomePageReport();
        Long authId = CasinoProxyUtil.getAuthId();
        proxyHomeReport.setProxyUserId(authId);
        ProxyUser proxyUser = proxyUserService.findById(authId);
        CompanyProxyReportVo companyProxyReportVo = null;
        if ((CasinoProxyUtil.checkNull(startDate,endDate)) || DateUtil.isEffectiveDate(new Date(),startDate,endDate)){
            try {
                companyProxyReportVo = this.assemble(proxyUser);
            } catch (ParseException e) {
                log.error("代理每日结算细节失败",e);
            }
        }
        Sort sort=Sort.by("id").descending();
        List<CompanyProxyReportVo> list = new LinkedList<>();
        String startTime = startDate==null? null:DateUtil.getSimpleDateFormat1().format(startDate);
        String endTime =  endDate==null? null:DateUtil.getSimpleDateFormat1().format(endDate);
        List<ProxyHomePageReport> proxyHomePageReports = proxyHomePageReportService.findHomePageReports(proxyHomeReport,startTime,endTime,sort);
        if (!CasinoProxyUtil.checkNull(companyProxyReportVo)){
            list.add(companyProxyReportVo);
        }
        if (!CasinoProxyUtil.checkNull(proxyHomePageReports) && proxyHomePageReports.size() > CommonConst.NUMBER_0){
            proxyHomePageReports.forEach(proxyHomePageReport -> {
                list.add(this.assemble(proxyHomePageReport));
            });
        }
        return ResponseUtil.success(list);
    }
    private CompanyProxyReportVo assemble(ProxyUser byId) throws ParseException {
        Calendar nowTime = Calendar.getInstance();
        String format = DateUtil.getSimpleDateFormat1().format(nowTime.getTime());
        ProxyHomePageReport proxyHomePageReport = new ProxyHomePageReport();
        proxyHomePageReport.setStaticsTimes(format);
        String startTime = format + start;
        String endTime = format + end;
        Date start = DateUtil.getSimpleDateFormat().parse(startTime);
        Date end = DateUtil.getSimpleDateFormat().parse(endTime);
        proxyHomePageReportService.chargeOrder(byId, start, end, proxyHomePageReport);
        proxyHomePageReportService.withdrawOrder(byId, start, end, proxyHomePageReport);
        proxyHomePageReportService.gameRecord(byId, startTime, endTime, proxyHomePageReport);
        proxyHomePageReportService.getNewUsers(byId, start, end, proxyHomePageReport);
        CompanyProxyReportVo companyProxyReportVo = this.assemble(proxyHomePageReport);
        return this.assemble(companyProxyReportVo,byId);
    }

    private CompanyProxyReportVo assemble(CompanyProxyReportVo companyProxyReportVo,ProxyUser byId){
        companyProxyReportVo.setId(byId.getId());
        companyProxyReportVo.setParentId(byId.getSecondProxy());
        companyProxyReportVo.setUserName(byId.getUserName());
        companyProxyReportVo.setNickName(byId.getNickName());
        companyProxyReportVo.setProxyRole(byId.getProxyRole());
        return companyProxyReportVo;
    }
    private CompanyProxyReportVo assemble(ProxyHomePageReport proxyHomePageReport){
        CompanyProxyReportVo companyProxyReportVo = new CompanyProxyReportVo();
        companyProxyReportVo.setChargeAmount(proxyHomePageReport.getChargeAmount());
        companyProxyReportVo.setWithdrawMoney(proxyHomePageReport.getWithdrawMoney());
        companyProxyReportVo.setGroupPerformance(proxyHomePageReport.getValidbetAmount());
        companyProxyReportVo.setGroupNewUsers(proxyHomePageReport.getNewUsers());
        companyProxyReportVo.setActiveUsers(proxyHomePageReport.getActiveUsers());
        companyProxyReportVo.setStaticsTimes(proxyHomePageReport.getStaticsTimes());
        return companyProxyReportVo;
    }

    private List<CompanyProxyReportVo> getCompanyProxyReportVos(List<CompanyProxyReportVo> list){
        if (list.size() > CommonConst.NUMBER_0){
            List<Long> collect = list.stream().map(CompanyProxyReportVo::getParentId).collect(Collectors.toList());
            List<ProxyUser> proxyUsers = proxyUserService.findProxyUser(collect);
            list.stream().forEach(companyProxyReportVo -> {
                proxyUsers.forEach(proxyUser -> {
                    if (proxyUser.getId().equals(companyProxyReportVo.getParentId())){
                        companyProxyReportVo.setSuperiorProxyAccount(proxyUser.getUserName());
                    }
                });
            });
        }
        return list;
    }
}

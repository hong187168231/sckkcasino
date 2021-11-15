package com.qianyi.casinoproxy.controller;

import com.qianyi.casinocore.model.CompanyProxyDetail;
import com.qianyi.casinocore.model.ProxyUser;
import com.qianyi.casinocore.service.CompanyProxyDetailService;
import com.qianyi.casinocore.service.ProxyUserService;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinocore.util.PageUtil;
import com.qianyi.casinocore.vo.CompanyProxyDetailVo;
import com.qianyi.casinocore.vo.CompanyProxyReportVo;
import com.qianyi.casinocore.vo.PageResultVO;
import com.qianyi.casinocore.vo.PageVo;
import com.qianyi.casinoproxy.model.ProxyHomePageReport;
import com.qianyi.casinoproxy.service.ProxyHomePageReportService;
import com.qianyi.casinoproxy.task.ProxyHomePageReportTask;
import com.qianyi.casinoproxy.util.CasinoProxyUtil;
import com.qianyi.casinoproxy.vo.ProxyHomePageReportVo;
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

@Api(tags = "代理中心")
@RestController
@Slf4j
@RequestMapping("companyProxyDetail")
public class CompanyProxyDetailController {
    @Autowired
    private ProxyUserService proxyUserService;

    @Autowired
    private ProxyHomePageReportTask proxyHomePageReportTask;

    @Autowired
    private ProxyHomePageReportService proxyHomePageReportService;

    public final static String start = " 00:00:00";

    public final static String end = " 23:59:59";
    @ApiOperation("查询代理报表")
    @GetMapping("/find")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "proxyRole", value = "代理级别1：总代理 2：区域代理 3：基层代理", required = false),
            @ApiImplicitParam(name = "userName", value = "账号", required = false),
            @ApiImplicitParam(name = "tag", value = "1：含下级 0：不包含", required = false),
            @ApiImplicitParam(name = "startDate", value = "起始时间查询", required = true),
            @ApiImplicitParam(name = "endDate", value = "结束时间查询", required = true),
    })
    public ResponseEntity<CompanyProxyReportVo> find(Integer proxyRole, Integer tag, String userName,
                                                     @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
                                                     @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endDate){
        List<CompanyProxyReportVo> list = new LinkedList<>();
        ProxyUser proxyUser = new ProxyUser();
        if (CasinoProxyUtil.setParameter(proxyUser)){
            return ResponseUtil.custom(CommonConst.NETWORK_ANOMALY);
        }
        ProxyHomePageReport proxyHomeReport = new  ProxyHomePageReport();
        if (CasinoProxyUtil.setParameter(proxyHomeReport)){
            return ResponseUtil.custom(CommonConst.NETWORK_ANOMALY);
        }
        proxyUser.setProxyRole(proxyRole);
        proxyHomeReport.setProxyRole(proxyRole);
        if (!CasinoProxyUtil.checkNull(userName)){
            if (CasinoProxyUtil.checkNull(tag)){
                return ResponseUtil.custom("参数不合法");
            }
            ProxyUser byUserName = proxyUserService.findByUserName(userName);
            if (CasinoProxyUtil.checkNull(byUserName)){
                return ResponseUtil.success(list);
            }
            if (tag == CommonConst.NUMBER_0){
                proxyUser.setUserName(userName);
                proxyHomeReport.setProxyUserId(byUserName.getId());
            }
            if (tag == CommonConst.NUMBER_1){
                if (byUserName.getProxyRole() == CommonConst.NUMBER_2){
                    proxyUser.setSecondProxy(byUserName.getId());
                    proxyHomeReport.setSecondProxy(byUserName.getId());
                }
            }
        }
        List<ProxyUser> proxyUserList = proxyUserService.findProxyUserList(proxyUser);
        if (CasinoProxyUtil.checkNull(proxyUserList) || proxyUserList.size() == CommonConst.NUMBER_0){
            return ResponseUtil.success(list);
        }
        try {
            proxyUserList.forEach(proxyUser1 -> {
                CompanyProxyReportVo companyProxyReportVo = null;
                if ((CasinoProxyUtil.checkNull(startDate,endDate)) || DateUtil.isEffectiveDate(new Date(),startDate,endDate)){
                    try {
                        companyProxyReportVo = this.assemble(proxyUser1);
                    } catch (ParseException e) {
                        log.error("代理当日报表统计失败",e);
                    }
                }else {
                    companyProxyReportVo = this.assemble(new CompanyProxyReportVo(),proxyUser1);
                }
                list.add(companyProxyReportVo);
            });
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
            @ApiImplicitParam(name = "id", value = "当前id", required = false),
            @ApiImplicitParam(name = "userName", value = "账号", required = false),
            @ApiImplicitParam(name = "startDate", value = "起始时间查询", required = true),
            @ApiImplicitParam(name = "endDate", value = "结束时间查询", required = true),
    })
    public ResponseEntity<CompanyProxyReportVo> findDailyDetails(Long id,String userName,@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
                                                     @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endDate){
        if (CasinoProxyUtil.checkNull(id) && CasinoProxyUtil.checkNull(userName)  ){
            return ResponseUtil.custom("参数不合法");
        }
        ProxyHomePageReport proxyHomeReport = new  ProxyHomePageReport();
        ProxyUser proxyUser = null;
        if (!CasinoProxyUtil.checkNull(id)){
            proxyUser = proxyUserService.findById(id);
            proxyHomeReport.setProxyUserId(proxyUser == null?0L:proxyUser.getId());
        }
        if (!CasinoProxyUtil.checkNull(userName)){
            proxyUser = proxyUserService.findByUserName(userName);
            proxyHomeReport.setProxyUserId(proxyUser == null?0L:proxyUser.getId());
        }
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
        proxyHomePageReportTask.chargeOrder(byId, start, end, proxyHomePageReport);
        proxyHomePageReportTask.withdrawOrder(byId, start, end, proxyHomePageReport);
        proxyHomePageReportTask.gameRecord(byId, startTime, endTime, proxyHomePageReport);
        proxyHomePageReportTask.getNewUsers(byId, start, end, proxyHomePageReport);
        if (byId.getProxyRole() == CommonConst.NUMBER_1){
            proxyHomePageReportTask.getNewSecondProxys(byId,start,end,proxyHomePageReport);
        }
        if (byId.getProxyRole() != CommonConst.NUMBER_3){
            proxyHomePageReportTask.getNewThirdProxys(byId,start,end,proxyHomePageReport);
        }
        CompanyProxyReportVo companyProxyReportVo = this.assemble(proxyHomePageReport);
        return this.assemble(companyProxyReportVo,byId);
    }

    private CompanyProxyReportVo assemble(CompanyProxyReportVo companyProxyReportVo,ProxyUser byId){
        companyProxyReportVo.setId(byId.getId());
        companyProxyReportVo.setParentId(CommonConst.LONG_0);
        if (byId.getProxyRole() == CommonConst.NUMBER_3){
            companyProxyReportVo.setParentId(byId.getSecondProxy());
        }else if(byId.getProxyRole() == CommonConst.NUMBER_2){
            companyProxyReportVo.setParentId(byId.getFirstProxy());
        }
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
        companyProxyReportVo.setGroupNewProxyUsers(proxyHomePageReport.getNewSecondProxys() + proxyHomePageReport.getNewThirdProxys());
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

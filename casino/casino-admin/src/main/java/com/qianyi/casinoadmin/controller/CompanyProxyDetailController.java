package com.qianyi.casinoadmin.controller;
import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.ProxyHomePageReport;
import com.qianyi.casinocore.model.ProxyUser;
import com.qianyi.casinocore.service.ProxyHomePageReportService;
import com.qianyi.casinocore.service.ProxyUserService;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinocore.vo.CompanyProxyReportVo;
import com.qianyi.casinocore.vo.PageResultVO;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.DateUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
@Slf4j
@RestController
@RequestMapping("companyProxyDetail")
public class CompanyProxyDetailController {
    @Autowired
    private ProxyUserService proxyUserService;

    @Autowired
    private ProxyHomePageReportService proxyHomePageReportService;

    public final static String start = " 00:00:00";

    public final static String end = " 23:59:59";
    @ApiOperation("查询代理报表")
    @GetMapping("/find")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageSize", value = "每页大小(默认10条)", required = false),
            @ApiImplicitParam(name = "pageCode", value = "当前页(默认第一页)", required = false),
            @ApiImplicitParam(name = "proxyRole", value = "代理级别1：总代理 2：区域代理 3：基层代理", required = false),
            @ApiImplicitParam(name = "userName", value = "账号", required = false),
            @ApiImplicitParam(name = "tag", value = "1：含下级 0：不包含", required = false),
            @ApiImplicitParam(name = "startDate", value = "起始时间查询", required = false),
            @ApiImplicitParam(name = "endDate", value = "结束时间查询", required = false),
    })
    public ResponseEntity<CompanyProxyReportVo> find(Integer pageSize, Integer pageCode,Integer proxyRole, Integer tag, String userName,
                                                     @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
                                                     @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endDate){
        ProxyUser proxyUser = new ProxyUser();
        Sort sort=Sort.by("proxyRole").ascending();
        sort = sort.and(Sort.by("id").descending());
        Pageable pageable = LoginUtil.setPageable(pageCode, pageSize, sort);
        proxyUser.setProxyRole(proxyRole);
        proxyUser.setIsDelete(CommonConst.NUMBER_1);
        ProxyHomePageReport proxyHomeReport = new  ProxyHomePageReport();
        proxyHomeReport.setProxyRole(proxyRole);
        if (!LoginUtil.checkNull(userName)){
            if (LoginUtil.checkNull(tag)){
                return ResponseUtil.custom("参数不合法");
            }
            ProxyUser byUserName = proxyUserService.findByUserName(userName);
            if (LoginUtil.checkNull(byUserName)){
                return ResponseUtil.success(new PageResultVO());
            }
            if (tag == CommonConst.NUMBER_0){
                proxyUser.setUserName(userName);
                proxyHomeReport.setProxyUserId(byUserName.getId());
            }
            if (tag == CommonConst.NUMBER_1){
                if (byUserName.getProxyRole() == CommonConst.NUMBER_2){
                    proxyUser.setSecondProxy(byUserName.getId());
                    proxyHomeReport.setSecondProxy(byUserName.getId());
                }else if (byUserName.getProxyRole() == CommonConst.NUMBER_1){
                    proxyUser.setFirstProxy(byUserName.getId());
                    proxyHomeReport.setFirstProxy(byUserName.getId());
                }else {
                    proxyUser.setId(byUserName.getId());
                    proxyHomeReport.setProxyUserId(byUserName.getId());
                }
            }
        }
        Page<ProxyUser> proxyUserPage = proxyUserService.findProxyUserPage(pageable,proxyUser,null,null);
        if (LoginUtil.checkNull(proxyUserPage) || proxyUserPage.getContent().size() == CommonConst.NUMBER_0){
            return ResponseUtil.success(new PageResultVO());
        }
        PageResultVO<CompanyProxyReportVo> pageResultVO = new PageResultVO(proxyUserPage);
        List<CompanyProxyReportVo> list = new LinkedList<>();
        try {
            proxyUserPage.getContent().forEach(proxyUser1 -> {
                CompanyProxyReportVo companyProxyReportVo = null;
                if ((LoginUtil.checkNull(startDate,endDate)) || DateUtil.isEffectiveDate(new Date(),startDate,endDate)){
                    try {
                        companyProxyReportVo = this.assemble(proxyUser1);
                    } catch (ParseException e) {
                        log.error("admin代理当日报表统计失败",e);
                    }
                }else {
                    companyProxyReportVo = this.assemble(new CompanyProxyReportVo(),proxyUser1);
                }
                list.add(companyProxyReportVo);
            });
            String startTime = startDate==null? null:DateUtil.getSimpleDateFormat1().format(startDate);
            String endTime =  endDate==null? null:DateUtil.getSimpleDateFormat1().format(endDate);
            List<Long> proxyUserId = list.stream().map(CompanyProxyReportVo::getId).collect(Collectors.toList());
            List<ProxyHomePageReport> proxyHomePageReports = proxyHomePageReportService.findHomePageReports(proxyUserId,proxyHomeReport,startTime,endTime);
            if (LoginUtil.checkNull(proxyHomePageReports) || proxyHomePageReports.size() == CommonConst.NUMBER_0){
                this.getCompanyProxyReportVos(list);
                pageResultVO.setContent(list);
                return ResponseUtil.success(pageResultVO);
            }
            Map<Long, List<ProxyHomePageReport>> firstMap = proxyHomePageReports.stream().collect(Collectors.groupingBy(ProxyHomePageReport::getProxyUserId));
            list.forEach(vo->{
                List<ProxyHomePageReport> proxyHomes = firstMap.get(vo.getId());
                if (!LoginUtil.checkNull(proxyHomes) && proxyHomes.size() > CommonConst.NUMBER_0){
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
            log.error("admin代理报表统计失败",ex);
            return ResponseUtil.custom("查询失败");
        }
        this.getCompanyProxyReportVos(list);
        pageResultVO.setContent(list);
        return ResponseUtil.success(pageResultVO);
    }
    @ApiOperation("每日结算细节")
    @GetMapping("/findDailyDetails")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "当前id", required = false),
            @ApiImplicitParam(name = "userName", value = "账号", required = false),
            @ApiImplicitParam(name = "startDate", value = "起始时间查询", required = false),
            @ApiImplicitParam(name = "endDate", value = "结束时间查询", required = false),
    })
    public ResponseEntity<CompanyProxyReportVo> findDailyDetails(Long id,String userName,@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
                                                                 @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endDate){
        if (LoginUtil.checkNull(id) && LoginUtil.checkNull(userName)  ){
            return ResponseUtil.custom("参数不合法");
        }
        ProxyHomePageReport proxyHomeReport = new  ProxyHomePageReport();
        ProxyUser proxyUser = null;
        if (!LoginUtil.checkNull(id)){
            proxyUser = proxyUserService.findById(id);
            proxyHomeReport.setProxyUserId(proxyUser == null?0L:proxyUser.getId());
        }
        if (!LoginUtil.checkNull(userName)){
            proxyUser = proxyUserService.findByUserName(userName);
            proxyHomeReport.setProxyUserId(proxyUser == null?0L:proxyUser.getId());
        }
        CompanyProxyReportVo companyProxyReportVo = null;
        if ((LoginUtil.checkNull(startDate,endDate)) || DateUtil.isEffectiveDate(new Date(),startDate,endDate)){
            try {
                companyProxyReportVo = this.assemble(proxyUser);
            } catch (ParseException e) {
                log.error("admin代理每日结算细节失败",e);
            }
        }
        Sort sort=Sort.by("id").descending();
        List<CompanyProxyReportVo> list = new LinkedList<>();
        String startTime = startDate==null? null:DateUtil.getSimpleDateFormat1().format(startDate);
        String endTime =  endDate==null? null:DateUtil.getSimpleDateFormat1().format(endDate);
        List<ProxyHomePageReport> proxyHomePageReports = proxyHomePageReportService.findHomePageReports(proxyHomeReport,startTime,endTime,sort);
        if (!LoginUtil.checkNull(companyProxyReportVo)){
            list.add(companyProxyReportVo);
        }
        if (!LoginUtil.checkNull(proxyHomePageReports) && proxyHomePageReports.size() > CommonConst.NUMBER_0){
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
        if (byId.getProxyRole() == CommonConst.NUMBER_1){
            proxyHomePageReportService.getNewSecondProxys(byId,start,end,proxyHomePageReport);
        }
        if (byId.getProxyRole() != CommonConst.NUMBER_3){
            proxyHomePageReportService.getNewThirdProxys(byId,start,end,proxyHomePageReport);
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

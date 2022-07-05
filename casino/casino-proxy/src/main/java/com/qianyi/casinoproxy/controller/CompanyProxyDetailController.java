package com.qianyi.casinoproxy.controller;

import com.qianyi.casinocore.model.GameRecordReportNew;
import com.qianyi.casinocore.model.ProxyUser;
import com.qianyi.casinocore.model.UserRunningWater;
import com.qianyi.casinocore.service.GameRecordReportNewService;
import com.qianyi.casinocore.service.ProxyUserService;
import com.qianyi.casinocore.service.UserRunningWaterService;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinocore.util.CommonUtil;
import com.qianyi.casinocore.vo.CompanyProxyReportVo;
import com.qianyi.casinocore.model.ProxyHomePageReport;
import com.qianyi.casinocore.service.ProxyHomePageReportService;
import com.qianyi.casinocore.vo.PageResultVO;
import com.qianyi.casinoproxy.util.CasinoProxyUtil;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.DateUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
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
@RestController
@Slf4j
@RequestMapping("companyProxyDetail")
public class CompanyProxyDetailController {
    @Autowired
    private ProxyUserService proxyUserService;

    @Autowired
    private ProxyHomePageReportService proxyHomePageReportService;

    @Autowired
    private GameRecordReportNewService gameRecordReportNewService;

    public final static String startStr = " 12:00:00";

    public final static String endStr = " 11:59:59";
    @ApiOperation("查询代理报表")
    @GetMapping("/find")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "pageSize", value = "每页大小(默认10条)", required = false),
        @ApiImplicitParam(name = "pageCode", value = "当前页(默认第一页)", required = false),
        @ApiImplicitParam(name = "proxyRole", value = "代理级别1：总代理 2：区域代理 3：基层代理", required = false),
        @ApiImplicitParam(name = "userName", value = "账号", required = false),
        @ApiImplicitParam(name = "tag", value = "1：含下级 0：不包含", required = false),
        @ApiImplicitParam(name = "startDate", value = "起始时间查询", required = true),
        @ApiImplicitParam(name = "endDate", value = "结束时间查询", required = true),
    })
    public ResponseEntity<CompanyProxyReportVo> find(Integer pageSize, Integer pageCode,Integer proxyRole, Integer tag, String userName,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endDate){
        if (CasinoProxyUtil.checkNull(startDate,endDate)){
            return ResponseUtil.custom("参数不合法");
        }
        ProxyUser proxyUser = new ProxyUser();
        proxyUser.setProxyRole(proxyRole);
        proxyUser.setIsDelete(CommonConst.NUMBER_1);
        Sort sort=Sort.by("proxyRole").ascending();
        sort = sort.and(Sort.by("id").descending());
        Pageable pageable = CasinoProxyUtil.setPageable(pageCode, pageSize, sort);
        if (CasinoProxyUtil.setParameter(proxyUser)){
            return ResponseUtil.custom(CommonConst.NETWORK_ANOMALY);
        }
        if (!CasinoProxyUtil.checkNull(userName)){
            if (CasinoProxyUtil.checkNull(tag)){
                return ResponseUtil.custom("参数不合法");
            }
            ProxyUser byUserName = proxyUserService.findByUserName(userName);
            if (CasinoProxyUtil.checkNull(byUserName)){
                return ResponseUtil.success(new PageResultVO());
            }
            if (tag == CommonConst.NUMBER_0){
                proxyUser.setUserName(userName);
            }
            if (tag == CommonConst.NUMBER_1){
                if (byUserName.getProxyRole() == CommonConst.NUMBER_2){
                    proxyUser.setSecondProxy(byUserName.getId());
                }
            }
        }
        Page<ProxyUser> proxyUserPage = proxyUserService.findProxyUserPage(pageable,proxyUser,null,null);
        if (CasinoProxyUtil.checkNull(proxyUserPage) || proxyUserPage.getContent().size() == CommonConst.NUMBER_0){
            return ResponseUtil.success(new PageResultVO());
        }
        PageResultVO<CompanyProxyReportVo> pageResultVO = new PageResultVO(proxyUserPage);
        List<CompanyProxyReportVo> list = new LinkedList<>();

        String startTime = DateUtil.getSimpleDateFormat1().format(startDate);
        String endTime = DateUtil.getSimpleDateFormat1().format(endDate);

        //偏移12小时
        Date start = cn.hutool.core.date.DateUtil.offsetHour(startDate, 12);
        Date end = cn.hutool.core.date.DateUtil.offsetHour(endDate, 12);

        String startTimeHH = DateUtil.dateToPatten2(start);// yyyy-MM-dd HH 查询对账报表
        String endTimeHH = DateUtil.dateToPatten2(end);
        try {
            proxyUserPage.getContent().forEach(proxyUser1 -> {
                CompanyProxyReportVo companyProxyReportVo = this.assembleData(proxyUser1,start,end,startTime,endTime);
                companyProxyReportVo = this.sumWashCodeChange(proxyUser1, startTimeHH, endTimeHH, companyProxyReportVo);
                list.add(companyProxyReportVo);
            });
            this.getCompanyProxyReportVos(list);
            pageResultVO.setContent(list);
            return ResponseUtil.success(pageResultVO);

        }catch (Exception ex){
            log.error("代理报表统计失败",ex);
            return ResponseUtil.custom("查询失败");
        }
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
        if (CasinoProxyUtil.checkNull(startDate,endDate) ){
            return ResponseUtil.custom("参数不合法");
        }
        ProxyUser proxyUser = null;
        if (!CasinoProxyUtil.checkNull(id)){
            proxyUser = proxyUserService.findById(id);
        }
        if (!CasinoProxyUtil.checkNull(userName)){
            proxyUser = proxyUserService.findByUserName(userName);
        }
        if (CasinoProxyUtil.checkNull(proxyUser)){
            return ResponseUtil.success();
        }
        ProxyUser proxy = proxyUser;
        List<CompanyProxyReportVo> list = new LinkedList<>();
        try {
            Map<Integer,String> mapDate = CommonUtil.findDates("D", startDate, endDate);
            mapDate.forEach((k,today)->{
                Calendar calendar = Calendar.getInstance();
                Date  todayDate = null;
                try {
                    todayDate = DateUtil.getSimpleDateFormat1().parse(today);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                calendar.setTime(todayDate);
                calendar.add(Calendar.DATE, 1);
                String tomorrow = DateUtil.dateToPatten1(calendar.getTime());
                //                System.out.println("昨日"+yesterday+"===========今日"+today);
                try {
                    Date start = DateUtil.getSimpleDateFormat().parse(today+startStr);
                    Date end = DateUtil.getSimpleDateFormat().parse(tomorrow+endStr);
                    CompanyProxyReportVo companyProxyReportVo = this.assembleData(proxy,start,end,today,today);
                    companyProxyReportVo.setStaticsTimes(today);
                    list.add(companyProxyReportVo);
                }catch (ParseException px){
                    px.printStackTrace();
                }
            });
        }catch (Exception ex){
            log.error("proxy代理报表每日明细查询失败",ex);
            return ResponseUtil.custom("查询失败");
        }

        return ResponseUtil.success(list);
    }
    private CompanyProxyReportVo assembleData(ProxyUser byId,Date start,Date end,String startTime,String endTime){
        ProxyHomePageReport proxyHomePageReport = new ProxyHomePageReport();
        proxyHomePageReportService.chargeOrder(byId, start, end, proxyHomePageReport);
        proxyHomePageReportService.withdrawOrder(byId, start, end, proxyHomePageReport);
        //        proxyHomePageReportService.gameRecord(byId, startTime, endTime, proxyHomePageReport);
        proxyHomePageReportService.findGameRecord(byId, startTime, endTime, proxyHomePageReport);
        proxyHomePageReportService.getNewUsers(byId, start, end, proxyHomePageReport);
        if (byId.getProxyRole() == CommonConst.NUMBER_1){
            proxyHomePageReportService.getNewSecondProxys(byId,start,end,proxyHomePageReport);
        }
        if (byId.getProxyRole() != CommonConst.NUMBER_3){
            proxyHomePageReportService.getNewThirdProxys(byId,start,end,proxyHomePageReport);
        }
        CompanyProxyReportVo companyProxyReportVo = new CompanyProxyReportVo();
        BeanUtils.copyProperties(proxyHomePageReport, companyProxyReportVo);
        companyProxyReportVo.setGroupPerformance(proxyHomePageReport.getValidbetAmount());
        companyProxyReportVo.setGroupNewUsers(proxyHomePageReport.getNewUsers());
        companyProxyReportVo.setGroupNewProxyUsers(proxyHomePageReport.getNewSecondProxys() + proxyHomePageReport.getNewThirdProxys());
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
            collect.clear();
            proxyUsers.clear();
        }
        return list;
    }

    private CompanyProxyReportVo sumWashCodeChange(ProxyUser byId, String startTimeHH, String endTimeHH,
        CompanyProxyReportVo companyProxyReportVo) {
        GameRecordReportNew gameRecordReportNew = new GameRecordReportNew();
        if (CommonUtil.setParameter(gameRecordReportNew, byId)) {
            return companyProxyReportVo;
        }
        GameRecordReportNew gameRecordReportNewSum =
            gameRecordReportNewService.findGameRecordReportNewSum(gameRecordReportNew, startTimeHH, endTimeHH);
        companyProxyReportVo
            .setWinLossAmount(companyProxyReportVo.getWinLossAmount().subtract(gameRecordReportNewSum.getAmount()));
        return companyProxyReportVo;
    }
}

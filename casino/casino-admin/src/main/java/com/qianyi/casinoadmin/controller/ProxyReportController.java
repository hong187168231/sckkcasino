package com.qianyi.casinoadmin.controller;

import com.alibaba.fastjson.JSONObject;
import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinoadmin.vo.ProxyReportVo;
import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/proxyReport")
@Api(tags = "人人代管理")
public class ProxyReportController {
    @Autowired
    private UserService userService;

    @Autowired
    private ProxyReportService proxyReportService;

    @Autowired
    private ShareProfitChangeService shareProfitChangeService;

    @Autowired
    private ProxyDayReportService proxyDayReportService;

    @Autowired
    private GameRecordService gameRecordService;

    public final static  SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    public final static  SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public final static String start = " 00:00:00";

    public final static String end = " 23:59:59";

    @ApiOperation("查询人人代报表")
    @GetMapping("/find")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userName", value = "账号", required = true),
            @ApiImplicitParam(name = "startDate", value = "起始时间查询", required = true),
            @ApiImplicitParam(name = "endDate", value = "结束时间查询", required = true),
    })
    public ResponseEntity<ProxyReportVo> find(String userName, @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
                                              @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endDate){
        if (LoginUtil.checkNull(userName,startDate,endDate)){
            return ResponseUtil.custom("参数必填");
        }
        User byAccount = userService.findByAccount(userName);
        if (LoginUtil.checkNull(byAccount)){
            return ResponseUtil.custom("找不到该会员");
        }
        String startTime = format.format(startDate);
        String endTime = format.format(endDate);
        List<ProxyReportVo> list = new LinkedList();
        this.assemble(byAccount,startTime,endTime,list, CommonConst.NUMBER_0,startDate,endDate);
        List<User> firstUsers = userService.findByStateAndFirstPid(Constants.open, byAccount.getId());
        if (!LoginUtil.checkNull(firstUsers) && firstUsers.size() > CommonConst.NUMBER_0){
            firstUsers.forEach(f ->{
                this.assemble(f,startTime,endTime,list, CommonConst.NUMBER_1,startDate,endDate);
            });
            List<User> secondPid = userService.findByStateAndSecondPid(Constants.open, byAccount.getId());
            if (!LoginUtil.checkNull(secondPid) && secondPid.size() > CommonConst.NUMBER_0){
                secondPid.forEach(s ->{
                    this.assemble(s,startTime,endTime,list, CommonConst.NUMBER_2,startDate,endDate);
                });
                List<User> thirdPid = userService.findByStateAndThirdPid(Constants.open, byAccount.getId());
                if (!LoginUtil.checkNull(thirdPid) && thirdPid.size() > CommonConst.NUMBER_0){
                    thirdPid.forEach(t ->{
                        this.assemble(t,startTime,endTime,list, CommonConst.NUMBER_3,startDate,endDate);
                    });
                }
            }
        }
        if (list.size() > CommonConst.NUMBER_0){
            List<Long> userIds = list.stream().map(ProxyReportVo::getFirstPid).collect(Collectors.toList());
            List<User> userList = userService.findAll(userIds);
            list.stream().forEach(proxyReportVo ->{
                userList.stream().forEach(user->{
                    if (user.getId().equals(proxyReportVo.getFirstPid())){
                        proxyReportVo.setFirstPidAccount(user.getAccount());
                    }
                });
            });
        }
        return this.getData(list);
    }
    @ApiOperation("下级明细")
    @GetMapping("/findDetail")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "当前列id", required = true),
            @ApiImplicitParam(name = "startDate", value = "起始时间查询", required = true),
            @ApiImplicitParam(name = "endDate", value = "结束时间查询", required = true),
    })
    public ResponseEntity<ProxyReportVo> findDetail(Long id, @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
                                              @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endDate){
        if (LoginUtil.checkNull(id,startDate,endDate)){
            return ResponseUtil.custom("参数必填");
        }
        User user = userService.findById(id);
        if (LoginUtil.checkNull(user)){
            return ResponseUtil.custom("找不到该会员");
        }
        String startTime = format.format(startDate);
        String endTime = format.format(endDate);
        List<ProxyReportVo> list = new LinkedList();
        this.assemble(user,startTime,endTime,list, CommonConst.NUMBER_1,startDate,endDate);
        List<User> firstUsers = userService.findByStateAndFirstPid(Constants.open, user.getId());
        if (!LoginUtil.checkNull(firstUsers) && firstUsers.size() > CommonConst.NUMBER_0){
            firstUsers.forEach(f ->{
                this.assemble(f,startTime,endTime,list, CommonConst.NUMBER_2,startDate,endDate);
            });
            List<User> secondPid = userService.findByStateAndSecondPid(Constants.open, user.getId());
            if (!LoginUtil.checkNull(secondPid) && secondPid.size() > CommonConst.NUMBER_0){
                secondPid.forEach(s ->{
                    this.assemble(s,startTime,endTime,list, CommonConst.NUMBER_3,startDate,endDate);
                });
            }
        }
        return this.getData(list);
    }
    private ResponseEntity<ProxyReportVo> getData(List<ProxyReportVo> list){
        ProxyReportVo proxyReportVo = new ProxyReportVo();
        proxyReportVo.setPerformance(list.stream().map(ProxyReportVo::getPerformance).reduce(BigDecimal.ZERO, BigDecimal::add));
        proxyReportVo.setContribution(list.stream().map(ProxyReportVo::getContribution).reduce(BigDecimal.ZERO, BigDecimal::add));
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("data", list);
        jsonObject.put("sum", proxyReportVo);
        return ResponseUtil.success(jsonObject);
    }
    @ApiOperation("每日结算细节")
    @GetMapping("/findDayDetail")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "当前列id", required = true),
            @ApiImplicitParam(name = "startDate", value = "起始时间查询", required = true),
            @ApiImplicitParam(name = "endDate", value = "结束时间查询", required = true),
    })
    public ResponseEntity<ProxyReportVo> findDayDetail(Long id, @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
                                                    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endDate){
        if (LoginUtil.checkNull(id,startDate,endDate)){
            return ResponseUtil.custom("参数必填");
        }
        List<String> dateLists = this.findDates("D", startDate, endDate);
        List<ProxyReportVo> list = new LinkedList();
        dateLists.forEach(date ->{
            try {
                this.assemble(id,list,date);
            }catch (ParseException e){

            }
        });
        return ResponseUtil.success(list);
    }
    private void assemble(Long userId,List<ProxyReportVo> list,String date) throws ParseException {
        String startTime = date+start;
        String endTime  = date+end;
        Date startDate = formatter.parse(startTime);
        Date endDate = formatter.parse(endTime);
        ProxyReportVo proxyReportVo = new ProxyReportVo();
        proxyReportVo.setStaticsTimes(date);
        GameRecord gameRecord = gameRecordService.findRecordRecordSum(userId, startTime, endTime);
        proxyReportVo.setPerformance((gameRecord == null || gameRecord.getValidbet() == null) ? BigDecimal.ZERO:new BigDecimal(gameRecord.getValidbet()));
        List<ProxyDayReport> commission = proxyDayReportService.getCommission(userId, date, date);
        BigDecimal allPerformance = commission.stream().map(ProxyDayReport::getGroupBeAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        proxyReportVo.setAllPerformance(allPerformance);
        List<ShareProfitChange> shareProfitChanges = shareProfitChangeService.findAll(userId, startDate, endDate);
        proxyReportVo.setCommission((shareProfitChanges == null || shareProfitChanges.size() == CommonConst.NUMBER_0)? BigDecimal.ZERO:shareProfitChanges.get(CommonConst.NUMBER_0).getProfitRate());
        BigDecimal contribution = shareProfitChanges.stream().map(ShareProfitChange::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        proxyReportVo.setContribution(contribution);
        list.add(proxyReportVo);
    }

    private void assemble(User user,String startTime,String endTime,List<ProxyReportVo> list,Integer tier,Date startDate,Date endDate){
        ProxyReportVo proxyReportVo = new ProxyReportVo();
        if (tier != CommonConst.NUMBER_3){
            ProxyReport byUserId = proxyReportService.findByUserId(user.getId());
            proxyReportVo.setAllGroupNum(byUserId.getAllGroupNum());
        }
        proxyReportVo.setTier(tier);
        GameRecord gameRecord = gameRecordService.findRecordRecordSum(user.getId(), startTime+start, endTime+end);
        proxyReportVo.setPerformance((gameRecord == null || gameRecord.getValidbet() == null) ? BigDecimal.ZERO:new BigDecimal(gameRecord.getValidbet()));
        List<ProxyDayReport> commission = proxyDayReportService.getCommission(user.getId(), startTime, endTime);
        BigDecimal allPerformance = commission.stream().map(ProxyDayReport::getGroupBeAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        proxyReportVo.setAllPerformance(allPerformance);
        List<ShareProfitChange> shareProfitChanges = shareProfitChangeService.findAll(user.getId(), startDate, endDate);
        BigDecimal contribution = shareProfitChanges.stream().map(ShareProfitChange::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        proxyReportVo.setContribution(contribution);
        proxyReportVo.setUserId(user.getId());
        proxyReportVo.setAccount(user.getAccount());
        proxyReportVo.setFirstPid(user.getFirstPid());
        list.add(proxyReportVo);
    }

    public static List<String> findDates(String dateType, Date dBegin, Date dEnd){
        List<String> listDate = new ArrayList<>();
        Calendar calBegin = Calendar.getInstance();
        calBegin.setTime(dBegin);
        Calendar calEnd = Calendar.getInstance();
        calEnd.setTime(dEnd);
        while (calEnd.after(calBegin)) {
            if (calEnd.after(calBegin))
                listDate.add(new SimpleDateFormat("yyyy-MM-dd").format(calBegin.getTime()));
            else
                listDate.add(new SimpleDateFormat("yyyy-MM-dd").format(calEnd.getTime()));
            switch (dateType) {
                case "M":
                    calBegin.add(Calendar.MONTH, 1);
                    break;
                case "D":
                    calBegin.add(Calendar.DAY_OF_YEAR, 1);break;
                case "H":
                    calBegin.add(Calendar.HOUR, 1);break;
                case "N":
                    calBegin.add(Calendar.SECOND, 1);break;
            }
        }
        return listDate;
    }
}

package com.qianyi.casinoproxy.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import com.qianyi.casinocore.exception.BusinessException;
import com.qianyi.casinocore.model.ProxyUser;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.service.ProxyUserService;
import com.qianyi.casinocore.service.ReportService;
import com.qianyi.casinocore.service.UserService;
import com.qianyi.casinocore.util.BillThreadPool;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinocore.util.DTOUtil;
import com.qianyi.casinocore.vo.PageResultVO;
import com.qianyi.casinocore.vo.PersonReportTotalVo;
import com.qianyi.casinocore.vo.PersonReportVo;
import com.qianyi.casinoproxy.util.CasinoProxyUtil;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.annotation.NoAuthorization;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@Api(tags = "报表中心")
@Slf4j
@RestController
@RequestMapping("report")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @Autowired
    private UserService userService;

    @Autowired
    private ProxyUserService proxyUserService;

    private static final BillThreadPool threadPool = new BillThreadPool(CommonConst.NUMBER_10);

    public static final List<String> platforms = new ArrayList<>();

    public static final String seleAllFirst = " and u.first_proxy = ";
    public static final String seleAllSecond = " and u.second_proxy = ";
    public static final String seleAllThird = " and u.third_proxy = ";

    public static final String seleSumFirst = "  where u.first_proxy = ";
    public static final String seleSumSecond = " where u.second_proxy = ";
    public static final String seleSumThird = " where u.third_proxy = ";

    static {
        platforms.add("WM");
        platforms.add("PG");
        platforms.add("CQ9");
        platforms.add(Constants.PLATFORM_OBDJ);
        platforms.add(Constants.PLATFORM_OBTY);
        platforms.add(Constants.PLATFORM_DMC);
        platforms.add(Constants.PLATFORM_DG);
    }

    //    @NoAuthorization
    @ApiOperation("查询个人报表")
    @GetMapping("/queryPersonReport")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "pageSize", value = "每页大小(默认10条)", required = false),
        @ApiImplicitParam(name = "pageCode", value = "当前页(默认第一页)", required = false),
        @ApiImplicitParam(name = "userName", value = "账号", required = false),
        @ApiImplicitParam(name = "startTime", value = "起始时间查询", required = true),
        @ApiImplicitParam(name = "endTime", value = "结束时间查询", required = true),
        @ApiImplicitParam(name = "platform", value = "游戏类别编号 WM、PG、CQ9 ", required = false),
        @ApiImplicitParam(name = "sort", value = "1 正序 2 倒序", required = false),
        @ApiImplicitParam(name = "sort", value = "1 正序 2 倒序", required = false),
        @ApiImplicitParam(name = "tag", value = "1：投注笔数 2：投注金额 3：有效投注 4：洗码发放 5：用户输赢金额", required = false),
    })
    public ResponseEntity<PersonReportVo> queryPersonReport(
        Integer pageSize,
        Integer pageCode,
        String userName,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startTime,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endTime,
        String platform,
        Integer sort,
        Integer tag){
        if (CasinoProxyUtil.checkNull(startTime, endTime, pageSize, pageCode)){
            return ResponseUtil.custom("参数不合法");
        }

        String orderTimeStart = "'"+DateUtil.formatDate(startTime)+"'";
        String orderTimeEnd = "'"+DateUtil.formatDate(endTime)+"'";


        // 向后偏移12小时
        if (CasinoProxyUtil.checkNull(platform) || platform.equals("WM")){
            startTime = DateUtil.offsetHour(startTime, 12);
            endTime = DateUtil.offsetHour(endTime, 12);
        }

        String startTimeStr = DateUtil.formatDateTime(startTime);
        String endTimeStr = DateUtil.formatDateTime(endTime);

        Long authId = CasinoProxyUtil.getAuthId();
        ProxyUser byId = proxyUserService.findById(authId);

        String proxy = "";
        if (byId.getProxyRole() == CommonConst.NUMBER_1){
            proxy = seleAllFirst+byId.getId();
        }else if(byId.getProxyRole() == CommonConst.NUMBER_2){
            proxy = seleAllSecond+byId.getId();
        }else {
            proxy = seleAllThird+byId.getId();
        }

        if(StringUtils.hasLength(userName)){
            User user = userService.findByAccount(userName);
            if(user != null){
                List<PersonReportVo> reportResult = userService.findMapOne(platform,startTimeStr,endTimeStr,user.getId(),orderTimeStart,orderTimeEnd,proxy);
                PageResultVO<PersonReportVo> mapPageResultVO = combinePage(reportResult, 1, pageCode, pageSize);
                return ResponseUtil.success(getMap(mapPageResultVO));
            }
            List<PersonReportVo> emptyResult = new ArrayList<>();
            return ResponseUtil.success(combinePage(emptyResult, 0, pageCode, pageSize));
        }

        int page = (pageCode-1)*pageSize;
        List<PersonReportVo> reportResult = null;

        try {
            String statement = getOrderByStatement(tag, sort);
            //            reportResult = userService.findMap(platform, startTimeStr, endTimeStr, page, pageSize, statement,orderTimeStart,orderTimeEnd,proxy);

            if (Objects.nonNull(tag) && tag == CommonConst.NUMBER_4){
                reportResult = this.findWashOrderBy(platform,startTimeStr,endTimeStr,page,pageSize,statement,orderTimeStart,orderTimeEnd,proxy);
            }else {
                reportResult = this.findBetOrderBy(platform,startTimeStr,endTimeStr,page,pageSize,statement,orderTimeStart,orderTimeEnd,proxy);
            }
        } catch (Exception e) {
            log.error("查询失败", e);
            return ResponseUtil.custom("查询失败");
        }

        int totalElement = CommonConst.NUMBER_0;
        if (byId.getProxyRole() == CommonConst.NUMBER_1){
            totalElement = reportService.queryAllByFirst(byId.getId());
        }else if(byId.getProxyRole() == CommonConst.NUMBER_2){
            totalElement = reportService.queryAllBySecond(byId.getId());
        }else {
            totalElement = reportService.queryAllByThird(byId.getId());
        }
        PageResultVO<PersonReportVo> mapPageResultVO = combinePage(reportResult, totalElement, pageCode, pageSize);
        return ResponseUtil.success(getMap(mapPageResultVO));
    }

    private List<PersonReportVo> findWashOrderBy(String platform, String startTimeStr,
        String endTimeStr, Integer page, Integer pageSize, String statement, String orderTimeStart, String orderTimeEnd,String proxy) {
        List<PersonReportVo> reportResult = userService.findMapWash(platform, startTimeStr, endTimeStr, page, pageSize, statement, proxy);
        if (CollUtil.isNotEmpty(reportResult)) {
            ReentrantLock reentrantLock = new ReentrantLock();
            Condition condition = reentrantLock.newCondition();
            AtomicInteger atomicInteger = new AtomicInteger(reportResult.size());
            Vector<PersonReportVo> list = new Vector<>();
            for (PersonReportVo per : reportResult) {
                threadPool.execute(() -> {
                    try {
                        PersonReportVo vo =
                            userService.findMapWash(platform, startTimeStr, endTimeStr, per.getId().toString(),orderTimeStart,orderTimeEnd);
                        per.setNum(vo.getNum());
                        per.setBetAmount(vo.getBetAmount());
                        per.setValidbet(vo.getValidbet());
                        per.setWinLoss(vo.getWinLoss());
                        per.setServiceCharge(vo.getServiceCharge());
                        per.setAllProfitAmount(vo.getAllProfitAmount());
                        per.setAllWater(vo.getAllWater());
                        BigDecimal avgBenefit = per.getWinLoss().add(per.getWashAmount()).add(per.getAllWater());
                        per.setAvgBenefit(avgBenefit.negate());
                        per.setTotalAmount(
                            per.getAvgBenefit().subtract(per.getAllProfitAmount()).add(per.getServiceCharge()));
                        list.add(per);
                    } catch (Exception ex) {
                        log.error("异步查询报表异常", ex);
                    } finally {
                        atomicInteger.decrementAndGet();
                        BillThreadPool.toResume(reentrantLock, condition);
                    }
                });
            }
            BillThreadPool.toWaiting(reentrantLock, condition, atomicInteger);
            reportResult = list;
            Collections.sort(reportResult, (o1, o2) -> -o2.getSort().compareTo(o1.getSort()));
        }
        return reportResult;
    }

    private List<PersonReportVo> findBetOrderBy(String platform, String startTimeStr,
        String endTimeStr, Integer page, Integer pageSize, String statement, String orderTimeStart, String orderTimeEnd,String proxy) {
        List<PersonReportVo> reportResult = userService.findMapBet(platform, startTimeStr, endTimeStr, page, pageSize, statement,
            orderTimeStart, orderTimeEnd, proxy);
        if (CollUtil.isNotEmpty(reportResult)) {
            ReentrantLock reentrantLock = new ReentrantLock();
            Condition condition = reentrantLock.newCondition();
            AtomicInteger atomicInteger = new AtomicInteger(reportResult.size());
            Vector<PersonReportVo> list = new Vector<>();
            for (PersonReportVo per : reportResult) {
                threadPool.execute(() -> {
                    try {
                        PersonReportVo vo =
                            userService.findMapBet(platform, startTimeStr, endTimeStr, per.getId().toString());
                        per.setWashAmount(vo.getWashAmount());
                        per.setServiceCharge(vo.getServiceCharge());
                        per.setAllProfitAmount(vo.getAllProfitAmount());
                        per.setAllWater(vo.getAllWater());
                        BigDecimal avgBenefit = per.getWinLoss().add(per.getWashAmount()).add(per.getAllWater());
                        per.setAvgBenefit(avgBenefit.negate());
                        per.setTotalAmount(
                            per.getAvgBenefit().subtract(per.getAllProfitAmount()).add(per.getServiceCharge()));
                        list.add(per);
                    } catch (Exception ex) {
                        log.error("异步查询报表异常", ex);
                    } finally {
                        atomicInteger.decrementAndGet();
                        BillThreadPool.toResume(reentrantLock, condition);
                    }
                });
            }
            BillThreadPool.toWaiting(reentrantLock, condition, atomicInteger);
            reportResult = list;
            Collections.sort(reportResult, (o1, o2) -> -o2.getSort().compareTo(o1.getSort()));
        }
        return reportResult;
    }

    // 获取 order by 语句
    private String getOrderByStatement(Integer tag, Integer sort){
        if (CasinoProxyUtil.checkNull(tag)) {
            return "";
        }
        String str = "ORDER BY {0} ";
        switch (tag) {
            case 1:
                str = MessageFormat.format(str,"num");
                break;
            case 2:
                str = MessageFormat.format(str,"bet_amount");
                break;
            case 3:
                str = MessageFormat.format(str,"validbet");
                break;
            case 4:
                str = MessageFormat.format(str,"wash_amount");
                break;
            case 5:
                str = MessageFormat.format(str,"win_loss");
                break;
            default:
                throw new BusinessException("参数不合法");
        }
        if (CasinoProxyUtil.checkNull(sort) || sort == CommonConst.NUMBER_1){
            str = str + "ASC";
        }else {
            str = str + "DESC";
        }
        return str;
    }

    private PageResultVO<PersonReportVo> getMap(PageResultVO<PersonReportVo> mapPageResultVO){
        List<PersonReportVo> content = (List<PersonReportVo>) mapPageResultVO.getContent();

        if (!CasinoProxyUtil.checkNull(content) && content.size() > CommonConst.NUMBER_0){
            for (PersonReportVo item: content){
                if (item.getThirdProxy() == null){
                    continue;
                }
                ProxyUser third_proxy = proxyUserService.findById(Long.parseLong(item.getThirdProxy()));
                item.setThirdProxyName(third_proxy==null?"":third_proxy.getUserName());
            }
        }
        return mapPageResultVO;
    }

    private PageResultVO<PersonReportVo> combinePage(List<PersonReportVo> reportResult, int totalElement, int page, int num){
        PageResultVO<PersonReportVo> pageResult = new PageResultVO<>(page, num, Long.parseLong(totalElement+""), reportResult);
        return pageResult;
    }

    //    @NoAuthorization
    @ApiOperation("查询个人报表总计")
    @GetMapping("/queryTotal")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "platform", value = "游戏类别编号 WM、PG、CQ9 ", required = false),
        @ApiImplicitParam(name = "userName", value = "账号", required = false),
        @ApiImplicitParam(name = "startDate", value = "起始时间查询", required = false),
        @ApiImplicitParam(name = "endDate", value = "结束时间查询", required = false),
    })
    public ResponseEntity<PersonReportTotalVo> queryTotal(
        String userName,
        String platform,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endDate){
        if (CasinoProxyUtil.checkNull(startDate,endDate)){
            return ResponseUtil.custom("参数不合法");
        }

        String orderTimeStart = "'"+DateUtil.formatDate(startDate)+"'";
        String orderTimeEnd = "'"+DateUtil.formatDate(endDate)+"'";

        // 向后偏移12小时
        if (CasinoProxyUtil.checkNull(platform) || platform.equals("WM")){
            startDate = DateUtil.offsetHour(startDate, 12);
            endDate = DateUtil.offsetHour(endDate, 12);
        }
        String startTime = DateUtil.formatDateTime(startDate);
        String endTime = DateUtil.formatDateTime(endDate);
        String proxy = "";
        Long authId = CasinoProxyUtil.getAuthId();
        ProxyUser byId = proxyUserService.findById(authId);
        Long userId=null;
        PersonReportTotalVo itemObject = null;
        if(StringUtils.hasLength(userName)){
            User user = userService.findByAccount(userName);
            if(user != null){

                if (byId.getProxyRole() == CommonConst.NUMBER_1){
                    proxy = seleAllFirst+byId.getId();
                }else if(byId.getProxyRole() == CommonConst.NUMBER_2){
                    proxy = seleAllSecond+byId.getId();
                }else {
                    proxy = seleAllThird+byId.getId();
                }
                userId=user.getId();
                List<PersonReportVo> maps = userService.findMapOne(platform, startTime, endTime, userId,orderTimeStart,orderTimeEnd,proxy);
                itemObject = DTOUtil.toDTO(maps.get(0), PersonReportTotalVo.class);
            }
        }else {
            if (byId.getProxyRole() == CommonConst.NUMBER_1){
                proxy = seleSumFirst+byId.getId();
            }else if(byId.getProxyRole() == CommonConst.NUMBER_2){
                proxy = seleSumSecond+byId.getId();
            }else {
                proxy = seleSumThird+byId.getId();
            }
            Map<String,Object> result = userService.findMapSum(platform,startTime,endTime,orderTimeStart,orderTimeEnd,proxy);
            itemObject = DTOUtil.toDTO(result, PersonReportTotalVo.class);
        }
        return ResponseUtil.success(itemObject);
    }

    /*private HistoryTotal getHistoryItem(Map<String,Object> result){
        HistoryTotal historyTotal = new HistoryTotal();
        historyTotal.setAll_profit_amount(new BigDecimal(result.get("all_profit_amount").toString()).setScale(2, RoundingMode.HALF_UP));
        historyTotal.setService_charge(new BigDecimal(result.get("service_charge").toString()).setScale(2, RoundingMode.HALF_UP));
        historyTotal.setTotal_amount(new BigDecimal(result.get("total_amount").toString()).setScale(2, RoundingMode.HALF_UP));
        historyTotal.setNum(Integer.parseInt(result.get("num").toString()));
        historyTotal.setBet_amount(new BigDecimal(result.get("bet_amount").toString()).setScale(2, RoundingMode.HALF_UP));
        historyTotal.setWin_loss(new BigDecimal(result.get("win_loss").toString()).setScale(2, RoundingMode.HALF_UP));
        historyTotal.setValidbet(new BigDecimal(result.get("validbet").toString()).setScale(2, RoundingMode.HALF_UP));
        historyTotal.setWash_amount(new BigDecimal(result.get("wash_amount").toString()).setScale(2, RoundingMode.HALF_UP));
        historyTotal.setAvg_benefit(new BigDecimal(result.get("avg_benefit").toString()).setScale(2, RoundingMode.HALF_UP));
        return historyTotal;
    }*/

    @ApiOperation("查询平台列表")
    @GetMapping("/getData")
    @NoAuthorization
    public ResponseEntity getData(){
        return ResponseUtil.success(platforms);
    }
}

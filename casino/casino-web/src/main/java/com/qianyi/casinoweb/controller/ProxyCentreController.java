package com.qianyi.casinoweb.controller;

import com.qianyi.casinocore.enums.AccountChangeEnum;
import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.ProxyDayReportService;
import com.qianyi.casinocore.service.ProxyReportService;
import com.qianyi.casinocore.service.UserMoneyService;
import com.qianyi.casinocore.service.UserService;
import com.qianyi.casinocore.vo.AccountChangeVo;
import com.qianyi.casinoweb.util.CasinoWebUtil;
import com.qianyi.casinoweb.vo.ProxyCentreVo;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.executor.AsyncService;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.DateUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("proxyCentre")
@Api(tags = "代理中心")
public class ProxyCentreController {

    @Autowired
    private UserService userService;
    @Autowired
    private UserMoneyService userMoneyService;
    @Autowired
    @Qualifier("accountChangeJob")
    private AsyncService asyncService;
    @Autowired
    private ProxyDayReportService proxyDayReportService;
    @Autowired
    private ProxyReportService proxyReportService;


    @ApiOperation("查询今日，昨日，本周佣金")
    @GetMapping("/getCommission")
    public ResponseEntity<ProxyCentreVo> getCommission() {
        //获取登陆用户
        Long userId = CasinoWebUtil.getAuthId();
        ProxyCentreVo vo = new ProxyCentreVo();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 0);
        String today = formatter.format(calendar.getTime());
        List<ProxyDayReport> todayData = proxyDayReportService.getCommission(userId, today, null);
        BigDecimal defaultVal = BigDecimal.ZERO.setScale(2);
        if (CollectionUtils.isEmpty(todayData)) {
            vo.setTodayCommission(defaultVal);
        } else {
            BigDecimal todayCommission = todayData.get(0).getProfitAmount() == null ? defaultVal : todayData.get(0).getProfitAmount();
            vo.setTodayCommission(todayCommission);
        }
        //昨日佣金
        calendar.add(Calendar.DATE, -1);
        String yesterday = formatter.format(calendar.getTime());
        List<ProxyDayReport> yesterdayData = proxyDayReportService.getCommission(userId, yesterday, null);
        if (CollectionUtils.isEmpty(yesterdayData)) {
            vo.setYesterdayCommission(defaultVal);
        } else {
            BigDecimal yesterdayCommission = yesterdayData.get(0).getProfitAmount() == null ? defaultVal : yesterdayData.get(0).getProfitAmount();
            vo.setYesterdayCommission(yesterdayCommission);
        }
        //本周佣金
        Date weekStartDate = DateUtil.getWeekStartDate();
        String weekStart = formatter.format(weekStartDate);
        Date weekEndDate = DateUtil.getWeekEndDate();
        String weekEnd = formatter.format(weekEndDate);
        List<ProxyDayReport> weekData = proxyDayReportService.getCommission(userId, weekStart, weekEnd);
        if (CollectionUtils.isEmpty(weekData)) {
            vo.setWeekCommission(defaultVal);
        } else {
            BigDecimal weekSum = weekData.stream().filter(item -> item.getProfitAmount() != null).map(ProxyDayReport::getProfitAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            vo.setWeekCommission(weekSum);
        }
        return ResponseUtil.success(vo);
    }

    @ApiOperation("我的团队")
    @GetMapping("/myTeam")
    public ResponseEntity<ProxyCentreVo.MyTeam> myTeam() {
        //获取登陆用户
        Long userId = CasinoWebUtil.getAuthId();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String today = formatter.format(new Date());
        BigDecimal defaultVal = BigDecimal.ZERO.setScale(2);
        ProxyCentreVo.MyTeam myTeam = new ProxyCentreVo.MyTeam();
        List<ProxyDayReport> todayData = proxyDayReportService.getCommission(userId, today, null);
        if (CollectionUtils.isEmpty(todayData)) {
            myTeam.setDeppositeAmount(defaultVal);
            myTeam.setBetAmount(defaultVal);
            myTeam.setNewNum(0);
        } else {
            ProxyDayReport proxyDayReport = todayData.get(0);
            BigDecimal deppositeAmount = proxyDayReport.getDeppositeAmount() == null ? defaultVal : proxyDayReport.getDeppositeAmount();
            myTeam.setDeppositeAmount(deppositeAmount);
            BigDecimal betAmount = proxyDayReport.getBetAmount() == null ? defaultVal : proxyDayReport.getBetAmount();
            myTeam.setBetAmount(betAmount);
            int newNum = proxyDayReport.getNewNum() == null ? 0 : proxyDayReport.getNewNum();
            myTeam.setNewNum(newNum);
        }
        ProxyReport proxyReport = proxyReportService.findByUserId(userId);
        if (proxyReport == null) {
            myTeam.setAllGroupNum(0);
            myTeam.setDirectGroupNum(0);
        } else {
            int allGroupNum = proxyReport.getAllGroupNum() == null ? 0 : proxyReport.getAllGroupNum();
            int directGroupNum = proxyReport.getDirectGroupNum() == null ? 0 : proxyReport.getDirectGroupNum();
            myTeam.setAllGroupNum(allGroupNum);
            myTeam.setDirectGroupNum(directGroupNum);
        }
        return ResponseUtil.success(myTeam);
    }

    @ApiOperation("代理报表")
    @GetMapping("/proxyReport")
    public ResponseEntity<ProxyReport> proxyReport() {
        //获取登陆用户
        Long userId = CasinoWebUtil.getAuthId();
        ProxyReport proxyReport = proxyReportService.findByUserId(userId);
        if (proxyReport != null) {
            return ResponseUtil.success(proxyReport);
        }
        return ResponseUtil.success(new ProxyReport());
    }

    @ApiOperation("业绩查询")
    @GetMapping("/findAchievementPage")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageSize", value = "每页大小(默认10条)", required = false),
            @ApiImplicitParam(name = "pageCode", value = "当前页(默认第一页)", required = false),
            @ApiImplicitParam(name = "memberId", value = "会员ID", required = false),
    })
    public ResponseEntity<ProxyReport> findAchievementPage(Integer pageSize, Integer pageCode,Long memberId) {
        //获取登陆用户
        Long userId = CasinoWebUtil.getAuthId();
        Sort sort = Sort.by("allBetAmount").descending();
        Pageable pageable = CasinoWebUtil.setPageable(pageCode, pageSize, sort);
        Page<ProxyReport> list = proxyReportService.findAchievementPage(pageable, userId,memberId);
        return ResponseUtil.success(list);
    }

    @ApiOperation("用户领取分润金额")
    @GetMapping("/receiveShareProfit")
    @Transactional
    public ResponseEntity<AccountChangeVo> receiveWashCode() {
        //获取登陆用户
        Long userId = CasinoWebUtil.getAuthId();
        UserMoney userMoney = userMoneyService.findUserByUserIdUseLock(userId);
        BigDecimal shareProfit = BigDecimal.ZERO;
        if (userMoney != null && userMoney.getShareProfit() != null) {
            shareProfit = userMoney.getShareProfit();
        }
        if (shareProfit.compareTo(BigDecimal.ONE) == -1) {
            return ResponseUtil.custom("金额小于1,不能领取");
        }
        userMoneyService.addMoney(userId, shareProfit);
        userMoneyService.subShareProfit(userId, shareProfit);

        AccountChangeVo vo = new AccountChangeVo();
        vo.setUserId(userId);
        vo.setChangeEnum(AccountChangeEnum.SHARE_PROFIT);
        vo.setAmount(shareProfit);
        vo.setAmountBefore(userMoney.getMoney());
        vo.setAmountAfter(userMoney.getMoney().add(shareProfit));
        asyncService.executeAsync(vo);
        return ResponseUtil.success();
    }
}

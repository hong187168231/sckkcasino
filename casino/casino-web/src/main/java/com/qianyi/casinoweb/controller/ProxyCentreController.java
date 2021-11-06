package com.qianyi.casinoweb.controller;

import com.qianyi.casinocore.enums.AccountChangeEnum;
import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.vo.AccountChangeVo;
import com.qianyi.casinoweb.util.CasinoWebUtil;
import com.qianyi.casinoweb.vo.ProxyCentreVo;
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
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
    @Autowired
    private ShareProfitChangeService shareProfitChangeService;


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
        if (!CollectionUtils.isEmpty(todayData)) {
            vo.setTodayCommission(todayData.get(0).getProfitAmount());
        }
        //昨日佣金
        calendar.add(Calendar.DATE, -1);
        String yesterday = formatter.format(calendar.getTime());
        List<ProxyDayReport> yesterdayData = proxyDayReportService.getCommission(userId, yesterday, null);
        if (!CollectionUtils.isEmpty(yesterdayData)) {
            vo.setYesterdayCommission(yesterdayData.get(0).getProfitAmount());
        }
        //本周佣金
        Date weekStartDate = DateUtil.getWeekStartDate();
        String weekStart = formatter.format(weekStartDate);
        Date weekEndDate = DateUtil.getWeekEndDate();
        String weekEnd = formatter.format(weekEndDate);
        List<ProxyDayReport> weekData = proxyDayReportService.getCommission(userId, weekStart, weekEnd);
        if (!CollectionUtils.isEmpty(weekData)) {
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
        ProxyCentreVo.MyTeam myTeam = new ProxyCentreVo.MyTeam();
        List<ProxyDayReport> todayData = proxyDayReportService.getCommission(userId, today, null);
        if (!CollectionUtils.isEmpty(todayData)) {
            ProxyDayReport proxyDayReport = todayData.get(0);
            myTeam.setDeppositeAmount(proxyDayReport.getDeppositeAmount());
            myTeam.setBetAmount(proxyDayReport.getBetAmount());
            int newNum = proxyDayReport.getNewNum() == null ? 0 : proxyDayReport.getNewNum();
            myTeam.setNewNum(newNum);
        }
        ProxyReport proxyReport = proxyReportService.findByUserId(userId);
        if (proxyReport != null) {
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
    @GetMapping("/findAchievementList")
    @ApiImplicitParams({@ApiImplicitParam(name = "account", value = "会员账号", required = false),
    })
    public ResponseEntity<List<ProxyCentreVo.ShareProfit>> findAchievementList(String account) {
        //获取登陆用户
        Long userId = CasinoWebUtil.getAuthId();
        //查询所有直属
        List<User> users = null;
        if (ObjectUtils.isEmpty(account)) {
            users = userService.findFirstUser(userId);
        } else {
            users = userService.findByFirstPidAndAccount(userId, account);
        }
        List<ProxyCentreVo.ShareProfit> dataList = new ArrayList<>();
        if (CollectionUtils.isEmpty(users)) {
            return ResponseUtil.success(dataList);
        }
        //查询直属总贡献
        List<ShareProfitChange> directList = shareProfitChangeService.getShareProfitList(userId, 1, users);
        //查询两级附属
        for (ShareProfitChange direct : directList) {
            ProxyCentreVo.ShareProfit shareProfit = new ProxyCentreVo.ShareProfit();
            shareProfit.setUserId(direct.getFromUserId());
            shareProfit.setDirectProfitAmount(direct.getAmount());
            shareProfit.setDirectBetAmount(direct.getValidbet());
            //第一级附属
            List<ShareProfitChange> subsidiary1 = shareProfitChangeService.getShareProfitList(direct.getFromUserId(), 1, null);
            //第一级附属总贡献
            BigDecimal subsidiary1Sum = subsidiary1.stream().filter(item -> item.getAmount() != null).map(ShareProfitChange::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            //第二级附属总贡献
            BigDecimal subsidiary2Sum = BigDecimal.ZERO;
            for (ShareProfitChange change2 : subsidiary1) {
                List<ShareProfitChange> subsidiary2List = shareProfitChangeService.getShareProfitList(change2.getFromUserId(), 1, null);
                BigDecimal subsidiary2 = subsidiary2List.stream().filter(item -> item.getAmount() != null).map(ShareProfitChange::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                subsidiary2Sum = subsidiary2Sum.add(subsidiary2);
            }
            shareProfit.setOtherProfitAmount(subsidiary1Sum.add(subsidiary2Sum));
            dataList.add(shareProfit);
        }
        //没有数据的直属默认显示0
        for (User user : users) {
            boolean flag = true;
            for (ProxyCentreVo.ShareProfit shareProfit : dataList) {
                if (user.getId() == shareProfit.getUserId()) {
                    shareProfit.setAccount(user.getAccount());
                    flag = false;
                    break;
                }
            }
            if (flag) {
                ProxyCentreVo.ShareProfit shareProfit = new ProxyCentreVo.ShareProfit();
                shareProfit.setUserId(user.getId());
                shareProfit.setAccount(user.getAccount());
                dataList.add(shareProfit);
            }
        }
        return ResponseUtil.success(dataList);
    }

    @ApiOperation("用户领取分润金额")
    @GetMapping("/receiveShareProfit")
    @Transactional
    public ResponseEntity<String> receiveWashCode() {
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
        return ResponseUtil.success("成功领取金额：" + shareProfit);
    }
}

package com.qianyi.casinoproxy.controller;

import com.qianyi.casinocore.model.GameRecordReportNew;
import com.qianyi.casinocore.model.ProxyUser;
import com.qianyi.casinocore.service.GameRecordReportNewService;
import com.qianyi.casinocore.service.ProxyUserService;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinocore.util.UserPasswordUtil;
import com.qianyi.casinocore.vo.GameRecordReportTotalVo;
import com.qianyi.casinocore.vo.GameRecordReportVo;
import com.qianyi.casinocore.vo.PageResultVO;
import com.qianyi.casinoproxy.util.CasinoProxyUtil;
import com.qianyi.modulecommon.annotation.NoAuthentication;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Api(tags = "代理实时报表")
@Slf4j
@RestController
@RequestMapping("agentInTimeReport")
public class InTimeReportController {

    @Autowired
    private GameRecordReportNewService gameRecordReportNewService;

    @Autowired
    private ProxyUserService proxyUserService;

    @NoAuthentication
    @ApiOperation("查询代理报表")
    @GetMapping("/find")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "pageSize", value = "每页大小(默认10条)", required = false),
        @ApiImplicitParam(name = "pageCode", value = "当前页(默认第一页)", required = false),
        @ApiImplicitParam(name = "userName", value = "代理账号", required = false),
        @ApiImplicitParam(name = "agentMark", value = "代理标识", required = false),
        @ApiImplicitParam(name = "agentId", value = "总代id", required = false),
        @ApiImplicitParam(name = "currentAgentId", value = "当前登录代理id", required = true),
        @ApiImplicitParam(name = "platform", value = "游戏类别编号 WM、PG、CQ9 ", required = false),
        @ApiImplicitParam(name = "startDate", value = "起始时间查询", required = true),
        @ApiImplicitParam(name = "endDate", value = "结束时间查询", required = true),
    })
    public ResponseEntity<GameRecordReportVo> find(Integer pageSize, Integer pageCode, String userName,Boolean agentMark,Integer agentId, Long currentAgentId, String platform, @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endDate){
        if (CasinoProxyUtil.checkNull(startDate,endDate,agentMark)){
            return ResponseUtil.custom("参数不合法");
        }
        Sort sort = Sort.by("id").descending();
        Pageable pageable = CasinoProxyUtil.setPageable(pageCode, pageSize, sort);
        if (agentMark){
            pageable = PageRequest.of(0, 1000,sort);
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        calendar.add(Calendar.HOUR, 12);
        startDate = calendar.getTime();
        String startTime = DateUtil.dateToPatten2(startDate);
        calendar.setTime(endDate);
        calendar.add(Calendar.HOUR, 12);
        endDate = calendar.getTime();
        String endTime = DateUtil.dateToPatten2(endDate);
        GameRecordReportNew gameRecordReport = new GameRecordReportNew();
        gameRecordReport.setPlatform(platform);
        Long proxyId = null;
        Integer proxyRole = null;
        Integer currentRole = null;
        Boolean mark = true;
        Long firstProxy = 0l;
        if (!CasinoProxyUtil.checkNull(userName)){
            ProxyUser byUserName = proxyUserService.findByUserName(userName);
            if (CasinoProxyUtil.checkNull(byUserName)){
                return ResponseUtil.success(new PageResultVO());
            }
            proxyId = byUserName.getId();
            proxyRole = byUserName.getProxyRole();
            firstProxy=byUserName.getFirstProxy();
            if( (agentMark && proxyRole== CommonConst.NUMBER_2) || proxyRole== CommonConst.NUMBER_3){
                mark=false;
            }
        }
        ProxyUser  proxyUserId = proxyUserService.findById(currentAgentId);
        currentRole=proxyUserId.getProxyRole();

        Page<GameRecordReportNew> gameRecordReportPage = gameRecordReportNewService.findGameRecordReportPageProxy(pageable, gameRecordReport, startTime, endTime,proxyId,proxyRole,agentMark,agentId , currentRole,currentAgentId);
        PageResultVO<GameRecordReportVo> pageResultVO = new PageResultVO(gameRecordReportPage);
        List<GameRecordReportNew> gameRecordReports = gameRecordReportPage.getContent();
        if(!CasinoProxyUtil.checkNull(gameRecordReports) && gameRecordReports.size() > 0){
            List<GameRecordReportVo> gameRecordReportVos = new LinkedList();
            List<Long> firsts = gameRecordReports.stream().map(GameRecordReportNew::getFirstProxy).collect(Collectors.toList());
            List<ProxyUser> proxyUsers = proxyUserService.findProxyUser(firsts);
            Boolean finalMark = mark;
            Long finalFirstProxy = firstProxy;
            gameRecordReports.stream().forEach(gameRecordReport1 -> {
                GameRecordReportVo vo = new GameRecordReportVo();
                vo.setTopLevelFirstProxy(finalFirstProxy == 0 ?gameRecordReport1.getFirstProxy(): finalFirstProxy);
                vo.setHasChildren(finalMark);
                BeanUtils.copyProperties(gameRecordReport1,vo);
                vo.setAmount(gameRecordReport1.getNewAmount() != null? gameRecordReport1.getNewAmount().setScale(2, RoundingMode.HALF_UP):
                    BigDecimal.ZERO);
                vo.setUserAmount(gameRecordReport1.getNewUserAmount() != null? gameRecordReport1.getNewUserAmount().setScale(2, RoundingMode.HALF_UP):
                    BigDecimal.ZERO);
                vo.setSurplusAmount(gameRecordReport1.getNewSurplusAmount() != null? gameRecordReport1.getNewSurplusAmount().setScale(2, RoundingMode.HALF_UP):
                    BigDecimal.ZERO);
                vo.setTotalWinLossAmount(vo.getWinLossAmount().add(vo.getAmount()).add(vo.getUserAmount()).add(vo.getSurplusAmount()).add(vo.getTodayAward()).add(vo.getRiseAward()));
                vo.setAccountId(gameRecordReport1.getId());
                vo.setTag(UserPasswordUtil.getRandomPwd());
                proxyUsers.stream().forEach(proxyUser -> {
                    if (gameRecordReport1.getFirstProxy().equals(proxyUser.getId())){
                        vo.setAccount(proxyUser.getUserName());
                        vo.setAccountId(proxyUser.getId());
                    }
                });
                gameRecordReportVos.add(vo);
            });
            pageResultVO.setContent(gameRecordReportVos);
        }
        return ResponseUtil.success(pageResultVO);
    }

    @ApiOperation("查询代理报表统计")
    @GetMapping("/findSum")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "userName", value = "代理账号", required = false),
        @ApiImplicitParam(name = "currentAgentId", value = "当前登录代理id", required = true),
        //            @ApiImplicitParam(name = "gid", value = "游戏类别编号 百家乐:101 龙虎:102 轮盘:103 骰宝:104 牛牛:105 番摊:107 色碟:108 鱼虾蟹:110 炸金花:111 安达巴哈:128", required = false),
        @ApiImplicitParam(name = "platform", value = "游戏类别编号 WM、PG、CQ9 ", required = false),
        @ApiImplicitParam(name = "startDate", value = "起始时间查询", required = true),
        @ApiImplicitParam(name = "endDate", value = "结束时间查询", required = true),
    })
    @NoAuthentication
    public ResponseEntity findSum( String userName,Long currentAgentId,String platform,@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endDate){
        if (CasinoProxyUtil.checkNull(startDate) ||  CasinoProxyUtil.checkNull(endDate)){
            return ResponseUtil.custom("参数不合法");
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        calendar.add(Calendar.HOUR, 12);
        startDate = calendar.getTime();
        String startTime = DateUtil.dateToPatten2(startDate);
        calendar.setTime(endDate);
        calendar.add(Calendar.HOUR, 12);
        endDate = calendar.getTime();
        String endTime = DateUtil.dateToPatten2(endDate);
        GameRecordReportNew gameRecordReport = new GameRecordReportNew();
        gameRecordReport.setPlatform(platform);
        Long proxyId = null;
        Integer proxyRole = null;
        if (!CasinoProxyUtil.checkNull(userName)){
            ProxyUser byUserName = proxyUserService.findByUserName(userName);
            if (CasinoProxyUtil.checkNull(byUserName)){
                return ResponseUtil.success(new PageResultVO());
            }
            proxyId = byUserName.getId();
            proxyRole = byUserName.getProxyRole();
        }
        ProxyUser  proxyUserId = proxyUserService.findById(currentAgentId);
        Integer  currentRole=proxyUserId.getProxyRole();

        GameRecordReportNew recordRecordSum = gameRecordReportNewService.findRecordRecordSumProxy(gameRecordReport, startTime, endTime, proxyId, proxyRole,currentRole,currentAgentId);
        if (!CasinoProxyUtil.checkNull(recordRecordSum)){
            recordRecordSum.setAmount(recordRecordSum.getNewAmount() != null? recordRecordSum.getNewAmount().setScale(2, RoundingMode.HALF_UP):
                BigDecimal.ZERO);
            recordRecordSum.setUserAmount(recordRecordSum.getNewUserAmount() != null? recordRecordSum.getNewUserAmount().setScale(2, RoundingMode.HALF_UP):
                BigDecimal.ZERO);
            recordRecordSum.setSurplusAmount(recordRecordSum.getNewSurplusAmount() != null? recordRecordSum.getNewSurplusAmount().setScale(2, RoundingMode.HALF_UP):
                BigDecimal.ZERO);
        }
        GameRecordReportTotalVo gameRecordReportTotalVo=new GameRecordReportTotalVo();
        gameRecordReportTotalVo.setBetAmount(recordRecordSum.getBetAmount() != null? recordRecordSum.getBetAmount() : BigDecimal.ZERO);
        gameRecordReportTotalVo.setBettingNumber(recordRecordSum.getBettingNumber()!= null? recordRecordSum.getBettingNumber() : CommonConst.NUMBER_0);
        gameRecordReportTotalVo.setValidAmount(recordRecordSum.getValidAmount()!= null? recordRecordSum.getValidAmount() : BigDecimal.ZERO);
        gameRecordReportTotalVo.setWinLossAmount(recordRecordSum.getWinLossAmount()!= null? recordRecordSum.getWinLossAmount() : BigDecimal.ZERO);
        gameRecordReportTotalVo.setAmount(recordRecordSum.getAmount()!= null? recordRecordSum.getAmount() : BigDecimal.ZERO);
        gameRecordReportTotalVo.setSurplusAmount(recordRecordSum.getSurplusAmount()!=null?recordRecordSum.getSurplusAmount():BigDecimal.ZERO);
        gameRecordReportTotalVo.setUserAmount(recordRecordSum.getUserAmount()!=null?recordRecordSum.getUserAmount():BigDecimal.ZERO);
        gameRecordReportTotalVo.setTodayAward(recordRecordSum.getTodayAward()!=null ? recordRecordSum.getTodayAward() :BigDecimal.ZERO);
        gameRecordReportTotalVo.setRiseAward(recordRecordSum.getRiseAward()!=null ? recordRecordSum.getRiseAward() :BigDecimal.ZERO);
        gameRecordReportTotalVo.setTotalWinLossAmount(gameRecordReportTotalVo.getWinLossAmount().add(gameRecordReportTotalVo.getAmount()).
            add(gameRecordReportTotalVo.getUserAmount()).add(gameRecordReportTotalVo.getSurplusAmount()).add(gameRecordReportTotalVo.getTodayAward()).add(gameRecordReportTotalVo.getRiseAward()));
        return ResponseUtil.success(gameRecordReportTotalVo);

    }
}

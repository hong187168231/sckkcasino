package com.qianyi.casinoproxy.controller.jiceng;

import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinocore.util.PageUtil;
import com.qianyi.casinocore.vo.CompanyProxyDetailVo;
import com.qianyi.casinocore.vo.CompanyProxyReportVo;
import com.qianyi.casinocore.vo.PageResultVO;
import com.qianyi.casinocore.vo.PageVo;
import com.qianyi.casinoproxy.util.CasinoProxyUtil;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.CommonUtil;
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
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Api(tags = "代理中心")
@RestController
@Slf4j
@RequestMapping("companyProxyDetail/jiceng")
public class ThridCompanyProxyDetailController {

    @Autowired
    private ProxyUserService proxyUserService;

    @Autowired
    private UserService userService;

    @Autowired
    private GameRecordService gameRecordService;

    @Autowired
    private ChargeOrderService chargeOrderService;

    @Autowired
    private WithdrawOrderService withdrawOrderService;

    public final static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

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
        if (CasinoProxyUtil.checkNull(startDate,endDate)){
            return ResponseUtil.custom("参数必填");
        }
        Long authId = CasinoProxyUtil.getAuthId();
        ProxyUser byId = proxyUserService.findById(authId);
        List<CompanyProxyReportVo> list = new LinkedList<>();
        this.assemble(list,startDate,endDate,byId,null);
        return ResponseUtil.success(list);
    }
    @ApiOperation("每日结算细节")
    @GetMapping("/findDailyDetails")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "当前id", required = true),
            @ApiImplicitParam(name = "startDate", value = "注册起始时间查询", required = true),
            @ApiImplicitParam(name = "endDate", value = "注册结束时间查询", required = true),
    })
    public ResponseEntity<CompanyProxyDetail> findDailyDetails(Long id,@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
                                                               @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endDate){
        if (CasinoProxyUtil.checkNull(id,startDate,endDate)){
            return ResponseUtil.custom("参数必填");
        }
        ProxyUser byId = proxyUserService.findById(id);
        if (CasinoProxyUtil.checkNull(byId)){
            return ResponseUtil.custom("没有这个代理");
        }
        List<CompanyProxyReportVo> list = new LinkedList<>();
        List<String> dateLists = this.findDates("D", startDate, endDate);
        dateLists.forEach(date->{
            try {
                String startTime = date+start;
                String endTime  = date+end;
                Date start = formatter.parse(startTime);
                Date end = formatter.parse(endTime);
                this.assemble(list,start,end,byId,date);
            } catch (ParseException e) {
                log.error("日期转换错误",e);
            }

        });
        return ResponseUtil.success(list);
    }
    private void assemble(List<CompanyProxyReportVo> list,Date startDate,Date endDate,ProxyUser proxyUser,String date){
        CompanyProxyReportVo companyProxyReportVo = new CompanyProxyReportVo();
        companyProxyReportVo.setGroupNewUsers(this.getGroupNewUsers(proxyUser.getId(),startDate,endDate));
        this.getGameRecords(proxyUser.getId(),startDate,endDate,companyProxyReportVo);
        companyProxyReportVo.setChargeAmount(this.getChargeAmount(proxyUser.getId(),startDate,endDate));
        companyProxyReportVo.setWithdrawMoney(this.getWithdrawMoney(proxyUser.getId(),startDate,endDate));
        companyProxyReportVo.setNickName(proxyUser.getNickName());
        companyProxyReportVo.setUserName(proxyUser.getUserName());
        companyProxyReportVo.setProxyRole(proxyUser.getProxyRole());
        companyProxyReportVo.setStaticsTimes(date);
        companyProxyReportVo.setId(proxyUser.getId());
        list.add(companyProxyReportVo);
    }

    private BigDecimal getWithdrawMoney(Long id,Date startDate,Date endDate){
        WithdrawOrder withdrawOrder = new WithdrawOrder();
        withdrawOrder.setThirdProxy(id);
        withdrawOrder.setStatus(CommonConst.NUMBER_1);
        List<WithdrawOrder> withdrawOrders = withdrawOrderService.findOrderList(withdrawOrder, startDate, endDate);
        if (CasinoProxyUtil.checkNull(withdrawOrders) || withdrawOrders.size() == CommonConst.NUMBER_0){
            return BigDecimal.ZERO;
        }
        BigDecimal chargeAmount = withdrawOrders.stream().map(WithdrawOrder::getWithdrawMoney).reduce(BigDecimal.ZERO, BigDecimal::add);
        return chargeAmount;
    }

    private BigDecimal getChargeAmount(Long id,Date startDate,Date endDate){
        ChargeOrder chargeOrder = new ChargeOrder();
        chargeOrder.setThirdProxy(id);
        chargeOrder.setStatus(CommonConst.NUMBER_1);
        List<ChargeOrder> chargeOrders = chargeOrderService.findChargeOrders(chargeOrder, startDate, endDate);
        if (CasinoProxyUtil.checkNull(chargeOrders) || chargeOrders.size() == CommonConst.NUMBER_0){
            return BigDecimal.ZERO;
        }
        BigDecimal chargeAmount = chargeOrders.stream().map(ChargeOrder::getChargeAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        return chargeAmount;
    }

    private Integer getGroupNewUsers(Long id,Date startDate,Date endDate){
        User user = new User();
        user.setThirdProxy(id);
        List<User> userList = userService.findUserList(user, startDate, endDate);
        return userList==null ? CommonConst.NUMBER_0 : userList.size();
    }
    private void getGameRecords(Long id,Date startDate,Date endDate,CompanyProxyReportVo companyProxyReportVo){
        GameRecord gameRecord = new GameRecord();
        gameRecord.setThirdProxy(id);
        String startTime = formatter.format(startDate);
        String endTime = formatter.format(endDate);
        List<GameRecord> gameRecords = gameRecordService.findGameRecords(gameRecord, startTime, endTime);
        if (CasinoProxyUtil.checkNull(gameRecord) || gameRecords.size() == CommonConst.NUMBER_0){
            companyProxyReportVo.setActiveUsers(CommonConst.NUMBER_0);
            companyProxyReportVo.setGroupPerformance(BigDecimal.ZERO);
            return;
        }
        BigDecimal validbet = BigDecimal.ZERO;
        for (GameRecord g : gameRecords){
            validbet = validbet.add(new BigDecimal(g.getValidbet()));
        }
        companyProxyReportVo.setGroupPerformance(validbet);
        gameRecords = gameRecords.stream().filter(CommonUtil.distinctByKey(GameRecord::getUser)).collect(Collectors.toList());
        companyProxyReportVo.setActiveUsers(gameRecords.size());
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

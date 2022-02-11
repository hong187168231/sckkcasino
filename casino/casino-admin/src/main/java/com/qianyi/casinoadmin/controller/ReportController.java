package com.qianyi.casinoadmin.controller;

import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinoadmin.vo.HistoryTotal;
import com.qianyi.casinocore.model.ProxyUser;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.service.ProxyUserService;
import com.qianyi.casinocore.service.ReportService;
import com.qianyi.casinocore.service.UserService;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.casinocore.vo.CompanyProxyReportVo;
import com.qianyi.casinocore.vo.PageResultVO;
import com.qianyi.modulecommon.annotation.NoAuthorization;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.DateUtil;
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
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.*;

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

    //    @NoAuthorization
    @ApiOperation("查询个人报表")
    @GetMapping("/queryPersonReport")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "pageSize", value = "每页大小(默认10条)", required = false),
        @ApiImplicitParam(name = "pageCode", value = "当前页(默认第一页)", required = false),
        @ApiImplicitParam(name = "userName", value = "账号", required = false),
        @ApiImplicitParam(name = "startTime", value = "起始时间查询", required = true),
        @ApiImplicitParam(name = "endTime", value = "结束时间查询", required = true),
    })
    public ResponseEntity<Map<String,Object>> queryPersonReport(Integer pageSize, Integer pageCode, String userName,
        String startTime, String endTime){
        if (LoginUtil.checkNull(startTime,endTime,pageSize,pageCode)){
            return ResponseUtil.custom("参数不合法");
        }
        try {
            Date startDate = DateUtil.getSimpleDateFormat().parse(startTime);
            Date endDate = DateUtil.getSimpleDateFormat().parse(endTime);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startDate);
            calendar.add(Calendar.HOUR, 12);
            startDate = calendar.getTime();
            startTime = DateUtil.dateToPatten(startDate);
            calendar.setTime(endDate);
            calendar.add(Calendar.HOUR, 12);
            endDate = calendar.getTime();
            endTime = DateUtil.dateToPatten(endDate);
        } catch (ParseException e) {
            return ResponseUtil.custom("参数不合法");
        }

        if(StringUtils.hasLength(userName)){
            User user = userService.findByAccount(userName);
            if(user != null){
                List<Map<String,Object>> reportResult = reportService.queryPersonReport(user.getId(),startTime,endTime);
                PageResultVO<Map<String, Object>> mapPageResultVO = combinePage(reportResult, 1, pageCode, pageSize);
                return ResponseUtil.success(getMap(mapPageResultVO));
            }
            List<Map<String,Object>> emptyResult = new ArrayList<Map<String,Object>>();
            return ResponseUtil.success(combinePage(emptyResult, 0, pageCode, pageSize));
        }

        int page = (pageCode-1)*pageSize;
        List<Map<String,Object>> reportResult = reportService.queryAllPersonReport(startTime,endTime,page,pageSize);
        int totalElement = reportService.queryTotalElement(startTime,endTime);
        PageResultVO<Map<String, Object>> mapPageResultVO = combinePage(reportResult, totalElement, pageCode, pageSize);
        return ResponseUtil.success(getMap(mapPageResultVO));
    }

    private PageResultVO<Map<String, Object>> getMap(PageResultVO<Map<String, Object>> mapPageResultVO){
        List<Map<String, Object>> content = (List<Map<String, Object>>)mapPageResultVO.getContent();
        List<Map<String, Object>> list = null;
        if (!LoginUtil.checkNull(content) && content.size() > CommonConst.NUMBER_0){
            list = new LinkedList<>();
            for (Map<String, Object> item:content){
                if (LoginUtil.checkNull(item.get("third_proxy"))){
                    list.add(item);
                    continue;
                }
                ProxyUser third_proxy = proxyUserService.findById(Long.parseLong(item.get("third_proxy").toString()));
                Map<String, Object> newMap = new HashMap<>(item);
                newMap.put("thirdProxy",third_proxy==null?"":third_proxy.getUserName());
                list.add(newMap);
            }
        }
        mapPageResultVO.setContent(list);
        return mapPageResultVO;
    }
    private PageResultVO<Map<String,Object>> combinePage(List<Map<String,Object>> reportResult,int totalElement,int page,int num){
        PageResultVO<Map<String,Object>> pageResult = new PageResultVO<Map<String,Object>>(page,num,Long.parseLong(totalElement+""),reportResult);
        return pageResult;
    }

    //    @NoAuthorization
    @ApiOperation("查询个人报表总计")
    @GetMapping("/queryTotal")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "userName", value = "账号", required = false),
        @ApiImplicitParam(name = "startDate", value = "起始时间查询", required = false),
        @ApiImplicitParam(name = "endDate", value = "结束时间查询", required = false),
    })
    public ResponseEntity<Map<String,Object>> queryTotal(String userName,@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
        @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endDate){
        if (LoginUtil.checkNull(startDate,endDate)){
            return ResponseUtil.custom("参数不合法");
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        calendar.add(Calendar.HOUR, 12);
        startDate = calendar.getTime();
        String startTime = DateUtil.dateToPatten(startDate);
        calendar.setTime(endDate);
        calendar.add(Calendar.HOUR, 12);
        endDate = calendar.getTime();
        String endTime = DateUtil.dateToPatten(endDate);
        Map<String,Object> result = reportService.queryAllTotal(startTime,endTime);
        HistoryTotal itemObject = getHistoryItem(result);

        return ResponseUtil.success(itemObject);
    }

    private HistoryTotal getHistoryItem(Map<String,Object> result){
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
    }
}

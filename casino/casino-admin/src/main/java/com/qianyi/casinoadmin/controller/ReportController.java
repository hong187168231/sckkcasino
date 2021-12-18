package com.qianyi.casinoadmin.controller;

import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.service.ReportService;
import com.qianyi.casinocore.service.UserService;
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

import java.util.Date;
import java.util.List;
import java.util.Map;

@Api(tags = "报表中心")
@Slf4j
@RestController
@RequestMapping("report")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @Autowired
    private UserService userService;

    @NoAuthorization
    @ApiOperation("查询个人报表")
    @GetMapping("/queryPersonReport")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageSize", value = "每页大小(默认10条)", required = false),
            @ApiImplicitParam(name = "pageCode", value = "当前页(默认第一页)", required = false),
            @ApiImplicitParam(name = "userName", value = "账号", required = false),
            @ApiImplicitParam(name = "startTime", value = "起始时间查询", required = false),
            @ApiImplicitParam(name = "endTime", value = "结束时间查询", required = false),
    })
    public ResponseEntity<Map<String,Object>> queryPersonReport(Integer pageSize, Integer pageCode, String userName,
                                                     String startTime, String endTime){
//        String startTime = startDate==null? null: DateUtil.getSimpleDateFormat1().format(startDate);
//        String endTime =  endDate==null? null:DateUtil.getSimpleDateFormat1().format(endDate);
        if(StringUtils.hasLength(userName)){
            User user = userService.findByAccount(userName);
            List<Map<String,Object>> reportResult = reportService.queryPersonReport(user.getId(),startTime,endTime);
            return ResponseUtil.success(combinePage(reportResult,pageSize,pageCode));
        }

        int page = (pageCode-1)*pageSize;
        List<Map<String,Object>> reportResult = reportService.queryAllPersonReport(startTime,endTime,page,pageSize);
        return ResponseUtil.success(combinePage(reportResult,pageSize,pageSize));
    }

    private PageResultVO<Map<String,Object>> combinePage(List<Map<String,Object>> reportResult,int page,int num){
        PageResultVO<Map<String,Object>> pageResult = new PageResultVO<Map<String,Object>>(page,num,Long.parseLong(reportResult.size()+""),reportResult);
        return pageResult;
    }

    @NoAuthorization
    @ApiOperation("查询个人报表总计")
    @GetMapping("/queryTotal")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userName", value = "账号", required = false),
            @ApiImplicitParam(name = "startDate", value = "起始时间查询", required = false),
            @ApiImplicitParam(name = "endDate", value = "结束时间查询", required = false),
    })
    public ResponseEntity<Map<String,Object>> queryTotal(String userName,@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
                                                         @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endDate){
        String startTime = startDate==null? null: DateUtil.getSimpleDateFormat1().format(startDate);
        String endTime =  endDate==null? null:DateUtil.getSimpleDateFormat1().format(endDate);
        Map<String,Object> result = reportService.queryAllTotal(startTime,endTime);
        return ResponseUtil.success(result);
    }
}

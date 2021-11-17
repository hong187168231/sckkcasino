package com.qianyi.casinoadmin.controller;

import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.UserRunningWater;
import com.qianyi.casinocore.service.UserRunningWaterService;
import com.qianyi.casinocore.vo.CompanyProxyReportVo;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
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

import java.util.Date;

@Api(tags = "客户中心")
@Slf4j
@RestController
@RequestMapping("userRunningWater")
public class UserRunningWaterController {

    @Autowired
    private UserRunningWaterService userRunningWaterService;

    @ApiOperation("查询会员流水报表")
    @GetMapping("/find")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageSize", value = "每页大小(默认10条)", required = false),
            @ApiImplicitParam(name = "pageCode", value = "当前页(默认第一页)", required = false),
            @ApiImplicitParam(name = "userId", value = "会员id", required = true),
            @ApiImplicitParam(name = "startDate", value = "起始时间查询", required = false),
            @ApiImplicitParam(name = "endDate", value = "结束时间查询", required = false),
    })
    public ResponseEntity<UserRunningWater> find(Integer pageSize, Integer pageCode, Long userId,
                                                 @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
                                                 @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date endDate){
        if (LoginUtil.checkNull(userId)){
            return ResponseUtil.custom("参数必填");
        }
        UserRunningWater userRunningWater = new UserRunningWater();
        userRunningWater.setUserId(userId);
        Sort sort=Sort.by("id").descending();
        Pageable pageable = LoginUtil.setPageable(pageCode, pageSize, sort);
        Page<UserRunningWater> userPage = userRunningWaterService.findUserPage(pageable, userRunningWater,startDate,endDate);
        return ResponseUtil.success(userPage);
    }
}

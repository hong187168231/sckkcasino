package com.qianyi.casinoadmin.controller;

import com.alibaba.fastjson.JSONObject;
import com.qianyi.casinoadmin.util.LoginUtil;
import com.qianyi.casinocore.model.PlatformConfig;
import com.qianyi.casinocore.service.PlatformConfigService;
import com.qianyi.casinocore.util.CommonConst;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.DateUtil;
import com.qianyi.modulecommon.util.HttpClient4Util;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.MessageFormat;
import java.util.Date;

@Api(tags = "运营中心")
@RestController
@Slf4j
@RequestMapping("rePullGameRecord")
public class RePullGameRecord {

    @Autowired
    private PlatformConfigService platformConfigService;

    private String rePullDataUrl = "/supplement/supplementByPlatform?";

    @ApiOperation("补历史注单")
    @GetMapping("pullGameRecordDate")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "startDate", value = "开始时间", required = true),
            @ApiImplicitParam(name = "endDate", value = "结束时间", required = true),
            @ApiImplicitParam(name = "platform", value = "平台：WM,PG,CQ9", required = true)
    })
    public ResponseEntity pullGameRecordDate(@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date startDate,
                                             @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")Date endDate,
                                             String platform){

        try {
            int numDay = DateUtil.daysBetween(startDate, endDate);
            if(numDay >= CommonConst.NUMBER_1){
                return ResponseUtil.custom("参数不合法");
            }
            int maxNum = DateUtil.daysBetween(startDate, endDate);
            if(maxNum > CommonConst.NUMBER_30){
                return ResponseUtil.custom("参数不合法");
            }
            PlatformConfig first = platformConfigService.findFirst();
            String rePullGameurl = first == null?"":first.getWebConfiguration();
            rePullGameurl = rePullGameurl + rePullDataUrl;
            String param = "platform={0}&startTime={1}&endTime={2} ";
            param = MessageFormat.format(param,platform,startDate,endDate);
            String s = HttpClient4Util.get(rePullGameurl + param);
            log.info("平台【{}】，开始时间：【{}】，结束时间：【{}】查询web接口返回【{}】", platform, startDate, endDate, s);
            JSONObject jsonObject = JSONObject.parseObject(s);
            if (LoginUtil.checkNull(jsonObject) || LoginUtil.checkNull(jsonObject.get("code"),jsonObject.get("msg"))){
                return ResponseUtil.custom("查询WM余额失败");
            }
            Integer code = (Integer) jsonObject.get("code");
            if (code == CommonConst.NUMBER_0){
                if (LoginUtil.checkNull(jsonObject.get("data"))){
                    return ResponseUtil.success(CommonConst.NUMBER_0);
                }
                return ResponseUtil.success(jsonObject.get("data"));
            }else {
                return ResponseUtil.custom(jsonObject.get("msg").toString());
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseUtil.custom("参数不合法");
        }
    }

}

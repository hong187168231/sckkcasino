package com.qianyi.casinoreport.controller;

import com.qianyi.casinoreport.task.CompanyProxyTask;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("company")
@Api(value = "报表数据处理")
public class CompanyProxyController {

    @Autowired
    private CompanyProxyTask companyProxyTask;

    @ApiOperation("补充推广贷数据")
    @PostMapping("/supplementaryData")
    public ResponseEntity supplementaryData(){
        companyProxyTask.supplementaryData(null);
        return ResponseUtil.success();
    }
}

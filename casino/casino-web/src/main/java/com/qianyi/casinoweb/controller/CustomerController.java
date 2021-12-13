package com.qianyi.casinoweb.controller;

import com.qianyi.casinocore.model.CustomerConfigure;
import com.qianyi.casinocore.service.CustomerConfigureService;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.annotation.NoAuthentication;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("customer")
@Api(tags = "客服中心")
public class CustomerController {

    @Autowired
    private CustomerConfigureService customerConfigureService;

    @GetMapping("contact")
    @ApiOperation("客服联系方式")
    @NoAuthentication
    public ResponseEntity<List<CustomerConfigure>> contact() {
        List<CustomerConfigure> list = customerConfigureService.findByState(Constants.open);
        return ResponseUtil.success(list);
    }
}

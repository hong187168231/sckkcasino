package com.qianyi.casinoadmin.controller;

import com.qianyi.casinocore.model.Customer;
import com.qianyi.casinocore.service.CustomerService;
import com.qianyi.modulecommon.annotation.NoAuthentication;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("demo")
@Api(tags = "测试demo")
public class DemoController {

//    @Autowired
//    private CustomerService customerService;
//
//    @GetMapping("contact")
//    @ApiOperation("测试案例")
//    @NoAuthentication
//    public ResponseEntity contact() {
//        Customer customer = customerService.findFirst();
//        return ResponseUtil.success(customer);
//    }

}

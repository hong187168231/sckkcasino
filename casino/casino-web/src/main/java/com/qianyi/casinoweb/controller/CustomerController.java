package com.qianyi.casinoweb.controller;

import com.qianyi.casinocore.model.Customer;
import com.qianyi.casinocore.service.CustomerService;
import com.qianyi.modulecommon.annotation.NoAuthentication;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import com.qianyi.modulecommon.util.CommonUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("customer")
@Api(tags = "客服中心")
public class CustomerController {

    @Autowired
    CustomerService customerService;

    @GetMapping("contact")
    @ApiOperation("客服联系方式")
    @NoAuthentication
    public ResponseEntity<Customer> contact() {
        Customer customer = customerService.findFirst();
        if (customer != null) {
            return ResponseUtil.success(customer);
        }
        return ResponseUtil.success(new Customer());
    }
}

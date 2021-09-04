package com.qianyi.casinoweb.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mysql.cj.util.StringUtils;
import com.qianyi.casinocore.model.BankcardsCustomer;
import com.qianyi.casinocore.model.Customer;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.service.BankInfoService;
import com.qianyi.casinocore.service.BankcardsCustomerService;
import com.qianyi.casinocore.service.CustomerService;
import com.qianyi.casinocore.service.UserService;
import com.qianyi.casinoweb.util.CasinoWebUtil;
import com.qianyi.modulecommon.annotation.NoAuthentication;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("customer")
@Api(tags = "用户绑定银行卡相关接口")
public class BankcardsCustomerController {
	
    @Autowired
    UserService userService;

    @Autowired
    private BankInfoService bankInfoService;
    
    @Autowired
    private BankcardsCustomerService bankcardsCustomerService;
    
    @GetMapping("/bankList")
    @ApiOperation("银行卡列表")
    @NoAuthentication
    public ResponseEntity bankList() {
        return ResponseUtil.success(bankInfoService.findAll());
    }
    
    @GetMapping("/boundBankList")
    @ApiOperation("用户已绑定银行卡列表")
    @NoAuthentication
    public ResponseEntity boundBankList(@RequestBody BankcardsCustomer bankcardsCustomer) {
        User user = userService.findById(CasinoWebUtil.getAuthId());
    	if(StringUtils.isNullOrEmpty(user.getAccount())) {
    		return ResponseUtil.success(bankcardsCustomerService.findByExample(bankcardsCustomer));
    	}
    	return null;
    }

    @PostMapping("/boundBank")
    @ApiOperation("用户绑定银行卡")
    @NoAuthentication
    public ResponseEntity boundBank(@RequestBody BankcardsCustomer bankcardsCustomer) {
    	User user = userService.findById(CasinoWebUtil.getAuthId());
    	bankcardsCustomer.setAccount(user.getAccount());
        Integer count = bankcardsCustomerService.boundBank(bankcardsCustomer);
        
        return ResponseUtil.success(count);
    }

}

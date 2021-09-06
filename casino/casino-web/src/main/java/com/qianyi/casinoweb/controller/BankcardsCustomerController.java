package com.qianyi.casinoweb.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.mysql.cj.util.StringUtils;
import com.qianyi.casinocore.model.BankcardsCustomer;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.service.BankInfoService;
import com.qianyi.casinocore.service.BankcardsCustomerService;
import com.qianyi.casinocore.service.SysDictService;
import com.qianyi.casinocore.service.UserService;
import com.qianyi.casinoweb.util.CasinoWebUtil;
import com.qianyi.modulecommon.annotation.NoAuthentication;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/customer/bank")
@Api(tags = "用户中心")
public class BankcardsCustomerController {
	
    @Autowired
    UserService userService;
    
    @Autowired
    SysDictService sysDictService;

    @Autowired
    private BankInfoService bankInfoService;
    
    @Autowired
    private BankcardsCustomerService bankcardsCustomerService;
    
    @GetMapping("/list")
    @ApiOperation("银行卡列表")
    @NoAuthentication
    @ResponseBody
    public ResponseEntity bankList() {
        return ResponseUtil.success(bankInfoService.findAll());
    }
    
    @GetMapping("/boundList")
    @ApiOperation("用户已绑定银行卡列表")
    @NoAuthentication
    @ResponseBody
    public ResponseEntity boundList() {
        User user = userService.findById(CasinoWebUtil.getAuthId());
    	if(StringUtils.isNullOrEmpty(user.getAccount())) {
    		BankcardsCustomer bankcardsCustomer = new BankcardsCustomer();
    		bankcardsCustomer.setAccount(user.getAccount());
    		return ResponseUtil.success(bankcardsCustomerService.findByExample(bankcardsCustomer));
    	}
    	return ResponseUtil.fail();
    }

    @PostMapping("/bound")
    @ApiOperation("用户绑定银行卡")
    @NoAuthentication
    @ApiImplicitParams({
        @ApiImplicitParam(name = "bankName", value = "银行名", required = true),
        @ApiImplicitParam(name = "bankId", value = "银行卡id", required = true),
        @ApiImplicitParam(name = "bankAccount", value = "用户的银行/支付宝账号", required = true),
        @ApiImplicitParam(name = "province", value = "省", required = true),
        @ApiImplicitParam(name = "city", value = "市区", required = true),
        @ApiImplicitParam(name = "address", value = "支行名,开户地址", required = true),
        @ApiImplicitParam(name = "realName", value = "开户名", required = true)})
	public ResponseEntity bound(String bankName, Integer bankId, String bankAccount, String province, String city,
			String address, String realName) {
    	BankcardsCustomer bankcardsCustomer = new BankcardsCustomer();
    	bankcardsCustomer.setBankName(bankName);
    	bankcardsCustomer.setBankId(bankId);
    	bankcardsCustomer.setBankAccount(bankAccount);
    	bankcardsCustomer.setProvince(province);
    	bankcardsCustomer.setCity(city);
    	bankcardsCustomer.setAddress(address);
    	bankcardsCustomer.setRealName(realName);
    	
    	User user = userService.findById(CasinoWebUtil.getAuthId());
    	bankcardsCustomer.setAccount(user.getAccount());
        Integer count = bankcardsCustomerService.bound(bankcardsCustomer);
        return ResponseUtil.success(count);
    }

	@PostMapping("/unBound")
	@ApiOperation("用户解绑银行卡")
	@NoAuthentication
	@ApiImplicitParams({@ApiImplicitParam(name = "id", value = "id", required = true)})
	public ResponseEntity unBound(Integer id) {
		BankcardsCustomer bankcardsCustomer = new BankcardsCustomer();
		bankcardsCustomer.setId(id);
		
		User user = userService.findById(CasinoWebUtil.getAuthId());
		bankcardsCustomer.setAccount(user.getAccount());
		Integer count = bankcardsCustomerService.unBound(bankcardsCustomer);
		return ResponseUtil.success(count);
	}
}

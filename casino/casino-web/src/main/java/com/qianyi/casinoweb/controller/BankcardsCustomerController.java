package com.qianyi.casinoweb.controller;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.qianyi.casinocore.model.BankInfo;
import com.qianyi.casinocore.model.BankcardsCustomer;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.repository.BankInfoRepository;
import com.qianyi.casinocore.repository.BankcardsCustomerRepository;
import com.qianyi.casinocore.service.BankInfoService;
import com.qianyi.casinocore.service.BankcardsCustomerService;
import com.qianyi.casinocore.service.SysDictService;
import com.qianyi.casinocore.service.UserService;
import com.qianyi.casinoweb.util.CasinoWebUtil;
import com.qianyi.modulecommon.Constants;
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
    
    @Autowired
    private BankcardsCustomerRepository bankcardsCustomerRepository;
    
    @Autowired
    private BankInfoRepository bankInfoRepository;

    
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
    	if(StringUtils.isNotEmpty(user.getAccount())) {
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
        @ApiImplicitParam(name = "bankAccount", value = "用户的银行账号", required = true),
        @ApiImplicitParam(name = "address", value = "开户地址", required = true),
        @ApiImplicitParam(name = "realName", value = "开户名")})
	public ResponseEntity bound(String bankName, Long bankId, String bankAccount, String address, String realName) {
    	
    	// 必要的字段进行合法判断
		String checkParamFroBound = BankcardsCustomer.checkParamFroBound(bankName, bankId, bankAccount, address);
		if (StringUtils.isNotEmpty(checkParamFroBound)) {
			return ResponseUtil.custom(checkParamFroBound);
		}

    	BankcardsCustomer bankcardsCustomer = new BankcardsCustomer();
    	
    	// 1.查询银行卡是否存在
		BankInfo bankInfo = new BankInfo();
		bankInfo.setId(bankId);
		Boolean bankExists = bankInfoRepository.exists(Example.of(bankInfo));
		if(!bankExists) {
			return ResponseUtil.custom("不支持该银行，请更换银行卡");
		}
		
		// 2.查询当前卡号是否存在
		User user = userService.findById(CasinoWebUtil.getAuthId());
    	
		BankcardsCustomer bankAccountCrad = new BankcardsCustomer();
		bankAccountCrad.setBankAccount(bankAccount);
		
		// 当前用户是否已经绑定过，可以删除：同一张卡不同用户绑定
		bankAccountCrad.setAccount(user.getAccount());
		Boolean bankAccountExists = bankcardsCustomerRepository.exists(Example.of(bankAccountCrad));
		if(bankAccountExists) {
			return ResponseUtil.custom("当前银行卡已经被绑定，请换一张卡");
		}
		
		// 3.查看已绑定的数量 不可大于最大数量
		int findByAccountCount = bankcardsCustomerService.countByAccount(user.getAccount());
		if(findByAccountCount >= Constants.BANK_USER_BOUND_MAX) {
			return ResponseUtil.custom("最多绑定" + Constants.BANK_USER_BOUND_MAX + "张银行卡，已超出限制。");
		}
		
		// 4.设置用户真实姓名 ,如果是第一张默认为当前传入的名字
		if (findByAccountCount == 0) {
			if(StringUtils.isEmpty(realName)) {
				return ResponseUtil.custom("真实姓名不能为空！");
			}
			bankcardsCustomer.setRealName(realName);
		} else {
			// 获取一张卡的真实名字
			bankcardsCustomer.setRealName(bankcardsCustomerService.findByAccountOne(user.getAccount()));
		}		
		
		//TODO : 其余代码具体业务逻辑待定 例如：同一张卡的重复绑定 或者 其他相关业务的处理

		// 5.执行保存
		Date now = new Date();
		bankcardsCustomer.setAccount(user.getAccount());
		bankcardsCustomer.setUpdateTime(now);
		bankcardsCustomer.setCreateTime(now);
		
    	bankcardsCustomer.setBankName(bankName);
    	bankcardsCustomer.setBankId(bankId);
    	bankcardsCustomer.setBankAccount(bankAccount);
    	bankcardsCustomer.setAddress(address);
		
		BankcardsCustomer fristBank = new BankcardsCustomer();
		fristBank.setAccount(bankcardsCustomer.getAccount());
		bankcardsCustomer.setDefaultCard(0);
		// 如果当前用户没有绑定过卡,默认第一张卡位主卡
		boolean fristBankExists = bankcardsCustomerRepository.exists(Example.of(fristBank));
		if(!fristBankExists) {
			bankcardsCustomer.setDefaultCard(1);
		}
    	
        Integer count = bankcardsCustomerService.bound(bankcardsCustomer);
        return ResponseUtil.success(count);
    }

	@PostMapping("/unBound")
	@ApiOperation("用户解绑银行卡")
	@NoAuthentication
	@ApiImplicitParams({@ApiImplicitParam(name = "id", value = "id", required = true)})
	public ResponseEntity unBound(Long id) {
		BankcardsCustomer bankcardsCustomer = new BankcardsCustomer();
		bankcardsCustomer.setId(id);
		
		User user = userService.findById(CasinoWebUtil.getAuthId());
		bankcardsCustomer.setAccount(user.getAccount());
		// 1.查询用户当前银行ID是否存在 
		boolean bankAccountExists = bankcardsCustomerRepository.exists(Example.of(bankcardsCustomer));
		// 如果不存在，报错
		if(!bankAccountExists) {
			return ResponseUtil.custom("用户当前银行卡不存在");
		}
		
		Integer count = bankcardsCustomerService.unBound(bankcardsCustomer);
		return ResponseUtil.success(count);
	}
}

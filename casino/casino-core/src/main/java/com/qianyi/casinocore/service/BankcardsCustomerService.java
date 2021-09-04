package com.qianyi.casinocore.service;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import com.qianyi.casinocore.model.BankInfo;
import com.qianyi.casinocore.model.BankcardsCustomer;
import com.qianyi.casinocore.repository.BankInfoRepository;
import com.qianyi.casinocore.repository.BankcardsCustomerRepository;
import com.qianyi.modulecommon.util.Assert;

@Service
public class BankcardsCustomerService {
	
    @Autowired
    private BankcardsCustomerRepository bankcardsCustomerRepository;
    
    @Autowired
    private BankInfoRepository bankInfoRepository;

    public List<BankcardsCustomer> findByExample(BankcardsCustomer bankcardsCustomer) {
    	return bankcardsCustomerRepository.findAll(Example.of(bankcardsCustomer));
    }

	/**
	 * 绑定用户的银行卡
	 * @param bankcardsCustomer
	 * @return
	 */
	public Integer boundBank(BankcardsCustomer bankcardsCustomer) {
		// 1.查询银行卡是否存在
		BankInfo bankInfo = new BankInfo();
		bankInfo.setId(bankcardsCustomer.getBankId());
		boolean noBankExists = !bankInfoRepository.exists(Example.of(bankInfo));
		Assert.isTrue(noBankExists, "不支持该银行，请更换银行卡");
		// 2.查询当前卡号是否存在
		BankcardsCustomer bankAccount = new BankcardsCustomer();
		bankAccount.setBankAccount(bankcardsCustomer.getBankAccount());
		// 当前用户是否已经绑定过，可以删除：同一张卡不同用户绑定
		bankAccount.setAccount(bankcardsCustomer.getAccount());
		boolean bankAccountExists = bankcardsCustomerRepository.exists(Example.of(bankAccount));
		Assert.isTrue(bankAccountExists, "当前银行卡已经被绑定，请换一张卡");
		// 3.必要的字段进行合法判断
		Assert.isNull(bankcardsCustomer.getAddress(), "开户地址不能为空！");
		Assert.isNull(bankcardsCustomer.getRealName(), "真实姓名不能为空！");
		Assert.isNull(bankcardsCustomer.getBankName(), "银行名不能为空！");
		Assert.isNull(bankcardsCustomer.getBankAccount(), "银行账号不能为空！");
		Assert.designatedArea(bankcardsCustomer.getBankAccount(), "长度只能在16~20位！", 16, 20);
		//TODO : 其余代码具体业务逻辑待定 例如：会员最大绑定数量，同一张卡的重复绑定
		
		// 执行保存
		Date now = new Date();
		bankcardsCustomer.setUpdateTime(now);
		bankcardsCustomer.setCreateTime(now);
		BankcardsCustomer save = bankcardsCustomerRepository.save(bankcardsCustomer);
		return 1;
	}
}

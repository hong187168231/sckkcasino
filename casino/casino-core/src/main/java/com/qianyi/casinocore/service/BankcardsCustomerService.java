//package com.qianyi.casinocore.service;
//
//import java.util.List;
//
//import javax.transaction.Transactional;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import com.qianyi.casinocore.model.BankcardsCustomer;
//
//@Service
//public class BankcardsCustomerService {
//
//    @Autowired
//    private BankcardsCustomerRepository bankcardsCustomerRepository;
//
//    public List<BankcardsCustomer> findByExample(BankcardsCustomer bankcardsCustomer) {
//    	List<BankcardsCustomer> findByAccountAndBankName = bankcardsCustomerRepository.findByAccountAndBankName("jordan", "中国银行");
//    	return findByAccountAndBankName;
//    }
//
//    public String findByAccountOne(String account) {
//    	return bankcardsCustomerRepository.findByAccountOne(account);
//    }
//
//    public int countByAccount(String account) {
//    	return bankcardsCustomerRepository.countByAccount(account);
//    }
//
//	/**
//	 * 绑定用户的银行卡
//	 * @param bankcardsCustomer
//	 * @return
//	 */
//    @Transactional
//	public Integer bound(BankcardsCustomer bankcardsCustomer) {
////
////		bankcardsCustomer.setAccount("农村娃");
////    	bankcardsCustomer.setBankName("中国银行");
////    	bankcardsCustomer.setBankId(1);
////    	bankcardsCustomer.setBankAccount("161261056156056");
////    	bankcardsCustomer.setProvince("上海");
////    	bankcardsCustomer.setCity("闵行");
////    	bankcardsCustomer.setAddress("人民东路");
////    	bankcardsCustomer.setDisable(0);
////    	bankcardsCustomer.setUpdateBy("jordan");
////    	bankcardsCustomer.setDefaultCard(1);
////    	bankcardsCustomer.setRealName("打工人");
////    	bankcardsCustomer.setCreateTime(new Date());
////    	bankcardsCustomer.setUpdateTime(new Date());
////    	bankcardsCustomerRepository.save(bankcardsCustomer);
//
//		BankcardsCustomer save = bankcardsCustomerRepository.save(bankcardsCustomer);
//		return 1;
//	}
//
//	/**
//	 * 解除绑定银行卡
//	 * @param bankcardsCustomer
//	 * @return
//	 */
//	public Integer unBound(BankcardsCustomer bankcardsCustomer) {
//
//
//		// 2.执行删除银行卡
//		Long id = (long)bankcardsCustomer.getId();
//		bankcardsCustomerRepository.deleteById(id);
//
//		// TODO:解绑后需要做的业务处理
//		return 1;
//	}
//}

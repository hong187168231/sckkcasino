package com.qianyi.casinocore.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.qianyi.casinocore.model.BankcardsCustomer;

public interface BankcardsCustomerRepository extends JpaRepository<BankcardsCustomer,Long> {
	
	
	/**
	 * 
	 * 	方法命名规则查询 
	 * 	命名规则 按照Spring Data JPA 定义的规则，查询方法以findBy开头，删除方法以deleteBy......
	 * 	涉及条件查询时，条件的属性用条件关键字连接，要注意的是：
	 * 	条件属性首字母需大写。框架在进行方法名解析时，
	 * 	会先把方法名多余的前缀截取掉，然后对剩下部分进行解析。
	 * 
	 * @param account
	 * @param bankName
	 * @return
	 */
	List<BankcardsCustomer> findByAccountAndBankName(String account, String bankName);
	
	int countByAccount(String account);

	@Query(value = "select real_name from bankcards_customer where account = ? limit 1",nativeQuery = true)
	String findByAccountOne(String account);
	
	/**
	 *  	使用sql进行条件查询
	 * 	  自定义的方法，与jpql不同的是，这种方法需要加上nativeQuery=true来声明这是一个本地查询（sql查询）
	 * 	 数字其实就表示这个属性对应方法内形参的位置，这样我们就可以不按照属性的顺序进行赋值了
	 * 
	 * @param account
	 * @param bankName
	 * @return
	 */
	@Query(value = "select * from bankcards_customer where account = ?1 and bank_name = ?2 ",nativeQuery = true)
	List<BankcardsCustomer> sqlFindByName(String account, String bankName);
	
	
	
}

package com.qianyi.casinocore.business;

import com.qianyi.casinocore.model.Bankcards;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.repository.BankcardsRepository;
import com.qianyi.casinocore.service.BankcardsService;
import com.qianyi.casinocore.service.UserService;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class WithdrawBusiness {

    @Autowired
    UserService userService;

    @Autowired
    BankcardsService bankcardsService;

    public String getWithdrawFullMoney(Long userId){
        User user = userService.findById(userId);
        return user==null?"0":user.getWithdrawMoney().toString();
    }

    public List<Map<String,Object>> getWithdrawBankcardsList(Long userId){
        return bankcardsService.findForBankcardsByUserId(userId);
    }

    public User getUserByLock(Long userId){
        return userService.findUserByIdUseLock(userId);
    }

    public BigDecimal checkMoney(String money){
        BigDecimal decMoney = null;
        try {
            decMoney = new BigDecimal(money);
        }catch (Exception e){
            decMoney = new BigDecimal(-1);
        }

        return decMoney;
    }

    /*
    * 检测参数
    * */
    public String checkParams(String withdrawPwd, BigDecimal decMoney, User user){
        //判断密码是否正确
        if(!withdrawPwd.equals(user.getWithdrawPassword())){
            return "交易密码错误";
        }

        log.info("bigdecimal is devid is {}",decMoney.compareTo(user.getWithdrawMoney()));

        //判断是否大于可提金额
        if(decMoney.compareTo(user.getWithdrawMoney())>=0){
            return "超过可提金额";
        }
        return null;
    }

    /*
    * 进行转账
    * */
    @Transactional
    public User processWithdraw(BigDecimal money,User user){
        log.info("money is {}, draw money is {}",money,user.getWithdrawMoney());
        user.setWithdrawMoney(user.getWithdrawMoney().subtract(money));
        userService.save(user);
        return user;
    }

}

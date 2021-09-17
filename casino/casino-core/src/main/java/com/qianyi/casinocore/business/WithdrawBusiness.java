package com.qianyi.casinocore.business;

import com.qianyi.casinocore.model.Bankcards;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.model.UserMoney;
import com.qianyi.casinocore.model.WithdrawOrder;
import com.qianyi.casinocore.repository.BankcardsRepository;
import com.qianyi.casinocore.service.*;
import com.qianyi.modulecommon.reponse.ResponseEntity;
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
    private UserService userService;

    @Autowired
    private BankcardsService bankcardsService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private WithdrawOrderService withdrawOrderService;

    @Autowired
    private UserMoneyService userMoneyService;

    public String getWithdrawFullMoney(Long userId){
        User user = userService.findById(userId);
        return user==null?"0":user.getWithdrawMoney().toString();
    }

    public List<Map<String,Object>> getWithdrawBankcardsList(Long userId){
        return bankcardsService.findForBankcardsByUserId(userId);
    }

    public User getUserById(Long userId){
        return userService.findById(userId);
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
    public User processWithdraw(BigDecimal money, String bankId,Long userId){
        WithdrawOrder withdrawOrder = getWidrawOrder(money,bankId,userId);
        withdrawOrderService.saveOrder(withdrawOrder);
        User user = userService.findUserByIdUseLock(userId);
        log.info("money is {}, draw money is {}",money,user.getWithdrawMoney());
        user.setWithdrawMoney(user.getWithdrawMoney().subtract(money));
        userService.save(user);
        return user;
    }

    private WithdrawOrder getWidrawOrder(BigDecimal money, String bankId, Long userId){
        WithdrawOrder withdrawOrder = new WithdrawOrder();
        withdrawOrder.setWithdrawMoney(money);
        withdrawOrder.setBankId(bankId);
        withdrawOrder.setUserId(userId);
        withdrawOrder.setNo(orderService.getOrderNo());
        withdrawOrder.setStatus(0);
        return withdrawOrder;
    }

    @Transactional
    public ResponseEntity updateWithdrawAndUser(WithdrawOrder withdrawOrder) {
        Long userId = withdrawOrder.getUserId();
        //对用户数据进行行锁
        UserMoney userMoney = userMoneyService.findUserByUserIdUseLock(userId);
        if (userMoney == null) {
            return ResponseUtil.custom("用户不存在");
        }
        userMoney.setMoney(userMoney.getMoney().add(withdrawOrder.getWithdrawMoney()));
        WithdrawOrder withdraw = withdrawOrderService.saveOrder(withdrawOrder);
        log.info("user sum money is {}, add withdrawMoney is {}",userMoney.getMoney(), withdrawOrder.getWithdrawMoney());
        userMoneyService.save(userMoney);
        return ResponseUtil.success(withdraw);
    }
}

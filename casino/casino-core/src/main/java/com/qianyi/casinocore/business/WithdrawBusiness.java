package com.qianyi.casinocore.business;

import com.qianyi.casinocore.model.Bankcards;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.model.UserMoney;
import com.qianyi.casinocore.model.WithdrawOrder;
import com.qianyi.casinocore.repository.BankcardsRepository;
import com.qianyi.casinocore.service.*;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.reponse.ResponseCode;
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
        if (user != null) {
            return user.getWithdrawMoney() == null ? "0" : user.getWithdrawMoney().toString();
        }
        return "0";
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

    /**
     * 获取用户的可提现金额
     * @param userId
     * @return
     */
    public BigDecimal getWithdrawMoneyByUserId(Long userId) {
        UserMoney userMoney = userMoneyService.findByUserId(userId);
        BigDecimal defaultVal = BigDecimal.ZERO.setScale(2);
        if (userMoney == null) {
            return defaultVal;
        }
        BigDecimal codeNum = userMoney.getCodeNum() == null ? defaultVal : userMoney.getCodeNum();
        //打码量为0时才有可提现金额
        if (BigDecimal.ZERO.compareTo(codeNum) == 0) {
            BigDecimal money = userMoney.getMoney() == null ? defaultVal : userMoney.getMoney();
            return money.setScale(2, BigDecimal.ROUND_HALF_UP);
        }
        return defaultVal;
    }

    /*
    * 检测参数
    * */
    public String checkParams(String withdrawPwd, BigDecimal decMoney, User user){
        //判断密码是否正确
        if(!withdrawPwd.equals(user.getWithdrawPassword())){
            return "交易密码错误";
        }
        BigDecimal withdrawMoney = getWithdrawMoneyByUserId(user.getId());
        log.info("bigdecimal is devid is {}",decMoney.compareTo(withdrawMoney));

        //判断是否大于可提金额
        if(decMoney.compareTo(withdrawMoney)>=0){
            return "超过可提金额";
        }
        return null;
    }

    /*
    * 进行转账
    * */
    @Transactional
    public User processWithdraw(BigDecimal money, String bankId,Long userId){
        User user = userService.findById(userId);
        WithdrawOrder withdrawOrder = getWidrawOrder(money,bankId,userId);
        withdrawOrderService.saveOrder(withdrawOrder);
        UserMoney userMoney = userMoneyService.findUserByUserIdUseLock(userId);
        log.info("money is {}, draw money is {}",money,userMoney.getMoney());
        userMoneyService.subMoney(userId,money);
        user.setWithdrawMoney(userMoney.getMoney().subtract(money));
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

//    @Transactional
//    public ResponseEntity updateWithdrawAndUser(WithdrawOrder withdrawOrder) {
//        Long userId = withdrawOrder.getUserId();
//        //对用户数据进行行锁
//        UserMoney userMoney = userMoneyService.findUserByUserIdUseLock(userId);
//        if (userMoney == null) {
//            return ResponseUtil.custom("用户不存在");
//        }
//        userMoney.setMoney(userMoney.getMoney().add(withdrawOrder.getWithdrawMoney()));
//        WithdrawOrder withdraw = withdrawOrderService.saveOrder(withdrawOrder);
//        log.info("user sum money is {}, add withdrawMoney is {}",userMoney.getMoney(), withdrawOrder.getWithdrawMoney());
//        userMoneyService.save(userMoney);
//        return ResponseUtil.success(withdraw);
//    }
    @Transactional
    public ResponseEntity updateWithdrawAndUser(Long id, Integer status) {
        WithdrawOrder withdrawOrder = withdrawOrderService.findUserByIdUseLock(id);
        if(withdrawOrder == null && withdrawOrder.getStatus() != 0){
            return ResponseUtil.custom("订单已被处理");
        }
        //提现通过或其他
        withdrawOrder.setStatus(status);
        if(status != Constants.WITHDRAW_REFUSE){
            withdrawOrderService.saveOrder(withdrawOrder);
            return ResponseUtil.success();
        }
        Long userId = withdrawOrder.getUserId();
        //对用户数据进行行锁
        UserMoney userMoney = userMoneyService.findUserByUserIdUseLock(userId);
        if (userMoney == null) {
            return ResponseUtil.custom("用户钱包不存在");
        }
        userMoney.setMoney(userMoney.getMoney().add(withdrawOrder.getWithdrawMoney()));
        WithdrawOrder withdraw = withdrawOrderService.saveOrder(withdrawOrder);
        log.info("user sum money is {}, add withdrawMoney is {}",userMoney.getMoney(), withdrawOrder.getWithdrawMoney());
        userMoneyService.save(userMoney);
        return ResponseUtil.success(withdraw);
    }
}

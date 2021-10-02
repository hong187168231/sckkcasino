package com.qianyi.casinocore.business;

import com.qianyi.casinocore.enums.AccountChangeEnum;
import com.qianyi.casinocore.model.*;
import com.qianyi.casinocore.repository.BankcardsRepository;
import com.qianyi.casinocore.service.*;
import com.qianyi.casinocore.vo.AccountChangeVo;
import com.qianyi.modulecommon.Constants;
import com.qianyi.modulecommon.executor.AsyncService;
import com.qianyi.modulecommon.reponse.ResponseCode;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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

    @Autowired
    private AmountConfigService amountConfigService;

    @Autowired
    @Qualifier("accountChangeJob")
    AsyncService asyncService;

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
    @Transactional
    public BigDecimal getWithdrawMoneyByUserId(Long userId) {
        UserMoney userMoney = userMoneyService.findUserByUserIdUseLock(userId);
        BigDecimal defaultVal = BigDecimal.ZERO.setScale(2);
        if (userMoney == null) {
            return defaultVal;
        }
        BigDecimal codeNum = userMoney.getCodeNum();
        //打码量为0时才有可提现金额
        if (codeNum != null && BigDecimal.ZERO.compareTo(codeNum) == 0) {
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
    public ResponseEntity processWithdraw(BigDecimal money, String bankId,Long userId){
        if (money == null) {
            return ResponseUtil.custom("提现金额不允许为空");
        }
        UserMoney userMoney = userMoneyService.findUserByUserIdUseLock(userId);
        if (userMoney == null || userMoney.getCodeNum() == null || userMoney.getMoney() == null) {
            return ResponseUtil.custom("用户钱包不存在");

        }
        Integer count = withdrawOrderService.countByUserIdAndStatus(userId,0);
        if (count > 0) {
            return ResponseUtil.custom("您有一笔提款订单正在审核,请交易完成后再提交");
        }
        BigDecimal codeNum = userMoney.getCodeNum();
        //打码量未清0没有可提现金额
        if (codeNum.compareTo(BigDecimal.ZERO) == 1) {
            return ResponseUtil.custom("当前用户可提现金额为0");
        }
        BigDecimal withdrawMoney = userMoney.getMoney();
        if (money.compareTo(withdrawMoney) == 1) {
            return ResponseUtil.custom("超过可提金额");
        }
        //查询提现金额限制
        AmountConfig amountConfig = amountConfigService.findAmountConfigById(2L);
        if (amountConfig != null) {
            BigDecimal minMoney = amountConfig.getMinMoney();
            BigDecimal maxMoney = amountConfig.getMaxMoney();
            if (minMoney != null && money.compareTo(minMoney) == -1) {
                return ResponseUtil.custom("提现金额小于最低提现金额,最低提现金额为:" + minMoney);
            }
            if (maxMoney != null && money.compareTo(maxMoney) == 1) {
                return ResponseUtil.custom("提现金额大于最高提现金额,最高提现金额为:" + maxMoney);
            }
        }
        WithdrawOrder withdrawOrder = getWidrawOrder(money,bankId,userId);
        withdrawOrderService.saveOrder(withdrawOrder);
        log.info("money is {}, draw money is {}",money,userMoney.getMoney());
        userMoneyService.subMoney(userId,money);
        userMoney.setMoney(userMoney.getMoney().subtract(money));
        //账变中心记录账变
        AccountChangeVo vo=new AccountChangeVo();
        vo.setUserId(userId);
        vo.setChangeEnum(AccountChangeEnum.WITHDRAW_APPLY);
        vo.setAmount(money.negate());
        vo.setAmountBefore(userMoney.getMoney());
        vo.setAmountAfter(userMoney.getMoney().subtract(money));
        asyncService.executeAsync(vo);
        return ResponseUtil.success(userMoney);
    }

    private WithdrawOrder getWidrawOrder(BigDecimal money, String bankId, Long userId){
        WithdrawOrder withdrawOrder = new WithdrawOrder();
        withdrawOrder.setWithdrawMoney(money);
        withdrawOrder.setBankId(bankId);
        withdrawOrder.setUserId(userId);
        withdrawOrder.setNo(orderService.getOrderNo());
        withdrawOrder.setStatus(0);
        withdrawOrder.setRemitType(1);
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
    //后台直接下分
    @Transactional
    public ResponseEntity updateWithdrawAndUser(Long userId, BigDecimal withdrawMoney,String bankId) {
        UserMoney userMoney = userMoneyService.findUserByUserIdUseLock(userId);
        if(userMoney == null){
            return ResponseUtil.custom("用户钱包不存在");
        }
        if (userMoney.getMoney().compareTo(withdrawMoney)<=0){
            return ResponseUtil.custom("余额不足");
        }
        WithdrawOrder withdrawOrder = new WithdrawOrder();
        withdrawOrder.setWithdrawMoney(withdrawMoney);
        withdrawOrder.setPracticalAmount(withdrawMoney);
        withdrawOrder.setServiceCharge(BigDecimal.ZERO);
        withdrawOrder.setBankId(bankId);
        withdrawOrder.setUserId(userId);
        withdrawOrder.setNo(orderService.getOrderNo());
        withdrawOrder.setStatus(Constants.withdrawOrder_success);
        withdrawOrderService.saveOrder(withdrawOrder);
        BigDecimal amountBefore = userMoney.getMoney();
        BigDecimal money = amountBefore.subtract(withdrawMoney);
        userMoney.setMoney(money);
        userMoneyService.save(userMoney);
        log.info("后台直接下分userId {} 订单号 {} withdrawMoney is {}, money is {}",userMoney.getUserId(),withdrawOrder.getNo(),withdrawMoney, money);
        //记录用户账变
        this.saveAccountChang(AccountChangeEnum.SUB_CODE,userMoney.getUserId(),withdrawMoney,amountBefore,money,withdrawOrder.getNo());
        return ResponseUtil.success(userMoney);
    }

    @Transactional
    public ResponseEntity updateWithdrawAndUser(Long id, Integer status) {
        WithdrawOrder withdrawOrder = withdrawOrderService.findUserByIdUseLock(id);
        if(withdrawOrder == null || withdrawOrder.getStatus() != 0){
            return ResponseUtil.custom("订单已被处理");
        }
        //提现通过或其他
//        if(status == Constants.withdrawOrder_freeze){//冻结提现金额
//            withdrawOrder.setStatus(status);
//            withdrawOrderService.saveOrder(withdrawOrder);
//            return ResponseUtil.success();
//        }
        Long userId = withdrawOrder.getUserId();
        BigDecimal money = withdrawOrder.getWithdrawMoney();
        if(status == Constants.WITHDRAW_PASS){//通过提现审核的计算手续费
            AmountConfig amountConfig = amountConfigService.findAmountConfigById(2L);
            withdrawOrder.setServiceCharge(BigDecimal.ZERO);
            withdrawOrder.setPracticalAmount(money);
            if (amountConfig != null){
                //得到手续费
                BigDecimal serviceCharge = amountConfig.getServiceCharge(money);
                BigDecimal practicalAmount = withdrawOrder.getWithdrawMoney().subtract(serviceCharge);
                withdrawOrder.setServiceCharge(serviceCharge);
                withdrawOrder.setPracticalAmount(practicalAmount);
            }
            withdrawOrder.setStatus(status);
            log.info("通过提现userId {} 订单号 {} withdrawMoney is {}, practicalAmount is {}",withdrawOrder.getUserId(),withdrawOrder.getNo(),money, withdrawOrder.getPracticalAmount());
            return ResponseUtil.success();
        }
        //对用户数据进行行锁
        UserMoney userMoney = userMoneyService.findUserByUserIdUseLock(userId);
        if (userMoney == null) {
            return ResponseUtil.custom("用户钱包不存在");
        }
        withdrawOrder.setStatus(status);
        BigDecimal amountBefore = userMoney.getMoney();
        userMoney.setMoney(userMoney.getMoney().add(money));
        userMoneyService.save(userMoney);
        //记录用户账变
        this.saveAccountChang(AccountChangeEnum.WITHDRAWDEFEATED_CODE,userMoney.getUserId(),money,amountBefore,userMoney.getMoney(),withdrawOrder.getNo());
        log.info("拒绝提现userId {} 订单号 {} withdrawMoney is {}, money is {}",userMoney.getUserId(),withdrawOrder.getNo(),money, userMoney.getMoney());
        return ResponseUtil.success();
    }
    private void saveAccountChang(AccountChangeEnum changeEnum, Long userId, BigDecimal amount, BigDecimal amountBefore,
                                  BigDecimal amountAfter,String orderNo){
        AccountChangeVo vo=new AccountChangeVo();
        vo.setUserId(userId);
        vo.setOrderNo(orderNo);
        vo.setChangeEnum(changeEnum);
        vo.setAmount(amount);
        vo.setAmountBefore(amountBefore);
        vo.setAmountAfter(amountAfter);
        asyncService.executeAsync(vo);
    }
    @Transactional
    public void save(User user) {
        userService.save(user);
    }
}

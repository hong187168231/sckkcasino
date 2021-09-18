package com.qianyi.casinocore.business;

import com.qianyi.casinocore.model.ChargeOrder;
import com.qianyi.casinocore.model.CollectionBankcard;
import com.qianyi.casinocore.model.User;
import com.qianyi.casinocore.model.UserMoney;
import com.qianyi.casinocore.service.*;
import com.qianyi.modulecommon.reponse.ResponseEntity;
import com.qianyi.modulecommon.reponse.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ChargeBusiness {

    @Autowired
    private CollectionBankcardService collectionBankcardService;

    @Autowired
    private ChargeOrderService chargeOrderService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;
    @Autowired
    private UserMoneyService userMoneyService;
    public List<CollectionBankcard> getCollectionBankcards(){
        return collectionBankcardService.getCollectionBandcards();
    }


    public ChargeOrder submitOrder(String chargeAmount,Integer remitType,String remitterName,Long userId){

        BigDecimal decChargeAmount = new BigDecimal(chargeAmount);

        ChargeOrder chargeOrder = getChargeOrder(decChargeAmount,remitType,remitterName,userId);
        return chargeOrderService.saveOrder(chargeOrder);
    }

    private ChargeOrder getChargeOrder(BigDecimal chargeAmount,Integer remitType,String remitterName,Long userId){
        ChargeOrder chargeOrder = new ChargeOrder();
        chargeOrder.setOrderNo(orderService.getOrderNo());
        chargeOrder.setChargeAmount(chargeAmount);
        chargeOrder.setStatus(0);
        chargeOrder.setRemitter(remitterName);
        chargeOrder.setUserId(userId);
        chargeOrder.setRemitType(remitType);
        chargeOrder.setRemark("");
        return chargeOrder;
    }
    /**
     * 管理后台充值账变
     */
    @Transactional
    public ResponseEntity updateChargeOrderAndUser(ChargeOrder chargeOrder) {
        Long userId = chargeOrder.getUserId();
        User user = userService.findUserByIdUseLock(userId);
        if (user == null) {
            return ResponseUtil.custom("用户不存在");
        }
        UserMoney userMoney = userMoneyService.findUserByUserIdUseLock(userId);
        if(userMoney == null){
            return ResponseUtil.custom("用户额度表不存在");
        }
        log.info("账变开始orderNo is {},chargeAmount is {} userMoney is {}",chargeOrder.getOrderNo(),chargeOrder.getChargeAmount(),userMoney.getMoney());
        BigDecimal money = userMoney.getMoney() == null ? BigDecimal.ZERO : userMoney.getMoney();
        userMoney.setMoney(money);
        userMoney.setMoney(userMoney.getMoney().add(chargeOrder.getChargeAmount()));
        chargeOrderService.saveOrder(chargeOrder);
        userMoneyService.save(userMoney);
        log.info("账变结束orderNo is {},money is {}",chargeOrder.getOrderNo(),userMoney.getMoney());
        return ResponseUtil.success(chargeOrder);
    }
    /**
     * 管理后台定时清除超时充值订单
     */
    @Transactional
    public void updateChargeOrderStatus(Long id){
        ChargeOrder chargeOrder = chargeOrderService.findChargeOrderByIdUseLock(id);
        if (chargeOrder !=null && chargeOrder.getStatus()!=0)
            return;
        chargeOrder.setStatus(3);
        chargeOrderService.saveOrder(chargeOrder);
    }

}

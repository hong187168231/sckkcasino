package com.qianyi.casinocore.business;

import com.qianyi.casinocore.model.ChargeOrder;
import com.qianyi.casinocore.model.CollectionBankcard;
import com.qianyi.casinocore.service.ChargeOrderService;
import com.qianyi.casinocore.service.CollectionBankcardService;
import com.qianyi.casinocore.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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


}

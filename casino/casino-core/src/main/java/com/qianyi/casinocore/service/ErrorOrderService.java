package com.qianyi.casinocore.service;

import com.qianyi.casinocore.business.SupplementBusiness;
import com.qianyi.casinocore.enums.AccountChangeEnum;
import com.qianyi.casinocore.model.ErrorOrder;
import com.qianyi.casinocore.repository.ErrorOrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
public class ErrorOrderService {

    @Autowired
    private ErrorOrderRepository errorOrderRepository;
    @Autowired
    private SupplementBusiness supplementBusiness;

    @Async
    public void syncSaveErrorOrder(String thirdAccount, Long userId, String account, String orderNo, BigDecimal money, AccountChangeEnum ChangeEnum, String platform) {
        ErrorOrder errorOrder = new ErrorOrder();
        errorOrder.setUserId(userId);
        errorOrder.setUserName(account);
        errorOrder.setStatus(0);
        errorOrder.setOrderNo(orderNo);
        errorOrder.setMoney(money.abs());
        errorOrder.setType(ChangeEnum.getType());
        errorOrder.setPlatform(platform);
        errorOrderRepository.save(errorOrder);
        //尝试3次补单
        supplementBusiness.trySupplement(errorOrder, thirdAccount);
    }

    public void save(ErrorOrder errorOrder) {
        errorOrderRepository.save(errorOrder);
    }

}

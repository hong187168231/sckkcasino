package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.CodeNumChange;
import com.qianyi.casinocore.repository.CodeNumChangeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class CodeNumChangeService {
    @Autowired
    private CodeNumChangeRepository codeNumChangeRepository;

    public void save(Long userId, Long gameRecordId, BigDecimal amount, BigDecimal amountBefore, BigDecimal amountAfter) {
        CodeNumChange codeNumChange=new CodeNumChange();
        codeNumChange.setUserId(userId);
        codeNumChange.setGameRecordId(gameRecordId);
        codeNumChange.setAmount(amount);
        codeNumChange.setAmountBefore(amountBefore);
        codeNumChange.setAmountAfter(amountAfter);
        codeNumChangeRepository.save(codeNumChange);
    }
}

package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.Bankcards;
import com.qianyi.casinocore.repository.BankcardsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class BankcardsService {

    @Autowired
    BankcardsRepository bankcardsRepository;

    public List<Bankcards> findBankcardsByUserId(Long userId){
        return bankcardsRepository.findBankcardsByUserIdOrderByDefaultCardDesc(userId);
    }

    public Bankcards boundCard(Bankcards bankcards){
        return bankcardsRepository.save(bankcards);
    }

    public Bankcards findBankCardsInByUserId(Long userId) {
        return bankcardsRepository.findFirstByUserId(userId);
    }

    public int countByUserId(Long userId){
        return bankcardsRepository.countByUserId(userId);
    }
}

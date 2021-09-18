package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.CollectionBankcard;
import com.qianyi.casinocore.model.SysUser;
import com.qianyi.casinocore.repository.CollectionBankCardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CollectionBankcardService {

    @Autowired
    private CollectionBankCardRepository collectionBankCardRepository;

    public List<CollectionBankcard> getCollectionBandcards(){
        return collectionBankCardRepository.findAll();
    }

    public CollectionBankcard findByBankNo(String bankNo) {
        return collectionBankCardRepository.findByBankNo(bankNo);
    }

    public void save(CollectionBankcard bankcard) {
        collectionBankCardRepository.save(bankcard);
    }

    public CollectionBankcard findById(Long id) {
        Optional<CollectionBankcard> optional = collectionBankCardRepository.findById(id);
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }

    public List<CollectionBankcard> findAll() {
        return collectionBankCardRepository.findAll();
    }
}

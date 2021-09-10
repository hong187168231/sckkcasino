package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.CollectionBankcard;
import com.qianyi.casinocore.repository.CollectionBankCardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CollectionBankcardService {

    @Autowired
    private CollectionBankCardRepository collectionBankCardRepository;

    public List<CollectionBankcard> getCollectionBandcards(){
        return collectionBankCardRepository.findAll();
    }
}

package com.qianyi.casinocore.service;

import com.qianyi.casinocore.model.UserBalanceBlock;
import com.qianyi.casinocore.repository.UserBalanceBlockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class UserBalanceBlockService {

    @Autowired
    private UserBalanceBlockRepository userBalanceBlockRepository;

    public UserBalanceBlock findByUserName(String userName) {
        List<UserBalanceBlock> userBalanceBlockList = userBalanceBlockRepository.findByUserName(userName);
        if(userBalanceBlockList != null && userBalanceBlockList.size() > 0){
            return userBalanceBlockList.get(0);
        }else{
            return null;
        }
    }

    public void updateUserBalanceBlock(UserBalanceBlock userBalanceBlock) {
        userBalanceBlockRepository.updateStatus(userBalanceBlock.getStatus() , userBalanceBlock.getId());
    }

    public void save(UserBalanceBlock userBalanceBlock) {
        userBalanceBlockRepository.save(userBalanceBlock);
    }
}
